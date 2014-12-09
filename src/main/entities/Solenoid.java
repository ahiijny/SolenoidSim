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
	public boolean cached = false;

	public Solenoid(double current, double[] origin, double[] direction, double radius, double height, double turns) 
	{
		this.origin = origin;
		this.direction = Calc.unit(direction);
		this.radius = radius;
		this.height = height;		
		this.turns = turns;
		this.color = Color.black;
		this.current = current;
		initRotationMatrix();
	}
	
	/** Find the rotation matrix that will rotate the direction vector
	 * from the default of (0,0,1) to the specified direction vector. If
	 * the direction vector is parallel to (0,0,1), then returns the
	 * identity matrix.
	 * 
	 * http://math.stackexchange.com/questions/180418/calculate-rotation-matrix-to-align-vector-a-to-vector-b-in-3d
	 */
	private void initRotationMatrix()
	{
		// Find the cross product.
		double[] k = {0,0,1};
		double[] u = Calc.cross(k, direction);
		
		// Find sin
		double sin = Calc.mag(u);
			
		// Find cos
		double cos = Calc.dot(k, direction);
		
		if (sin != 0)
		{				
			// Find the rotation matrix
			
			double[][] VX = Matrix.getSkewSymmetricCrossProductMatrix(u);
			double[][] VXsq = Matrix.multiply(VX, VX);
			double[][] I = Matrix.getIdentityMatrix(3);
			
			rotation = Matrix.add(Matrix.add(I, VX), Matrix.scale(VXsq, (1 - cos)/(sin*sin)));
		}
		else
		{
			rotation = Matrix.scale(Matrix.getIdentityMatrix(3), Math.signum(cos));
		}
	}

	public double[][] getPoints(double ds) 
	{
		if (!cached)
		{
			ds /= (2* Math.PI * radius);
			int n = getPointCount(ds);
			double[][] points = new double[n][3];
			
			for (int i = 0; i < n; i++)
				points[i] = get_s(i * ds);
			vertices = points;
		}		
		return vertices;
	}
	
	public int getPointCount(double ds)
	{
		return (int)(get_t2()/ds);
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

}
