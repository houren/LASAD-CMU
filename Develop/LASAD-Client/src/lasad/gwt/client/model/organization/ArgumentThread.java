package lasad.gwt.client.model.organization;

import java.util.HashMap;
import lasad.gwt.client.model.organization.LinkedBox;
import lasad.gwt.client.model.organization.OrganizerLink;
import java.util.Collection;

public class ArgumentThread
{
	// HashMap allows for constant lookup time by BoxID
	private HashMap<Integer, LinkedBox> boxMap;

	private HashMap<Integer, OrganizerLink> linkMap;

	// Root boxes are boxes that start an argument thread (i.e. have no parents).  No association with rootID
	private HashMap<Integer, LinkedBox> rootBoxMap;

	public ArgumentThread()
	{
		this.boxMap = new HashMap<Integer, LinkedBox>();
		this.rootBoxMap = new HashMap<Integer, LinkedBox>();
		this.linkMap = new HashMap<Integer, OrganizerLink>();
	}

	public ArgumentThread(LinkedBox box)
	{
		this.boxMap = new HashMap<Integer, LinkedBox>();
		this.boxMap.put(new Integer(box.getBoxID()), box);
		this.rootBoxMap = new HashMap<Integer, LinkedBox>();
		ifRootAddBox(box);
		this.linkMap = new HashMap<Integer, OrganizerLink>();
	}

	public ArgumentThread(Collection<LinkedBox> boxes)
	{
		this.boxMap = new HashMap<Integer, LinkedBox>();
		this.rootBoxMap = new HashMap<Integer, LinkedBox>();
		for (LinkedBox box : boxes)
		{
			addBox(box);
		}
		this.linkMap = new HashMap<Integer, OrganizerLink>();
	}

	public Collection<LinkedBox> getBoxes()
	{
		return boxMap.values();
	}

	public void addBox(LinkedBox box)
	{
		boxMap.put(new Integer(box.getBoxID()), box);
		ifRootAddBox(box);
	}

	public void addBoxes(Collection<LinkedBox> boxes)
	{
		for (LinkedBox box : boxes)
		{
			this.addBox(box);
		}
	}

	public LinkedBox removeBoxByBoxID(int boxID)
	{
		LinkedBox toRemove = this.getBoxByBoxID(boxID);
		if (toRemove != null)
		{
			toRemove.removeLinksToSelf();
			Integer boxIntID = new Integer(boxID);
			LinkedBox returnValue = boxMap.remove(boxIntID);
			rootBoxMap.remove(boxIntID);
			return returnValue;
		}
		else
		{
			return null;
		}
	}

	public OrganizerLink removeLinkByLinkID(int linkID)
	{
		OrganizerLink toRemove = this.getLinkByLinkID(linkID);
		if (toRemove != null)
		{
			Integer linkIntID = new Integer(linkID);
			OrganizerLink returnValue = linkMap.remove(linkIntID);
			return returnValue;
		}
		else
		{
			return null;
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

	public OrganizerLink getLinkByLinkID(int linkID)
	{
		Integer id = new Integer(linkID);
		return linkMap.get(id);
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

	public Collection<LinkedBox> getRootBoxes()
	{
		return rootBoxMap.values();
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

	private void ifRootAddBox(LinkedBox box)
	{
		if (box.getNumParents() == 0)
		{
			rootBoxMap.put(new Integer(box.getBoxID()), box);
		}
	}

	@Override
	public String toString()
	{
		StringBuilder buffer = new StringBuilder("\nArgument Thread\n");
		buffer = buffer.append("boxes\n");
		for (LinkedBox box : boxMap.values())
		{
			buffer = buffer.append(box.toString());
		}

		buffer = buffer.append("End of thread\n\n");
		return buffer.toString();
	}
}