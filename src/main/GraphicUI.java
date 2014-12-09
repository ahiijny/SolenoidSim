package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashSet;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import main.entities.Cube;
import main.entities.Solenoid;
import main.entities.StraightWire;

/** Referenced http://tips4java.wordpress.com/2013/06/09/motion-using-the-keyboard/
 * for key bindings.
 * 
 * @author Jiayin
 *
 */
public class GraphicUI extends JFrame
{
	public Viewport viewport;
	public Sim sim;
	public int width, height;
	private Timer timer;
	private int keyboardDelay = 50;	
	private HashSet<String> pressedKeys;
	private boolean sneaking = false;
	
	public double dtheta = Math.PI / 30;
	/** The rotation angle is divided by this factor during
	 * numpad rotations if shift is held down. This permits 
	 * fine-tuning in the rotation of the viewport. 
	 */
	public double sneakFactor = 10;
	public double zoomInFactor = 1.25;	

	public GraphicUI(String title, int width, int height)
	{
		super(title);
		
		sim = new Sim();
		viewport = new Viewport(this, sim);
		pressedKeys = new HashSet<String>();
		timer = new Timer(keyboardDelay, new KeyboardAction());
		timer.setInitialDelay(0);

		this.width = width;
		this.height = height;
		
		setContentPane(createContent());	
		setJMenuBar(createMenuBar());
		setKeyBindings();
		initSim();

		setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);	
		setSize(width, height);	
		setVisible(true);
		
		viewport.requestFocusInWindow();
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
		JMenuBar menuBar = new JMenuBar();
		JMenu file, simulation, view, help;
		JMenuItem button;
		int menuKeyMask = InputEvent.CTRL_MASK;

		// Attempt to make MenuShortcutKeyMask valid for multiple platforms.
		try {
			menuKeyMask = java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		} catch (Exception e) {			
		}

		// "File" Menu        
		file = new JMenu ("File");
		/*file.setMnemonic('f');
		
		button = new JMenuItem ("Load State");
		button.setMnemonic('o');
		button.setAccelerator(KeyStroke.getKeyStroke (
				KeyEvent.VK_O, menuKeyMask));
		button.addActionListener (new MenuListener ());
		file.add(button);
		
		button = new JMenuItem ("Save State"); 
		button.setMnemonic('s');
		button.setAccelerator(KeyStroke.getKeyStroke (
				KeyEvent.VK_S, menuKeyMask));
		button.addActionListener (new MenuListener ());
		file.add(button);
		
		button = new JMenuItem ("Export to CSV");
		button.setMnemonic('x');
		button.setAccelerator(KeyStroke.getKeyStroke (
				KeyEvent.VK_X, menuKeyMask));
		button.addActionListener (new MenuListener ());
		file.add(button);
*/
		button = new JMenuItem ("Exit"); // exit button
		button.setMnemonic('x');
		button.addActionListener (new MenuListener ());
		file.add(button);
		
		// "Simulation" Menu
		simulation = new JMenu ("Simulation");
		simulation.setMnemonic('s');	
					
		button = new JMenuItem ("Sim step default");
		button.addActionListener (new MenuListener());
		simulation.add(button);
					
				
		// "View" Menu
		
		view = new JMenu ("View");
		view.setMnemonic('v');	
		
		button = new JMenuItem ("Zoom in");
		button.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0));
		button.addActionListener (new MenuListener());
		view.add(button);
		
		button = new JMenuItem ("Zoom out");
		button.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0));
		button.addActionListener (new MenuListener());
		view.add(button);
		
		button = new JMenuItem ("Move screen in");
		button.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, 0));
		button.addActionListener (new MenuListener());
		view.add(button);
		
		button = new JMenuItem ("Move screen out");
		button.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0));
		button.addActionListener (new MenuListener());
		view.add(button);
		
		button = new JMenuItem ("Move camera in");
		button.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0));
		button.addActionListener (new MenuListener());
		view.add(button);
		
		button = new JMenuItem ("Move camera out");
		button.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0));
		button.addActionListener (new MenuListener());
		view.add(button);
		
		button = new JMenuItem ("Default zoom");
		button.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SLASH, 0));
		button.addActionListener (new MenuListener());
		view.add(button);	
		
		button = new JMenuItem ("Plot step default");
		button.addActionListener (new MenuListener());
		view.add(button);
		
		button = new JMenuItem ("Refresh");
		button.setMnemonic('r');
		button.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		button.addActionListener (new MenuListener ());
		view.add(button);	

		// "Help" Menu
		help = new JMenu ("Help");
		help.setMnemonic('h');	

		button = new JMenuItem ("About"); // about button
		button.setMnemonic('a');
		button.addActionListener (new MenuListener ());
		help.add(button);


		// Add All Menus        
		menuBar.add (file);
		menuBar.add (simulation);
		menuBar.add (view);
		menuBar.add (help);

		// Return        
		return menuBar;
	}
	
	private void initSim()
	{
		double[] origin = {0, 0, 0};
		double[] center = {0, -50, 0};
		double[] m = {0, 1, 0};
		Cube cube = new Cube(origin, 200, Color.black);
		sim.add(cube);
		sim.addWire(new Solenoid(10, center, m, 10, 100, 10));
		//sim.addWire(new StraightWire(10, center, m, 200));
					
		double[][] lattice = new double[125][3];
		int counter = 0;
		
		for (int i = -100; i <= 100; i += 50)
			for (int j = -100; j <= 100; j += 50)
				for (int k = -100; k <= 100; k += 50)				
					lattice[counter++] = new double[] {i, j, k};		
		
		sim.simulate(lattice);	
	}
	
	private void setKeyBindings()
	{
		InputMap im = viewport.getInputMap(JPanel.WHEN_FOCUSED);
	    ActionMap am = viewport.getActionMap();
	    
	    addAction(im, am, KeyEvent.VK_NUMPAD8, 0, "pitch+");
	    addAction(im, am, KeyEvent.VK_NUMPAD2, 0, "pitch-");
	    addAction(im, am, KeyEvent.VK_NUMPAD1, 0, "yaw+");
	    addAction(im, am, KeyEvent.VK_NUMPAD3, 0, "yaw-");
	    addAction(im, am, KeyEvent.VK_NUMPAD6, 0, "roll+");
	    addAction(im, am, KeyEvent.VK_NUMPAD4, 0, "roll-");

	    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0, false), "sneak pressed");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0, true), "sneak released");
		am.put("sneak pressed", new SneakTracker(true));
		am.put("sneak released", new SneakTracker(false));
	    
	}
	
	public void addAction(InputMap im, ActionMap am, int key, int modifier, String name)
    {
		String pressed = name + " pressed";
		String released = name + " released";
		
		im.put(KeyStroke.getKeyStroke(key, modifier, false), pressed);
		im.put(KeyStroke.getKeyStroke(key, modifier, true), released);
		am.put(pressed, new KeyTracker(name, true));
		am.put(released, new KeyTracker(name, false));
    }
	
	private class KeyTracker extends AbstractAction
    {
		String key;
		boolean isPressed;
		
        public KeyTracker(String key, boolean isPressed)
        {
        	this.key = key;
        	this.isPressed = isPressed;
        }

        public void actionPerformed(ActionEvent e)
        {
        	handleKeyEvent(key, isPressed);
        }
    }
	
	private class SneakTracker extends AbstractAction
    {
		boolean isPressed;
		
		public SneakTracker(boolean isPressed)
		{
			this.isPressed = isPressed;
		}
		
		public void actionPerformed(ActionEvent e)
	    {
			sneaking = isPressed;
	    }		
    }
	
	/** Invoked whenever a key is pressed or released.
	 */
    private void handleKeyEvent(String key, boolean pressed)
    {
        //  Keep track of which keys are pressed

        if (!pressed)
        	pressedKeys.remove(key);
        else
        	pressedKeys.add(key);

        //  Start the Timer when the first key is pressed
        if (pressedKeys.size() == 1)
        	timer.start();

        //  Stop the Timer when all keys have been released
        if (pressedKeys.size() == 0)
        	timer.stop();
    }
    
    private class KeyboardAction extends AbstractAction
	{
		@Override
		public void actionPerformed(ActionEvent e) 
		{			
			for (String cmd : pressedKeys)
			{
				double nudgeAngle = sneaking ? dtheta / sneakFactor : dtheta;
				
				if (cmd.equals("pitch+"))
				{				
					viewport.pitch(nudgeAngle);
					viewport.refresh();
				}
				else if (cmd.equals("pitch-"))
				{
					viewport.pitch(-nudgeAngle);
					viewport.refresh();
				}
				else if (cmd.equals("yaw+"))
				{
					viewport.yaw(nudgeAngle);
					viewport.refresh();
				}
				else if (cmd.equals("yaw-"))
				{
					viewport.yaw(-nudgeAngle);
					viewport.refresh();
				}
				else if (cmd.equals("roll+"))
				{
					viewport.roll(nudgeAngle);
					viewport.refresh();
				}
				else if (cmd.equals("roll-"))
				{
					viewport.roll(-nudgeAngle);
					viewport.refresh();
				}
			}
		}
	}
    
    public void refresh()
    {
    	viewport.refresh();
    	viewport.requestFocusInWindow();
    }
	
	public void close ()
	{
		dispose();	
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
			if (e.getKeyCode() == KeyEvent.VK_SHIFT)
				sneaking = true;						
		}

		@Override
		public void keyReleased(KeyEvent e) 
		{
			if (e.getKeyCode() == KeyEvent.VK_SHIFT)
				sneaking = false;
		}

		@Override
		public void keyTyped(KeyEvent e) 
		{
		}
	}
	
	private class MenuListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			Object parent = e.getSource();
			if (parent instanceof JMenuItem)
			{
				JMenuItem button = (JMenuItem) parent;
				String name = button.getText();
				double dzoom = sneaking ? ((zoomInFactor - 1) / sneakFactor) + 1 : zoomInFactor;
				if (name.equals("Exit"))
				{					
					close();
				}
				else if (name.equals("Sim step default"))
				{
					sim.dt = Sim.defaultDt;
				}
				else if (name.equals("Zoom in"))
				{
					viewport.zoom *= dzoom;
					refresh();
				}
				else if (name.equals("Zoom out"))
				{
					viewport.zoom /= dzoom;
					refresh();
				}
				else if (name.equals("Move camera in"))
				{
					viewport.scaleCamera(1/dzoom);
					refresh();
				}
				else if (name.equals("Move camera out"))
				{
					viewport.scaleCamera(dzoom);
					refresh();
				}
				else if (name.equals("Move screen in"))
				{
					viewport.scaleScreen(1/dzoom);
					refresh();
				}
				else if (name.equals("Move screen out"))
				{
					viewport.scaleScreen(dzoom);
					refresh();
				}
				else if (name.equals("Default zoom"))
				{
					viewport.defaultScale();
					refresh();
				}
				else if (name.equals("Plot step default"))
				{
					viewport.plotStep = Viewport.defaultPlotStep;
				}
				else if (name.equals("Refresh"))
				{
					refresh();
				}
				else if (name.equals("About"))
				{
					String message = "Version: 2014.12.09\n";					
					message += "Program by: Jiayin Huang\n";
					message += "Solly Sim.\n";
					JOptionPane.showMessageDialog(GraphicUI.this, message, "About", JOptionPane.PLAIN_MESSAGE);			
				}
			}			
		}
	}

}
