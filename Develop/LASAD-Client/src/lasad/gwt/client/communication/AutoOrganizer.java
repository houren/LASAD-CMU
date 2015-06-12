package lasad.gwt.client.communication;

import java.util.Vector;
import java.util.List;
import java.util.Collections;
import lasad.gwt.client.ui.box.LinkedBox;
import lasad.gwt.client.ui.box.AbstractBox;
import lasad.gwt.client.communication.helper.ActionFactory;
import lasad.gwt.client.communication.LASADActionSender;
import lasad.gwt.client.model.argument.MVController;
import lasad.gwt.client.ui.workspace.graphmap.AbstractGraphMap;
import lasad.shared.communication.objects.parameters.ParameterTypes;
import lasad.gwt.client.logger.Logger;

import com.extjs.gxt.ui.client.widget.Component;
import lasad.gwt.client.ui.link.AbstractLinkPanel;

/**
 * An AutoOrganizer cleans up the user's workspace into a clearer visual representation of the argument.
 * "autoorganize" is an optional attribute for an ontology, and thus AutoOrganizer is only called when the
 * attribute is set to "true."
 *
 * Though autoOrganize is specifically built for maps using Mara Harrell's template, in can be applied to any map from any template
 * provided that linked premises name the type of relation used between them as "Group".
 * @author Kevin Loughlin
 */

/*

	Do I want to do all nodes every time or just the next updated one?
	Is this called referenced each time there is a map event?

	Category of command: map
	There should be a command (Cmd) called OrganizeMap

	Call ActionFactory.updateBoxPosition
	ActionFactory.updateLinkPosition may also prove to be useful

	Should I just read in XML?  Maybe button to AutoOrganize?

*/

// Button is added via ArgumentMapMenuBar.java, search for lines with Kevin Loughlin
public class AutoOrganizer
{

	// Space between organized nodes
	private final int Y_SPACE = 150;

	private AbstractGraphMap map;
	private List<Component> mapComponents;
	//private Vector<Integer> organizedModel = null;
	// Perfectly centered box location
	private final int CENTER_X = 2400;
	private final int CENTER_Y = CENTER_X;
	//final String GROUP = new String("group");
	private Vector<LinkedBox> boxes = new Vector<LinkedBox>();
	private Vector<AbstractBox> abstractBoxes = new Vector<AbstractBox>();
	private Vector<LinkedBox> rootBoxes = new Vector<LinkedBox>();
	private Vector<AbstractLinkPanel> linkPanels = new Vector<AbstractLinkPanel>();

	private LASADActionSender communicator = LASADActionSender.getInstance();
	private ActionFactory actionBuilder = ActionFactory.getInstance();

	public AutoOrganizer(AbstractGraphMap map)
	{
		this.map = map;
		this.mapComponents = map.getItems();
	}

	// Does nothing and can't be accessed, intentional because it should not be instantiated via default constructor
	// It's just to shut the compiler up
	private AutoOrganizer()
	{
	}

	// Work in Id, not RootElementId, because you need Id for ActionPackage
	public void run()
	{
		Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] Running AutoOrganizer...", Logger.DEBUG);

		for (Component mapComponent : mapComponents)
		{
			if (mapComponent instanceof AbstractBox)
			{
				AbstractBox abstractBox = (AbstractBox) mapComponent;
				abstractBoxes.add(abstractBox);
				LinkedBox new_box = new LinkedBox(abstractBox.getConnectedModel().getId() );
				boxes.add(new_box);

			}
			else if (mapComponent instanceof AbstractLinkPanel)
			{
				linkPanels.add( (AbstractLinkPanel) mapComponent);
			}
		}

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

	private void setBoxPositions(LinkedBox box, int midLevel)
	{
		int levelDifference = box.getHeightLevel() - midLevel;
		sendUpdatePositionToServer(box, CENTER_X, CENTER_Y + (levelDifference * Y_SPACE) );
	}

	private void sendUpdatePositionToServer(LinkedBox box, int x, int y)
	{
		communicator.sendActionPackage(actionBuilder.updateBoxPosition(map.getID(), box.getBoxID(), x, y));
	}

}