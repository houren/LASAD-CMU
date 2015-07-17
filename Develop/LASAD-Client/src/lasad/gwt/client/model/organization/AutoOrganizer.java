package lasad.gwt.client.model.organization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

import lasad.gwt.client.communication.helper.ActionFactory;
import lasad.gwt.client.communication.LASADActionSender;
import lasad.gwt.client.communication.LASADActionReceiver;

import lasad.gwt.client.LASAD_Client;

import lasad.gwt.client.model.argument.MVController;
import lasad.gwt.client.model.ElementInfo;

// I'm aware that importing from the same package is unnecessary, but I do it in case I chnage the package of this class ever.
import lasad.gwt.client.model.organization.ArgumentModel;
import lasad.gwt.client.model.organization.ArgumentThread;
import lasad.gwt.client.model.organization.LinkedBox;
import lasad.gwt.client.model.organization.GroupedBoxesStatusCodes;
import lasad.gwt.client.model.organization.OrganizerLink;

import lasad.gwt.client.logger.Logger;
import lasad.gwt.client.ui.workspace.graphmap.AbstractGraphMap;

import lasad.shared.communication.objects.ActionPackage;

import java.io.StringWriter;
import java.io.PrintWriter;

/**
 * An AutoOrganizer can clean up the user's workspace into a clearer visual representation of the argument. It can also update links
 * in ArgumentMap representations where a type of relation can create groups of boxes. The overall map organizing function,
 * accordingly called organizeMap(), is only called when the user clicks the corresponding button on the ArgumentMapMenuBar. Though originally
 * built for maps using Mara Harrell's template, this class can be applied to any map from any template.  There is a model that updates
 * with every change to the map.  Thus, we don't need to start from scratch and gather components every time these methods are called.
 * @author Kevin Loughlin and Darlan Santana Farias
 * @since 12 June 2015, Updated 6 July 2015
 */
public class AutoOrganizer
{

	// The maximum number of siblings (grouped boxes) a box can have
	private final int MAX_SIBLINGS = 2;

	// Perfectly centered box location
	private final double CENTER_X = 2400.0;
	private final double CENTER_Y = CENTER_X;

	// The map that this instance of AutoOrganizer corresponds to
	private AbstractGraphMap map;
	
	// Keeps track of the boxes visited during function execution, must be cleaned before used
	private HashSet<LinkedBox> visited = new HashSet<LinkedBox>();
	
	// Keeps track of the boxes visited as well as the order in which the boxes are inserted
	private LinkedHashSet<LinkedBox> visitedLHSet = new LinkedHashSet<LinkedBox>();

	// Instances of autoOrganizer: one per map.  String is mapID.
	private static HashMap<String, AutoOrganizer> instances = new HashMap<String, AutoOrganizer>();

	// For sending map updates to the server
	private LASADActionSender communicator = LASADActionSender.getInstance();
	private ActionFactory actionBuilder = ActionFactory.getInstance();

	/**
	 *	The only constructor that should be used
	 *	@param map - The argument map for this instance of AutoOrganizer
	 */
	public AutoOrganizer(AbstractGraphMap map)
	{
		this.map = map;
		instances.put(map.getID(), this);
	}

	// Don't use default constructor
	private AutoOrganizer()
	{
	}

	/**
	 * Returns the AutoOrganizer instance relating to the parameter mapID
	 * @param mapID - The ID for the map that corresponds to an instance of AutoOrganizer
	 * @return The autoOrganizer instance corresponding to mapID, or null if the instance does not exist
	 */
	public static AutoOrganizer getInstanceByMapID(String mapID)
	{
		return instances.get(mapID);
	}

	public void organizeMap()
	{
		try
		{
			boolean isOrganizeTopToBottom = map.getMyViewSession().getController().getMapInfo().isOrganizeTopToBottom();
			Logger.log("Entered organizeMap", Logger.DEBUG);

			ArgumentModel argModel = ArgumentModel.getInstanceByMapID(map.getID());
			for (ArgumentThread argThread : argModel.getArgThreads())
			{
				// Because this is recursive we only need first box, then can break; boxes will now be on grid
				argThread.organizeGrid(isOrganizeTopToBottom);
			}

			int numThreads = argModel.getArgThreads().size();
			HashMap<Integer, ArgumentThread> threadsIndexed = new HashMap<Integer, ArgumentThread>();

			int threadCount = 0;
			for (ArgumentThread argThread : argModel.getArgThreads())
			{
				threadsIndexed.put(new Integer(threadCount), argThread);
				threadCount++;
			}

			int maxHeight = Integer.MIN_VALUE;
			int maxWidth = Integer.MIN_VALUE;
			for (ArgumentThread argThread : argModel.getArgThreads())
			{
				for (LinkedBox box : argThread.getGrid())
				{
					if (box.getHeight() > maxHeight)
					{
						maxHeight = box.getHeight();
					}

					if (box.getWidth() > maxWidth)
					{
						maxWidth = box.getWidth();
					}
				}
			}

			int half = threadCount / 2;
			int gridOffsetWidth = 0;

			int totalModelWidth = 0;
			int totalModelHeight = 0;
			for (ArgumentThread thread : argModel.getArgThreads())
			{
				totalModelWidth += thread.getMaxWidthLevelOnGrid();
				totalModelHeight += thread.getMaxHeightLevelOnGrid();
			}

			final double START_X = CENTER_X - (totalModelWidth * maxWidth) / 2.0;
			final double START_Y = CENTER_Y + (totalModelHeight * maxHeight) / 2.0;

			for (int counter = 0; counter < threadCount; counter++)
			{
				ArgumentThread thread = threadsIndexed.get(counter);
				int maxHeightLevel = thread.getMaxHeightLevelOnGrid();
				for (LinkedBox box : thread.getGrid())
				{
					if (thread.getBoxes().contains(box))
					{
						Logger.log(box.toStringShort(), Logger.DEBUG);
						sendUpdatePositionToServer(box, START_X + gridOffsetWidth + box.getWidthLevel() * (maxWidth * 3.0) / 4.0 - box.getWidth() / 2.0, START_Y - box.getHeightLevel() * (maxHeight / 2.0));
					}
				}

				gridOffsetWidth += thread.getMaxWidthLevelOnGrid();
			}

			centerMap();

			Logger.log("Exiting organizeMap", Logger.DEBUG);
		}
		catch (Exception e)
		{
			Logger.log("EXCEPTION THROWN", Logger.DEBUG);
			Logger.log("toSTRING: " + e.toString(), Logger.DEBUG);
			Logger.log("MESSAGE: " + e.getMessage(), Logger.DEBUG);
			Logger.log("STACK TRACE", Logger.DEBUG);
			for (StackTraceElement stackFrame : e.getStackTrace())
			{
				Logger.log(stackFrame.toString(), Logger.DEBUG);
			}
		}
	}

	/**
	 * Updates the sibling boxes (i.e. grouped boxes) related to the creation of a new link.
	 * For example, if box A is attached to box B via a group link, and box B gets a new child, then box A should also
	 * have a relation pointing to that child.  This method uses the private helper method "updateRecursive" to do its dirty work.
	 * @param link - The new, user-drawn link from which we must search for possibly necessary additional new links
	 */
	public void updateSiblingLinks(OrganizerLink link)
	{
		//Logger.log("Entered updateSiblingLinks", Logger.DEBUG);
		HashSet<OrganizerLink> linksToCreate = new HashSet<OrganizerLink>();

		// The original link data
		LinkedBox origStartBox = link.getStartBox();
		LinkedBox origEndBox = link.getEndBox();
		String linkType = link.getType();

		if (link.getConnectsGroup())
		{
			HashSet<OrganizerLink> origStartChildLinks = origStartBox.getChildLinks();
			HashSet<LinkedBox> origStartChildBoxes = origStartBox.getChildBoxes();

			HashSet<OrganizerLink> origEndChildLinks = origEndBox.getChildLinks();
			HashSet<LinkedBox> origEndChildBoxes = origEndBox.getChildBoxes();

			for (OrganizerLink origStartChildLink : origStartChildLinks)
			{
				LinkedBox newChildBox = origStartChildLink.getEndBox();
				if (!origEndChildBoxes.contains(newChildBox))
				{
					OrganizerLink newLink = new OrganizerLink(origEndBox, newChildBox, origStartChildLink.getType(), origStartChildLink.getConnectsGroup());
					linksToCreate.add(newLink);
				}
			}

			for (OrganizerLink origEndChildLink : origEndChildLinks)
			{
				LinkedBox newChildBox = origEndChildLink.getEndBox();
				if (!origStartChildBoxes.contains(newChildBox))
				{
					OrganizerLink newLink = new OrganizerLink(origStartBox, newChildBox, origEndChildLink.getType(), origEndChildLink.getConnectsGroup());
					linksToCreate.add(newLink);
				}
			}
		}
		else
		{
			HashSet<LinkedBox> origStartSiblingBoxes = origStartBox.getSiblingBoxes();

			// We only need the first one, hence why I break, but I use this for loop in case origStartSiblingBoxes is empty so that it will skip
			for (LinkedBox origStartSiblingBox : origStartSiblingBoxes)
			{
				linksToCreate = updateRecursive(origStartSiblingBox, origEndBox, link, new VisitedAndLinksHolder()).getLinks();
				break;
			}
		}

		addLinksToVisual(linksToCreate);
	}

	/**
	 *	Holds the visited boxes and links accumulated in a recursive method, with the benefit of one data structure
	 *	For use with updateSiblingLinks and updateRecursive
	 */
	class VisitedAndLinksHolder
	{
		private HashSet<LinkedBox> visited;
		private HashSet<OrganizerLink> links;

		public VisitedAndLinksHolder()
		{
			visited = new HashSet<LinkedBox>();
			links = new HashSet<OrganizerLink>();
		}

		public void addVisited(LinkedBox box)
		{
			visited.add(box);
		}

		public void addLink(OrganizerLink link)
		{
			links.add(link);
		}

		public HashSet<LinkedBox> getVisited()
		{
			return visited;
		}

		public HashSet<OrganizerLink> getLinks()
		{
			return links;
		}
	}

	/*	
	 *	Recursively checks the siblings of a given start box to see if they need to be updated with a new relation.
	 *	Keeps track of boxes visited so that the method will eventually end.
	 *	@param startBox - The box from which we might make a link
	 *	@param END_BOX - The constant box to which we will be connecting
	 *	@param LINK_TYPE - The type of connection we will make if necessary
	 *  @param holder - The visited boxes and the links that need to be created, should be initialized as empty
	*/
	private VisitedAndLinksHolder updateRecursive(LinkedBox startBox, LinkedBox END_BOX, OrganizerLink LINK_DATA, VisitedAndLinksHolder holder)
	{
		//Logger.log("Entered update recursive.", Logger.DEBUG);
		if (!holder.getVisited().contains(startBox))
		{
			holder.addVisited(startBox);
			if (!startBox.getChildBoxes().contains(END_BOX))
			{
				holder.addLink(new OrganizerLink(startBox, END_BOX, LINK_DATA.getType(), LINK_DATA.getConnectsGroup()));
			}
			for (LinkedBox siblingBox : startBox.getSiblingBoxes())
			{
				holder = updateRecursive(siblingBox, END_BOX, LINK_DATA, holder);
			}
		}

		return holder;
	}

	/**
	 *	Determines whether or not the passed group OrganizerLink can be created on the map, which depends on invariants such as
	 *	the maximum number of permitted siblings per box, both boxes being the same type, groupable, and no conflicting links between siblings.
	 *	@param link - The link to check for valid creation
	 *	@return Success/error integer code: 0 for success, greater than 0 for error code
	 */
	public int groupedBoxesCanBeCreated(OrganizerLink link)
	{
		//Logger.log("Entered GroupedBoxesCanBeCreated", Logger.DEBUG);
		LinkedBox startBox = link.getStartBox();
		LinkedBox endBox = link.getEndBox();

		// Boxes shouldn't be null
		if (startBox == null || endBox == null)
		{
			return GroupedBoxesStatusCodes.NULL_BOX;
		}

		// Can't create a link to self
		if (startBox.equals(endBox))
		{
			return GroupedBoxesStatusCodes.SAME_BOX;
		}

		// Checks if they are of groupable type
		if (startBox.getCanBeGrouped())
		{
			// Checks that both boxes are of same type, else return error code
			if (startBox.getType().equalsIgnoreCase(endBox.getType()))
			{
				// Checks that they both have fewer than 2 siblings, else return error code
				if (startBox.getNumSiblings() < MAX_SIBLINGS && endBox.getNumSiblings() < MAX_SIBLINGS)
				{
					// See this.isCompatible for what the method within this if statement checks, else return error code
					if (this.isCompatible(startBox, endBox))
					{
						return GroupedBoxesStatusCodes.SUCCESS;
					}
					else
					{
						return GroupedBoxesStatusCodes.TWO_WAY_LINK;
					}
				}
				else
				{
					return GroupedBoxesStatusCodes.TOO_MANY_SIBS;
				}
			}
			else
			{
				return GroupedBoxesStatusCodes.NOT_SAME_TYPE;
			}
		}
		else
		{
			return GroupedBoxesStatusCodes.CANT_BE_GROUPED;
		}
	}

	/*	
	 *	Checks that there aren't existing invalid connections between the startBoxAndExtSibs group and the end Box children.
	 *	@param startBoxAndExtSibs - The startBox for the new link and its extended siblings
	 *	@param endBox - The end box for the new link
	 *	@return true if there are no conflicts, false if the grouped boxes should not be created
	 */
	private boolean isCompatible(LinkedBox startBox, LinkedBox endBox)
	{
		//Logger.log("INSIDE isCompatible", Logger.DEBUG);
		HashSet<LinkedBox> startChildBoxes = startBox.getChildBoxes();
		HashSet<LinkedBox> endChildBoxes = endBox.getChildBoxes();
		
		for (LinkedBox startChildBox : startChildBoxes)
		{
			if (endBox.hasNonChildLinkWith(startChildBox))
			{
				return false;
			}
		}

		for (LinkedBox endChildBox : endChildBoxes)
		{
			if (startBox.hasNonChildLinkWith(endChildBox))
			{
				return false;
			}
		}

		return true;
	}

	/*
	 *	Updates a box position on the map, might need to add to redraw the relations because we're losing arrow heads on them for some reason.
	 *	@param box - The box whose position we will update
	 *	@param x - The new x coordinate for the box
	 *	@param y - The new y coordinate for the box
	 */
	private void sendUpdatePositionToServer(LinkedBox box, double x, double y)
	{
		int intX = (int) Math.round(x);
		int intY = (int) Math.round(y);
		ArgumentModel.getInstanceByMapID(map.getID()).getBoxByBoxID(box.getBoxID()).setPosition(intX, intY);
		communicator.sendActionPackage(actionBuilder.updateBoxPosition(map.getID(), box.getBoxID(), intX, intY));
	}

	private void centerMap()
	{
		ArgumentModel argModel = ArgumentModel.getInstanceByMapID(map.getID());
		// Scroll the map to the center of all boxes
		int xsum = 0;
		int ysum = 0;
		int numberOfObjects = 0;

		for (ArgumentThread argThread : argModel.getArgThreads())
		{
			for (LinkedBox box : argThread.getBoxes())
			{
				xsum += box.getXLeft() + box.getWidth() / 2;
				ysum += box.getYTop() + box.getHeight() / 2;
				numberOfObjects++;
			}
		}

		if (numberOfObjects > 0) {
			map.getLayoutTarget().dom.setScrollLeft(xsum / numberOfObjects - map.getInnerWidth() / 2);
			map.getLayoutTarget().dom.setScrollTop(ysum / numberOfObjects - map.getInnerHeight() / 2);
		} else {
			map.getLayoutTarget().dom.setScrollLeft(map.getMapDimensionSize().width / 2 - map.getInnerWidth() / 2);
			map.getLayoutTarget().dom.setScrollTop(map.getMapDimensionSize().height / 2 - map.getInnerHeight() / 2);
		}
		//communicator.sendActionPackage(actionBuilder.centerMap(mapID));
		Logger.log("Sent centerMap to server", Logger.DEBUG);
	}

	/*
	 *	Wraps each new link to be created as an actionPackage, which is sent to server to be added to the model and map.
	 */
	private void addLinksToVisual(HashSet<OrganizerLink> linksToCreate)
	{
		//Logger.log("Entered addLinksToVisual", Logger.DEBUG);	
		String elementType = "relation";

		MVController controller = LASAD_Client.getMVCController(map.getID());

		ElementInfo linkInfo = new ElementInfo();
		linkInfo.setElementType(elementType);

		for (OrganizerLink link : linksToCreate)
		{
			// a better name for element ID here would be subtype, as in, what kind of relation.  Alas, I didn't name it.
			linkInfo.setElementID(link.getType());

			String startBoxStringID = Integer.toString(link.getStartBox().getBoxID());
			String endBoxStringID = Integer.toString(link.getEndBox().getBoxID());

			ActionPackage myPackage = actionBuilder.createLinkWithElements(linkInfo, map.getID(), startBoxStringID, endBoxStringID);
			LASADActionReceiver.getInstance().setSiblingsAlreadyUpdated(true);
			communicator.sendActionPackage(myPackage);
		}
	}

	/**
	 *	Determines if supplemental links need to be removed after the passed link is removed.  This might be necessary, for example,
	 *	if the removal of solely the passed link would result in a violation of the grouped boxes invariants (i.e. each sibling box must
	 *	link to each other sibling's children).
	 *	@param removedLink - The link already removed from the model, that provides an easy access point for other nearby links to remove
	 */
	public void determineLinksToRemove(OrganizerLink removedLink)
	{
		//ogger.log("Entered determineLinksToRemove", Logger.DEBUG);
		HashSet<OrganizerLink> linksToRemove = new HashSet<OrganizerLink>();

		if (!removedLink.getConnectsGroup())
		{
			LinkedBox removedLinkStartBox = removedLink.getStartBox();
			int numSiblings = removedLinkStartBox.getNumSiblings();
			HashSet<OrganizerLink> siblingLinks = removedLinkStartBox.getSiblingLinks();
			for (OrganizerLink link : siblingLinks)
			{
				linksToRemove.add(link);
			}
		}

		removeLinksFromVisual(linksToRemove);
	}

	/*
	 *	Helper method called by determineLinksToRemove that actually sends the actionPackage to the server, telling the server to
	 *	remove each necessary link from the map.
	 */
	private void removeLinksFromVisual(HashSet<OrganizerLink> linksToRemove)
	{
		//Logger.log("Entered removeLinksFromVisual", Logger.DEBUG);
		MVController controller = LASAD_Client.getMVCController(map.getID());

		for (OrganizerLink link : linksToRemove)
		{
			ActionPackage myPackage = actionBuilder.removeElement(map.getID(), link.getLinkID());
			LASADActionReceiver.getInstance().setLinksAlreadyRemoved(true);
			communicator.sendActionPackage(myPackage);
		}
	}

	/**
	 *	Checks to see if the removal of the passed link warrants the creation of a new Thread, and creates the thread if needed
	 *	@param removedLink - The link removed from the model
	 */
	public void createNewThreadIfNecessary(OrganizerLink removedLink)
	{
		//Logger.log("Entered createNewThreadIfNecessary", Logger.DEBUG);
		ArgumentModel argModel = ArgumentModel.getInstanceByMapID(map.getID());
		ArgumentThread startArgThread = argModel.getBoxThread(removedLink.getStartBox());
		HashSet<LinkedBox> allThreadBoxes = new HashSet<LinkedBox>(startArgThread.getBoxes());
		HashSet<LinkedBox> boxesReached = visitRecursive(removedLink.getEndBox(), new HashSet<LinkedBox>());
		if (boxesReached.size() == allThreadBoxes.size() && boxesReached.containsAll(allThreadBoxes))
		{
			// They're still in the same thread
			return;
		}
		else
		{
			ArgumentThread newThread = new ArgumentThread();
			startArgThread.removeBoxes(boxesReached);
			newThread.addBoxes(boxesReached);
			argModel.addArgThread(newThread);
		}
	}

	/**
	 *	Checks to see if the removal of the passed box warrants the removal of a new Thread, and removes the thread if needed
	 *	@param removedBox - The box removed from the model
	 */
	public void removeThreadIfNecessary(LinkedBox removedBox)
	{
		//Logger.log("Entered removeThreadIfNecessary", Logger.DEBUG);
		ArgumentModel argModel = ArgumentModel.getInstanceByMapID(map.getID());
		ArgumentThread argThread = argModel.getBoxThread(removedBox);
		if (argThread.getBoxes().size() == 0)
		{
			argModel.removeArgThread(argThread);
		}
	}

	/*
	 *	Goes through all the boxes directly/indirectly attached to box to see if we can reach all from the original thread
	 *	@param box - The box currently being visited, should be initialized as the endBox of the deleted link
	 *	@param boxesReached - The boxes so far reached, should be initialized as a new, empty HashSet
	 */
	private HashSet<LinkedBox> visitRecursive(LinkedBox box, HashSet<LinkedBox> boxesReached)
	{
		//Logger.log("Entered visit recursive.", Logger.DEBUG);
		if (!boxesReached.contains(box))
		{
			boxesReached.add(box);
			for (LinkedBox childBox : box.getChildBoxes())
			{
				boxesReached = visitRecursive(childBox, boxesReached);
			}

			for (LinkedBox parentBox : box.getParentBoxes())
			{
				boxesReached = visitRecursive(parentBox, boxesReached);
			}

			for (LinkedBox siblingBox : box.getSiblingBoxes())
			{
				boxesReached = visitRecursive(siblingBox, boxesReached);
			}
		}

		return boxesReached;
	}
}