package main.entities;
import java.awt.Color;

public abstract class Entity 
{
	public Color color;
	
	public abstract double[][] getPoints(double ds);
	
	public abstract int getPointCount(double ds);
}
