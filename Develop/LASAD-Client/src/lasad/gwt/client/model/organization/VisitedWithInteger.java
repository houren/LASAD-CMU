package lasad.gwt.client.model.organization;

import java.util.HashSet;

public class VisitedWithInteger
{
	private int integer;
	private HashSet<LinkedBox> visited;

	public VisitedWithInteger()
	{
		integer = Integer.MIN_VALUE;
		visited = new HashSet<LinkedBox>();
	}

	public VisitedWithInteger(HashSet<LinkedBox> visited, int integer)
	{
		this();
		this.addAllVisited(visited);
		this.setInteger(integer);
	}

	public void setInteger(int integer)
	{
		this.integer = integer;
	}

	public int getInteger()
	{
		return integer;
	}

	public void addVisited(LinkedBox box)
	{
		visited.add(box);
	}

	public void addAllVisited(HashSet<LinkedBox> visited)
	{
		this.visited.addAll(visited);
	}

	public HashSet<LinkedBox> getVisited()
	{
		return visited;
	}
}