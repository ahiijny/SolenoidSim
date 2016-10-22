package ahiijny.sollysim.entities;

import java.awt.Color;
import java.util.ArrayList;

public class Cube extends Entity
{
	public ArrayList<Line> lines;
	public double width;
	
	public Cube(double[] center, double width, Color color)
	{
		this(center, width, 0.5, color);
	}
	
	public Cube(double[] center, double width, double dt, Color color) 
	{
		this.color = color;
		this.width = width;
		this.plotStepScalar = 1;
		lines = new ArrayList<Line>(12);
		
		double[] r1 = {center[0] - width/2, center[1] + width/2, center[2] + width/2};
		double[] r2 = {r1[0] + width, r1[1] - width, r1[2]};
		double[] r3 = {r1[0], r1[1] - width, r1[2] - width};
		double[] r4 = {r1[0] + width, r1[1], r1[2] - width};
		double[] m1 = {1, 0, 0};
		double[] m2 = {0, 1, 0};
		double[] m3 = {0, 0, 1};
		lines.add(new Line(r1, m1, 0, width));
		lines.add(new Line(r1, m2, -width, 0));
		lines.add(new Line(r1, m3, -width, 0));
		lines.add(new Line(r2, m1, -width, 0));
		lines.add(new Line(r2, m2, 0, width));
		lines.add(new Line(r2, m3, -width, 0));
		lines.add(new Line(r3, m1, 0, width));
		lines.add(new Line(r3, m2, 0, width));
		lines.add(new Line(r3, m3, 0, width));
		lines.add(new Line(r4, m1, -width, 0));
		lines.add(new Line(r4, m2, -width, 0));
		lines.add(new Line(r4, m3, 0, width));		
	}
	
	public double[][] getPoints(double ds) 
	{
		int counter = 0;
		int n = getPointCount(ds);
		
		double[][] points = new double[n][3];
		
		for (int i = 0; i < lines.size(); i++)
		{
			double[][] temp = lines.get(i).getPoints(ds);
			for (int j = 0; j < temp.length; j++)
				points[counter++] = temp[j];
		}
		
		return points;
	}
	
	public int getPointCount(double ds)
	{
		int n = 0;		
		
		for (int i = 0; i < lines.size(); i++)
			n += lines.get(i).getPointCount(ds);
		
		return n;
	}
}
