package lasad.gwt.client.model.organization;

import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;

import lasad.gwt.client.model.organization.LinkedBox;
import lasad.gwt.client.logger.Logger;
import lasad.gwt.client.model.organization.Coordinate;
import lasad.gwt.client.model.organization.IntPair;

/**
 *	Provides a chessboard like organization of each argument thread, with specific coordinate positions translated from this model by AutoOrganizer.
 *	@author Kevin Loughlin
 *	@since 20 July 2015, Last Updated 29 July 2015	
 */
public class ArgumentGrid
{
	// Horizontal and vertical space between boxes in the same row and column respectively
	private final int HOR_SPACE = 2;
	private final int VERT_SPACE = 1;

	// The actual grid, wrapped in this class
	private HashMap<Coordinate, LinkedBox> grid;

	public ArgumentGrid()
	{
		grid = new HashMap<Coordinate, LinkedBox>();
	}

	public ArgumentGrid(HashMap<Coordinate, LinkedBox> grid)
	{
		this();
		this.grid = grid;
	}

	/**
	 *	The grid will be reaccumulated with each autoOrganize cycle, so clear it between uses for memory speed
	 */
	public void empty()
	{
		this.grid.clear();
	}

	public int size()
	{
		return this.grid.size();
	}

	public Collection<LinkedBox> getBoxes()
	{
		return grid.values();
	}

	/* If they want the boxes, they can get them with getBoxes.  This is just for inside this class. */
	private HashMap<Coordinate, LinkedBox> getGrid()
	{
		return grid;
	}

	/*
		For the downward orientation, recursively sets height levels.
	*/
	private HashSet<LinkedBox> topToBottomSetHeightLevels(LinkedBox box, final int HEIGHT_LEVEL, HashSet<LinkedBox> visited)
	{
		if (!visited.contains(box))
		{
			box.setHeightLevel(HEIGHT_LEVEL);
			visited.add(box);

			for (LinkedBox sibling : box.getSiblingBoxes())
			{
				visited = topToBottomSetHeightLevels(sibling, HEIGHT_LEVEL, visited);
			}

			for (LinkedBox child : box.getChildBoxes())
			{
				visited = topToBottomSetHeightLevels(child, HEIGHT_LEVEL - VERT_SPACE, visited);
			}
			
			for (LinkedBox parent : box.getParentBoxes())
			{
				visited = topToBottomSetHeightLevels(parent, HEIGHT_LEVEL + VERT_SPACE, visited);
			}
		}

		return visited;
	}

	/*
		For the upwardward orientation, recursively sets height levels.
	*/
	private HashSet<LinkedBox> bottomToTopSetHeightLevels(LinkedBox box, final int HEIGHT_LEVEL, HashSet<LinkedBox> visited)
	{
		if (!visited.contains(box))
		{
			box.setHeightLevel(HEIGHT_LEVEL);
			visited.add(box);

			for (LinkedBox sibling : box.getSiblingBoxes())
			{
				visited = bottomToTopSetHeightLevels(sibling, HEIGHT_LEVEL, visited);
			}

			for (LinkedBox child : box.getChildBoxes())
			{
				visited = bottomToTopSetHeightLevels(child, HEIGHT_LEVEL + VERT_SPACE, visited);
			}
			
			for (LinkedBox parent : box.getParentBoxes())
			{
				visited = bottomToTopSetHeightLevels(parent, HEIGHT_LEVEL - VERT_SPACE, visited);
			}
		}

		return visited;
	}

	// Calculates lowest height level currently on the grid
	private int calcLowestLevel()
	{
		int lowestRow = Integer.MAX_VALUE;
		for (LinkedBox box : this.getBoxes())
		{
			if (box.getHeightLevel() < lowestRow)
			{
				lowestRow = box.getHeightLevel();
			}
		}
		return lowestRow;
	}

	// Calculates highest height level currently on the grid
	private int calcHighestLevel()
	{
		int highestRow = Integer.MIN_VALUE;
		for (LinkedBox box : this.getBoxes())
		{
			if (box.getHeightLevel() > highestRow)
			{
				highestRow = box.getHeightLevel();
			}
		}
		return highestRow;
	}

	// Sorts the "starting" row of the grid (top row for upward orientation and bottom row for downward) and puts it on the grid
	private void sortAndPutStartRow(Collection<LinkedBox> startRow)
	{
		HashSet<LinkedBox> visited = new HashSet<LinkedBox>();
		int widthLevel = 0;
		for (LinkedBox box : startRow)
		{
			if (!visited.contains(box))
			{
				box.setWidthLevel(widthLevel);

				if (box.getNumSiblings() == 0)
				{
					visited.add(box);
					widthLevel = putSoloBoxOnGrid(box);
				}
				else
				{
					ArrayList<LinkedBox> sortedGroup = this.sortGroup(box, widthLevel);
					visited.addAll(sortedGroup);
					widthLevel = putGroupOnGrid(sortedGroup);
				}
			}		
		}
	}

	/**
	 *	The method that actually organizes the grid and thus is key to auto organization.
	 *	This should only be called from organizeGrid in ArgumentThread.
	 *	@param DOWNWARD - The orientation of the organization, true for downward, false for upward
	 *	@param boxesToPutOnGrid - The corresponding argThread's boxes.
	 *	@return The new, organized ArgumentGrid
	 */
	public ArgumentGrid organize(final boolean DOWNWARD, Collection<LinkedBox> boxesToPutOnGrid)
	{
		// Remember to clear the grid before organization, since boxes must be reaccumulated
		this.empty();

		if (boxesToPutOnGrid == null || boxesToPutOnGrid.size() == 0)
		{
			return this;
		}

		// Set the height levels of the boxes and determine the starting row
		// Start row is bottom row if DOWNWARD = true, top row if false
		int startRowNum = Integer.MAX_VALUE;	
		if (DOWNWARD)
		{
			for (LinkedBox box : boxesToPutOnGrid)
			{
				boxesToPutOnGrid = topToBottomSetHeightLevels(box, 0, new HashSet<LinkedBox>());
				startRowNum = determineMinMaxHeightLevels(boxesToPutOnGrid).getMin();
				break;
			}
		}
		else
		{
			for (LinkedBox box : boxesToPutOnGrid)
			{
				boxesToPutOnGrid = bottomToTopSetHeightLevels(box, 0, new HashSet<LinkedBox>());
				startRowNum = determineMinMaxHeightLevels(boxesToPutOnGrid).getMax();
				break;
			}
		}

		// Gather the startRow boxes and determine the max and min row (height level) of the grid
		HashSet<LinkedBox> startRowBoxes = new HashSet<LinkedBox>();
		int minRow = Integer.MAX_VALUE;
		int maxRow = Integer.MIN_VALUE;
		for (LinkedBox box : boxesToPutOnGrid)
		{
			final int HEIGHT = box.getHeightLevel();
			if (HEIGHT == startRowNum)
			{
				startRowBoxes.add(box);
			}
			if (HEIGHT < minRow)
			{
				minRow = HEIGHT;
			}

			if (HEIGHT > maxRow)
			{
				maxRow = HEIGHT;
			}
		}

		// Adds start row to grid
		this.sortAndPutStartRow(startRowBoxes);

		final int RANGE = maxRow - minRow;
		int rowIndex = startRowNum;

		// Adds all the rest of the rows to the grid, based from the startLevel.  Essentially we take an already present row and add its children.
		// If DOWNWARD orientation of the grid, we're travelling in reverse direction up the rows during this loop, and vice-versa.
		for (int counter = 0; counter < RANGE; counter++)
		{
			ArrayList<LinkedBox> rowBoxes = this.getBoxesAtHeightLevel(rowIndex);

			int nextAvailableWidth = putParentsOfRowOnGrid(rowBoxes);

			final int PARENT_HEIGHT;
			if (DOWNWARD)
			{
				PARENT_HEIGHT = rowIndex + 1;
			}
			else
			{
				PARENT_HEIGHT = rowIndex - 1;
			}

			ArrayList<LinkedBox> parentLevelBoxes = new ArrayList<LinkedBox>();

			for (LinkedBox box : boxesToPutOnGrid)
			{
				if (box.getHeightLevel() == PARENT_HEIGHT)
				{
					parentLevelBoxes.add(box);
				}
			}

			// Adds (to the grid) boxes in the parentLevel that didnt have children in the currentRow, specified by rowIndex
			for (LinkedBox box : parentLevelBoxes)
			{
				if (!grid.values().contains(box))
				{
					box.setWidthLevel(nextAvailableWidth);

					if (box.getNumSiblings() == 0)
					{
						nextAvailableWidth = putSoloBoxOnGrid(box);
					}
					else
					{
						nextAvailableWidth = putGroupOnGrid(sortGroup(box, nextAvailableWidth));
					}
				}
			}

			if (DOWNWARD)
			{
				rowIndex++;
			}
			else
			{
				rowIndex--;
			}
		}

		this.setGrid(centerSharedChildren(this.clone(), DOWNWARD));
		return this;
	}

	/*
		Takes in a row of boxes and puts its parents on the grid, returns the nextAvailableWidth for a box to be placed on the parent level
	*/
	private int putParentsOfRowOnGrid(ArrayList<LinkedBox> rowBoxes)
	{
		final int SIZE = rowBoxes.size();
		if (SIZE > 0)
		{
			int nextParentWidth = rowBoxes.get(0).getWidthLevel();
			// Width level of row
			for (int i = 0; i < SIZE; i++)
			{
				LinkedBox rowBox = rowBoxes.get(i);
				HashSet<LinkedBox> parents = rowBox.getParentBoxes();
				HashSet<LinkedBox> sharers = new HashSet<LinkedBox>();

				// We put parents that are "sharing" children to the right, that way they can be near both their children.
				// This is why they are handled separately below.  We also only deal with parents that aren't already on the grid.
				for (LinkedBox parent : parents)
				{
					if (!grid.values().contains(parent))
					{
						if (parent.getNumChildren() == 1)
						{
							parent.setWidthLevel(nextParentWidth);

							if (parent.getNumSiblings() == 0)
							{
								nextParentWidth = putSoloBoxOnGrid(parent);
							}
							else
							{
								nextParentWidth = putGroupOnGrid(sortGroup(parent, nextParentWidth));
							}
						}
						else
						{
							sharers.add(parent);
						}
					}
				}

				// All sharers have more than 1 child
				for (LinkedBox sharer : sharers)
				{
					if (!grid.values().contains(sharer))
					{
						sharer.setWidthLevel(nextParentWidth);

						if (sharer.getNumSiblings() == 0)
						{
							nextParentWidth = putSoloBoxOnGrid(sharer);
						}
						else
						{
							nextParentWidth = putGroupOnGrid(sortGroup(sharer, nextParentWidth));
						}
					}
				}
			}
			return nextParentWidth;
		}
		else
		{
			return 0;
		}
	}

	// Puts a solo box on grid if not already present, finding a different location if a different box is in the way
	private int putSoloBoxOnGrid(LinkedBox box)
	{
		final Coordinate POSITION = box.getGridPosition();
		final int WIDTH_LEVEL = POSITION.getX();

		if (!grid.values().contains(box))
		{	
			if (this.getBoxAt(POSITION) == null)
			{
				grid.put(POSITION, box);
				return WIDTH_LEVEL + HOR_SPACE;
			}
			else
			{
				box.setWidthLevel(WIDTH_LEVEL + HOR_SPACE);
				return putSoloBoxOnGrid(box);
			}
		}
		else
		{
			return WIDTH_LEVEL;
		}
	}

	// Puts a group of boxes (extended siblings) on grid if not already present, finding different locations if other box(es) are in the way
	// The "sortedGroup" is sorted in ascending widthLevel order.
	private int putGroupOnGrid(ArrayList<LinkedBox> sortedGroup)
	{
		final int GROUP_SIZE = sortedGroup.size();
		final int GROUP_LEFT = sortedGroup.get(0).getWidthLevel();
		final int GROUP_RIGHT = sortedGroup.get(GROUP_SIZE - 1).getWidthLevel();
		final int GROUP_HEIGHT = sortedGroup.get(0).getHeightLevel();
		
		ArrayList<LinkedBox> boxesAtHeightLevel = this.getBoxesAtHeightLevel(GROUP_HEIGHT);
		if (boxesAtHeightLevel.size() != 0)
		{
			final int RIGHT_WIDTH_AT_HEIGHT = boxesAtHeightLevel.get(boxesAtHeightLevel.size() - 1).getWidthLevel();
			if (RIGHT_WIDTH_AT_HEIGHT >= GROUP_LEFT)
			{
				sortedGroup = shiftGroup(sortedGroup, RIGHT_WIDTH_AT_HEIGHT + HOR_SPACE - GROUP_LEFT);
			}	
		}
		
		int nextWidth = Integer.MIN_VALUE;

		for (LinkedBox box : sortedGroup)
		{
			int returnValue = this.putSoloBoxOnGrid(box);
			if (returnValue > nextWidth)
			{
				nextWidth = returnValue;
			}
		}

		return nextWidth;
	}

	// Just to update the grid if we need to internally
	private void setGrid(HashMap<Coordinate, LinkedBox> grid)
	{
		this.grid = grid;
	}
	
	// Centers shared children above/below the parents sharing them, depending on the value of DOWNWARD
	// Actually creates a newGrid with all of the boxes in origGrid moved, and then returns the HashMap of this new grid
	private HashMap<Coordinate, LinkedBox> centerSharedChildren(ArgumentGrid origGrid, final boolean DOWNWARD)
	{
		ArgumentGrid finalGrid = new ArgumentGrid();
		final int MIN_LEVEL = origGrid.calcLowestLevel();
		final int MAX_LEVEL = origGrid.calcHighestLevel();
		final int RANGE = MAX_LEVEL - MIN_LEVEL;
		final int ROOT_LEVEL;
		if (DOWNWARD)
		{
			ROOT_LEVEL = MAX_LEVEL;

		}
		else
		{
			ROOT_LEVEL = MIN_LEVEL;
		}

		for (LinkedBox box : origGrid.getBoxesAtHeightLevel(ROOT_LEVEL))
		{
			finalGrid.putSoloBoxOnGrid(box);
		}

		int currentLevel = ROOT_LEVEL;

		final int LAST_ITER = RANGE - 1;

		// Root level, orig parent level, is already in place; we skip end level because we put the children of levels onto the grid, meaning the last will be covered
		for (int counter = 0; counter < RANGE; counter++)
		{
			ArrayList<LinkedBox> origGridParentLevel = origGrid.getBoxesAtHeightLevel(currentLevel);
			ArrayList<LinkedBox> finalGridParentLevel = finalGrid.getBoxesAtHeightLevel(currentLevel);

			// Gets any boxes that aren't already put on the final grid at the parent level (if the box doesn't have parents, it would have been skipped otherwise)
			if (origGridParentLevel.size() != finalGridParentLevel.size())
			{
				int nextWidth = finalGridParentLevel.get(finalGridParentLevel.size() - 1).getWidthLevel() + HOR_SPACE;
				for (LinkedBox origBox : origGridParentLevel)
				{
					if (!finalGridParentLevel.contains(origBox))
					{
						origBox.setWidthLevel(nextWidth);
						nextWidth = finalGrid.putSoloBoxOnGrid(origBox); 
					}
				}
			}

			final int CHILD_LEVEL;
			if (DOWNWARD)
			{
				CHILD_LEVEL = currentLevel - 1;
				
			}
			else
			{
				CHILD_LEVEL = currentLevel + 1;
			}

			// parent level is set now, so declare as final
			final ArrayList<LinkedBox> PARENT_LEVEL_BOXES = finalGrid.getBoxesAtHeightLevel(currentLevel);
			ArrayList<LinkedBox> boxesAtChildLevel = origGrid.getBoxesAtHeightLevel(CHILD_LEVEL);

			final int PARENT_LEVEL_SIZE = PARENT_LEVEL_BOXES.size();
			final int CHILD_LEVEL_SIZE = boxesAtChildLevel.size();

			// Go through each box in the parent level and puts its children on the finalGrid in a sorted order and position
			for (int i = 0; i < PARENT_LEVEL_SIZE; i++)
			{
				LinkedBox currentBox = PARENT_LEVEL_BOXES.get(i);
				ArrayList<LinkedBox> parentGroup = new ArrayList<LinkedBox>();
				parentGroup.add(currentBox);
				ArrayList<LinkedBox> childGroup = sortAscendingWidth(currentBox.getChildBoxes());
				if (childGroup.size() == 0)
				{
					continue;
				}

				// Creates parent group
				for (int j = i + 1; j < PARENT_LEVEL_SIZE; j++)
				{
					LinkedBox otherParent = PARENT_LEVEL_BOXES.get(j);
					HashSet<LinkedBox> otherChildren = otherParent.getChildBoxes();
					if (otherChildren.containsAll(childGroup))
					{
						parentGroup.add(otherParent);
					}
				}

				final int LEFT_PARENT_WIDTH = parentGroup.get(0).getWidthLevel();
				final int LEFT_CHILD_WIDTH = childGroup.get(0).getWidthLevel();

				int widthLeftChildShouldBe = LEFT_PARENT_WIDTH + parentGroup.size() - childGroup.size();

				// Hack for bug in which siblings in the last row sorted, that don't have parents or children, were not being repositioned
				// Now if we find such boxes, we don't proceed normally and instead use putGroupOnGrid
				boolean proceedNormally = true;
				if (counter == LAST_ITER)
				{
					HashSet<LinkedBox> siblingsWithoutParentsOrChildren = new HashSet<LinkedBox>();
					for (LinkedBox child : childGroup)
					{
						siblingsWithoutParentsOrChildren.addAll(child.getExtendedSiblingsWithoutParentsOrChildren());
					}

					if (siblingsWithoutParentsOrChildren.size() != 0)
					{
						proceedNormally = false;
					}
					 
				}

				int nextWidth = widthLeftChildShouldBe;
				
				if (proceedNormally)
				{
					for (LinkedBox child : childGroup)
					{
						if (!finalGrid.getBoxes().contains(child))
						{
							child.setWidthLevel(nextWidth);
							nextWidth = finalGrid.putSoloBoxOnGrid(child);
						}
					}
				}
				else
				{
					for (LinkedBox child : childGroup)
					{
						if (!finalGrid.getBoxes().contains(child))
						{
							child.setWidthLevel(nextWidth);
							if (child.getNumSiblings() == 0)
							{
								nextWidth = finalGrid.putSoloBoxOnGrid(child);
							}
							else
							{
								nextWidth = finalGrid.putGroupOnGrid(sortGroup(child));
							}
						}
					}

				}		
			}
			if (DOWNWARD)
			{
				currentLevel--;
			}
			else
			{
				currentLevel++;
			}
		}
		return finalGrid.getGrid();
	}

	// Sorts a collection of boxes from lowest to greatest widthLevel, returned as an ArrayList
	private ArrayList<LinkedBox> sortAscendingWidth(final Collection<LinkedBox> origBoxes)
	{
		HashSet<LinkedBox> boxes = new HashSet<LinkedBox>(origBoxes);
		ArrayList<LinkedBox> sortedBoxes = new ArrayList<LinkedBox>();

		final int SIZE = boxes.size();
		for (int i = 0; i < SIZE; i++)
		{
			int minWidth = Integer.MAX_VALUE;
			LinkedBox minBox = null;

			for (LinkedBox box : boxes)
			{
				if (box.getWidthLevel() < minWidth)
				{
					minWidth = box.getWidthLevel();
					minBox = box;
				}
			}
			if (minBox != null)
			{
				boxes.remove(minBox);
				sortedBoxes.add(minBox);
			}
		}

		return sortedBoxes;
	}
	
	/*
	 *	Sorts a group of sibling boxes, returning them from lowestWidthLevel to highest WidthLevel.
	 */
	private ArrayList<LinkedBox> sortGroup(LinkedBox original, final int leftMostWidth)
	{
		final int origHeightLevel = original.getHeightLevel();

		final HashSet<LinkedBox> thisAndExtended = original.getThisAndExtendedSiblings();
		final int rightMostWidth = leftMostWidth + (thisAndExtended.size() - 1) * HOR_SPACE;

		boolean needFirstSibling = true;

		int nextWidth = leftMostWidth + HOR_SPACE;
		for (LinkedBox box : thisAndExtended)
		{
			if (box.getNumSiblings() == 1)
			{
				if (needFirstSibling)
				{
					box.setGridPosition(leftMostWidth, origHeightLevel);
					needFirstSibling = false;
				}
				else
				{
					box.setGridPosition(rightMostWidth, origHeightLevel);
				}
			}
			else
			{
				box.setGridPosition(nextWidth, origHeightLevel);
				nextWidth += HOR_SPACE;
			}
		}

		ArrayList<LinkedBox> ordered = sortAscendingWidth(thisAndExtended);

		return ordered;
	}

	/*
	 *	Sorts a group of siblings boxes. In this overload, the original box of the group will maintain its position, and the other group boxes will go around it.
	 */
	private ArrayList<LinkedBox> sortGroup(LinkedBox original)
	{
		final int origWidthLevel = original.getWidthLevel();
		final int origHeightLevel = original.getHeightLevel();

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

		return sortAscendingWidth(siblingGroup);
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

	/**
	 *	Get the min and max height levels of the passed set of boxes.
	 */
	public IntPair determineMinMaxHeightLevels(Collection<LinkedBox> boxes)
	{
		int minLevel = Integer.MAX_VALUE;
		int maxLevel = Integer.MIN_VALUE;

		for (LinkedBox box : boxes)
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
	 *	Get the min and max width levels of the passed set of boxes.
	 */
	public IntPair determineMinMaxWidthLevels(Collection<LinkedBox> boxes)
	{
		int minLevel = Integer.MAX_VALUE;
		int maxLevel = Integer.MIN_VALUE;

		for (LinkedBox box : boxes)
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
	 *	Gets the boxes on the grid at the passed width level, returning them in no particular order
	 */
	public HashSet<LinkedBox> getBoxesAtWidthLevel(int widthLevel)
	{
		HashSet<LinkedBox> boxesAtWidthLevel = new HashSet<LinkedBox>();
		for (LinkedBox box : grid.values())
		{
			if (box.getWidthLevel() == widthLevel)
			{
				boxesAtWidthLevel.add(box);
			}
		}

		return boxesAtWidthLevel;
	}

	/**
	 *	Gets the boxes on the grid at the passed height level, returning them as an ArrayList from lowest to highest width
	 */
	public ArrayList<LinkedBox> getBoxesAtHeightLevel(int heightLevel)
	{
		HashSet<LinkedBox> boxesAtHeightLevel = new HashSet<LinkedBox>();
		for (LinkedBox box : grid.values())
		{
			if (box.getHeightLevel() == heightLevel)
			{
				boxesAtHeightLevel.add(box);
			}
		}

		ArrayList<LinkedBox> sortedBoxes = new ArrayList<LinkedBox>();

		final int SIZE = boxesAtHeightLevel.size();
		for (int i = 0; i < SIZE; i++)
		{
			int minWidth = Integer.MAX_VALUE;
			LinkedBox minBox = null;

			for (LinkedBox box : boxesAtHeightLevel)
			{
				if (box.getWidthLevel() < minWidth)
				{
					minWidth = box.getWidthLevel();
					minBox = box;
				}
			}
			if (minBox != null)
			{
				boxesAtHeightLevel.remove(minBox);
				sortedBoxes.add(minBox);
			}
		}

		return sortedBoxes;
	}

	/**
	 *	Gets the boxes on the grid at the root level.  If DOWNWARD is true, the rootLevel is top and endLevel is bottom.
	 *	Vice-versa if false.
	 */
	public ArrayList<LinkedBox> getBoxesAtRootLevel(final boolean DOWNWARD)
	{
		IntPair minMaxLevels = determineMinMaxHeightLevels(grid.values());
		if (DOWNWARD)
		{
			return getBoxesAtHeightLevel(minMaxLevels.getMax());
		}
		else
		{
			return getBoxesAtHeightLevel(minMaxLevels.getMin());
		}
	}

	public ArrayList<LinkedBox> getBoxesAtEndLevel(final boolean DOWNWARD)
	{
		IntPair minMaxLevels = determineMinMaxHeightLevels(grid.values());
		if (DOWNWARD)
		{
			return getBoxesAtHeightLevel(minMaxLevels.getMin());
		}
		else
		{
			return getBoxesAtHeightLevel(minMaxLevels.getMax());
		}
	}

	// Shifts a group of boxes rage spaces of width, returning the shifted group as an update
	private ArrayList<LinkedBox> shiftGroup(ArrayList<LinkedBox> group, final int range)
	{
		for (LinkedBox box : group)
		{
			box.setWidthLevel(box.getWidthLevel() + range);
		}

		return group;
	}
	
	// Get the box on the grid at the coordinate location, null if none there
	public LinkedBox getBoxAt(final Coordinate POSITION)
	{
		return grid.get(POSITION);
	}

	public LinkedBox getBoxAt(final int WIDTH, final int HEIGHT)
	{
		return this.getBoxAt(new Coordinate(WIDTH, HEIGHT));
	}

	// Get the passed box if present on the grid, return null if not present
	public LinkedBox findBoxOnGrid(LinkedBox boxToFind)
	{
		LinkedBox returnBox = null;
		for (LinkedBox box : grid.values())
		{
			if (box.equals(boxToFind))
			{
				returnBox = box;
				break;
			}
		}
		return returnBox;
	}

	public ArgumentGrid clone()
	{
		return new ArgumentGrid(this.grid);
	}

	@Override
	public String toString()
	{
		IntPair heightData = determineMinMaxHeightLevels(grid.values());
		int minHeight = heightData.getMin();
		int maxHeight = heightData.getMax();

		IntPair widthData = determineMinMaxWidthLevels(grid.values());
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

		buffer.append("\t------");

		for (int currentColumn = minWidth; currentColumn <= maxWidth; currentColumn++)
		{
			buffer.append("------");
		}

		buffer.append("\n\t\t");

		for (int currentColumn = minWidth; currentColumn <= maxWidth; currentColumn++)
		{
			buffer.append(currentColumn + "\t");
		}
		return buffer.toString();
	}
}