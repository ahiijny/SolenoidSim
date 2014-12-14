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
import javax.swing.JCheckBox;
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
import main.entities.Vector;
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
	public String[] integratorLabels = {"Runge-Kutta, 1st order (RK1)", 
			"Runge-Kutta, 2nd order (RK2)",								
			"Runge-Kutta, 4th order (RK4)",
			"Runge-Kutta, 5th order (RK5)"};
	public JTextField[] wireSpecs;
	public JTextField[][] probeSpecs;
	public JTextField[][] latticeSpecs;
	public JTextField simStep, zoom, scale, fov, plotStep, maxB, maxScale, 
					arrowHeadThreshold, arrowHeadLength, minColor, maxColor, 
					magB, cubeSpecs, plotStepScalar, progressInterval;	
	public JCheckBox probePersist;
	public JButton specsBut, simBut, latticeBut, simStepBut, zoomBut, scaleBut, fovBut, plotStepBut, vectorBut, probeBut, otherBut;
	
	public Cube cube;
	
	public double[][] latticePoints;
	public double[] probePoint;
	public Wire wire;		
	public Vector probeReading;
	
	public int xorBack = Color.white.getRGB();
	public int xorFront = Color.black.getRGB();
	
	public double[][] latticeBounds = {	{-5, 5, 5},
										{-5, 5, 5},
										{-5, 5, 5} };	
	private Timer timer;
	private int keyboardDelay = 50;	
	private HashSet<String> pressedKeys;
	private boolean sneaking = false;	
	
	public Logger logger;
	public SaveLoader saveloader;
	
	public String title;

	public GraphicUI(String title, int width, int height)
	{
		super(title);
		
		this.title = title;
		sim = new Sim(this);	
		viewport = new Viewport(this, sim);
		pressedKeys = new HashSet<String>();
		timer = new Timer(keyboardDelay, new KeyboardAction());
		timer.setInitialDelay(0);

		this.width = width;
		this.height = height;
		
		logger = new Logger(sim);
		saveloader = new SaveLoader(sim, "init.cfg");
		
		setContentPane(createContent());	
		setJMenuBar(createMenuBar());
		setKeyBindings();
		initSim();
		setLattice();
		load();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		setSize(width, height);	
		refresh();
		setVisible(true);			
	}
	
	private JPanel createContent()
	{
		JPanel content = new JPanel(new BorderLayout());
		
		content.add(createMiddlePanel(),BorderLayout.CENTER);
		content.add(createLeftPanel(),BorderLayout.WEST);
		content.add(createRightPanel(),BorderLayout.EAST);
		content.add(createTopPanel(),BorderLayout.NORTH);
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
		JPanel right = new JPanel(new BorderLayout());
		JPanel rightpane = new JPanel(new BorderLayout());
		JPanel vectorpane = new JPanel(new GridBagLayout());
		JPanel probepane = new JPanel(new GridBagLayout());
		JPanel otherpane = new JPanel(new GridBagLayout());
		JLabel label;
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2,4,2,4);
		
		// Vector Panel
		
		JLabel title = new JLabel ("B Vectors");
		title.setFont(new Font("Arial", Font.PLAIN, 14));
		gridBagAdd(vectorpane, c, 0, 0, 2, GridBagConstraints.FIRST_LINE_START, title);
		
		label = new JLabel("Max B");
		label.setFont(new Font("Courier New", Font.PLAIN, 12));
		maxB = new JTextField(15);
		gridBagAdd(vectorpane, c, 0, ++c.gridy, 1, GridBagConstraints.FIRST_LINE_START, label);
		gridBagAdd(vectorpane, c, 1, c.gridy, 1, GridBagConstraints.FIRST_LINE_START, maxB);
		
		label = new JLabel("Max length");
		label.setFont(new Font("Courier New", Font.PLAIN, 12));
		maxScale = new JTextField(15);
		gridBagAdd(vectorpane, c, 0, ++c.gridy, 1, GridBagConstraints.FIRST_LINE_START, label);
		gridBagAdd(vectorpane, c, 1, c.gridy, 1, GridBagConstraints.FIRST_LINE_START, maxScale);
		
		label = new JLabel("Arrowhead limit");
		label.setFont(new Font("Courier New", Font.PLAIN, 12));
		arrowHeadThreshold = new JTextField(15);
		gridBagAdd(vectorpane, c, 0, ++c.gridy, 1, GridBagConstraints.FIRST_LINE_START, label);
		gridBagAdd(vectorpane, c, 1, c.gridy, 1, GridBagConstraints.FIRST_LINE_START, arrowHeadThreshold);
		
		label = new JLabel("Arrowhead size");
		label.setFont(new Font("Courier New", Font.PLAIN, 12));
		arrowHeadLength = new JTextField(15);
		gridBagAdd(vectorpane, c, 0, ++c.gridy, 1, GridBagConstraints.FIRST_LINE_START, label);
		gridBagAdd(vectorpane, c, 1, c.gridy, 1, GridBagConstraints.FIRST_LINE_START, arrowHeadLength);
		
		label = new JLabel("Min color");
		label.setFont(new Font("Courier New", Font.PLAIN, 12));
		minColor = new JTextField(15);
		gridBagAdd(vectorpane, c, 0, ++c.gridy, 1, GridBagConstraints.FIRST_LINE_START, label);
		gridBagAdd(vectorpane, c, 1, c.gridy, 1, GridBagConstraints.FIRST_LINE_START, minColor);
		
		label = new JLabel("Max color");
		label.setFont(new Font("Courier New", Font.PLAIN, 12));
		maxColor = new JTextField(15);
		gridBagAdd(vectorpane, c, 0, ++c.gridy, 1, GridBagConstraints.FIRST_LINE_START, label);
		gridBagAdd(vectorpane, c, 1, c.gridy, 1, GridBagConstraints.FIRST_LINE_START, maxColor);
		
		vectorBut = new JButton("Set");
		vectorBut.addActionListener(new MyListener());
		gridBagAdd(vectorpane, c, 0, ++c.gridy, 2, GridBagConstraints.CENTER, vectorBut);
		
		separator(vectorpane, c, 0, ++c.gridy, 2);
		
		// Probe panel
		
		c.gridx = 0;
		c.gridy = 0;
		
		title = new JLabel ("Probe");
		title.setFont(new Font("Arial", Font.PLAIN, 14));
		gridBagAdd(probepane, c, 0, 0, 4, GridBagConstraints.FIRST_LINE_START, title);
		
		label = new JLabel ("x");
		label.setFont(new Font("Courier New", Font.PLAIN, 12));
		gridBagAdd(probepane, c, 1, ++c.gridy, 1, GridBagConstraints.CENTER, label);
		
		label = new JLabel ("y");
		label.setFont(new Font("Courier New", Font.PLAIN, 12));
		gridBagAdd(probepane, c, 2, c.gridy, 1, GridBagConstraints.CENTER, label);
		
		label = new JLabel ("z");
		label.setFont(new Font("Courier New", Font.PLAIN, 12));
		gridBagAdd(probepane, c, 3, c.gridy, 1, GridBagConstraints.CENTER, label);
		
		label = new JLabel ("Pos");
		label.setFont(new Font("Courier New", Font.PLAIN, 12));
		gridBagAdd(probepane, c, 0, ++c.gridy, 1, GridBagConstraints.FIRST_LINE_START, label);
		
		label = new JLabel ("B");
		label.setFont(new Font("Courier New", Font.PLAIN, 12));
		gridBagAdd(probepane, c, 0, ++c.gridy, 1, GridBagConstraints.FIRST_LINE_START, label);
		
		c.gridy -= 2;
		
		probeSpecs = new JTextField[2][3];
		for (int i = 0; i < 2; i++)
		{
			c.gridy++;
			for (int j = 0; j < 3; j++)
			{
				probeSpecs[i][j] = new JTextField(7);
				gridBagAdd(probepane, c, 1+j, c.gridy, 1, GridBagConstraints.FIRST_LINE_START, probeSpecs[i][j]);
			}
		}
		
		for (int i = 0; i < 3; i++)
			probeSpecs[1][i].setEditable(false);
		
		label = new JLabel("B mag");
		label.setFont(new Font("Courier New", Font.PLAIN, 12));
		magB = new JTextField(6);
		magB.setEditable(false);
		gridBagAdd(probepane, c, 0, ++c.gridy, 1, GridBagConstraints.FIRST_LINE_START, label);
		c.fill = GridBagConstraints.HORIZONTAL;
		gridBagAdd(probepane, c, 1, c.gridy, 3, GridBagConstraints.FIRST_LINE_START, magB);
		c.fill = GridBagConstraints.NONE;
		
		probeBut = new JButton("Probe");
		probeBut.addActionListener(new MyListener());
		gridBagAdd(probepane, c, 0, ++c.gridy, 4, GridBagConstraints.CENTER, probeBut);
		
		separator(probepane, c, 0, ++c.gridy, 4);
		
		// Other Pane
				
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		
		title = new JLabel ("Other settings");
		title.setFont(new Font("Arial", Font.PLAIN, 14));
		gridBagAdd(otherpane, c, 0, 0, 2, GridBagConstraints.FIRST_LINE_START, title);
		
		label = new JLabel("Cube length");
		label.setFont(new Font("Courier New", Font.PLAIN, 12));
		gridBagAdd(otherpane, c, 0, ++c.gridy, 1, GridBagConstraints.FIRST_LINE_START, label);		
		cubeSpecs = new JTextField(18);
		gridBagAdd(otherpane, c, 1, c.gridy, 1, GridBagConstraints.FIRST_LINE_START, cubeSpecs);
		
		label = new JLabel("Show progress");
		label.setFont(new Font("Courier New", Font.PLAIN, 12));
		gridBagAdd(otherpane, c, 0, ++c.gridy, 1, GridBagConstraints.FIRST_LINE_START, label);
		progressInterval = new JTextField(18);
		gridBagAdd(otherpane, c, 1, c.gridy, 1, GridBagConstraints.FIRST_LINE_START, progressInterval);
		
		c.fill = GridBagConstraints.NONE;
		
		otherBut = new JButton("Set");
		otherBut.addActionListener(new MyListener());
		gridBagAdd(otherpane, c, 0, ++c.gridy, 2, GridBagConstraints.CENTER, otherBut);
				
		// Add panels
		
		rightpane.add(vectorpane, BorderLayout.NORTH);
		rightpane.add(probepane, BorderLayout.CENTER);
		rightpane.add(otherpane, BorderLayout.SOUTH);
		right.add(rightpane, BorderLayout.NORTH);
		return right;
	}
	
	private JPanel createLeftPanel()
	{
		JPanel left = new JPanel(new BorderLayout());
		JPanel leftpane = new JPanel(new BorderLayout());
		JPanel wirepane = new JPanel(new GridBagLayout());
		JPanel simpane = new JPanel(new GridBagLayout());
		JLabel label;
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2,4,2,4);
		
		// Input data
		
		entitySel = new JComboBox<String>();
		entitySel.setPreferredSize(new Dimension(200, 20));
		entitySel.addActionListener(new MyListener());
		
		gridBagAdd(wirepane, c, 0, 0, new JLabel("Select wire"));
		gridBagAdd(wirepane, c, 0, 1, 2, GridBagConstraints.CENTER, entitySel);
		
		// Input fields
		
		wireSpecs = new JTextField[specLabels.length];
		
		for (int i = 0; i < wireSpecs.length; i++)
		{	
			if (i == 0)
			{
				separator(wirepane, c, 0, ++c.gridy, 2);
				
				label = new JLabel("Position");
				gridBagAdd(wirepane, c, 0, ++c.gridy, label);
			}
			else if (i == 3)
			{
				separator(wirepane, c, 0, ++c.gridy, 2);
				
				label = new JLabel("Direction");
				gridBagAdd(wirepane, c, 0, ++c.gridy, label);
			}
			else if (i == 6)
			{
				separator(wirepane, c, 0, ++c.gridy, 2);
				
				label = new JLabel("Other Parameters");
				gridBagAdd(wirepane, c, 0, ++c.gridy, label);
			}
			
			wireSpecs[i] = new JTextField();
			wireSpecs[i].setColumns(11);
			
			label = new JLabel(specLabels[i]);
			label.setFont(new Font("Courier New", Font.PLAIN, 12));
			
			gridBagAdd(wirepane, c, 0, ++c.gridy, label);
			gridBagAdd(wirepane, c, 1, c.gridy, wireSpecs[i]);
		}			
		
		separator(wirepane, c, 0, ++c.gridy, 2);
		
		label = new JLabel("Scale plot step");
		label.setFont(new Font("Courier New", Font.PLAIN, 12));
		plotStepScalar = new JTextField(11);
		
		gridBagAdd(wirepane, c, 0, ++c.gridy, label);
		gridBagAdd(wirepane, c, 1, c.gridy, plotStepScalar);
		
		specsBut = new JButton("Set");
		specsBut.addActionListener(new MyListener());		
		
		gridBagAdd(wirepane, c, 0, ++c.gridy, 2, GridBagConstraints.CENTER, specsBut);
		
		// Sim pane
		
		c.gridx = 0;
		c.gridy = 0;
		
		separator(simpane, c, 0, c.gridy, 4);
		
		// Vector Lattice
		
		JLabel title = new JLabel ("Simulation");
		title.setFont(new Font("Arial", Font.PLAIN, 14));
		gridBagAdd(simpane, c, 0, ++c.gridy, 4, GridBagConstraints.FIRST_LINE_START, title);
		
		label = new JLabel("Vector Lattice");
		label.setFont(new Font("Courier New", Font.PLAIN, 12));
		gridBagAdd(simpane, c, 0, ++c.gridy, 4, GridBagConstraints.FIRST_LINE_START, label);
		
		label = new JLabel("min");
		label.setFont(new Font("Courier New", Font.PLAIN, 12));
		gridBagAdd(simpane, c, 1, ++c.gridy, 1, GridBagConstraints.CENTER, label);
		
		label = new JLabel("max");
		label.setFont(new Font("Courier New", Font.PLAIN, 12));
		gridBagAdd(simpane, c, 2, c.gridy, 1, GridBagConstraints.CENTER, label);
		
		label = new JLabel("n");
		label.setFont(new Font("Courier New", Font.PLAIN, 12));
		gridBagAdd(simpane, c, 3, c.gridy, 1, GridBagConstraints.CENTER, label);
		
		// Lattice Input
		
		latticeSpecs = new JTextField[3][3];
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 2; j++)
				latticeSpecs[i][j] = new JTextField(6);
		for (int i = 0; i < 3; i++)
			latticeSpecs[i][2] = new JTextField(4);
		
		label = new JLabel("x : ");
		label.setFont(new Font("Courier New", Font.PLAIN, 12));
		gridBagAdd(simpane, c, 0, ++c.gridy, 1, GridBagConstraints.FIRST_LINE_START, label);	
		
		for (int i = 0; i < 3; i++)
			gridBagAdd(simpane, c, i+1, c.gridy, 1, GridBagConstraints.FIRST_LINE_START, latticeSpecs[0][i]);	
		
		label = new JLabel("y : ");
		label.setFont(new Font("Courier New", Font.PLAIN, 12));
		gridBagAdd(simpane, c, 0, ++c.gridy, 1, GridBagConstraints.FIRST_LINE_START, label);
		
		for (int i = 0; i < 3; i++)
			gridBagAdd(simpane, c, i+1, c.gridy, 1, GridBagConstraints.FIRST_LINE_START, latticeSpecs[1][i]);	
		
		label = new JLabel("z : ");
		label.setFont(new Font("Courier New", Font.PLAIN, 12));
		gridBagAdd(simpane, c, 0, ++c.gridy, 1, GridBagConstraints.FIRST_LINE_START, label);
		
		for (int i = 0; i < 3; i++)
			gridBagAdd(simpane, c, i+1, c.gridy, 1, GridBagConstraints.FIRST_LINE_START, latticeSpecs[2][i]);
		
		latticeBut = new JButton ("Update");
		latticeBut.addActionListener(new MyListener());
		gridBagAdd(simpane, c, 0, ++c.gridy, 4, GridBagConstraints.CENTER, latticeBut);			
		
		// Add panels
		
		leftpane.add(wirepane, BorderLayout.NORTH);
		leftpane.add(simpane, BorderLayout.SOUTH);
		left.add(leftpane, BorderLayout.NORTH);
		
		return left;
	}
	
	private void separator(JPanel panel, GridBagConstraints c, int x, int y, int width)
	{
		c.gridx = x;
		c.gridy = y;
		c.gridwidth = width;
		JSeparator sep = new JSeparator();
		//sep.setPreferredSize(new Dimension(100, 20));
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(sep, c);
		c.fill = GridBagConstraints.NONE;
	}
	
	private JPanel createTopPanel()
	{
		JPanel top = new JPanel(new BorderLayout());
		JPanel row = new JPanel();
		
		simBut = new JButton("SIMULATE");
		simBut.addActionListener(new MyListener());		
		row.add(simBut);
		
		top.add(row, BorderLayout.CENTER);
		
		return top;
	}
	
	private JPanel createBottomPanel()
	{
		JPanel bottom = new JPanel(new BorderLayout());					
		
		// Simulation
		
		JPanel row2 = new JPanel();
						
		JLabel label = new JLabel("Integrator: ");		
		
		rkSel = new JComboBox<String>();
		rkSel.setPreferredSize(new Dimension(170, 20));
		for (int i = 0; i < integratorLabels.length; i++)			
			rkSel.addItem(integratorLabels[i]);		
		rkSel.setSelectedIndex(Sim.RK4);
		rkSel.addActionListener(new MyListener());		
		row2.add(label);
		row2.add(rkSel);
						
		label = new JLabel("Sim step:");		
		simStep = new JTextField();
		simStep.setColumns(5);		
		simStepBut = new JButton("Update");
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
		
		label = new JLabel("FOV(\u00B0):");
		fov = new JTextField();
		fov.setColumns(6);
		fovBut = new JButton("Set");
		fovBut.addActionListener(new MyListener());		
		row2.add(label);
		row2.add(fov);
		row2.add(fovBut);

		label = new JLabel("Plot step:");
		plotStep = new JTextField();
		plotStep.setColumns(5);
		plotStepBut = new JButton("Set");
		plotStepBut.addActionListener(new MyListener());
		row2.add(label);
		row2.add(plotStep);
		row2.add(plotStepBut);
		
		label = new JLabel("Probe persist: ");
		probePersist = new JCheckBox();
		row2.add(label);
		row2.add(probePersist);
		 
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
		file.setMnemonic('f');
		
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
		
		file.add(new JSeparator());

		button = new JMenuItem ("Exit"); // exit button
		button.setMnemonic('x');
		button.addActionListener (new MenuListener ());
		file.add(button);
		
		// "Simulation" Menu
		simulation = new JMenu ("Simulation");
		simulation.setMnemonic('s');
		
		button = new JMenuItem ("Add straight wire");
		button.addActionListener (new MenuListener());
		simulation.add(button);
		
		button = new JMenuItem ("Add solenoid");
		button.addActionListener (new MenuListener());
		simulation.add(button);
		
		button = new JMenuItem ("Default sim step");
		button.addActionListener (new MenuListener());
		simulation.add(button);
		
		button = new JMenuItem ("Remove selected wire");
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
		
		view.add(new JSeparator());
				
		button = new JMenuItem ("Default zoom");
		button.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SLASH, 0));
		button.addActionListener (new MenuListener());
		view.add(button);				
		
		button = new JMenuItem ("Default plot step");
		button.addActionListener (new MenuListener());
		view.add(button);
		
		button = new JMenuItem ("Reset camera");
		button.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
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
		
		button = new JMenuItem ("Controls"); // about button
		button.setMnemonic('c');
		button.addActionListener (new MenuListener ());
		help.add(button);

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
		cube = new Cube(origin, 15, Color.black);
		sim.add(cube);	
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
    	refreshLeftFields();
    	refreshBottomFields();
    	refreshRightFields();
    }
    
    public void refreshLeftFields()
    {
    	// Wire specs
    	if (wire != null)
    	{
    		double[] r = wire.get_position();
    		double[] d = wire.get_direction();
    		wireSpecs[0].setText(Calc.precise.format(r[0]));
    		wireSpecs[1].setText(Calc.precise.format(r[1]));
    		wireSpecs[2].setText(Calc.precise.format(r[2]));
    		wireSpecs[3].setText(Calc.precise.format(d[0]));
    		wireSpecs[4].setText(Calc.precise.format(d[1]));
    		wireSpecs[5].setText(Calc.precise.format(d[2]));
    		wireSpecs[6].setText(Calc.precise.format(wire.current()));
    		if (wire instanceof StraightWire)
    		{
    			wireSpecs[7].setText(Calc.precise.format(wire.get_t2()));
    			wireSpecs[8].setText(" ");
    			wireSpecs[9].setText(" ");
    			plotStepScalar.setText(Calc.smaller.format(((StraightWire)wire).plotStepScalar));
    		}
    		else if (wire instanceof Solenoid)
    		{
    			Solenoid sol = (Solenoid)wire;
    			wireSpecs[7].setText(Calc.precise.format(sol.height));
    			wireSpecs[8].setText(Calc.precise.format(sol.radius));
    			wireSpecs[9].setText(Calc.precise.format(sol.turns));
    			plotStepScalar.setText(Calc.smaller.format(((Solenoid)wire).plotStepScalar));
    		}
    	}
    	else
    	{
    		for (int i = 0; i < wireSpecs.length; i++)
    		{
    			wireSpecs[i].setText(" ");
    		}
    	}
    	
    	// Lattice specs
    	
    	for (int i = 0; i < 3; i++)
    		for (int j = 0; j < 2; j++)
    			latticeSpecs[i][j].setText(Calc.smaller.format(latticeBounds[i][j]));
    	for (int i = 0; i < 3; i++)
    		latticeSpecs[i][2].setText(Calc.whole.format(latticeBounds[i][2]));
    }
    
    public void refreshBottomFields()
	{
    	simStep.setText(Calc.smaller.format(sim.dt));
    	zoom.setText(Calc.smaller.format(viewport.zoom));
		scale.setText(Calc.small.format(viewport.scale));
		fov.setText(Calc.whole.format(Math.toDegrees(viewport.fov)));
		plotStep.setText(Calc.smaller.format(viewport.plotStep));		
	}
    
    public void refreshRightFields()
    {
    	maxB.setText(Calc.larger.format(sim.maxB));
    	maxScale.setText(Calc.smaller.format(sim.maxScale));
    	arrowHeadThreshold.setText(Calc.smaller.format(Vector.arrowHeadThreshold));
    	arrowHeadLength.setText(Calc.smaller.format(Vector.arrowHeadLength));
    	minColor.setText(Calc.smaller.format(sim.minColor));    	
    	maxColor.setText(Calc.smaller.format(sim.maxColor));
    	
    	Color min = Vector.decimalToHue(sim.minColor, 0, 1);
    	Color max = Vector.decimalToHue(sim.maxColor, 0, 1);
    	minColor.setBackground(min);
    	maxColor.setBackground(max);
    	minColor.setForeground(new Color(xorBack^xorFront^min.getRGB()));
    	maxColor.setForeground(new Color(xorBack^xorFront^max.getRGB()));
    	
    	if (probeReading != null)
    	{
    		for (int i = 0; i < 3; i++)
    			probeSpecs[1][i].setText(Calc.large.format(probeReading.value[i]));
    		magB.setText(Double.toString(Calc.mag(probeReading.value)));
    	}
    	else
    	{
    		for (int i = 0; i < 3; i++)
    			probeSpecs[1][i].setText(" ");
    		magB.setText(" ");
    	} 	
    	
    	cubeSpecs.setText(Calc.smaller.format(cube.width));
    	progressInterval.setText(Calc.whole.format(sim.progressInterval));
    }
    
    public void simulate()
    {
    	sim.simulate(latticePoints);
    	if (probePoint != null)
    		setProbe();
    	refresh();
    }
    
    public void updateProcess()
    {
    	setTitle(title + " (" + sim.process + ")");
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
	
	public void setEntitySel(int index)
	{
		if (sim.wires.size() != 0)
		{
			wire = sim.wires.get(index);
			boolean isSolenoid = wire instanceof Solenoid;
			for (int i = 8; i < 10; i++)
			{
				wireSpecs[i].setEditable(isSolenoid);
				if (!isSolenoid)
					wireSpecs[i].setText(" ");
			}
			refreshLeftFields();
		}
	}
	
	public void setVectors()
	{
		try
		{
			double newMaxB = Double.parseDouble(maxB.getText());
			double newMaxScale = Double.parseDouble(maxScale.getText());
			double newArrowHeadThreshold = Double.parseDouble(arrowHeadThreshold.getText());
			double newArrowHeadLength = Double.parseDouble(arrowHeadLength.getText());
			double newMinColor = Double.parseDouble(minColor.getText());
			double newMaxColor = Double.parseDouble(maxColor.getText());
			
			Vector.arrowHeadThreshold = newArrowHeadThreshold;
			Vector.arrowHeadLength = newArrowHeadLength;
			sim.minColor = newMinColor;
			sim.maxColor = newMaxColor;
			sim.rescaleVectors(newMaxB, newMaxScale);
			
			refresh();
		}
		catch (Exception e)
		{
			refreshRightFields();
		}
		
	}	
	
	public void setWireSpecs()
	{
		try
		{
			double rx = Double.parseDouble(wireSpecs[0].getText());
			double ry = Double.parseDouble(wireSpecs[1].getText());
			double rz = Double.parseDouble(wireSpecs[2].getText());
			double dx = Double.parseDouble(wireSpecs[3].getText());
			double dy = Double.parseDouble(wireSpecs[4].getText());
			double dz = Double.parseDouble(wireSpecs[5].getText());
			double i = Double.parseDouble(wireSpecs[6].getText());
			double h = Double.parseDouble(wireSpecs[7].getText());
			double scale = Double.parseDouble(plotStepScalar.getText());
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
				sol.plotStepScalar = scale;
				sol.updateRotationMatrix();
				sol.resetCache();				
			}
			else if (wire instanceof StraightWire)
			{
				StraightWire str = (StraightWire)wire;
				str.origin = new double[] {rx, ry, rz};
				str.direction = Calc.unit(new double[] {dx, dy, dz});
				str.plotStepScalar = scale;
				str.current = i;
				str.t2 = h;
			}
			sim.objects.removeAll(sim.vectors);
			sim.vectors.clear();
			probeReading = null;
			probePoint = null;
			refresh();
		}
		catch (Exception e)
		{
			refreshLeftFields();
		}
	}	
	
	public void readLatticeSpecs()
	{
		try
		{
			// Read in values
			
			latticeBounds[0][2] = Integer.parseInt(latticeSpecs[0][2].getText());
			latticeBounds[1][2] = Integer.parseInt(latticeSpecs[1][2].getText());
			latticeBounds[2][2] = Integer.parseInt(latticeSpecs[2][2].getText());
			
			for (int i = 0; i < 3; i++)
				if (latticeBounds[i][2] < 0)
					latticeBounds[i][2] = 0;
			
			latticeBounds[0][0] = Double.parseDouble(latticeSpecs[0][0].getText());
			latticeBounds[0][1] = Double.parseDouble(latticeSpecs[0][1].getText());
			latticeBounds[1][0] = Double.parseDouble(latticeSpecs[1][0].getText());
			latticeBounds[1][1] = Double.parseDouble(latticeSpecs[1][1].getText());
			latticeBounds[2][0] = Double.parseDouble(latticeSpecs[2][0].getText());
			latticeBounds[2][1] = Double.parseDouble(latticeSpecs[2][1].getText());				
		}
		catch (Exception e)
		{
			refreshLeftFields();
		}
	}
	
	public void setFOV()
	{
		try
		{
			double newFOV = Math.toRadians(Double.parseDouble(fov.getText()));
			viewport.incrementFOV(newFOV - viewport.fov);			
		}
		catch (Exception e)
		{
			refreshBottomFields();
		}
	}
	
	public void setCube()
	{
		try
		{
			double width = Double.parseDouble(cubeSpecs.getText());
			sim.remove(cube);
			cube = new Cube(new double[] {0, 0, 0}, width, Color.black);
			sim.add(cube);
		}
		catch (Exception e)		
		{
			refreshRightFields();
		}
	}
	
	public void setProgressInterval()
	{
		try
		{
			int dn = Integer.parseInt(progressInterval.getText());
			sim.progressInterval = dn;
			System.out.println(dn);
		}
		catch (Exception e)		
		{
			refreshRightFields();
		}
	}
	
	public void setLattice()
	{
		// Take parameters from latticeSpecs array
		
		int xn = (int)(latticeBounds[0][2]);
		int yn = (int)(latticeBounds[1][2]);
		int zn = (int)(latticeBounds[2][2]);
		
		if (xn * yn * zn == 0)
			latticePoints = new double[0][3];
		else
		{			
			double xmin = latticeBounds[0][0];
			double xmax = latticeBounds[0][1];
			double ymin = latticeBounds[1][0];
			double ymax = latticeBounds[1][1];
			double zmin = latticeBounds[2][0];
			double zmax = latticeBounds[2][1];
			
			double dx = (xmax - xmin) / (xn - 1);
			double dy = (ymax - ymin) / (yn - 1);
			double dz = (zmax - zmin) / (zn - 1);
			
			// Store lattice points
						
			latticePoints = new double[xn*yn*zn][3];
			int counter = 0;
			
			for (double i = xmin; i <= xmax; i += dx)
				for (double j = ymin; j <= ymax; j += dy)
					for (double k = zmin; k <= zmax; k += dz)		
						latticePoints[counter++] = new double[] {i, j, k};
		}
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
		}
		catch (Exception e)
		{
			refreshBottomFields();
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
			refreshBottomFields();
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
			refreshBottomFields();
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
			refreshBottomFields();
		}	
	}
	
	public void setProbe()
	{
		try
		{
			double px = Double.parseDouble(probeSpecs[0][0].getText());
			double py = Double.parseDouble(probeSpecs[0][1].getText());
			double pz = Double.parseDouble(probeSpecs[0][2].getText());
			probePoint = new double[] {px, py, pz};			
			sim.simulate(new double[][] {probePoint}, false, probeReading != null && !probePersist.isSelected());			
			probeReading = sim.vectors.get(sim.vectors.size() - 1);
			refresh();
		}
		catch (Exception e)
		{
			if (probeSpecs[0][0].getText().equals("") &&
				probeSpecs[0][1].getText().equals("") &&
				probeSpecs[0][2].getText().equals(""))
			{
				probePoint = null;
				probeReading = null;
			}
			refreshRightFields();
		}
	}
	
	public void addSolenoid()
	{
		Solenoid sol = new Solenoid();
		sim.vectors.clear();
		sim.addWire(sol);	
		probeReading = null;
		
		int newIndex = entitySel.getItemCount() - 1;
		entitySel.setSelectedIndex(newIndex);
		setEntitySel(newIndex);
		refresh();
	}
	
	public void addStraightWire()
	{
		StraightWire wir = new StraightWire();
		sim.vectors.clear();
		sim.addWire(wir);	
		probeReading = null;
		
		int newIndex = entitySel.getItemCount() - 1;
		entitySel.setSelectedIndex(newIndex);
		setEntitySel(newIndex);
		refresh();
	}
	
	public void removeSelectedWire()
	{
		if (sim.wires.size() > 0)
		{
			int index = entitySel.getSelectedIndex();
			sim.removeWire(sim.wires.get(index));
			if (index >= sim.wires.size() && index > 0)
				entitySel.setSelectedIndex(index - 1);
			else
				wire = null;
			refresh();
		}
	}
	
	public void load()
	{
		String message;
		try
		{
			// Remove all Wires
			
			while (wire != null)
				removeSelectedWire();
			
			// Load from file
			
			saveloader.loadSave();						
			setLattice();
			setCube();			
			message = "Successfully loaded from init.cfg.";
			JOptionPane.showMessageDialog(GraphicUI.this, message, "Load State", JOptionPane.PLAIN_MESSAGE);
		}
		catch (Exception ex)
		{
		}	
	}
	
	public void save()
	{
		String message;
		try
		{
			saveloader.writeSave();
			message = "Successfully saved to init.cfg.";
			JOptionPane.showMessageDialog(GraphicUI.this, message, "Save State", JOptionPane.PLAIN_MESSAGE);
		}
		catch (Exception ex)
		{
		}		
	}
	
	public void export()
	{
		String message;
		try
		{
			logger.log();
			logger.write();
			message = "Exported to data.csv.";						
		}
		catch (Exception ex)
		{
			message = ex.getLocalizedMessage();
		}
		JOptionPane.showMessageDialog(GraphicUI.this, message, "Export Data", JOptionPane.PLAIN_MESSAGE);
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
				else if (button.equals(latticeBut))
				{
					readLatticeSpecs();
					setLattice();
					simulate();
				}
				else if (button.equals(simBut))
					simulate();
				else if (button.equals(simStepBut))
				{
					setSimStep();
					simulate();
				}
				else if (button.equals(zoomBut))
					setZoom();
				else if (button.equals(scaleBut))
					setScale();
				else if (button.equals(plotStepBut))
					setPlotStep();
				else if (button.equals(vectorBut))
					setVectors();
				else if (button.equals(probeBut))
					setProbe();
				else if (button.equals(otherBut))
				{
					setCube();
					setProgressInterval();
					refresh();
				}
			}
			else if (parent instanceof JComboBox)
			{
				JComboBox<String> combo = (JComboBox<String>)parent;
				if (combo.equals(entitySel))
					setEntitySel(entitySel.getSelectedIndex());
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
				if (name.equals("Load State"))
				{
					load();
					refresh();
				}
				else if (name.equals("Save State"))
					save();
				else if (name.equals("Export to CSV"))
					export();
				else if (name.equals("Exit"))
					close();
				else if (name.equals("Add solenoid"))
					addSolenoid();
				else if (name.equals("Add straight wire"))
					addStraightWire();
				else if (name.equals("Remove selected wire"))
					removeSelectedWire();
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
					viewport.scaleScale(dzoom);
					refresh();					
				}
				else if (name.equals("Scale down"))
				{
					viewport.scaleScale(1/dzoom);
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
				else if (name.equals("Default sim step"))
				{
					sim.dt = Sim.defaultDt;
					refresh();
				}
				else if (name.equals("Default plot step"))
				{
					viewport.plotStep = Viewport.defaultPlotStep;
					refresh();
				}
				else if (name.equals("Reset camera"))
				{
					viewport.resetCamera();
					refresh();
				}
				else if (name.equals("Refresh"))
					refresh();
				else if (name.equals("Controls"))
				{
					String message = "Shortcut keys:\n";
					message += "Numpad 1: yaw left\n";
					message += "Numpad 3: yaw right\n";
					message += "Numpad 4: roll left\n";
					message += "Numpad 6: roll right\n";
					message += "Numpad 2: pitch down\n";
					message += "Numpad 8: pitch up\n";										
					message += "z: hold for finer adjustment\n";
					JOptionPane.showMessageDialog(GraphicUI.this, message, "Controls", JOptionPane.PLAIN_MESSAGE);
				}
				else if (name.equals("About"))
				{
					String message = "Version: 2014.12.11\n";					
					message += "Program by: Jiayin Huang\n";
					message += "Solly Sim.\n";
					JOptionPane.showMessageDialog(GraphicUI.this, message, "About", JOptionPane.PLAIN_MESSAGE);			
				}				
			}			
		}
	}

}
