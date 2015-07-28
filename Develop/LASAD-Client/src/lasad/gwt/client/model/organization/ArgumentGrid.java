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
 *	@since 20 July 2015, Last Updated 21 July 2015	
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

	// Children go below
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

	// Children go above
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

	// Sorts the starting row of the grid and puts it on the grid
	private void sortAndPutStartRow(Collection<LinkedBox> startRow)
	{
		HashSet<LinkedBox> visited = new HashSet<LinkedBox>();
		int widthLevel = 0;
		for (LinkedBox box : startRow)
		{
			if (!visited.contains(box))
			{
				box.setWidthLevel(widthLevel);

				// Returns next available widthLevel to right
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
	 *	The method that actually organizes the grid and thus is key to auto organization
	 */
	public ArgumentGrid organize(final boolean TOP_TO_BOTTOM, Collection<LinkedBox> boxesToPutOnGrid)
	{
		// Remember to clear the grid before organization, since boxes must be reaccumulated
		this.empty();

		if (boxesToPutOnGrid == null || boxesToPutOnGrid.size() == 0)
		{
			return this;
		}

		int startRowNum = Integer.MAX_VALUE;	

		if (TOP_TO_BOTTOM)
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

		if (startRowNum == Integer.MAX_VALUE)
		{
			Logger.log("Error in assignment of startRowNum", Logger.DEBUG);
			return this;
		}

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

		Logger.log("maxRow: " + maxRow + "min: " + minRow, Logger.DEBUG);
		final int RANGE = maxRow - minRow;
		int rowIndex = startRowNum;

		// Adds all the rest of the rows to the grid, based from the startLevel
		for (int counter = 0; counter < RANGE; counter++)
		{
			Logger.log("entered loop", Logger.DEBUG);
			ArrayList<LinkedBox> rowBoxes = this.getBoxesAtHeightLevel(rowIndex);

			int nextAvailableWidth = putParentsOfRowOnGrid(rowBoxes);

			final int PARENT_HEIGHT;
			if (TOP_TO_BOTTOM)
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

			// Handles boxes that didnt have children in rowBoxes
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

			if (TOP_TO_BOTTOM)
			{
				rowIndex++;
			}
			else
			{
				rowIndex--;
			}
		}

		this.setGrid(centerSharedChildren(this.clone(), TOP_TO_BOTTOM));
		return this;
	}

	private int putParentsOfRowOnGrid(ArrayList<LinkedBox> rowBoxes)
	{
		Logger.log("Entered put Parents of Row on grid", Logger.DEBUG);
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

	// Puts a solo box on grid, making room if necessary
	private int putSoloBoxOnGrid(LinkedBox box)
	{
		final Coordinate POSITION = box.getGridPosition();
		final int WIDTH_LEVEL = POSITION.getX();

		if (!grid.values().contains(box))
		{	
			if (this.getBoxAt(POSITION) == null)
			{
				grid.put(POSITION, box);
				Logger.log("Put box " + box.getRootID() + " at " + box.getGridPosition().toString(), Logger.DEBUG);
				Logger.log(this.toString(), Logger.DEBUG);
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

	private void setGrid(HashMap<Coordinate, LinkedBox> grid)
	{
		this.grid = grid;
	}
	
	// Centers shared children above/below the parents sharing them
	private HashMap<Coordinate, LinkedBox> centerSharedChildren(ArgumentGrid origGrid, final boolean TOP_TO_BOTTOM)
	{
		Logger.log("Entered centerSharedChildren", Logger.DEBUG);
		ArgumentGrid finalGrid = new ArgumentGrid();
		final int MIN_LEVEL = origGrid.calcLowestLevel();
		final int MAX_LEVEL = origGrid.calcHighestLevel();
		final int RANGE = MAX_LEVEL - MIN_LEVEL;
		final int ROOT_LEVEL;
		if (TOP_TO_BOTTOM)
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

		HashSet<LinkedBox> visited = new HashSet<LinkedBox>();

		// Root level, orig parent level, is already in place; we skip end level because we deal with parent levels
		for (int counter = 0; counter < RANGE; counter++)
		{
			ArrayList<LinkedBox> origGridParentLevel = origGrid.getBoxesAtHeightLevel(currentLevel);
			ArrayList<LinkedBox> finalGridParentLevel = finalGrid.getBoxesAtHeightLevel(currentLevel);

			// Gets any boxes that aren't already put on the final grid at the parent level (like if the box doesn't have parents)
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
			if (TOP_TO_BOTTOM)
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

			for (int i = 0; i < PARENT_LEVEL_SIZE; i++)
			{
				LinkedBox currentBox = PARENT_LEVEL_BOXES.get(i);
				if (!visited.contains(currentBox))
				{
					ArrayList<LinkedBox> parentGroup = new ArrayList<LinkedBox>();
					parentGroup.add(currentBox);
					HashSet<LinkedBox> childGroup = currentBox.getChildBoxes();

					// Creates parent group
					for (int j = i + 1; j < PARENT_LEVEL_SIZE; j++)
					{
						LinkedBox otherParent = PARENT_LEVEL_BOXES.get(j);
						HashSet<LinkedBox> otherChildren = otherParent.getChildBoxes();
						if (otherChildren.size() == childGroup.size() && otherChildren.containsAll(childGroup))
						{
							parentGroup.add(otherParent);
						}
					}

					visited.addAll(parentGroup);
					if (childGroup.size() == 0)
					{
						continue;
					}

					final int LEFT_PARENT_WIDTH = findLeftMostWidthOfGroup(parentGroup);
					final int LEFT_CHILD_WIDTH = findLeftMostWidthOfGroup(childGroup);

					int widthLeftChildShouldBe = LEFT_PARENT_WIDTH + parentGroup.size() - childGroup.size();
					ArrayList<LinkedBox> boxesOnGridAtChildLevel = finalGrid.getBoxesAtHeightLevel(CHILD_LEVEL);
					if (boxesOnGridAtChildLevel.size() > 0)
					{
						int rightMostBoxWidth = boxesOnGridAtChildLevel.get(boxesOnGridAtChildLevel.size() - 1).getWidthLevel();
						if (rightMostBoxWidth >= widthLeftChildShouldBe)
						{
							widthLeftChildShouldBe = rightMostBoxWidth + HOR_SPACE;
						}
					}
						

					final int NUM_LEVELS_MOVE = widthLeftChildShouldBe - LEFT_CHILD_WIDTH;
					for (LinkedBox child : childGroup)
					{
						child.setWidthLevel(child.getWidthLevel() + NUM_LEVELS_MOVE);
						finalGrid.putSoloBoxOnGrid(child);
					}
				}		
			}
			if (TOP_TO_BOTTOM)
			{
				currentLevel--;
			}
			else
			{
				currentLevel++;
			}
		}
		if (finalGrid.getBoxes().size() != origGrid.getBoxes().size())
		{
			Logger.log("We forgot box(es)", Logger.DEBUG);
			Logger.log("FG size: " + finalGrid.getBoxes().size(), Logger.DEBUG);
			Logger.log("OG size: " + origGrid.getBoxes().size(), Logger.DEBUG);
		}
		return finalGrid.getGrid();
	}

	private int findLeftMostWidthOfGroup(Collection<LinkedBox> boxes)
	{
		int leftMostWidth = Integer.MAX_VALUE;
		for (LinkedBox box : boxes)
		{
			final int widthLevel = box.getWidthLevel();
			if (widthLevel < leftMostWidth)
			{
				leftMostWidth = widthLevel;
			}
		}

		return leftMostWidth;
	}
	
	/*
	 *	Sorts a group, preserving the order of the boxes in a logical sense (i.e. the siblings in the group with only 1 direct sibling will go
	 *	on the ends of the group).
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

		ArrayList<LinkedBox> ordered = new ArrayList<LinkedBox>();
		final int SIZE = thisAndExtended.size();

		for (int i = 0; i < SIZE; i++)
		{
			int minWidth = Integer.MAX_VALUE;
			LinkedBox minBox = null;
			for (LinkedBox box : thisAndExtended)
			{
				if (box.getWidthLevel() < minWidth)
				{
					minWidth = box.getWidthLevel();
					minBox = box;
				}
			}
			if (minBox != null)
			{
				ordered.add(minBox);
				thisAndExtended.remove(minBox);
			}
		}
		if (ordered.size() != SIZE)
		{
			Logger.log("Houston we have a problem", Logger.DEBUG);
		}
		return ordered;
	}

	/**
	 *	Get the min and max height levels of the grid.
	 */
	public IntPair determineMinMaxHeightLevels()
	{
		int minLevel = Integer.MAX_VALUE;
		int maxLevel = Integer.MIN_VALUE;

		for (LinkedBox box : grid.values())
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
	 *	Get the min and max width levels of the grid.
	 */
	public IntPair determineMinMaxWidthLevels()
	{
		int minLevel = Integer.MAX_VALUE;
		int maxLevel = Integer.MIN_VALUE;

		for (LinkedBox box : grid.values())
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
	 *	Gets the boxes on the grid at the passed width level
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
	 *	Gets the boxes on the grid at the passed height level
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

		final int SIZE = boxesAtHeightLevel.size();

		ArrayList<LinkedBox> orderedRow = new ArrayList<LinkedBox>();
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
				orderedRow.add(minBox);
			}
		}

		return orderedRow;
	}

	/**
	 *	Gets the boxes on the grid at the root level.  If isOrganizeTopToBottom is true, the rootLevel is top and endLevel is bottom.
	 *	Vice-versa if false.
	 */
	public ArrayList<LinkedBox> getBoxesAtRootLevel(final boolean isOrganizeTopToBottom)
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

	public ArrayList<LinkedBox> getBoxesAtEndLevel(final boolean isOrganizeTopToBottom)
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

	private ArrayList<LinkedBox> shiftGroup(ArrayList<LinkedBox> group, final int range)
	{
		for (LinkedBox box : group)
		{
			box.setWidthLevel(box.getWidthLevel() + range);
		}

		return group;
	}
	
	// get the box on the grid at the coordinate location, null if none there
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

		buffer.append("\t-----");

		for (int currentColumn = minWidth; currentColumn <= maxWidth; currentColumn++)
		{
			buffer.append("-----");
		}

		buffer.append("\n\t\t");

		for (int currentColumn = minWidth; currentColumn <= maxWidth; currentColumn++)
		{
			buffer.append(currentColumn + "\t");
		}
		return buffer.toString();
	}
}