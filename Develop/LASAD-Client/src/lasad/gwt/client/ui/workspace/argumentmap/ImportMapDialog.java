package lasad.gwt.client.ui.workspace.argumentmap;

import com.extjs.gxt.ui.client.widget.Window;
import com.google.gwt.user.client.Element;
import java.util.Vector;

import lasad.gwt.client.LASAD_Client;
import lasad.gwt.client.communication.LASADActionSender;
import lasad.gwt.client.communication.helper.ActionFactory;
import lasad.gwt.client.importer.ImportFileChecker;
import lasad.gwt.client.logger.Logger;
import lasad.gwt.client.ui.workspace.LASADInfo;
import lasad.gwt.client.ui.workspace.loaddialogues.ImportingMapDialogue;
import lasad.gwt.client.ui.workspace.loaddialogues.LoadingMapFromFileDialogue;
import lasad.gwt.client.xml.LoadSessionFromXMLFileParser;
import lasad.shared.communication.objects.ActionPackage;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.core.client.GWT;
import lasad.gwt.client.ui.workspace.tabs.map.ImportMapFormPanel;

public class ImportMapDialog extends Window
{

	public ImportMapDialog()
	{
	}

	@Override
	protected void onRender(Element parent, int index)
	{
		super.onRender(parent, index);
		this.setAutoHeight(true);
		this.setWidth(500);
		this.setAutoHide(true);
		this.add(new ImportMapFormPanel(this));
	}
}

	