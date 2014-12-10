package main.entities;

public interface Wire
{
	public double current();
	
	public double[] diff_s (double t, double[] vars);	
	public double[] get_s(double t);
	public double[] get_position();
	public double[] get_direction();
	
	public double get_t1();
	public double get_t2();
}
