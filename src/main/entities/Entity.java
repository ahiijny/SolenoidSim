package main.entities;
import java.awt.Color;

public abstract class Entity 
{
	public Color color;
	public double plotStepScalar;
	
	public abstract double[][] getPoints(double ds);
	
	public abstract int getPointCount(double ds);
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " " + Integer.toHexString(hashCode());
	}
}
