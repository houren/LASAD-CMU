package lasad.gwt.client.model.organization;
import lasad.gwt.client.model.organization.LinkedBox;

/**
 * OrganizerLink is, like LinkedBox is to AbstractBox, a way of representing an AbstractLinkPanel (and/or an AbstractLink) that is more
 * friendly to AutoOrganizer.  The important info for updating a link via an Action is contained within an OrganizerLink.
 * @author Kevin Loughlin
 * @since 17 June 2015, Updated 28 August 2015
 */
public class OrganizerLink
{
	// Just for helping assign unique linkIDs, not even sure if this is ecessary because LASAD seems to correctly assign link IDs elsewhere
	private static int lastLinkID = -1;

	private LinkedBox startBox;
	private LinkedBox endBox;

	// I.e. what kind of relation (perhaps it could be support, refutation, Linked Premises, depending on the ontology and its terminology)
	private String type;

	// Helps with removal of links, otherwise not needed
	private int linkID;

	// Whether or not this type of OrganizerLink can group boxes together
	private boolean connectsGroup;

	/**
	 * Constructor
	 */
	public OrganizerLink(int linkID, LinkedBox startBox, LinkedBox endBox, String type, boolean connectsGroup)
	{
		this.linkID = linkID;
		lastLinkID = this.linkID;
		this.startBox = startBox;
		this.endBox = endBox;
		this.type = type;
		this.connectsGroup = connectsGroup;
	}

	/**
	 * Somehow this linkID turns out right in the end.  No idea how...
	 */
	public OrganizerLink(LinkedBox startBox, LinkedBox endBox, String type, boolean connectsGroup)
	{
		this.linkID = lastLinkID + 1;
		lastLinkID = this.linkID;
		this.startBox = startBox;
		this.endBox = endBox;
		this.type = type;
		this.connectsGroup = connectsGroup;
	}

	/* Gets */

	public int getLinkID()
	{
		return linkID;
	}

	public LinkedBox getStartBox()
	{
		return startBox;
	}

	public LinkedBox getEndBox()
	{
		return endBox;
	}

	public int getStartBoxID()
	{
		return startBox.getBoxID();
	}

	public int getEndBoxID()
	{
		return endBox.getBoxID();
	}

	public int getStartBoxRootID()
	{
		return startBox.getRootID();
	}

	public int getEndBoxRootID()
	{
		return endBox.getRootID();
	}

	public String getType()
	{
		return type;
	}

	public boolean getConnectsGroup()
	{
		return connectsGroup;
	}

	/* Sets */

	public void setStartBox(LinkedBox startBox)
	{
		this.startBox = startBox;
	}

	public void setEndBox(LinkedBox endBox)
	{
		this.endBox = endBox;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	@Override
	public int hashCode()
	{
		return ((this.startBox.getBoxID())^3 + (this.endBox.getBoxID()^3));
	}

	public OrganizerLink clone()
	{
		return new OrganizerLink(this.getLinkID(), this.getStartBox(), this.getEndBox(), this.getType(), this.getConnectsGroup());
	}

	/* BoxIDs should be unique and thus just checking the start and end as well as type should be sufficient for equality.
		If the invariants of an ArgumentMap change, then this method will need to be updated accordingly. */
	@Override
	public boolean equals(Object object)
	{
		if (object instanceof OrganizerLink)
		{
			OrganizerLink link = (OrganizerLink) object;
			if (startBox.equals(link.getStartBox()) && endBox.equals(link.getEndBox()))
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
		return new String("linkID: " + linkID + "; type: " + type + "; startBox: " + startBox.getRootID() + "; endBox: " + endBox.getRootID());
	}
}