package lasad.gwt.client.model.organization;

// Aware that this is unnecessary, I just do it as a reminder in case I change location
import lasad.gwt.client.model.organization.ArgumentThread;
import java.util.Vector;
import java.util.HashMap;
import java.util.Collection;

import lasad.gwt.client.model.organization.OrganizerLink;
import lasad.gwt.client.model.organization.LinkedBox;


/**
 *	An argument model is simply a vector of threads, i.e. a vector separate chains of arguments on the map space.
 *	This format of modeling is more conducive to support for AutoOrganizer.
 *	@author Kevin Loughlin
 *	@since 19 June 2015, Updated 24 June 2015
 */
public class ArgumentModel
{
	private Vector<ArgumentThread> argThreads;

	// One model instance per map, where String is mapID
	private static HashMap<String, ArgumentModel> instances = new HashMap<String, ArgumentModel>();

	// Just for this class, if we need to create a new instance below
	private ArgumentModel()
	{
		this.argThreads = new Vector<ArgumentThread>();
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
	}

	public void removeBoxByBoxID(int boxID)
	{
		for (ArgumentThread argThread : argThreads)
		{
			LinkedBox removed = argThread.removeBoxByBoxID(boxID);
			if (removed != null)
			{
				break;
			}
		}
	}

	public LinkedBox getBoxByBoxID(int boxID)
	{
		for (ArgumentThread argThread : argThreads)
		{
			LinkedBox box = argThread.getBoxByBoxID(boxID);
			if (box != null)
			{
				return box;
			}
		}
		return null;
	}

	public OrganizerLink removeLinkByLinkID(int linkID)
	{
		for (ArgumentThread argThread : argThreads)
		{
			OrganizerLink link = argThread.removeLinkByLinkID(linkID);
			if (link != null)
			{
				return link;
			}
		}
		return null;
	}

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

	public Collection<ArgumentThread> getArgThreads()
	{
		Collection<ArgumentThread> colArgThreads = argThreads;
		return colArgThreads;
	}

	@Override
	public String toString()
	{
		StringBuilder buffer = new StringBuilder("\n***********\nBEGIN ARGUMENT MODEL\n***********");
		for (ArgumentThread argThread : this.argThreads)
		{
			buffer.append(argThread.toString());
		}
		buffer.append("\n***********\nEND OF MODEL\n***********\n");
		return buffer.toString();
	}
}