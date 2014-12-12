package main;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;

import javax.swing.JOptionPane;

import main.entities.Entity;
import main.entities.Solenoid;
import main.entities.StraightWire;
import main.entities.Vector;
import main.entities.Wire;


public class SaveLoader 
{
	public static final String endl = System.lineSeparator();
	public GraphicUI gui;
	public Sim sim;
	public File path = new File("init.scn");

	public SaveLoader(Sim sim, String path) 
	{
		this.sim = sim;
		this.gui = sim.parent;
		this.path = new File(path);
	}
	
	public String getString(GraphicUI gui)
	{
		String str = "";
		
		str += ";Simulation Settings " + endl;
		str += "integrator = " + sim.integratorLabelsShort[sim.integrationMethod] + endl;
		str += "simStep = " + sim.dt + endl;
		str += "maxB = " + sim.maxB + endl;
		str += "maxVectorLength = " + sim.maxScale + endl;
		str += "showArrowheadLowerLimit = " + Vector.arrowHeadThreshold + endl;
		str += "arrowHeadLength = " + Vector.arrowHeadLength + endl;
		str += "minColor = " + sim.minColor + endl;
		str += "maxColor = " + sim.maxColor + endl;
		str += "progressInterval = " + sim.progressInterval + endl; 
		str += endl;
		
		str += ";Vector Lattice " + endl;
		if (gui.probePoint != null)
		{
			str += "probe =";
			for (int i = 0; i < 3; i++)
				str += " " + gui.probePoint[i];
			str += endl;
		}
		str += "xRange = " + gui.latticeBounds[0][0] + " " + gui.latticeBounds[0][1] + endl;
		str += "yRange = " + gui.latticeBounds[1][0] + " " + gui.latticeBounds[1][1] + endl;
		str += "zRange = " + gui.latticeBounds[2][0] + " " + gui.latticeBounds[2][1] + endl;
		str += "xNum = " + gui.latticeBounds[0][2] + endl;
		str += "yNum = " + gui.latticeBounds[1][2] + endl;
		str += "zNum = " + gui.latticeBounds[2][2] + endl;
		str += endl;
				
		str += ";Viewport Settings " + endl;		
		str += "zoom = " + gui.viewport.zoom + endl;
		str += "upscale = " + gui.viewport.scale + endl;
		str += "fov = " + Math.toDegrees(gui.viewport.fov) + endl;
		str += "screen_x-axis =";
		for (int i = 0; i < 3; i++)
			str += " " + gui.viewport.xaxis[i];
		str += endl + "screen_y-axis =";
		for (int i = 0; i < 3; i++)
			str += " " + gui.viewport.yaxis[i];
		str += endl + "screen =";
		for (int i = 0; i < 3; i++)
			str += " " + gui.viewport.screen[i];
		str += endl + "camera =";
		for (int i = 0; i < 3; i++)
			str += " " + gui.viewport.camera[i];
		str += endl;
		str += "plotStep = " + gui.viewport.plotStep + endl;
		str += "cubeLength = " + gui.cube.width + endl;
		
		return str;
	}

	public String getString(Wire w)
	{
		String str = "";
		str += "=== " + sim.wires.indexOf(w) + " " + w.getClass().getSimpleName() + " ===" + endl;
		double[] r = w.get_position();
		double[] d = w.get_direction();
		double length = w.get_t2() - w.get_t1();
		str += "position = " + r[0] + " " + r[1] + " " + r[2] + endl;
		str += "direction = " + d[0] + " " + d[1] + " " + d[2] + endl;
		str += "current = " + w.current() + endl;			
		if (w instanceof Solenoid)
		{
			Solenoid sol = (Solenoid)w;
			str += "length = " + sol.height + endl;	
			str += "radius = " + sol.radius + endl;
			str += "turns = " + sol.turns + endl;
		}
		else
			str += "length = " + length + endl;
		str += "scale_plot_step = " + ((Entity)w).plotStepScalar + endl;
		return str;
	}

	public void writeSave()
	{
		BufferedWriter out = null;

		try // try writing to the file
		{
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(path), "utf-8"));
			for (int i = 0; i < sim.wires.size(); i++)
			{
				out.write(getString(gui));
				out.write(endl);
				out.write(getString(sim.wires.get(i)));
				out.write(endl);
			}
		} 
		catch (Exception e) // show error dialog
		{
			String message = "Error. Could not write file.";
			int type = JOptionPane.INFORMATION_MESSAGE;
			JOptionPane.showMessageDialog(null, message, "Save", type);
		} 
		finally 
		{
			try 
			{
				out.close();
			} 
			catch (Exception ex) {}
		}
	}
	
	public static int atoi(String str)
	{
		return Integer.parseInt(str);
	}
	
	public static double atof(String str)
	{
		return Double.parseDouble(str);
	}

	public void loadSave() throws Exception
	{
		String save = readSave();
		String[] parameters = save.split(endl);
		boolean sameWire = true;
		int line = 0; 
		int wireId = 0;

		// Iterate through save String

		try
		{
			for (line = 0 ; line < parameters.length; line++)
			{                               
				String text = parameters[line]; 
				if (!text.startsWith (";")) // Line starting with ";" are comments
				{
					String[] params = text.split(" ");
															
					// Load Wire Specifications
					
					if (params[0].equals("==="))
					{
						int index = atoi(params[1]);
						if (params[2].contains("StraightWire"))	
						{
							sim.addWire(new StraightWire());
							wireId = 0;
						}
						else
						{
							sim.addWire(new Solenoid());
							wireId = 1;
						}
						Wire wire = sim.wires.get(index);
						
						params = parameters[++line].split(" ");
						sameWire = !params[0].equals("===");
												
						while (sameWire)
						{
							if (params[0].equals("position"))
							{
								double rx = atof(params[2]);
								double ry = atof(params[3]);
								double rz = atof(params[4]);
								double[] r = {rx, ry, rz};
								
								if (wireId == 0)
									((StraightWire)wire).origin = r;
								else if (wireId == 1)
									((Solenoid)wire).origin = r;
							}
							else if (params[0].equals("direction"))
							{
								double dx = atof(params[2]);
								double dy = atof(params[3]);
								double dz = atof(params[4]);
								double[] d = {dx, dy, dz};
								d = Calc.unit(d);
								
								if (wireId == 0)
									((StraightWire)wire).direction = d;
								else if (wireId == 1)
									((Solenoid)wire).direction = d;
							}
							else if (params[0].equals("length"))
							{
								double length = atof(params[2]);
								if (wireId == 0)
									((StraightWire)wire).t2 = length;
								else if (wireId == 1)
									((Solenoid)wire).height = length;
							}
							else if (params[0].equals("current"))
							{
								double current = atof(params[2]);
								if (wireId == 0)
									((StraightWire)wire).current = current;
								else if (wireId == 1)
									((Solenoid)wire).current = current;
							}							
							else if (params[0].equals("radius"))
							{
								double radius = atof(params[2]);
								((Solenoid)wire).radius = radius;
							}
							else if (params[0].equals("turns"))
							{
								double turns = atof(params[2]);
								((Solenoid)wire).turns = turns;
							}
							else if (params[0].equals("scale_plot_step"))
								((Entity)wire).plotStepScalar = atof(params[2]);
							line++;
							sameWire = line < parameters.length;
							if (sameWire)
							{
								params = parameters[line].split(" ");
								sameWire = !params[0].equals("===");
							}
						}
						if (wireId == 1)
						{
							((Solenoid)wire).updateRotationMatrix();
						}
						line--;			
					}
					
					// Load Sim Specifications
					
					else if (params[0].equals("integrator"))
					{
						int index = sim.integratorLabelsShort.length - 1;
						
						while (index >= 0 && sim.integratorLabelsShort[index] != params[2])
							index--;
						
						if (index != -1)
						{
							sim.integrationMethod = index;
							gui.rkSel.setSelectedIndex(index);
						}						
					}
					else if (params[0].equals("simStep"))
						sim.dt = atof(params[2]);
					else if (params[0].equals("maxB"))
						sim.maxB = atof(params[2]);
					else if (params[0].equals("maxVectorLength"))
						sim.maxScale = atof(params[2]);
					else if (params[0].equals("showArrowheadLowerLimit"))
						Vector.arrowHeadThreshold = atof(params[2]);
					else if (params[0].equals("arrowHeadLength"))
						Vector.arrowHeadLength = atof(params[2]);
					else if (params[0].equals("minColor"))
						sim.minColor = atof(params[2]);
					else if (params[0].equals("maxColor"))
						sim.maxColor = atof(params[2]);
					else if (params[0].equals("probe"))
						gui.probePoint = new double[] {atof(params[2]), atof(params[3]), atof(params[4])};
					else if (params[0].equals("xRange"))
					{
						gui.latticeBounds[0][0] = atof(params[2]);
						gui.latticeBounds[0][1] = atof(params[3]);
					}
					else if (params[0].equals("yRange"))
					{
						gui.latticeBounds[1][0] = atof(params[2]);
						gui.latticeBounds[1][1] = atof(params[3]);
					}
					else if (params[0].equals("zRange"))
					{
						gui.latticeBounds[2][0] = atof(params[2]);
						gui.latticeBounds[2][1] = atof(params[3]);
					}
					else if (params[0].equals("xNum"))
						gui.latticeBounds[0][2] = atof(params[2]);
					else if (params[0].equals("yNum"))
						gui.latticeBounds[1][2] = atof(params[2]);
					else if (params[0].equals("zNum"))
						gui.latticeBounds[2][2] = atof(params[2]);
					else if (params[0].equals("progressInterval"))
						sim.progressInterval = atoi(params[2]);
					else if (params[0].equals("zoom"))
					{
						double zoom = atof(params[2]);
						Viewport.defaultZoom = zoom;
						gui.viewport.setZoom(zoom);
					}
					else if (params[0].equals("upscale"))
					{
						double scale = atof(params[2]);
						Viewport.defaultScale = scale;
						gui.viewport.scale = scale;
						gui.viewport.enforceFOV();
					}
					else if (params[0].equals("fov"))
					{
						double fov = Math.toRadians(atof(params[2]));
						Viewport.defaultFOV = fov;
						gui.viewport.fov = fov;;
						gui.viewport.enforceFOV();
					}
					else if (params[0].equals("screen_x-axis"))
						gui.viewport.xaxis = new double[] {atof(params[2]), atof(params[3]), atof(params[4])};
					else if (params[0].equals("screen_y-axis"))
						gui.viewport.yaxis = new double[] {atof(params[2]), atof(params[3]), atof(params[4])};
					else if (params[0].equals("screen"))
						gui.viewport.screen = new double[] {atof(params[2]), atof(params[3]), atof(params[4])};
					else if (params[0].equals("camera"))
						gui.viewport.camera = new double[] {atof(params[2]), atof(params[3]), atof(params[4])};
					else if (params[0].equals("plotStep"))
					{
						double plotStep = atof(params[2]);
						Viewport.defaultPlotStep = plotStep;
						gui.viewport.plotStep = plotStep;
					}
					else if (params[0].equals("cubeLength"))
						gui.cubeSpecs.setText(params[2]);
				}
			}     
		}
		catch (Exception e)
		{
			String exMessage = (line + 1) + " :\n" + e.getMessage();
			int type = JOptionPane.INFORMATION_MESSAGE;
            String message = "Error: Corrupt save file.\n";
            message += "Could not parse file at line " + exMessage;                                        
            JOptionPane.showMessageDialog(null, message, "Error", type);
            throw e;
		}
	}

	/** Reads the file at the indicated path and returns
	 * a String representation of the contents of the file.
	 * 
	 * @param path  the file to be read
	 * @return the file's contents in String format
	 */
	private String readSave() throws Exception
	{
		// Declaration of Variables
		String save = "";
		BufferedReader in = null;
		boolean reading = true;

		try     // try reading the file
		{
			in = new BufferedReader (new FileReader (path));

			while (reading)
			{               
				String line = in.readLine();                                
				if (line != null)
					save += line + endl;
				else
					reading = false;
			}

			in.close();
		}
		catch (Exception e) // show error dialog
		{
			String message = "Error. Could not read file:" + path;
			int type = JOptionPane.INFORMATION_MESSAGE;
			JOptionPane.showMessageDialog(null, message, "Load", type);
			throw e;
		}
		return save;
	}
}
