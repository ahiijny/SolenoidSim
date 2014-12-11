package main.entities;

import java.awt.Color;

import main.Calc;
import main.Matrix;

public class Solenoid extends Entity implements Wire
{
	public double[] origin;
	public double[] direction;
	public double current;
	public double radius;
	public double height;
	public double turns;	
	
	public double[][] rotation;
	public double[][] vertices;
	public double cached_ds = 0;
	
	public Solenoid()
	{
		this(0, new double[] {0, 0, 0}, new double[] {0, 1, 0}, 0.5, 10, 5);
	}

	public Solenoid(double current, double[] origin, double[] direction, double radius, double height, double turns) 
	{
		this.origin = origin;
		this.direction = Calc.unit(direction);
		this.radius = radius;
		this.height = height;		
		this.turns = turns;
		this.color = Color.black;
		this.current = current;
		updateRotationMatrix();
	}
	
	/** Find the rotation matrix that will rotate the direction vector
	 * from the default of (0,0,1) to the specified direction vector. If
	 * the direction vector is parallel to (0,0,1), then returns the
	 * identity matrix.
	 * 
	 * http://math.stackexchange.com/questions/180418/calculate-rotation-matrix-to-align-vector-a-to-vector-b-in-3d
	 */
	public void updateRotationMatrix()
	{
		// Find the cross product.
		double[] k = {0,0,1};
		rotation = Matrix.getRotationMatrix(k, direction);
	}

	public double[][] getPoints(double ds) 
	{
		if (ds != cached_ds)
		{
			cached_ds = ds;						
			int n = getPointCount(ds);
			ds = get_t2()/n;
			double[][] points = new double[n][3];
			
			for (int i = 0; i < n; i++)
				points[i] = get_s(i * ds);
			vertices = points;
		}		
		return vertices;
	}
	
	public int getPointCount(double ds)
	{
		return (int)((2* Math.PI * radius * turns + height)/ds);
	}

	@Override
	public double current() 
	{
		return current;
	}

	@Override
	public double[] diff_s(double t, double[] vars)
	{
		double dx = -radius * Math.sin(t);
		double dy = radius * Math.cos(t);
		double dz = height/(2 * Math.PI * turns);
		
		double[] ds = {dx, dy, dz};
		ds = Matrix.multiply(rotation, ds);
		
		return ds;
	}

	@Override
	public double[] get_s(double t) 
	{
		double x = radius * Math.cos(t);
		double y = radius * Math.sin(t);
		double z = height/(2 * Math.PI * turns) * t;
		
		double[] s = {x, y, z};
		s = Matrix.multiply(rotation, s);
		s = Calc.add(s, origin);
		
		return s;
	}

	@Override
	public double get_t1() 
	{
		return 0;
	}

	@Override
	public double get_t2() 
	{
		return 2 * Math.PI * turns;
	}

	@Override
	public double[] get_position() 
	{
		return origin;
	}

	@Override
	public double[] get_direction() 
	{
		return direction;
	}
	
	/** Nulls cached plot points of shape, forcing
	 * the program to regenerate these points from scratch. 
	 */
	public void resetCache()
	{
		cached_ds = 0;
	}

}
