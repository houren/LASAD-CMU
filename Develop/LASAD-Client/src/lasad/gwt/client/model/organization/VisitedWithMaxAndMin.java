package lasad.gwt.client.model.organization;

import java.util.HashSet;

public class VisitedWithMaxAndMin
{
	private int max;
	private int min;
	private HashSet<LinkedBox> visited;

	public VisitedWithMaxAndMin()
	{
		max = Integer.MIN_VALUE;
		min = Integer.MAX_VALUE;
		visited = new HashSet<LinkedBox>();
	}

	public VisitedWithMaxAndMin(HashSet<LinkedBox> visited)
	{
		this();
		this.setVisited(visited);
	}

	public void setMin(int min)
	{
		if (min < this.min)
		{
			this.min = min;
		}
	}

	public void setMax(int max)
	{
		if (max > this.max)
		{
			this.max = max;
		}
	}

	public int getMin()
	{
		return min;
	}

	public int getMax()
	{
		return max;
	}

	public void addVisited(LinkedBox box)
	{
		visited.add(box);
	}

	public void setVisited(HashSet<LinkedBox> visited)
	{
		this.visited = visited;
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