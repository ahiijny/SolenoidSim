package main;

import java.util.ArrayList;
import java.util.Collection;

import main.entities.Entity;
import main.entities.Vector;
import main.entities.Wire;

/** BIOTSAVART
 * http://www.myphysicslab.com/runge_kutta.html
 * http://en.wikipedia.org/wiki/Runge%E2%80%93Kutta_methods
 * http://en.wikipedia.org/wiki/List_of_Runge%E2%80%93Kutta_methods
 */
public class Sim 
{
	public static final double u0 = 4 * Math.PI * 1E-7;
	public static final double defaultDt = 0.5;
	
	public static final int P_X = 0;
	public static final int P_Y = 1;
	public static final int P_Z = 2;	
	public static final int S_X = 3;
	public static final int S_Y = 4;
	public static final int S_Z = 5;
	public static final int B_X = 6;
	public static final int B_Y = 7;
	public static final int B_Z = 8;
			
	public static final int RK1 = 0;
	public static final int RK2 = 1;
	public static final int RK4 = 2;
	
	public String[] integratorLabelsShort = {"RK1", "RK2", "RK4"};
	public int[] integratorIndices = {RK1, RK2, RK4};
	
	public int integrationMethod = RK4;
	
	public double dt = 0.5;	
	public double maxB = 0;	
	public double maxScale = 5;
	
	public double minColor = 0;
	public double maxColor = 0.85;
	
	public static final double[][][] butcherTableau = 
		{
			{
				{0, 0},
				{0, 1}
			}
			,
			{
				{0,     0,     0},
				{1/2.0, 1/2.0, 0},
				{0,     0,     1}
			}
			,
			{
				{0,     0,     0,     0,     0},
				{1/2.0, 1/2.0, 0,     0,     0},
				{1/2.0, 0,     1/2.0, 0,     0},
				{1,     0,     0,     1,     0},
				{0,     1/6.0, 1/3.0, 1/3.0, 1/6.0}
			}
		}
		;	
	public ArrayList<Entity> objects;
	protected ArrayList<Entity> shapes;
	protected ArrayList<Vector> vectors;
	protected ArrayList<Wire> wires;
	
	private Wire integratingWire;
	
	public GraphicUI parent;
	public int progressInterval = 25;
	
	public String process = "";
	
	public Sim(GraphicUI parent) 
	{
		this.parent = parent;
		objects = new ArrayList<Entity>();
		shapes = new ArrayList<Entity>();
		vectors = new ArrayList<Vector>();
		wires = new ArrayList<Wire>();		
	}
	
	public void add(Entity e)
	{
		shapes.add(e);
		updateVertices();
	}
	
	public void addVector(Vector v)
	{
		vectors.add(v);
		updateVertices();
	}
	
	public void addWire(Wire w)
	{
		wires.add(w);
		updateVertices();
		parent.entitySel.addItem(w.toString());
	}		
	
	public boolean remove(Entity e)
	{
		return shapes.remove(e);
	}
	
	public boolean removeVector(Vector v)
	{
		return vectors.remove(v);
	}
	
	public boolean removeWire(Wire w)
	{
		boolean removed = wires.remove(w);
		objects.remove(w);
		parent.entitySel.removeItem(w.toString());
		return removed;
	}			
	
	public void rescaleVectors(double newMaxB, double newMaxScale)
	{
		maxB = newMaxB;
		maxScale = newMaxScale;
		for (Vector v : vectors)
		{				
			v.setColorScale(maxB, minColor, maxColor);
			v.setScale(maxScale/maxB);
		}
	}
	
	public void simulate(double[][] points)
	{
		simulate(points, true, false);
	}
	
	public void simulate(double[][] points, boolean resetVectors, boolean removePreviousProbe)
	{
		if (resetVectors)
		{
			vectors.clear();
			maxB = 0;
		}
		else
		{
			if (removePreviousProbe)
				if (vectors.size() > 0)
					vectors.remove(vectors.size() - 1);
		}
		if (points != null)
		{
			for (int i = 0; i < points.length; i++)
			{
				double[] point = points[i];				
				double[] direction = compute_field(point);
				
				Vector vector = new Vector(point, direction, 1);
				
				vectors.add(vector);
				process = (i+1) + "/" + points.length;
				if ((i+1) % progressInterval == 0 || i == points.length - 1)
					parent.updateProcess();
			}
			
			for (Vector v : vectors)
			{				
				v.setColorScale(maxB, minColor, maxColor);
				v.setScale(maxScale/maxB);
			}
		}
		updateVertices();
	}
	
	@SuppressWarnings("unchecked")
	private void updateVertices()
	{
		objects.clear();
		objects.addAll(shapes);
		objects.addAll((Collection<? extends Entity>) wires);
		objects.addAll((Collection<? extends Entity>) vectors);
	}
	
	private double[] compute_field(double[] point)
	{
		double[] B = new double[] {0, 0, 0};
		
		for (Wire w : wires)
		{
			double t1 = w.get_t1();
			double t2 = w.get_t2();
			integratingWire = w;
			
			double[] vars = getVars(t1, w, point);
			
			for (double t = t1; t < t2; t += dt)
				vars = rkn(t, dt, vars);
			
			// Multiplying by the constant here at the end
			// because it is constant throughout the integration,
			// so it doesn't need to be inside the integration.
			
			double[] wireB = new double[] {vars[B_X], vars[B_Y], vars[B_Z]};
			wireB = Calc.scale(wireB, u0 / (4 * Math.PI) * integratingWire.current());
			B = Calc.add(B, wireB);
		}
		
		double mag = Calc.mag(B);
		if (!Double.isNaN(mag))
			maxB = Math.max(Calc.mag(B), maxB);
		
		return B;
	}		
	
	public double[] getVars(double t, Wire w, double[] point)
	{
		double[] vars = new double[10];
		double[] s = w.get_s(t);
		vars[P_X] = point[0];
		vars[P_Y] = point[1];
		vars[P_Z] = point[2];
		vars[S_X] = s[0];
		vars[S_Y] = s[1];
		vars[S_Z] = s[2];	
		vars[B_X] = 0;
		vars[B_Y] = 0;
		vars[B_Z] = 0;
		
		return vars;
	}
	
	public double[] diff_vars(double t, double[] vars)
	{
		double[] diff = new double[vars.length];
		double[] dp = {0, 0, 0};
		double[] ds = integratingWire.diff_s(t, vars);		
		double[] p = {vars[P_X], vars[P_Y], vars[P_Z]};
		double[] s = {vars[S_X], vars[S_Y], vars[S_Z]};		
		double[] r = Calc.add(p, Calc.scale(s, -1));
		double r_sqmag = Calc.sqmag(r);
		double[] r_hat = Calc.unit(r);		
		double[] dB = Calc.scale(Calc.cross(ds, r_hat), 1/r_sqmag);
				
		diff[P_X] = dp[0];
		diff[P_Y] = dp[1];
		diff[P_Z] = dp[2];
		diff[S_X] = ds[0];
		diff[S_Y] = ds[1];
		diff[S_Z] = ds[2];
		diff[B_X] = dB[0];
		diff[B_Y] = dB[1];
		diff[B_Z] = dB[2];
		
		return diff;
	}
	
	public double[] rk1(double t, double dt, double[] vars)
	{
		double[] diff = diff_vars(t, vars);
		for (int i = 0; i < vars.length; i++)
			vars[i] += dt * diff[i];
		return vars;
	}
	
	/** http://en.wikipedia.org/wiki/List_of_Runge%E2%80%93Kutta_methods
	 * Uses the Runge-Kutta method of the nth order, with n set
	 * by the <code>integrationMethod</code> variable.
	 */
	public double[] rkn(double t, double dt, double[] vars)
	{
		int s = butcherTableau[integrationMethod].length - 1;
		double[] temp = new double[vars.length];		
		double[][] K = new double[s][vars.length];
		
		// Determine K values
		
		for (int i = 0; i < s; i++)
		{
			double c = butcherTableau[integrationMethod][i][0];
			double t_n = t + c*dt;
			temp = Calc.copy(vars);
			
			for (int j = 1; j < i+1; j++)
			{
				double a = butcherTableau[integrationMethod][i][j];
				if (a != 0)				
					temp = Calc.add(temp, Calc.scale(K[j-1], dt*a));				
			}
			K[i] = diff_vars(t_n, temp);				
		}

		// Sum all of the K's accordingly
		
		double[] diff = new double[vars.length];
		diff = Calc.empty(diff);
		
		for (int i = 0; i < K.length; i++)
		{
			double b = butcherTableau[integrationMethod][s][i+1];
			diff = Calc.add(diff, Calc.scale(K[i],b));
		}
		
		// Scale by dt
		
		diff = Calc.scale(diff, dt);
		
		// Apply deltas to the input vars
		
		vars = Calc.add(vars, diff);
		return vars;
	}	
}
