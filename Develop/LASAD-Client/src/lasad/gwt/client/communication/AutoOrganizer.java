// I'm debating whether this is the best package for AutoOrganizer.  I really don't think it matters in the end though.
package lasad.gwt.client.communication;

import java.util.Vector;
import java.util.List;
import java.util.Collections;
import lasad.gwt.client.ui.box.LinkedBox;
import lasad.gwt.client.ui.link.OrganizerLink;
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

/**
 * An AutoOrganizer can clean up the user's workspace into a clearer visual representation of the argument. It can also update group links
 * in ArgumentMap representations where a type of relation is listed as "group" (not case sensitive). The overall map organizing function,
 * accordingly called organizeMap(), is only called when the user clicks the corresponding button on the ArgumentMapMenuBar. Though originally
 * built for maps using Mara Harrell's template, this class can be applied to any map from any template.
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

	/* rootBoxes are those that are on height level 1, i.e. they start an argument thread.  Theoretically, a map could have multiple
		threads (even if that would be poor organization, and thus that must be taken into account in the organizeMap method. */
	private Vector<LinkedBox> rootBoxes = new Vector<LinkedBox>();

	private Vector<OrganizerLink> links = new Vector<OrganizerLink>();
	private Vector<OrganizerLink> groupLinks = new Vector<OrganizerLink>();

	// For sending map updates to the server
	private LASADActionSender communicator = LASADActionSender.getInstance();
	private ActionFactory actionBuilder = ActionFactory.getInstance();

	// Only constructor currently in use
	public AutoOrganizer(AbstractGraphMap map)
	{
		this.map = map;
		this.mapComponents = map.getItems();
	}

	// Don't use default constructor
	private AutoOrganizer()
	{
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

	// The method for updating the group links, if the ontology supports group links.
	public void updateGroupLinks()
	{
		Logger.log("Entered updateGroupLinks", Logger.DEBUG);
		sortMapComponents();
		Logger.log("Sorted Components", Logger.DEBUG);
		createConnectionRelationships();
		Logger.log("Created Relationships", Logger.DEBUG);

		// Essentially, we want the the "minor" group box to have all the same children as the "major" group box
		//TODO Might need to look into just getting which has more children and telling the other to add the missing ones
		for (LinkedBox majorGroupBox : groupBoxes)
		{
			Logger.log("FIRST", Logger.DEBUG);

			HashMap<LinkedBox, OrganizerLink> majorChildConnections = majorGroupBox.getChildConnections();
			HashMap<LinkedBox, OrganizerLink> minorGroupConnections = majorGroupBox.getChildGroupConnections();

			for (HashMap.Entry<LinkedBox, OrganizerLink> minorGroupConnection : minorGroupConnections.entrySet())
			{
				Logger.log("second", Logger.DEBUG);
				HashMap<LinkedBox, OrganizerLink> minorChildConnections = minorGroupConnection.getKey().getChildConnections(); //getChildConnections();
				for (HashMap.Entry<LinkedBox, OrganizerLink> majorChildConnection : majorChildConnections.entrySet())
				{
					Logger.log("tHiRd", Logger.DEBUG);
					LinkedBox majorChildBox = majorChildConnection.getKey();
					if (!(minorChildConnections.containsKey(majorChildBox)))
					{
						Logger.log("Inside final if", Logger.DEBUG);
						LinkedBox minorGroupBox = minorGroupConnection.getKey();
						OrganizerLink newLink = majorChildConnection.getValue().copy();

						newLink.setStartBoxID(minorGroupBox.getBoxID());
						newLink.setStartBoxRootID(minorGroupBox.getRootID());

						majorChildBox.addParentConnection(minorGroupBox, newLink);
						minorGroupBox.addChildConnection(majorChildBox, newLink);

						sendUpdateGroupLinkToServer(newLink);
					}
				}
			}

		}

	}

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
				LinkedBox new_box = new LinkedBox(abstractBox.getConnectedModel().getId(), Integer.parseInt(abstractBox.getConnectedModel().getValue(ParameterTypes.RootElementId)) );
				boxes.add(new_box);

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

			for (LinkedBox box : boxes)
			{
				if(box.getRootID() == startBoxRootID)
				{
					startBoxID = box.getBoxID();
				}
				else if(box.getRootID() == endBoxRootID)
				{
					endBoxID = box.getBoxID();
				}
			}

			OrganizerLink organizerLink = new OrganizerLink(startBoxID, endBoxID, startBoxRootID, endBoxRootID, type);

			links.add(organizerLink);
		}
	}

	// Helper method that associates each LinkedBox to LinkedBox connection with a corresponding OrganizerLink
	private void createConnectionRelationships()
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
	}

	// Not in use right now
	private void setBoxPositions(LinkedBox box, int midLevel)
	{
		int levelDifference = box.getHeightLevel() - midLevel;
		sendUpdatePositionToServer(box, CENTER_X, CENTER_Y + (levelDifference * Y_SPACE) );
	}

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

		communicator.sendActionPackage(actionBuilder.createLinkWithElements(linkInfo, map.getID(), startBoxStringID, endBoxStringID));
	}
}