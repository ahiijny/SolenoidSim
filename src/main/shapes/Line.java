package main.shapes;

import java.awt.Color;

import main.Calc;


public class Line extends Entity
{
	public double[] origin;
	public double[] direction;
	public double ds;
	public double t1;
	public double t2;
	
	public Line()
	{
		ds = 1;
		t1 = 0;
		t2 = 0;
	}
	
	public Line(double[] origin, double[] direction, double ds, double t1, double t2)
	{
		this(origin, direction, ds, t1, t2, Color.black);
	}

	public Line(double[] origin, double[] direction, double ds, double t1, double t2, Color color) 
	{
		this.origin = Calc.copy(origin);
		this.direction = Calc.copy(direction);
		this.ds = ds;
		this.t1 = t1;
		this.t2 = t2;
		this.color = color;
	}
	
	@Override
	public double[][] getPoints()
	{
		double[][] points = new double[(int)((t2-t1)/ds)][3];
		
		for (int i = 0; i < points.length; i++)
		{
			points[i] = Calc.add(origin, Calc.scale(direction, t1 + i * ds));
		}
		
		return points;
	}
	
	public int getPointCount()
	{
		return (int)((t2-t1)/ds);
	}
}
