package lasad.gwt.client.ui.workspace.argumentmap;

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
import com.extjs.gxt.ui.client.widget.form.ComboBox;

//import com.extjs.gxt.ui.client.widget.form.CheckBox;

import com.google.gwt.user.client.ui.CheckBox;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;

import lasad.gwt.client.model.organization.AutoOrganizer;
import lasad.gwt.client.model.organization.ArgumentModel;

import com.extjs.gxt.ui.client.widget.Slider;
import com.extjs.gxt.ui.client.widget.form.SliderField;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener; 
import com.extjs.gxt.ui.client.event.BaseEvent;

import lasad.gwt.client.logger.Logger;

/**
 *	Creates the preferences menu that appears when selected from the LASAD menu, found in ArgumentMapMenuBar.
 *	The preferences menu allows the user to select the font size for LASAD, as well as the default box width and size for autoOrganizer.
 *	@author Kevin Loughlin
 *	@since 31 July 2015, Last Updated 31 July 2015
 */
public class CreatePreferencesDialog extends Window
{
	// The default values for the width and height sliders (referring to box width and height upon autoOrganization)
	private final int DEFAULT_WIDTH = 200;
	private final int DEFAULT_HEIGHT = DEFAULT_WIDTH / 2;

	private String mapID;

	private Slider widthSlider;
	private Slider heightSlider;

	private SliderField widthField;
	private SliderField heightField;

	private CheckBox orientUpward;
	private CheckBox orientDownward;

	//private SimpleComboBox<String> fontSizeSelector;

	private FormData formData;

	public CreatePreferencesDialog(String mapID)
	{
		this.mapID = mapID;
	}

	@Override
	protected void onRender(Element parent, int index)
	{
		super.onRender(parent, index);
		this.setAutoHeight(true);
		this.setWidth(500);
		this.setHeading("Auto Organization Preferences");
		formData = new FormData("-20");
		createForm();
	}

	// Changes the font size and box size in live time
	private void createForm()
	{
		// Save these values for cancel button, which will revert to original values
		//final int ORIG_FONT_SIZE = ArgumentModel.getInstanceByMapID(mapID).getFontSize();
		final int ORIG_BOX_WIDTH = AutoOrganizer.getInstanceByMapID(mapID).getBoxWidth();
		final int ORIG_MIN_BOX_HEIGHT = AutoOrganizer.getInstanceByMapID(mapID).getMinBoxHeight();

		FormPanel thisForm = new FormPanel();
		thisForm.setFrame(true);
		thisForm.setHeaderVisible(false);
		thisForm.setAutoHeight(true);
		

		/* fontSizeSelector = new SimpleComboBox<String>();
		fontSizeSelector.setTriggerAction(ComboBox.TriggerAction.ALL);

		fontSizeSelector.setFieldLabel("<font color=\"#000000\">" + "Font Size [" + ORIG_FONT_SIZE + "]" + "</font>");
		fontSizeSelector.setAllowBlank(true);

		// Allow even font sizes from 8-36 as options
		for (int i = 8; i < 37; i += 2)
		{
			fontSizeSelector.add(String.valueOf(i));
		}
		
		fontSizeSelector.addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<String>>()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent<SimpleComboValue<String>> se)
			{
				fontSizeSelector.setFieldLabel("<font color=\"#000000\">" + "Font Size [" + fontSizeSelector.getRawValue() + "]" + "</font>");
			}
		});

		thisForm.add(fontSizeSelector, formData);
*/
		
		orientUpward = new CheckBox();
		orientUpward.setText("Orient Upward");
		//orientUpward.setBoxLabel("Orient Upward");

		orientDownward = new CheckBox();
		orientDownward.setText("Orient Downward");
		//orientDownward.setBoxLabel("Orient Downward");
			

		final boolean IS_DOWNWARD = AutoOrganizer.getInstanceByMapID(mapID).getOrientation();
		if (IS_DOWNWARD)
		{
			orientDownward.setValue(true);
			orientUpward.setValue(false);
		}
		else
		{
			orientDownward.setValue(false);
			orientUpward.setValue(true);
		}

		/*
		orientUpward.addListener(Events.OnClick, new Listener()
		{
			@Override
			public void handleEvent(BaseEvent e)
			{
				orientUpward.setValue(true);
				orientDownward.setValue(false);
			}
		});

		orientDownward.addListener(Events.OnClick, new Listener()
		{
			@Override
			public void handleEvent(BaseEvent e)
			{
				orientUpward.setValue(false);
				orientDownward.setValue(true);
			}
		});
		*/
		

		
		ClickHandler myHandler = new ClickHandler()
		{
			@Override
			public void onClick(ClickEvent event)
			{
				orientUpward.setValue(true);
				orientDownward.setValue(false);
			}

		};
		orientUpward.addClickHandler(myHandler);

		orientDownward.addClickHandler(new ClickHandler()
		{
			@Override
			public void onClick(ClickEvent event)
			{
				orientUpward.setValue(false);
				orientDownward.setValue(true);
			}

		});
		orientUpward.setHTML(orientUpward.getHTML() + "<br>");

		thisForm.add(orientUpward, formData);
		thisForm.add(orientDownward, formData);
			

		widthSlider = new Slider()
		{
			@Override
			protected void onValueChange(int value)
			{
				super.onValueChange(value);
				widthField.setFieldLabel("Box Width [" + value + "]");
			}

		};
		widthSlider.setMinValue(150);
		widthSlider.setMaxValue(450);
		widthSlider.setIncrement(1);
		widthSlider.setMessage("Box Width: {0}");

		widthField = new SliderField(widthSlider);

		// Set the default value as the current box width for auto organizer
		widthSlider.setValue(ORIG_BOX_WIDTH);

		thisForm.add(widthField, formData);

		heightSlider = new Slider()
		{
			@Override
			protected void onValueChange(int value)
			{
				super.onValueChange(value);
				heightField.setFieldLabel("Minimum Box Height [" + value + "]");
			}

		};
		heightSlider.setMinValue(100);
		heightSlider.setMaxValue(450);
		heightSlider.setIncrement(1);
		heightSlider.setMessage("Minimum Box Height: {0}");

		heightField = new SliderField(heightSlider);

		// Set the default value as the current min box height for auto organizer
		heightSlider.setValue(ORIG_MIN_BOX_HEIGHT);

		thisForm.add(heightField, formData);

		// Okay Button
		Button btnOkay = new Button("Organize");
		btnOkay.addSelectionListener(new SelectionListener<ButtonEvent>()
		{
			@Override
			public void componentSelected(ButtonEvent ce)
			{
				/*
				try
				{
					// if fontsizeselector is left blank, this will throw an exception
					int newFontSize = Integer.parseInt(fontSizeSelector.getRawValue());
					ArgumentModel.getInstanceByMapID(mapID).setFontSize(newFontSize);
				}
				catch (NumberFormatException e)
				{
					ArgumentModel.getInstanceByMapID(mapID).setFontSize(ORIG_FONT_SIZE);
				}
				*/

				AutoOrganizer myOrganizer = AutoOrganizer.getInstanceByMapID(mapID);

				myOrganizer.setBoxWidth(widthSlider.getValue());
				myOrganizer.setMinBoxHeight(heightSlider.getValue());
				final boolean DOWNWARD;
				if (orientUpward.getValue())
				{
					DOWNWARD = false;
				}
				else
				{
					DOWNWARD = true;
				}
				myOrganizer.setOrientation(DOWNWARD);
				CreatePreferencesDialog.this.hide();
				myOrganizer.organizeMap();
			}
		});
		thisForm.addButton(btnOkay);

		// Cancel Button
		Button btnCancel = new Button("Cancel");
		btnCancel.addSelectionListener(new SelectionListener<ButtonEvent>()
		{
			@Override
			public void componentSelected(ButtonEvent ce)
			{
				/*
				ArgumentModel.getInstanceByMapID(mapID).setFontSize(ORIG_FONT_SIZE);
				AutoOrganizer.getInstanceByMapID(mapID).setBoxWidth(ORIG_BOX_WIDTH);
				AutoOrganizer.getInstanceByMapID(mapID).setMinBoxHeight(ORIG_MIN_BOX_HEIGHT);
				*/
				CreatePreferencesDialog.this.hide();
			}
		});
		thisForm.addButton(btnCancel);

		thisForm.setButtonAlign(HorizontalAlignment.CENTER);
		FormButtonBinding binding = new FormButtonBinding(thisForm);
		binding.addButton(btnOkay);

		this.add(thisForm);

		/*
		ElementInfo newElement = createNewElement(elInfo);
		// Add default text field
		Vector<Parameter> subElOptions = new Vector<Parameter>();
		Vector<Parameter> subUiOptions = new Vector<Parameter>();
		subUiOptions.add(new Parameter(ParameterTypes.MinHeight, "42"));

		String subElementName = title.getValue() + "-text";
		ElementInfo newElementTextField = OntologyGenerator.createElementInfo(subElementName, "text", 1, 1, 1, subElOptions, subUiOptions);
		newElement.getChildElements().put(subElementName, newElementTextField);
		// SN: Added Awareness Checkbox - Behavior during preview.
		if (addAwarenessCheckbox.getValue()) {
			addAwareness(newElement);
		}

		// SN: End Awareness Checkbox
		

		CreateModifyAndDeleteOntology.getInstance().createPreviewForElement(newElement);
		*/
	}
}