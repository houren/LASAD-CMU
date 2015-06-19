package lasad.gwt.client.model.organization;

import java.util.HashMap;
import lasad.gwt.client.model.organization.LinkedBox;
import java.util.Collection;

public class ArgumentThread
{
	// HashMap allows for constant lookup time by BoxID
	private HashMap<Integer, LinkedBox> boxes;
	private HashMap<Integer, LinkedBox> rootBoxes;

	public ArgumentThread()
	{
		this.boxes = new HashMap<Integer, LinkedBox>();
		this.rootBoxes = new HashMap<Integer, LinkedBox>();
	}

	public ArgumentThread(LinkedBox box)
	{
		this.boxes = new HashMap<Integer, LinkedBox>();
		this.boxes.put(new Integer(box.getBoxID()), box);
		this.rootBoxes = new HashMap<Integer, LinkedBox>();
		ifRootAddBox(box);
	}

	public ArgumentThread(Collection<LinkedBox> boxes)
	{
		this.boxes = new HashMap<Integer, LinkedBox>();
		this.rootBoxes = new HashMap<Integer, LinkedBox>();
		for (LinkedBox box : boxes)
		{
			this.boxes.put(new Integer(box.getBoxID()), box);
			ifRootAddBox(box);
		}
	}

	public Collection<LinkedBox> getBoxes()
	{
		return boxes.values();
	}

	public void addBox(LinkedBox box)
	{
		boxes.put(new Integer(box.getBoxID()), box);
		ifRootAddBox(box);
	}

	public void addBoxes(Collection<LinkedBox> boxes)
	{
		for (LinkedBox box : boxes)
		{
			this.addBox(box);
			ifRootAddBox(box);
		}
	}

	public void removeBox(LinkedBox box)
	{
		Integer boxID = new Integer(box.getBoxID());
		boxes.remove(boxID);
		rootBoxes.remove(boxID);
	}

	public void removeBoxes(Collection<LinkedBox> boxes)
	{
		for (LinkedBox box : boxes)
		{
			this.removeBox(box);
		}
	}

	public boolean contains(LinkedBox box)
	{
		return boxes.containsValue(box);
	}

	public LinkedBox getBoxByID(int boxID)
	{
		Integer id = new Integer(boxID);
		return boxes.get(id);
	}

	// Since ID would be the same, I can just remove and add the "newBox"
	public void replaceBox(LinkedBox newBox)
	{
		this.removeBox(newBox);
		this.addBox(newBox);
	}

	public Collection<LinkedBox> getRootBoxes()
	{
		return rootBoxes.values();
	}

	private void ifRootAddBox(LinkedBox box)
	{
		if (box.getNumParents() == 0)
		{
			rootBoxes.put(new Integer(box.getBoxID()), box);
		}
	}
}