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

public class Viewport extends JPanel 
{
	public GraphicUI parent;
	public double zoom = 1;
	
	public double plotStep = Math.toRadians(0.05);
	public Dimension size = new Dimension(450,550);
	public Point mid = new Point(0,0);
	
	public double screen[] = {50, 0, 0};
	public double camera[] = {500, 0, 0};
	public double yaxis[] = {0, 1, 0};
	public double xaxis[] = {0, 0, -1};
	
	public double zbuffer[][] = new double[450][550];
	public BufferedImage buffer = new BufferedImage(450, 550, BufferedImage.TYPE_INT_RGB);
	public Graphics2D bg = buffer.createGraphics();
	
	public Color background = Color.white;
	
	public Viewport(GraphicUI parent) 
	{
		this.parent = parent;
		updateRotation();
		clearScreen();
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
             clearScreen();
             System.out.println(size);
             test();
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
		// XC is the line connecting point X and the camera		
		double[] XC = Calc.add(camera, Calc.scale(X, -1));
					
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
		
		return t <= 1;
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
				int x = (int)(zoom * ((point[0] + 0.5) + size.width/2)+0.5);
				int y = (int)(zoom * ((point[1] + 0.5) + size.height/2)+0.5);								
				double distance = point[2];
				
				if (inbounds(x, y))
				{						
					if (distance <= zbuffer[x][y])
					{
						zbuffer[x][y] = distance;
						bg.fillRect(x, y, 1, 1);
					}
					/*
					int[][] areas = new int[2][2];	// anti-aliasing
					int left = (int)x;
					int down = (int)y;
					double width1 = (left+1)-x;
					double width2 = x-left;
					double height1 = (down+1)-y;
					double height2 = y-down;
					areas[0][0] = (int)(255 * width1 * height1);
					areas[0][1] = (int)(255 * width1 * height2);
					areas[1][0] = (int)(255 * width2 * height1);
					areas[1][1] = (int)(255 * width2 * height2);
					int corex = (int)(x + 0.5 - left);
					int corey = (int)(y + 0.5 - down);
					areas[corex][corey] = 255;
					for (int dx = 0; dx < 2; dx++)
					{
						for (int dy = 0; dy < 2; dy++)
						{
							Point p = new Point(left + dx, down + dy);
							if (inbounds(p.x, p.y))
							{
								if (distance <= zbuffer[p.x][p.y])
								{
									if (dx == corex && dy == corey)
										zbuffer[p.x][p.y] = distance;
									Color pixel = new Color(buffer.getRGB(p.x, p.y));
									bg.setColor(Calc.average(color, areas[dx][dy], pixel, 255-areas[dx][dy]));
									bg.fillRect(p.x, p.y, 1, 1);
								}
							}
						}
					}	*/				
				}					
			}
		}
	}
	
	private void test()
	{			
		double[] o = {0,0,0};
		
		
		double[]r1 = {-300, 100, 0};
		double[]r2 = {-100, -100, 0};
		double[]r3 = {-300, -100, -200};
		double[]r4 = {-100, 100, -200};
		double[]m1 = {1, 0, 0};
		double[]m2 = {0, 1, 0};
		double[]m3 = {0, 0, 1};
		Line a = new Line(r1, m1);
		Line b = new Line(r1, m2);
		Line c = new Line(r1, m3);
		Line d = new Line(r2, m1);
		Line e = new Line(r2, m2);
		Line f = new Line(r2, m3);
		Line g = new Line(r3, m1);
		Line h = new Line(r3, m2);
		Line i = new Line(r3, m3);
		Line j = new Line(r4, m1);
		Line k = new Line(r4, m2);
		Line l = new Line(r4, m3);
		render(a.getPoints(1, 0, 200), Color.red);
		render(b.getPoints(1, -200, 0), Color.red);
		render(c.getPoints(1, -200, 0), Color.red);
		render(d.getPoints(1, -200, 0), Color.orange);
		render(e.getPoints(1, 0, 200), Color.orange);
		render(f.getPoints(1, -200, 0), Color.orange);
		render(g.getPoints(1, 0, 200), Color.green);
		render(h.getPoints(1, 0, 200), Color.green);
		render(i.getPoints(1, 0, 200), Color.green);
		render(j.getPoints(1, -200, 0), Color.blue);
		render(k.getPoints(1, -200, 0), Color.blue);
		render(l.getPoints(1, 0, 200), Color.blue);
		
		Line x = new Line(o, m1);
		Line y = new Line(o, m2);
		Line z = new Line(o, m3);
		
		render(x.getPoints(0.5, 0, 400), Color.cyan);
		render(y.getPoints(0.5, 0, 400), Color.magenta);
		render(z.getPoints(0.5, 0, 400), Color.lightGray);
	}
	
	public void updateRotation()
	{
		clearScreen();
		test();
		repaint();
	}
	
	public void yaw(double dtheta)
	{
		double[][] R = Matrix.getRotationMatrix(yaxis, dtheta);
		screen = Matrix.multiply(R, screen);
		camera = Matrix.multiply(R, camera);
		xaxis = Calc.unit(Calc.cross(yaxis, screen));
		Calc.println(xaxis);
		Calc.println(yaxis);
		System.out.println("-----------------------------");
		updateRotation();
	}
	
	public void pitch(double dtheta)
	{
		double[][] R = Matrix.getRotationMatrix(xaxis, dtheta);
		screen = Matrix.multiply(R, screen);
		camera = Matrix.multiply(R, camera);
		yaxis = Calc.unit(Calc.cross(screen, xaxis));
		Calc.println(xaxis);
		Calc.println(yaxis);
		System.out.println("-----------------------------");
		updateRotation();
	}
	
	public void roll(double dtheta)
	{
		Calc.println(Calc.unit(screen));
		//double[][] R = Matrix.getRotationMatrixX(-dtheta);
		double[][] R = Matrix.getRotationMatrix(Calc.unit(screen), dtheta);
		yaxis = Matrix.multiply(R, yaxis);
		xaxis = Calc.unit(Calc.cross(yaxis, screen));
		
		Calc.println(xaxis);
		Calc.println(yaxis);
		System.out.println("-----------------------------");
		updateRotation();
	}
}
