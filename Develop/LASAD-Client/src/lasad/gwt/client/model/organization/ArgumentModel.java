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
	
	private ArrayList<LinkedBox> discoverArgThread(LinkedBox rootBox){
		ArrayList<LinkedBox> siblings = new ArrayList<LinkedBox>();
		siblings.add(rootBox);
		siblings.addAll(rootBox.getSiblingBoxes());
		
		visited.addAll(siblings);
		
		ArrayList<LinkedBox> allBranch = new ArrayList<LinkedBox>();
		allBranch.addAll(siblings);
		
		for(LinkedBox box : siblings){
			for(LinkedBox childBox : box.getChildBoxes())
			{
				if(!visited.contains(childBox))
				{
					allBranch.addAll(discoverArgThread(childBox));
				}else{
					allBranch.add(childBox);
					return allBranch;
				}
			}
		}
		return allBranch;
	}
	
	//guarantees that the ArgThreads listed correspond to the current state of the map
	public void updateArgThreads()
	{
		HashSet<LinkedBox> allBoxes = new HashSet<LinkedBox>();
		for(ArgumentThread argThread : this.getArgThreads()){
			allBoxes.addAll(argThread.getBoxes());
			this.removeArgThread(argThread);
		}
		
		ArrayList<ArrayList<LinkedBox>> threads = new ArrayList<ArrayList<LinkedBox>>();
		visited.clear();
		ArrayList<LinkedBox> thread;
		for(LinkedBox box : allBoxes)
		{
			if(visited.contains(box)) continue;
			thread = discoverArgThread(box);
			
			boolean partOfOther = false;
			LinkedBox lastChild = thread.get(thread.size()-1);
			HashSet<LinkedBox> firstParents = thread.get(0).getParentBoxes();
			for(ArrayList<LinkedBox> set : threads)
			{
				if(partOfOther)
					break;
				
				if(set.contains(lastChild))
				{
					thread.remove(lastChild);
					set.addAll(thread);
					partOfOther = true;
					break;
				}
				
				for(LinkedBox parent : firstParents)
				{
					if(set.contains(parent))
					{
						set.addAll(thread);
						partOfOther = true;
						break;
					}
				}
			}
			
			if(!partOfOther)
				threads.add(thread);
		}
		
		for(ArrayList<LinkedBox> set : threads)
		{
			this.addArgThread(new ArgumentThread(set));
		}
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