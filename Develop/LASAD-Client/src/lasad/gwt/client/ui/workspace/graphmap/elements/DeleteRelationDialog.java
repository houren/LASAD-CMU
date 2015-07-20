package lasad.gwt.client.ui.workspace.graphmap.elements;

import lasad.gwt.client.model.AbstractMVController;
import lasad.gwt.client.model.ElementInfo;
import lasad.gwt.client.ui.box.AbstractBox;
import lasad.gwt.client.ui.link.AbstractLink;
import lasad.gwt.client.ui.workspace.LASADInfo;
import lasad.shared.communication.objects.parameters.ParameterTypes;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.user.client.Element;

import lasad.gwt.client.model.organization.ArgumentModel;
import lasad.gwt.client.model.organization.LinkedBox;
import lasad.gwt.client.model.organization.OrganizerLink;
import lasad.gwt.client.model.organization.ArgumentThread;
import lasad.gwt.client.model.organization.AutoOrganizer;
import lasad.gwt.client.logger.Logger;

import lasad.gwt.client.ui.workspace.graphmap.AbstractGraphMap;

import lasad.gwt.client.LASAD_Client;
import lasad.gwt.client.communication.LASADActionSender;
import lasad.gwt.client.communication.helper.ActionFactory;
import lasad.gwt.client.model.argument.MVController;

import java.util.HashSet;

/**
 * This class creates the dialog box for when the user selects to add a relation via the argument map drop down menu.
 * If creating links via dragging, see AbstractCreateLinkDialog (link only) and AbstractCreateBoxLinkDialog (link and box).
 * Documentation added by Kevin Loughlin, 16 June 2015
 * Modified by Kevin Loughlin 6 July 2015 to add limitations on what types of links can be added and when.
 * @author Unknown
 */

public class DeleteRelationDialog extends Window {

//	private final LASADActionSender communicator = LASADActionSender.getInstance();
//	private final ActionFactory actionBuilder = ActionFactory.getInstance();
//	private MVController myController;

	protected FormData formData;
	protected SimpleComboBox<String> comboStart = new SimpleComboBox<String>();
	protected SimpleComboBox<String> comboEnd = new SimpleComboBox<String>();
	protected String correspondingMapId;

	// Maps ROOTELEMENTID to corresponding element
	private ArgumentModel argModel;
	private HashSet<LinkedBox> boxes;
	private final LASADActionSender communicator = LASADActionSender.getInstance();
	private final ActionFactory actionBuilder = ActionFactory.getInstance();
	private MVController myController = null;

	protected boolean allowLinksToLinks;

	public DeleteRelationDialog(String mapId) {
		this.correspondingMapId = mapId;
		this.argModel = ArgumentModel.getInstanceByMapID(mapId);
		this.boxes = argModel.getBoxes();
		this.allowLinksToLinks = AbstractGraphMap.getInstanceByMapID(correspondingMapId).getMyViewSession().getController().getMapInfo().isAllowLinksToLinks();
	}

	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		this.setAutoHeight(true);
		this.setWidth(200);
		this.setHeading("Delete relation...");
		formData = new FormData("-20");
		createForm();
	}

	private void createForm() {
		FormPanel simple = new FormPanel();
		simple.setFrame(true);
		simple.setHeaderVisible(false);
		simple.setAutoHeight(true);

		// Fill combo boxes
		comboStart.setFieldLabel("<font color=\"#000000\">" + "Start" + "</font>");
		comboStart.setAllowBlank(false);
		for (LinkedBox box : argModel.getBoxes())
		{
			if (box.getNumRelations() > 0)
			{
				Integer rootID = box.getRootID();
				comboStart.add(rootID.toString());
			}
		}

		simple.add(comboStart, formData);

		comboEnd.setFieldLabel("<font color=\"#000000\">" + "End" + "</font>");
		comboEnd.setAllowBlank(false);

		/*
		if (this.getAllowLinksToLinks())
		{
			for(Map.Entry<String, AbstractLink> entry : links.entrySet())
			{
				String details = entry.getValue().getElementInfo().getElementOption(ParameterTypes.Details);
				if (details == null || details.equalsIgnoreCase("true"))
				{
					comboEnd.add(entry.getKey());
				}
			}
		}
		*/

		comboEnd.setEnabled(false);
		simple.add(comboEnd, formData);

		// Filter comboEnd depending on comboStart selection
		comboStart.addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<String>>()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent<SimpleComboValue<String>> se)
			{
				comboEnd.removeAll();
				// Important to use rootID
				LinkedBox startBox = argModel.getBoxByRootID(Integer.parseInt(comboStart.getRawValue()));
				HashSet<LinkedBox> relatedBoxes = startBox.getRelatedBoxes();

				for (LinkedBox box : relatedBoxes)
				{
					Integer rootID = box.getRootID();
					comboEnd.add(rootID.toString());
				}

				if (getMyController() == null) {
//					myController = LASAD_Client.getMVCController(correspondingMapId);
					setMyController();
				}

				comboEnd.setEnabled(true);
			}
		});

		comboEnd.addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<String>>()
		{

			@Override
			public void selectionChanged(SelectionChangedEvent<SimpleComboValue<String>> se)
			{
				comboStart.removeAll();
				// Important to use rootID
				LinkedBox endBox = argModel.getBoxByRootID(Integer.parseInt(comboEnd.getRawValue()));
				HashSet<LinkedBox> relatedBoxes = endBox.getRelatedBoxes();

				for (LinkedBox box : relatedBoxes)
				{
					Integer rootID = box.getRootID();
					comboStart.add(rootID.toString());
				}

				if (getMyController() == null) {
//					myController = LASAD_Client.getMVCController(correspondingMapId);
					setMyController();
				}
			}
		});

		// Okay Button
		Button btnOkay = new Button("Ok");
		btnOkay.addSelectionListener(new SelectionListener<ButtonEvent>()
		{
			@Override
			public void componentSelected(ButtonEvent ce)
			{
				LinkedBox startBox = argModel.getBoxByRootID(Integer.parseInt(comboStart.getRawValue()));
				LinkedBox endBox =  argModel.getBoxByRootID(Integer.parseInt(comboEnd.getRawValue()));
				OrganizerLink link = startBox.getConnectingLink(endBox);
				if (link != null)
				{
					onClickSendRemoveElementToServer(correspondingMapId, link.getLinkID());
				}
				else
				{
					LASADInfo.display("Error", "Link does not exist between these boxes.");
				}
				//communicator.sendActionPackage(actionBuilder.createLinkWithElements(config, correspondingMapId, startId, endId));
				
				DeleteRelationDialog.this.hide();
			}
		});
		simple.addButton(btnOkay);

		// Cancel Button
		Button btnCancel = new Button("Cancel");
		btnCancel.addSelectionListener(new SelectionListener<ButtonEvent>()
		{
			@Override
			public void componentSelected(ButtonEvent ce)
			{
				DeleteRelationDialog.this.hide();
			}
		});
		simple.addButton(btnCancel);

		simple.setButtonAlign(HorizontalAlignment.CENTER);
		FormButtonBinding binding = new FormButtonBinding(simple);
		binding.addButton(btnOkay);

		this.add(simple);
	}
	protected void onClickSendRemoveElementToServer(String mapID, int linkID)
	{
		communicator.sendActionPackage(actionBuilder.removeElement(mapID, linkID));
	}
	protected AbstractMVController getMyController()
	{
		return myController;
	}
	protected void setMyController()
	{
		myController = LASAD_Client.getMVCController(correspondingMapId);
	}
}