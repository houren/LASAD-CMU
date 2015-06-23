package lasad.gwt.client.model.organization;
import lasad.gwt.client.model.organization.LinkedBox;

// Doesn't work --- import lasad.database.DatabaseConnectionHandler;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
	private final int linkID;	
	private LinkedBox startBox;
	private LinkedBox endBox;
	private String type;

	// I.e. what kind of relation (perhaps it could be support, refutation, group, depending on the ontology and its terminology)
	

	//
	public OrganizerLink(int linkID, LinkedBox startBox, LinkedBox endBox, String type)
	{
		this.linkID = linkID;
		this.startBox = startBox;
		this.endBox = endBox;
		this.type = type;
	}

	public OrganizerLink(LinkedBox startBox, LinkedBox endBox, String type)
	{
		/*
		Connection con = null;
		try
		{
			con = DatabaseConnectionHandler.getConnection(OrganizerLink.class);
			PreparedStatement getLastID = con.prepareStatement("SELECT LAST_INSERT_ID()");
			ResultSet rs = getLastID.executeQuery();
			rs.next();
			
			this.linkID = rs.getInt(1);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(con != null) {
//				try {
//					con.close();
					//close Connection
					DatabaseConnectionHandler.closeConnection(OrganizerLink.class, con);
//				} catch (SQLException e){
//					e.printStackTrace();
//				}
			}
		}
		*/
		this.linkID = -1;
		this.startBox = startBox;
		this.endBox = endBox;
		this.type = type;
	}

	// Don't use the default constructor, hence why it's set as private and does nothing
	private OrganizerLink()
	{
		this.linkID = -1;
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

	/* BoxIDs should be unique and thus just checking the start and end as well as type should be sufficient for equality.
		If the invariants of an ArgumentMap change, then this method will need to be updated accordingly. */
	@Override
	public boolean equals(Object object)
	{
		if (object instanceof OrganizerLink)
		{
			OrganizerLink link = (OrganizerLink) object;
			if (this.startBox.equals(link.getStartBox()) && this.endBox.equals(link.getEndBox()) && link.getType().equalsIgnoreCase(this.type))
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