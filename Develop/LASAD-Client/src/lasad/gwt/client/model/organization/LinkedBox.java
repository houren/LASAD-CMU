package lasad.gwt.client.model.organization;

import java.util.Vector;
import lasad.gwt.client.model.organization.OrganizerLink;

/**
 * LinkedBox is an alternative representation to AbstractBox, more conducive for map organization and modeling.  Each LinkedBox has its boxID
 * and rootID, as well as all its types of Links (i.e. an OrganizerLink that points to the next box). This allows for an easy
 * organization of all Links on the map, as they can be followed in a chain similar to a linked list.  This class is key to AutoOrganizer.
 * All names within this class are self-explanatory.
 * @author Kevin Loughlin
 * @since 11 June 2015, Updated 19 June 2015
 */

public class LinkedBox
{
	private final int MAX_SIBLINGS = 2;
	private final int ERROR = -1;

	// Be mindful of difference between boxID and rootID.
	private final int boxID;
	private final int rootID;

	private Vector<OrganizerLink> childLinks;
	private Vector<OrganizerLink> parentLinks;
	private Vector<OrganizerLink> siblingLinks;

	private int heightLevel;
	private int widthLevel;

	/* Methods are structured so that an update to childGroupLinks or parentGroupLinks will be added to allGroupConenctions.
		childGroupLinks are groupLinks where this box is the parent (i.e. starting box for the link),
		and vice-versa for parentGroupLinks. */

	// I doubt this constructor is ever going to be used, but I made it just in case
	public LinkedBox(int boxID, int rootID, Vector<OrganizerLink> childLinks, Vector<OrganizerLink> parentLinks, Vector<OrganizerLink> siblingLinks, int heightLevel, int widthLevel)
	{
		this.boxID = boxID;
		this.rootID = rootID;
		this.childLinks = childLinks;
		this.parentLinks = parentLinks;
		this.siblingLinks = siblingLinks;
		this.heightLevel = heightLevel;
		this.widthLevel = widthLevel;
	}

	// This is the meat and bones constructor used in AutoOrganizer
	public LinkedBox(int boxID, int rootID)
	{
		this.boxID = boxID;
		this.rootID = rootID;
		this.childLinks = new Vector<OrganizerLink>();
		this.parentLinks = new Vector<OrganizerLink>();
		this.siblingLinks = new Vector<OrganizerLink>();
		this.heightLevel = 0;
		this.widthLevel = 0;
	}

	// I don't want people to use the default constructor because this LinkedBox needs definitive IDs
	private LinkedBox()
	{
		this.boxID = ERROR;
		this.rootID = ERROR;
		this.childLinks = new Vector<OrganizerLink>();
		this.parentLinks = new Vector<OrganizerLink>();
		this.siblingLinks = new Vector<OrganizerLink>();
		this.heightLevel = ERROR;
		this.widthLevel = ERROR;
	}

	public int getBoxID()
	{
		return boxID;
	}

	public int getRootID()
	{
		return rootID;
	}

	public Vector<OrganizerLink> getChildLinks()
	{
		return childLinks;
	}

	public Vector<OrganizerLink> getParentLinks()
	{
		return parentLinks;
	}

	public Vector<OrganizerLink> getSiblingLinks()
	{
		return siblingLinks;
	}

	public void addChildLink(OrganizerLink link)
	{
		this.childLinks.add(link);
	}

	public void addParentLink(OrganizerLink link)
	{
		this.parentLinks.add(link);
	}

	// Like I said above, the next two methods for adding child/parent groupLinks also add the connection to the overall HashMap of groupLinks
	public void addSiblingLink(OrganizerLink link)
	{
		if (siblingLinks.size() < MAX_SIBLINGS)
		{
			this.siblingLinks.add(link);
		}
	}

	public void removeChildLink(OrganizerLink link)
	{
		this.childLinks.remove(link);
	}

	public void removeParentLink(OrganizerLink link)
	{
		this.parentLinks.remove(link);
	}

	// Like I said above, the next two methods for adding child/parent groupLinks also add the connection to the overall HashMap of groupLinks
	public void removeSiblingLink(OrganizerLink link)
	{
		if (siblingLinks.size() < MAX_SIBLINGS)
		{
			this.siblingLinks.remove(link);
		}
	}

	// I don't think checking for null in these "getNum" methods is necessary because I instantiate the HashMaps on creation of this LinkedBox, but it doesn't hurt to check
	public int getNumChildren()
	{
		return childLinks.size();
	}

	public int getNumParents()
	{
		return parentLinks.size();
	}

	public int getNumSiblings()
	{
		return siblingLinks.size();
	}

	public void setHeightLevel(int heightLevel)
	{
		this.heightLevel = heightLevel;
	}

	public void setWidthLevel(int widthLevel)
	{
		this.widthLevel = widthLevel;
	}

	public int getHeightLevel()
	{
		return heightLevel;
	}

	public int getWidthLevel()
	{
		return widthLevel;
	}

	public void removeLinksToSelf()
	{
		Vector<OrganizerLink> childLinks = this.getChildLinks();
		Vector<OrganizerLink> parentLinks = this.getParentLinks();
		Vector<OrganizerLink> siblingLinks = this.getSiblingLinks();

		for (OrganizerLink childLink : childLinks)
		{
			if (childLink.getStartBox().equals(this)) 
			{
				childLink.getEndBox().removeParentLink(childLink);
			}
		}
		for (OrganizerLink parentLink : parentLinks)
		{
			if (parentLink.getEndBox().equals(this)) 
			{
				parentLink.getStartBox().removeChildLink(parentLink);
			}
		}
		for (OrganizerLink siblingLink : siblingLinks)
		{
			if (siblingLink.getStartBox().equals(this)) 
			{
				siblingLink.getEndBox().removeSiblingLink(siblingLink);
			}
			else if (siblingLink.getEndBox().equals(this)) 
			{
				siblingLink.getStartBox().removeSiblingLink(siblingLink);
			}
		}
	}

	public void removeLinkToBoxID(int boxID)
	{
		Vector<OrganizerLink> childLinks = this.getChildLinks();
		Vector<OrganizerLink> parentLinks = this.getParentLinks();
		Vector<OrganizerLink> siblingLinks = this.getSiblingLinks();
		for (OrganizerLink childLink : childLinks)
		{
			LinkedBox endBox = childLink.getEndBox();
			if (endBox.getBoxID() == boxID) 
			{
				this.childLinks.remove(childLink);
				endBox.removeParentLink(childLink);
				return;
			}
		}
		for (OrganizerLink parentLink : parentLinks)
		{
			LinkedBox startBox = parentLink.getStartBox();
			if (startBox.getBoxID() == boxID) 
			{
				this.parentLinks.remove(parentLink);
				startBox.removeChildLink(parentLink);
				return;
			}
		}
		for (OrganizerLink siblingLink : siblingLinks)
		{
			LinkedBox startBox = siblingLink.getStartBox();
			LinkedBox endBox = siblingLink.getEndBox();
			if (startBox.getBoxID() == boxID) 
			{
				this.siblingLinks.remove(siblingLink);
				startBox.removeSiblingLink(siblingLink);
				return;
			}
			else if (endBox.getBoxID() == boxID) 
			{
				this.siblingLinks.remove(siblingLink);
				endBox.removeSiblingLink(siblingLink);
				return;
			}
		}
	}

	// Box IDs are unique and final, so this is all we need for equality for now.  May change this later if I start comparing between maps for some reason
	@Override
	public boolean equals(Object object)
	{
		if (object instanceof LinkedBox)
		{
			LinkedBox otherBox = (LinkedBox) object;
			if (this.boxID == otherBox.getBoxID() || this.rootID == otherBox.getRootID())
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}	
	}

	@Override
	public String toString()
	{
		return "Box RootID: " + Integer.toString(rootID) + "; Box ID " + Integer.toString(boxID) + "\n";
	}

	//TODO not really necessary at this point in time, but it's always good practice to provide a toString and copy method
}