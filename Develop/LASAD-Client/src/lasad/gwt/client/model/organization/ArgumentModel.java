package lasad.gwt.client.model.organization;

// Aware that this is unnecessary, I just do it as a reminder in case I change location
import lasad.gwt.client.model.organization.ArgumentThread;
import java.util.Vector;
import java.util.HashMap;
import java.util.Collection;

import lasad.gwt.client.model.organization.OrganizerLink;
import lasad.gwt.client.model.organization.LinkedBox;

public class ArgumentModel
{
	private Vector<ArgumentThread> argThreads;
	private static HashMap<String, ArgumentModel> instances = new HashMap<String, ArgumentModel>();

	private ArgumentModel()
	{
		this.argThreads = new Vector<ArgumentThread>();
	}

	public static ArgumentModel getInstanceByMapID(String mapID)
	{
		ArgumentModel myArgModel = instances.get(mapID);

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

	/*
	public boolean fromSameThread(OrganizerLink link)
	{
		return (getBoxThread(link.getStartBox()) == getBoxThread(link.getEndBox()));
	}
	*/

	// Dangerous, be careful with this
	public Collection<ArgumentThread> getArgThreads()
	{
		Collection<ArgumentThread> colArgThreads = argThreads;
		return colArgThreads;
	}

	@Override
	public String toString()
	{
		StringBuilder buffer = new StringBuilder("\n\n*****\nArgument Model\n*****\n");
		for (ArgumentThread argThread : this.argThreads)
		{
			buffer.append(argThread.toString());
		}
		buffer.append("END OF MODEL\n\n");
		return buffer.toString();
	}

}