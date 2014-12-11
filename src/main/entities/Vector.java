package main.entities;

import java.awt.Color;

import main.Calc;
import main.Matrix;

public class Vector extends Line 
{
	public static double arrowHeadLength1 = 0.4;
	public static double arrowHeadLength2 = 0.4;
	public static double arrowHeadThreshold = 0.5;
	public static double arrowHeadAngle = Math.toRadians(45);
	public Line arrowHead1 = new Line();
	public Line arrowHead2 = new Line();
	public double[] value;
	
	public Vector(double[] position, double[] direction, double scale) 
	{
		this(position, direction, scale, new Color((float)Math.random(), (float)Math.random(), (float)Math.random()));
	}
	
	public Vector(double[] position, double[] direction, double scale, Color color) 
	{
		this.origin = position;
		this.value = direction;
		this.direction = Calc.unit(direction);
		this.t1 = 0;
		this.color = color;
		setScale(scale); 	
		setArrowHeads();
	}
	
	public void setScale(double scale)
	{
		t2 = Calc.mag(value) * scale;
		double[] r = Calc.add(origin, Calc.scale(direction, t2));
		arrowHead1.origin = r;
		arrowHead2.origin = r;
	}
	
	private void setArrowHeads()
	{
		double[] r = {0, t2, 0};
		double[] m1 = {1, -4, 0};
		double[] m2 = {-1, -4, 0};
		double[][] R = Matrix.getRotationMatrix(r, direction);
		r = Matrix.multiply(R, r);
		m1 = Calc.unit(Matrix.multiply(R, m1));
		m2 = Calc.unit(Matrix.multiply(R, m2));
		
		arrowHead1 = new Line(r, m1, 0, arrowHeadLength1, color);
		arrowHead2 = new Line(r, m2, 0, arrowHeadLength2, color);
	}		
	
	public void setColorScale(double maxValue, double lowerClip, double higherClip)
	{
		this.color = decimalToHue(t2/maxValue, lowerClip, higherClip);		
	}
	
	@Override
	public double[][] getPoints(double ds)
	{
		if (t2 > arrowHeadThreshold)
		{
			// Return points from vector body and 2 arrowheads
			
			int n1 = getPointCount(ds);
			int n2 = arrowHead1.getPointCount(ds);
			int n3 = arrowHead2.getPointCount(ds);
			int n = n1 + n2 + n3;
			double[][] points = new double[n][3];
			int counter = 0;
			
			// Compute points
			
			for (int i = 0; i < n1; i++)
				points[counter++] = Calc.add(origin, Calc.scale(direction, t1 + i * ds));
			for (int i = 0; i < n2; i++)
				points[counter++] = Calc.add(arrowHead1.origin, Calc.scale(arrowHead1.direction, arrowHead1.t1 + i * ds));
			for (int i = 0; i < n3; i++)
				points[counter++] = Calc.add(arrowHead2.origin, Calc.scale(arrowHead2.direction, arrowHead2.t1 + i * ds));
			
			return points;
		}
		else
			return super.getPoints(ds);
	}
	
	public static Color decimalToHue(double decimal, double lowerClip, double higherClip)
	{
		if (decimal < lowerClip)
			decimal = lowerClip;
		else if (decimal > higherClip)
			decimal = higherClip;
		
		// Convert to "in"
		final int total = (255 * 6 + 143);
        int in = (int) Math.round (decimal * total);
        int R, G, B;


        // Process
        // Red
        if (in >= 255 &&
                in <= 510)
        {
            R = 255;
        }
        else if (in == 1418)
        {
            R = 143;
        }

        else
        {
            R = 0;
        }

        // Green
        if (in >= 510 &&
                in <= 1020)
        {
            G = 255;
        }
        else
        {
            G = 0;
        }

        // Blue
        if (in >= 1020 &&
                in <= 1418)
        {
            B = 255;
        }
        else
        {
            B = 0;
        }

        // Remainder
        if (in >= 0 &&
                in < 255)
        {
            R = in;
        }

        if (in >= 255 &&
                in < 510)
        {
            G = in - 255;
        }

        if (in >= 510 &&
                in < 765)
        {
            R = 255 - in + 510;
        }

        if (in >= 765 &&
                in < 1020)
        {
            B = in - 765;
        }

        if (in >= 1020 &&
                in < 1275)
        {
            G = 255 - in + 1020;
        }

        if (in >= 1275 &&
                in < 1418)
        {
            R = in - 1275;
        }

        if (in >= 1418 &&
                in < 1561)
        {
            R = 143 - in + 1418;
            B = 255 - in + 1418;
        }

        if (in >= 1561 &&
                in < 1673)
        {
            B = 112 - in + 1561;
        }
        
        return new Color(R, G, B);
	}

}
