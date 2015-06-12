package lasad.gwt.client.ui.box;

import java.util.Vector;

/**
 * LinkedBox represents the ID of an AbstractBox, as well as its connected IDs for its potential child boxes, sibling (group) boxes,
 * and parent boxes.  This allows for an easy organization of all boxes on the map, as they can be followed in a chain similar to 
 * a linked this.  This class is key to AutoOrganizer.  All names within the class are self-explanatory.
 * @author Kevin Loughlin
 * @since 11 June 2015
 */

public class LinkedBox
{
	private final int boxID;
	private Vector<LinkedBox> childBoxes = null;
	private Vector<LinkedBox> parentBoxes = null;
	private int heightLevel = 0;
	private int widthLevel = 0;

	
	// ALL sibling boxes (inbound and outbound)
	/*
	private Vector<LinkedBox> siblingBoxes = null;

	// A workaround as I do linked premises.
	// "outbound" siblings -> that is, links go out from a box and into a sibling, but not the other way around
	private Vector<LinkedBox> outboundSiblings = null;
	*/

	//public LinkedBox(int boxID, Vector<LinkedBox> childBoxes, Vector<LinkedBox> parentBoxes, Vector<LinkedBox> siblingBoxes, Vector<LinkedBox> outboundSiblings)
	public LinkedBox(int boxID, Vector<LinkedBox> childBoxes, Vector<LinkedBox> parentBoxes)
	{
		this.boxID = boxID;
		this.childBoxes = childBoxes;
		this.parentBoxes = parentBoxes;
		//this.siblingBoxes = siblingBoxes;
		//this.outboundSiblings = outboundSiblings;
	}

	public LinkedBox(int boxID)
	{
		this.boxID = boxID;
	}

	// I don't want people to use default constructor
	private LinkedBox()
	{
		this.boxID = 0;
	}

	public int getBoxID()
	{
		return boxID;
	}

	public Vector<LinkedBox> getChildBoxes()
	{
		return childBoxes;
	}

	public Vector<LinkedBox> getParentBoxes()
	{
		return parentBoxes;
	}

	public void addChildBox(LinkedBox childBox)
	{
		this.childBoxes.add(childBox);
	}

	public void addParentBox(LinkedBox parentBox)
	{
		this.parentBoxes.add(parentBox);
	}

	public int getNumChildren()
	{
		if (childBoxes == null)
		{
			return 0;
		}
		else
		{
			return childBoxes.size();
		}
	}

	public int getNumParents()
	{
		if (parentBoxes == null)
		{
			return 0;
		}
		else
		{
			return parentBoxes.size();
		}
	}

	public void setHeightLevel(int heightLevel)
	{
		this.heightLevel = heightLevel;
	}

	public void setWidthLevel(int widthLevel)
	{
		this.widthLevel = widthLevel;
	}

	public int getHeightLevel()
	{
		return heightLevel;
	}

	public int getWidthLevel()
	{
		return widthLevel;
	}

	// Box IDs are unique, so this is all we need for equality
	@Override
	public boolean equals(Object object)
	{
		if (object instanceof LinkedBox)
		{
			if (this.boxID == ( (LinkedBox) object).boxID )
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
/*
	public Vector<LinkedBox> getSiblingBoxes()
	{
		return siblingBoxes;
	}

	public Vector<LinkedBox> getOutBoundSiblings()
	{
		return outboundSiblings;
	}

	public void addSiblingBox(LinkedBox siblingBox)
	{
		this.siblingBoxes.add(siblingBox);
	}

	public void addOutboundSibling(LinkedBox outboundSibling)
	{
		this.outboundSiblings.add(outboundSibling);
	}

	public int getNumSiblings()
	{
		if (siblingBoxes == null)
		{
			return 0;
		}
		else
		{
			return siblingBoxes.size();
		}
	}

	public int getNumOutboundSiblings()
	{
		if (outboundSiblings == null)
		{
			return 0;
		}
		else
		{
			return outboundSiblings.size();
		}
	}
*/
}