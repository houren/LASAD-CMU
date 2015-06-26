package lasad.gwt.client.model.organization;

import java.util.HashSet;
import lasad.gwt.client.model.organization.OrganizerLink;

/**
 * LinkedBox is an alternative representation to AbstractBox, more conducive for map organization and modeling.  Each LinkedBox has its boxID
 * and rootID, as well as all its types of Links (i.e. an OrganizerLink that points to the next box). This allows for an easy
 * organization of all Links on the map, as they can be followed in a chain similar to a linked list.  This class is key to AutoOrganizer.
 * All names within this class are self-explanatory.
 * @author Kevin Loughlin
 * @since 11 June 2015, Updated 24 June 2015
 */

public class LinkedBox
{
	private final int MAX_SIBLINGS = 2;
	private final int ERROR = -1;

	private final String type;

	// Be mindful of difference between boxID and rootID.
	private final int boxID;
	private final int rootID;

	private HashSet<OrganizerLink> childLinks;
	private HashSet<LinkedBox> childBoxes;

	private HashSet<OrganizerLink> parentLinks;
	private HashSet<LinkedBox> parentBoxes;

	// siblingLinks are those that are of subType "Linked Premises" and are thus handled differently
	private HashSet<OrganizerLink> siblingLinks;
	private HashSet<LinkedBox> siblingBoxes;

	// Height and width level will be used like a coordinate grid once we come to organizeMap() of AutoOrganizer
	private int heightLevel;
	private int widthLevel;

	// This is the meat and bones constructor
	public LinkedBox(int boxID, int rootID, String type)
	{
		this.boxID = boxID;
		this.rootID = rootID;
		this.type = type;
		this.childLinks = new HashSet<OrganizerLink>();
		this.parentLinks = new HashSet<OrganizerLink>();
		this.siblingLinks = new HashSet<OrganizerLink>();
		this.childBoxes = new HashSet<LinkedBox>();
		this.parentBoxes = new HashSet<LinkedBox>();
		this.siblingBoxes = new HashSet<LinkedBox>();
		this.heightLevel = 0;
		this.widthLevel = 0;
	}

	// I don't want people to use the default constructor because this LinkedBox needs definitive IDs.  This just quiets the compiler.
	private LinkedBox()
	{
		this.boxID = ERROR;
		this.rootID = ERROR;
		this.type = "garbage";
		this.childLinks = new HashSet<OrganizerLink>();
		this.parentLinks = new HashSet<OrganizerLink>();
		this.siblingLinks = new HashSet<OrganizerLink>();
		this.childBoxes = new HashSet<LinkedBox>();
		this.parentBoxes = new HashSet<LinkedBox>();
		this.siblingBoxes = new HashSet<LinkedBox>();
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

	public String getType()
	{
		return type;
	}

	public HashSet<OrganizerLink> getChildLinks()
	{
		return childLinks;
	}

	public HashSet<OrganizerLink> getParentLinks()
	{
		return parentLinks;
	}

	public HashSet<OrganizerLink> getSiblingLinks()
	{
		return siblingLinks;
	}

	public HashSet<LinkedBox> getChildBoxes()
	{
		return childBoxes;
	}

	public HashSet<LinkedBox> getParentBoxes()
	{
		return parentBoxes;
	}

	public HashSet<LinkedBox> getSiblingBoxes()
	{
		return siblingBoxes;
	}

	// I naturally avoid duplicates in all add methods by implementing this with HashSet.  Takes care of updating boxes too.

	public void addChildLink(OrganizerLink link)
	{
		this.childLinks.add(link);
		this.childBoxes.add(link.getEndBox());
	}

	public void addParentLink(OrganizerLink link)
	{

		this.parentLinks.add(link);
		this.parentBoxes.add(link.getStartBox());
	}

	public void addSiblingLink(OrganizerLink link)
	{
		this.siblingLinks.add(link);
		if (link.getStartBox().equals(this))
		{
			this.siblingBoxes.add(link.getEndBox());
		}
		else
		{
			this.siblingBoxes.add(link.getStartBox());
		}
	}

	public void removeChildLink(OrganizerLink link)
	{
		this.childLinks.remove(link);
		this.childBoxes.remove(link.getEndBox());
	}

	public void removeParentLink(OrganizerLink link)
	{
		this.parentLinks.remove(link);
		this.parentBoxes.remove(link.getStartBox());
	}

	public void removeSiblingLink(OrganizerLink link)
	{
		this.siblingLinks.remove(link);
		if (link.getStartBox().equals(this))
		{
			this.siblingBoxes.remove(link.getEndBox());
		}
		else
		{
			this.siblingBoxes.remove(link.getStartBox());
		}
	}

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

	/**
	 *	Gets rid of the traces of itself in children/parents/siblings by removing the links to itself from those boxes.
	 *	Will be key for removal later on.
	 */
	public void removeLinksToSelf()
	{
		HashSet<OrganizerLink> childLinks = this.getChildLinks();
		HashSet<OrganizerLink> parentLinks = this.getParentLinks();
		HashSet<OrganizerLink> siblingLinks = this.getSiblingLinks();

		// Remove references to self in children
		for (OrganizerLink childLink : childLinks)
		{
			if (childLink.getStartBox().equals(this)) 
			{
				childLink.getEndBox().removeParentLink(childLink);
			}
		}

		// Remove references to self in parents
		for (OrganizerLink parentLink : parentLinks)
		{
			if (parentLink.getEndBox().equals(this)) 
			{
				parentLink.getStartBox().removeChildLink(parentLink);
			}
		}

		// Remove references to self in siblings
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

	/**
	 *	Gets rid of the specific link from this to boxID
	 */
	public void removeLinkToBoxID(int boxID)
	{
		HashSet<OrganizerLink> childLinks = this.getChildLinks();
		HashSet<OrganizerLink> parentLinks = this.getParentLinks();
		HashSet<OrganizerLink> siblingLinks = this.getSiblingLinks();

		// Check the children
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

		// Check the parents
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

		// Check the siblings
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

		// If link hasn't been found by this point, it doesn't exist and we just return
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
	public int hashCode()
	{
		return this.boxID;
	}

	// Just outputs the box's boxID and rootID, not its children and parents and siblings
	public String toStringShort()
	{
		StringBuilder buffer = new StringBuilder("\n\t\t\t\tBox RootID: " + Integer.toString(rootID) + "; Box ID " + Integer.toString(boxID) + "; Box Type: " + type);
		return buffer.toString();
	}

	/**
	 *	Outputs this box's identification numbers as well as the ID's of all its children/parents/siblings
	 */
	@Override
	public String toString()
	{
		StringBuilder buffer = new StringBuilder("\n\t\tBEGIN BOX\n\t\tBox RootID: " + Integer.toString(rootID) + "; Box ID " + Integer.toString(boxID) + "; Box Type: " + type);
		buffer.append("\n\t\t\tCHILD BOXES...");
		for (OrganizerLink childLink : childLinks)
		{
			buffer.append(childLink.getEndBox().toStringShort());
		}
		buffer.append("\n\t\t\tPARENT BOXES...");
		for (OrganizerLink parentLink : parentLinks)
		{
			buffer.append(parentLink.getStartBox().toStringShort());
		}
		buffer.append("\n\t\t\tSIBLING BOXES...");
		for (OrganizerLink siblingLink : siblingLinks)
		{
			if (siblingLink.getEndBox().equals(this))
			{
				buffer.append(siblingLink.getStartBox().toStringShort());
			}
			else if (siblingLink.getStartBox().equals(this))
			{
				buffer.append(siblingLink.getEndBox().toStringShort());
			}
		}
		buffer.append("\n\t\tEND BOX\n");
		return buffer.toString();
	}
}