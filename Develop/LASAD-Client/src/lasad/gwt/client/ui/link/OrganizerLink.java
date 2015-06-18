package lasad.gwt.client.ui.link;

/**
 * OrganizerLink is, like LinkedBox is to AbstractBox, a way of representing an AbstractLinkPanel (and/or an AbstractLink) that is more
 * friendly to AutoOrganizer.  The important info for updating a link via an Action is contained within an OrganizerLink.
 * @author Kevin Loughlin
 * @since 17 June 2015, Updated 18 June 2015
 */
public class OrganizerLink
{
	/* Be mindful of difference between boxID and rootID.  I considered making these fields final but then realized I needed to change them
		at one point after making a copy of an OrganizerLink in AutoOrganizer. */
	private int startBoxID;
	private int endBoxID;
	private int startBoxRootID;
	private int endBoxRootID;

	// I.e. what kind of relation (perhaps it could be support, refutation, group, depending on the ontology and its terminology)
	private String type;

	// The only organizer that should be used as of right now
	public OrganizerLink(int startBoxID, int endBoxID, int startBoxRootID, int endBoxRootID, String type)
	{
		this.startBoxID = startBoxID;
		this.endBoxID = endBoxID;
		this.startBoxRootID = startBoxRootID;
		this.endBoxRootID = endBoxRootID;
		this.type = type;
	}

	// Don't use the default constructor, hence why it's set as private and does nothing
	private OrganizerLink()
	{
	}

	public int getStartBoxID()
	{
		return startBoxID;
	}

	public int getEndBoxID()
	{
		return endBoxID;
	}

	public int getStartBoxRootID()
	{
		return startBoxRootID;
	}

	public int getEndBoxRootID()
	{
		return endBoxRootID;
	}

	public String getType()
	{
		return type;
	}

	public void setStartBoxID(int startBoxID)
	{
		this.startBoxID = startBoxID;
	}

	public void setEndBoxID(int endBoxID)
	{
		this.endBoxID = endBoxID;
	}

	public void setStartBoxRootID(int startBoxRootID)
	{
		this.startBoxRootID = startBoxRootID;
	}

	public void setEndBoxRootID(int endBoxRootID)
	{
		this.endBoxRootID = endBoxRootID;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	// For creating a separate version of this same OrganizerLink that can then be updated
	public OrganizerLink copy()
	{
		return new OrganizerLink(this.startBoxID, this.endBoxID, this.startBoxRootID, this.endBoxRootID, this.type);
	}

	/* BoxIDs should be unique and thus just checking the start and end as well as type should be sufficient for equality.
		If the invariants of an ArgumentMap change, then this method will need to be updated accordingly. */
	@Override
	public boolean equals(Object object)
	{
		if (object instanceof OrganizerLink)
		{
			OrganizerLink link = (OrganizerLink) object;
			if (link.getStartBoxID() == this.startBoxID  && link.getEndBoxID() == this.endBoxID && link.getType().equalsIgnoreCase(this.type))
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
	//TODO not necessary for my purposes right now, but a toString method is always nice. Self reminder to come back and write it.
}