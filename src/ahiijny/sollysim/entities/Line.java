package ahiijny.sollysim.entities;

import java.awt.Color;

import ahiijny.sollysim.Calc;


public class Line extends Entity
{
	public double[] origin = {0, 0, 0};
	public double[] direction = {0, 0, 0};
	public double t1;
	public double t2;
	
	public Line()
	{
		t1 = 0;
		t2 = 0;
	}
	
	public Line(double[] origin, double[] direction, double length)
	{
		this(origin, Calc.unit(direction), 0, length);
	}
	
	public Line(double[] origin, double[] direction, double t1, double t2)
	{
		this(origin, direction, t1, t2, Color.black);
	}
	
	public Line(double[] origin, double[] direction, double t1, double t2, Color color) 
	{
		this.origin = Calc.copy(origin);
		this.direction = Calc.copy(direction);
		this.t1 = t1;
		this.t2 = t2;
		this.color = color;
		this.plotStepScalar = 1;
	}
		
	public double[][] getPoints(double ds)
	{
		double[][] points = new double[(int)((t2-t1)/ds)][3];
		
		for (int i = 0; i < points.length; i++)
		{
			points[i] = Calc.add(origin, Calc.scale(direction, t1 + i * ds));
		}
		
		return points;
	}
	
	public int getPointCount(double ds)
	{
		return (int)(plotStepScalar * (t2-t1)/ds);
	}
}
