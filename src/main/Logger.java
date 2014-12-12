package main;
import java.io.BufferedWriter;
import java.io.FileWriter;

import main.entities.Solenoid;
import main.entities.Vector;
import main.entities.Wire;

public class Logger 
{
	public StringBuffer csv;
	public Sim sim;
	public BufferedWriter out;
	
	public Logger(Sim sim) 
	{
		this.sim = sim;		
	}
	
	public void log()
	{
		csv = new StringBuffer(0);
		logWires();
		logVectors();
	}
	
	private void logWires()
	{
		csv.append("Wire,rx,ry,rz,dx,dy,dz,i,L,r,n\n");
		for (Wire w : sim.wires)
		{
			double[] r = w.get_position();
			double[] d = w.get_direction();
			csv.append(w.getClass().getSimpleName() + ",");
			for (int i = 0; i < 3; i++)
				csv.append(r[i] + ",");
			for (int i = 0; i < 3; i++)
				csv.append(d[i] + ",");
			
			csv.append(w.current() + ",");
			if (w instanceof Solenoid)
			{
				Solenoid sol = (Solenoid)w;
				csv.append(sol.height + ",");
				csv.append(sol.radius + ",");
				csv.append(sol.turns + ",\n");
			}
			else
			{
				csv.append((w.get_t2() - w.get_t1() + ","));
				csv.append(",\n");
			}			
		}
		csv.append("\n");
	}
	
	private void logVectors()
	{
		csv.append("Index,rx,ry,rz,Bx,By,Bz,,Bmag,,\n");
		for (int i = 0; i < sim.vectors.size(); i++)
		{
			Vector v = sim.vectors.get(i);
			csv.append((i+1) + ",");
			for (int j = 0; j < 3; j++)
				csv.append(v.origin[j] + ",");
			for (int j = 0; j < 3; j++)
				csv.append(v.value[j] + ",");
			csv.append("," + Calc.mag(v.value) + ",,\n");
		}
	}
	
	public void write() throws Exception
	{	
		try 
		{
			BufferedWriter out = new BufferedWriter(new FileWriter("data.csv"));
			out.write(csv.toString());
			out.flush();
			out.close();
		} 
		catch (Exception e) 
		{
			throw e;
		}
	}

}
