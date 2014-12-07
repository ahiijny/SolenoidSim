package main;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import main.shapes.Cube;
import main.shapes.Line;
import main.shapes.Vector;


public class GraphicUI extends JFrame
{
	public Viewport viewport;
	public Sim sim;
	public int width, height;
	
	public double dtheta = Math.PI / 30;

	public GraphicUI(String title, int width, int height)
	{
		super(title);
		
		sim = new Sim();
		viewport = new Viewport(this, sim);

		this.width = width;
		this.height = height;
		
		setContentPane(createContent());	
		setJMenuBar(createMenuBar());
		initSim();

		setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);	
		setSize(width, height);	
		setVisible(true);
	}
	
	private JPanel createContent()
	{
		JPanel content = new JPanel(new BorderLayout());
		content.add(viewport, BorderLayout.CENTER);
		content.addMouseListener(new MyMouseListener());
		content.addMouseMotionListener(new MyMouseListener());
		content.addKeyListener(new MyKeyListener());
		
		return content;		
	}
	
	private JMenuBar createMenuBar()
	{
		return null;
	}
	
	private void initSim()
	{
		double[] o = {0,0,0};	
		double[] m1 = {1, 0, 0};
		double[] m2 = {0, 1, 0};
		double[] m3 = {0, 0, 1};
		double[] center = {0, 0, 0};
		
		Line x = new Line(o, m1, 0.5, 0, 400, Color.magenta);
		Line y = new Line(o, m2, 0.5, 0, 400, Color.cyan);
		Line z = new Line(o, m3, 0.5, 0, 400, Color.gray);
		sim.add(x);
		sim.add(y);
		sim.add(z);
		
		Cube cube = new Cube(center, 200, Color.black);
		sim.add(cube);
		
		for (int i = -200; i <= 200; i += 100)
			for (int j = -200; j <= 200; j += 100)
				for (int k = -200; k <= 200; k += 100)
				{
					double[] position = {i, j, k};
					double[] direction = {i, j, k + i + j};
					double zoom = 0.1;
					double ds = 0.25;
					sim.add(new Vector(position, direction, zoom, ds));
				}
	}
		
	
	private class MyMouseListener implements MouseListener, MouseMotionListener
	{

		@Override
		public void mouseClicked(MouseEvent e) {
			e.getComponent().requestFocus();
			repaint();
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		public void mouseDragged(MouseEvent e) {
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			repaint();
		}		
	}
	
	private class MyKeyListener implements KeyListener
	{

		@Override
		public void keyPressed(KeyEvent e) 
		{
			if (e.getKeyCode() == KeyEvent.VK_W)
			{
				viewport.pitch(dtheta);
				viewport.refresh();
			}
			else if (e.getKeyCode() == KeyEvent.VK_X)
			{
				viewport.pitch(-dtheta);
				viewport.refresh();
			}
			else if (e.getKeyCode() == KeyEvent.VK_C)
			{
				viewport.yaw(dtheta);
				viewport.refresh();
			}
			else if (e.getKeyCode() == KeyEvent.VK_Z)
			{
				viewport.yaw(-dtheta);
				viewport.refresh();
			}
			else if (e.getKeyCode() == KeyEvent.VK_D)
			{
				viewport.roll(dtheta);
				viewport.refresh();
			}
			else if (e.getKeyCode() == KeyEvent.VK_A)
			{
				viewport.roll(-dtheta);
				viewport.refresh();
			}				
		}

		@Override
		public void keyReleased(KeyEvent e) 
		{
		}

		@Override
		public void keyTyped(KeyEvent e) 
		{
		}
	}
	
	private class InputListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			Object parent = e.getSource();
			
		}
	}

}
