/*
// Added by Kevin Loughlin to update Group Links, location not ideal, investigate where to best put it
			Logger.log("Arrived at new code", Logger.DEBUG_DETAILS);
			// Kevin Loughlin, to get all the nonGroupRelations in the event of a group link, to create the necessary new links
			//if (info.getElementID().equalsIgnoreCase("Group") )
			//{
			//Logger.log("Entered first if", Logger.DEBUG_DETAILS);
			AutoOrganizer autoOrganizer = new AutoOrganizer(LASAD_Client.getMapTab(controller.getMapID()).getMyMapSpace().getMyMap());
			autoOrganizer.updateGroupLinks();
			Logger.log("AutoOrganizer successfully returned", Logger.DEBUG_DETAILS);
			//}
			//End of added by Kevin Loughlin
*/


// I'm debating whether this is the best package for AutoOrganizer.  I really don't think it matters in the end though.
package lasad.gwt.client.model.organization;

import java.util.Vector;
import java.util.List;
import java.util.Collections;
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

import lasad.gwt.client.model.organization.LinkedBox;
import lasad.gwt.client.model.organization.OrganizerLink;

/**
 * An AutoOrganizer can clean up the user's workspace into a clearer visual representation of the argument. It can also update group links
 * in ArgumentMap representations where a type of relation is listed as "group" (not case sensitive). The overall map organizing function,
 * accordingly called organizeMap(), is only called when the user clicks the corresponding button on the ArgumentMapMenuBar. Though originally
 * built for maps using Mara Harrell's template, this class can be applied to any map from any template.
 * TODO: I could look into having a model update one element with each action, rather than running the whole organization process from scratch.
 * @author Kevin Loughlin
 * @since 12 June 2015, Updated 18 June 2015
 */
public class AutoOrganizer
{

	// Space between organized nodes
	private final int Y_SPACE = 150;

	private AbstractGraphMap map;
	private List<Component> mapComponents;

	// Perfectly centered box location
	private final int CENTER_X = 2400;
	private final int CENTER_Y = CENTER_X;

	private Vector<LinkedBox> boxes = new Vector<LinkedBox>();

	// groupBoxes are, as you would guess, boxes that are attached via a link of type "group"
	private Vector<LinkedBox> groupBoxes = new Vector<LinkedBox>();

	private Vector<Vector<LinkedBox>> groupFamilies = new Vector<Vector<LinkedBox>>();

	/* rootBoxes are those that are on height level 1, i.e. they start an argument thread.  Theoretically, a map could have multiple
		threads (even if that would be poor organization, and thus that must be taken into account in the organizeMap method. */
	private Vector<LinkedBox> rootBoxes = new Vector<LinkedBox>();

	private Vector<OrganizerLink> links = new Vector<OrganizerLink>();
	//private Vector<OrganizerLink> groupLinks = new Vector<OrganizerLink>();

	private Vector<LinkedBox> visited = new Vector<LinkedBox>();

	private static HashMap<String, AutoOrganizer> instances = new HashMap<String, AutoOrganizer>();

	// For sending map updates to the server
	private LASADActionSender communicator = LASADActionSender.getInstance();
	private ActionFactory actionBuilder = ActionFactory.getInstance();

	// Only constructor currently in use
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

	public static AutoOrganizer getInstanceByMapID(String mapID)
	{
		return instances.get(mapID);
	}

	// The methd for organizing the map
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
			yCoord += 150;
		}

		Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] Positions updated...", Logger.DEBUG);

	}

	public void updateGroupLinks(OrganizerLink link)
	{
		visited.clear();
		LinkedBox origStartBox = link.getStartBox();
		LinkedBox origEndBox = link.getEndBox();


		String linkType = link.getType();

		visited.add(origStartBox);

		if (linkType.equalsIgnoreCase("Linked Premises"))
		{
			Vector<OrganizerLink> startChildLinks = origStartBox.getChildLinks();
			Vector<OrganizerLink> startSiblingLinks = origStartBox.getSiblingLinks();

			Vector<OrganizerLink> endChildLinks = origEndBox.getChildLinks();
			Vector<OrganizerLink> endSiblingLinks = origEndBox.getSiblingLinks();

			for (OrganizerLink startChildLink : startChildLinks)
			{
				for (OrganizerLink endSiblingLink : endSiblingLinks)
				{
					updateRecursive(endSiblingLink, startChildLink);
				}
			}

			for (OrganizerLink endChildLink : endChildLinks)
			{
				for (OrganizerLink startSiblingLink : startSiblingLinks)
				{
					updateRecursive(startSiblingLink, endChildLink);
				}
			}
		}
		else
		{
			Vector<OrganizerLink> siblingLinks = origStartBox.getSiblingLinks();
			for (OrganizerLink siblingLink : siblingLinks)
			{
				updateRecursive(siblingLink, link);
			}
		}

		visited.clear();	
	}

	private void updateRecursive(OrganizerLink siblingLink, OrganizerLink origLink)
	{
		LinkedBox startBox = siblingLink.getStartBox();
		LinkedBox endBox = siblingLink.getEndBox();

		String linkType = origLink.getType();
		LinkedBox origEndBox = origLink.getEndBox();

		if (!visited.contains(startBox))
		{
			OrganizerLink newLink = new OrganizerLink(startBox, origEndBox, linkType);
			visited.add(startBox);
			sendUpdateGroupLinkToServer(newLink);

			Vector<OrganizerLink> nextSiblingLinks = startBox.getSiblingLinks();
			for (OrganizerLink nextSiblingLink : nextSiblingLinks)
			{
				updateRecursive(nextSiblingLink, origLink);
			}
		}
		if (!visited.contains(endBox))
		{
			OrganizerLink newLink = new OrganizerLink(endBox, origEndBox, linkType);
			visited.add(endBox);
			sendUpdateGroupLinkToServer(newLink);

			Vector<OrganizerLink> nextSiblingLinks = endBox.getSiblingLinks();
			for (OrganizerLink nextSiblingLink : nextSiblingLinks)
			{
				updateRecursive(nextSiblingLink, origLink);
			}
		}

	}

	// The method for updating the group links, if the ontology supports group links.
	/*
	public void updateGroupLinks()
	{
		Logger.log("Entered updateGroupLinks", Logger.DEBUG);
		sortMapComponents();
		Logger.log("Sorted Components", Logger.DEBUG);
		createConnectionRelationships();
		Logger.log("Created Relationships", Logger.DEBUG);

		for (Vector<LinkedBox> groupFamily : groupFamilies)
		{
			for (LinkedBox alpha : groupFamily)
			{
				for (LinkedBox beta : groupFamily)
				{
					if (!alpha.equals(beta))
					{
						HashMap<LinkedBox, OrganizerLink> alphaChildConnections = alpha.getChildConnections();
						HashMap<LinkedBox, OrganizerLink> betaChildConnections = beta.getChildConnections();
						for (HashMap.Entry<LinkedBox, OrganizerLink> alphaChildConnection : alphaChildConnections.entrySet())
						{
							if (!(betaChildConnections.containsKey(alphaChildConnection.getKey())))
							{
								OrganizerLink newLink = alphaChildConnection.getValue();
								newLink.setStartBoxID(beta.getBoxID());
								newLink.setStartBoxRootID(beta.getRootID());

								alphaChildConnection.getKey().addParentConnection(beta, newLink);
								beta.addChildConnection(alphaChildConnection.getKey(), newLink);

								sendUpdateGroupLinkToServer(newLink);
							}
						}

						for (HashMap.Entry<LinkedBox, OrganizerLink> betaChildConnection : betaChildConnections.entrySet())
						{
							if (!(alphaChildConnections.containsKey(betaChildConnection.getKey())))
							{
								OrganizerLink newLink = betaChildConnection.getValue();
								newLink.setStartBoxID(alpha.getBoxID());
								newLink.setStartBoxRootID(alpha.getRootID());

								betaChildConnection.getKey().addParentConnection(alpha, newLink);
								alpha.addChildConnection(betaChildConnection.getKey(), newLink);

								sendUpdateGroupLinkToServer(newLink);
							}
						}
					}
				}
			}
		}
	}
	/*


						HashMap<LinkedBox, OrganizerLink> greaterChildConnections;
						HashMap<LinkedBox, OrganizerLink> fewerChildConnections;
						LinkedBox newStartBox;

						if (alphaChildConnections.size() >= betaChildConnections.size() )
						{
							greaterChildConnections = alphaChildConnections;
							fewerChildConnections = betaChildConnections;
							newStartBox = betaGroupConnection.getKey();
						}
						else // if (alphaChildConnections.size() < betaChildConnections.size() )
						{
							greaterChildConnections = betaChildConnections;
							fewerChildConnections = alphaChildConnections;
							newStartBox = alphaGroupBox;
						}

						for (HashMap.Entry<LinkedBox, OrganizerLink> greaterChildConnection : greaterChildConnections.entrySet())
						{
							LinkedBox greaterChildBox = greaterChildConnection.getKey();
							if (!(fewerChildConnections.containsKey(greaterChildBox)))
							{
								OrganizerLink newLink = greaterChildConnection.getValue();
								newLink.setStartBoxID(newStartBox.getBoxID());
								newLink.setStartBoxRootID(newStartBox.getRootID());

								greaterChildBox.addParentConnection(newStartBox, newLink);
								newStartBox.addChildConnection(greaterChildBox, newLink);

								sendUpdateGroupLinkToServer(newLink);
							}

						}
					}
				}
			}
		}

		// Essentially, we want the the group box with fewer children to add a link to all the missing children from the greater group box
		//TODO Might need to look into just getting which has more children and telling the other to add the missing ones
		for (LinkedBox alphaGroupBox : groupBoxes)
		{
			Logger.log("FIRST", Logger.DEBUG);

			HashMap<LinkedBox, OrganizerLink> alphaChildConnections = alphaGroupBox.getChildConnections();
			HashMap<LinkedBox, OrganizerLink> betaGroupConnections = alphaGroupBox.getAllGroupConnections();

			for (HashMap.Entry<LinkedBox, OrganizerLink> betaGroupConnection : betaGroupConnections.entrySet())
			{
				Logger.log("second", Logger.DEBUG);
				HashMap<LinkedBox, OrganizerLink> betaChildConnections = betaGroupConnection.getKey().getChildConnections(); //getChildConnections();
				
				HashMap<LinkedBox, OrganizerLink> greaterChildConnections;
				HashMap<LinkedBox, OrganizerLink> fewerChildConnections;
				LinkedBox newStartBox;

				if (alphaChildConnections.size() >= betaChildConnections.size() )
				{
					greaterChildConnections = alphaChildConnections;
					fewerChildConnections = betaChildConnections;
					newStartBox = betaGroupConnection.getKey();
				}
				else // if (alphaChildConnections.size() < betaChildConnections.size() )
				{
					greaterChildConnections = betaChildConnections;
					fewerChildConnections = alphaChildConnections;
					newStartBox = alphaGroupBox;
				}

				for (HashMap.Entry<LinkedBox, OrganizerLink> greaterChildConnection : greaterChildConnections.entrySet())
				{
					LinkedBox greaterChildBox = greaterChildConnection.getKey();
					if (!(fewerChildConnections.containsKey(greaterChildBox)))
					{
						OrganizerLink newLink = greaterChildConnection.getValue();
						newLink.setStartBoxID(newStartBox.getBoxID());
						newLink.setStartBoxRootID(newStartBox.getRootID());

						greaterChildBox.addParentConnection(newStartBox, newLink);
						newStartBox.addChildConnection(greaterChildBox, newLink);

						sendUpdateGroupLinkToServer(newLink);
					}

				}

			}

		}

	}
*/

	// Helper method that sorts the mapComponents into a LinkedBox Vector or an OrganizerLink vector
	private void sortMapComponents()
	{
		Vector<AbstractBox> abstractBoxes = new Vector<AbstractBox>();
		Vector<AbstractLinkPanel> abstractLinkPanels = new Vector<AbstractLinkPanel>();

		for (Component mapComponent : mapComponents)
		{
			if (mapComponent instanceof AbstractBox)
			{
				AbstractBox abstractBox = (AbstractBox) mapComponent;
				LinkedBox newBox = new LinkedBox(abstractBox.getConnectedModel().getId(), Integer.parseInt(abstractBox.getConnectedModel().getValue(ParameterTypes.RootElementId)) );
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


	// Helper method that associates each LinkedBox to LinkedBox connection with a corresponding OrganizerLink
	// Might be able to optimize speed by removing a link if match is found
	// My checks for contain are probably not necessary now that I flipped the nesting of the loops
	/*
	private void createConnectionRelationships()
	{
		for (LinkedBox box : boxes)
		{
			for (OrganizerLink link : links)
			{
				int startBoxID = link.getStartBoxID();
				int endBoxID = link.getEndBoxID();
				int startBoxRootID = link.getStartBoxRootID();
				int endBoxRootID = link.getEndBoxRootID();

				LinkedBox startBox = new LinkedBox(startBoxID, startBoxRootID);
				LinkedBox endBox = new LinkedBox(endBoxID, endBoxRootID);

				boolean isGroup = link.getType().equalsIgnoreCase("Group");

				if (box.equals(startBox) )
				{
					if (isGroup)
					{
						box.addChildGroupConnection(endBox, link);
						if (!(groupBoxes.contains(box) ) )
						{
							groupBoxes.add(box);
						}
					}
					else
					{
						box.addChildConnection(endBox, link);
					}
				}
				else if (box.equals(endBox) )
				{
					if (isGroup)
					{
						box.addParentGroupConnection(startBox, link);
						if (!(groupBoxes.contains(box) ) )
						{
							groupBoxes.add(box);
						}
					}
					else
					{
						box.addParentConnection(startBox, link);
					}
				}
			}

		}

		int groupID = 0;
		for (LinkedBox groupBox : groupBoxes)
		{
			sortGroupFamily(groupBox, groupID);
			groupID++;
		}

		visited.clear();
		/*
		for (OrganizerLink link : links)
		{
			int startBoxID = link.getStartBoxID();
			int endBoxID = link.getEndBoxID();
			int startBoxRootID = link.getStartBoxRootID();
			int endBoxRootID = link.getEndBoxRootID();

			LinkedBox startBox = new LinkedBox(startBoxID, startBoxRootID);
			LinkedBox endBox = new LinkedBox(endBoxID, endBoxRootID);

			boolean isGroup = link.getType().equalsIgnoreCase("Group");
			if (isGroup)
			{
				groupLinks.add(link);
			}

			for (LinkedBox box : boxes)
			{
				if (box.equals(startBox) )
				{
					if (isGroup)
					{
						box.addChildGroupConnection(endBox, link);
						if (!(groupBoxes.contains(box) ) )
						{
							groupBoxes.add(box);
						}
					}
					else
					{
						box.addChildConnection(endBox, link);
					}
				}
				else if (box.equals(endBox) )
				{
					if (isGroup)
					{
						box.addParentGroupConnection(startBox, link);
						if (!(groupBoxes.contains(box) ) )
						{
							groupBoxes.add(box);
						}
					}
					else
					{
						box.addParentConnection(startBox, link);
					}
				}
			}
		}
		*/
	//}

/*
	private void sortGroupFamily(LinkedBox box, int groupID)
	{
		Logger.log("STUCK AT SORT GROUP FAMILY", Logger.DEBUG);

		if (!visited.contains(box))
		{
			visited.add(box);
			box.setGroupID(groupID);

			try
			{
				groupFamilies.get(groupID).add(box);
			}
			catch (ArrayIndexOutOfBoundsException e)
			{
				groupFamilies.add(new Vector<LinkedBox>());
				groupFamilies.get(groupID).add(box);
			}

			for (LinkedBox groupBox : box.getAllGroupConnections().keySet())
			{
				sortGroupFamily(groupBox, groupID);
			}
		}
	}

	// Not in use right now
	private void setBoxPositions(LinkedBox box, int midLevel)
	{
		int levelDifference = box.getHeightLevel() - midLevel;
		sendUpdatePositionToServer(box, CENTER_X, CENTER_Y + (levelDifference * Y_SPACE) );
	}
*/
	// Updates a box position on the map, might need to add to redraw the links because I'm losing arrow heads on them for some reason
	private void sendUpdatePositionToServer(LinkedBox box, int x, int y)
	{
		communicator.sendActionPackage(actionBuilder.updateBoxPosition(map.getID(), box.getBoxID(), x, y));
	}

	// Adds necessary connections from a groupLink
	private void sendUpdateGroupLinkToServer(OrganizerLink link)
	{
		
		ElementInfo linkInfo = new ElementInfo();
		linkInfo.setElementType("relation");
		linkInfo.setElementID(link.getType());

		String startBoxStringID = Integer.toString(link.getStartBoxID());
		String endBoxStringID = Integer.toString(link.getEndBoxID());

		//communicator.sendActionPackage(actionBuilder.createLinkWithElements(linkInfo, map.getID(), startBoxStringID, endBoxStringID));
		

		ActionPackage myPackage = actionBuilder.createLinkWithElements(linkInfo, map.getID(), startBoxStringID, endBoxStringID);

		for (Action a : myPackage.getActions())
		{
			String mapIDString = a.getParameterValue(ParameterTypes.MapId);

			MVController controller = null;
			if (mapIDString != null) {
				controller = LASAD_Client.getMVCController(mapIDString);
			}
			if (controller != null)
			{
		// Currently feedback engines count as "element", thus we have
				// to filter them here...
				String elementType = a.getParameterValue(ParameterTypes.Type);
				String elementIDString = a.getParameterValue(ParameterTypes.Id);
				int elementID = -1;
				if (elementIDString != null) {
					elementID = Integer.parseInt(elementIDString);
				}

				String username = a.getParameterValue(ParameterTypes.UserName);

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
					if (!param.getType().equals(ParameterTypes.Parent))
						elementModel.setValue(param.getType(), param.getValue());
				}

				// Work on parent relations
				if (a.getParameterValues(ParameterTypes.Parent) != null) {

					for (String parentID : a.getParameterValues(ParameterTypes.Parent)) {
						controller.setParent(elementModel, controller.getElement(Integer.parseInt(parentID)));

						Logger.log("[lasad.gwt.client.communication.LASADActionReceiver] Added ParentElement: " + parentID, Logger.DEBUG);
					}
				}

				// Now Register new Element to the Model
				controller.addElementModel(elementModel);
			}
		}
	}
}