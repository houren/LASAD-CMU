package lasad.gwt.client.model.organization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.HashSet;
import lasad.gwt.client.model.organization.LinkedBox;
import lasad.gwt.client.model.organization.OrganizerLink;
import java.util.Collection;
import lasad.gwt.client.logger.Logger;
import lasad.gwt.client.model.organization.Coordinate;
import lasad.gwt.client.model.organization.IntPair;

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
			boxMap.put(box.getBoxID(), box);
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
		LinkedBox boxToRemove = this.getBoxByBoxID(boxID);
		if (boxToRemove != null)
		{
			this.removeLinksTo(boxToRemove);
			LinkedBox returnValue = boxMap.remove(boxID);
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

	public void removeLinksTo(LinkedBox boxBeingRemoved)
	{
		HashSet<LinkedBox> relatedBoxes = boxBeingRemoved.getRelatedBoxes();
		for (LinkedBox box : this.getBoxes())
		{
			if (relatedBoxes.contains(box))
			{
				box.removeLinkTo(boxBeingRemoved);
			}
		}
	}

	public boolean contains(LinkedBox box)
	{
		return boxMap.containsValue(box);
	}

	public LinkedBox getBoxByBoxID(int boxID)
	{
		return boxMap.get(boxID);
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

	public static void decNumThreads()
	{
		numThreads--;
	}
/*
	public void fixOverlaps()
	{
		int rootLevel;
		int minLevel = Integer.MAX_VALUE;;
		int maxLevel = Integer.MIN_VALUE;;

		IntPair returnData = determineMinMaxHeightLevels();

		int minLevel = returnData.getMin();
		int maxLevel = returnData.getMax();
		int rootLevel = returnData.calcRoot(isOrganizeTopToBottom);
		int range = maxLevel - minLevel;

		int currentLevel = rootLevel;

		for (int counter = 0; counter <= range; counter++)
		{
			fixRowSpacing(getBoxesOnGridAtHeightLevel(currentLevel));

			if (isOrganizeTopToBottom)
			{
				currentLevel--;
			}
			else
			{
				currentLevel++;
			}
		}
	}

	public void fixRowSpacing(HashSet<LinkedBox> row)
	{
		HashSet<LinkedBox> loopController = new HashSet<LinkedBox>(row);
		Vector<LinkedBox> boxesLeftToRight = new Vector<LinkedBox>();
		LinkedBox leftBox = null;

		for (LinkedBox counter : loopController)
		{
			int minWidth = Integer.MAX_VALUE;

			for (LinkedBox box : row)
			{
				if (box.getWidthLevel() < minWidth)
				{
					leftBox = box;
				}
			}

			if (leftBox != null)
			{
				boxesLeftToRight.add(leftBox);
				row.remove(leftBox);
			}
		}

		int numLevelsToMove = 0;
		for (LinkedBox box : boxesLeftToRight)
		{
			if ()
			if (grid.getBoxAt)
			grid.remove(box);
			box.setWidthLevel(box.getWidthLevel() + numLevelsToMove);
			grid.add(box);
		}
	}
	*/

	public IntPair determineMinMaxHeightLevels()
	{
		int minLevel = Integer.MAX_VALUE;
		int maxLevel = Integer.MIN_VALUE;

		for (LinkedBox box : grid)
		{
			int heightLevel = box.getHeightLevel();

			if (heightLevel < minLevel)
			{
				minLevel = heightLevel;
			}

			if (heightLevel > maxLevel)
			{
				maxLevel = heightLevel;
			}
		}

		return new IntPair(minLevel, maxLevel);
	}

	public IntPair determineMinMaxWidthLevels()
	{
		int minLevel = Integer.MAX_VALUE;
		int maxLevel = Integer.MIN_VALUE;

		for (LinkedBox box : grid)
		{
			int widthLevel = box.getWidthLevel();

			if (widthLevel < minLevel)
			{
				minLevel = widthLevel;
			}

			if (widthLevel > maxLevel)
			{
				maxLevel = widthLevel;
			}
		}

		return new IntPair(minLevel, maxLevel);
	}

	public void organizeGrid(final boolean isOrganizeTopToBottom)
	{
		grid.clear();
		for (LinkedBox box : this.getBoxes())
		{
			organizeGridRecursive(box, 0, 0, new HashSet<LinkedBox>(), isOrganizeTopToBottom);
			break;
		}

		IntPair returnData = determineMinMaxHeightLevels();

		int minLevel = returnData.getMin();
		int maxLevel = returnData.getMax();
		int rootLevel = returnData.calcRoot(isOrganizeTopToBottom);
		int range = maxLevel - minLevel;

		int currentLevel = rootLevel;

		for (int counter = 0; counter <= range; counter++)
		{
			centerChildrenOfGroups(getBoxesOnGridAtHeightLevel(currentLevel), isOrganizeTopToBottom);

			if (isOrganizeTopToBottom)
			{
				currentLevel--;
			}
			else
			{
				currentLevel++;
			}
		}
		
	}

	public void centerChildrenOfGroups(HashSet<LinkedBox> boxesAtLevel, final boolean isOrganizeTopToBottom)
	{
		HashSet<LinkedBox> visited = new HashSet<LinkedBox>();

		for (LinkedBox box : boxesAtLevel)
		{
			if (!visited.contains(box))
			{
				HashSet<LinkedBox> thisAndExtendedSibs = box.getThisAndExtendedSiblings();
				visited.addAll(thisAndExtendedSibs);
				int numParentsSharing = thisAndExtendedSibs.size();

				int leftMostParentWidth = Integer.MAX_VALUE;
				for (LinkedBox sharer : thisAndExtendedSibs)
				{
					if (sharer.getWidthLevel() < leftMostParentWidth)
					{
						leftMostParentWidth = sharer.getWidthLevel();
					}
				}
				int numChildren = box.getNumChildren();
				int leftMostChildWidth = Integer.MAX_VALUE;

				for (LinkedBox child : box.getChildBoxes())
				{
					if (child.getWidthLevel() < leftMostChildWidth)
					{
						leftMostChildWidth = child.getWidthLevel();
					}
				}
				int widthLeftChildShouldBe = leftMostParentWidth + numParentsSharing - numChildren;
				int numLevelsToMove = widthLeftChildShouldBe - leftMostChildWidth;

				for (LinkedBox child : box.getChildBoxes())
				{
					shiftHeightLevelAndAboveOrBelow(child.getHeightLevel(), numLevelsToMove, isOrganizeTopToBottom);
					break;
				}
			}
		}
	}

	public void shiftHeightLevelAndAboveOrBelow(int heightLevel, int amount, final boolean isOrganizeTopToBottom)
	{
		if (isOrganizeTopToBottom)
		{
			for (LinkedBox onGrid : grid)
			{
				if (heightLevel >= onGrid.getHeightLevel())
				{
					onGrid.setWidthLevel(onGrid.getWidthLevel() + amount);
				}
			}
		}
		else
		{
			for (LinkedBox onGrid : grid)
			{
				if (heightLevel <= onGrid.getHeightLevel())
				{
					onGrid.setWidthLevel(onGrid.getWidthLevel() + amount);
				}
			}
		}
	}
	

	private void organizeGridRecursive(LinkedBox box, final int widthLevel, final int heightLevel, HashSet<LinkedBox> visited, final boolean isOrganizeTopToBottom)
	{
		if (!visited.contains(box))
		{
			visited.add(box);

			LinkedBox boxAlreadyOnGrid = this.getBoxOnGrid(box);

			if (boxAlreadyOnGrid == null)
			{
				box.setGridPosition(widthLevel, heightLevel);
				//putSoloBoxOnGrid(box);

				
				if (box.getNumSiblings() == 0)
				{
					putSoloBoxOnGrid(box);
				}
				else
				{
					putGroupOnGrid(sortGroup(box));
				}
			}
			else
			{
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

	public HashSet<LinkedBox> assignGridPositionToSibling(LinkedBox box, final boolean shouldIncrease, final int widthLevel, final int heightLevel, HashSet<LinkedBox> visited)
	{
		if (!visited.contains(box))
		{
			visited.add(box);
			box.setGridPosition(widthLevel, heightLevel);
			for (LinkedBox sibling : box.getSiblingBoxes())
			{
				if (shouldIncrease)
				{
					visited = assignGridPositionToSibling(sibling, shouldIncrease, widthLevel + SPACE, heightLevel, visited);
				}
				else
				{
					visited = assignGridPositionToSibling(sibling, shouldIncrease, widthLevel - SPACE, heightLevel, visited);
				}
			}
		}
		return visited;
	}
	
	public GroupWithLeftAndRight sortGroup(LinkedBox original)
	{
		int origWidthLevel = original.getWidthLevel();
		int origHeightLevel = original.getHeightLevel();

		HashSet<LinkedBox> siblingGroup = new HashSet<LinkedBox>();

		siblingGroup.add(original);

		boolean isFirstSibling = true;
		for (LinkedBox sibling : original.getSiblingBoxes())
		{
			if (isFirstSibling)
			{
				siblingGroup.addAll(assignGridPositionToSibling(sibling, true, origWidthLevel + SPACE, origHeightLevel, siblingGroup));
				isFirstSibling = false;
			}
			else
			{
				siblingGroup.addAll(assignGridPositionToSibling(sibling, false, origWidthLevel - SPACE, origHeightLevel, siblingGroup));
			}

		}
		int farthestLeftLevel = Integer.MAX_VALUE;

		for (LinkedBox box : siblingGroup)
		{
			if (box.getWidthLevel() < farthestLeftLevel)
			{
				farthestLeftLevel = box.getWidthLevel();
			}
		}

		int farthestRightLevel = farthestLeftLevel + (siblingGroup.size() - 1) * SPACE;

		return new GroupWithLeftAndRight(siblingGroup, farthestLeftLevel, farthestRightLevel);
	}

	public int getMaxWidthLevelOnGrid()
	{
		int maxX = Integer.MIN_VALUE;

		for (LinkedBox box : grid)
		{
			Coordinate coordinate = box.getGridPosition();
			if (coordinate.getX() > maxX)
			{
				maxX = coordinate.getX();
			}
		}
		return maxX;
	}

	public int getMaxHeightLevelOnGrid()
	{
		int maxY = Integer.MIN_VALUE;

		for (LinkedBox box : grid)
		{
			Coordinate coordinate = box.getGridPosition();
			if (coordinate.getY() > maxY)
			{
				maxY = coordinate.getY();
			}
		}
		return maxY;
	}

	public HashSet<LinkedBox> getBoxesOnGridAtWidthLevel(int widthLevel)
	{
		HashSet boxesAtWidthLevel = new HashSet<LinkedBox>();
		for (LinkedBox box : grid)
		{
			if (box.getWidthLevel() == widthLevel)
			{
				boxesAtWidthLevel.add(box);
			}
		}

		return boxesAtWidthLevel;
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

	public HashSet<LinkedBox> getBoxesOnGridAtRootLevel(final boolean isOrganizeTopToBottom)
	{
		IntPair minMaxLevels = determineMinMaxHeightLevels();
		if (isOrganizeTopToBottom)
		{
			return getBoxesOnGridAtHeightLevel(minMaxLevels.getMax());
		}
		else
		{
			return getBoxesOnGridAtHeightLevel(minMaxLevels.getMin());
		}
	}

	public HashSet<LinkedBox> getBoxesOnGridAtEndLevel(final boolean isOrganizeTopToBottom)
	{
		IntPair minMaxLevels = determineMinMaxHeightLevels();
		if (isOrganizeTopToBottom)
		{
			return getBoxesOnGridAtHeightLevel(minMaxLevels.getMin());
		}
		else
		{
			return getBoxesOnGridAtHeightLevel(minMaxLevels.getMax());
		}
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
		for (LinkedBox boxOnGrid : getBoxesOnGridAtHeightLevel(heightLevel))
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
		int heightLevel = 0;
		for (LinkedBox box : group)
		{
			heightLevel = box.getHeightLevel();
			break;
		}
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
		for (LinkedBox boxOnGrid : getBoxesOnGridAtHeightLevel(heightLevel))
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

		LinkedBox boxAlreadyThere = this.getBoxOnGridAt(box.getGridPosition());
		if (boxAlreadyThere != null)
		{
			Logger.log("putGroupOnGrid Error.", Logger.DEBUG);
			Logger.log(box.toStringShort() + " wants to be placed where " + boxAlreadyThere.toStringShort() + " already is, which is " + box.getWidthLevel() + ", " + box.getHeightLevel(), Logger.DEBUG);
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

		int heightLevel = 0;
		for (LinkedBox box : group)
		{
			heightLevel = box.getHeightLevel();
			break;
		}

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
			if ((this.getBoxOnGridAt(farthestLeftLevel - 1, heightLevel) != null) || (this.getBoxOnGridAt(farthestRightLevel + 1, heightLevel) != null))
			{
				foundBoxInTheWay = true;
			}

			if (foundBoxInTheWay)
			{
				group = this.makeRoomForGroup(group, false, farthestLeftLevel, farthestRightLevel);
			}
		}

		for (LinkedBox box : group)
		{
			LinkedBox boxAlreadyThere = this.getBoxOnGridAt(box.getGridPosition());
			if (boxAlreadyThere != null)
			{
				Logger.log("putGroupOnGrid Error.", Logger.DEBUG);
				Logger.log(box.toStringShort() + " wants to be placed where " + boxAlreadyThere.toStringShort() + " already is, which is " + box.getWidthLevel() + ", " + box.getHeightLevel(), Logger.DEBUG);
			}
		}

		grid.addAll(group);

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