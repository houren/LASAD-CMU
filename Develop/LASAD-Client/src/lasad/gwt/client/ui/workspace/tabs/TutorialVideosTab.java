package lasad.gwt.client.ui.workspace.tabs;

import com.extjs.gxt.ui.client.widget.TabItem;
import com.google.gwt.user.client.ui.HTML;
import com.extjs.gxt.ui.client.Style.Scroll;

public class TutorialVideosTab extends TabItem
{
	private final int VID_HEIGHT = 390;
	private final int VID_WIDTH = 640;

	private static TutorialVideosTab instance = null;

	private TutorialVideosTab()
	{
		this.setText("Tutorial Videos");
		this.setClosable(true);
		this.setScrollMode(Scroll.AUTO);
		this.addStyleName("pad-text");
		this.setStyleAttribute("backgroundColor", "#FBFBFB");
		// item.setEnabled(false);

		// TODO This text should be loaded from the server.
		HTML introduction = new HTML();
		introduction
				.setHTML("<br><div align=\"center\"><img src=\"resources/images/lasad-tutorial-videos.png\" border=\"0\">"+vidString("Logging in and Joining Maps", "resources/videos/login.mp4")+vidString("Workspace Tour", "resources/videos/workspace.mp4")+"<font color=#000000></div></font>");
		this.add(introduction);
		
	}

	private String vidString(String title, String filename)
	{
		return "<br><br><h2>"+title+"</h2><video width=\""+VID_WIDTH+"\" height=\""+VID_HEIGHT+"\" style=\"border:5px solid black\" controls><source src=\""+filename+"\" type=\"video/mp4\">Your browser does not support the video tag.</video>";
	}

	public static TutorialVideosTab getInstance()
	{
		if (instance == null)
		{
			instance = new TutorialVideosTab();
		}
		
		return instance;
	}
}