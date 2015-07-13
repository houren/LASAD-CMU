package lasad.gwt.client.model.organization;

import lasad.gwt.client.model.organization.ArgumentThread;
import lasad.gwt.client.model.organization.Coordinate;
import lasad.gwt.client.model.organization.LinkedBox;
import lasad.gwt.client.model.organization.VisitedWithMaxAndMin;
import lasad.gwt.client.model.organization.VisitedWithInteger;

import java.util.HashSet;
import java.util.HashMap;

import lasad.gwt.client.logger.Logger;

public class ThreadGrid
{
	private final int SPACE = 2;

	private HashMap<Coordinate, LinkedBox> boxLocations;
	private HashSet<LinkedBox> boxes;

	private boolean isOrganizeTopToBottom = false;

	private int rootLevel = 0;

	public ThreadGrid()
	{
		boxLocations = new HashMap<Coordinate, LinkedBox>();
		boxes = new HashSet<LinkedBox>();
	}

	public ThreadGrid(ArgumentThread thread)
	{
		this();
		this.boxes.addAll(thread.getBoxes());
	}

	public void addBox(LinkedBox box)
	{
		this.boxes.add(box);
	}

	public void removeBox(LinkedBox box)
	{
		this.boxes.remove(box);
	}

	public LinkedBox getBoxAtLocation(Coordinate location)
	{
		return boxLocations.get(location);
	}

	public int getMaxX()
	{
		Logger.log("Entered getMaxX", Logger.DEBUG);
		int maxX = Integer.MIN_VALUE;

		for (Coordinate coordinate : boxLocations.keySet())
		{
			if (coordinate.getX() > maxX)
			{
				maxX = coordinate.getX();
			}
		}
		Logger.log("Exiting getMaxX", Logger.DEBUG);
		return maxX;
	}

	public int getMaxY()
	{
		Logger.log("Entered getMaxY", Logger.DEBUG);
		int maxY = Integer.MIN_VALUE;

		for (Coordinate coordinate : boxLocations.keySet())
		{
			if (coordinate.getY() > maxY)
			{
				maxY = coordinate.getY();
			}
		}
		Logger.log("Exiting getMaxY", Logger.DEBUG);
		return maxY;
	}

	public HashSet<LinkedBox> getBoxes()
	{
		return boxes;
	}


	public void putBoxOnGrid(LinkedBox boxToPut)
	{
		Logger.log("Entered putBoxOnGrid", Logger.DEBUG);
		LinkedBox boxAlreadyThere = this.getBoxAtLocation(boxToPut.getGridPosition());

		if (boxAlreadyThere != null)
		{
			makeRoomForBox(boxToPut);

		}
		else
		{
			boxLocations.put(boxToPut.getGridPosition(), boxToPut);
		}
		Logger.log("Exiting putBoxOnGrid", Logger.DEBUG);
	}

	public void putBoxesOnGrid(HashSet<LinkedBox> boxes)
	{
		for (LinkedBox box : this.boxes)
		{
			putBoxOnGrid(box);
		}
	}

	private void makeRoomForBox(LinkedBox boxToPut)
	{
		Logger.log("Entered makeRoomForBox", Logger.DEBUG);
		for (LinkedBox box : this.boxes)
		{
			if (box.getWidthLevel() >= boxToPut.getWidthLevel())
			{
				box.setWidthLevel(box.getWidthLevel() + 1);

				LinkedBox removed = boxLocations.remove((box.getGridPosition()));
				if (removed != null)
				{
					putBoxOnGrid(box);
				}
			}
		}
		Logger.log("Exiting makeRoomForBox", Logger.DEBUG);
	}

	public void organizeGrid()
	{
		Logger.log("Entered organizeGrid", Logger.DEBUG);
		HashSet<LinkedBox> boxes = this.boxes;

		if (boxes.size() == 0)
		{
			return;
		}

		VisitedWithMaxAndMin returnData = null;

		// I just want the first box
		for (LinkedBox box : boxes)
		{
			returnData = determineRawHeightLevels(box, 0, new VisitedWithMaxAndMin());
			Logger.log("made final return from determineRawHeightLevels", Logger.DEBUG);
			break;
		}
		
		if (returnData == null)
		{
			Logger.log("returnData is null", Logger.DEBUG);
			return;
		}
		else
		{
			Logger.log("returnData is NOT null", Logger.DEBUG);	
		}

		int minLevel = returnData.getMin();

		Logger.log("got Min", Logger.DEBUG);

		// where integer is the level of the box
		HashMap<Integer, HashSet<LinkedBox>> boxesByLevel = new HashMap<Integer, HashSet<LinkedBox>>();

		// This will set the lowest boxes to a height level of 0 and highest to a height level of returnData.getMax()
		// Also organizes the height level group
		Logger.log("Entering loop for assigning heights", Logger.DEBUG);
		for (LinkedBox box : boxes)
		{
			box.setHeightLevel(box.getHeightLevel() - minLevel);

			Integer heightLevelObj = new Integer(box.getHeightLevel());
			HashSet<LinkedBox> boxesAtThisLevel = boxesByLevel.get(heightLevelObj);
			if (boxesAtThisLevel == null)
			{
				HashSet<LinkedBox> levelBoxes = new HashSet<LinkedBox>();
				levelBoxes.add(box);
				boxesByLevel.put(heightLevelObj, levelBoxes);
			}
			else
			{
				boxesAtThisLevel.add(box);
				boxesByLevel.put(heightLevelObj, boxesAtThisLevel);
			}
		}
		Logger.log("Exiting loop for assigning heights", Logger.DEBUG);
		// Heights are now correct

		int nextWidthLevel = 0;

		VisitedWithInteger nextWidthAndVisited = new VisitedWithInteger(new HashSet<LinkedBox>(), nextWidthLevel);

		// Organizes the root row, which may need to be adjusted later
		Logger.log("Entering loop for organizing root Row", Logger.DEBUG);
		for (LinkedBox box : boxesByLevel.get(new Integer(rootLevel)))
		{
			HashSet<LinkedBox> thisAndExtendedSiblings = box.getThisAndExtendedSiblings();

			// No siblings
			if (thisAndExtendedSiblings.size() == 1)
			{
				box.setWidthLevel(nextWidthAndVisited.getInteger());
				nextWidthAndVisited.setInteger(nextWidthAndVisited.getInteger() + SPACE);
				nextWidthAndVisited.addVisited(box);
			}

			// Has siblings and this box hasn't been visited already
			else if (!nextWidthAndVisited.getVisited().contains(box))
			{
				nextWidthAndVisited = organizeSiblings(thisAndExtendedSiblings, nextWidthAndVisited);
				nextWidthLevel = nextWidthAndVisited.getInteger();
			}
		}
		Logger.log("Exiting loop for organizing root Row", Logger.DEBUG);

		int minWidth = Integer.MAX_VALUE;
		for (LinkedBox box : boxesByLevel.get(new Integer(rootLevel)))
		{
			HashSet<LinkedBox> rootBoxes = boxesByLevel.get(new Integer(rootLevel));
			rootBoxes.remove(box);
			minWidth = determineWidthRecursive(box, new VisitedWithMaxAndMin(rootBoxes)).getMin();
			break;
		}

		// Lowest width should be 0 -i.e. no negatives
		for (LinkedBox box : boxes)
		{
			box.setWidthLevel(box.getWidthLevel() - minWidth);
		}

		this.putBoxesOnGrid(boxes);
		Logger.log("Exiting organizeGrid", Logger.DEBUG);
	}

	private VisitedWithMaxAndMin determineWidthRecursive(LinkedBox box, VisitedWithMaxAndMin visitedWithLowWidth)
	{
		Logger.log("Entered determineWidthRecursive", Logger.DEBUG);
		if (!visitedWithLowWidth.getVisited().contains(box))
		{
			visitedWithLowWidth.addVisited(box);
			int origWidth = box.getWidthLevel();
			HashSet<LinkedBox> childBoxes = box.getChildBoxes();



			int nextWidthLevel = box.getWidthLevel() - childBoxes.size() + 1;
			visitedWithLowWidth.setMin(nextWidthLevel);

			visitedWithLowWidth = handleBoxSet(childBoxes, visitedWithLowWidth, nextWidthLevel);

			HashSet<LinkedBox> parentBoxes = box.getParentBoxes();
			nextWidthLevel = box.getWidthLevel() - parentBoxes.size() + 1;
			visitedWithLowWidth.setMin(nextWidthLevel);

			visitedWithLowWidth = handleBoxSet(parentBoxes, visitedWithLowWidth, nextWidthLevel);
		}
		Logger.log("Exiting determineWidthRecursive", Logger.DEBUG);
		return visitedWithLowWidth;
	}

	// This takes care of siblings too
	private VisitedWithMaxAndMin handleBoxSet(HashSet<LinkedBox> boxes, VisitedWithMaxAndMin visitedWithLowWidth, int nextWidthLevel)
	{
		Logger.log("Entered handleBoxSet", Logger.DEBUG);
		for (LinkedBox box : boxes)
		{
			if (!visitedWithLowWidth.getVisited().contains(box))
			{
				visitedWithLowWidth.addVisited(box);
				HashSet<LinkedBox> thisAndExtendedSibs = box.getThisAndExtendedSiblings();
				if (thisAndExtendedSibs.size() == 1)
				{
					box.setWidthLevel(nextWidthLevel);
				}
				else
				{
					VisitedWithInteger nextWidthAndVisited = new VisitedWithInteger(visitedWithLowWidth.getVisited(), nextWidthLevel);
					nextWidthAndVisited = organizeSiblings(box.getThisAndExtendedSiblings(), nextWidthAndVisited);
					nextWidthLevel = nextWidthAndVisited.getInteger() - SPACE;
					visitedWithLowWidth.setVisited(nextWidthAndVisited.getVisited());
				}
			}

			nextWidthLevel += SPACE;

			determineWidthRecursive(box, visitedWithLowWidth);

		}
		Logger.log("Exiting determineWidthRecursive", Logger.DEBUG);
		return visitedWithLowWidth;
	}

	// Rteurns the new value for nextWidthLevel
	private VisitedWithInteger organizeSiblings(HashSet<LinkedBox> thisAndExtendedSiblings, VisitedWithInteger nextWidthAndVisited)
	{
		Logger.log("Entered organizeSiblings", Logger.DEBUG);
		for (LinkedBox box : thisAndExtendedSiblings)
		{
			HashSet<LinkedBox> siblingBoxes = box.getSiblingBoxes();

			// Looks for the "start" edge box and then calls a recursive helper to do the rest
			if (siblingBoxes.size() == 1)
			{
				for (LinkedBox siblingBox : siblingBoxes)
				{
					siblingBox.setWidthLevel(nextWidthAndVisited.getInteger());
					nextWidthAndVisited.setInteger(nextWidthAndVisited.getInteger() + SPACE);
					nextWidthAndVisited.addVisited(siblingBox);
					return organizeSibsRecursive(box, nextWidthAndVisited);
				}

			}
		}
		Logger.log("Exited organizeSiblings", Logger.DEBUG);
		return nextWidthAndVisited;
	}

	// Box is intiliazed as the first box after the start edge box; nextWithAndVisited must be properly incremented and include the start edge box
	private VisitedWithInteger organizeSibsRecursive(LinkedBox box, VisitedWithInteger nextWidthAndVisited)
	{
		Logger.log("Entered organizeSibsRecursive", Logger.DEBUG);
		nextWidthAndVisited.addVisited(box);
		box.setWidthLevel(nextWidthAndVisited.getInteger());

		HashSet<LinkedBox> siblingBoxes = box.getSiblingBoxes();

		// if box has 2 siblings and we're in the middle so we need recursion
		if (siblingBoxes.size() == 2)
		{
			// Given our linear tarversal, one box will have been visited and the other not.
			for (LinkedBox siblingBox : siblingBoxes)
			{
				if (!nextWidthAndVisited.getVisited().contains(siblingBox))
				{
					nextWidthAndVisited.setInteger(nextWidthAndVisited.getInteger() + SPACE);
					return organizeSibsRecursive(siblingBox, nextWidthAndVisited);
				}
			}
		}

		// If we make it this far we're at the end of the linear siblings, i.e. the other edge box that has just 1 sibling
		nextWidthAndVisited.setInteger(nextWidthAndVisited.getInteger() + SPACE);
		Logger.log("Exiting organizeSibsRecursive", Logger.DEBUG);
		return nextWidthAndVisited;
	}

	private VisitedWithMaxAndMin determineRawHeightLevels(LinkedBox box, int heightLevel, VisitedWithMaxAndMin storage)
	{
		Logger.log("Entered determineRawHeightLevels", Logger.DEBUG);
		if (!storage.getVisited().contains(box))
		{
			box.setHeightLevel(heightLevel);
			storage.addVisited(box);

			for (LinkedBox childBox : box.getChildBoxes())
			{
				if (isOrganizeTopToBottom)
				{
					storage.setMin(heightLevel - SPACE);
					storage = determineRawHeightLevels(childBox, heightLevel - SPACE, storage);
				}
				else
				{
					storage.setMax(heightLevel + SPACE);
					storage = determineRawHeightLevels(childBox, heightLevel + SPACE, storage);
				}
			}

			for (LinkedBox parentBox : box.getParentBoxes())
			{
				if (isOrganizeTopToBottom)
				{
					storage.setMax(heightLevel + SPACE);
					storage = determineRawHeightLevels(parentBox, heightLevel + SPACE, storage);
				}
				else
				{
					storage.setMin(heightLevel - SPACE);
					storage = determineRawHeightLevels(parentBox, heightLevel - SPACE, storage);
				}
			}

			for (LinkedBox siblingBox : box.getSiblingBoxes())
			{
				storage = determineRawHeightLevels(siblingBox, heightLevel, storage);
			}
		}

		Logger.log("Exited determineRawHeightLevels", Logger.DEBUG);
		return storage;
	}
}