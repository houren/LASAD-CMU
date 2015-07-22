package lasad.gwt.client.model.organization;

import java.util.HashMap;
import java.util.HashSet;
import lasad.gwt.client.model.organization.LinkedBox;
import lasad.gwt.client.model.organization.OrganizerLink;
import java.util.Collection;
import lasad.gwt.client.logger.Logger;
import lasad.gwt.client.model.organization.ArgumentGrid;

/**
 *	An argument thread is a connected chain of boxes on the argument map space.
 *  This class is useful for AutoOrganizer.
 *	@author Kevin Loughlin
 *	@since 19 June 2015, Updated 21 July 2015
 */

public class ArgumentThread
{
	// HashMap allows for constant lookup time by BoxID
	private HashMap<Integer, LinkedBox> boxMap;

	private int height;
	private int width;

	private int threadID;

	// The chessboard set up used for auto organization of the map
	private ArgumentGrid grid;

	public ArgumentThread()
	{
		this.boxMap = new HashMap<Integer, LinkedBox>();
		this.threadID = 0;
		this.grid = new ArgumentGrid();
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

	public void setThreadID(int threadID)
	{
		this.threadID = threadID;
	}

	public int getThreadID()
	{
		return threadID;
	}

	public Collection<LinkedBox> getBoxes()
	{
		return boxMap.values();
	}

	public ArgumentGrid getGrid()
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
		return boxMap.remove(this.getBoxByBoxID(boxID));
	}

	// Elt ID is boxID for boxes.  Whoever came up with the idea to give boxes two separate ID's is killing me.
	// I just spent an hour debugging to realize the error was that I used rootID instead of boxID.
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

						LinkedBox startBox = childLink.getStartBox();
						LinkedBox endBox = childLink.getEndBox();

						// We might have already removed the start box from the link
						if (startBox != null)
						{
							startBox.removeChildLink(childLink);
						}

						// We might have already removed the end box from the link
						if (endBox != null)
						{
							endBox.removeParentLink(childLink);
						}

						return returnValue;
					}
				}

				for (OrganizerLink parentLink : box.getParentLinks())
				{
					if (parentLink.getLinkID() == eltID)
					{
						returnValue = parentLink.clone();

						LinkedBox startBox = parentLink.getStartBox();
						LinkedBox endBox = parentLink.getEndBox();

						// We might have already removed the start box from the link
						if (startBox != null)
						{
							startBox.removeChildLink(parentLink);
						}

						// We might have already removed the end box from the link
						if (endBox != null)
						{
							endBox.removeParentLink(parentLink);
						}

						return returnValue;
					}
				}

				for (OrganizerLink siblingLink : box.getSiblingLinks())
				{
					if (siblingLink.getLinkID() == eltID)
					{
						returnValue = siblingLink.clone();

						LinkedBox startBox = siblingLink.getStartBox();
						LinkedBox endBox = siblingLink.getEndBox();

						if (startBox!= null)
						{
							startBox.removeSiblingLink(siblingLink);
						}

						if (endBox != null)
						{
							endBox.removeSiblingLink(siblingLink);
						}

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

	/*
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
	*/

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