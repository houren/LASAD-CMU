package lasad.gwt.client.model.organization;

import java.util.HashSet;
import lasad.gwt.client.model.organization.LinkedBox;
import lasad.gwt.client.logger.Logger;
import lasad.gwt.client.model.organization.Coordinate;
import lasad.gwt.client.model.organization.IntPair;

public class ArgumentGrid
{
	private final int HOR_SPACE = 2;
	private final int VERT_SPACE = 1;

	private HashSet<LinkedBox> grid;

	public ArgumentGrid()
	{
		grid = new HashSet<LinkedBox>();
	}

	public void clear()
	{
		this.grid.clear();
	}

	public int size()
	{
		return this.grid.size();
	}

	public HashSet<LinkedBox> getBoxes()
	{
		return grid;
	}

	public void organize(final boolean isOrganizeTopToBottom, HashSet<LinkedBox> boxesToPutOnGrid)
	{
		this.clear();
		for (LinkedBox box : boxesToPutOnGrid)
		{
			organizeRecursive(box, 0, 0, new HashSet<LinkedBox>(), isOrganizeTopToBottom);
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
			centerSharedChildren(getBoxesAtHeightLevel(currentLevel), isOrganizeTopToBottom);

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

	private void organizeRecursive(LinkedBox box, final int widthLevel, final int heightLevel, HashSet<LinkedBox> visited, final boolean isOrganizeTopToBottom)
	{
		if (!visited.contains(box))
		{
			visited.add(box);

			LinkedBox boxAlreadyOnGrid = this.findBoxOnGrid(box);

			if (boxAlreadyOnGrid == null)
			{
				box.setGridPosition(widthLevel, heightLevel);
				
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

			for (LinkedBox child : box.getChildBoxes())
			{
				if (isOrganizeTopToBottom)
				{
					organizeRecursive(child, box.getWidthLevel(), box.getHeightLevel() - VERT_SPACE, visited, isOrganizeTopToBottom);
				}
				else
				{
					organizeRecursive(child, box.getWidthLevel(), box.getHeightLevel() + VERT_SPACE, visited, isOrganizeTopToBottom);	
				}
			}

			for (LinkedBox parent : box.getParentBoxes())
			{
				if (isOrganizeTopToBottom)
				{
					organizeRecursive(parent, box.getWidthLevel(), box.getHeightLevel() + VERT_SPACE, visited, isOrganizeTopToBottom);
				}
				else
				{
					organizeRecursive(parent, box.getWidthLevel(), box.getHeightLevel() - VERT_SPACE, visited, isOrganizeTopToBottom);	
				}
			}
		}
	}

	private void centerSharedChildren(HashSet<LinkedBox> boxesAtLevel, final boolean isOrganizeTopToBottom)
	{
		HashSet<LinkedBox> visited = new HashSet<LinkedBox>();
		HashSet<LinkedBox> otherBoxes = new HashSet<LinkedBox>();
		otherBoxes.addAll(boxesAtLevel);

		for (LinkedBox boxA : boxesAtLevel)
		{
			if (!visited.contains(boxA))
			{
				otherBoxes.remove(boxA);
			
				HashSet<LinkedBox> parentGroup = new HashSet<LinkedBox>();
				parentGroup.add(boxA);
				HashSet<LinkedBox> childGroup = boxA.getChildBoxes();

				for (LinkedBox boxB : otherBoxes)
				{
					HashSet<LinkedBox> childrenB = boxB.getChildBoxes();
					if (childGroup.size() == childrenB.size() && childGroup.containsAll(childrenB))
					{
						parentGroup.add(boxB);
						otherBoxes.remove(boxB);
					}
				}

				visited.addAll(parentGroup);

				int numParentsSharing = parentGroup.size();

				int leftMostParentWidth = Integer.MAX_VALUE;
				for (LinkedBox sharer : parentGroup)
				{
					if (sharer.getWidthLevel() < leftMostParentWidth)
					{
						leftMostParentWidth = sharer.getWidthLevel();
					}
				}

				int numChildren = childGroup.size();
				int leftMostChildWidth = Integer.MAX_VALUE;
				for (LinkedBox child : childGroup)
				{
					if (child.getWidthLevel() < leftMostChildWidth)
					{
						leftMostChildWidth = child.getWidthLevel();
					}
				}
				int widthLeftChildShouldBe = leftMostParentWidth + numParentsSharing - numChildren;
				int numLevelsToMove = widthLeftChildShouldBe - leftMostChildWidth;

				for (LinkedBox child : childGroup)
				{
					shiftHeightLevelAndAboveOrBelow(child.getHeightLevel(), numLevelsToMove, isOrganizeTopToBottom);
					break;
				}
			}
		}
	}

	private void shiftHeightLevelAndAboveOrBelow(int heightLevel, int amount, final boolean isOrganizeTopToBottom)
	{
		if (isOrganizeTopToBottom)
		{
			for (LinkedBox boxOnGrid : grid)
			{
				if (heightLevel >= boxOnGrid.getHeightLevel())
				{
					boxOnGrid.setWidthLevel(boxOnGrid.getWidthLevel() + amount);
				}
			}
		}
		else
		{
			for (LinkedBox boxOnGrid : grid)
			{
				if (heightLevel <= boxOnGrid.getHeightLevel())
				{
					boxOnGrid.setWidthLevel(boxOnGrid.getWidthLevel() + amount);
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

	private HashSet<LinkedBox> assignGridPositionToSibling(LinkedBox box, final boolean shouldIncrease, final int widthLevel, final int heightLevel, HashSet<LinkedBox> visited)
	{
		if (!visited.contains(box))
		{
			visited.add(box);
			box.setGridPosition(widthLevel, heightLevel);
			for (LinkedBox sibling : box.getSiblingBoxes())
			{
				if (shouldIncrease)
				{
					visited = assignGridPositionToSibling(sibling, shouldIncrease, widthLevel + HOR_SPACE, heightLevel, visited);
				}
				else
				{
					visited = assignGridPositionToSibling(sibling, shouldIncrease, widthLevel - HOR_SPACE, heightLevel, visited);
				}
			}
		}
		return visited;
	}
	
	private GroupWithLeftAndRight sortGroup(LinkedBox original)
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
				siblingGroup.addAll(assignGridPositionToSibling(sibling, true, origWidthLevel + HOR_SPACE, origHeightLevel, siblingGroup));
				isFirstSibling = false;
			}
			else
			{
				siblingGroup.addAll(assignGridPositionToSibling(sibling, false, origWidthLevel - HOR_SPACE, origHeightLevel, siblingGroup));
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

		int farthestRightLevel = farthestLeftLevel + (siblingGroup.size() - 1) * HOR_SPACE;

		return new GroupWithLeftAndRight(siblingGroup, farthestLeftLevel, farthestRightLevel);
	}

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

	public int getMaxWidthLevel()
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

	public int getMaxHeightLevel()
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

	public HashSet<LinkedBox> getBoxesAtWidthLevel(int widthLevel)
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

	public HashSet<LinkedBox> getBoxesAtHeightLevel(int heightLevel)
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

	public HashSet<LinkedBox> getBoxesAtRootLevel(final boolean isOrganizeTopToBottom)
	{
		IntPair minMaxLevels = determineMinMaxHeightLevels();
		if (isOrganizeTopToBottom)
		{
			return getBoxesAtHeightLevel(minMaxLevels.getMax());
		}
		else
		{
			return getBoxesAtHeightLevel(minMaxLevels.getMin());
		}
	}

	public HashSet<LinkedBox> getBoxesAtEndLevel(final boolean isOrganizeTopToBottom)
	{
		IntPair minMaxLevels = determineMinMaxHeightLevels();
		if (isOrganizeTopToBottom)
		{
			return getBoxesAtHeightLevel(minMaxLevels.getMin());
		}
		else
		{
			return getBoxesAtHeightLevel(minMaxLevels.getMax());
		}
	}

	private LinkedBox makeRoomForSoloBox(LinkedBox box, boolean boxWasThere)
	{
		int widthLevel = box.getWidthLevel();
		int heightLevel = box.getHeightLevel();

		// If we made it this far, there was no intersecting group so we may proceed normally
		for (LinkedBox boxOnGrid : getBoxesAtHeightLevel(heightLevel))
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

	
	private HashSet<LinkedBox> makeRoomForGroup(HashSet<LinkedBox> group, boolean boxWasThere, int groupFarthestLeftLevel, int groupFarthestRightLevel)
	{
		int heightLevel = 0;
		for (LinkedBox box : group)
		{
			heightLevel = box.getHeightLevel();
			break;
		}

		int range = groupFarthestRightLevel - groupFarthestLeftLevel;
		// If we've made it this far, there's no intersecting group and we can proceed normally
		for (LinkedBox boxOnGrid : getBoxesAtHeightLevel(heightLevel))
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

	private LinkedBox putSoloBoxOnGrid(LinkedBox box)
	{
		if (this.getBoxAt(box.getGridPosition()) == null)
		{
			if ((this.getBoxAt(box.getWidthLevel() + 1, box.getHeightLevel()) != null) || (this.getBoxAt(box.getWidthLevel() - 1, box.getHeightLevel()) != null))
			{
				box = this.makeRoomForSoloBox(box, false);
			}
		}
		else
		{
			box = this.makeRoomForSoloBox(box, true);
		}

		LinkedBox boxAlreadyThere = this.getBoxAt(box.getGridPosition());
		if (boxAlreadyThere != null)
		{
			Logger.log("putGroupOnGrid Error.", Logger.DEBUG);
			Logger.log(box.toStringShort() + " wants to be placed where " + boxAlreadyThere.toStringShort() + " already is, which is " + box.getWidthLevel() + ", " + box.getHeightLevel(), Logger.DEBUG);
		}

		grid.add(box);

		return box;
	}

	
	private HashSet<LinkedBox> putGroupOnGrid(GroupWithLeftAndRight sortedGroup)
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
			if (this.getBoxAt(box.getGridPosition()) != null)
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
			if ((this.getBoxAt(farthestLeftLevel - 1, heightLevel) != null) || (this.getBoxAt(farthestRightLevel + 1, heightLevel) != null))
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
			LinkedBox boxAlreadyThere = this.getBoxAt(box.getGridPosition());
			if (boxAlreadyThere != null)
			{
				Logger.log("putGroupOnGrid Error.", Logger.DEBUG);
				Logger.log(box.toStringShort() + " wants to be placed where " + boxAlreadyThere.toStringShort() + " already is, which is " + box.getWidthLevel() + ", " + box.getHeightLevel(), Logger.DEBUG);
			}
		}

		grid.addAll(group);

		return group;
	}
	

	public LinkedBox getBoxAt(Coordinate location)
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

	public LinkedBox getBoxAt(int widthLevel, int heightLevel)
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

	public LinkedBox findBoxOnGrid(LinkedBox boxToFind)
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
}