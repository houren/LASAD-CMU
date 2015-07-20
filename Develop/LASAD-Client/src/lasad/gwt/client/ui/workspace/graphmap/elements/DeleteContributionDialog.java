package lasad.gwt.client.ui.workspace.graphmap.elements;

import java.util.HashSet;

import lasad.gwt.client.model.AbstractMVController;
import lasad.gwt.client.model.ElementInfo;

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

import lasad.gwt.client.logger.Logger;

import lasad.gwt.client.LASAD_Client;
import lasad.gwt.client.communication.LASADActionSender;
import lasad.gwt.client.communication.helper.ActionFactory;
import lasad.gwt.client.model.argument.MVController;

/**
 * This class creates the dialog box for when the user selects to add a relation via the argument map drop down menu.
 * If creating links via dragging, see AbstractCreateLinkDialog (link only) and AbstractCreateBoxLinkDialog (link and box).
 * Documentation added by Kevin Loughlin, 16 June 2015
 * Modified by Kevin Loughlin 6 July 2015 to add limitations on what types of links can be added and when.
 * @author Unknown
 */

public class DeleteContributionDialog extends Window {

//	private final LASADActionSender communicator = LASADActionSender.getInstance();
//	private final ActionFactory actionBuilder = ActionFactory.getInstance();
//	private MVController myController;

	protected FormData formData;
	protected SimpleComboBox<String> boxSelector = new SimpleComboBox<String>();

	protected String correspondingMapId;

	// Maps ROOTELEMENTID to corresponding element

	private ArgumentModel argModel;
	private HashSet<LinkedBox> boxes;
	private final LASADActionSender communicator = LASADActionSender.getInstance();
	private final ActionFactory actionBuilder = ActionFactory.getInstance();
	private MVController myController = null;

	public DeleteContributionDialog(String mapId)
	{
		this.correspondingMapId = mapId;
		this.argModel = ArgumentModel.getInstanceByMapID(mapId);
		this.boxes = argModel.getBoxes();
	}

	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		this.setAutoHeight(true);
		this.setWidth(200);
		this.setHeading("Delete contribution...");
		formData = new FormData("-20");
		createForm();
	}

	private void createForm() {
		FormPanel simple = new FormPanel();
		simple.setFrame(true);
		simple.setHeaderVisible(false);
		simple.setAutoHeight(true);

		// Fill combo boxes
		boxSelector.setFieldLabel("<font color=\"#000000\">" + "Contribution" + "</font>");
		boxSelector.setAllowBlank(false);
		for (LinkedBox box : boxes)
		{
			Integer rootID = box.getRootID();
			boxSelector.add(rootID.toString());
		}

		simple.add(boxSelector, formData);

		// Filter comboEnd depending on comboStart selection
		boxSelector.addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<String>>()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent<SimpleComboValue<String>> se)
			{
				if (getMyController() == null)
				{
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
				try
				{
					int rootID = Integer.parseInt(boxSelector.getRawValue());
					onClickSendRemoveElementToServer(correspondingMapId, rootID);
					DeleteContributionDialog.this.hide();
				}
				catch (Exception e)
				{
					Logger.log("EXCEPTION THROWN IN DELETE CONTRIBUTION", Logger.DEBUG);
				}
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
				DeleteContributionDialog.this.hide();
			}
		});
		simple.addButton(btnCancel);

		simple.setButtonAlign(HorizontalAlignment.CENTER);
		FormButtonBinding binding = new FormButtonBinding(simple);
		binding.addButton(btnOkay);

		this.add(simple);
	}
	protected void onClickSendRemoveElementToServer(String mapID, int rootID)
	{
		int boxID = argModel.getBoxByRootID(rootID).getBoxID();
		communicator.sendActionPackage(actionBuilder.removeElement(mapID, boxID));
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