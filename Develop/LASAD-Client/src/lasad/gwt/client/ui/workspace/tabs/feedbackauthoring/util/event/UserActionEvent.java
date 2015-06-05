package lasad.gwt.client.ui.workspace.tabs.feedbackauthoring.util.event;

import lasad.gwt.client.ui.workspace.tabs.feedbackauthoring.grid.CustomizedGrid;

/**
 * @author Anahuac
 *
 */
public interface UserActionEvent {
	
	public abstract String getProperty(ActionUserEventProperty name);
	
	public abstract void addProperty(ActionUserEventProperty name, String value);
	
}
