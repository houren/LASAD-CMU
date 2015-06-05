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

/**
 * this class handles all actions about map
 * 
 * @author ?
 */
public class MapActionProcessor extends AbstractActionObserver implements ActionObserver {

	public MapActionProcessor() {
		super();
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
				ActionPackage ap = ActionPackageFactory.error("Element is already deleted. Delete failed");
				Logger.doCFLogging(ap);
				ManagementController.addToUsersActionQueue(ap, u.getSessionID());
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

	@Override
	public boolean processAction(Action a, User u, String sessionID) {
		boolean returnValue = false;
		if (u != null && a.getCategory().equals(Categories.Map)) {
			switch (a.getCmd()) {
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
			default:
				break;
			}
		}
		return returnValue;
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