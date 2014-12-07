import java.awt.BorderLayout;
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


public class GraphicUI extends JFrame
{
	public Viewport viewport;
	public int width, height;
	
	public double dtheta = Math.PI / 30;

	public GraphicUI(String title, int width, int height)
	{
		super(title);
		
		viewport = new Viewport(this);

		this.width = width;
		this.height = height;
		
		setContentPane(createContent());	
		setJMenuBar(createMenuBar());		

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
				viewport.updateRotation();
			}
			else if (e.getKeyCode() == KeyEvent.VK_X)
			{
				viewport.pitch(-dtheta);
				viewport.updateRotation();
			}
			else if (e.getKeyCode() == KeyEvent.VK_C)
			{
				viewport.yaw(dtheta);
				viewport.updateRotation();
			}
			else if (e.getKeyCode() == KeyEvent.VK_Z)
			{
				viewport.yaw(-dtheta);
				viewport.updateRotation();
			}
			else if (e.getKeyCode() == KeyEvent.VK_D)
			{
				viewport.roll(dtheta);
				viewport.updateRotation();
			}
			else if (e.getKeyCode() == KeyEvent.VK_A)
			{
				viewport.roll(-dtheta);
				viewport.updateRotation();
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
