package main;

import java.text.DecimalFormat;

/** Static class that performs various math operations. For succinctness,
 * does not verify inputs. If the programmer inputs a vector of the wrong
 * dimension into a method, they'll get a nice ArrayIndexOutOfBoundsException.
 * All vectors in this class are taken as arrays of doubles, usually
 * of dimension R3 (e.g. requirement for the cross product).
 * 
 * @author Jiayin
 */
public class Calc 
{			
	public static DecimalFormat large = new DecimalFormat("0.000E0");
	public static DecimalFormat small = new DecimalFormat("0.00");
	public static DecimalFormat precise = new DecimalFormat("0.############");
	
	/** Returns the cross product of a and b. Must be in R3.
	 * 
	 * @param a 	the first vector
	 * @param b		the second vector
	 * @return	a x b
	 */
	public static double[] cross(double[] a, double[] b)
	{
		double[] c = new double[3];
		c[0] = a[1]*b[2] - a[2]*b[1];
		c[1] = -(a[0]*b[2] - a[2]*b[0]);
		c[2] = a[0]*b[1] - a[1]*b[0];
		return c;
	}
	
	/** Multiplies the given vector by the given scalar.
	 * 
	 * @param a			the vector
	 * @param scalar	the scalar
	 * @return the scaled vector.
	 */
	public static double[] scale(double[] a, double scalar)
	{
		double c[] = new double[a.length];
		for (int i = 0; i < a.length; i++)
			c[i] = a[i] * scalar;
		return c;
	}
	
	/** Adds the two vectors together.
	 * 
	 * @param a		the first vector
	 * @param b		the second vector
	 * @return the sum of a and b.
	 */
	public static double[] add(double[] a, double[] b)
	{
		double c[] = new double[a.length];
		for (int i = 0; i < a.length; i++)
			c[i] = a[i] + b[i];
		return c;
	}
	
	/** Returns the dot product of a and b. Must be in R3. 
	 * 
	 * @param a		the first vector
	 * @param b		the second vector
	 * @return a &sdot; b
	 */
	public static double dot(double[] a, double[] b)
	{
		return a[0]*b[0] + a[1]*b[1] + a[2]*b[2];
	}
	
	/** Returns the magnitude of the given R3 vector.
	 * 
	 * @param a		the vector
	 * @return the magnitude (length) of the vector
	 */
	public static double mag(double[] a)
	{
		double m = a[0]*a[0] + a[1]*a[1] + a[2]*a[2];
		m = Math.sqrt(m);
		return m;
	}
	
	/** Returns the square of the magnitude of the given R3 vector.
	 * 
	 * @param a		the vector
	 * @return the vector magnitude squared
	 */
	public static double sqmag(double[] a)
	{
		return a[0]*a[0] + a[1]*a[1] + a[2]*a[2];
	}
	
	/** Scales the given vector such that it is a unit vector pointing
	 * in the same direction as the given vector. If the given vector
	 * has magnitude zero, does nothing and returns the given vector.
	 * 
	 * @param a		the vector
	 * @return unit vector if magnitude not 0; the given vector otherwise
	 */
	public static double[] unit(double[] a)
	{
		double mag = mag(a);
		if (mag != 0)
			return scale(a, 1/mag);
		else
			return copy(a);
	}
	
	/** Returns a copy of the given vector. The returned
	 * vector is a reference to a new object separate from
	 * the given vector. Changing one will not change the other.
	 * 
	 * @param a		the vector
	 * @return a copy of the given vector
	 */
	public static double[] copy(double[] a)
	{
		double[] c = new double[a.length];
		for (int i = 0; i < a.length; i++)
			c[i] = a[i];
		return c;		
	}
	
	/** Sets all of the components of the given vector to 0.
	 * 
	 * @param a		the vector
	 * @return the given vector, but zeroed
	 */
	public static double[] empty(double[] a)
	{
		for (int i = 0; i < a.length; i++)
			a[i] = 0;
		return a;
	}
	
	/** For debugging. Prints the given vector. And then a line break.
	 * 
	 * @param a		the vector
	 */
	public static void println(double[] a)
	{
		for (int i = 0; i < a.length - 1; i++)
			System.out.print(a[i] + ",");
		System.out.println(a[a.length-1]);
	}
}
