package lasad.gwt.client.model.organization;

// Some of these import statements can be deleted, but I'm too lazy to do that right now and it's not exactly a big deal.
import java.util.HashSet;
import java.util.List;

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

/**
 * An AutoOrganizer can clean up the user's workspace into a clearer visual representation of the argument. It can also update links
 * in ArgumentMap representations where a type of relation is listed as "Linked Premises" (not case sensitive). The overall map organizing function,
 * accordingly called organizeMap(), is only called when the user clicks the corresponding button on the ArgumentMapMenuBar. Though originally
 * built for maps using Mara Harrell's template, this class can be applied to any map from any template.
 * This class, especially organizeMap, needs significant overhauls, as I have changed the structuring such that there is a model that updates
 * with every change to the map.  Thus, we don't need to start from scratch and gather components every time the map is called.
 * @author Kevin Loughlin
 * @since 12 June 2015, Updated 24 June 2015
 */

// TODO how do we handle circular links?  Right now they MIGHT cause an endless loop, although I think this.visited takes care of that
public class AutoOrganizer
{
	// Space between organized nodes
	private final int Y_SPACE = 150;

	// The map
	private AbstractGraphMap map;

	/*	The map's components (i.e. instances of AbstractBox and AbstractLinkPanel, I would imagine child elements would be stored here)
		but they're not needed */
	private List<Component> mapComponents;

	// Perfectly centered box location
	private final int CENTER_X = 2400;
	private final int CENTER_Y = CENTER_X;

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

	/**
	 * UNDER DEVELOPMENT
	 * Organizes the map from bottom to top: Root "parents" start at the bottom, leading the way to children above.
	 * "sibling" boxes (i.e. boxes attached via Linked Premises) are placed adjacently.
	 */
	public void organizeMap()
	{
		Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] Running AutoOrganizer...", Logger.DEBUG);

		sortMapComponents();

		Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] Components sorted...", Logger.DEBUG);

		// Just testing to see if this works
		int yCoord = 2200;			
		for (LinkedBox box : boxes)
		{
			sendUpdatePositionToServer(box, CENTER_X, yCoord);
			yCoord += Y_SPACE;
		}

		Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] Positions updated...", Logger.DEBUG);

	}

	/**
	 * UNDER DEVELOPMENT
	 * Updates the sibling boxes (i.e. linked premises boxes) related to the creation of a new link.
	 * For example, if box A is attached to box B as linked premises, and box B gets a new child, then box A should also
	 * have a relation pointing to that child.  This method uses the private helper method "updateRecursive" to do its dirty work.
	 * @param link - The new, user-drawn link from which we must check for potentially-needed other new links
	 */
	public void updateSiblingLinks(OrganizerLink link)
	{
		Logger.log("Entered updateSiblingLinks", Logger.DEBUG);
		// The original link data
		LinkedBox origStartBox = link.getStartBox();
		LinkedBox origEndBox = link.getEndBox();
		String linkType = link.getType();

		if (origStartBox == null)
		{
			Logger.log("NULL NEG 1", Logger.DEBUG);
		}
		if (origEndBox == null)
		{
			Logger.log("NULL NEG 2", Logger.DEBUG);
		}
		if (linkType == null)
		{
			Logger.log("NULL NEG 3", Logger.DEBUG);	
		}


		if (linkType.equalsIgnoreCase("Linked Premises"))
		{
			Logger.log("Entered LinkPremises", Logger.DEBUG);
			HashSet<OrganizerLink> origStartChildLinks = origStartBox.getChildLinks();
			HashSet<LinkedBox> origStartChildBoxes = origStartBox.getChildBoxes();

			HashSet<OrganizerLink> origEndChildLinks = origEndBox.getChildLinks();
			HashSet<LinkedBox> origEndChildBoxes = origEndBox.getChildBoxes();

			if (origStartChildLinks == null)
			{
				Logger.log("NULL 1", Logger.DEBUG);
			}
			if (origStartChildBoxes == null)
			{
				Logger.log("NULL 2", Logger.DEBUG);
			}
			if (origEndChildLinks == null)
			{
				Logger.log("NULL 3", Logger.DEBUG);
			}
			if (origEndChildBoxes == null)
			{
				Logger.log("NULL 4", Logger.DEBUG);	
			}

			Logger.log("Entering first loop", Logger.DEBUG);

			for (OrganizerLink origStartChildLink : origStartChildLinks)
			{
				LinkedBox newChildBox = origStartChildLink.getEndBox();
				if (!origEndChildBoxes.contains(newChildBox))
				{
					OrganizerLink newLink = new OrganizerLink(origEndBox, newChildBox, origStartChildLink.getType());
					addLinkToVisual(newLink);
				}
			}
			Logger.log("Exited first loop", Logger.DEBUG);

			Logger.log("Entering second loop", Logger.DEBUG);
			for (OrganizerLink origEndChildLink : origEndChildLinks)
			{
				LinkedBox newChildBox = origEndChildLink.getEndBox();
				if (!origStartChildBoxes.contains(newChildBox))
				{
					OrganizerLink newLink = new OrganizerLink(origStartBox, newChildBox, origEndChildLink.getType());
					addLinkToVisual(newLink);
				}
			}
			Logger.log("Exited second loop", Logger.DEBUG);

			Logger.log("Exited LinkPremises", Logger.DEBUG);
		}
		else
		{
			Logger.log("Entered NON-LinkPremises", Logger.DEBUG);
			// IMPORTANT: Clear the visited nodes HashSet to avoid collisions with anything in there previously
			HashSet<LinkedBox> origStartSiblingBoxes = origStartBox.getSiblingBoxes();
			visited.clear();
			for (LinkedBox origStartSiblingBox : origStartSiblingBoxes)
			{
				updateRecursive(origStartSiblingBox, origEndBox, linkType);
			}
			 // potential start box, end box, type of link
			visited.clear();

			Logger.log("Exited NON-LinkPremises", Logger.DEBUG);
		}
	}
		/*
		// IMPORTANT: Clear the visited nodes HashSet to avoid collisions with anything in there previously
		visited.clear();

		// The original link data
		LinkedBox origStartBox = link.getStartBox();
		LinkedBox origEndBox = link.getEndBox();
		String linkType = link.getType();

		visited.add(origStartBox);

		// Behaves differently depending on whether or not the link sub type is Linked Premises
		if (linkType.equalsIgnoreCase("Linked Premises"))
		{
			/*	Important to note that the concept of "startBox" versus "endBox" isn't as crucial for siblingLinks
				other than to distinguish between the two boxes. */
/*
			Logger.log("Entered Linked Premises Code", Logger.DEBUG);

			HashSet<OrganizerLink> startChildLinks = origStartBox.getChildLinks();
			HashSet<OrganizerLink> startSiblingLinks = origStartBox.getSiblingLinks();

			HashSet<OrganizerLink> endChildLinks = origEndBox.getChildLinks();
			HashSet<OrganizerLink> endSiblingLinks = origEndBox.getSiblingLinks();

			for (OrganizerLink startChildLink : startChildLinks)
			{
				// Verify that every sibling of origStartBox connects to every child of the startBox
				for (OrganizerLink startSiblingLink : startSiblingLinks)
				{
					updateRecursive(startSiblingLink, startChildLink);
				}

				visited.clear();
				/*
				// Verify that every sibling of origEndBox connects to every child of the startBox
				for (OrganizerLink endSiblingLink : endSiblingLinks)
				{
					updateRecursive(endSiblingLink, startChildLink);
				}

				visited.clear();
				*//*
			}

			for (OrganizerLink endChildLink : endChildLinks)
			{
				// Verify that every sibling of origStartBox connects to every child of the endBox
				for (OrganizerLink startSiblingLink : startSiblingLinks)
				{
					updateRecursive(startSiblingLink, endChildLink);
				}

				visited.clear();
				/*
				// Verify that every sibling of origEndBox connects to every child of the endBox
				for (OrganizerLink endSiblingLink : endSiblingLinks)
				{
					updateRecursive(endSiblingLink, endChildLink);
				}

				visited.clear();
				*//*
			}
		}
		else
		{
			Logger.log("Entered Other Link Type Code", Logger.DEBUG);
			HashSet<OrganizerLink> siblingLinks = origStartBox.getSiblingLinks();

			// Verify that all siblings of the origStartBox point to the endBox of the origLink
			for (OrganizerLink siblingLink : siblingLinks)
			{
				updateRecursive(siblingLink, link);
			}
		}

		// Reclear visited for added protection in case I forget to anywhere else
		visited.clear();	
		*/

	/*	Recursively checks the siblings of a given box to see if they need to be updated with a new relation.
	 *	Keeps track of boxes visited so that the method will eventually end.
	 *	@param siblingLink - The Linked Premise from a box to its sibling box.  The sibling needs to receive a corresponding
	 *	link similar to origLink.
	 *	@param origLink - The original link from the startBox to a child/sibling.  We are checking the siblings of startBox
	 *	for needed relation updates.  We use the type of origLink of one of its two boxes in making our new relation.
	*/
	private void updateRecursive(LinkedBox startBox, LinkedBox END_BOX, String LINK_TYPE)
	{
		Logger.log("Entered updateRecursive", Logger.DEBUG);
		if (!visited.contains(startBox))
		{
			visited.add(startBox);
			if (!startBox.getChildBoxes().contains(END_BOX))
			{
				addLinkToVisual(new OrganizerLink(startBox, END_BOX, LINK_TYPE));
			}
			for (LinkedBox siblingBox : startBox.getSiblingBoxes())
			{
				updateRecursive(siblingBox, END_BOX, LINK_TYPE);
			}
		}
	}

	/*	Helper method that sorts the mapComponents into a LinkedBox HashSet or an OrganizerLink HashSet
		Might not be used now that organization will come from an already updatedModel
		Don't worry about this method for now because we're not doing organizeMap() yet */
	private void sortMapComponents()
	{
		HashSet<AbstractBox> abstractBoxes = new HashSet<AbstractBox>();
		HashSet<AbstractLinkPanel> abstractLinkPanels = new HashSet<AbstractLinkPanel>();

		for (Component mapComponent : mapComponents)
		{
			if (mapComponent instanceof AbstractBox)
			{
				AbstractBox abstractBox = (AbstractBox) mapComponent;
				LinkedBox newBox = new LinkedBox(abstractBox.getConnectedModel().getId(), Integer.parseInt(abstractBox.getConnectedModel().getValue(ParameterTypes.RootElementId)), abstractBox.getElementInfo().getElementID());
				boxes.add(newBox);

			}
			else if (mapComponent instanceof AbstractLinkPanel)
			{
				abstractLinkPanels.add((AbstractLinkPanel) mapComponent);
			}
		}

		for (AbstractLinkPanel linkPanel : abstractLinkPanels)
		{
			String startBoxRootString;
			String endBoxRootString;
			int startBoxRootID;
			int endBoxRootID;

			// Shuts the compiler up
			int startBoxID = -1;
			int endBoxID = -1;

			String type = linkPanel.getMyLink().getElementInfo().getElementID();
			String direction = linkPanel.getMyLink().getConnectedModel().getValue(ParameterTypes.Direction);

			if (direction != null && direction.split(",")[0].equalsIgnoreCase(Integer.toString(linkPanel.getMyLink().getConnectedModel().getParents().get(1).getId())))
			{
				startBoxRootString = linkPanel.getMyLink().getConnectedModel().getParents().get(1).getValue(ParameterTypes.RootElementId);	
				endBoxRootString = linkPanel.getMyLink().getConnectedModel().getParents().get(0).getValue(ParameterTypes.RootElementId);	
			} 
			else
			{
				startBoxRootString = linkPanel.getMyLink().getConnectedModel().getParents().get(0).getValue(ParameterTypes.RootElementId);		//getValue(ParameterTypes.Id);
				endBoxRootString = linkPanel.getMyLink().getConnectedModel().getParents().get(1).getValue(ParameterTypes.RootElementId);	
			}

			startBoxRootID = Integer.parseInt(startBoxRootString);
			endBoxRootID = Integer.parseInt(endBoxRootString);

			/* For every link, there must be a corresponding start and end box (invariant).  So even if the compiler thinks startBoxID
				and endBoxID might not be changed from -1, they always will be. */

			LinkedBox startBox = null;
			LinkedBox endBox = null;

			for (LinkedBox box : boxes)
			{
				if(box.getRootID() == startBoxRootID)
				{
					startBox = box;
				}
				else if(box.getRootID() == endBoxRootID)
				{
					endBox = box;
				}
				if (startBox != null && endBox != null)
				{
					break;
				}
			}

			OrganizerLink organizerLink = new OrganizerLink(startBox, endBox, type);

			links.add(organizerLink);
		}
	}

	// Updates a box position on the map, might need to add to redraw the relations because I'm losing arrow heads on them for some reason
	private void sendUpdatePositionToServer(LinkedBox box, int x, int y)
	{
		communicator.sendActionPackage(actionBuilder.updateBoxPosition(map.getID(), box.getBoxID(), x, y));
	}

	/*	Adds the passed link to the organizer model and map
		@param link - the link to be added to the model and map
	*/
	private void addLinkToVisual(OrganizerLink link)
	{
		try
		{
			Logger.log("Entered add link to visual", Logger.DEBUG);
			// Get the argModel that we need to update
			ArgumentModel argModel = ArgumentModel.getInstanceByMapID(map.getID());

			// Add the necessary links to the organizer model
			if (link.getType().equalsIgnoreCase("Linked Premises"))
			{
				argModel.getBoxByBoxID(link.getStartBox().getBoxID()).addSiblingLink(link);
				argModel.getBoxByBoxID(link.getEndBox().getBoxID()).addSiblingLink(link);
			}
			else
			{
				argModel.getBoxByBoxID(link.getStartBox().getBoxID()).addChildLink(link);
				argModel.getBoxByBoxID(link.getEndBox().getBoxID()).addParentLink(link);
			}

			Logger.log("Arg model updated", Logger.DEBUG);
			
			String elementType = "relation";

			MVController controller = LASAD_Client.getMVCController(map.getID());

			ElementInfo linkInfo = new ElementInfo();
			linkInfo.setElementType(elementType);

			// a better name for element ID here would be subtype, as in, what kind of relation.  I didn't write it.
			linkInfo.setElementID(link.getType());

			String startBoxStringID = Integer.toString(link.getStartBox().getBoxID());
			String endBoxStringID = Integer.toString(link.getEndBox().getBoxID());

			Logger.log("Creating action package", Logger.DEBUG);

			ActionPackage myPackage = actionBuilder.createLinkWithElements(linkInfo, map.getID(), startBoxStringID, endBoxStringID);

			Logger.log("Entering action loop", Logger.DEBUG);
			for (Action a : myPackage.getActions())
			{	
				// From now on, elementID refers to element ID number.

				int elementID = this.counterID;
				counterID--;
				String elementIDString = a.getParameterValue(ParameterTypes.Id);
				if (elementIDString == null)
				{
					Logger.log("Element ID is null", Logger.DEBUG);
				}
				else
				{
					elementID = Integer.parseInt(elementIDString);
				}
				
				Logger.log("Parse successfully", Logger.DEBUG);

				String username = a.getParameterValue(ParameterTypes.UserName);

				if (username == null)
				{
					Logger.log("User name is null", Logger.DEBUG);
				}

				Logger.log("[lasad.gwt.client.communication.LASADActionReceiver] Create Model: " + elementID + ", Type: " + elementType,
						Logger.DEBUG);
				AbstractUnspecifiedElementModel elementModel = new UnspecifiedElementModelArgument(elementID, elementType, username);

				if (a.getParameterValue(ParameterTypes.ReplayTime) != null) {
					elementModel.setIsReplay(true);
				}
				// Needed to enable the add and del buttons in box header
				if (a.getParameterValue(ParameterTypes.ElementId) != null) {
					elementModel.setElementId(a.getParameterValue(ParameterTypes.ElementId));
				}

				// Add more specific data to the model
				for (Parameter param : a.getParameters()) {
					if (param.getType() != null && !param.getType().equals(ParameterTypes.Parent)
							&& !param.getType().equals(ParameterTypes.HighlightElementId)) {
						elementModel.setValue(param.getType(), param.getValue());
					}
				}

				// Work on parent relations
				if (a.getParameterValues(ParameterTypes.Parent) != null) {
					for (String parentID : a.getParameterValues(ParameterTypes.Parent)) {
						controller.setParent(elementModel, controller.getElement(Integer.parseInt(parentID)));

						Logger.log("[lasad.gwt.client.communication.LASADActionReceiver] Added ParentElement: " + parentID, Logger.DEBUG);
					}
				}

				// Now Register new Element to the default Model (what will actually update it on the map)
				Logger.log("Adding to model", Logger.DEBUG);
				controller.addElementModel(elementModel);
				Logger.log("Added to model", Logger.DEBUG);

				// End Kevin Loughlin
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Logger.log("EXCEPTION CAUGHT", Logger.DEBUG);
		}

		Logger.log("Exited add link to visual", Logger.DEBUG);
	}
}