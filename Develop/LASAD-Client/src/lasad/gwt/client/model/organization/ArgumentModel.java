package lasad.gwt.client.model.organization;

// Aware that this is unnecessary, I just do it as a reminder in case I change location
import lasad.gwt.client.model.organization.ArgumentThread;
import java.util.Vector;
import java.util.HashMap;
import java.util.Collection;

public class ArgumentModel
{
	private Vector<ArgumentThread> argThreads;
	private static HashMap<Integer, ArgumentModel> instances = new HashMap<Integer, ArgumentModel>();

	private ArgumentModel()
	{
		this.argThreads = new Vector<ArgumentThread>();
	}

	public ArgumentModel getInstanceByMapID(int mapID)
	{
		Integer myMapID = new Integer(mapID);
		ArgumentModel myArgModel = instances.get(myMapID);

		if (myArgModel == null)
		{
			instances.put(myMapID, new ArgumentModel());
			myArgModel = instances.get(myMapID);
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

	// Dangerous, be careful with this
	public Collection<ArgumentThread> getArgThreads()
	{
		Collection<ArgumentThread> colArgThreads = argThreads;
		return colArgThreads;
	}
}