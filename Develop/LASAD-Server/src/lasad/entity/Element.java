package lasad.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import lasad.Config;
import lasad.State;
import lasad.database.DatabaseConnectionHandler;
import lasad.helper.ActionPackageFactory;
import lasad.shared.communication.objects.Action;
import lasad.shared.communication.objects.parameters.ParameterTypes;

public class Element {
	
	private int id;
	private int map_id;
	private int start_revision_id;
//	private int end_revision_id;
	
	private String typeValue;
	private State currentServerState;
	
	public Element(int mapID, Action a, int revision_id, State state) {
		this.map_id = mapID;
		this.start_revision_id = revision_id;
		this.currentServerState = state;
		
		this.typeValue = a.getParameterValue(ParameterTypes.Type);
		
		Connection con = null;
		PreparedStatement insertElement = null;
		PreparedStatement getLastID = null;
		ResultSet rs = null;		
		
		try {
			
//			con = DriverManager.getConnection(Config.connection, Config.dbUser, Config.dbPassword);
			con = DatabaseConnectionHandler.getConnection(Element.class);
			
			insertElement = con.prepareStatement("INSERT INTO "+Config.dbName+".elements (id, map_id, start_revision_id, end_revision_id, type) VALUES (NULL, ?, ?, NULL, ?);");
			insertElement.setInt(1, map_id);
			insertElement.setInt(2, start_revision_id);
			insertElement.setString(3, this.typeValue);
			insertElement.executeUpdate();		
			
			getLastID = con.prepareStatement("SELECT LAST_INSERT_ID()");
			rs = getLastID.executeQuery();
			rs.next();
			
			this.id = rs.getInt(1);
			
			if(typeValue.equalsIgnoreCase("box") || typeValue.equalsIgnoreCase("emptybox") || typeValue.equalsIgnoreCase("relation") || typeValue.equalsIgnoreCase("emptyrelation") || typeValue.equalsIgnoreCase("feedback-cluster")) {
				
				// The secondLastTopLevelElementID is required if the user creates a box and a relation in the same step to avoid the relation to use itself as parent
				this.currentServerState.secondLastTopLevelElementID = this.currentServerState.lastTopLevelElementID;
				this.currentServerState.lastTopLevelElementID = this.id;
			}
			
			// Add parent mapping to database. One element, e.g. a relation, can have more than one parent, e.g. start and end node of a relation.
			Vector<String> parents = a.getParameterValues(ParameterTypes.Parent);
			if(parents != null) {
				for(String s : parents) {
					if(s.equalsIgnoreCase("LAST-ID")) {
						if(typeValue.equalsIgnoreCase("relation")) {
							addParentMapping(con, id, this.currentServerState.secondLastTopLevelElementID);
						}
						else {
							addParentMapping(con, id, this.currentServerState.lastTopLevelElementID);
						}
					}
					else {
						addParentMapping(con, id, Integer.parseInt(s));
					}
				}
			}
			
			
		} catch (SQLException e){
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try{insertElement.close();}catch(Exception e){}
			try{getLastID.close();}catch(Exception e){}
			try{rs.close();}catch(Exception e){}
			if(con != null) {
//				try {
//					con.close();
					//close Connection
					DatabaseConnectionHandler.closeConnection(Element.class, con);
//				} catch (SQLException e){
//					e.printStackTrace();
//				}
			}
		}

	}
	
	private void addParentMapping(Connection con, int elementID, int parentElementID) throws SQLException {

		PreparedStatement insertMapping = con.prepareStatement("INSERT INTO "+Config.dbName+".element_parent_mapping (id, element_id, parent_element_id) VALUES (NULL, ?, ?);");
		insertMapping.setInt(1, elementID);
		insertMapping.setInt(2, parentElementID);
		insertMapping.executeUpdate();					
		try{insertMapping.close();}catch(Exception e){}
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public static void updateEndRevisionID(int elementID, int endRevisionID) {
		
		Connection con = null;
		PreparedStatement endRevision = null;
		
		try {
//			con = DriverManager.getConnection(Config.connection, Config.dbUser, Config.dbPassword);
			con = DatabaseConnectionHandler.getConnection(Element.class);
			
			// Update end revision id
			endRevision = con.prepareStatement("UPDATE "+Config.dbName+".elements SET end_revision_id = ? WHERE id = ?;");
			endRevision.setInt(1, endRevisionID);
			endRevision.setInt(2, elementID);

			endRevision.executeUpdate();
			
		} catch (SQLException e){
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try{endRevision.close();}catch(Exception e){}
			if(con != null) {
				//close Connection
				DatabaseConnectionHandler.closeConnection(Element.class, con);
//				try {
//					con.close();
//				} catch (SQLException e){
//					e.printStackTrace();
//				}
			}
		}
	}

	public static void updateEndRevisionIDs(Vector<Integer> childElementsIDs, int endRevisionID) {
		if(childElementsIDs.size() > 0) {
			Connection con = null;
			PreparedStatement endRevision = null;
			
			try {
				con = DatabaseConnectionHandler.getConnection(Element.class);
//				con = DriverManager.getConnection(Config.connection, Config.dbUser, Config.dbPassword);
				
				// Update end revision id
				endRevision = con.prepareStatement("UPDATE "+Config.dbName+".elements SET end_revision_id = ? WHERE id = ?;");
				
				for(Integer i : childElementsIDs) {
					endRevision.setInt(1, endRevisionID);
					endRevision.setInt(2, i);
					endRevision.executeUpdate();
				}
		
			} catch (SQLException e){
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			finally {
    			try { endRevision.close(); } catch (Exception e) { /* ignored */ }
				if(con != null) {
					//close Connection
					DatabaseConnectionHandler.closeConnection(Element.class, con);
//					try {
//						con.close();
//					} catch (SQLException e){
//						e.printStackTrace();
//					}
				}
			}
		}
	}

	public static Vector<Integer> getChildElementIDs(int elementID) {
		Vector<Integer> children = new Vector<Integer>();
		
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement childElement = null;
		
		try {
			con = DatabaseConnectionHandler.getConnection(Element.class);
//			con = DriverManager.getConnection(Config.connection, Config.dbUser, Config.dbPassword);
			
			childElement = con.prepareStatement("SELECT element_id FROM "+Config.dbName+".element_parent_mapping WHERE parent_element_id = ?;");
			childElement.setInt(1, elementID);

			rs = childElement.executeQuery();
			
			while(rs.next()) {
				children.add(rs.getInt(1));
			}
			
		} catch (SQLException e){
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
    		try { childElement.close(); } catch (Exception e) { /* ignored */ }
			if(con != null) {
				//close Connection
				DatabaseConnectionHandler.closeConnection(Element.class, con);
//				try {
//					con.close();
//				} catch (SQLException e){
//					e.printStackTrace();
//				}
			}
		}
		
		return children;
	}
	
	public static String getLastValueOfElementParameter(int elementID, String parameter) {
		String value = null;
		
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement elementValue = null;
		
		try {
			con = DatabaseConnectionHandler.getConnection(Element.class);
//			con = DriverManager.getConnection(Config.connection, Config.dbUser, Config.dbPassword);
			
			elementValue = con.prepareStatement("SELECT value, revision_id FROM "+Config.dbName+".actions WHERE element_id = ? AND parameter = ? ORDER BY revision_id DESC LIMIT 1;");
			elementValue.setInt(1, elementID);
			elementValue.setString(2, parameter);

			rs = elementValue.executeQuery();
			
			if(rs.next()) {
				value = rs.getString("value");
			}
			
		} catch (SQLException e){
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
    		try { elementValue.close(); } catch (Exception e) { /* ignored */ }
			if(con != null) {
				//close Connection
				DatabaseConnectionHandler.closeConnection(Element.class, con);
//				try {
//					con.close();
//				} catch (SQLException e){
//					e.printStackTrace();
//				}
			}
		}
		
		return value;
		
	}

	public static int getStartRevisionID(int elementID) {
		
		int result = 0;
		
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement childElement = null;

		try {
			con = DatabaseConnectionHandler.getConnection(Element.class);
//			con = DriverManager.getConnection(Config.connection, Config.dbUser, Config.dbPassword);
			
			childElement = con.prepareStatement("SELECT start_revision_id FROM "+Config.dbName+".elements WHERE id = ?;");
			childElement.setInt(1, elementID);

			rs = childElement.executeQuery();
			
			if(rs.next()) {
				result = rs.getInt(1);
			}
			
		} catch (SQLException e){
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
    		try { childElement.close(); } catch (Exception e) { /* ignored */ }
			if(con != null) {
				//close Connection
				DatabaseConnectionHandler.closeConnection(Element.class, con);
//				try {
//					con.close();
//				} catch (SQLException e){
//					e.printStackTrace();
//				}
			}
		}
		return result;
	}

	public static Vector<Integer> getParentElementIDs(int elementID) {
		Vector<Integer> parents = new Vector<Integer>();
		
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement childElement = null;
		
		try {
			con = DatabaseConnectionHandler.getConnection(Element.class);
//			con = DriverManager.getConnection(Config.connection, Config.dbUser, Config.dbPassword);
			
			childElement = con.prepareStatement("SELECT parent_element_id FROM "+Config.dbName+".element_parent_mapping WHERE element_id = ?;");
			childElement.setInt(1, elementID);

			rs = childElement.executeQuery();
			
			while(rs.next()) {
				parents.add(rs.getInt(1));
			}
			
		} catch (SQLException e){
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
    		try { childElement.close(); } catch (Exception e) { /* ignored */ }
			if(con != null) {
				//close Connection
				DatabaseConnectionHandler.closeConnection(Element.class, con);
//				try {
//					con.close();
//				} catch (SQLException e){
//					e.printStackTrace();
//				}
			}
		}
		
		return parents;
	}

	public static Vector<Integer> getAllElementsDeletedOnRevision(int revisionID) {
		Vector<Integer> elementsDeletedOnRevision = new Vector<Integer>();
		
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement childElement = null;
		
		try {
			con = DatabaseConnectionHandler.getConnection(Element.class);
//			con = DriverManager.getConnection(Config.connection, Config.dbUser, Config.dbPassword);
			
			childElement = con.prepareStatement("SELECT id FROM "+Config.dbName+".elements WHERE end_revision_id = ? ORDER BY id DESC;");
			childElement.setInt(1, revisionID);

			rs = childElement.executeQuery();
			
			while(rs.next()) {
				elementsDeletedOnRevision.add(rs.getInt("id"));
			}
			
		} catch (SQLException e){
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
    		try { childElement.close(); } catch (Exception e) { /* ignored */ }
			if(con != null) {
				//close Connection
				DatabaseConnectionHandler.closeConnection(Element.class, con);
//				try {
//					con.close();
//				} catch (SQLException e){
//					e.printStackTrace();
//				}
			}
		}
		
		return elementsDeletedOnRevision;
	}

	public static int getAwarenessCursorElementID(int mapID, int userID) {
		
		int elementID = 0;
		
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement awarenessCursorElement = null;
		
		try {
			con = DatabaseConnectionHandler.getConnection(Element.class);
//			con = DriverManager.getConnection(Config.connection, Config.dbUser, Config.dbPassword);
			
			awarenessCursorElement = con.prepareStatement("SELECT E.id FROM "+Config.dbName+".elements AS E INNER JOIN "+Config.dbName+".revisions AS R ON E.start_revision_id = R.id WHERE E.map_id = ? AND type = ? AND R.creator_user_id = ? AND E.end_revision_id IS NULL;");
			awarenessCursorElement.setInt(1, mapID);
			awarenessCursorElement.setString(2, "awareness-cursor");
			awarenessCursorElement.setInt(3, userID);

			rs = awarenessCursorElement.executeQuery();
			
			if(rs.next()) {
				elementID = rs.getInt("id");
			}
			
		} catch (SQLException e){
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
    		try { awarenessCursorElement.close(); } catch (Exception e) { /* ignored */ }
			if(con != null) {
				//close Connection
				DatabaseConnectionHandler.closeConnection(Element.class, con);
//				try {
//					con.close();
//				} catch (SQLException e){
//					e.printStackTrace();
//				}
			}
		}
		return elementID;
	}

	public static Action removeLastActiveLockOfUser(User u, int mapID) {
		Action a = null;
		
		int lockedElementID = 0;
		
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement getLastLockedElementID = null;
		
		try {
			con = DatabaseConnectionHandler.getConnection(Element.class);
//			con = DriverManager.getConnection(Config.connection, Config.dbUser, Config.dbPassword);
			
			getLastLockedElementID = con.prepareStatement("SELECT A.element_id, A.value FROM "+Config.dbName+".actions AS A INNER JOIN "+Config.dbName+".revisions AS R ON A.revision_id = R.id INNER JOIN "+Config.dbName+".users AS U ON R.creator_user_id = U.id WHERE parameter = ? AND R.creator_user_id = ? AND R.map_id = ? ORDER BY R.id DESC LIMIT 1;");
			getLastLockedElementID.setString(1, "STATUS");
			getLastLockedElementID.setInt(2, u.getUserID());
			getLastLockedElementID.setInt(3, mapID);

			rs = getLastLockedElementID.executeQuery();
			
			if(rs.next()) {
				if(rs.getString("value").equalsIgnoreCase("LOCK")) {
					lockedElementID = rs.getInt("element_id");	
				}
			}
			
		} catch (SQLException e){
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
    		try { getLastLockedElementID.close(); } catch (Exception e) { /* ignored */ }
			if(con != null) {
				//close Connection
				DatabaseConnectionHandler.closeConnection(Element.class, con);
//				try {
//					con.close();
//				} catch (SQLException e){
//					e.printStackTrace();
//				}
			}
		}
		
		if(lockedElementID != 0) {
			a = ActionPackageFactory.getUnlockElementAction(u, mapID, lockedElementID);
		}
		return a;
		
	}

	public static Vector<Integer> getIDsOfElementsConnectedToRevision(int revID) {
		Vector<Integer> elementIDs = new Vector<Integer>();

		Connection con = null;
		ResultSet rs = null;
		PreparedStatement getRevisionIDs = null;
		try {
			con = DatabaseConnectionHandler.getConnection(Element.class);
//			con = DriverManager.getConnection(Config.connection, Config.dbUser, Config.dbPassword);

			getRevisionIDs = con.prepareStatement("SELECT id FROM " + Config.dbName + ".elements WHERE start_revision_id = ? OR end_revision_id = ?;");
			getRevisionIDs.setInt(1, revID);
			getRevisionIDs.setInt(2, revID);

			rs = getRevisionIDs.executeQuery();

			while (rs.next()) {
				elementIDs.add(rs.getInt(1));
			}			

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
    		try { getRevisionIDs.close(); } catch (Exception e) { /* ignored */ }
			if (con != null) {
				//close Connection
				DatabaseConnectionHandler.closeConnection(Element.class, con);
//				try {
//					con.close();
//				} catch (SQLException e) {
//					e.printStackTrace();
//				}
			}
		}

		return elementIDs;
	}

	public static void delete(int elementID) {
		Connection con = null;
		PreparedStatement deleteElement = null; 
		PreparedStatement deleteElementMappings = null;		
		
		try {
			con = DatabaseConnectionHandler.getConnection(Element.class);
//			con = DriverManager.getConnection(Config.connection, Config.dbUser, Config.dbPassword);
			
			// Delete actual element
			
			deleteElement = con.prepareStatement("DELETE FROM "+Config.dbName+".elements WHERE id = ? LIMIT 1;");
			deleteElement.setInt(1, elementID);
			deleteElement.executeUpdate();
			
			// Delete element_parent_mapping with this element
			deleteElementMappings = con.prepareStatement("DELETE FROM "+Config.dbName+".element_parent_mapping WHERE element_id = ? OR parent_element_id = ?;");
			deleteElementMappings.setInt(1, elementID);
			deleteElementMappings.setInt(2, elementID);
			deleteElementMappings.executeUpdate();			
			
		} catch (SQLException e){
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try{ deleteElement.close(); } catch(Exception e){}
			try{ deleteElementMappings.close(); } catch(Exception e){}
			if(con != null) {
				//close Connection
				DatabaseConnectionHandler.closeConnection(Element.class, con);
//				try {
//					con.close();
//				} catch (SQLException e){
//					e.printStackTrace();
//				}
			}
		}
	}

	public static boolean isElementActive(int elementID) {
		
		boolean isActive = true;
		
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement endRevision = null;

		try {
			con = DatabaseConnectionHandler.getConnection(Element.class);
//			con = DriverManager.getConnection(Config.connection, Config.dbUser, Config.dbPassword);
			
			endRevision = con.prepareStatement("SELECT end_revision_id FROM "+Config.dbName+".elements WHERE id = ?;");
			endRevision.setInt(1, elementID);
		
			rs = endRevision.executeQuery();
			
			if(rs.next()) {
				if(rs.getInt("end_revision_id") != 0) { // SQL NULL is 0
					isActive = false;
				}
			}
			
		} catch (SQLException e){
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try{ rs.close(); } catch(Exception e){}
			try{ endRevision.close(); } catch(Exception e){}
			if(con != null) {
				//close Connection
				DatabaseConnectionHandler.closeConnection(Element.class, con);
//				try {
//					con.close();
//				} catch (SQLException e){
//					e.printStackTrace();
//				}
			}
		}
		return isActive;
	}

	public static void updateModificationTime(int id, long modificationTime) {
		Connection con = null;
		PreparedStatement updModificationTime = null;
		try {
			con = DatabaseConnectionHandler.getConnection(Element.class);
//			con = DriverManager.getConnection(Config.connection, Config.dbUser, Config.dbPassword);
			
			// Update end revision id
			updModificationTime = con.prepareStatement("UPDATE "+Config.dbName+".elements SET last_modified = ? WHERE id = ? LIMIT 1;");
			updModificationTime.setLong(1, modificationTime);
			updModificationTime.setInt(2, id);

			updModificationTime.executeUpdate();
			
		} catch (SQLException e){
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try{ updModificationTime.close(); } catch(Exception e){}
			if(con != null) {
				//close Connection
				DatabaseConnectionHandler.closeConnection(Element.class, con);
//				try {
//					con.close();
//				} catch (SQLException e){
//					e.printStackTrace();
//				}
			}
		}
	}
	
	public static long getLastModificationTime(int elementID) {
		long lastModificationTime = 0;
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement childElement = null;
		
		try {
			con = DatabaseConnectionHandler.getConnection(Element.class);
//			con = DriverManager.getConnection(Config.connection, Config.dbUser, Config.dbPassword);
			
			childElement = con.prepareStatement("SELECT last_modified FROM "+Config.dbName+".elements WHERE id = ?;");
			childElement.setInt(1, elementID);

			rs = childElement.executeQuery();
			
			if(rs.next()) {
				lastModificationTime = rs.getLong("last_modified");
			}
			
		} catch (SQLException e){
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
    		try { childElement.close(); } catch (Exception e) { /* ignored */ }
			if(con != null) {
				//close Connection
				DatabaseConnectionHandler.closeConnection(Element.class, con);
//				try {
//					con.close();
//				} catch (SQLException e){
//					e.printStackTrace();
//				}
			}
		}
		
		return lastModificationTime;
	}
	
}