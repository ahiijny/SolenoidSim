package main;
import java.io.BufferedWriter;
import java.io.FileWriter;

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
		csv.append("(s),(m),(m)\n");
		csv.append("Time,V-bar,R-bar\n");
	}
	
	public void write()
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
			e.printStackTrace();
		}
	}

}
