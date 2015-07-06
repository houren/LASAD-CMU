package lasad.gwt.client.model.organization;

// Some of these import statements can be deleted, but I'm too lazy to do that right now and it's not exactly a big deal.
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ArrayList;

import lasad.gwt.client.ui.box.AbstractBox;
import lasad.gwt.client.communication.helper.ActionFactory;
import lasad.gwt.client.communication.LASADActionSender;
import lasad.gwt.client.model.argument.MVController;
import lasad.gwt.client.ui.workspace.graphmap.AbstractGraphMap;
import lasad.shared.communication.objects.parameters.ParameterTypes;
import lasad.gwt.client.logger.Logger;
import lasad.gwt.client.model.ElementInfo;

import com.extjs.gxt.ui.client.widget.Component;
import lasad.gwt.client.ui.link.AbstractLinkPanel;
import java.util.HashMap;
import java.util.Set;

import lasad.shared.communication.objects.Action;
import lasad.shared.communication.objects.ActionPackage;
import lasad.gwt.client.model.argument.MVController;
import lasad.gwt.client.model.AbstractUnspecifiedElementModel;
import lasad.gwt.client.LASAD_Client;
import lasad.shared.communication.objects.Parameter;
import lasad.gwt.client.model.argument.UnspecifiedElementModelArgument;

// I'm aware that importing from the same package is unnecessary, but I do it in case I chnage the package of this class ever.
import lasad.gwt.client.model.organization.LinkedBox;
import lasad.gwt.client.model.organization.OrganizerLink;
import lasad.gwt.client.model.organization.ArgumentThread;
import lasad.gwt.client.model.organization.ArgumentModel;

import lasad.gwt.client.communication.LASADActionReceiver;
import lasad.gwt.client.ui.workspace.LASADInfo;

/**
 * An AutoOrganizer can clean up the user's workspace into a clearer visual representation of the argument. It can also update links
 * in ArgumentMap representations where a type of relation is listed as "Linked Premises" (not case sensitive). The overall map organizing function,
 * accordingly called organizeMap(), is only called when the user clicks the corresponding button on the ArgumentMapMenuBar. Though originally
 * built for maps using Mara Harrell's template, this class can be applied to any map from any template.
 * This class, especially organizeMap, needs significant overhauls, as I have changed the structuring such that there is a model that updates
 * with every change to the map.  Thus, we don't need to start from scratch and gather components every time the map is called.
 * @author Kevin Loughlin
 * @since 12 June 2015, Updated 30 June 2015
 */

public class AutoOrganizer
{
	// Space between organized nodes
	private final int Y_SPACE = 150;
	private final int X_SPACE = 150;

	private final int MAX_SIBLINGS = 2;
	
	//Initial coordinates
	private final int INITIAL_X = 0;
	private final int INITIAL_Y = 2200;

	// The map
	private AbstractGraphMap map;

	/*	The map's components (i.e. instances of AbstractBox and AbstractLinkPanel, I would imagine child elements would be stored here)
		but they're not needed */
	private List<Component> mapComponents;

	// Perfectly centered box location
	private final int CENTER_X = 2400;
	private final int CENTER_Y = CENTER_X;

	// This is hack for LinkID, need to find to a way to get next available ID
	private static int counterID = -1;

	// All the boxes on the map
	private HashSet<LinkedBox> boxes = new HashSet<LinkedBox>();

	// siblingBoxes are for Mara Harrell's ontology; they are boxes attached via Linked Premises relations
	private HashSet<LinkedBox> siblingBoxes = new HashSet<LinkedBox>();

	/* rootBoxes are those that are on height level 1, i.e. they start an argument thread.  Theoretically, a map could have multiple
		threads (even if that would be poor organization, and thus that must be taken into account in the organizeMap method. */
	private HashSet<LinkedBox> rootBoxes = new HashSet<LinkedBox>();

	// All the links (relations) on a map
	private HashSet<OrganizerLink> links = new HashSet<OrganizerLink>();

	// Stores boxes that have been visited during a method call.  Important to clear at beginning of method call.
	private HashSet<LinkedBox> visited = new HashSet<LinkedBox>();
	
	// Same as the variable above, but this one keeps the order in which the boxes are inserted
	private ArrayList<LinkedBox> visitedList = new ArrayList<LinkedBox>();

	// Instances of autoOrganizer: one per map.  String is mapID.
	private static HashMap<String, AutoOrganizer> instances = new HashMap<String, AutoOrganizer>();

	// Must be cleared when updateSiblingLinks is called
	private HashSet<OrganizerLink> linksToCreate = new HashSet<OrganizerLink>();
	private HashSet<OrganizerLink> linksToRemove = new HashSet<OrganizerLink>();

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
		this.mapComponents = map.getItems();
		instances.put(map.getID(), this);
	}

	// Don't use default constructor
	private AutoOrganizer()
	{
	}

	/**
	 * Returns the AutoOrganizer instance relating to the parameter mapID
	 * @param mapID - The ID for the map that corresponds to an autoOrganizer in the instances of AutoOrganizer
	 * @return The autoOrganizer instance for mapID, or null if it does not exist
	 */
	public static AutoOrganizer getInstanceByMapID(String mapID)
	{
		return instances.get(mapID);
	}

	/* UNDER DEVELOPMENT
	 * Recursively calculates the coordinates for each box in an argument thread
	 * @param level - Indicates the height level of the 'root' parameter, should be 0 when called for an actual root
	 * @param root - the root "parent" of the argument thread
	 * @param xCoords - stores the x coordinates calculated
	 * @param yCoords - stores the y coordinates calculated
	 * @param neighborBoxes - boxes in the same height level as the root
	 * @return The maximum number of boxes in a single height level of the thread
	 */
	private int organizeThread(int level, LinkedBox root, ArrayList<Integer> xCoords, ArrayList<Integer> yCoords, LinkedHashSet<LinkedBox> neighborBoxes)
	{
		if(visitedList.contains(root) || root == null) return 0;
		
		int span;
		if(!neighborBoxes.contains(root)) span = neighborBoxes.size()+1; else span = neighborBoxes.size(); 
		
		LinkedHashSet<LinkedBox> childrenNeighbors = new LinkedHashSet<LinkedBox>();
		
		xCoords.add(new Integer(-(span-1)));
		yCoords.add(level);
			
		visitedList.add(root);
		childrenNeighbors.addAll(root.getChildBoxes());
			
		for(LinkedBox box : root.getSiblingBoxes())
		{
			xCoords.add(new Integer(xCoords.get(xCoords.size()-1)+2));
			yCoords.add(level);
				
			visitedList.add(box);
			childrenNeighbors.addAll(box.getChildBoxes());
		}
		
		for(LinkedBox box : neighborBoxes){
			if(visitedList.contains(box))
				continue;
			
			xCoords.add(new Integer(xCoords.get(xCoords.size()-1)+2));
			yCoords.add(level);
				
			visitedList.add(box);
			childrenNeighbors.addAll(box.getChildBoxes());
		}
			
		int childSpan;
		for(LinkedBox box : childrenNeighbors)
		{
			for(LinkedBox siblingBox : box.getSiblingBoxes())
			{
				if(!childrenNeighbors.contains(siblingBox))
					childrenNeighbors.add(siblingBox);
			}
		}
		
		for(LinkedBox box : childrenNeighbors)
		{
			childSpan = organizeThread(level+1, box, xCoords, yCoords, childrenNeighbors);
			
			if(childSpan > span) span = childSpan;
		}
		
		return span;		
	}

	/**
	 * UNDER DEVELOPMENT
	 * Organizes the map from bottom to top: Root "parents" start at the bottom, leading the way to children above.
	 * "sibling" boxes (i.e. boxes attached via Linked Premises) are placed adjacently.
	 */
	public void organizeMap()
	{
		Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] Running AutoOrganizer...", Logger.DEBUG);

		ArgumentModel argModel = ArgumentModel.getInstanceByMapID(map.getID());

		int yCoord = INITIAL_Y;
		int xCoord = INITIAL_X;
		int newXCoord = 0;
		int maxSpan = 0;
		int lastSpan = 0;
		int span = 0;
		int init = 0;
		int yMax = 0;
	
		ArrayList<Integer> yCoords = new ArrayList<Integer>();
		ArrayList<Integer> xCoords = new ArrayList<Integer>();
		ArrayList<ArgumentThread> threadList = new ArrayList<ArgumentThread>();
		visitedList.clear();
		
		Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] Retrieving threads...", Logger.DEBUG);
		Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] Number of threads: "+argModel.getArgThreads().size(), Logger.DEBUG);
		HashSet<LinkedBox> roots = new HashSet<LinkedBox>();
		
		argModel.updateArgThreads();
		
		for (ArgumentThread argThread : argModel.getArgThreads())
		{
			threadList.add(argThread);
			LinkedBox root = null;
			
			Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] All boxes in the thread: "+argThread, Logger.DEBUG);
			for(LinkedBox box : argThread.getRootBoxes()){
				root = box;
				break;
			}
			
			Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] Got the root: "+root, Logger.DEBUG);
			Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] Number of roots: "+argThread.getRootBoxes().size(), Logger.DEBUG);
			if(root == null) continue;
			
			span = organizeThread(0, root, xCoords, yCoords, new LinkedHashSet(argThread.getRootBoxes()));
			maxSpan += lastSpan+span;
			lastSpan = span;
			Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] Shift: "+maxSpan, Logger.DEBUG);
			Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] X Coords (before shift): "+xCoords, Logger.DEBUG);
			
			for(int i = init; i < xCoords.size(); i++)
			{
				xCoords.set(i, xCoords.get(i)+maxSpan);
			}
		
			Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] X Coords (after shift): "+xCoords, Logger.DEBUG);
			Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] Y Coords "+yCoords, Logger.DEBUG);
			
			init = xCoords.size();
			if(init-1 >= 0) 
				if(yCoords.get(init-1) > yMax) yMax = yCoords.get(init-1);
			
			//xCoord = newXCoord;
		}
		Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] Going to update positions...", Logger.DEBUG);
		int i = 0;
		int j = 0;
		for(LinkedBox box : visitedList)
		{
			xCoord = CENTER_X+(xCoords.get(i)-maxSpan/2)*X_SPACE;
			yCoord = (yMax/2-yCoords.get(i))*Y_SPACE+CENTER_Y;
			sendUpdatePositionToServer(box, xCoord, yCoord);
			i++;
		}
		Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] Components organized... Boxes visitedList: "+visitedList.size(), Logger.DEBUG);
		visitedList.clear();

		Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] Positions updated...", Logger.DEBUG);

	}

	/**
	 * Updates the sibling boxes (i.e. linked premises boxes) related to the creation of a new link.
	 * For example, if box A is attached to box B as linked premises, and box B gets a new child, then box A should also
	 * have a relation pointing to that child.  This method uses the private helper method "updateRecursive" to do its dirty work.
	 * @param link - The new, user-drawn link from which we must check for potentially-needed other new links
	 */
	public void updateSiblingLinks(OrganizerLink link)
	{
		linksToCreate.clear();
		// The original link data
		LinkedBox origStartBox = link.getStartBox();
		LinkedBox origEndBox = link.getEndBox();
		String linkType = link.getType();

		if (linkType.equalsIgnoreCase("Linked Premises"))
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
					OrganizerLink newLink = new OrganizerLink(origEndBox, newChildBox, origStartChildLink.getType());
					linksToCreate.add(newLink);
				}
			}

			for (OrganizerLink origEndChildLink : origEndChildLinks)
			{
				LinkedBox newChildBox = origEndChildLink.getEndBox();
				if (!origStartChildBoxes.contains(newChildBox))
				{
					OrganizerLink newLink = new OrganizerLink(origStartBox, newChildBox, origEndChildLink.getType());
					linksToCreate.add(newLink);
				}
			}
		}
		else
		{
			// IMPORTANT: Clear the visited nodes HashSet to avoid collisions with anything in there previously
			HashSet<LinkedBox> origStartSiblingBoxes = origStartBox.getSiblingBoxes();
			visited.clear();
			for (LinkedBox origStartSiblingBox : origStartSiblingBoxes)
			{
				updateRecursive(origStartSiblingBox, origEndBox, linkType);
			}
			 // potential start box, end box, type of link
			visited.clear();
		}

		addLinksToVisual();
		linksToCreate.clear();
	}

	/*	Recursively checks the siblings of a given start box to see if they need to be updated with a new relation.
	 *	Keeps track of boxes visited so that the method will eventually end.
	 *	@param startBox - The box from which we might make a link
	 *	@param END_BOX - The constant box to which we will be connecting
	 *	@param LINK_TYPE - The type of connection we will make
	*/
	private void updateRecursive(LinkedBox startBox, LinkedBox END_BOX, String LINK_TYPE)
	{
		if (!visited.contains(startBox))
		{
			visited.add(startBox);
			if (!startBox.getChildBoxes().contains(END_BOX))
			{
				linksToCreate.add(new OrganizerLink(startBox, END_BOX, LINK_TYPE));
			}
			for (LinkedBox siblingBox : startBox.getSiblingBoxes())
			{
				updateRecursive(siblingBox, END_BOX, LINK_TYPE);
			}
		}
	}

	// Returns 0 for yes, otherwise error code
	public int linkedPremisesCanBeCreated(OrganizerLink link)
	{
		LinkedBox startBox = link.getStartBox();
		LinkedBox endBox = link.getEndBox();

		if (startBox == null || endBox == null)
		{
			Logger.log("null box", Logger.DEBUG);
			return 5;
		}

		if (startBox.equals(endBox))
		{
			return 4;
		}

		// Checks that both boxes are premises, else return error code 1
		if (startBox.getType().equalsIgnoreCase("Premise") && endBox.getType().equalsIgnoreCase("Premise"))
		{
			// Checks that they both have fewer than 2 siblings, else return error code 2
			if (startBox.getNumSiblings() < MAX_SIBLINGS && endBox.getNumSiblings() < MAX_SIBLINGS)
			{
				HashSet<LinkedBox> startBoxAndSibs = new HashSet<LinkedBox>(startBox.getSiblingBoxes());
				startBoxAndSibs.add(startBox);
				// Checks that there aren't existing "circular" connections with each other's children, else return error code 3
				if (this.isCompatible(startBoxAndSibs, endBox))
				{
					return 0;
				}
				else
				{
					return 3;
				}
			}
			else
			{
				return 2;
			}
		}
		else
		{
			return 1;
		}
	}

	/*	Checks that there aren't existing nonChild connections between the start box plus siblings group and the end Box children.  Returns true if compatible.
		StartBox and endBox are two boxes that are having a linked premises relation created between them. */
	private boolean isCompatible(HashSet<LinkedBox> startBoxAndSibs, LinkedBox endBox)
	{
		for (LinkedBox startBox : startBoxAndSibs)
		{
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
		}

		return true;
	}

	// Updates a box position on the map, might need to add to redraw the relations because I'm losing arrow heads on them for some reason
	private void sendUpdatePositionToServer(LinkedBox box, int x, int y)
	{
		communicator.sendActionPackage(actionBuilder.updateBoxPosition(map.getID(), box.getBoxID(), x, y));
	}

	/*	Adds the new links to the organizer model and map
	*/
	private void addLinksToVisual()
	{	
		String elementType = "relation";

		MVController controller = LASAD_Client.getMVCController(map.getID());

		ElementInfo linkInfo = new ElementInfo();
		linkInfo.setElementType(elementType);

		for (OrganizerLink link : linksToCreate)
		{
			// a better name for element ID here would be subtype, as in, what kind of relation.  I didn't write it.
			linkInfo.setElementID(link.getType());

			String startBoxStringID = Integer.toString(link.getStartBox().getBoxID());
			String endBoxStringID = Integer.toString(link.getEndBox().getBoxID());

			ActionPackage myPackage = actionBuilder.createLinkWithElements(linkInfo, map.getID(), startBoxStringID, endBoxStringID);
			LASADActionReceiver.getInstance().setSiblingsAlreadyUpdated(true);
			communicator.sendActionPackage(myPackage);
		}
	}

	public void determineLinksToRemove(OrganizerLink removedLink)
	{
		linksToCreate.clear();
		linksToRemove.clear();
		if (!removedLink.getType().equalsIgnoreCase("Linked Premises"))
		{
			LinkedBox removedLinkStartBox = removedLink.getStartBox();
			int numSiblings = removedLinkStartBox.getNumSiblings();
			HashSet<OrganizerLink> siblingLinks = removedLinkStartBox.getSiblingLinks();
			for (OrganizerLink link : siblingLinks)
			{
				linksToRemove.add(link);
			}
		}

		removeLinksFromVisual();
		linksToCreate.clear();
		linksToRemove.clear();
	}

	private void removeLinksFromVisual()
	{
		MVController controller = LASAD_Client.getMVCController(map.getID());

		for (OrganizerLink link : linksToRemove)
		{
			ActionPackage myPackage = actionBuilder.removeElement(map.getID(), link.getLinkID());
			LASADActionReceiver.getInstance().setPremisesAlreadyRemoved(true);
			communicator.sendActionPackage(myPackage);
		}
	}
}