package lasad.gwt.client.model.organization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.HashSet;
import lasad.gwt.client.model.organization.LinkedBox;
import lasad.gwt.client.model.organization.OrganizerLink;
import java.util.Collection;
import lasad.gwt.client.logger.Logger;

/**
 *	An argument thread is a connected chain of boxes on the argument map space.
 *  This class is useful for AutoOrganizer.
 *	@author Kevin Loughlin
 *	@since 19 June 2015, Updated 30 June 2015
 */

public class ArgumentThread
{
	private static int numThreads = 0;
	// HashMap allows for constant lookup time by BoxID
	private HashMap<Integer, LinkedBox> boxMap;

	private int threadID;

	// Clear this before use in a method
	private HashSet<LinkedBox> visited = new HashSet<LinkedBox>();

	public ArgumentThread()
	{
		this.boxMap = new HashMap<Integer, LinkedBox>();
		numThreads++;
		this.threadID = numThreads;
	}

	public ArgumentThread(LinkedBox box)
	{
		this.boxMap = new HashMap<Integer, LinkedBox>();
		this.boxMap.put(new Integer(box.getBoxID()), box);
		numThreads++;
		this.threadID = numThreads;
	}

	public ArgumentThread(Collection<LinkedBox> boxes)
	{
		this.boxMap = new HashMap<Integer, LinkedBox>();
		for (LinkedBox box : boxes)
		{
			addBox(box);
		}
		numThreads++;
		this.threadID = numThreads;
	}

	public Collection<LinkedBox> getBoxes()
	{
		return boxMap.values();
	}

	public void addBox(LinkedBox box)
	{
		if (boxMap.values().contains(box))
		{
			return;
		}
		else
		{
			boxMap.put(new Integer(box.getBoxID()), box);
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
		LinkedBox toRemove = this.getBoxByBoxID(boxID);
		if (toRemove != null)
		{
			toRemove.removeLinksToSelf();
			Integer boxIntID = new Integer(boxID);
			LinkedBox returnValue = boxMap.remove(boxIntID);
			return returnValue;
		}
		else
		{
			return null;
		}
	}

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
						childLink.getEndBox().removeParentLink(childLink);
						box.removeChildLink(childLink);
						return returnValue;
					}
				}

				for (OrganizerLink parentLink : box.getParentLinks())
				{
					if (parentLink.getLinkID() == eltID)
					{
						returnValue = parentLink.clone();
						parentLink.getStartBox().removeChildLink(parentLink);
						box.removeParentLink(parentLink);
						return returnValue;
					}
				}

				for (OrganizerLink siblingLink : box.getSiblingLinks())
				{
					if (siblingLink.getLinkID() == eltID)
					{
						returnValue = siblingLink.clone();
						if (box.equals(siblingLink.getStartBox()))
						{
							siblingLink.getEndBox().removeSiblingLink(siblingLink);
						}
						else
						{
							siblingLink.getStartBox().removeSiblingLink(siblingLink);
						}
						box.removeSiblingLink(siblingLink);
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

	public boolean contains(LinkedBox box)
	{
		return boxMap.containsValue(box);
	}

	public LinkedBox getBoxByBoxID(int boxID)
	{
		Integer id = new Integer(boxID);
		return boxMap.get(id);
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
	
	//identify a cycle
	// As used, start is initialiazed as the INITIAL currentBox and its siblings
	private boolean isCycle(HashSet<LinkedBox> start, LinkedBox currentBox){
		if(start.contains(currentBox))
		{
			visited.addAll(start);
			
			for(LinkedBox box : start)
			{
				for(LinkedBox childBox : box.getChildBoxes())
				{
					if(isCycle(start, childBox))
						return true;
				}
			}
		}
		else
		{
			HashSet<LinkedBox> siblings = new HashSet<LinkedBox>(currentBox.getSiblingBoxes());
			siblings.add(currentBox);
			
			for(LinkedBox box : siblings)
			{
				for(LinkedBox childBox : box.getChildBoxes())
				{
					if(start.contains(childBox)){
						visited.addAll(siblings);
						return true;
					}if(visited.contains(childBox)){
						return false;
					}if(isCycle(start, childBox)){
						visited.addAll(siblings);
						return true;
					}
				}
			}
		}
		return false;
	}

	// RootBoxes are boxes that no have parents, nor do their siblings.  They are useful for a "starting point" when traversing a thread.
	public HashSet<LinkedBox> getRootBoxes()
	{
		HashSet<LinkedBox> rootBoxes = new HashSet<LinkedBox>();
		for (LinkedBox box : boxMap.values())
		{
			visited.clear();
			if (isRoot(box))
			{
				rootBoxes.addAll(visited);
				//break;
			}
		}
		visited.clear();
		
		//when the threads has boxes but no root is found, the root is a cycle
		if(boxMap.values().size() > 0 && rootBoxes.size() == 0)
		{
			Logger.log("[lasad.gwt.client.communication.ArgumentThread][run] Starting Cycle detection", Logger.DEBUG);
			boolean hasParent;
			HashSet<LinkedBox> start = new HashSet<LinkedBox>();
			HashSet<LinkedBox> verified = new HashSet<LinkedBox>();
			
			for (LinkedBox box : boxMap.values())
			{
				hasParent = false;
				visited.clear();
				start.clear();
				start.addAll(box.getSiblingBoxes());
				start.add(box);
				if(verified.contains(box))
					continue;
				verified.addAll(start);
				
				Logger.log("[lasad.gwt.client.communication.ArgumentThread][run] Gonna run isCycle, box: "+box, Logger.DEBUG);
				if (isCycle(start, box))
				{
					LinkedBox root = null;
					int minParents = Integer.MAX_VALUE;
					int numParents;
					Logger.log("[lasad.gwt.client.communication.ArgumentThread][run] Finding the best box to be root", Logger.DEBUG);
					for(LinkedBox cBox : visited)
					{
						numParents = cBox.getParentBoxes().size();
						for(LinkedBox sibling : cBox.getSiblingBoxes())
							numParents += sibling.getParentBoxes().size();
						
						if(numParents < minParents)
						{
							minParents = numParents;
							root = cBox;
						}
					}
					
					if(root != null)
					{
						rootBoxes.addAll(root.getSiblingBoxes());
						rootBoxes.add(root);
						break;
					}
					Logger.log("[lasad.gwt.client.communication.ArgumentThread][run] Assigning roots: "+rootBoxes, Logger.DEBUG);
				}
			}
		
			visited.clear();
		}
		
		return rootBoxes;
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

	// Determines if a box is a root box (visited must be cleared before entering this function)
	private boolean isRoot(LinkedBox box)
	{
		if (!findIfBoxHasExtendedParents(box))
		{
			return true;
		}
		return false;
	}

	// Recursive method, determines if a box or its siblings has parents (and thus wouldn't be a rootBox);
	// Visited must be cleared before calling this function
	private boolean findIfBoxHasExtendedParents(LinkedBox box)
	{
		if (box.getNumParents() > 0)
		{
			return true;
		}
		else
		{
			if (visited.contains(box))
			{
				return false;
			}
			else
			{
				visited.add(box);
				HashSet<LinkedBox> siblingBoxes = box.getSiblingBoxes();

				for (LinkedBox siblingBox : siblingBoxes)
				{
					if(findIfBoxHasExtendedParents(siblingBox))
					{
						return true;
					}
				}
				return false;
			}
		}
	}

	public static void decNumThreads()
	{
		numThreads--;
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