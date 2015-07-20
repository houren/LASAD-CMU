package lasad.gwt.client.model.organization;

public class Coordinate
{
	private int x;
	private int y;

	public Coordinate()
	{
		x = 0;
		y = 0;
	}

	public Coordinate(int x, int y)
	{
		this.setX(x);
		this.setY(y);
	}

	public void setX(int x)
	{
		this.x = x;
	}

	public void setY(int y)
	{
		this.y = y;
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	@Override
	public boolean equals(Object o)
	{
		if (o instanceof Coordinate)
		{
			Coordinate oAsCoord = (Coordinate) o;
			if (oAsCoord.getX() == this.getX() && oAsCoord.getY() == this.getY())
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
}