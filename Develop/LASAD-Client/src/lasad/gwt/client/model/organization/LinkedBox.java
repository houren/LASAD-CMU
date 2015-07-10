package lasad.gwt.client.model.organization;

import java.util.HashSet;
import lasad.gwt.client.model.organization.OrganizerLink;
import lasad.gwt.client.logger.Logger;

/**
 * LinkedBox is an alternative representation to AbstractBox, more conducive for map organization and modeling.  Each LinkedBox has its boxID
 * and rootID, as well as all its types of Links (i.e. an OrganizerLink that points to the next box). This allows for an easy
 * organization of all Links on the map, as they can be followed in a chain similar to a linked list.  This class is key to AutoOrganizer.
 * All names within this class are self-explanatory.
 * @author Kevin Loughlin
 * @since 11 June 2015, Updated 6 July 2015
 */

public class LinkedBox
{
	private final int ERROR = -1;

	// I.e. premise, conclusion, etc.
	private final String type;

	// Be mindful of difference between boxID and rootID.
	private final int boxID;
	private final int rootID;

	private int width;
	private int height;

	// Note: updating xLeft will class methods will automatically update xCenter and vice-versa.  Same is true for y equivalents.
	private double xLeft;
	private double yTop;

	private double xCenter;
	private double yCenter;

	private HashSet<OrganizerLink> childLinks;
	private HashSet<LinkedBox> childBoxes;

	private HashSet<OrganizerLink> parentLinks;
	private HashSet<LinkedBox> parentBoxes;

	// siblingLinks are those that are of subType "Linked Premises" and are thus handled differently
	private HashSet<OrganizerLink> siblingLinks;
	private HashSet<LinkedBox> siblingBoxes;

	// Height and width level will be used like a coordinate grid once we come to organizeMap() of AutoOrganizer
	private int heightLevel;
	private int widthLevel;

	// if the box can be grouped
	private boolean canBeGrouped;

	// This is the meat and bones constructor
	public LinkedBox(int boxID, int rootID, String type, double xLeft, double yTop, int width, int height, boolean canBeGrouped)
	{
		this.boxID = boxID;
		this.rootID = rootID;
		this.type = type;
		this.xLeft = xLeft;
		this.yTop = yTop;
		this.width = width;
		this.height = height;
		this.xCenter = xLeft + width / 2.0;
		this.yCenter = yTop + height / 2.0;
		this.childLinks = new HashSet<OrganizerLink>();
		this.parentLinks = new HashSet<OrganizerLink>();
		this.siblingLinks = new HashSet<OrganizerLink>();
		this.childBoxes = new HashSet<LinkedBox>();
		this.parentBoxes = new HashSet<LinkedBox>();
		this.siblingBoxes = new HashSet<LinkedBox>();
		this.heightLevel = 0;
		this.widthLevel = 0;
		this.canBeGrouped = canBeGrouped;
	}

	// I don't want people to use the default constructor because this LinkedBox needs definitive IDs.  This just quiets the compiler.
	private LinkedBox()
	{
		this.boxID = ERROR;
		this.rootID = ERROR;
		this.type = "garbage";
		this.xLeft = ERROR;
		this.yTop = ERROR;
		this.width = ERROR;
		this.height = ERROR;
		this.xCenter = ERROR;
		this.yCenter = ERROR;
		this.childLinks = new HashSet<OrganizerLink>();
		this.parentLinks = new HashSet<OrganizerLink>();
		this.siblingLinks = new HashSet<OrganizerLink>();
		this.childBoxes = new HashSet<LinkedBox>();
		this.parentBoxes = new HashSet<LinkedBox>();
		this.siblingBoxes = new HashSet<LinkedBox>();
		this.heightLevel = ERROR;
		this.widthLevel = ERROR;
		this.canBeGrouped = false;
	}

	public boolean getCanBeGrouped()
	{
		return canBeGrouped;
	}

	public int getBoxID()
	{
		return boxID;
	}

	public int getRootID()
	{
		return rootID;
	}

	public String getType()
	{
		return type;
	}

	public int getWidth()
	{
		return width;
	}

	public void setWidth(int width)
	{
		this.width = width;
		this.xCenter = this.xLeft + width / 2.0;
	}

	public int getHeight()
	{
		return height;
	}

	public void setHeight(int height)
	{
		this.height = height;
		this.yCenter = this.yTop + height / 2.0;
	}

	public double getXLeft()
	{
		return xLeft;
	}

	public void setXLeft(double xLeft)
	{
		this.xLeft = xLeft;
		this.xCenter = xLeft + this.width / 2.0;
	}

	public double getXCenter()
	{
		return xCenter;
	}

	public void setXCenter(double xCenter)
	{
		this.xCenter = xCenter;
		this.xLeft = this.width / 2.0 - xCenter;
	}

	public double getYTop()
	{
		return yTop;
	}

	public void setYTop(double yTop)
	{
		this.yTop = yTop;
		this.yCenter = yTop + this.height / 2.0;
	}

	public double getYCenter()
	{
		return yCenter;
	}

	public void setYCenter(double yCenter)
	{
		this.yCenter = yCenter;
		this.yTop = this.height / 2.0 - yCenter;
	}

	public HashSet<OrganizerLink> getChildLinks()
	{
		return childLinks;
	}

	public HashSet<OrganizerLink> getParentLinks()
	{
		return parentLinks;
	}

	public HashSet<OrganizerLink> getSiblingLinks()
	{
		return siblingLinks;
	}

	public HashSet<LinkedBox> getChildBoxes()
	{
		return childBoxes;
	}

	public HashSet<LinkedBox> getParentBoxes()
	{
		return parentBoxes;
	}

	public HashSet<LinkedBox> getSiblingBoxes()
	{
		return siblingBoxes;
	}

	public HashSet<LinkedBox> getThisAndExtendedSiblings()
	{
		return findThisAndExtendedSiblings(this, new HashSet<LinkedBox>());
	}

	// intialize currentBox as this and accumulated should be empty; RECURSIVE
	private HashSet<LinkedBox> findThisAndExtendedSiblings(LinkedBox currentBox, HashSet<LinkedBox> accumulated)
	{
		//Logger.log("Entered findThisAndExtendedSiblings", Logger.DEBUG);
		if (!accumulated.contains(currentBox))
		{
			accumulated.add(currentBox);
			for (LinkedBox siblingBox : currentBox.getSiblingBoxes())
			{
				accumulated = findThisAndExtendedSiblings(siblingBox, accumulated);
			}
		}

		return accumulated;
	}

	// I naturally avoid duplicates in all add methods by implementing this with HashSet.  Takes care of updating boxes too.
	public void addChildLink(OrganizerLink link)
	{
		this.childLinks.add(link);
		this.childBoxes.add(link.getEndBox());
	}

	public void addParentLink(OrganizerLink link)
	{

		this.parentLinks.add(link);
		this.parentBoxes.add(link.getStartBox());
	}

	public void addSiblingLink(OrganizerLink link)
	{
		this.siblingLinks.add(link);
		if (link.getStartBox().equals(this))
		{
			this.siblingBoxes.add(link.getEndBox());
		}
		else
		{
			this.siblingBoxes.add(link.getStartBox());
		}
	}

	public void removeChildLink(OrganizerLink link)
	{
		this.childLinks.remove(link);
		this.childBoxes.remove(link.getEndBox());
	}

	public void removeParentLink(OrganizerLink link)
	{
		this.parentLinks.remove(link);
		this.parentBoxes.remove(link.getStartBox());
	}

	public void removeSiblingLink(OrganizerLink link)
	{
		this.siblingLinks.remove(link);
		if (link.getStartBox().equals(this))
		{
			this.siblingBoxes.remove(link.getEndBox());
		}
		else
		{
			this.siblingBoxes.remove(link.getStartBox());
		}
	}

	public int getNumChildren()
	{
		return childLinks.size();
	}

	public int getNumParents()
	{
		return parentLinks.size();
	}

	public int getNumSiblings()
	{
		return siblingLinks.size();
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

	/**
	 *	Gets rid of the traces of itself in children/parents/siblings by removing the links to itself from those boxes.
	 *	Will be key for removal from map/model later on.
	 */
	public void removeLinksToSelf()
	{
		HashSet<OrganizerLink> childLinks = this.getChildLinks();
		HashSet<OrganizerLink> parentLinks = this.getParentLinks();
		HashSet<OrganizerLink> siblingLinks = this.getSiblingLinks();

		// Remove references to self in children
		for (OrganizerLink childLink : childLinks)
		{
			childLink.getEndBox().removeParentLink(childLink);
		}

		// Remove references to self in parents
		for (OrganizerLink parentLink : parentLinks)
		{
			parentLink.getStartBox().removeChildLink(parentLink);
		}

		// Remove references to self in siblings
		for (OrganizerLink siblingLink : siblingLinks)
		{
			if (siblingLink.getStartBox().equals(this)) 
			{
				siblingLink.getEndBox().removeSiblingLink(siblingLink);
			}
			else if (siblingLink.getEndBox().equals(this)) 
			{
				siblingLink.getStartBox().removeSiblingLink(siblingLink);
			}
		}
	}

	public boolean hasChildLinkWith(LinkedBox other)
	{
		if (this.getChildBoxes().contains(other))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public boolean hasParentLinkWith(LinkedBox other)
	{
		if (this.getParentBoxes().contains(other))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public boolean hasSiblingLinkWith(LinkedBox other)
	{
		if (this.getSiblingBoxes().contains(other))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 *	ExtendedSiblingLinks reference checking for connections via siblings, siblings of siblings, and so on
	 *	@param boxToFind - The box with which we are checking this instance for an extended sibling connection
	 *	@return true if an extended sibling connection exists, false if not
	 */
	public boolean hasExtendedSiblingLinkWith(LinkedBox boxToFind)
	{
		boolean foundExtendedSibling = false;

		if (this.hasSiblingLinkWith(boxToFind))
		{
			foundExtendedSibling = true;
		}
		else
		{
			foundExtendedSibling = extendedSiblingRecursive(this, boxToFind, new VisitedAndBoolHolder()).getFound();
		}

		return foundExtendedSibling;
	}

	/**
	 *	Holds the visited boxes and whether or not an extended sibling has been found, data accumulated in a recursive method.
	 */
	class VisitedAndBoolHolder
	{
		private HashSet<LinkedBox> visited;
		private boolean foundExtendedSibling;

		public VisitedAndBoolHolder()
		{
			visited = new HashSet<LinkedBox>();
			foundExtendedSibling = false;
		}

		public void addVisited(LinkedBox box)
		{
			visited.add(box);
		}

		public void setFound(boolean found)
		{
			foundExtendedSibling = found;
		}

		public HashSet<LinkedBox> getVisited()
		{
			return visited;
		}

		public boolean getFound()
		{
			return foundExtendedSibling;
		}
	}

	/*
	 *	Recursive helper for hasExtendedSiblingLinkWith
	 *	@param box - the Box we are checking to see if it is the one we are searching for
	 *	@param BOX_TO_FIND - The box we are searching for
	 *	@param visited - The accumuated set of visited boxes, should be intiliazed as empty
	 *	@return true if match, false if not
	 */
	private VisitedAndBoolHolder extendedSiblingRecursive(LinkedBox box, LinkedBox BOX_TO_FIND, VisitedAndBoolHolder holder)
	{
		//Logger.log("Entered extendedSiblingRecursive", Logger.DEBUG);

		if (!holder.getVisited().contains(box))
		{
			holder.addVisited(box);
			if (box.equals(BOX_TO_FIND))
			{
				holder.setFound(true);
				return holder;
			}
			else
			{
				HashSet<LinkedBox> siblingBoxes = box.getSiblingBoxes();
				for (LinkedBox siblingBox : siblingBoxes)
				{
					if (extendedSiblingRecursive(siblingBox, BOX_TO_FIND, holder).getFound())
					{
						return holder;
					}
				}
			}
		}
		
		return holder;
	}

	/**
	 *	A nonChild link is a link that is only either a parent link or a sibling link
	 *	@param other - the box to check for the link
	 *	@return true if the link exists, else false
	 */
	public boolean hasNonChildLinkWith(LinkedBox other)
	{
		if (this.hasParentLinkWith(other) || this.hasSiblingLinkWith(other))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public boolean hasLinkWith(LinkedBox other)
	{
		if (this.hasChildLinkWith(other) || this.hasNonChildLinkWith(other))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	// Box IDs are unique and final, so this is all we need for equality for now.
	@Override
	public boolean equals(Object object)
	{
		if (object instanceof LinkedBox)
		{
			LinkedBox otherBox = (LinkedBox) object;
			if (this.boxID == otherBox.getBoxID() || this.rootID == otherBox.getRootID())
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

	// Necessary override since we're using HashMaps and HashSets in our code.  BoxIDs are unique, so this guarantees no collisions.
	@Override
	public int hashCode()
	{
		return this.boxID;
	}

	// Just outputs the box's boxID and rootID, not its children and parents and siblings
	public String toStringShort()
	{
		StringBuilder buffer = new StringBuilder("\n\t\t\t\tBox RootID: " + Integer.toString(rootID) + "; Box ID " + Integer.toString(boxID) + "; Box Type: " + type);
		return buffer.toString();
	}

	/**
	 *	Outputs this box's identification numbers as well as the ID's of all its children/parents/siblings
	 */
	@Override
	public String toString()
	{
		StringBuilder buffer = new StringBuilder("\n\t\tBEGIN BOX\n\t\tBox RootID: " + Integer.toString(rootID) + "; Box ID " + Integer.toString(boxID) + "; Box Type: " + type + "; xCenter: " + xCenter + "; yCenter: " + yCenter);
		buffer.append("\n\t\t\tCHILD BOXES...");
		for (OrganizerLink childLink : childLinks)
		{
			buffer.append(childLink.getEndBox().toStringShort());
		}
		buffer.append("\n\t\t\tPARENT BOXES...");
		for (OrganizerLink parentLink : parentLinks)
		{
			buffer.append(parentLink.getStartBox().toStringShort());
		}
		buffer.append("\n\t\t\tSIBLING BOXES...");
		for (OrganizerLink siblingLink : siblingLinks)
		{
			if (siblingLink.getEndBox().equals(this))
			{
				buffer.append(siblingLink.getStartBox().toStringShort());
			}
			else if (siblingLink.getStartBox().equals(this))
			{
				buffer.append(siblingLink.getEndBox().toStringShort());
			}
		}
		buffer.append("\n\t\tEND BOX\n");
		return buffer.toString();
	}

	// Needs a better name, do that tomorrow
	public boolean okayForLink(LinkedBox endBox)
	{
		HashSet<LinkedBox> startBoxAndExtSibs = this.getThisAndExtendedSiblings();
		for (LinkedBox startBox : startBoxAndExtSibs)
		{
			if (endBox.hasChildLinkWith(startBox))
			{
				return false;
			}
		}
		return true;
	}
}