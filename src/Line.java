
public class Line 
{
	public double[] origin;
	public double[] direction;

	public Line(double[] origin, double[] direction) 
	{
		this.origin = Calc.copy(origin);
		this.direction = Calc.copy(direction);
	}
	
	public double[][] getPoints(double ds, double t1, double t2)
	{
		double[][] points = new double[(int)((t2-t1)/ds)][3];
		
		for (int i = 0; i < points.length; i++)
		{
			points[i] = Calc.add(origin, Calc.scale(direction, t1 + i * ds));
		}
		
		return points;
	}

}
