package lasad.gwt.client.model.organization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

import lasad.gwt.client.communication.helper.ActionFactory;
import lasad.gwt.client.communication.LASADActionSender;
import lasad.gwt.client.communication.LASADActionReceiver;

import lasad.gwt.client.LASAD_Client;

import lasad.gwt.client.model.argument.MVController;
import lasad.gwt.client.model.ElementInfo;

// I'm aware that importing from the same package is unnecessary, but I do it in case I chnage the package of this class ever.
import lasad.gwt.client.model.organization.ArgumentModel;
import lasad.gwt.client.model.organization.ArgumentThread;
import lasad.gwt.client.model.organization.LinkedBox;
import lasad.gwt.client.model.organization.GroupedBoxesStatusCodes;
import lasad.gwt.client.model.organization.OrganizerLink;

import lasad.gwt.client.logger.Logger;
import lasad.gwt.client.ui.workspace.graphmap.AbstractGraphMap;

import lasad.shared.communication.objects.ActionPackage;

import java.io.StringWriter;
import java.io.PrintWriter;

/**
 * An AutoOrganizer can clean up the user's workspace into a clearer visual representation of the argument. It can also update links
 * in ArgumentMap representations where a type of relation can create groups of boxes. The overall map organizing function,
 * accordingly called organizeMap(), is only called when the user clicks the corresponding button on the ArgumentMapMenuBar. Though originally
 * built for maps using Mara Harrell's template, this class can be applied to any map from any template.  There is a model that updates
 * with every change to the map.  Thus, we don't need to start from scratch and gather components every time these methods are called.
 * @author Kevin Loughlin and Darlan Santana Farias
 * @since 12 June 2015, Updated 6 July 2015
 */
public class AutoOrganizer
{
	// Space between organized nodes
	private final int Y_SPACE = 100;
	private final int X_SPACE = 100;

	// The maximum number of siblings (grouped boxes) a box can have
	private final int MAX_SIBLINGS = 2;
	
	//Initial coordinates
	private final int INITIAL_X = 0;
	private final int INITIAL_Y = 2200;

	// Perfectly centered box location
	private final int CENTER_X = 2400;
	private final int CENTER_Y = CENTER_X;

	// The map that this instance of AutoOrganizer corresponds to
	private AbstractGraphMap map;
	
	// Keeps track of the boxes visited during function execution, must be cleaned before used
	private HashSet<LinkedBox> visited = new HashSet<LinkedBox>();
	
	// Keeps track of the boxes visited as well as the order in which the boxes are inserted
	private LinkedHashSet<LinkedBox> visitedLHSet = new LinkedHashSet<LinkedBox>();

	// Instances of autoOrganizer: one per map.  String is mapID.
	private static HashMap<String, AutoOrganizer> instances = new HashMap<String, AutoOrganizer>();

	// For sending map updates to the server
	private LASADActionSender communicator = LASADActionSender.getInstance();
	private ActionFactory actionBuilder = ActionFactory.getInstance();

	/**
	 *	The only constructor that should be used
	 *	@param map - The argument map for this instance of AutoOrganizer
	 */
	public AutoOrganizer(AbstractGraphMap map)
	{
		this.map = map;
		instances.put(map.getID(), this);
	}

	// Don't use default constructor
	private AutoOrganizer()
	{
	}

	/**
	 * Returns the AutoOrganizer instance relating to the parameter mapID
	 * @param mapID - The ID for the map that corresponds to an instance of AutoOrganizer
	 * @return The autoOrganizer instance corresponding to mapID, or null if the instance does not exist
	 */
	public static AutoOrganizer getInstanceByMapID(String mapID)
	{
		return instances.get(mapID);
	}

	/* UNDER DEVELOPMENT
	 * Recursively calculates the coordinates for each box in an argument thread
	 * @param level - Indicates the height level of the 'root' parameter, should be 0 when called for an actual root
	 * @param root - the root "parent" of the argument thread
	 * @param xCoords - stores the x coordinates calculated
	 * @param yCoords - stores the y coordinates calculated
	 * @param neighborBoxes - boxes in the same height level as the root
	 * @return The maximum number of boxes in a single height level of the thread
	 */
	private int organizeThread(int level, LinkedBox root, ArrayList<Integer> xCoords, ArrayList<Integer> yCoords, LinkedHashSet<LinkedBox> neighborBoxes)
	{
		if(visitedLHSet.contains(root) || root == null) return 0;
		
		int span;
		if(!neighborBoxes.contains(root)) span = neighborBoxes.size()+1; else span = neighborBoxes.size(); 
		
		LinkedHashSet<LinkedBox> childrenNeighbors = new LinkedHashSet<LinkedBox>();
		
		xCoords.add(new Integer(-(span-1)));
		yCoords.add(level);
			
		visitedLHSet.add(root);
		childrenNeighbors.addAll(root.getChildBoxes());
			
		for(LinkedBox box : root.getSiblingBoxes())
		{
			xCoords.add(new Integer(xCoords.get(xCoords.size()-1)+2));
			yCoords.add(level);
				
			visitedLHSet.add(box);
			childrenNeighbors.addAll(box.getChildBoxes());
		}
		
		for(LinkedBox box : neighborBoxes){
			if(visitedLHSet.contains(box))
				continue;
			
			xCoords.add(new Integer(xCoords.get(xCoords.size()-1)+2));
			yCoords.add(level);
				
			visitedLHSet.add(box);
			childrenNeighbors.addAll(box.getChildBoxes());
		}
			
		int childSpan;
		for(LinkedBox box : childrenNeighbors)
		{
			for(LinkedBox siblingBox : box.getSiblingBoxes())
			{
				if(!childrenNeighbors.contains(siblingBox))
					childrenNeighbors.add(siblingBox);
			}
		}
		
		for(LinkedBox box : childrenNeighbors)
		{
			childSpan = organizeThread(level+1, box, xCoords, yCoords, childrenNeighbors);
			
			if(childSpan > span) span = childSpan;
		}
		
		return span;		
	}
	
	/*
	 * UNDER DEVELOPMENT
	 * Given the ArrayList with an HashSet with the highest height roots, goes through the argument thread 
	 * and determine in which level each box is, inserting the boxes in the ArrayList corresponding to their level
	 */
	private void discoverLevels(ArrayList<HashSet<LinkedBox>> levels, int currentLevel)
	{
		boolean newParent = false;
		for(LinkedBox box : levels.get(currentLevel))
		{
			if(visited.contains(box)) {
				visited.remove(box);//allow multiple converging paths
				return; //stops cycle
			}

			visited.add(box);
			
			//Check if this box was inserted in any lower levels. If positive, remove it from there
			for(int i = 0; i < currentLevel; i++)
			{
				if(levels.get(i).contains(box))
				{
					levels.get(i).remove(box);
					levels.get(i).removeAll(box.getSiblingBoxes());
				}
			}
			
			for(LinkedBox child : box.getChildBoxes())
			{
				if(levels.size() == currentLevel+1)
					levels.add(new HashSet<LinkedBox>());
					
				levels.get(currentLevel+1).add(child);
				levels.get(currentLevel+1).addAll(child.getSiblingBoxes());
				
				for(LinkedBox parent : box.getParentBoxes())
				{
					if(!visited.contains(parent))
					{
						levels.get(currentLevel-1).add(parent);
						newParent = true;
					}	
				}
			}
		}
		
		discoverLevels(levels, currentLevel+1);
		
		if(newParent)
			discoverLevels(levels, currentLevel-1);
		visited.removeAll(levels.get(currentLevel));//allow multiple converging paths
	}
	
	/*
	 * UNDER DEVELOPMENT
	 * Given a root box, find out how high its tree goes (i.e., how many "generations" there are in its "family")
	 */
	private int getTreeHeight(LinkedBox root)
	{
		if(root.getChildBoxes().size() == 0)
			return 1;
		if(visited.contains(root)) //stops cycle
		{
			visited.remove(root);	//allow multiple converging paths
			return 0;
		}
		
		int maxHeight = 0;
		int height = 0;
		visited.add(root);
		for(LinkedBox child : root.getChildBoxes())
		{
			height = getTreeHeight(child);
			if(height > maxHeight)
				maxHeight = height;
		}
		
		visited.remove(root); //allow multiple converging paths
		return maxHeight+1;
	}

	private ArgumentThread organizeThread(ArgumentThread thread)
	{
		for (LinkedBox startBox : thread.getBoxes())
		{
			//startBox.setGridPosition(0,0);
			//thread.getGrid().organizeGrid(startBox);
		}

		return thread;
	}
	
	/* UNDER DEVELOPMENT
	 * Calculates the coordinates for each box in an argument thread
	 * @param levels - ArrayList in which each element contains a HashSet of the boxes in the corresponding level
	 * @param xCoords - stores the x coordinates calculated
	 * @param bottomUp - when true place parents below children
	 */
	private void organizeThread(ArrayList<HashSet<LinkedBox>> levels, HashMap<LinkedBox, Integer> xCoords)
	{
		HashMap<LinkedBox, HashSet<LinkedBox>> sharedParents = new HashMap<LinkedBox, HashSet<LinkedBox>>();
		HashMap<LinkedBox, HashSet<LinkedBox>> sharedChildren = new HashMap<LinkedBox, HashSet<LinkedBox>>();
		ArrayList<LinkedBox> nonLevelZeroRoots = new ArrayList<LinkedBox>();
		int lastX;
		for(int i = 0; i < levels.size(); i++)
		{
			sharedChildren.clear();
			sharedParents.clear();
			nonLevelZeroRoots.clear();
			lastX = -1;
			
			for(LinkedBox box : levels.get(i))
			{
				if(i > 0 && box.getNumParents() == 0 && !box.siblingHasParent())
					nonLevelZeroRoots.add(box);
				
				for(LinkedBox child : box.getChildBoxes())
				{
					if(sharedChildren.get(child) == null)
						sharedChildren.put(child, new HashSet<LinkedBox>());
					sharedChildren.get(child).add(box);
				}
				for(LinkedBox parent : box.getParentBoxes())
				{
					if(sharedParents.get(parent) == null)
						sharedParents.put(parent, new HashSet<LinkedBox>());
					sharedParents.get(parent).add(box);
				}
				if(i == 0 && box.getNumChildren() == 0)
				{
					if(box.siblingHasChild()) continue;
					xCoords.put(box, lastX+2);
					lastX += 2;
				}
			}
			int count;
			if(i == 0)//set positions for the level 0
			{
				for(LinkedBox child : sharedChildren.keySet())
				{
					for(LinkedBox box : sharedChildren.get(child))
					{

						if(xCoords.get(box) == null)
						{
							xCoords.put(box, lastX+2); lastX += 2;
							
							for(LinkedBox sibling : box.getThisAndExtendedSiblings())
							{
								xCoords.put(sibling, lastX+2);
								lastX += 2;
							}
						}
					}
				}
			}
			else
			{
				int lastPosition = -1;
				int start = -1; int end;
				for(LinkedBox parent : sharedParents.keySet())
				{
					LinkedBox first = null;
					LinkedBox last = null;
					ArrayList<LinkedBox> middle = new ArrayList<LinkedBox>();
					for(LinkedBox box : sharedParents.get(parent))
					{
						if(xCoords.get(box) != null)
						{
							if(start < 0)
								start = xCoords.get(box);
							else if(xCoords.get(box) < start)
								start = xCoords.get(box);
							
							continue;
						}
						if(first == null && box.getNumSiblings() > 0)
							first = box;
						else if(first != null && last == null && box.getNumSiblings() > 0)
							last = box;
						else if(last == null || !last.getThisAndExtendedSiblings().contains(box))
							middle.add(box);
					}
					if(first != null)
					{
						for(LinkedBox sibling : first.getThisAndExtendedSiblings()){
							xCoords.put(sibling, lastX+2); lastX += 2;
						}
						xCoords.put(first, lastX+2); lastX+=2; 
						if(start < 0) start = lastX;
					}
					for(LinkedBox box : middle)
					{
						if(xCoords.get(box) != null) continue;
						
						xCoords.put(box, lastX+2); lastX+=2; 
						if(start < 0) start = lastX;
						
						for(LinkedBox sibling : box.getThisAndExtendedSiblings()){
							xCoords.put(sibling, lastX+2); lastX += 2;
						}
					}
					if(last == null)
						end = lastX;
					else
					{
						xCoords.put(first, lastX+2); lastX+=2; end = lastX;
						for(LinkedBox sibling : last.getThisAndExtendedSiblings()){
							xCoords.put(sibling, lastX+2); lastX += 2;
						}
					}
					
					int parentPosition = xCoords.get(parent);
					int childrenCenter = (end+start)/2;
					if(parentPosition > childrenCenter)
					{
						int difference = parentPosition-childrenCenter;
						for(LinkedBox box : sharedParents.get(parent))
						{
							xCoords.put(box, xCoords.get(box)+difference);
						}
						lastX += difference;
					}
					
					else if(parentPosition < childrenCenter)
					{	visited.clear();
						recentralizeParent(parent, xCoords);
						visited.clear();
					}
				}
			}
		}
	}
	
	/*
	* 	Recursively move parents in the X axis so they get aligned to their children 
	*/
	private void recentralizeParent(LinkedBox parent, HashMap<LinkedBox, Integer> xCoords)
	{
		//TO DO get all of parent's children average their position and change his to match that, 
		//call this function to each of its parents
		if(visited.contains(parent))
			return;
		visited.add(parent);
		LinkedBox nextParent = null;
		int sum = 0;
		for(LinkedBox box : parent.getChildBoxes())
		{
			sum += xCoords.get(box);
		}
		
		xCoords.put(parent, sum/parent.getNumChildren());
		
		for(LinkedBox box : parent.getParentBoxes())
			recentralizeParent(box, xCoords);
	}

	public void organizeMap()
	{
		try
		{
			boolean isOrganizeTopToBottom = map.getMyViewSession().getController().getMapInfo().isOrganizeTopToBottom();
			Logger.log("Entered organizeMap", Logger.DEBUG);

			ArgumentModel argModel = ArgumentModel.getInstanceByMapID(map.getID());
			for (ArgumentThread argThread : argModel.getArgThreads())
			{
				// Because this is recursive we only need first box, then can break; boxes will now be on grid
				argThread.organizeGrid(isOrganizeTopToBottom);
			}

			int numThreads = argModel.getArgThreads().size();
			HashMap<Integer, ArgumentThread> threadsIndexed = new HashMap<Integer, ArgumentThread>();

			int threadCount = 0;
			for (ArgumentThread argThread : argModel.getArgThreads())
			{
				threadsIndexed.put(new Integer(threadCount), argThread);
				threadCount++;
			}

			int maxHeight = Integer.MIN_VALUE;
			int maxWidth = Integer.MIN_VALUE;
			for (ArgumentThread argThread : argModel.getArgThreads())
			{
				for (LinkedBox box : argThread.getGrid())
				{
					if (box.getHeight() > maxHeight)
					{
						maxHeight = box.getHeight();
					}

					if (box.getWidth() > maxWidth)
					{
						maxWidth = box.getWidth();
					}
				}
			}

			int half = threadCount / 2;
			int gridOffsetWidth = 0;

			int totalModelWidth = 0;
			int totalModelHeight = 0;
			for (ArgumentThread thread : argModel.getArgThreads())
			{
				totalModelWidth += thread.getMaxWidthLevelOnGrid();
				totalModelHeight += thread.getMaxHeightLevelOnGrid();
			}

			final int START_X = CENTER_X - (totalModelWidth * maxWidth) / 2;
			final int START_Y = CENTER_Y + (totalModelHeight * maxHeight) / 2;

			Logger.log("maxWidth: " + maxWidth, Logger.DEBUG);
			Logger.log("maxHeight: " + maxHeight, Logger.DEBUG);
			Logger.log("startX: " + START_X, Logger.DEBUG);
			Logger.log("startY: " + START_Y, Logger.DEBUG);

			for (int counter = 0; counter < threadCount; counter++)
			{
				ArgumentThread thread = threadsIndexed.get(counter);
				int maxHeightLevel = thread.getMaxHeightLevelOnGrid();
				for (LinkedBox box : thread.getGrid())
				{
					Logger.log("boxWidthLevel: " + box.getWidthLevel() + "; boxHeightLevel: " + box.getHeightLevel(), Logger.DEBUG);
					sendUpdatePositionToServer(box, START_X + gridOffsetWidth + box.getWidthLevel() * maxWidth, START_Y - box.getHeightLevel() * (maxHeight));
				}

				gridOffsetWidth += thread.getMaxWidthLevelOnGrid();
				Logger.log("counter: " + counter, Logger.DEBUG);
			}

			sendCenterMapToServer(map.getID());

			Logger.log("Exiting organizeMap", Logger.DEBUG);
		}
		catch (Exception e)
		{
			Logger.log("EXCEPTION THROWN", Logger.DEBUG);
			Logger.log("toSTRING: " + e.toString(), Logger.DEBUG);
			Logger.log("MESSAGE: " + e.getMessage(), Logger.DEBUG);
			Logger.log("STACK TRACE", Logger.DEBUG);
			for (StackTraceElement stackFrame : e.getStackTrace())
			{
				Logger.log(stackFrame.toString(), Logger.DEBUG);
			}
		}
	}

	/**
	 * UNDER DEVELOPMENT
	 * Organizes the map from bottom to top: Root "parents" start at the bottom, leading the way to children above.
	 * "sibling" boxes (i.e. boxes attached via group links) are placed adjacently.
	 */
	/*
	public void organizeMap(Object toShutTheCompilerUpWhileSimulatenouslyNotDeletingDarlansCodeAndInTheProcessCreatingALongVariableName)
	{	
		Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] Running AutoOrganizer...", Logger.DEBUG);

		ArgumentModel argModel = ArgumentModel.getInstanceByMapID(map.getID());

		int yCoord = INITIAL_Y;
		int xCoord = INITIAL_X;
		int newXCoord = 0;
		int maxSpan = 0;
		int lastSpan = 0;
		int span = 0;
		int init = 0;
		int yMax = 0;
		boolean bottomUp = true; //when true parents are below children
	
		HashMap<LinkedBox,Integer> yCoords = new HashMap<LinkedBox,Integer>();
		HashMap<LinkedBox,Integer> xCoords = new HashMap<LinkedBox,Integer>();
		visitedLHSet.clear();
		
		Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] Retrieving threads...", Logger.DEBUG);
		Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] Number of threads: "+argModel.getArgThreads().size(), Logger.DEBUG);
		HashSet<LinkedBox> roots = new HashSet<LinkedBox>();
		
		argModel.updateArgThreads();
		
		ArrayList<HashSet<LinkedBox>> levels = null;
		
		for (ArgumentThread argThread : argModel.getArgThreads())
		{
			Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] All boxes in the thread: "+argThread, Logger.DEBUG);
			
			levels = new ArrayList<HashSet<LinkedBox>>();
			
			int maxHeight = 0;
			int height;
			for(LinkedBox box : argThread.getRootBoxes())
			{
				visited.clear();
				height = getTreeHeight(box);
				if(height > maxHeight)
				{
					maxHeight = height;
					if(levels.size() == 0)
						levels.add(new HashSet<LinkedBox>());
					else 
						levels.set(0, new HashSet<LinkedBox>());
					levels.get(0).add(box);
				}
				else if(height == maxHeight)
				{
					levels.get(0).add(box);
				}
			}
			visited.clear();
			
			Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] Got the roots: "+levels.get(0), Logger.DEBUG);
			Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] Total number of roots: "+argThread.getRootBoxes().size(), Logger.DEBUG);
			
			discoverLevels(levels, 0);
			visited.clear();
			
			organizeThread(levels, xCoords);
			
			//EVERYTHING FROM THIS POINT WAS JUST COPIED FROM THE PREVIOUS VERSION
			/*maxSpan += lastSpan+span;
			lastSpan = span;
			Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] Shift: "+maxSpan, Logger.DEBUG);
			Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] X Coords (before shift): "+xCoords, Logger.DEBUG);
			
			for(int i = init; i < xCoords.size(); i++)
			{
				xCoords.set(i, xCoords.get(i)+maxSpan);
			}
		
			Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] X Coords (after shift): "+xCoords, Logger.DEBUG);
			Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] Y Coords "+yCoords, Logger.DEBUG);
			
			init = xCoords.size();
			if(init-1 >= 0) 
				if(yCoords.get(init-1) > yMax) yMax = yCoords.get(init-1);
			*/
			//xCoord = newXCoord;
				/*
		}
		Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] Going to update positions...", Logger.DEBUG);
		int i = 0;
		for(LinkedBox box : visitedLHSet)
		{
			xCoord = CENTER_X+(xCoords.get(i)-maxSpan/2)*X_SPACE;
			yCoord = (yMax/2-yCoords.get(i))*Y_SPACE+CENTER_Y;
			sendUpdatePositionToServer(box, xCoord, yCoord);
			i++;
		}
		Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] Components organized... Boxes visitedLHSet: "+visitedLHSet.size(), Logger.DEBUG);
		visitedLHSet.clear();

		Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] Positions updated...", Logger.DEBUG);

	}
	*/

	/**
	 * UNDER DEVELOPMENT
	 * Organizes the map from bottom to top: Root "parents" start at the bottom, leading the way to children above.
	 * "sibling" boxes (i.e. boxes attached via Linked Premises) are placed adjacently.
	 */
	/*public void organizeMap()
	{
		Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] Running AutoOrganizer...", Logger.DEBUG);

		ArgumentModel argModel = ArgumentModel.getInstanceByMapID(map.getID());

		int yCoord = INITIAL_Y;
		int xCoord = INITIAL_X;
		int newXCoord = 0;
		int maxSpan = 0;
		int lastSpan = 0;
		int span = 0;
		int init = 0;
		int yMax = 0;
	
		ArrayList<Integer> yCoords = new ArrayList<Integer>();
		ArrayList<Integer> xCoords = new ArrayList<Integer>();
		ArrayList<ArgumentThread> threadList = new ArrayList<ArgumentThread>();
		visitedLHSet.clear();
		
		Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] Retrieving threads...", Logger.DEBUG);
		Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] Number of threads: "+argModel.getArgThreads().size(), Logger.DEBUG);
		HashSet<LinkedBox> roots = new HashSet<LinkedBox>();
		
		argModel.updateArgThreads();
		
		for (ArgumentThread argThread : argModel.getArgThreads())
		{
			threadList.add(argThread);
			LinkedBox root = null;
			
			Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] All boxes in the thread: "+argThread, Logger.DEBUG);
			for(LinkedBox box : argThread.getRootBoxes()){
				root = box;
				break;
			}
			
			Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] Got the root: "+root, Logger.DEBUG);
			Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] Number of roots: "+argThread.getRootBoxes().size(), Logger.DEBUG);
			if(root == null) continue;
			
			span = organizeThread(0, root, xCoords, yCoords, new LinkedHashSet(argThread.getRootBoxes()));
			maxSpan += lastSpan+span;
			lastSpan = span;
			Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] Shift: "+maxSpan, Logger.DEBUG);
			Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] X Coords (before shift): "+xCoords, Logger.DEBUG);
			
			for(int i = init; i < xCoords.size(); i++)
			{
				xCoords.set(i, xCoords.get(i)+maxSpan);
			}
		
			Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] X Coords (after shift): "+xCoords, Logger.DEBUG);
			Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] Y Coords "+yCoords, Logger.DEBUG);
			
			init = xCoords.size();
			if(init-1 >= 0) 
				if(yCoords.get(init-1) > yMax) yMax = yCoords.get(init-1);
			
			//xCoord = newXCoord;
		}
		Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] Going to update positions...", Logger.DEBUG);
		int i = 0;
		for(LinkedBox box : visitedLHSet)
		{
			xCoord = CENTER_X+(xCoords.get(i)-maxSpan/2)*X_SPACE;
			yCoord = (yMax/2-yCoords.get(i))*Y_SPACE+CENTER_Y;
			sendUpdatePositionToServer(box, xCoord, yCoord);
			i++;
		}
		Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] Components organized... Boxes visitedLHSet: "+visitedLHSet.size(), Logger.DEBUG);
		visitedLHSet.clear();

		Logger.log("[lasad.gwt.client.communication.AutoOrganizer][run] Positions updated...", Logger.DEBUG);

	}*/

	/**
	 * Updates the sibling boxes (i.e. grouped boxes) related to the creation of a new link.
	 * For example, if box A is attached to box B via a group link, and box B gets a new child, then box A should also
	 * have a relation pointing to that child.  This method uses the private helper method "updateRecursive" to do its dirty work.
	 * @param link - The new, user-drawn link from which we must search for possibly necessary additional new links
	 */
	public void updateSiblingLinks(OrganizerLink link)
	{
		//Logger.log("Entered updateSiblingLinks", Logger.DEBUG);
		HashSet<OrganizerLink> linksToCreate = new HashSet<OrganizerLink>();

		// The original link data
		LinkedBox origStartBox = link.getStartBox();
		LinkedBox origEndBox = link.getEndBox();
		String linkType = link.getType();

		if (link.getConnectsGroup())
		{
			HashSet<OrganizerLink> origStartChildLinks = origStartBox.getChildLinks();
			HashSet<LinkedBox> origStartChildBoxes = origStartBox.getChildBoxes();

			HashSet<OrganizerLink> origEndChildLinks = origEndBox.getChildLinks();
			HashSet<LinkedBox> origEndChildBoxes = origEndBox.getChildBoxes();

			for (OrganizerLink origStartChildLink : origStartChildLinks)
			{
				LinkedBox newChildBox = origStartChildLink.getEndBox();
				if (!origEndChildBoxes.contains(newChildBox))
				{
					OrganizerLink newLink = new OrganizerLink(origEndBox, newChildBox, origStartChildLink.getType(), origStartChildLink.getConnectsGroup());
					linksToCreate.add(newLink);
				}
			}

			for (OrganizerLink origEndChildLink : origEndChildLinks)
			{
				LinkedBox newChildBox = origEndChildLink.getEndBox();
				if (!origStartChildBoxes.contains(newChildBox))
				{
					OrganizerLink newLink = new OrganizerLink(origStartBox, newChildBox, origEndChildLink.getType(), origEndChildLink.getConnectsGroup());
					linksToCreate.add(newLink);
				}
			}
		}
		else
		{
			HashSet<LinkedBox> origStartSiblingBoxes = origStartBox.getSiblingBoxes();

			// We only need the first one, hence why I break, but I use this for loop in case origStartSiblingBoxes is empty so that it will skip
			for (LinkedBox origStartSiblingBox : origStartSiblingBoxes)
			{
				linksToCreate = updateRecursive(origStartSiblingBox, origEndBox, link, new VisitedAndLinksHolder()).getLinks();
				break;
			}
		}

		addLinksToVisual(linksToCreate);
	}

	/**
	 *	Holds the visited boxes and links accumulated in a recursive method, with the benefit of one data structure
	 *	For use with updateSiblingLinks and updateRecursive
	 */
	class VisitedAndLinksHolder
	{
		private HashSet<LinkedBox> visited;
		private HashSet<OrganizerLink> links;

		public VisitedAndLinksHolder()
		{
			visited = new HashSet<LinkedBox>();
			links = new HashSet<OrganizerLink>();
		}

		public void addVisited(LinkedBox box)
		{
			visited.add(box);
		}

		public void addLink(OrganizerLink link)
		{
			links.add(link);
		}

		public HashSet<LinkedBox> getVisited()
		{
			return visited;
		}

		public HashSet<OrganizerLink> getLinks()
		{
			return links;
		}
	}

	/*	
	 *	Recursively checks the siblings of a given start box to see if they need to be updated with a new relation.
	 *	Keeps track of boxes visited so that the method will eventually end.
	 *	@param startBox - The box from which we might make a link
	 *	@param END_BOX - The constant box to which we will be connecting
	 *	@param LINK_TYPE - The type of connection we will make if necessary
	 *  @param holder - The visited boxes and the links that need to be created, should be initialized as empty
	*/
	private VisitedAndLinksHolder updateRecursive(LinkedBox startBox, LinkedBox END_BOX, OrganizerLink LINK_DATA, VisitedAndLinksHolder holder)
	{
		//Logger.log("Entered update recursive.", Logger.DEBUG);
		if (!holder.getVisited().contains(startBox))
		{
			holder.addVisited(startBox);
			if (!startBox.getChildBoxes().contains(END_BOX))
			{
				holder.addLink(new OrganizerLink(startBox, END_BOX, LINK_DATA.getType(), LINK_DATA.getConnectsGroup()));
			}
			for (LinkedBox siblingBox : startBox.getSiblingBoxes())
			{
				holder = updateRecursive(siblingBox, END_BOX, LINK_DATA, holder);
			}
		}

		return holder;
	}

	/**
	 *	Determines whether or not the passed group OrganizerLink can be created on the map, which depends on invariants such as
	 *	the maximum number of permitted siblings per box, both boxes being the same type, groupable, and no conflicting links between siblings.
	 *	@param link - The link to check for valid creation
	 *	@return Success/error integer code: 0 for success, greater than 0 for error code
	 */
	public int groupedBoxesCanBeCreated(OrganizerLink link)
	{
		//Logger.log("Entered GroupedBoxesCanBeCreated", Logger.DEBUG);
		LinkedBox startBox = link.getStartBox();
		LinkedBox endBox = link.getEndBox();

		// Boxes shouldn't be null
		if (startBox == null || endBox == null)
		{
			return GroupedBoxesStatusCodes.NULL_BOX;
		}

		// Can't create a link to self
		if (startBox.equals(endBox))
		{
			return GroupedBoxesStatusCodes.SAME_BOX;
		}

		// Checks if they are of groupable type
		if (startBox.getCanBeGrouped())
		{
			// Checks that both boxes are of same type, else return error code
			if (startBox.getType().equalsIgnoreCase(endBox.getType()))
			{
				// Checks that they both have fewer than 2 siblings, else return error code
				if (startBox.getNumSiblings() < MAX_SIBLINGS && endBox.getNumSiblings() < MAX_SIBLINGS)
				{
					// See this.isCompatible for what the method within this if statement checks, else return error code
					if (this.isCompatible(startBox, endBox))
					{
						return GroupedBoxesStatusCodes.SUCCESS;
					}
					else
					{
						return GroupedBoxesStatusCodes.TWO_WAY_LINK;
					}
				}
				else
				{
					return GroupedBoxesStatusCodes.TOO_MANY_SIBS;
				}
			}
			else
			{
				return GroupedBoxesStatusCodes.NOT_SAME_TYPE;
			}
		}
		else
		{
			return GroupedBoxesStatusCodes.CANT_BE_GROUPED;
		}
	}

	/*	
	 *	Checks that there aren't existing invalid connections between the startBoxAndExtSibs group and the end Box children.
	 *	@param startBoxAndExtSibs - The startBox for the new link and its extended siblings
	 *	@param endBox - The end box for the new link
	 *	@return true if there are no conflicts, false if the grouped boxes should not be created
	 */
	private boolean isCompatible(LinkedBox startBox, LinkedBox endBox)
	{
		//Logger.log("INSIDE isCompatible", Logger.DEBUG);
		HashSet<LinkedBox> startChildBoxes = startBox.getChildBoxes();
		HashSet<LinkedBox> endChildBoxes = endBox.getChildBoxes();
		
		for (LinkedBox startChildBox : startChildBoxes)
		{
			if (endBox.hasNonChildLinkWith(startChildBox))
			{
				return false;
			}
		}

		for (LinkedBox endChildBox : endChildBoxes)
		{
			if (startBox.hasNonChildLinkWith(endChildBox))
			{
				return false;
			}
		}

		return true;
	}

	/*
	 *	Updates a box position on the map, might need to add to redraw the relations because we're losing arrow heads on them for some reason.
	 *	@param box - The box whose position we will update
	 *	@param x - The new x coordinate for the box
	 *	@param y - The new y coordinate for the box
	 */
	private void sendUpdatePositionToServer(LinkedBox box, int x, int y)
	{
		communicator.sendActionPackage(actionBuilder.updateBoxPosition(map.getID(), box.getBoxID(), x, y));
	}

	private void sendCenterMapToServer(String mapID)
	{
		communicator.sendActionPackage(actionBuilder.centerMap(mapID));
		Logger.log("Sent centerMap to server", Logger.DEBUG);
	}

	/*
	 *	Wraps each new link to be created as an actionPackage, which is sent to server to be added to the model and map.
	 */
	private void addLinksToVisual(HashSet<OrganizerLink> linksToCreate)
	{
		//Logger.log("Entered addLinksToVisual", Logger.DEBUG);	
		String elementType = "relation";

		MVController controller = LASAD_Client.getMVCController(map.getID());

		ElementInfo linkInfo = new ElementInfo();
		linkInfo.setElementType(elementType);

		for (OrganizerLink link : linksToCreate)
		{
			// a better name for element ID here would be subtype, as in, what kind of relation.  Alas, I didn't name it.
			linkInfo.setElementID(link.getType());

			String startBoxStringID = Integer.toString(link.getStartBox().getBoxID());
			String endBoxStringID = Integer.toString(link.getEndBox().getBoxID());

			ActionPackage myPackage = actionBuilder.createLinkWithElements(linkInfo, map.getID(), startBoxStringID, endBoxStringID);
			LASADActionReceiver.getInstance().setSiblingsAlreadyUpdated(true);
			communicator.sendActionPackage(myPackage);
		}
	}

	/**
	 *	Determines if supplemental links need to be removed after the passed link is removed.  This might be necessary, for example,
	 *	if the removal of solely the passed link would result in a violation of the grouped boxes invariants (i.e. each sibling box must
	 *	link to each other sibling's children).
	 *	@param removedLink - The link already removed from the model, that provides an easy access point for other nearby links to remove
	 */
	public void determineLinksToRemove(OrganizerLink removedLink)
	{
		//ogger.log("Entered determineLinksToRemove", Logger.DEBUG);
		HashSet<OrganizerLink> linksToRemove = new HashSet<OrganizerLink>();

		if (!removedLink.getConnectsGroup())
		{
			LinkedBox removedLinkStartBox = removedLink.getStartBox();
			int numSiblings = removedLinkStartBox.getNumSiblings();
			HashSet<OrganizerLink> siblingLinks = removedLinkStartBox.getSiblingLinks();
			for (OrganizerLink link : siblingLinks)
			{
				linksToRemove.add(link);
			}
		}

		removeLinksFromVisual(linksToRemove);
	}

	/*
	 *	Helper method called by determineLinksToRemove that actually sends the actionPackage to the server, telling the server to
	 *	remove each necessary link from the map.
	 */
	private void removeLinksFromVisual(HashSet<OrganizerLink> linksToRemove)
	{
		//Logger.log("Entered removeLinksFromVisual", Logger.DEBUG);
		MVController controller = LASAD_Client.getMVCController(map.getID());

		for (OrganizerLink link : linksToRemove)
		{
			ActionPackage myPackage = actionBuilder.removeElement(map.getID(), link.getLinkID());
			LASADActionReceiver.getInstance().setLinksAlreadyRemoved(true);
			communicator.sendActionPackage(myPackage);
		}
	}

	/**
	 *	Checks to see if the removal of the passed link warrants the creation of a new Thread, and creates the thread if needed
	 *	@param removedLink - The link removed from the model
	 */
	public void createNewThreadIfNecessary(OrganizerLink removedLink)
	{
		//Logger.log("Entered createNewThreadIfNecessary", Logger.DEBUG);
		ArgumentModel argModel = ArgumentModel.getInstanceByMapID(map.getID());
		ArgumentThread startArgThread = argModel.getBoxThread(removedLink.getStartBox());
		HashSet<LinkedBox> allThreadBoxes = new HashSet<LinkedBox>(startArgThread.getBoxes());
		HashSet<LinkedBox> boxesReached = visitRecursive(removedLink.getEndBox(), new HashSet<LinkedBox>());
		if (boxesReached.size() == allThreadBoxes.size() && boxesReached.containsAll(allThreadBoxes))
		{
			// They're still in the same thread
			return;
		}
		else
		{
			ArgumentThread newThread = new ArgumentThread();
			startArgThread.removeBoxes(boxesReached);
			newThread.addBoxes(boxesReached);
			argModel.addArgThread(newThread);
		}
	}

	/**
	 *	Checks to see if the removal of the passed box warrants the removal of a new Thread, and removes the thread if needed
	 *	@param removedBox - The box removed from the model
	 */
	public void removeThreadIfNecessary(LinkedBox removedBox)
	{
		//Logger.log("Entered removeThreadIfNecessary", Logger.DEBUG);
		ArgumentModel argModel = ArgumentModel.getInstanceByMapID(map.getID());
		ArgumentThread argThread = argModel.getBoxThread(removedBox);
		if (argThread.getBoxes().size() == 0)
		{
			argModel.removeArgThread(argThread);
		}
	}

	/*
	 *	Goes through all the boxes directly/indirectly attached to box to see if we can reach all from the original thread
	 *	@param box - The box currently being visited, should be initialized as the endBox of the deleted link
	 *	@param boxesReached - The boxes so far reached, should be initialized as a new, empty HashSet
	 */
	private HashSet<LinkedBox> visitRecursive(LinkedBox box, HashSet<LinkedBox> boxesReached)
	{
		//Logger.log("Entered visit recursive.", Logger.DEBUG);
		if (!boxesReached.contains(box))
		{
			boxesReached.add(box);
			for (LinkedBox childBox : box.getChildBoxes())
			{
				boxesReached = visitRecursive(childBox, boxesReached);
			}

			for (LinkedBox parentBox : box.getParentBoxes())
			{
				boxesReached = visitRecursive(parentBox, boxesReached);
			}

			for (LinkedBox siblingBox : box.getSiblingBoxes())
			{
				boxesReached = visitRecursive(siblingBox, boxesReached);
			}
		}

		return boxesReached;
	}
}