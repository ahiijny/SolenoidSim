package main.entities;

import java.awt.Color;

import main.Calc;

public class StraightWire extends Line implements Wire 
{	
	public double current;

	public StraightWire(double current, double[] origin, double[] direction, double length)
	{
		super(origin, Calc.unit(direction), 0, length);
		this.current = current;
	}

	public StraightWire(double current, double[] origin, double[] direction, double t1,	double t2) 
	{
		super(origin, direction, t1, t2);
		this.current = current;
	}

	public StraightWire(double current, double[] origin, double[] direction, double t1,	double t2, Color color) 
	{
		super(origin, direction, t1, t2, color);
		this.current = current;
	}

	@Override
	public double current() 
	{
		return current;
	}

	@Override
	public double[] diff_s(double t, double[] vars) 
	{
		return direction;
	}

	@Override
	public double[] get_s(double t) 
	{
		return Calc.add(origin, Calc.scale(direction, t));
	}

	@Override
	public double get_t1() 
	{
		return t1;
	}

	@Override
	public double get_t2() 
	{
		return t2;
	}

	@Override
	public double[] get_position() 
	{
		return get_s(0);
	}

	@Override
	public double[] get_direction() 
	{
		return direction;
	}


}
