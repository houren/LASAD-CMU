package lasad.gwt.client.model.organization;

import java.util.Arrays;
import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;

import lasad.gwt.client.model.organization.LinkedBox;
import lasad.gwt.client.logger.Logger;
import lasad.gwt.client.model.organization.Coordinate;
import lasad.gwt.client.model.organization.IntPair;
import lasad.gwt.client.ui.workspace.LASADInfo;

/**
 *	Provides a chessboard like organization of each argument thread, with specific coordinate positions translated from this model by AutoOrganizer.
 *	The grid contains boxes only while organizeMap from AutoOrganizer is running.  Otherwise, it is cleared to release memory.
 *	@author Kevin Loughlin
 *	@since 20 July 2015, Last Updated 4 August 2015	
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
		Recursively sets height levels, assuming upward orientation (if downward orientation is being used, we will flip at the end of organization)
	*/
	private HashSet<LinkedBox> recursivelySetHeightLevels(LinkedBox box, final int HEIGHT_LEVEL, HashSet<LinkedBox> visited)
	{
		if (!visited.contains(box))
		{
			box.setHeightLevel(HEIGHT_LEVEL);
			visited.add(box);

			for (LinkedBox sibling : box.getSiblingBoxes())
			{
				visited = recursivelySetHeightLevels(sibling, HEIGHT_LEVEL, visited);
			}

			for (LinkedBox child : box.getChildBoxes())
			{
				visited = recursivelySetHeightLevels(child, HEIGHT_LEVEL + VERT_SPACE, visited);
			}
			
			for (LinkedBox parent : box.getParentBoxes())
			{
				visited = recursivelySetHeightLevels(parent, HEIGHT_LEVEL - VERT_SPACE, visited);
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

	/*
		Sorts the "starting" row of the grid (which for our upward orientation assumption is the top row) and puts this row on the grid.
		This base row then allows us to build the other rows from it.  Boxes will be placed into groups, with groups with higher average
		number of parents going to the middle and ones with fewer parents going to the edges of the row.
	*/
	private void sortAndPutStartRow(Collection<LinkedBox> startRow)
	{
		Logger.log("[lasad.gwt.client.model.organization.ArgumentGrid] Adding start row to grid...", Logger.DEBUG);
		final int SIZE = startRow.size();
		HashSet<LinkedBox> visited = new HashSet<LinkedBox>();

		// Even single boxes will be treated as "groups" of 1 box, so that we can treat groups/solo boxes in the same manner for simplicity
		ArrayList<ArrayList<LinkedBox>> boxesAsGroups = new ArrayList<ArrayList<LinkedBox>>();

		// Build each group
		for (LinkedBox box : startRow)
		{
			// If the box has been visited, it already belongs to a group
			if (!visited.contains(box))
			{
				ArrayList<LinkedBox> toAdd = new ArrayList<LinkedBox>();
				if (box.getNumSiblings() == 0)
				{
					visited.add(box);
					toAdd.add(box);
				}
				else
				{
					ArrayList<LinkedBox> sortedGroup = this.sortGroup(box, 0);
					visited.addAll(sortedGroup);
					toAdd.addAll(sortedGroup);
				}
				boxesAsGroups.add(toAdd);
			}
		}

		// The key is the arraylist of the organized group in ascending width order.  the value is the average number of parents for each box in the group
		HashMap<ArrayList<LinkedBox>, Integer> groupWithAverageParents = new HashMap<ArrayList<LinkedBox>, Integer>();

		// Calculate the average number of parents for each group and put it in the hashmap
		for (ArrayList<LinkedBox> group : boxesAsGroups)
		{
			final int GROUP_SIZE = group.size();
			if (GROUP_SIZE != 0)
			{
				double numParents = 0.0;
				for (LinkedBox box : group)
				{
					numParents += box.getNumParents();
				}

				groupWithAverageParents.put(group, (int) Math.round(numParents / GROUP_SIZE));
			}
		}

		// Put each box into an array, with boxes from groups with fewer parents going to the edges of the array and boxes with lots of parents going to the middle of the array
		LinkedBox[] orderedArray = new LinkedBox[SIZE];
		final int ITERATIONS = groupWithAverageParents.keySet().size();

		// This boolean allows us to alternate between placing the boxes at the beginning versus the end of remaining indexes in the array
		boolean placeAtBeginning = true;

		// The start and end indexes at which to place boxes.  Start will icnrease with occupation of slots, end will decrease.
		int nextStartIndex = 0;
		int nextEndIndex = SIZE - 1;

		// Go through the untouched boxes of the group and find the group with the fewest parents average.  Put it in the array.
		for (int i = 0; i < ITERATIONS; i++)
		{
			int minParents = Integer.MAX_VALUE;
			ArrayList<LinkedBox> toRemove = null;
			for (ArrayList<LinkedBox> group : groupWithAverageParents.keySet())
			{

				if (groupWithAverageParents.get(group) < minParents)
				{
					toRemove = group;
					minParents = groupWithAverageParents.get(group);
				}
			}

			// Place the group and then remove it for next iteration
			if (toRemove != null)
			{
				if (placeAtBeginning)
				{
					for (LinkedBox box : toRemove)
					{
						orderedArray[nextStartIndex] = box;
						nextStartIndex++;
					}
					placeAtBeginning = false;
				}
				else
				{
					for (int j = toRemove.size() - 1; j > -1; j--)
					{
						orderedArray[nextEndIndex] = toRemove.get(j);
						nextEndIndex--;
					}
					placeAtBeginning = true;
				}
				groupWithAverageParents.remove(toRemove);
			}
		}

		// IMPORTANT: Clear visited!
		visited.clear();

		// Starting width level (arbitrary).  Will update as boxes are placed on the grid
		int widthLevel = 0;

		// Put the array on the grid
		for (LinkedBox box : orderedArray)
		{
			if (!visited.contains(box))
			{
				box.setWidthLevel(widthLevel);

				ArrayList<LinkedBox> sortedGroup = this.sortGroup(box, widthLevel);
				visited.addAll(sortedGroup);
				widthLevel = putGroupOnGrid(sortedGroup);

				/*
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
				*/
			}		
		}
	}

	private void fixCounterOrientedLinks(Collection<LinkedBox> boxesToPutOnGrid)
	{
		for(LinkedBox box : boxesToPutOnGrid)
		{
			for(LinkedBox child : box.getChildBoxes())
			{
				if(box.getHeightLevel() >= child.getHeightLevel())
				{
					final int NEW_HEIGHT = child.getHeightLevel() - 1;
					box.setHeightLevel(NEW_HEIGHT);
					for (LinkedBox kid : box.getChildBoxes())
					{
						if(kid.getHeightLevel() == NEW_HEIGHT + 1)
						{
							continue;
						}

						boolean allParentsCorrect = true;
						for (LinkedBox parent : kid.getParentBoxes())
						{
							if (parent.getHeightLevel() != NEW_HEIGHT)
							{
								allParentsCorrect = false;
								break;
							}
						}
						if (allParentsCorrect)
						{
							kid.setHeightLevel(NEW_HEIGHT + 1);
						}
					}
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
		Logger.log("[lasad.gwt.client.model.organization.ArgumentGrid] Organizing grid...", Logger.DEBUG);
		// Remember to clear the grid before organization, since boxes must be reaccumulated
		this.empty();

		// If nothing is there just return
		if (boxesToPutOnGrid == null || boxesToPutOnGrid.size() == 0)
		{
			return this;
		}

		final int ORIG_SIZE = boxesToPutOnGrid.size();

		LinkedBox startBox = null;
		if (DOWNWARD)
		{
			for (LinkedBox box : boxesToPutOnGrid)
			{
				if (startBox == null)
				{
					startBox = box;
				}
				else if (box.getYTop() < startBox.getYTop())
				{
					startBox = box;
				}
				else if (Math.abs(box.getYTop() - startBox.getYTop()) < 0.1)
				{
					if (box.getXLeft() < startBox.getXLeft())
					{
						startBox = box;
					}
				}
			}
		}
		else
		{
			for (LinkedBox box : boxesToPutOnGrid)
			{
				if (startBox == null)
				{
					startBox = box;
				}
				else if (box.getYTop() > startBox.getYTop())
				{
					startBox = box;
				}
				else if (Math.abs(box.getYTop() - startBox.getYTop()) < 0.1)
				{
					if (box.getXLeft() < startBox.getXLeft())
					{
						startBox = box;
					}
				}
			}
		}	

		// Set the height levels of the boxes from bottom to top, with lowest height level as 0
		if (startBox != null)
		{
			boxesToPutOnGrid = recursivelySetHeightLevels(startBox, 0, new HashSet<LinkedBox>());
		}
		else
		{
			Logger.log("Failed to organize, startBox is null", Logger.DEBUG);
			return this;
		}
			

		fixCounterOrientedLinks(boxesToPutOnGrid);

		// Sets the lowest level to 0
		int origLowestRow = determineMinMaxHeightLevels(boxesToPutOnGrid).getMin();
		for (LinkedBox box : boxesToPutOnGrid)
		{
			box.setHeightLevel(box.getHeightLevel() - origLowestRow);
		}

		IntPair minMaxHeightLevels = determineMinMaxHeightLevels(boxesToPutOnGrid);
		final int MIN_ROW = minMaxHeightLevels.getMin();
		final int MAX_ROW = minMaxHeightLevels.getMax();

		// Gather the maxRow boxes
		HashSet<LinkedBox> maxRowBoxes = new HashSet<LinkedBox>();
		for (LinkedBox box : boxesToPutOnGrid)
		{
			final int HEIGHT = box.getHeightLevel();
			if (HEIGHT == MAX_ROW)
			{
				maxRowBoxes.add(box);
			}
		}

		// Adds start row to grid
		this.sortAndPutStartRow(maxRowBoxes);

		// Adds all the rest of the rows to the grid, based from the startLevel.  Essentially we take an already present row and add its children.
		for (int rowIndex = MAX_ROW; rowIndex > MIN_ROW; rowIndex--)
		{
			ArrayList<LinkedBox> rowBoxes = this.getBoxesAtHeightLevel(rowIndex);

			int nextAvailableWidth = putParentsOfRowOnGrid(rowBoxes);

			final int PARENT_HEIGHT = rowIndex - 1;

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

					/*
					if (box.getNumSiblings() == 0)
					{
						nextAvailableWidth = putSoloBoxOnGrid(box);
					}
					*/
					nextAvailableWidth = putGroupOnGrid(sortGroup(box, nextAvailableWidth));
				}
			}
		}

		// Aligns the children above their parents
		this.setGrid(alignBoxes(this.clone()));

		// Flip box height levels if downward orientation
		if (DOWNWARD)
		{
			for (LinkedBox box : this.getGrid().values())
			{
				box.setHeightLevel(-1 * box.getHeightLevel());
			}
		}
		return this;
	}

	/*
		Takes in a row of boxes and puts its parents on the grid, returns the nextAvailableWidth for a box to be placed on the parent level
	*/
	private int putParentsOfRowOnGrid(ArrayList<LinkedBox> rowBoxes)
	{
		Logger.log("[lasad.gwt.client.model.organization.ArgumentGrid] Putting successive height levels on grid...", Logger.DEBUG);
		final int SIZE = rowBoxes.size();
		if (SIZE > 0)
		{
			final int HEIGHT_BELOW = rowBoxes.get(0).getHeightLevel() - 1;
			int nextParentWidth = rowBoxes.get(0).getWidthLevel();
			// Width level of row
			for (int i = 0; i < SIZE; i++)
			{
				LinkedBox rowBox = rowBoxes.get(i);

				HashSet<LinkedBox> parents = new HashSet(rowBox.getParentBoxes());
				HashSet<LinkedBox> toRemove = new HashSet<LinkedBox>();
				for (LinkedBox parent : parents)
				{
					if (parent.getHeightLevel() != HEIGHT_BELOW)
					{
						toRemove.add(parent);
					}
				}

				parents.removeAll(toRemove);

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

							/*
							if (parent.getNumSiblings() == 0)
							{
								nextParentWidth = putSoloBoxOnGrid(parent);
							}
							else
							{
								*/
								nextParentWidth = putGroupOnGrid(sortGroup(parent, nextParentWidth));
							//}
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

						/*
						if (sharer.getNumSiblings() == 0)
						{
							nextParentWidth = putSoloBoxOnGrid(sharer);
						}
						else
						{*/
							nextParentWidth = putGroupOnGrid(sortGroup(sharer, nextParentWidth));
						//}
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
		final int HEIGHT_LEVEL = POSITION.getY();

		if (!grid.values().contains(box))
		{	
			if (this.getBoxAt(POSITION) == null)
			{
				if (this.getBoxAt(WIDTH_LEVEL - 1, HEIGHT_LEVEL) == null)
				{
					if (this.getBoxAt(WIDTH_LEVEL + 1, HEIGHT_LEVEL) == null)
					{
						grid.put(POSITION, box);
						Logger.log("Put box " + box.getRootID(), Logger.DEBUG);
						Logger.log(this.toString(), Logger.DEBUG);
						return WIDTH_LEVEL + HOR_SPACE;
					}
					else
					{
						box.setWidthLevel(HOR_SPACE + HOR_SPACE / 2 + WIDTH_LEVEL);
						return putSoloBoxOnGrid(box);
					}
				}
				else
				{
					box.setWidthLevel(HOR_SPACE / 2 + WIDTH_LEVEL);
					return putSoloBoxOnGrid(box);
				}
				
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
		if (GROUP_SIZE == 1)
		{
			for (LinkedBox solo : sortedGroup)
			{
				return putSoloBoxOnGrid(solo);
			}
		}
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

	private void removeBoxes(Collection<LinkedBox> boxes)
	{
		for (LinkedBox box : boxes)
		{
			LinkedBox removed = this.grid.remove(box.getGridPosition());
			Logger.log("removed box " + removed.getRootID(), Logger.DEBUG);
			Logger.log(this.toString(), Logger.DEBUG);
		}	
	}

	private HashMap<Coordinate, LinkedBox> alignBoxes(ArgumentGrid origGrid)
	{
		final int MIN_LEVEL = origGrid.calcLowestLevel();
		final int MAX_LEVEL = origGrid.calcHighestLevel();
		if (MIN_LEVEL == MAX_LEVEL)
		{
			return origGrid.getGrid();
		}

		ArgumentGrid finalGrid = new ArgumentGrid();

		for (int parentLevel = MIN_LEVEL; parentLevel < MAX_LEVEL; parentLevel++)
		{
			final int PARENT_LEVEL = parentLevel;
			final int CHILD_LEVEL = PARENT_LEVEL + 1;
			ArrayList<LinkedBox> allParentLevelBoxes = origGrid.getBoxesAtHeightLevel(PARENT_LEVEL);
			ArrayList<LinkedBox> parentBoxesAlreadyOnGrid = finalGrid.getBoxesAtHeightLevel(PARENT_LEVEL);
			ArrayList<LinkedBox> allChildLevelBoxes = origGrid.getBoxesAtHeightLevel(CHILD_LEVEL);
			int nextParentWidth;
			if (parentBoxesAlreadyOnGrid.size() == 0)
			{
				nextParentWidth = 0;
			}
			else
			{
				nextParentWidth = parentBoxesAlreadyOnGrid.get(parentBoxesAlreadyOnGrid.size() - 1).getWidthLevel() + HOR_SPACE;
			}

			final int PARENT_LEVEL_SIZE = allParentLevelBoxes.size();

			HashSet<LinkedBox> verified = new HashSet<LinkedBox>();

			for (int i = 0; i < PARENT_LEVEL_SIZE; i++)
			{
				LinkedBox parentLevelBox = allParentLevelBoxes.get(i);
				
				ArrayList<LinkedBox> parentGroup = new ArrayList<LinkedBox>();
				parentGroup.add(parentLevelBox);
				ArrayList<LinkedBox> childGroup = sortAscendingNumParents(parentLevelBox.getChildBoxesOnNextLevel());

				if (childGroup.size() == 0)
				{
					continue;
				}

				// Creates parent group, i.e. the parents sharing the child group
				for (int j = i + 1; j < PARENT_LEVEL_SIZE; j++)
				{
					LinkedBox otherParent = allParentLevelBoxes.get(j);
					HashSet<LinkedBox> otherChildren = otherParent.getChildBoxesOnNextLevel();
					if (otherChildren.containsAll(childGroup))
					{
						parentGroup.add(otherParent);
					}
				}

				for (LinkedBox parent : parentGroup)
				{
					if (!finalGrid.getGrid().values().contains(parent))
					{
						parent.setWidthLevel(nextParentWidth);
						nextParentWidth = finalGrid.putGroupOnGrid(sortGroup(parent, nextParentWidth));
					}
				}

				int nextChildWidth = calcAverageWidthLevel(parentGroup) - (childGroup.size() - 1);
				LinkedBox boxOnSpot = finalGrid.getBoxAt(nextChildWidth, CHILD_LEVEL);
				LinkedBox boxToLeft = finalGrid.getBoxAt(nextChildWidth - 1, CHILD_LEVEL);
				if ((boxOnSpot != null && !childGroup.contains(boxOnSpot)) || (boxToLeft != null && !childGroup.contains(boxToLeft)))
				{
					ArrayList<LinkedBox> childrenAlreadyOnGrid = finalGrid.getBoxesAtHeightLevel(CHILD_LEVEL);
					boolean foundMatch = false;
					for (LinkedBox child : childrenAlreadyOnGrid)
					{
						if (childGroup.contains(child))
						{
							nextChildWidth = child.getWidthLevel();
							foundMatch = true;
							break;
						}
					}
					if (!foundMatch)
					{
						nextChildWidth = childrenAlreadyOnGrid.get(childrenAlreadyOnGrid.size() - 1).getWidthLevel() + HOR_SPACE;
					}
					nextParentWidth = nextChildWidth;
					for (LinkedBox parent : parentGroup)
					{
						finalGrid.removeBoxes(parent.getThisAndExtendedSiblings());
					}
					for (LinkedBox parent : parentGroup)
					{
						parent.setWidthLevel(nextParentWidth);
						nextParentWidth = finalGrid.putGroupOnGrid(sortGroup(parent, nextParentWidth));
					}
				}
				
				for (LinkedBox child : childGroup)
				{
					if (!finalGrid.getGrid().values().contains(child))
					{
						child.setWidthLevel(nextChildWidth);
						nextChildWidth = finalGrid.putGroupOnGrid(sortGroup(child, nextChildWidth));
					}	
				}

				if (parentLevel == MIN_LEVEL)
				{
					int nextVerifiedParentWidth = calcAverageWidthLevel(childGroup) - (parentGroup.size() - 1);
					if (parentGroup.get(0).getWidthLevel() != nextVerifiedParentWidth)
					{
						for (LinkedBox parent : parentGroup)
						{
							if (!verified.contains(parent))
							{
								finalGrid.removeBoxes(parent.getThisAndExtendedSiblings());	
							}
						}

						for (LinkedBox parent : parentGroup)
						{
							if (!verified.contains(parent))
							{
								parent.setWidthLevel(nextVerifiedParentWidth);
								nextVerifiedParentWidth = finalGrid.putGroupOnGrid(sortGroup(parent, nextVerifiedParentWidth));
								verified.add(parent);
							}
						}
					}
					else
					{
						verified.addAll(parentGroup);
					}
				}	
			}
		}
		return finalGrid.getGrid();
	}

	private int calcAverageWidthLevel(final Collection<LinkedBox> boxes)
	{
		double sum = 0.0;
		final int SIZE = boxes.size();
		if (SIZE != 0)
		{
			for (LinkedBox box : boxes)
			{
				sum += box.getWidthLevel();
			}
			return (int) Math.round(sum / SIZE);
		}
		else
		{
			Logger.log("Passed empty group to calcAverageWidthLevel", Logger.DEBUG);
			return 0;
		}
	}

	private ArrayList<LinkedBox> sortAscendingNumParents(final Collection<LinkedBox> group)
	{
		HashSet<LinkedBox> unsortedChildGroup = new HashSet<LinkedBox>(group);
		ArrayList<LinkedBox> sortedChildGroup = new ArrayList<LinkedBox>();

		final int SIZE = group.size();
		for (int i = 0; i < SIZE; i++)
		{
			int minNumParents = Integer.MAX_VALUE;
			LinkedBox minBox = null;

			for (LinkedBox box : unsortedChildGroup)
			{
				if (box.getNumParents() < minNumParents)
				{
					minNumParents = box.getNumParents();
					minBox = box;
				}
			}
			if (minBox != null)
			{
				unsortedChildGroup.remove(minBox);
				sortedChildGroup.add(minBox);
			}
		}

		return sortedChildGroup;
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
		if (thisAndExtended.size() == 1)
		{
			ArrayList<LinkedBox> singleBoxAsGroup = new ArrayList<LinkedBox>();
			singleBoxAsGroup.add(original);
			return singleBoxAsGroup;
		}
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

		HashSet<LinkedBox> siblings = original.getSiblingBoxes();
		if (siblings.size() == 0)
		{
			ArrayList<LinkedBox> singleBoxAsGroup = new ArrayList<LinkedBox>();
			singleBoxAsGroup.add(original);
			return singleBoxAsGroup;
		}

		for (LinkedBox sibling : siblings)
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
	public static IntPair determineMinMaxHeightLevels(Collection<LinkedBox> boxes)
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
	public static IntPair determineMinMaxWidthLevels(Collection<LinkedBox> boxes)
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