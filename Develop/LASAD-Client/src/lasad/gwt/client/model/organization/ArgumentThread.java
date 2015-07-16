package lasad.gwt.client.model.organization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.HashSet;
import lasad.gwt.client.model.organization.LinkedBox;
import lasad.gwt.client.model.organization.OrganizerLink;
import java.util.Collection;
import lasad.gwt.client.logger.Logger;
import lasad.gwt.client.model.organization.Coordinate;

/**
 *	An argument thread is a connected chain of boxes on the argument map space.
 *  This class is useful for AutoOrganizer.
 *	@author Kevin Loughlin
 *	@since 19 June 2015, Updated 30 June 2015
 */

public class ArgumentThread
{
	private final int SPACE = 2;

	private static int numThreads = 0;
	// HashMap allows for constant lookup time by BoxID
	private HashMap<Integer, LinkedBox> boxMap;

	private int height;
	private int width;

	private int threadID;

	private HashSet<LinkedBox> grid;

	public ArgumentThread()
	{
		this.boxMap = new HashMap<Integer, LinkedBox>();
		numThreads++;
		this.threadID = numThreads;
		this.grid = new HashSet<LinkedBox>();
	}

	public ArgumentThread(LinkedBox box)
	{
		this();
		this.addBox(box);
	}

	public ArgumentThread(Collection<LinkedBox> boxes)
	{
		this();
		for (LinkedBox box : boxes)
		{
			this.addBox(box);
		}
	}

	public Collection<LinkedBox> getBoxes()
	{
		return boxMap.values();
	}

	public HashSet<LinkedBox> getGrid()
	{
		return grid;
	}

	public void addBox(LinkedBox box)
	{
		if (boxMap.values().contains(box))
		{
			return;
		}
		else
		{
			boxMap.put(new Integer(box.getBoxID()), box);
		}
	}

	public void addBoxes(Collection<LinkedBox> boxes)
	{
		for (LinkedBox box : boxes)
		{
			this.addBox(box);
		}
	}

	// Helper for removeEltByEltID
	private LinkedBox removeBoxByBoxID(int boxID)
	{
		LinkedBox toRemove = this.getBoxByBoxID(boxID);
		if (toRemove != null)
		{
			toRemove.removeLinksToSelf();
			Integer boxIntID = new Integer(boxID);
			LinkedBox returnValue = boxMap.remove(boxIntID);
			return returnValue;
		}
		else
		{
			return null;
		}
	}

	public Object removeEltByEltID(int eltID)
	{
		Object returnValue = this.removeBoxByBoxID(eltID);
		if (returnValue == null)
		{
			for (LinkedBox box : boxMap.values())
			{
				for (OrganizerLink childLink : box.getChildLinks())
				{
					if (childLink.getLinkID() == eltID)
					{
						returnValue = childLink.clone();
						childLink.getEndBox().removeParentLink(childLink);
						box.removeChildLink(childLink);
						return returnValue;
					}
				}

				for (OrganizerLink parentLink : box.getParentLinks())
				{
					if (parentLink.getLinkID() == eltID)
					{
						returnValue = parentLink.clone();
						parentLink.getStartBox().removeChildLink(parentLink);
						box.removeParentLink(parentLink);
						return returnValue;
					}
				}

				for (OrganizerLink siblingLink : box.getSiblingLinks())
				{
					if (siblingLink.getLinkID() == eltID)
					{
						returnValue = siblingLink.clone();
						if (box.equals(siblingLink.getStartBox()))
						{
							siblingLink.getEndBox().removeSiblingLink(siblingLink);
						}
						else
						{
							siblingLink.getStartBox().removeSiblingLink(siblingLink);
						}
						box.removeSiblingLink(siblingLink);
						return returnValue;
					}
				}
			}

			return null;
		}
		else
		{
			return returnValue;
		}
	}

	public void removeBoxes(Collection<LinkedBox> boxes)
	{
		for (LinkedBox box : boxes)
		{
			this.removeBoxByBoxID(box.getBoxID());
		}
	}

	public boolean contains(LinkedBox box)
	{
		return boxMap.containsValue(box);
	}

	public LinkedBox getBoxByBoxID(int boxID)
	{
		Integer id = new Integer(boxID);
		return boxMap.get(id);
	}

	public LinkedBox getBoxByRootID(int rootID)
	{
		for (LinkedBox box : boxMap.values())
		{
			if (box.getRootID() == rootID)
			{
				return box;
			}
		}
		
		return null;
	}

	// Since ID would be the same, I can just remove and add the "newBox"
	public void replaceBox(LinkedBox newBox)
	{
		this.removeBoxByBoxID(newBox.getBoxID());
		this.addBox(newBox);
	}
	
	//identify a cycle
	// As used, start is initialiazed as the INITIAL currentBox and its siblings
	/*
	private boolean isCycle(HashSet<LinkedBox> start, LinkedBox currentBox){
		if(start.contains(currentBox))
		{
			visited.addAll(start);
			
			for(LinkedBox box : start)
			{
				for(LinkedBox childBox : box.getChildBoxes())
				{
					if(isCycle(start, childBox))
						return true;
				}
			}
		}
		else
		{
			HashSet<LinkedBox> siblings = new HashSet<LinkedBox>(currentBox.getSiblingBoxes());
			siblings.add(currentBox);
			
			for(LinkedBox box : siblings)
			{
				for(LinkedBox childBox : box.getChildBoxes())
				{
					if(start.contains(childBox)){
						visited.addAll(siblings);
						return true;
					}if(visited.contains(childBox)){
						return false;
					}if(isCycle(start, childBox)){
						visited.addAll(siblings);
						return true;
					}
				}
			}
		}
		return false;
	}
	*/

	/*
	public LinkedBox getRootBox()
	{
		HashSet<LinkedBox> potentialRoots = new HashSet<LinkedBox>();
		LinkedBox lowestBox = new LinkedBox(-1, -1, "", -1.0, Double.MAX_VALUE, -1, -1, false);

		for (LinkedBox box : this.getBoxes())
		{
			if (box.getYTop() < lowestBox.getYTop())
			{
				lowestBox = box;
			}
			if (box.getNumParents() == 0)
			{
				potentialRoots.add(box);
			}	
		}

		if (potentialRoots.size() == 0)
		{
			return lowestBox;
		}
		else
		{
			return this.getPotentialRootSpanningMostLevels(potentialRoots, new HashSet<LinkedBox>());
		}
	}
	*/

/*
	private LinkedBox getPotentialRootSpanningMostLevels(HashSet<LinkedBox> potentialRoots, HashSet<LinkedBox> visited)
	{
		for(LinkedBox potentialRoot : potentialRoots)
		{
			if(!visited.contains(potentialRoot))
			{
				getVerticalTraversalDistance(potentialRoot);
			}
		}
	}
	*/

	class IntAndVisited
	{
		private int myInt;
		private HashSet<LinkedBox> visited;

		public IntAndVisited()
		{
			myInt = 0;
			visited = new HashSet<LinkedBox>();
		}

		public void addBox(LinkedBox box)
		{
			visited.add(box);
		}

		public void incMyInt()
		{
			myInt++;
		}

		public HashSet<LinkedBox> getVisited()
		{
			return visited;
		}

		public int getMyInt()
		{
			return myInt;
		}
	}

	// RootBoxes are boxes that no have parents, nor do their siblings.  They are useful for a "starting point" when traversing a thread.
	/*
	public HashSet<LinkedBox> getRootBoxes()
	{
		HashSet<LinkedBox> rootBoxes = new HashSet<LinkedBox>();
		for (LinkedBox box : boxMap.values())
		{
			visited.clear();
			if (isRoot(box))
			{
				rootBoxes.addAll(visited);
				//break;
			}
		}
		visited.clear();
		
		//when the threads has boxes but no root is found, the root is a cycle
		if(boxMap.values().size() > 0 && rootBoxes.size() == 0)
		{
			Logger.log("[lasad.gwt.client.communication.ArgumentThread][run] Starting Cycle detection", Logger.DEBUG);
			boolean hasParent;
			HashSet<LinkedBox> start = new HashSet<LinkedBox>();
			HashSet<LinkedBox> verified = new HashSet<LinkedBox>();
			
			for (LinkedBox box : boxMap.values())
			{
				hasParent = false;
				visited.clear();
				start.clear();
				start.addAll(box.getSiblingBoxes());
				start.add(box);
				if(verified.contains(box))
					continue;
				verified.addAll(start);
				
				Logger.log("[lasad.gwt.client.communication.ArgumentThread][run] Gonna run isCycle, box: "+box, Logger.DEBUG);
				if (isCycle(start, box))
				{
					LinkedBox root = null;
					int minParents = Integer.MAX_VALUE;
					int numParents;
					Logger.log("[lasad.gwt.client.communication.ArgumentThread][run] Finding the best box to be root", Logger.DEBUG);
					for(LinkedBox cBox : visited)
					{
						numParents = cBox.getParentBoxes().size();
						for(LinkedBox sibling : cBox.getSiblingBoxes())
							numParents += sibling.getParentBoxes().size();
						
						if(numParents < minParents)
						{
							minParents = numParents;
							root = cBox;
						}
					}
					
					if(root != null)
					{
						rootBoxes.addAll(root.getSiblingBoxes());
						rootBoxes.add(root);
						break;
					}
					Logger.log("[lasad.gwt.client.communication.ArgumentThread][run] Assigning roots: "+rootBoxes, Logger.DEBUG);
				}
			}
		
			visited.clear();
		}
		
		return rootBoxes;
	}
	*/

	@Override
	public int hashCode()
	{
		return this.threadID;
	}

	@Override
	public boolean equals(Object o)
	{
		if (o instanceof ArgumentThread)
		{
			ArgumentThread other = (ArgumentThread) o;
			Collection<LinkedBox> thisBoxes = this.getBoxes();
			int thisSize = thisBoxes.size();
			Collection<LinkedBox> otherBoxes = other.getBoxes();
			if (thisSize != otherBoxes.size())
			{
				return false;
			}
			else if(thisBoxes.containsAll(otherBoxes))
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

	// Determines if a box is a root box (visited must be cleared before entering this function)
	/*
	private boolean isRoot(LinkedBox box)
	{
		if (!findIfBoxHasExtendedParents(box))
		{
			return true;
		}
		return false;
	}

	// Recursive method, determines if a box or its siblings has parents (and thus wouldn't be a rootBox);
	// Visited must be cleared before calling this function
	private boolean findIfBoxHasExtendedParents(LinkedBox box)
	{
		if (box.getNumParents() > 0)
		{
			return true;
		}
		else
		{
			if (visited.contains(box))
			{
				return false;
			}
			else
			{
				visited.add(box);
				HashSet<LinkedBox> siblingBoxes = box.getSiblingBoxes();

				for (LinkedBox siblingBox : siblingBoxes)
				{
					if(findIfBoxHasExtendedParents(siblingBox))
					{
						return true;
					}
				}
				return false;
			}
		}
	}
	*/

	public static void decNumThreads()
	{
		numThreads--;
	}

	public void organizeGrid(final boolean isOrganizeTopToBottom)
	{
		grid.clear();
		for (LinkedBox box : this.getBoxes())
		{
			organizeGridRecursive(box, 0, 0, new HashSet<LinkedBox>(), isOrganizeTopToBottom);
			break;
		}
		/*
		HashMap<Integer, HashSet<LinkedBox>> boxesByHeightLevel = sortByHeightLevel(grid);

		int rootLevel;
		int minLevel = Integer.MAX_VALUE;;
		int maxLevel = Integer.MIN_VALUE;;

		for (Integer i : boxesByHeightLevel.keySet())
		{
			if (i < minLevel)
			{
				minLevel = i;
			}

			if (i > maxLevel)
			{
				maxLevel = i;
			}
		}

		if (isOrganizeTopToBottom)
		{
			rootLevel = maxLevel;
		}
		else
		{
			rootLevel = minLevel;
		}

		int range = maxLevel - minLevel + 1;

		for (int counter = 0; counter < range; counter++)
		{
			centerChildrenOfGroups(boxesByHeightLevel.get(rootLevel), isOrganizeTopToBottom);

			if (isOrganizeTopToBottom)
			{
				rootLevel--;
			}
			else
			{
				rootLevel++;
			}
		}
		*/
	}

	/*
	public HashMap<Integer, HashSet<LinkedBox>> sortByHeightLevel(HashSet<LinkedBox> myGrid)
	{
		int minHeightLevel = Integer.MAX_VALUE;
		int maxHeightLevel = Integer.MIN_VALUE;

		for (LinkedBox box : myGrid)
		{
			if (box.getHeightLevel() < minHeightLevel)
			{
				minHeightLevel = box.getHeightLevel();
			}

			if (box.getHeightLevel() > maxHeightLevel)
			{
				maxHeightLevel = box.getHeightLevel();
			}
		}

		HashMap<Integer, HashSet<LinkedBox>> toReturn = new HashMap<Integer, HashSet<LinkedBox>>();

		for (int counter = minHeightLevel; counter <= maxHeightLevel; counter++)
		{
			HashSet<LinkedBox> boxesInLevel = new HashSet<LinkedBox>();
			for (LinkedBox box : myGrid)
			{
				if (box.getHeightLevel() == counter)
				{
					boxesInLevel.add(box);
				}
			}

			toReturn.put(new Integer(counter), boxesInLevel);
		}

		return toReturn;
	}

	public void centerChildrenOfGroups(Collection<LinkedBox> boxesAtLevel, final boolean isOrganizeTopToBottom)
	{
		if (!visited.contains(box))
		{
			int groupSize = box.getThisAndExtendedSiblings().size();
			HashSet<LinkedBox> children = box.getChildBoxes();
			int numChildren = box.getNumChildren();

			for (LinkedBox child : children)
			{
				shiftHeightLevelAndAbove(child, isOrganizeTopToBottom);
				break;
			}
		}
	}

	public void shiftHeightLevelAndAboveOrBelow(LinkedBox box, final boolean isOrganizeTopToBottom)
	{
		if (isOrganizeTopToBottom)
		{
			for (LinkedBox onGrid : grid)
			{
				if (box.getHeightLevel() >= onGrid.getHeightLevel())
				{
					onGrid.decWidthLevel();
				}
			}
		}
		else
		{
			for (LinkedBox onGrid : grid)
			{
				if (box.getHeightLevel() <= onGrid.getHeightLevel())
				{
					onGrid.decWidthLevel();
				}
			}
		}
	}
	*/

	private void organizeGridRecursive(LinkedBox box, final int widthLevel, final int heightLevel, HashSet<LinkedBox> visited, final boolean isOrganizeTopToBottom)
	{
		if (!visited.contains(box))
		{
			visited.add(box);

			LinkedBox boxAlreadyOnGrid = this.getBoxOnGrid(box);

			if (boxAlreadyOnGrid == null)
			{
				Logger.log("The following box was not on the grid..." + box.toStringShort(), Logger.DEBUG);
				box.setGridPosition(widthLevel, heightLevel);
				//putSoloBoxOnGrid(box);

				
				if (box.getNumSiblings() == 0)
				{
					putSoloBoxOnGrid(box);
				}
				else
				{
					putGroupOnGrid(sortGroup(box.getThisAndExtendedSiblings(), box));
				}
			}
			else
			{
				Logger.log("The following box was aleady on the grid..." + box.toStringShort(), Logger.DEBUG);
				box.setGridPosition(boxAlreadyOnGrid.getGridPosition());
			}
			/*
			boolean isFirstSibling = true;
			for (LinkedBox sibling : box.getSiblingBoxes())
			{
				if (isFirstSibling)
				{
					organizeGridRecursive(sibling, box.getWidthLevel() + SPACE, box.getHeightLevel(), visited, isOrganizeTopToBottom);
					isFirstSibling = false;
				}
				else
				{
					organizeGridRecursive(sibling, box.getWidthLevel() - SPACE, box.getHeightLevel(), visited, isOrganizeTopToBottom);
				}
			}
			*/

			for (LinkedBox child : box.getChildBoxes())
			{
				if (isOrganizeTopToBottom)
				{
					organizeGridRecursive(child, box.getWidthLevel(), box.getHeightLevel() - SPACE, visited, isOrganizeTopToBottom);
				}
				else
				{
					organizeGridRecursive(child, box.getWidthLevel(), box.getHeightLevel() + SPACE, visited, isOrganizeTopToBottom);	
				}
			}

			for (LinkedBox parent : box.getParentBoxes())
			{
				if (isOrganizeTopToBottom)
				{
					organizeGridRecursive(parent, box.getWidthLevel(), box.getHeightLevel() + SPACE, visited, isOrganizeTopToBottom);
				}
				else
				{
					organizeGridRecursive(parent, box.getWidthLevel(), box.getHeightLevel() - SPACE, visited, isOrganizeTopToBottom);	
				}
			}
		}
	}

	class GroupWithLeftAndRight
	{	
		private HashSet<LinkedBox> group;
		private int farthestLeftLevel;
		private int farthestRightLevel;

		public GroupWithLeftAndRight()
		{
			group = new HashSet<LinkedBox>();
			farthestLeftLevel = Integer.MAX_VALUE;
			farthestRightLevel = Integer.MIN_VALUE;
		}

		public GroupWithLeftAndRight(HashSet<LinkedBox> group, int farthestLeftLevel, int farthestRightLevel)
		{
			this();
			setGroup(group);
			setFarthestLeftLevel(farthestLeftLevel);
			setFarthestRightLevel(farthestRightLevel);
		}

		public HashSet<LinkedBox> getGroup()
		{
			return group;
		}

		public int getFarthestLeftLevel()
		{
			return farthestLeftLevel;
		}

		public int getFarthestRightLevel()
		{
			return farthestRightLevel;
		}

		public void setGroup(HashSet<LinkedBox> group)
		{
			this.group = group;
		}

		public void setFarthestLeftLevel(int leftLevel)
		{
			farthestLeftLevel = leftLevel;
		}

		public void setFarthestRightLevel(int rightLevel)
		{
			farthestRightLevel = rightLevel;
		}
	}
	
	public GroupWithLeftAndRight sortGroup(HashSet<LinkedBox> group, LinkedBox original)
	{
		int groupSize = group.size();

		int origWidthLevel = original.getWidthLevel();
		int origHeightLevel = original.getHeightLevel();

		boolean needFirstEnd;
		int farthestLeftLevel;

		if (original.getNumSiblings() == 1)
		{
			farthestLeftLevel = origWidthLevel;
			needFirstEnd = false;
		}
		else
		{
			farthestLeftLevel = origWidthLevel - groupSize + 1;
			needFirstEnd = true;
		}

		int farthestRightLevel = (groupSize - 1) * SPACE + farthestLeftLevel;

		int counter = 1;

		for (LinkedBox box : group)
		{
			if (box.getNumSiblings() == 1)
			{
				if (needFirstEnd)
				{
					box.setGridPosition(farthestLeftLevel, origHeightLevel);
				}
				else
				{
					box.setGridPosition(farthestRightLevel, origHeightLevel);
				}
			}
			else
			{
				int levelToSet = farthestLeftLevel + counter * SPACE;
				if (levelToSet == origWidthLevel)
				{
					box.setGridPosition(levelToSet + SPACE, origHeightLevel);
					counter += 2;
				}
				else
				{
					box.setGridPosition(levelToSet, origHeightLevel);
					counter++;
				}
			}
		}

		group.add(original);

		return new GroupWithLeftAndRight(group, farthestLeftLevel, farthestRightLevel);
	}

	public int getMaxWidthLevelOnGrid()
	{
		Logger.log("Entered getMaxX", Logger.DEBUG);
		int maxX = Integer.MIN_VALUE;

		for (LinkedBox box : grid)
		{
			Coordinate coordinate = box.getGridPosition();
			if (coordinate.getX() > maxX)
			{
				maxX = coordinate.getX();
			}
		}
		Logger.log("Exiting getMaxX", Logger.DEBUG);
		return maxX;
	}

	public int getMaxHeightLevelOnGrid()
	{
		Logger.log("Entered getMaxY", Logger.DEBUG);
		int maxY = Integer.MIN_VALUE;

		for (LinkedBox box : grid)
		{
			Coordinate coordinate = box.getGridPosition();
			if (coordinate.getY() > maxY)
			{
				maxY = coordinate.getY();
			}
		}
		Logger.log("Exiting getMaxY", Logger.DEBUG);
		return maxY;
	}

	public HashSet<LinkedBox> getBoxesOnGridAtHeightLevel(int heightLevel)
	{
		HashSet boxesAtHeightLevel = new HashSet<LinkedBox>();
		for (LinkedBox box : grid)
		{
			if (box.getHeightLevel() == heightLevel)
			{
				boxesAtHeightLevel.add(box);
			}
		}

		return boxesAtHeightLevel;
	}

	public LinkedBox makeRoomForSoloBox(LinkedBox box, boolean boxWasThere)
	{
		int widthLevel = box.getWidthLevel();
		int heightLevel = box.getHeightLevel();

		/*
		for (LinkedBox boxOnGrid : getBoxesOnGridAtHeightLevel(heightLevel))
		{
			int thisWidthLevel = boxOnGrid.getWidthLevel();
			HashSet<LinkedBox> thisAndExtended = boxOnGrid.getThisAndExtendedSiblings();

			int farthestLeftLevel = Integer.MAX_VALUE;
			int farthestRightLevel = Integer.MIN_VALUE;

			if (thisAndExtended.size() > 1)
			{
				for (LinkedBox groupedBox : thisAndExtended)
				{
					if (thisWidthLevel < farthestLeftLevel)
					{
						farthestLeftLevel = thisWidthLevel;
					}

					if (thisWidthLevel > farthestRightLevel)
					{
						farthestRightLevel = thisWidthLevel;
					}
				}

				// If an interesting group was there, set the box to the right edge of the group and reput it on the grid
				if (widthLevel >= farthestLeftLevel && widthLevel < farthestRightLevel)
				{
					box.setWidthLevel(farthestRightLevel);
					return makeRoomFor(box, true);
				}
			}
		}
		*/

		// If we made it this far, there was no intersecting group so we may proceed normally
		for (LinkedBox boxOnGrid : grid)
		{
			if (boxOnGrid.getWidthLevel() > widthLevel)
			{
				boxOnGrid.incWidthLevel();
			}
			else
			{
				boxOnGrid.decWidthLevel();
			}
		}

		if (boxWasThere)
		{
			box.incWidthLevel();
		}

		return box;
	}

	
	public HashSet<LinkedBox> makeRoomForGroup(HashSet<LinkedBox> group, boolean boxWasThere, int groupFarthestLeftLevel, int groupFarthestRightLevel)
	{
		/*
		for (LinkedBox box : group)
		{
			for (LinkedBox boxOnGrid : getBoxesOnGridAtHeightLevel(box.getHeightLevel()))
			{
				HashSet<LinkedBox> thisAndExtended = boxOnGrid.getThisAndExtendedSiblings();

				if (thisAndExtended.size() > 1)
				{
					int farthestLeftLevel = Integer.MAX_VALUE;
					int farthestRightLevel = Integer.MIN_VALUE;

					for (LinkedBox groupedBox : thisAndExtended)
					{
						int thisWidthLevel = groupedBox.getWidthLevel();
						if (thisWidthLevel < farthestLeftLevel)
						{
							farthestLeftLevel = thisWidthLevel;
						}

						if (thisWidthLevel > farthestRightLevel)
						{
							farthestRightLevel = thisWidthLevel;
						}
					}

					// If an intersecting group was there, move the boxes to the right edge of the group and farther and reput it on the grid
					if (groupFarthestLeftLevel >= farthestLeftLevel && groupFarthestRightLevel <= farthestRightLevel)
					{
						for (LinkedBox boxToShift : group)
						{
							boxToShift.setWidthLevel(boxToShift.getWidthLevel() - groupFarthestLeftLevel + farthestRightLevel);
						}

						return putGroupOnGrid(group);
					}
				}
			}

			break;
		}
		*/

		int range = groupFarthestRightLevel - groupFarthestLeftLevel;
		// If we've made it this far, there's no intersecting group and we can proceed normally
		for (LinkedBox boxOnGrid : grid)
		{
			if (boxOnGrid.getWidthLevel() > groupFarthestRightLevel)
			{
				boxOnGrid.incWidthLevel();	
			}
			else
			{
				boxOnGrid.setWidthLevel(boxOnGrid.getWidthLevel() - range);
				boxOnGrid.decWidthLevel();
			}
		}

		if (boxWasThere)
		{
			for (LinkedBox box : group)
			{
				box.incWidthLevel();
			}
		}

		return group;
	}
	

	/*
	private void putBoxOnGridFromGroup(LinkedBox box)
	{
		grid.add(box);
	}
	*/

	public LinkedBox putSoloBoxOnGrid(LinkedBox box)
	{
		if (this.getBoxOnGridAt(box.getGridPosition()) == null)
		{
			if ((this.getBoxOnGridAt(box.getWidthLevel() + 1, box.getHeightLevel()) != null) || (this.getBoxOnGridAt(box.getWidthLevel() - 1, box.getHeightLevel()) != null))
			{
				box = this.makeRoomForSoloBox(box, false);
			}
		}
		else
		{
			box = this.makeRoomForSoloBox(box, true);
		}

		grid.add(box);
		return box;
	}

	
	public HashSet<LinkedBox> putGroupOnGrid(GroupWithLeftAndRight sortedGroup)
	{
		boolean foundBoxInTheWay = false;
		int farthestLeftLevel = sortedGroup.getFarthestLeftLevel();
		int farthestRightLevel = sortedGroup.getFarthestRightLevel();
		int thisWidthLevel = 0;

		HashSet<LinkedBox> group = sortedGroup.getGroup();

		for (LinkedBox box : group)
		{
			if (this.getBoxOnGridAt(box.getGridPosition()) != null)
			{
				foundBoxInTheWay = true;
				break;
			}
		}

		if (foundBoxInTheWay)
		{
			group = this.makeRoomForGroup(group, true, farthestLeftLevel, farthestRightLevel);
		}
		else
		{
			// Just getting height level from first box for check
			for (LinkedBox box : group)
			{
				if ((this.getBoxOnGridAt(farthestLeftLevel - 1, box.getHeightLevel()) != null) || (this.getBoxOnGridAt(farthestRightLevel + 1, box.getHeightLevel()) != null))
				{
					foundBoxInTheWay = true;
				}
				break;
			}

			if (foundBoxInTheWay)
			{
				group = this.makeRoomForGroup(group, false, farthestLeftLevel, farthestRightLevel);
			}
		}

		for (LinkedBox box : group)
		{
			grid.add(box);
		}

		return group;
	}
	

	public LinkedBox getBoxOnGridAt(Coordinate location)
	{
		LinkedBox returnBox = null;
		for (LinkedBox box : grid)
		{
			if (box.getGridPosition().equals(location))
			{
				returnBox = box;
				break;
			}
		}
		return returnBox;
	}

	public LinkedBox getBoxOnGridAt(int widthLevel, int heightLevel)
	{
		Coordinate searchCoord = new Coordinate(widthLevel, heightLevel);
		LinkedBox returnBox = null;
		for (LinkedBox box : grid)
		{
			if (box.getGridPosition().equals(searchCoord))
			{
				returnBox = box;
				break;
			}
		}
		return returnBox;
	}

	public LinkedBox getBoxOnGrid(LinkedBox boxToFind)
	{
		LinkedBox returnBox = null;
		for (LinkedBox box : grid)
		{
			if (box.equals(boxToFind))
			{
				returnBox = box;
				break;
			}
		}
		return returnBox;
	}

	@Override
	public String toString()
	{
		StringBuilder buffer = new StringBuilder("\n\tBEGIN ARGUMENT THREAD\n");
		for (LinkedBox box : boxMap.values())
		{
			buffer = buffer.append(box.toString());
		}

		buffer = buffer.append("\n\tEND ARGUMENT THREAD\n");
		return buffer.toString();
	}
}