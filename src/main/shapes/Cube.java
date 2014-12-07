package main.shapes;

import java.awt.Color;
import java.util.ArrayList;

public class Cube extends Entity
{
	public ArrayList<Line> lines;
	
	public Cube(double[] center, double width, Color color)
	{
		this(center, width, 0.5, color);
	}
	
	public Cube(double[] center, double width, double dt, Color color) 
	{
		this.color = color;
		lines = new ArrayList<Line>(12);
		
		double[] r1 = {center[0] - width/2, center[1] + width/2, center[2] + width/2};
		double[] r2 = {r1[0] + width, r1[1] - width, r1[2]};
		double[] r3 = {r1[0], r1[1] - width, r1[2] - width};
		double[] r4 = {r1[0] + width, r1[1], r1[2] - width};
		double[] m1 = {1, 0, 0};
		double[] m2 = {0, 1, 0};
		double[] m3 = {0, 0, 1};
		lines.add(new Line(r1, m1, dt, 0, width));
		lines.add(new Line(r1, m2, dt, -width, 0));
		lines.add(new Line(r1, m3, dt, -width, 0));
		lines.add(new Line(r2, m1, dt, -width, 0));
		lines.add(new Line(r2, m2, dt, 0, width));
		lines.add(new Line(r2, m3, dt, -width, 0));
		lines.add(new Line(r3, m1, dt, 0, width));
		lines.add(new Line(r3, m2, dt, 0, width));
		lines.add(new Line(r3, m3, dt, 0, width));
		lines.add(new Line(r4, m1, dt, -width, 0));
		lines.add(new Line(r4, m2, dt, -width, 0));
		lines.add(new Line(r4, m3, dt, 0, width));		
	}

	@Override
	public double[][] getPoints() 
	{
		int counter = 0;
		int n = 0;		
		
		for (int i = 0; i < lines.size(); i++)
			n += lines.get(i).getPointCount();
		
		double[][] points = new double[n][3];
		
		for (int i = 0; i < lines.size(); i++)
		{
			double[][] temp = lines.get(i).getPoints();
			for (int j = 0; j < temp.length; j++)
				points[counter++] = temp[j];
		}
		
		return points;
	}
}
