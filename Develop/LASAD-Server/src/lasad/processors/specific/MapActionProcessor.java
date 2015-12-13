package lasad.processors.specific;

import java.util.Vector;

import lasad.controller.ManagementController;
import lasad.entity.ActionParameter;
import lasad.entity.Element;
import lasad.entity.Map;
import lasad.entity.Revision;
import lasad.entity.User;
import lasad.helper.ActionPackageFactory;
import lasad.logging.Logger;
import lasad.processors.ActionObserver;
import lasad.processors.ActionProcessor;
import lasad.shared.communication.objects.Action;
import lasad.shared.communication.objects.ActionPackage;
import lasad.shared.communication.objects.Parameter;
import lasad.shared.communication.objects.categories.Categories;
import lasad.shared.communication.objects.commands.Commands;
import lasad.shared.communication.objects.parameters.ParameterTypes;

import edu.cmu.pslc.logging.element.ConditionElement;
import edu.cmu.pslc.logging.element.CustomFieldElement;
import edu.cmu.pslc.logging.element.DatasetElement;
import edu.cmu.pslc.logging.element.InterpretationElement;
import edu.cmu.pslc.logging.element.LevelElement;
import edu.cmu.pslc.logging.element.MetaElement;
import edu.cmu.pslc.logging.element.ProblemElement;
import edu.cmu.pslc.logging.element.PropertyElement;
import edu.cmu.pslc.logging.element.SkillElement;
import edu.cmu.pslc.logging.element.StepElement;
import edu.cmu.pslc.logging.element.StepSequenceElement;
import edu.cmu.pslc.logging.OliDatabaseLogger;
import edu.cmu.pslc.logging.*;

import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;

/**
 * this class handles all actions about map
 * 
 * @author ?
 */
public class MapActionProcessor extends AbstractActionObserver implements ActionObserver {

	private OliDatabaseLogger dsLogger;
	private HashMap<String, HashSet<String>> mapBoxes;
	private HashMap<String, HashSet<String>> mapLinks;

	// userName
	private HashSet<String> harrellClass;

	// user, sessionID
	private HashMap<String, String> loggedSessions;

	public MapActionProcessor()
	{
		super();
		dsLogger = OliDatabaseLogger.create("https://pslc-qa.andrew.cmu.edu/log/server", "UTF-8");
		mapBoxes = new HashMap<String, HashSet<String>>();
		mapLinks = new HashMap<String, HashSet<String>>();

		loggedSessions = new HashMap<String, String>();
		harrellClass = new HashSet<String>();
		harrellClass.add("Sam.Speight");
		harrellClass.add("Mara.Harrell");

		harrellClass.add("Sonia.Lee");
		harrellClass.add("Jeremy.Meola");
		harrellClass.add("Sascha.Demetris");
		harrellClass.add("Kajae.Jones");
		harrellClass.add("Jonathan.Li");

		harrellClass.add("Nicole.Matamala");
		harrellClass.add("Brad.Edgington");
		harrellClass.add("Dave.Singh");
		harrellClass.add("Izzy.Roscoe");
		harrellClass.add("Simone.Schneeberg");

		harrellClass.add("Natsuha.Omori");
		harrellClass.add("Bobbie.Chen");
		harrellClass.add("Brendan.Wixen");
		harrellClass.add("Oliver.Liburd");
		harrellClass.add("Kevin.Riordan");
	}

	/**
     * Logs some random actions.
     */
    private void logToDataShop(Action a, User u)
    {
    	try
    	{
			String userName = u.getNickname();
	        String sessionId = u.getSessionID();

	        if (loggedSessions.get(userName) == null || !loggedSessions.get(userName).equals(sessionId))
	        {
	        	dsLogger.logSession(userName, sessionId);
	        	loggedSessions.put(userName, sessionId);
	        }

	        String timeString = Long.toString(a.getTimeStamp());
		    String timeZone = "UTC";
		    MetaElement metaElement = new MetaElement(userName, sessionId, timeString, timeZone);
			ContextMessage contextMsg = ContextMessage.createStartProblem(metaElement);

			String problemName = Map.getMapName(this.aproc.getMapIDFromAction(a));
			ProblemElement problem = new ProblemElement(problemName);

	        if (harrellClass.contains(userName))
	        {
	        	LevelElement sectionLevel = new LevelElement("Section", "01", problem);

		        String className = "Engineering Ethics";
		        String school = "CMU";
		        String period = "01";
		        String instructorOne = "Mara Harrell";

		        contextMsg.setClassName(className);
		        contextMsg.setSchool(school);
		        contextMsg.setPeriod(period);
		        contextMsg.addInstructor(instructorOne);
		        contextMsg.setDataset(new DatasetElement("Engineering-Ethics-Test-2", sectionLevel));
	        }
	        else
	        {
	        	LevelElement sectionLevel = new LevelElement("Section", "", problem);

	        	contextMsg.setClassName("");
		        contextMsg.setSchool("");
		        contextMsg.setPeriod("");
		        contextMsg.addInstructor("");
	        	contextMsg.setDataset(new DatasetElement("LASAD-V2", sectionLevel));
	        }

	        ToolMessage toolMsg = ToolMessage.create(contextMsg);
	        String selection;
	        if (a.getParameterValue(ParameterTypes.Id) == null)
	        {
	        	selection = "";
	        }
	        else
	        {
	        	selection = a.getParameterValue(ParameterTypes.Id);
	        }
	        String action = a.getCmd().name();
	        final String input;

	        if (action.equals("CreateElement"))
	        {
	        	input = "";

	        	String type = a.getParameterValue(ParameterTypes.Type);
	        	if (type == null)
	        	{
	        		return;
	        	}
	        	else if (type.equals("box"))
	        	{
	        		if (!mapBoxes.keySet().contains(problemName) || mapBoxes.get(problemName) == null)
	        		{
	        			HashSet<String> boxes = new HashSet<String>();
	        			boxes.add(selection);
	        			mapBoxes.put(problemName, boxes);
	        		}
	        		else
	        		{
	        			mapBoxes.get(problemName).add(selection);
	        		}
	        		toolMsg.setAsAttempt("Create Box");
	        	}
	        	else if (type.equals("relation"))
	        	{
	        		if (!mapLinks.keySet().contains(problemName))
	        		{
	        			HashSet<String> links = new HashSet<String>();
	        			links.add(selection);
	        			mapLinks.put(problemName, links);
	        		}
	        		else
	        		{
	        			mapLinks.get(problemName).add(selection);
	        		}
	        		toolMsg.setAsAttempt("Create Relation");
	        	}
	        	else
	        	{
	        		return;
	        	}
	        }
	        else if (action.equals("UpdateElement"))
	        {
	        	String text = a.getParameterValue(ParameterTypes.Text);
		        if (text == null)
		        {
		        	String posX = a.getParameterValue(ParameterTypes.PosX);
		        	if (posX == null)
		        	{
		        		String width = a.getParameterValue(ParameterTypes.Width);
		        		if (width == null)
		        		{
		        			return;
		        		}
		        		else
		        		{
		        			input = "Width: " + width + "; Height: " + a.getParameterValue(ParameterTypes.Height);
		        			toolMsg.setAsAttempt("Resize Element");
		        		}
		        	}
		        	else
		        	{
		        		input = "PosX: " + posX + "; PosY: " + a.getParameterValue(ParameterTypes.PosY);
		        		toolMsg.setAsAttempt("Reposition Element");
		        	}
		        }
		        else
		        {
		        	toolMsg.setAsAttempt("Modify Element Text");
		        	input = text;
		        }
	        }
	        else if (action.equals("DeleteElement"))
	        {
	        	if (mapBoxes.get(problemName).remove(selection))
	        	{
	        		toolMsg.setAsAttempt("Delete Box");
        		}
        		else if (mapLinks.get(problemName).remove(selection))
        		{
        			toolMsg.setAsAttempt("Delete Relation");
        		}
	        	else
	        	{
	        		return;
	        	}
	        	input = "";
	        }
	        else if (action.equals("AutoOrganize"))
	        {
	        	toolMsg.setAsAttempt("");
	        	String orientationBool = a.getParameterValue(ParameterTypes.OrganizerOrientation);
	        	String orient;
	        	if (Boolean.parseBoolean(orientationBool))
	        	{
	        		orient = "downward";
	        	}
	        	else
	        	{
	        		orient = "upward";
	        	}
	        	String width = a.getParameterValue(ParameterTypes.OrganizerBoxWidth);
	        	String height = a.getParameterValue(ParameterTypes.OrganizerBoxHeight);
	        	input = "Orientation: " + orient + "; Width: " + width + "; Height: " + height;
	        }
	        else if (action.equals("ChangeFontSize"))
	        {
	        	toolMsg.setAsAttempt("");
	        	input = "Font Size: " + a.getParameterValue(ParameterTypes.FontSize);
	        }
	        else
	        {
	        	return;
	        }

	        if (selection != null && action != null && input != null)
	        {
	        	toolMsg.addSai(selection, action, input);
	        }
	        else
	        {
	        	Logger.debugLog("ERROR: cannot log becase sai is null for following action!");
	        	Logger.debugLog(a.toString());
	        	return;
	        }

	        if (!dsLogger.log(contextMsg))
	        {
	        	Logger.debugLog("ERROR: context Message log failed for following action!");
	        	Logger.debugLog(a.toString());
	        	return;
	        }

	        if (!dsLogger.log(toolMsg))
	        {
	        	Logger.debugLog("ERROR: tool Message log failed for following action!");
	        	Logger.debugLog(a.toString());
	        	return;
	        }
		}	    	
        catch(Exception e)
        {
        	Logger.debugLog("ERROR: Exception thrown for following action!");
        	Logger.debugLog(a.toString());
        	Logger.debugLog("EXCEPTION INFO...");
			Logger.debugLog(e.toString());
			Logger.debugLog(e.getStackTrace().toString());
        } 	
    }

    /**
	 * create an Element in a map,for Example Box, Link etc. save it in the database
	 * 
	 * @param a a specific LASAD action
	 * @param u the User,who owns this map
	 * @author ZGE
	 */
	public void processCreateElement(Action a, User u) {
		int mapID = this.aproc.getMapIDFromAction(a);

		Vector<String> parents = a.getParameterValues(ParameterTypes.Parent);
		if (parents != null) {
			if (!parentsDoExist(a.getParameterValues(ParameterTypes.Parent))) {
				Logger.log("One of the parents is no longer active. Create element failed.");
				ActionPackage ap = ActionPackageFactory.error("One of the parents is no longer present on map. Create element failed");
				Logger.doCFLogging(ap);
				ManagementController.addToUsersActionQueue(ap, u.getSessionID());
				return;
			}
		}

		if (u.getSessionID().equals("DFKI") && Map.getMapName(mapID) == null) {
			Logger.log("[ActionProcessor.processCreateElement] ERROR: No LASAD map for ID submitted from xmpp - " + mapID
					+ " - Ignoring create action - \n" + a);
			return;
		}
		// Create new revision of the map
		Revision r = createNewRevision(mapID, u, a);

		r.saveToDatabase();

		// Create an element in the database with type information; replace
		// parents (LAST-ID) with correct top level element's ID
		Element e = new Element(mapID, a, r.getId(), myServer.currentState);

		// Replace TIME, ROOTELEMENTID, PARENT with actual values instead of
		// place holders
		a = replacePlaceHoldersAddIDsAddMetadata(a, e.getId(), u.getNickname());

		// Save action parameters for the revision
		ActionParameter.saveParametersForRevision(r.getId(), e.getId(), a);

		// Add to users' action queues
		ActionPackage ap = ActionPackage.wrapAction(a);
		Logger.doCFLogging(ap);
		ManagementController.addToAllUsersOnMapActionQueue(ap, mapID);
	}

	/**
	 * check if the elements parents exist
	 * 
	 * @param parameterVector a set of parameters describing the parents
	 * @return a boolean value , true the parents exist or false the parents don't exist
	 * @author ZGE
	 */
	private boolean parentsDoExist(Vector<String> parameterVector) {
		for (String p : parameterVector) {
			int elementID = -1;
			if (p.equalsIgnoreCase("LAST-ID")) {
				if ("LAST-ID".equalsIgnoreCase(p)) {
					elementID = myServer.currentState.lastTopLevelElementID;
				}
			} else {
				elementID = Integer.parseInt(p);
			}

			if (!Element.isElementActive(elementID)) {
				Logger.debugLog("ERROR: Element " + elementID + " is no longer active!");
				return false;
			} else {
				Logger.log("Element " + elementID + " is still active.");
			}
		}
		return true;
	}

	/**
	 * create a new revision of map and save it in the database
	 * 
	 * @param mapID the ID of this map
	 * @param u the owner of this map
	 * @param a the current action to create a new Revision
	 * @return the instance of the revision
	 * @author ZGE
	 */
	private Revision createNewRevision(int mapID, User u, Action a) {
		// Create new revision of the map
		Revision r;
		// special case for agent creating nodes for others
		if (u.getSessionID().equals("DFKI")) {

			String username = a.getParameterValue(ParameterTypes.UserName);
			if (username != null) {
				int userId = User.getId(username);
				if (userId != -1) {
					r = new Revision(mapID, userId);
				} else {
					r = new Revision(mapID, u.getUserID());
					Logger.log("[ActionProcessor.processCreateElement] ERROR: Non-LASAD username submitted from xmpp - " + username
							+ ", using username but default will appear upon relogin");
				}
			} else {
				r = new Revision(mapID, u.getUserID());
			}
		} else {
			r = new Revision(mapID, u.getUserID());
		}

		return r;
	}

	/**
	 * Replaces TIME, ROOTELEMENTID, PARENT with actual values instead of place holders
	 * 
	 * @param a
	 * @return
	 */
	private Action replacePlaceHoldersAddIDsAddMetadata(Action a, int ID, String username) {

		int mapID = this.aproc.getMapIDFromAction(a);

		boolean usernamePresent = false, idPresent = false;
		boolean relation = false;
		boolean addRootElementID = false;

		for (Parameter p : a.getParameters()) {

			// To decide which type of this action is
			switch (p.getType()) {
			case UserName:
				usernamePresent = true;
				break;
			case Type:
				if (p.getValue().equalsIgnoreCase("relation") || p.getValue().equalsIgnoreCase("emptyrelation")) {
					addRootElementID = true;
					relation = true;
				} else if (p.getValue().equalsIgnoreCase("box") || p.getValue().equalsIgnoreCase("emptybox")) {
					addRootElementID = true;
				}
				break;
			case Id:
				idPresent = true;
				break;
			}

			// Replace LAST-ID of parent parameters
			// The secondLastTopLevelElementID is required if the user creates a
			// box and a relation in the same step to avoid the relation to use
			// itself as parent
			if ("LAST-ID".equalsIgnoreCase(p.getValue())) {
				if (!relation) {
					p.setValue(myServer.currentState.lastTopLevelElementID + "");
				} else {
					p.setValue(myServer.currentState.secondLastTopLevelElementID + "");
				}
			}

			// Replace CURRENT-TIME of time parameters
			else if ("CURRENT-TIME".equalsIgnoreCase(p.getValue())) {
				p.setValue(System.currentTimeMillis() + "");
			}
		}// end for

		if (!idPresent) {
			a.addParameter(ParameterTypes.Id, ID + "");
		}

		if (!usernamePresent) {
			a.addParameter(ParameterTypes.UserName, username);
		}

		if (addRootElementID) {
			if (a.getParameterValue(ParameterTypes.RootElementId) == null) {
				a.addParameter(ParameterTypes.RootElementId, Map.getNewRootElementID(mapID) + "");
			}
		}

		return a;
	}

	/**
	 * after changing the status of the element in the map the relevant values in database are changed too and the relevant users
	 * are informed.
	 * 
	 * @param a a special lasad action
	 * @param u the current user
	 * @author ZGE
	 */
	public void processUpdateElement(Action a, User u) {
		a.addParameter(ParameterTypes.Received, System.currentTimeMillis() + "");

		int mapID = this.aproc.getMapIDFromAction(a);
		int elementID = Integer.parseInt(a.getParameterValue(ParameterTypes.Id));

		synchronized (ActionProcessor.DeleteUpdateJoinActionLock) {

			// Action is already obsolete
			if (Element.getLastModificationTime(elementID) > Long.parseLong(a.getParameterValue(ParameterTypes.Received))) {
				return;
			}

			if (!Element.isElementActive(elementID)) {
				Logger.log("Element " + elementID + " is no longer active. Update failed.");
				ActionPackage ap = ActionPackageFactory.error("Element is no longer present on map. Update failed");
				Logger.doCFLogging(ap);
				ManagementController.addToUsersActionQueue(ap, u.getSessionID());
				return;
			}

			// Create new revision of the map
			Revision r = new Revision(mapID, u.getUserID());
			r.saveToDatabase();

			// If it is a lock / unlock update of an element, check if element is already locked or unlocked
			if (a.getParameterValue(ParameterTypes.Status) != null) {
				String lockStatus = Element.getLastValueOfElementParameter(elementID, "STATUS");
				if (a.getParameterValue(ParameterTypes.Status).equalsIgnoreCase(lockStatus)) {
					return;
				}
			}

			// Update elements last update time
			Element.updateModificationTime(elementID, Long.parseLong(a.getParameterValue(ParameterTypes.Received)));

			// Add the username, etc.
			a = replacePlaceHoldersAddIDsAddMetadata(a, elementID, u.getNickname());

			// Save action parameters for the revision
			ActionParameter.saveParametersForRevision(r.getId(), elementID, a);

			// Add to users' action queues
			if (a.getCmd().equals(Commands.UpdateCursorPosition)) {
				ActionPackage ap = ActionPackage.wrapAction(a);
				Logger.doCFLogging(ap);
				ManagementController.addToAllUsersButMySelfOnMapActionQueue(ap, mapID, u);
			} else {
				ActionPackage ap = ActionPackage.wrapAction(a);
				Logger.doCFLogging(ap);
				ManagementController.addToAllUsersOnMapActionQueue(ap, mapID);
			}
		}
	}

	/**
	 * update the Cursor
	 * 
	 * @param a a special lasad action
	 * @param u the current user
	 * @author ZGE
	 */
	public void processCursorUpdate(Action a, User u) {
		if ("TRUE".equalsIgnoreCase(a.getParameterValue(ParameterTypes.Persistent))) {
			processUpdateElement(a, u);
		} else {
			distributeToAllUsersButMeWithoutSaving(a, u);
		}
	}

	/**
	 * TODO?
	 * 
	 * @param a, u
	 * @author ZGE
	 */
	private void distributeToAllUsersButMeWithoutSaving(Action a, User u) {
		int mapID = this.aproc.getMapIDFromAction(a);

		ActionPackage ap = ActionPackage.wrapAction(a);
		Logger.doCFLogging(ap);
		ManagementController.addToAllUsersButMySelfOnMapActionQueue(ap, mapID, u);
	}

	/**
	 * Delete the element and all of his relevant elements like his children etc.
	 * 
	 * @param a a special lasad action
	 * @param u the current user
	 * @author ZGE
	 */
	public void processDeleteElement(Action a, User u) {

		int mapID = this.aproc.getMapIDFromAction(a);
		int elementID = Integer.parseInt(a.getParameterValue(ParameterTypes.Id));

		synchronized (ActionProcessor.DeleteUpdateJoinActionLock) {

			if (!Element.isElementActive(elementID)) {
				Logger.log("Element " + elementID + " is no longer active. Delete failed.");
				// Not necessary -> ActionPackage ap = ActionPackageFactory.error("Element is already deleted. Delete failed");
				// Logger.doCFLogging(ap);
				// ManagementController.addToUsersActionQueue(ap, u.getSessionID());
				return;
			}

			// Create new revision of the map
			Revision r = new Revision(mapID, u.getUserID());
			r.saveToDatabase();

			ActionParameter.saveParametersForRevision(r.getId(), elementID, a);

			deleteElementAndChildrenStub(elementID, r.getId(), mapID, u.getNickname(), u.getSessionID());
		}
	}

	/**
	 * collect all informations of the element's children to prepare to delete
	 * 
	 * @param elementID the Id of element
	 * @param revisionID the ID of revision
	 * @param mapID the ID of the Map
	 * @param username the name of User
	 * @param sessionID the ID of the Session
	 * @author ZGE
	 */

	private void deleteElementAndChildrenStub(int elementID, int revisionID, int mapID, String username, String sessionID) {
		Element.updateEndRevisionID(elementID, revisionID);

		ActionPackage deleteBoxActionPackage = ActionPackageFactory.deleteElement(mapID, elementID, username);

		// need one action package with all delete actions for logging
		ActionPackage deleteAllActionPackage = new ActionPackage();
		deleteAllActionPackage.addAction(deleteBoxActionPackage.getActions().get(0));
		// get all childElement IDs of this element
		Vector<Integer> childElements = Element.getChildElementIDs(elementID);
		for (Integer i : childElements) {
			deleteElementAndChildrenRecursive(i, revisionID, mapID, username, sessionID, deleteAllActionPackage);
		}

		// log only the one large action package
		removeMetaInformation(deleteAllActionPackage);
		this.aproc.addMetaInformation(deleteAllActionPackage, sessionID);
		Logger.doCFLogging(deleteAllActionPackage);
		removeMetaInformation(deleteBoxActionPackage);

		// send out delete of only top-level box, other deletes sent separately
		ManagementController.addToAllUsersOnMapActionQueue(deleteBoxActionPackage, mapID);

	}

	/**
	 * collect all informations of the children of an element's children to prepare to delete
	 * 
	 * @param elementID the Id of element
	 * @param revisionID the ID of revision
	 * @param mapID the ID of the Map
	 * @param username the name of User
	 * @param sessionID the ID of the Session
	 * @param deleteAllActionPackage an ActionPackage including actions for deleting an element and his chirldren
	 * @author ZGE
	 */
	private void deleteElementAndChildrenRecursive(int elementID, int revisionID, int mapID, String username, String sessionID,
			ActionPackage deleteAllActionPackage) {
		Element.updateEndRevisionID(elementID, revisionID);

		Vector<Integer> childElements = Element.getChildElementIDs(elementID);

		for (Integer i : childElements) {
			deleteElementAndChildrenRecursive(i, revisionID, mapID, username, sessionID, deleteAllActionPackage);
		}

		ActionPackage ap = ActionPackageFactory.deleteElement(mapID, elementID, username);
		deleteAllActionPackage.addAction(ap.getActions().get(0));
		ManagementController.addToAllUsersOnMapActionQueue(ap, mapID);
	}

	// "Delete" actions end up with bad meta-information, so must be cleared for logging
	private void removeMetaInformation(ActionPackage p) {

		for (Action a : p.getActions()) {
			a.removeParameter(ParameterTypes.UserActionId);
			a.removeParameter(ParameterTypes.NumActions);
		}
	}

	private void processAutoOrganize(Action a, User u)
	{
		int mapID = this.aproc.getMapIDFromAction(a);
		
		// Create new revision of the map
		Revision r = createNewRevision(mapID, u, a);
		r.setDescription("Auto Organizing Map");
		r.saveToDatabase();
		
		ActionPackage ap = ActionPackage.wrapAction(a);
		Logger.doCFLogging(ap);	
		ManagementController.addToAllUsersOnMapActionQueue(ap, mapID);
	}

	@Override
	public boolean processAction(Action a, User u, String sessionID) {
		boolean returnValue = false;
		if (u != null && a.getCategory().equals(Categories.Map)) {
			switch (a.getCmd()) {
			case ChangeFontSize:
				processChangeFontSize(a, u);
				returnValue = true;
				break;
			case CreateElement:// Check
				processCreateElement(a, u);
				returnValue = true;
				break;
			case UpdateElement:
				processUpdateElement(a, u);
				returnValue = true;
				break;
			case UpdateCursorPosition:
				processCursorUpdate(a, u);
				returnValue = true;
				break;
			case DeleteElement:
				processDeleteElement(a, u);
				returnValue = true;
				break;
			//TODO Zhenyu
			case BackgroundImage:
				processBackgroudImage(a, u);
				returnValue = true;
				break;
			case AutoOrganize:
				processAutoOrganize(a, u);
				returnValue = true;
				break;
			default:
				break;
			}
		}
		if (returnValue)
		{
			logToDataShop(a, u);
		}
		return returnValue;
	}

	private void processChangeFontSize(Action a, User u){
		int mapID = this.aproc.getMapIDFromAction(a);
		
		Revision r = createNewRevision(mapID, u, a);
		r.setDescription("Changing the font size");
		r.saveToDatabase();
		
		ActionPackage ap = ActionPackage.wrapAction(a);
		Logger.doCFLogging(ap);	
		ManagementController.addToAllUsersOnMapActionQueue(ap, mapID);
	}
	
	//TODO Zhenyu
	private void processBackgroudImage(Action a,User u)
	{
		int mapID = this.aproc.getMapIDFromAction(a);
		
		//Save the url in the database
		Map.setBackgroundImage(mapID, a.getParameterValue(ParameterTypes.BackgroundImageURL));
		
		// Create new revision of the map
		Revision r = createNewRevision(mapID, u, a);
		r.setDescription("Adding a Background Image");
		r.saveToDatabase();
		
		
		ActionPackage ap = ActionPackage.wrapAction(a);
		Logger.doCFLogging(ap);	
		ManagementController.addToAllUsersOnMapActionQueue(ap, mapID);
	}
}