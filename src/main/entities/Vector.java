package main.entities;

import java.awt.Color;

import main.Calc;

public class Vector extends Line 
{
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
	}
	
	public void setScale(double scale)
	{
		t2 = Calc.mag(value) * scale;
	}
	
	public void setColorScale(double colorScale, double lowerClip, double higherClip)
	{
		this.color = decimalToHue(t2/colorScale, lowerClip, higherClip);		
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
