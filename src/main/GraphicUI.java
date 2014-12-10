package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import main.entities.Cube;
import main.entities.Solenoid;
import main.entities.StraightWire;
import main.entities.Wire;

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
	
	public double dtheta = Math.PI / 30;
	/** The rotation angle is divided by this factor during
	 * numpad rotations if shift is held down. This permits 
	 * fine-tuning in the rotation of the viewport. 
	 */
	public double sneakFactor = 10;
	public double zoomInFactor = 1.25;
	public double fovIncrement = Math.toRadians(10);
	
	public JComboBox<String> entitySel;
	public JComboBox<String> rkSel;
	public String[] specLabels = {"rx", "ry", "rz", "dx", "dy", "dz", "Current", "Length", "Radius", "Turns"};
	public String[] propagatorLabels = {"Runge-Kutta, 1st order (RK1)", 
			"Runge-Kutta, 2nd order (RK2)",								
			"Runge-Kutta, 4th order (RK4)"};
	public JTextField[] wireSpecs;
	public JTextField simStep, zoom, scale, plotStep;	
	public JButton specsBut, simBut, simStepBut, zoomBut, scaleBut, plotStepBut;
	
	public double[][] lattice;
	public Wire wire;		
	
	private Timer timer;
	private int keyboardDelay = 50;	
	private HashSet<String> pressedKeys;
	private boolean sneaking = false;

	public GraphicUI(String title, int width, int height)
	{
		super(title);
		
		sim = new Sim(this);	
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

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		setSize(width, height);	
		setVisible(true);
		
		simulate();		
	}
	
	private JPanel createContent()
	{
		JPanel content = new JPanel(new BorderLayout());
		
		content.add(createMiddlePanel(),BorderLayout.CENTER);
		content.add(createLeftPanel(),BorderLayout.WEST);
		//content.add(createRightPanel(),BorderLayout.EAST);
		//content.add(createTopPanel(),BorderLayout.NORTH);
		content.add(createBottomPanel(),BorderLayout.SOUTH);
		
		content.addMouseListener(new MyMouseListener());
		content.addMouseMotionListener(new MyMouseListener());
		content.addKeyListener(new MyKeyListener());
		
		return content;		
	}
	
	private JPanel createMiddlePanel()
	{		
		return viewport;
	}
	
	private JPanel createRightPanel()
	{
		return null;
	}
	
	private JPanel createLeftPanel()
	{
		JPanel leftpane = new JPanel(new BorderLayout());
		JPanel wirepane = new JPanel(new GridBagLayout());
		JButton button; 
		JLabel label;
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2,4,2,4);
		
		// Input data
		
		entitySel = new JComboBox<String>();
		entitySel.setPreferredSize(new Dimension(200, 20));
		entitySel.addActionListener(new MyListener());
		
		gridBagAdd(wirepane, c, 0, 0, new JLabel("Select wire:"));
		gridBagAdd(wirepane, c, 0, 1, 2, GridBagConstraints.CENTER, entitySel);
		
		// Input fields
		
		wireSpecs = new JTextField[specLabels.length];
		
		for (int i = 0; i < wireSpecs.length; i++)
		{	
			if (i == 0)
			{
				separator(wirepane, c);
				
				label = new JLabel("Position:");
				gridBagAdd(wirepane, c, 0, ++c.gridy, label);
			}
			else if (i == 3)
			{
				separator(wirepane, c);
				
				label = new JLabel("Direction:");
				gridBagAdd(wirepane, c, 0, ++c.gridy, label);
			}
			else if (i == 6)
			{
				separator(wirepane, c);
				
				label = new JLabel("Other Parameters:");
				gridBagAdd(wirepane, c, 0, ++c.gridy, label);
			}
			
			wireSpecs[i] = new JTextField();
			wireSpecs[i].setColumns(15);
			
			label = new JLabel(specLabels[i]);
			label.setFont(new Font("Courier New", Font.PLAIN, 12));
			
			gridBagAdd(wirepane, c, 0, ++c.gridy, label);
			gridBagAdd(wirepane, c, 1, c.gridy, wireSpecs[i]);
		}						
		specsBut = new JButton("Update");
		specsBut.addActionListener(new MyListener());
		simBut = new JButton("Simulate");
		simBut.addActionListener(new MyListener());
		
		gridBagAdd(wirepane, c, 0, ++c.gridy, 2, GridBagConstraints.CENTER, specsBut);		
		gridBagAdd(wirepane, c, 0, ++c.gridy, 2, GridBagConstraints.CENTER, simBut);
		
		// Add panels
		
		leftpane.add(wirepane, BorderLayout.NORTH);
		
		return leftpane;
	}
	
	private JPanel createTopPanel()
	{
		return null;
	}
	
	private JPanel createBottomPanel()
	{
JPanel bottom = new JPanel(new BorderLayout());					
		
		// Simulation
		
		JPanel row2 = new JPanel();
		
		JLabel label = new JLabel("Integrator: ");		
		
		rkSel = new JComboBox<String>();
		rkSel.setPreferredSize(new Dimension(170, 20));
		for (int i = 0; i < propagatorLabels.length; i++)			
			rkSel.addItem(propagatorLabels[i]);		
		rkSel.setSelectedIndex(Sim.RK4);
		rkSel.addActionListener(new MyListener());		
		row2.add(label);
		row2.add(rkSel);
						
		label = new JLabel("Sim step (s):");		
		simStep = new JTextField();
		simStep.setColumns(5);		
		simStepBut = new JButton("Set");
		simStepBut.addActionListener(new MyListener());		
		row2.add(label);
		row2.add(simStep);
		row2.add(simStepBut);
		
		// Display
		
		label = new JLabel("Zoom:");
		zoom = new JTextField();
		zoom.setColumns(5);		
		zoomBut = new JButton("Set");
		zoomBut.addActionListener(new MyListener());		
		row2.add(label);
		row2.add(zoom);
		row2.add(zoomBut);
		
		label = new JLabel("Upscale:");
		scale = new JTextField();
		scale.setColumns(6);
		scaleBut = new JButton("Set");
		scaleBut.addActionListener(new MyListener());		
		row2.add(label);
		row2.add(scale);
		row2.add(scaleBut);

		label = new JLabel("Plot step:");
		plotStep = new JTextField();
		plotStep.setColumns(5);
		plotStepBut = new JButton("Set");
		plotStepBut.addActionListener(new MyListener());
		row2.add(label);
		row2.add(plotStep);
		row2.add(plotStepBut);
		 
		bottom.add(row2, BorderLayout.WEST);
		
		return bottom;
	}
	
	private void gridBagAdd(JPanel panel, GridBagConstraints c, int x, int y, JComponent comp)
	{
		gridBagAdd(panel, c, x, y, 1, GridBagConstraints.FIRST_LINE_START, comp);
		
	}
	
	private void gridBagAdd(JPanel panel, GridBagConstraints c, int x, int y, int width, int align, JComponent comp)
	{
		c.gridx = x;
		c.gridy = y;
		c.gridwidth = width;
		c.anchor = align;		
		panel.add(comp, c);
		
		System.out.println(x + "," + y);
	}
	
	private void separator(JPanel panel, GridBagConstraints c)
	{
		c.gridy++;
		c.gridwidth = 2;
		c.gridx = 0;
		JSeparator sep = new JSeparator();
		//sep.setPreferredSize(new Dimension(100, 20));
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(sep, c);
		c.fill = GridBagConstraints.NONE;
		c.gridwidth = 1;		
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
		button.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, 0));
		button.addActionListener (new MenuListener());
		view.add(button);
		
		button = new JMenuItem ("Zoom out");
		button.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0));
		button.addActionListener (new MenuListener());
		view.add(button);
		
		button = new JMenuItem ("Scale up");
		button.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0));
		button.addActionListener (new MenuListener());
		view.add(button);
		
		button = new JMenuItem ("Scale down");
		button.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0));
		button.addActionListener (new MenuListener());
		view.add(button);
		
		button = new JMenuItem ("Decrease FOV");
		button.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, 0));
		button.addActionListener (new MenuListener());
		view.add(button);
		
		button = new JMenuItem ("Increase FOV");
		button.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, 0));
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
					
		lattice = new double[125][3];
		int counter = 0;
		
		for (int i = -100; i <= 100; i += 50)
			for (int j = -100; j <= 100; j += 50)
				for (int k = -100; k <= 100; k += 50)				
					lattice[counter++] = new double[] {i, j, k};		
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
    	refreshInFields();
    	refreshOtherFields();
    }
    
    public void refreshInFields()
    {
    	if (wire != null)
    	{
    		double[] r = wire.get_position();
    		double[] d = wire.get_direction();
    		wireSpecs[0].setText(Double.toString(r[0]));
    		wireSpecs[1].setText(Double.toString(r[1]));
    		wireSpecs[2].setText(Double.toString(r[2]));
    		wireSpecs[3].setText(Double.toString(d[0]));
    		wireSpecs[4].setText(Double.toString(d[1]));
    		wireSpecs[5].setText(Double.toString(d[2]));
    		wireSpecs[6].setText(Double.toString(wire.current()));
    		if (wire instanceof StraightWire)
    		{
    			wireSpecs[7].setText(Double.toString(wire.get_t2()));
    			wireSpecs[8].setText("");
    			wireSpecs[9].setText("");
    		}
    		else if (wire instanceof Solenoid)
    		{
    			Solenoid sol = (Solenoid)wire;
    			wireSpecs[7].setText(Double.toString(sol.height));
    			wireSpecs[8].setText(Double.toString(sol.radius));
    			wireSpecs[9].setText(Double.toString(sol.turns));
    		}
    	}
    }
    
    public void refreshOtherFields()
	{
    	simStep.setText(Calc.small.format(sim.dt));
    	zoom.setText(Calc.small.format(viewport.zoom));
		scale.setText(Calc.small.format(viewport.scale));
		plotStep.setText(Calc.small.format(viewport.plotStep));		
	}
    
    public void simulate()
    {
    	sim.simulate(lattice);
    	refresh();
    }
    
	
	public void close ()
	{
		dispose();	
	}
		
	
	private class MyMouseListener implements MouseListener, MouseMotionListener
	{

		@Override
		public void mouseClicked(MouseEvent e) 
		{
			viewport.requestFocus();
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
	
	public void setEntitySel()
	{
		int index = entitySel.getSelectedIndex();
		wire = sim.wires.get(index);
		boolean isSolenoid = wire instanceof Solenoid;
		for (int i = 6; i < 10; i++)
			wireSpecs[i].setEditable(isSolenoid);
	}
	
	public void setWireSpecs()
	{
		double rx = Double.parseDouble(wireSpecs[0].getText());
		double ry = Double.parseDouble(wireSpecs[1].getText());
		double rz = Double.parseDouble(wireSpecs[2].getText());
		double dx = Double.parseDouble(wireSpecs[3].getText());
		double dy = Double.parseDouble(wireSpecs[4].getText());
		double dz = Double.parseDouble(wireSpecs[5].getText());
		double i = Double.parseDouble(wireSpecs[6].getText());
		double h = Double.parseDouble(wireSpecs[7].getText());
		if (wire instanceof Solenoid)
		{
			double r = Double.parseDouble(wireSpecs[8].getText());
			double n = Double.parseDouble(wireSpecs[9].getText());
			
			Solenoid sol = (Solenoid)wire;			
			sol.radius = r;
			sol.height = h;
			sol.turns = n;
			sol.current = i;
			sol.origin = new double[] {rx, ry, rz};
			sol.direction = Calc.unit(new double[] {dx, dy, dz});
			sol.updateRotationMatrix();
			sol.resetCache();
		}
		else if (wire instanceof StraightWire)
		{
			StraightWire str = (StraightWire)wire;
			str.origin = new double[] {rx, ry, rz};
			str.direction = Calc.unit(new double[] {dx, dy, dz});
			str.current = i;
			str.t2 = h;
		}
		sim.objects.removeAll(sim.vectors);
		sim.vectors.clear();
		refresh();
	}	
	
	public void setPropagator(int rk)
	{
		sim.integrationMethod = rk;
	}
	
	public void setSimStep()
	{
		try
		{
			double newStep = Double.parseDouble(simStep.getText());
			sim.dt = newStep;
			simulate();
		}
		catch (Exception e)
		{
			refreshOtherFields();
		}		
	}
	
	public void setZoom()
	{
		try
		{
			double newZoom = Double.parseDouble(zoom.getText());
			viewport.setZoom(newZoom);
			refresh();			
		}
		catch (Exception e)
		{
			refreshOtherFields();
		}		
	}
	
	public void setScale()
	{
		try
		{
			double newScale = Double.parseDouble(scale.getText());
			viewport.scale = newScale;
			refresh();
		}
		catch (Exception e)
		{
			refreshOtherFields();
		}	
	}
	
	public void setPlotStep()
	{
		try
		{
			double newStep = Double.parseDouble(plotStep.getText());
			viewport.plotStep = newStep;
			refresh();
		}
		catch (Exception e)
		{
			refreshOtherFields();
		}	
	}
		
	private class MyListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			Object parent = e.getSource();
			if (parent instanceof JButton)
			{
				JButton button = (JButton)parent;
				if (button.equals(specsBut))
					setWireSpecs();
				else if (button.equals(simBut))
					simulate();
				else if (button.equals(simStepBut))
					setSimStep();
				else if (button.equals(zoomBut))
					setZoom();
				else if (button.equals(scaleBut))
					setScale();
				else if (button.equals(plotStepBut))
					setPlotStep();
			}
			else if (parent instanceof JComboBox)
			{
				JComboBox<String> combo = (JComboBox<String>)parent;
				if (combo.equals(entitySel))
					setEntitySel();
				else if (combo.equals(rkSel))
					setPropagator(combo.getSelectedIndex());
			}
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
				double dfov = sneaking ? fovIncrement / sneakFactor : fovIncrement;
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
					viewport.scaleScreen(1/dzoom);
					viewport.enforceFOV();
					refresh();					
				}
				else if (name.equals("Zoom out"))
				{
					viewport.scaleScreen(dzoom);
					viewport.enforceFOV();
					refresh();
				}
				else if (name.equals("Scale up"))
				{
					viewport.scale *= dzoom;
					refresh();					
				}
				else if (name.equals("Scale down"))
				{
					viewport.scale /= dzoom;
					refresh();
					
				}
				else if (name.equals("Increase FOV"))
				{
					viewport.incrementFOV(dfov);
					viewport.enforceFOV();
					refresh();
				}
				else if (name.equals("Decrease FOV"))
				{
					viewport.incrementFOV(-dfov);
					viewport.enforceFOV();
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
