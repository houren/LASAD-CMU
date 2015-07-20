package lasad.gwt.client.model.organization;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Collection;
import java.util.ArrayList;

// Aware that this is unnecessary, I just do it as a reminder in case I change location
import lasad.gwt.client.model.organization.ArgumentThread;
import lasad.gwt.client.model.organization.OrganizerLink;
import lasad.gwt.client.model.organization.LinkedBox;


/**
 *	An argument model is simply a vector of threads, i.e. a vector separate chains of arguments on the map space.
 *	This format of modeling is more conducive to support for AutoOrganizer.
 *	@author Kevin Loughlin
 *	@since 19 June 2015, Updated 30 June 2015
 */
public class ArgumentModel
{
	private HashSet<ArgumentThread> argThreads;

	// One model instance per map, where String is mapID
	private static HashMap<String, ArgumentModel> instances = new HashMap<String, ArgumentModel>();
	
	// Clear this before use in a method
	private HashSet<LinkedBox> visited = new HashSet<LinkedBox>();

	// Just for this class, if we need to create a new instance below
	private ArgumentModel()
	{
		this.argThreads = new HashSet<ArgumentThread>();
	}

	public static ArgumentModel getInstanceByMapID(String mapID)
	{
		ArgumentModel myArgModel = instances.get(mapID);

		// if the model doesn't already exist, create it
		if (myArgModel == null)
		{
			instances.put(mapID, new ArgumentModel());
			myArgModel = instances.get(mapID);
		}

		return myArgModel;
	}

	public void addArgThread(ArgumentThread argThread)
	{
		this.argThreads.add(argThread);
	}

	public void removeArgThread(ArgumentThread argThread)
	{
		this.argThreads.remove(argThread);
		ArgumentThread.decNumThreads();
	}

	public void removeEmptyThreads()
	{
		for (ArgumentThread argThread : argThreads)
		{
			if (argThread.getBoxes().size() == 0)
			{
				this.removeArgThread(argThread);
			}
		}
	}

	public Object removeEltByEltID(int eltID)
	{
		for (ArgumentThread argThread : argThreads)
		{
			Object removed = argThread.removeEltByEltID(eltID);
			if (removed != null)
			{
				return removed;
			}
		}

		return null;
	}

	public HashSet<LinkedBox> getBoxes()
	{
		HashSet<LinkedBox> boxes = new HashSet<LinkedBox>();
		for (ArgumentThread argThread : this.getArgThreads())
		{
			boxes.addAll(argThread.getBoxes());
		}
		return boxes;
	}

	public LinkedBox getBoxByBoxID(int boxID)
	{
		LinkedBox returnBox = null;
		for (ArgumentThread argThread : this.argThreads)
		{
			returnBox = argThread.getBoxByBoxID(boxID);
			if (returnBox != null)
			{
				return returnBox;
			}
		}
		
		return null;
		
	}

	public LinkedBox getBoxByRootID(int rootID)
	{
		for (ArgumentThread argThread : argThreads)
		{
			LinkedBox box = argThread.getBoxByRootID(rootID);
			if (box != null)
			{
				return box;
			}
		}
		return null;
	}

	// Returns the argument thread of the provided box, else null
	public ArgumentThread getBoxThread(LinkedBox box)
	{
		for (ArgumentThread thread : argThreads)
		{
			if (thread.contains(box))
			{
				return thread;
			}
		}
		return null;
	}

	public HashSet<ArgumentThread> getArgThreads()
	{
		return argThreads;
	}

	@Override
	public String toString()
	{
		int counter = 1;
		StringBuilder buffer = new StringBuilder("\n***********\nBEGIN ARGUMENT MODEL\n***********");
		for (ArgumentThread argThread : this.argThreads)
		{
			buffer.append("\n\tThread " + counter);
			buffer.append(argThread.toString());
			counter++;
		}
		buffer.append("\n***********\nEND OF MODEL\n***********\n");
		return buffer.toString();
	}
}