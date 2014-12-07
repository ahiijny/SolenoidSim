package main;
import java.awt.Color;


public class Calc 
{			
	public static double[] cross(double[] a, double[] b)
	{
		double[] c = new double[3];
		c[0] = a[1]*b[2] - a[2]*b[1];
		c[1] = -(a[0]*b[2] - a[2]*b[0]);
		c[2] = a[0]*b[1] - a[1]*b[0];
		return c;
	}
	
	public static double[] scale(double[] a, double scalar)
	{
		double c[] = new double[a.length];
		for (int i = 0; i < a.length; i++)
			c[i] = a[i] * scalar;
		return c;
	}
	
	public static double[] add(double[] a, double[] b)
	{
		double c[] = new double[a.length];
		for (int i = 0; i < a.length; i++)
			c[i] = a[i] + b[i];
		return c;
	}
	
	public static double dot(double[] a, double[] b)
	{
		return a[0]*b[0] + a[1]*b[1] + a[2]*b[2];
	}
	
	public static double mag(double[] a)
	{
		double m = a[0]*a[0] + a[1]*a[1] + a[2]*a[2];
		m = Math.sqrt(m);
		return m;
	}
	
	public static double sqmag(double[] a)
	{
		return a[0]*a[0] + a[1]*a[1] + a[2]*a[2];
	}
	
	public static double acosh(double a)
	{
		return Math.log(a + Math.sqrt(a+1) * Math.sqrt(a-1));
	}
	
	public static double[] unit(double[] a)
	{
		double mag = mag(a);
		if (mag != 0)
			return scale(a, 1/mag);
		else
			return copy(a);
	}
	
	public static double[] copy(double[] a)
	{
		double[] c = new double[a.length];
		for (int i = 0; i < a.length; i++)
			c[i] = a[i];
		return c;		
	}
	
	public static double[] empty(double[] a)
	{
		for (int i = 0; i < a.length; i++)
			a[i] = 0;
		return a;
	}
	
	public static double getInc(double[] a)
	{
		double Inc = Math.asin(a[1]/mag(a));
		return Inc;
	}
	
	public static double getLAN(double[] a)
	{
		double LAN = Math.atan2(a[2],a[0]);
		return LAN;
	}
	
	public static void println(double[] a)
	{
		for (int i = 0; i < a.length - 1; i++)
			System.out.print(a[i] + ",");
		System.out.println(a[a.length-1]);
	}
	
	public static Color average(Color col1, double weight1, Color col2, double weight2)
	{
		double total = weight1 + weight2;
		weight1 /= total;
		weight2 /= total;
		int r1 = col1.getRed();
		int g1 = col1.getGreen();
		int b1 = col1.getBlue();
		int r2 = col2.getRed();
		int g2 = col2.getGreen();
		int b2 = col2.getBlue();
		
		int r = (int)(weight1 * r1 + weight2 * r2 + 0.5);
		int g = (int)(weight1 * g1 + weight2 * g2 + 0.5);
		int b = (int)(weight1 * b1 + weight2 * b2 + 0.5);
		
		return new Color(r, g, b);
	}
}
