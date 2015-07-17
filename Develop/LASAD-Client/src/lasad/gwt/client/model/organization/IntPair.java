package lasad.gwt.client.model.organization;

public class IntPair
{
	int min;
	int max;

	public IntPair(int min, int max)
	{
		this.min = min;
		this.max = max;
	}

	public int getMin()
	{
		return min;
	}

	public int getMax()
	{
		return max;
	}

	public int calcRoot(boolean isOrganizeTopToBottom)
	{
		if (isOrganizeTopToBottom)
		{
			return max;
		}
		else
		{
			return min;
		}
	}
}