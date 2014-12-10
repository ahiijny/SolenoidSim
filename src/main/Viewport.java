package main;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/** The polynomial \(ax^2 + bx + c\).
 * 
 * @author Jiayin
 *
 */
public class Viewport extends JPanel 
{
	public static final double defaultScreen = 200;
	public static final double defaultCamera = 509.1;
	public static final double defaultScale = 2;
	public static final double defaultPlotStep = 0.5;
	
	public static final double maxFOV = Math.toRadians(121);
	public static final double minFOV = Math.toRadians(9);
	
	public GraphicUI parent;
	public Sim sim;
	public double zoom = 200;
	public double scale = 2;
	public double plotStep = 0.5;
	public boolean perspective = true;
		
	public Dimension size = new Dimension(450,550);
	public Point mid = new Point(0,0);
	
	public double fov = Math.toRadians(40);
	
	public double screen[] = {0, 0, 200};
	public double camera[] = {0, 0, 509.1};
	public double yaxis[] = {0, 1, 0};
	public double xaxis[] = {1, 0, 0};
	
	public double zbuffer[][] = new double[450][550];
	public BufferedImage buffer = new BufferedImage(450, 550, BufferedImage.TYPE_INT_RGB);
	public Graphics2D bg = buffer.createGraphics();
	
	public Color background = Color.white;
	
	public Viewport(GraphicUI parent, Sim sim) 
	{
		this.parent = parent;
		this.sim = sim;	
		enforceFOV();
		refresh();	
		addComponentListener(new ResizeListener());
	}
	
	public void clearScreen()
	{
		for (int i = 0; i < zbuffer.length; i++)		
			for (int j = 0; j < zbuffer[i].length; j++)			
				zbuffer[i][j] = Double.POSITIVE_INFINITY;
		
		bg.setColor(background);
		bg.fillRect(0, 0, size.width, size.height);	
	}	
	
	public boolean inbounds(double x, double y)
	{
		return x >= 0 && x < size.width && y >= 0 && y < size.height; 
	}
	
	@Override
	public void paintComponent(Graphics g)
	{		
		Graphics2D g2d = (Graphics2D)g;
		AffineTransform at = AffineTransform.getScaleInstance(1, -1);
		at.preConcatenate(AffineTransform.getTranslateInstance(0, size.height));
		g2d.transform(at);
		g2d.drawImage(buffer, null, 0, 0);
	}
	 
	private class ResizeListener extends ComponentAdapter
	{
		 public void componentResized(ComponentEvent e) 
		 {
             getSize(size);
             mid.x = size.width/2;
             mid.y = size.height/2;
             zbuffer = new double[size.width][size.height];
             buffer = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
             bg = buffer.createGraphics();
             refresh();
             System.out.println(size);
         }
	}
	
	public void render()
	{
		for (int i = 0; i < sim.objects.size(); i++)
			render(sim.objects.get(i).getPoints(plotStep), sim.objects.get(i).color);
	}
	
	public void render(double[][] points, Color color)
	{
		bg.setColor(color);
		
		for (int i = 0; i < points.length; i++)
		{
			double[] point = new double[3];
			boolean valid = rayTrace(points[i], point);
			
			if (valid)
			{
				int x = (int)((scale*(point[0] + 0.5) + size.width/2)+0.5);
				int y = (int)((scale*(point[1] + 0.5) + size.height/2)+0.5);								
				double distance = point[2];
				
				if (inbounds(x, y))
				{						
					if (distance <= zbuffer[x][y])
					{
						zbuffer[x][y] = distance;
						bg.fillRect(x, y, 1, 1);
					}			
				}					
			}
		}		
	}
	
	/** Using the Viewport's current settings, projects the
	 * specified point onto the screen, based on the camera.
	 * 
	 * @param X the Cartesian coordinates of the point in 3-space
	 * @return the (x,y) coordinates of the point projected onto the screen
	 */
	public boolean rayTrace(double[] X, double[] coords)
	{
		double[] XC;
		if (perspective)
		{
			// XC is the line connecting point X and the camera		
			XC = Calc.add(camera, Calc.scale(X, -1));
		}
		else
		{
			XC = screen;
		}
					
		// XM is the line connecting point X with the geometric center of the screen
		double[] XM = Calc.add(screen, Calc.scale(X, -1));
					
		// This value of t when substituted into the vector equation of XC
		// gives the point of intersection between XC and the screen		
		double t = Calc.dot(XM, screen) / Calc.dot(XC, screen);
							
		// P is the Cartesian coordinates of the projection of
		// X onto the screen
		double[] P = Calc.add(X, Calc.scale(XC, t));
		
		// Find the coordinates relative to the center of the screen
		double[] MP = Calc.add(P, Calc.scale(screen, -1));
		
		// To find the x and y coordinates on the screen,
		// find the scalar projection of MP onto xaxis and yaxis
				
		coords[0] = Calc.dot(MP, xaxis) / Calc.mag(xaxis);
		coords[1] = Calc.dot(MP, yaxis) / Calc.mag(yaxis);
		
		// For depth purposes, record square of distance from screen
		
		coords[2] = t * Calc.sqmag(XC);
		
		// Determine if object is in front of the screen.
		// This is only so if the distance to the screen
		// is less than the distance to the camera
		
		return perspective ? t <= 1 && t >= 0 : t > 0;
	}	
	
	public void refresh()
	{
		clearScreen();
		render();
		repaint();
	}
	
	public void scaleCamera(double scalar)
	{
		camera = Calc.scale(camera, scalar);
		System.out.print ("Camera = ");
		Calc.println(camera);
	}
	
	public void scaleScreen(double scalar)
	{
		screen = Calc.scale(screen, scalar);
		zoom *= scalar;
		System.out.print ("Screen = ");
		Calc.println(screen);
	}
	
	public void setZoom(double newZoom)
	{
		screen = Calc.scale(screen, newZoom / Calc.mag(screen));
		zoom = Calc.mag(screen);
		enforceFOV();
	}
	
	public void incrementFOV(double increment)
	{
		double newFOV = fov + increment;
		fov = (minFOV <= newFOV && newFOV <= maxFOV) ? newFOV : fov;
		System.out.println("FOV = " + Math.toDegrees(fov));
	}
	
	public void defaultScale()
	{
		camera = Calc.scale(Calc.unit(camera), defaultCamera);
		screen = Calc.scale(Calc.unit(screen), defaultScreen);
		scale = defaultScale;
		zoom = defaultScreen;
	}
	
	public void enforceFOV()
	{
		double screenWidth = size.width/scale;
		double distance = screenWidth /(2 * Math.tan(fov/2));
		if (!Double.isNaN(distance) && distance != 0)
		{
			double screen_r = Calc.mag(screen);
			camera = Calc.scale(screen, (screen_r + distance) / screen_r);
		}
	}
	
	public void yaw(double dtheta)
	{
		double[][] R = Matrix.getRotationMatrix(yaxis, dtheta);
		screen = Matrix.multiply(R, screen);
		camera = Matrix.multiply(R, camera);
		xaxis = Calc.unit(Calc.cross(yaxis, screen));
		refresh();
	}
	
	public void xturn(double dtheta)
	{
		double[][] R = Matrix.getRotationMatrixX(dtheta);
		screen = Matrix.multiply(R, screen);
		camera = Matrix.multiply(R, camera);
		yaxis = Matrix.multiply(R, yaxis);
		xaxis = Calc.unit(Calc.cross(yaxis, screen));
	}
	
	public void yturn(double dtheta)
	{
		double[][] R = Matrix.getRotationMatrixY(dtheta);
		screen = Matrix.multiply(R, screen);
		camera = Matrix.multiply(R, camera);
		yaxis = Matrix.multiply(R, yaxis);
		xaxis = Calc.unit(Calc.cross(yaxis, screen));
	}
	
	public void zturn(double dtheta)
	{
		double[][] R = Matrix.getRotationMatrixZ(dtheta);
		screen = Matrix.multiply(R, screen);
		camera = Matrix.multiply(R, camera);
		yaxis = Matrix.multiply(R, yaxis);
		xaxis = Calc.unit(Calc.cross(yaxis, screen));
	}
	
	public void pitch(double dtheta)
	{
		double[][] R = Matrix.getRotationMatrix(xaxis, dtheta);
		screen = Matrix.multiply(R, screen);
		camera = Matrix.multiply(R, camera);
		yaxis = Calc.unit(Calc.cross(screen, xaxis));
		refresh();
	}
	
	public void roll(double dtheta)
	{
		double[][] R = Matrix.getRotationMatrix(Calc.unit(screen), dtheta);
		yaxis = Matrix.multiply(R, yaxis);
		xaxis = Calc.unit(Calc.cross(yaxis, screen));
		refresh();
	}
}

