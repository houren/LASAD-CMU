package lasad.gwt.client.ui.box;

import java.util.HashMap;
import lasad.gwt.client.ui.link.OrganizerLink;

/**
 * LinkedBox is an alternative representation to AbstractBox, more conducive for map organization and modeling.  Each LinkedBox has its boxID
 * and rootID, as well as all its types of connections (i.e. a linked box and the type of link to that linked box). This allows for an easy
 * organization of all Connections on the map, as they can be followed in a chain similar to a linked this.  This class is key to AutoOrganizer.
 * All names within this class are self-explanatory.
 * @author Kevin Loughlin
 * @since 11 June 2015, Updated 18 June 2015
 */

public class LinkedBox
{
	// Be mindful of difference between boxID and rootID.
	private final int boxID;
	private final int rootID;

	private HashMap<LinkedBox, OrganizerLink> childConnections = null;
	private HashMap<LinkedBox, OrganizerLink> parentConnections = null;

	/* Methods are structured so that an update to childGroupConnections or parentGroupConnections will be added to allGroupConenctions.
		childGroupConnections are groupConnections where this box is the parent (i.e. starting box for the link),
		and vice-versa for parentGroupConnections. */
	private HashMap<LinkedBox, OrganizerLink> allGroupConnections = null;
	private HashMap<LinkedBox, OrganizerLink> childGroupConnections = null;
	private HashMap<LinkedBox, OrganizerLink> parentGroupConnections = null;

	// One of the keys to organization is assigning a height and width from 1 to max height. Think of it like the first quadrant of a coordinate grid.
	private int heightLevel = 0;
	private int widthLevel = 0;

	// I doubt this constructor is ever going to be used, but I made it just in case
	public LinkedBox(int boxID, int rootID, HashMap<LinkedBox, OrganizerLink> childConnections, HashMap<LinkedBox, OrganizerLink> parentConnections, HashMap<LinkedBox, OrganizerLink> allGroupConnections, HashMap<LinkedBox, OrganizerLink> childGroupConnections, HashMap<LinkedBox, OrganizerLink> parentGroupConnections)
	{
		this.boxID = boxID;
		this.rootID = rootID;
		this.childConnections = childConnections;
		this.parentConnections = parentConnections;
		this.allGroupConnections = allGroupConnections;
		this.childGroupConnections = childGroupConnections;
		this.parentGroupConnections = parentGroupConnections;
	}

	// This is the meat and bones constructor used in AutoOrganizer
	public LinkedBox(int boxID, int rootID)
	{
		this.boxID = boxID;
		this.rootID = rootID;
		this.childConnections = new HashMap<LinkedBox, OrganizerLink>();
		this.parentConnections = new HashMap<LinkedBox, OrganizerLink>();
		this.allGroupConnections = new HashMap<LinkedBox, OrganizerLink>();
		this.childGroupConnections = new HashMap<LinkedBox, OrganizerLink>();
		this.parentGroupConnections = new HashMap<LinkedBox, OrganizerLink>();
	}

	// I don't want people to use the default constructor because this LinkedBox needs definitive IDs
	private LinkedBox()
	{
		this.boxID = -1;
		this.rootID = -1;
		this.childConnections = new HashMap<LinkedBox, OrganizerLink>();
		this.parentConnections = new HashMap<LinkedBox, OrganizerLink>();
		this.allGroupConnections = new HashMap<LinkedBox, OrganizerLink>();
		this.childGroupConnections = new HashMap<LinkedBox, OrganizerLink>();
		this.parentGroupConnections = new HashMap<LinkedBox, OrganizerLink>();
	}

	public int getBoxID()
	{
		return boxID;
	}

	public int getRootID()
	{
		return rootID;
	}

	public HashMap<LinkedBox, OrganizerLink> getChildConnections()
	{
		return childConnections;
	}

	public HashMap<LinkedBox, OrganizerLink> getParentConnections()
	{
		return parentConnections;
	}

	public HashMap<LinkedBox, OrganizerLink> getAllGroupConnections()
	{
		return allGroupConnections;
	}

	public HashMap<LinkedBox, OrganizerLink> getChildGroupConnections()
	{
		return childGroupConnections;
	}

	public HashMap<LinkedBox, OrganizerLink> getParentGroupConnections()
	{
		return parentGroupConnections;
	}

	public void addChildConnection(LinkedBox childBox, OrganizerLink link)
	{
		this.childConnections.put(childBox, link);
	}

	public void addParentConnection(LinkedBox parentBox, OrganizerLink link)
	{
		this.parentConnections.put(parentBox, link);
	}

	// Like I said above, the next two methods for adding child/parent groupConnections also add the connection to the overall HashMap of groupConnections
	public void addChildGroupConnection(LinkedBox childGroupBox, OrganizerLink link)
	{
		if (!(childGroupConnections.containsKey(childGroupBox) ) )
		{
			this.childGroupConnections.put(childGroupBox, link);
		}
		if (!(allGroupConnections.containsKey(childGroupBox) ) )
		{
			this.allGroupConnections.put(childGroupBox, link);
		}
	}

	public void addParentGroupConnection(LinkedBox parentGroupBox, OrganizerLink link)
	{
		if (!(parentGroupConnections.containsKey(parentGroupBox) ) )
		{
			this.parentGroupConnections.put(parentGroupBox, link);
		}
		if (!(allGroupConnections.containsKey(parentGroupBox) ) )
		{
			this.allGroupConnections.put(parentGroupBox, link);
		}
	}

	// I don't think checking for null in these "getNum" methods is necessary because I instantiate the HashMaps on creation of this LinkedBox, but it doesn't hurt to check
	public int getNumChildren()
	{
		if (childConnections == null)
		{
			return 0;
		}
		else
		{
			return childConnections.size();
		}
	}

	public int getNumParents()
	{
		if (parentConnections == null)
		{
			return 0;
		}
		else
		{
			return parentConnections.size();
		}
	}

	public int getNumAllGroupConnections()
	{
		if (allGroupConnections == null)
		{
			return 0;
		}
		else
		{
			return allGroupConnections.size();
		}
	}

	public int getNumChildGroupConnections()
	{
		if (childGroupConnections == null)
		{
			return 0;
		}
		else
		{
			return childGroupConnections.size();
		}
	}

	public int getNumParentGroupConnections()
	{
		if (parentGroupConnections == null)
		{
			return 0;
		}
		else
		{
			return parentGroupConnections.size();
		}
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