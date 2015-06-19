package lasad.gwt.client.model.organization;

import java.util.Vector;
import lasad.gwt.client.model.organization.OrganizerLink;

/**
 * LinkedBox is an alternative representation to AbstractBox, more conducive for map organization and modeling.  Each LinkedBox has its boxID
 * and rootID, as well as all its types of Pointers (i.e. a linked box and the type of link to that linked box). This allows for an easy
 * organization of all Pointers on the map, as they can be followed in a chain similar to a linked this.  This class is key to AutoOrganizer.
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

	private Vector<OrganizerLink> childPointers;
	private Vector<OrganizerLink> parentPointers;
	private Vector<OrganizerLink> siblingPointers;

	private int heightLevel;
	private int widthLevel;

	/* Methods are structured so that an update to childGroupPointers or parentGroupPointers will be added to allGroupConenctions.
		childGroupPointers are groupPointers where this box is the parent (i.e. starting box for the link),
		and vice-versa for parentGroupPointers. */

	// I doubt this constructor is ever going to be used, but I made it just in case
	public LinkedBox(int boxID, int rootID, Vector<OrganizerLink> childPointers, Vector<OrganizerLink> parentPointers, Vector<OrganizerLink> siblingPointers, int heightLevel, int widthLevel)
	{
		this.boxID = boxID;
		this.rootID = rootID;
		this.childPointers = childPointers;
		this.parentPointers = parentPointers;
		this.siblingPointers = siblingPointers;
		this.heightLevel = heightLevel;
		this.widthLevel = widthLevel;
	}

	// This is the meat and bones constructor used in AutoOrganizer
	public LinkedBox(int boxID, int rootID)
	{
		this.boxID = boxID;
		this.rootID = rootID;
		this.childPointers = new Vector<OrganizerLink>();
		this.parentPointers = new Vector<OrganizerLink>();
		this.siblingPointers = new Vector<OrganizerLink>();
		this.heightLevel = 0;
		this.widthLevel = 0;
	}

	// I don't want people to use the default constructor because this LinkedBox needs definitive IDs
	private LinkedBox()
	{
		this.boxID = ERROR;
		this.rootID = ERROR;
		this.childPointers = new Vector<OrganizerLink>();
		this.parentPointers = new Vector<OrganizerLink>();
		this.siblingPointers = new Vector<OrganizerLink>();
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
		return childPointers;
	}

	public Vector<OrganizerLink> getParentLinks()
	{
		return parentPointers;
	}

	public Vector<OrganizerLink> getSiblingLinks()
	{
		return siblingPointers;
	}

	public void addChildConnection(OrganizerLink link)
	{
		this.childPointers.add(link);
	}

	public void addParentConnection(OrganizerLink link)
	{
		this.parentPointers.add(link);
	}

	// Like I said above, the next two methods for adding child/parent groupPointers also add the connection to the overall HashMap of groupPointers
	public void addSiblingConnection(OrganizerLink link)
	{
		if (siblingPointers.size() < MAX_SIBLINGS)
		{
			this.siblingPointers.add(link);
		}
	}

	// I don't think checking for null in these "getNum" methods is necessary because I instantiate the HashMaps on creation of this LinkedBox, but it doesn't hurt to check
	public int getNumChildren()
	{
		return childPointers.size();
	}

	public int getNumParents()
	{
		return parentPointers.size();
	}

	public int getNumSiblings()
	{
		return siblingPointers.size();
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

	// Box IDs are unique and final, so this is all we need for equality for now.  May change this later if I start comparing between maps for some reason
	@Override
	public boolean equals(Object object)
	{
		if (object instanceof LinkedBox)
		{
			if (this.boxID == ( (LinkedBox) object).boxID )
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

	//TODO not really necessary at this point in time, but it's always good practice to provide a toString and copy method
}