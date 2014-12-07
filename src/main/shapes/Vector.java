package main.shapes;

import java.awt.Color;

import main.Calc;

public class Vector extends Line 
{
	public double[] value;
	
	public Vector(double[] position, double[] direction, double scale, double ds) 
	{
		this(position, direction, scale, ds, new Color((float)Math.random(), (float)Math.random(), (float)Math.random()));
	}
	
	public Vector(double[] position, double[] direction, double scale, double ds, Color color) 
	{
		this.origin = position;
		this.value = direction;
		this.direction = Calc.unit(direction);
		this.ds = ds;
		this.t1 = 0;
		this.color = color;
		setScale(scale); 		
	}
	
	public void setScale(double scale)
	{
		t2 = Calc.mag(value) * scale;
	}

}
