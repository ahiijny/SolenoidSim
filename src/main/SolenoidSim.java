package main;

import javax.swing.UIManager;

public class SolenoidSim 
{
	public static void main(String[] args) 
	{
		try 
		{            
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{			
		}
		new GraphicUI("become the literally i know lives", 1200, 760);
	}

}
