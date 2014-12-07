package main;
import java.util.ArrayList;
import main.shapes.Entity;

public class Sim 
{
	public ArrayList<Entity> objects;
	
	public Sim() 
	{
		objects = new ArrayList<Entity>();
	}
	
	public void add(Entity e)
	{
		objects.add(e);
	}
	
	public boolean remove(Entity e)
	{
		return objects.remove(e);
	}
}
