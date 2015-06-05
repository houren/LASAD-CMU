package lasad.entity;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import lasad.Config;
import lasad.database.DatabaseConnectionHandler;
import lasad.entity.helper.Contribution;
import lasad.logging.Logger;
import lasad.shared.communication.objects.Action;
import lasad.shared.communication.objects.parameters.ParameterTypes;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;


public class Ontology {
	
	private int id;
	private String name, xml;
	
	public Ontology(String name, String xml) {
		this.name = name;
		this.xml = xml;
	}
	
	public Ontology(int id, String name, String xml) {
		this(name, xml);
		this.id = id;
	}
	
	public static int getOntologyID(String ontologyName) {
		Connection con = null; 		
		String SQL = null;
		
		try {
			con = DatabaseConnectionHandler.getConnection(Ontology.class);
//			con = DriverManager.getConnection(Config.connection, Config.dbUser, Config.dbPassword);
			
			PreparedStatement getOntologyID = con.prepareStatement("SELECT id FROM "+Config.dbName+".ontologies WHERE name = ?;");
			getOntologyID.setString(1, ontologyName);
			
			SQL = getOntologyID.toString();
			
			ResultSet rs = getOntologyID.executeQuery();
			if(rs.next()) {
				return rs.getInt(1);
			}
			else {
				return -1;
			}			
		} catch (SQLException e){
			System.err.println(SQL);
			e.printStackTrace();
			return -1;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		finally {
			if(con != null) {
				DatabaseConnectionHandler.closeConnection(Ontology.class, con);
//				try {
//					con.close();
//				} catch (SQLException e){
//					e.printStackTrace();
//				}
			}
		}
	}
	
	public static Ontology parseOntologyFromFile(File f) {	   	
		try {
			FileInputStream in = new FileInputStream(f);
	    	
	    	Ontology newOntology = null;
	    	String ontologyName;
	    	
			SAXBuilder builder = new SAXBuilder();
			Document rootdoc = builder.build(in);
			
			Element rootElement = rootdoc.getRootElement();
						
			if(rootElement.getName().equalsIgnoreCase("ontology")){
				
				// Get Ontology Name
				ontologyName = rootElement.getAttributeValue("type");
				
				// Create Ontology Element
				XMLOutputter out = new XMLOutputter();
				String ontology = out.outputString(rootdoc);
				newOntology = new Ontology(ontologyName, ontology);		
			}
			
			return newOntology;
			
		} catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public void saveToDatabase() {
	
		if(Ontology.getOntologyID(this.name) == -1) { // Ontology does not exist
			
			Connection con = null; 		
			
			try {
				con = DatabaseConnectionHandler.getConnection(Ontology.class);
//				con = DriverManager.getConnection(Config.connection, Config.dbUser, Config.dbPassword);
				
				PreparedStatement insertOntology = con.prepareStatement("INSERT INTO "+Config.dbName+".ontologies (id, name, xmlConfig) VALUES (NULL, ?, ?);");
				insertOntology.setString(1, this.name);
				insertOntology.setString(2, this.xml);
				insertOntology.executeUpdate();			
			} catch (SQLException e){
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				if(con != null) {
					DatabaseConnectionHandler.closeConnection(Ontology.class, con);
//					try {
//						con.close();
//					} catch (SQLException e){
//						e.printStackTrace();
//					}
				}
			}
		}
		else {
			// Ontology with this name exists already, do nothing
		}
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getXml() {
		return xml;
	}
	public void setXml(String xml) {
		this.xml = xml;
	}

	public static String getOntologyXML(int ontologyID) {
		
		String ontologyXML = null;
		Connection con = null; 		
		
		try {
			con = DatabaseConnectionHandler.getConnection(Ontology.class);
//			con = DriverManager.getConnection(Config.connection, Config.dbUser, Config.dbPassword);
			
			PreparedStatement getOntologyXML = con.prepareStatement("SELECT xmlConfig FROM "+Config.dbName+".ontologies WHERE id = ?;");
			getOntologyXML.setInt(1, ontologyID);
			
			ResultSet rs = getOntologyXML.executeQuery();
			rs.next();
			
			ontologyXML = rs.getString(1);			
		} catch (SQLException e){
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if(con != null) {
				DatabaseConnectionHandler.closeConnection(Ontology.class, con);
//				try {
//					con.close();
//				} catch (SQLException e){
//					e.printStackTrace();
//				}
			}
		}
		
		return ontologyXML;
	}
	
	public static HashMap<Contribution, Vector<String>> getOntologyElements(String ontologyName) {
		
		HashMap<Contribution, Vector<String>> ontologyElements = new HashMap<Contribution, Vector<String>>();
		
		String XML = getOntologyXML(getOntologyID(ontologyName));
		
		try {
			StringReader in = new StringReader(XML);
	    	
			SAXBuilder builder = new SAXBuilder();
			Document rootdoc = builder.build(in);
			
			Element rootElement = rootdoc.getRootElement();
			Element elementsElement = rootElement.getChild("elements");
			
			List<Element> boxesAndRelations = elementsElement.getChildren();
			
			for(Element boxOrRelation : boxesAndRelations) {
				
				String type = boxOrRelation.getAttributeValue("type");
				type = type.toUpperCase();
				
				Contribution currentContribution = null;
				
				if(type.equals("BOX") || type.equals("RELATION")) {		
					Element elementOptions = boxOrRelation.getChild("elementoptions");
					String name = elementOptions.getAttributeValue("heading");
					
					currentContribution = new Contribution(type, name);
					
					ontologyElements.put(currentContribution, new Vector<String>());
				}
	
				Element childrenElements = boxOrRelation.getChild("childelements");
				List<Element> children = childrenElements.getChildren("element");
				
				for(Element child : children) {
					String childrenType = child.getAttributeValue("elementtype");
					ontologyElements.get(currentContribution).add(childrenType);
				}
			}
			
		} catch(Exception e){
			e.printStackTrace();
			return null;
		}
		
		return ontologyElements;
	}

	public static Vector<Ontology> getOntologyList() {
		Vector<Ontology> ontologyList = new Vector<Ontology>();
		
		Connection con = null; 		
		
		try {
			con = DatabaseConnectionHandler.getConnection(Ontology.class);
//			con = DriverManager.getConnection(Config.connection, Config.dbUser, Config.dbPassword);
			
			Statement getOntologies = con.createStatement();
			getOntologies.executeQuery("SELECT * FROM "+Config.dbName+".ontologies;");
			
			ResultSet rs = getOntologies.executeQuery("SELECT * FROM "+Config.dbName+".ontologies;");
			while(rs.next()) {
				ontologyList.add(new Ontology(rs.getInt("id"), rs.getString("name"), rs.getString("xmlConfig")));
			}
			
		} catch (SQLException e){
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if(con != null) {
				DatabaseConnectionHandler.closeConnection(Ontology.class, con);
//				try {
//					con.close();
//				} catch (SQLException e){
//					e.printStackTrace();
//				}
			}
		}
		return ontologyList;
	}

	public static String getOntologyName(int ontologyId) {
		
		String ontologyName = null;
		Connection con = null; 		
		
		try {
			con = DatabaseConnectionHandler.getConnection(Ontology.class);
//			con = DriverManager.getConnection(Config.connection, Config.dbUser, Config.dbPassword);
			
			PreparedStatement getOntologyXML = con.prepareStatement("SELECT name FROM "+Config.dbName+".ontologies WHERE id = ?;");
			getOntologyXML.setInt(1, ontologyId);
			
			ResultSet rs = getOntologyXML.executeQuery();
			rs.next();
			
			ontologyName = rs.getString(1);			
		} catch (SQLException e){
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if(con != null) {
				DatabaseConnectionHandler.closeConnection(Ontology.class, con);
//				try {
//					con.close();
//				} catch (SQLException e){
//					e.printStackTrace();
//				}
			}
		}
		
		return ontologyName;
	}
	
	public static void removeOntologyFromDB(int ontologyID) {
		Connection con = null; 		
		
		try {
			con = DatabaseConnectionHandler.getConnection(Ontology.class);
//			con = DriverManager.getConnection(Config.connection, Config.dbUser, Config.dbPassword);
			
			PreparedStatement deleteOntology = con.prepareStatement("DELETE FROM "+Config.dbName+".ontologies WHERE id = ? LIMIT 1;");
			deleteOntology.setInt(1, ontologyID);
			
			deleteOntology.executeUpdate();
		} catch (SQLException e){
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if(con != null) {
				DatabaseConnectionHandler.closeConnection(Ontology.class, con);
//				try {
//					con.close();
//				} catch (SQLException e){
//					e.printStackTrace();
//				}
			}
		}
	}
	
	public static void removeFromFileSystem(String ontologyName) {
		File dir = new File(Config.configDir, "ontology");
		File[] ontologies = dir.listFiles();

		for (File f : ontologies) {
			if (!f.isHidden()) {

				Document doc = null;

				try {
					SAXBuilder builder = new SAXBuilder();
					doc = builder.build(f);

					org.jdom.Element root = doc.getRootElement();
					String ontoNameFromFile = root.getAttributeValue("type");
					if (ontoNameFromFile.equals(ontologyName)) {
						Logger.log("Deleting ontology file: " + f);
						f.delete();
						return;
					}
				} catch (JDOMException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
	}
	
	public static boolean delete(int ontologyID) {
		
		// Delete templates that use these ontologies
		Vector<Integer> templateIDs = Template.getIDsOfTemplatesThatUseTheOntology(ontologyID);
		
		for(int i : templateIDs) {
			Template.delete(i);
		}
		
		// Delete ontology from File system
		Ontology.removeFromFileSystem(Ontology.getOntologyName(ontologyID));
		
		// Delete ontology from DB
		Ontology.removeOntologyFromDB(ontologyID);
		
		return true;
	}

	public static Vector<String> getAllOntologyNames() {
		Vector<String> ontologyList = new Vector<String>();
		
		Connection con = null; 		
		
		try {
			con = DatabaseConnectionHandler.getConnection(Ontology.class);
//			con = DriverManager.getConnection(Config.connection, Config.dbUser, Config.dbPassword);
			
			PreparedStatement getAllOntologyNames = con.prepareStatement("SELECT name FROM "+Config.dbName+".ontologies;");
			
			ResultSet rs = getAllOntologyNames.executeQuery();
			
			while(rs.next()) {
				ontologyList.add(rs.getString("name"));
			}
				
		} catch (SQLException e){
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if(con != null) {
				DatabaseConnectionHandler.closeConnection(Ontology.class, con);
//				try {
//					con.close();
//				} catch (SQLException e){
//					e.printStackTrace();
//				}
			}
		}
		
		return ontologyList;
	}

	public static boolean isExisting(String ontologyName) {

		boolean existent = false;
		
		Connection con = null; 		
		
		try {
			con = DatabaseConnectionHandler.getConnection(Ontology.class);
//			con = DriverManager.getConnection(Config.connection, Config.dbUser, Config.dbPassword);
			
			PreparedStatement getOntology = con.prepareStatement("SELECT id FROM "+Config.dbName+".ontologies WHERE name = ?;");
			getOntology.setString(1, ontologyName);
			
			ResultSet rs = getOntology.executeQuery();
			if(rs.next()) {
				existent = true;
			}
						
		} catch (SQLException e){
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if(con != null) {
				DatabaseConnectionHandler.closeConnection(Ontology.class, con);
//				try {
//					con.close();
//				} catch (SQLException e){
//					e.printStackTrace();
//				}
			}
		}
		
		return existent;
	}

//	public static String getOntologyNameFromXMLFile(String ontologyXML) {
//		String title = null;
//		
//		try {
//			StringReader in = new StringReader(ontologyXML);
//	    	
//			SAXBuilder builder = new SAXBuilder();
//			Document rootdoc = builder.build(in);
//			
//			Element rootElement = rootdoc.getRootElement();
//						
//			if(rootElement.getName().equalsIgnoreCase("ontology")){
//				// Get ontology name
//				title = rootElement.getAttributeValue("type");
//			}		
//		} catch(Exception e){
//			e.printStackTrace();
//		}
//		
//		return title;
//	}

	public static void updateOntologyInDB(String ontologyName, String xml) {
		Connection con = null; 		
		
		try {
			con = DatabaseConnectionHandler.getConnection(Ontology.class);
//			con = DriverManager.getConnection(Config.connection, Config.dbUser, Config.dbPassword);
			
			PreparedStatement updateOntology = con.prepareStatement("UPDATE "+Config.dbName+".ontologies SET xmlConfig = ? WHERE name = ?;");
			updateOntology.setString(1, xml);
			updateOntology.setString(2, ontologyName);
			
			updateOntology.executeUpdate();						
		} catch (SQLException e){
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if(con != null) {
				DatabaseConnectionHandler.closeConnection(Ontology.class, con);
//				try {
//					con.close();
//				} catch (SQLException e){
//					e.printStackTrace();
//				}
			}
		}
	}

	public static void updateOntologyInFileSystem(String ontologyName, String xml) {
		try {
			File f = Ontology.getFileOfOntology(ontologyName);
			
			if(f != null) {
				String name = f.getAbsolutePath();
				f.delete();
				f = new File(name);
			}
					
			FileOutputStream out = new FileOutputStream(f);
	    	
			// output a UTF-8 File
			out.write(xml.getBytes("UTF-8"));
			
			out.flush();
			out.close();
			
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	private static File getFileOfOntology(String ontologyName) {
		File dir = new File(Config.configDir, "ontology");
		File[] ontologies = dir.listFiles();

		File returnFile = null;
		
		for (File f : ontologies) {
			if (!f.isHidden()) {
				try {
					FileInputStream in = new FileInputStream(f);
			    	
					SAXBuilder builder = new SAXBuilder();
					Document rootdoc = builder.build(in);
					
					Element rootElement = rootdoc.getRootElement();
								
					if(rootElement.getName().equalsIgnoreCase("ontology")){					
						// Get Ontology Name
						if(!rootElement.getAttributeValue("type").equals(ontologyName)) {
							continue;
						}
						else {
							returnFile = f;
							break;
						}
					}		
				} catch(Exception e){
					e.printStackTrace();
				}
				
			}
		}
		
		return returnFile;		
	}

	public static File generateOntologyConfigurationFile(Action a) {
		
			String name = a.getParameterValue(ParameterTypes.OntologyName);
			String xml = a.getParameterValue(ParameterTypes.OntologyXML);
			
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			
			File ontologyDir = new File(Config.configDir, "ontology");
			File f = new File(ontologyDir, name + " - " + timestamp.toString().substring(0, 10) + ".xml");
			

			try {
				FileOutputStream output = new FileOutputStream(f);
				
				// output a UTF-8 File
				output.write(xml.getBytes("UTF-8"));
				
				output.flush();
				output.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return f;
	}
}
