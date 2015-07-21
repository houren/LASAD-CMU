package lasad.gwt.client.model.organization;

import java.util.HashSet;
import lasad.gwt.client.model.organization.LinkedBox;
import lasad.gwt.client.logger.Logger;
import lasad.gwt.client.model.organization.Coordinate;
import lasad.gwt.client.model.organization.IntPair;

/**
 *	Provides a chessboard like organization of each argument thread, with specific coordinate positions translated from this model by AutoOrganizer.
 *	@author Kevin Loughlin
 *	@since 20 July 2015, Last Updated 21 July 2015	
 */
public class ArgumentGrid
{
	// Horizontal and vertical space between boxes in the same row and column respectively
	private final int HOR_SPACE = 2;
	private final int VERT_SPACE = 1;

	// The actual grid, wrapped in this class
	private HashSet<LinkedBox> grid;

	public ArgumentGrid()
	{
		grid = new HashSet<LinkedBox>();
	}

	/**
	 *	The grid will be reaccumulated with each autoOrganize cycle, so clear it between uses for memory speed
	 */
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

	/**
	 *	The method that actually organizes the grid and thus is key to auto organization
	 */
	public void organize(final boolean isOrganizeTopToBottom, HashSet<LinkedBox> boxesToPutOnGrid)
	{
		// Remember to clear the grid before organization, since boxes must be reaccumulated
		this.clear();

		// Call the recursive helper on the first box, and then break (since recursion will reach all of the boxes)
		for (LinkedBox box : boxesToPutOnGrid)
		{
			organizeRecursive(box, 0, 0, new HashSet<LinkedBox>(), isOrganizeTopToBottom);
			break;
		}

		// Necessary to center children of shared parents because my organization algorithm fails to do this
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

	/*
	 *	Recurisve helper for organize that visits each box, assigns it a widthlevel and heightlevel, and puts it on the grid
	 *	@param box - the box to be put, initiliazed as any box from the thread (all boxes will be visited via recursion)
	 *	@param widthLevel - The widthLevel to be assigned, initialize as 0
	 *	@param heightLevel - The heightLevel to be assigned, initialize as 0
	 *	@param visited - The boxes already visited, initialize as empty
	 *	@param isOrganizeTopToBottom - Whether or not to organizeTopToBottom or vice-versa
	 */
	private void organizeRecursive(LinkedBox box, final int widthLevel, final int heightLevel, HashSet<LinkedBox> visited, final boolean isOrganizeTopToBottom)
	{
		if (!visited.contains(box))
		{
			visited.add(box);

			LinkedBox boxAlreadyOnGrid = this.findBoxOnGrid(box);

			// If the box is not already on the grid, assign it the passed width and height levels
			if (boxAlreadyOnGrid == null)
			{
				box.setGridPosition(widthLevel, heightLevel);
				
				// If no siblings, handle box as solo
				if (box.getNumSiblings() == 0)
				{
					putSoloBoxOnGrid(box);
				}

				// If siblings, organize the group and put the group on together
				else
				{
					putGroupOnGrid(sortGroup(box));
				}
			}

			// If already on the grid, set the grid position as the version already on the grid
			else
			{
				box.setGridPosition(boxAlreadyOnGrid.getGridPosition());
			}

			// Children and parents should be placed directly above/below depending on isOrganizeTopToBottom.  If a box is already there,
			// room will be made when putting the box on the grid
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

	// Centers shared children above the parents sharing them
	private void centerSharedChildren(HashSet<LinkedBox> boxesAtLevel, final boolean isOrganizeTopToBottom)
	{
		HashSet<LinkedBox> visited = new HashSet<LinkedBox>();
		HashSet<LinkedBox> otherBoxes = new HashSet<LinkedBox>();
		otherBoxes.addAll(boxesAtLevel);

		for (LinkedBox boxA : boxesAtLevel)
		{
			if (!visited.contains(boxA))
			{
				visited.add(boxA);
				otherBoxes.removeAll(visited);
			
				HashSet<LinkedBox> parentGroup = new HashSet<LinkedBox>();
				parentGroup.add(boxA);
				HashSet<LinkedBox> childGroup = boxA.getChildBoxes();

				for (LinkedBox boxB : otherBoxes)
				{
					HashSet<LinkedBox> childrenB = boxB.getChildBoxes();
					if (childGroup.size() == childrenB.size() && childGroup.containsAll(childrenB))
					{
						parentGroup.add(boxB);
						visited.add(boxB);
					}
				}

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
					shiftBoxesAtHeightLevelAndAboveOrBelow(child.getHeightLevel(), numLevelsToMove, isOrganizeTopToBottom);
					break;
				}
			}
		}
	}

	// Shifts the boxes at the height level and above/below (depending on isOrganizeTopToBottom) by amount to maintain alignment
	private void shiftBoxesAtHeightLevelAndAboveOrBelow(int heightLevel, int amount, final boolean isOrganizeTopToBottom)
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

	/*
	 *	Provides a way to store a sibling group and its farthest right and left widthLevels all together for speed purposes
	 */
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

	/*
	 *	Siblings in one direction should all increase in widthLevel, the other direction must decrease
	 */
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
	
	/*
	 *	Sorts a group, preserving the order of the boxes in a logical sense (i.e. the siblings in the group with only 1 direct sibling will go
	 *	on the ends of the group).  The originalm box of the group will maintain its position, and the other group boxes will go around it.
	 */
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

	/**
	 *	Get the min and max height levels of the grid.
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

	/**
	 *	Get the min and max width levels of the grid.
	 */
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

	/**
	 *	Gets the boxes on the grid at the passed width level
	 */
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

	/**
	 *	Gets the boxes on the grid at the passed height level
	 */
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

	/**
	 *	Gets the boxes on the grid at the root level.  If isOrganizeTopToBottom is true, the rootLevel is top and endLevel is bottom.
	 *	Vice-versa if false.
	 */
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

	// moves boxes out of the way for a solo box
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

	// Moves boxes out of the way for a group of siblings boxes
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

	// Puts a solo box on grid, making room if necessary
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

	// puts all the group boxes on a grid adjacently, making room if necessary
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
	
	// get the box on the grid at the coordinate location, null if none there
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

	// Get the passed box if present on the grid, return null if not present
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

	public String toString()
	{
		IntPair heightData = determineMinMaxHeightLevels();
		int minHeight = heightData.getMin();
		int maxHeight = heightData.getMax();

		IntPair widthData = determineMinMaxWidthLevels();
		int minWidth = widthData.getMin();
		int maxWidth = widthData.getMax();

		StringBuilder buffer = new StringBuilder("\nGrid...\n");

		for (int currentRow = maxHeight; currentRow >= minHeight; currentRow--)
		{
			buffer.append(currentRow + "\t|\t");
			for (int currentColumn = minWidth; currentColumn <= maxWidth; currentColumn++)
			{
				LinkedBox boxThere = getBoxAt(currentColumn, currentRow);
				if (boxThere != null)
				{
					buffer.append(boxThere.getRootID() + "\t");
				}
				else
				{
					buffer.append("*\t");
				}
			}
			buffer.append("\n");
		}

		buffer.append("\t----");

		for (int currentColumn = minWidth; currentColumn <= maxWidth; currentColumn++)
		{
			buffer.append("----");
		}

		buffer.append("\n\t\t");

		for (int currentColumn = minWidth; currentColumn <= maxWidth; currentColumn++)
		{
			buffer.append(currentColumn + "\t");
		}
		return buffer.toString();
	}
}