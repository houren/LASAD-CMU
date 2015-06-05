package lasad.gwt.client.ui.workspace.tabs.authoring.helper.lists;

import com.extjs.gxt.ui.client.data.BaseTreeModel;

public class TemplateChild extends BaseTreeModel {

	public TemplateChild() {

	}

	public TemplateChild(String name) {
		set("name", name);
	}

	public String getName() {
		return (String) get("name");
	}

	public String toString() {
		return getName();
	}

}
