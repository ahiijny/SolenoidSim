package main.shapes;
import java.awt.Color;

public abstract class Entity 
{
	public Color color;
	
	public Entity() 
	{
	}
	
	public abstract double[][] getPoints();
}
