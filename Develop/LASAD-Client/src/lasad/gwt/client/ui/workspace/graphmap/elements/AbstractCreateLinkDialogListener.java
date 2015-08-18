package lasad.gwt.client.ui.workspace.graphmap.elements;

import java.util.Collection;

import lasad.gwt.client.model.ElementInfo;
import lasad.gwt.client.ui.box.AbstractBox;
import lasad.gwt.client.ui.link.AbstractLink;
import lasad.gwt.client.ui.workspace.graphmap.GraphMap;
import lasad.shared.communication.objects.parameters.ParameterTypes;

import com.extjs.gxt.ui.client.event.Events;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;

import lasad.gwt.client.model.organization.ArgumentModel;
import lasad.gwt.client.model.organization.LinkedBox;
import lasad.gwt.client.model.organization.OrganizerLink;
import lasad.gwt.client.model.organization.AutoOrganizer;
import lasad.gwt.client.model.organization.GroupedBoxesStatusCodes;
import lasad.gwt.client.logger.Logger;
import lasad.gwt.client.ui.workspace.LASADInfo;

import lasad.gwt.client.LASAD_Client;

public abstract class AbstractCreateLinkDialogListener implements EventListener {

//    private final LASADActionSender communicator = LASADActionSender.getInstance();
//    private final ActionFactory actionBuilder = ActionFactory.getInstance();

	protected final int MAX_SIBLINGS = 2;

    protected GraphMap myMap;
    private AbstractCreateLinkDialog myDialogue;
    private AbstractBox b1, b2;
    private AbstractLink l1, l2;

    public AbstractCreateLinkDialogListener(GraphMap map, AbstractCreateLinkDialog dialogue, AbstractBox b1, AbstractBox b2) {
		this.myMap = map;
		this.myDialogue = dialogue;
		this.b1 = b1;
		this.b2 = b2;
    }

    public AbstractCreateLinkDialogListener(GraphMap map, AbstractCreateLinkDialog dialogue, AbstractBox b1, AbstractLink l2) {
		this.myMap = map;
		this.myDialogue = dialogue;
		this.b1 = b1;
		this.l2 = l2;
		if (!myMap.getMyViewSession().getController().getMapInfo().isAllowLinksToLinks())
		{
			myDialogue.removeFromParent();
		}
    }

    public void onBrowserEvent(Event be) {
		if (be.getTypeInt() == Events.OnMouseOver.getEventCode()) {
		    handleMouseOver(be);
		} else if (be.getTypeInt() == Events.OnClick.getEventCode()) {
		    handleOnClick(be);
		} else if (be.getTypeInt() == Events.OnMouseOut.getEventCode()) {
		    handleMouseOut(be);
		}
		be.stopPropagation();
    }

    private void handleOnClick(Event be) {
//		for (ElementInfo info : myMap.getMyViewSession().getController().getMapInfo().getElementsByType("relation").values()) {
		for (ElementInfo info : getElementsByType("relation")) {
		    if (((Element) be.getEventTarget().cast()).getInnerText().equals(info.getElementOption(ParameterTypes.Heading))) {
				// Send Action --> Server
				if (b1 != null && b2 != null) {

					// Begin additions by Kevin Loughlin to handle when new links may actually be created (necessary to maintain grouped boxes invariants)
					String linkType = info.getElementID();

					String connectsGroupString = info.getElementOption(ParameterTypes.ConnectsGroup);
					boolean connectsGroup;
					if (connectsGroupString == null)
					{
						connectsGroup = false;
					}
					else
					{
						connectsGroup = Boolean.parseBoolean(connectsGroupString);
					}

					ArgumentModel argModel = myMap.getArgModel();
					LinkedBox alpha = argModel.getBoxByBoxID(b1.getConnectedModel().getId());
					LinkedBox beta = argModel.getBoxByBoxID(b2.getConnectedModel().getId());
					OrganizerLink newLink = new OrganizerLink(alpha, beta, linkType, connectsGroup);

					if (alpha.hasExtendedSiblingLinkWith(beta))
					{
						LASADInfo.display("Error", "You cannot create a relation between already grouped boxes.");
					}

					else if (newLink.getConnectsGroup())
					{
						int statusCode = myMap.getAutoOrganizer().groupedBoxesCanBeCreated(newLink);

						if (statusCode == GroupedBoxesStatusCodes.SUCCESS)
						{
							onClickSendUpdateToServer(info, myMap.getID(), b1.getConnectedModel().getId() + "", b2.getConnectedModel().getId() + "");
						}
						else
						{
							switch (statusCode)
							{
								case GroupedBoxesStatusCodes.NULL_BOX:
									Logger.log("Null box passed to GroupedBoxesCanBeCreated", Logger.DEBUG);
									break;
								case GroupedBoxesStatusCodes.SAME_BOX:
									Logger.log("Same start and end box passed to GroupedBoxesCanBeCreated", Logger.DEBUG);
									break;
								case GroupedBoxesStatusCodes.NOT_SAME_TYPE:
									LASADInfo.display("Error", "Group links can only be created between boxes of the same type. Link not created.");
									break;
								case GroupedBoxesStatusCodes.TOO_MANY_SIBS:
									LASADInfo.display("Error", "Exceeds limit of two group links from each box. Link not created.");
									break;
								case GroupedBoxesStatusCodes.TWO_WAY_LINK:
									LASADInfo.display("Error", "Group link would create a two-way link between boxes, which is not allowed. Link not created.");
									break;
								case GroupedBoxesStatusCodes.CANT_BE_GROUPED:
									LASADInfo.display("Error", "Cannot group this box type. Link not created.");
								default:
									Logger.log("ERROR: Unrecognized status code returned from AutoOrganizer.GroupedBoxesCanBeCreated", Logger.DEBUG);
									break;

							}
						}
						
					}
					else if (!alpha.okayForLink(beta))
					{
						LASADInfo.display("Error", "Creating a link here would result in a two-way link - can't create link.");
					}
					else
					{
						onClickSendUpdateToServer(info, myMap.getID(), b1.getConnectedModel().getId() + "", b2.getConnectedModel().getId() + "");
					}
				} else if (myMap.getMyViewSession().getController().getMapInfo().isAllowLinksToLinks() && b1 != null && l2 != null) {
				    //communicator.sendActionPackage(actionBuilder.createLinkWithElements(info, myMap.getID(), b1.getConnectedModel().getId() + "", l2.getConnectedModel().getId() + ""));
					onClickSendUpdateToServer(info, myMap.getID(), b1.getConnectedModel().getId() + "", l2.getConnectedModel().getId() + "");
				}
		    }
		}
		myDialogue.removeFromParent();
		
		
    }
    protected abstract Collection<ElementInfo> getElementsByType(String type);
    protected abstract void onClickSendUpdateToServer(ElementInfo info, String mapId, String firstElemId, String secondElemId);

    private void handleMouseOut(Event be) {
	// End hover effect
	if (((Element) be.getEventTarget().cast()).getClassName().equals("dialog-text-highlighted")) {
	    ((Element) be.getEventTarget().cast()).setClassName("dialog-text");
	}
    }

    private void handleMouseOver(Event be) {
	// Start hover effect
	if (((Element) be.getEventTarget().cast()).getClassName().equals("dialog-text")) {
	    ((Element) be.getEventTarget().cast()).setClassName("dialog-text-highlighted");
	}
    }
}