import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.*;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import com.adobe.flash.abc.visitors.IABCVisitor;
import com.adobe.flash.swf.tags.DoABCTag;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.javadocking.DockingManager;
import com.javadocking.dock.BorderDock;
import com.javadocking.dock.CompositeLineDock;
import com.javadocking.dock.FloatDock;
import com.javadocking.dock.GridDock;
import com.javadocking.dock.LineDock;
import com.javadocking.dock.Position;
import com.javadocking.dock.SplitDock;
import com.javadocking.dock.TabDock;
import com.javadocking.dock.docker.BorderDocker;
import com.javadocking.dock.factory.CompositeToolBarDockFactory;
import com.javadocking.dock.factory.LeafDockFactory;
import com.javadocking.dock.factory.ToolBarDockFactory;
import com.javadocking.dockable.ButtonDockable;
import com.javadocking.dockable.DefaultDockable;
import com.javadocking.dockable.Dockable;
import com.javadocking.dockable.DockableState;
import com.javadocking.dockable.DockingMode;
import com.javadocking.dockable.StateActionDockable;
import com.javadocking.dockable.action.DefaultDockableStateAction;
import com.javadocking.dockable.action.DefaultDockableStateActionFactory;
import com.javadocking.drag.DragListener;
import com.javadocking.drag.DraggerFactory;
import com.javadocking.drag.StaticDraggerFactory;
import com.javadocking.drag.painter.CompositeDockableDragPainter;
import com.javadocking.drag.painter.DockableDragPainter;
import com.javadocking.event.DockingEvent;
import com.javadocking.event.DockingListener;
import com.javadocking.model.DefaultDockingPath;
import com.javadocking.model.DockModel;
import com.javadocking.model.DockModelUtil;
import com.javadocking.model.DockingPath;
import com.javadocking.model.FloatDockModel;
import swftool.*;
import com.javadocking.util.LAF;
import com.javadocking.util.LookAndFeelUtil;
import com.javadocking.util.SampleComponentFactory;
import com.javadocking.util.ToolBarButton;
import com.javadocking.visualizer.DockingMinimizer;
import com.javadocking.visualizer.FloatExternalizer;
import com.javadocking.visualizer.SingleMaximizer;
import swftool.gencode.CodeGen;
import swftool.swftree.CodeInfo;
import swftool.swftree.CodeView;
import swftool.swftree.SwfTree;

/**
 * In this example graphical components are put in dockables.
 * The dockables can be dragged and docked in different docks.
 * 
 * Every dockable can be closed. When the dockable is added again later,
 * the dockable is docked as good as possible like it was docked before.
 * 
 * All the dockables can be minimized. The minimized components are also
 * put in dockables. They can be dragged and docked in other docks.
 * They can also be moved in their bar.
 * 
 * There are some tool bars with buttons. The buttons can be moved in their
 * tool bar or dragged to other tool bars. The tool bars can also be dragged
 * to other borders or they can be made floating.
 * 
 * The structure of the application window is like this:
 * 		First there is a border dock for tool bars with buttons. 
 * 		Inside that border dock is a minimizer that minimizes the dockables at the borders. 
 * 		Inside the minimizer is a maximizer. 
 * 		Inside the maximizer is the root dock for all the normal docks.
 * 
 * @author Heidi Rakels
 */
public class Main extends JPanel
{

	// Static fields.

	public static final int 			FRAME_WIDTH 	= 900;
	public static final int 			FRAME_HEIGHT 	= 650;
	public static LAF[] 				LAFS;
	
	// Fields.
	
	/** All the dockables of the application. */
	private Dockable[] dockables;
	/** All the dockables for buttons of the application. */
	private Dockable[] buttonDockables;

	// Constructors.

	public Main(JFrame frame)
	{
		
		super(new BorderLayout());

		// Create the dock model for the docks, minimizer and maximizer.
		FloatDockModel dockModel = new FloatDockModel("workspace.dck");
		String frameId = "frame";
		dockModel.addOwner(frameId, frame);

		// Give the dock model to the docking manager.
		DockingManager.setDockModel(dockModel);

		// Set our custom component factory.
		DockingManager.setComponentFactory(new SampleComponentFactory());

		// Create the content components.
		SwfToolUI swfmixer = new SwfToolUI();
		SwfTree contactTree = new SwfTree();


		// The arrays for the dockables and button dockables.
		dockables = new Dockable[2];
		buttonDockables = new Dockable[42];

		// Create the dockables around the content components.
		dockables[0] = createDockable("swfmixer", 	 swfmixer,      "swfmixer",  new ImageIcon(getClass().getResource("/com/javadocking/resources/images/text12.gif")),     "<html>De Bello Gallico: Liber 1<br><i>Gaius Julius Caesar</i><html>");
		dockables[1] = createDockable("Swf",contactTree,"Swf",new ImageIcon(getClass().getResource("/com/javadocking/resources/images/person12.gif")),   "Sales Contacts");

		// The dockable with the find panel may not be maximized.
		//((DefaultDockable)dockables[10]).setPossibleStates(DockableState.CLOSED | DockableState.NORMAL | DockableState.MINIMIZED | DockableState.EXTERNALIZED);
		
		// Add actions to the dockables.
		for (int index = 0; index < dockables.length; index++)
		{
			if (index == 10)
			{
				// All actions, except the maximize.
				dockables[index] = addLimitActions(dockables[index]);
			}
			else
			{
				// All actions.
				dockables[index] = addAllActions(dockables[index]);
			}
		}
		
		// Create the buttons with a dockable around.
		buttonDockables[0]  = createButtonDockable("ButtonDockableAdd",              "Add",               new ImageIcon(getClass().getResource("/com/javadocking/resources/icons/add.png")),               "Add!");

		// Give the float dock a different child dock factory.
		// We don't want the floating docks to be splittable.
		FloatDock floatDock = dockModel.getFloatDock(frame);
		floatDock.setChildDockFactory(new LeafDockFactory(false));

		// Create the tab docks.
		TabDock centerTabbedDock = new TabDock();
		TabDock bottomTabbedDock = new TabDock();
		TabDock leftTabbedDock = new TabDock();
		TabDock rightTabbedDock = new TabDock();

		// Add the dockables to these tab docks.
		centerTabbedDock.addDockable(dockables[0], new Position(0));
		centerTabbedDock.setSelectedDockable(dockables[0]);

		leftTabbedDock.addDockable(dockables[1], new Position(0));
		//rightTabbedDock.addDockable(dockables[13], new Position(0));

		// The 4 windows have to be splittable.
		SplitDock centerSplitDock = new SplitDock();
		centerSplitDock.addChildDock(centerTabbedDock, new Position(Position.CENTER));
		//centerSplitDock.addChildDock(rightTabbedDock, new Position(Position.RIGHT));
		centerSplitDock.setDividerLocation(530);
		SplitDock bottomSplitDock = new SplitDock();
		//bottomSplitDock.addChildDock(bottomTabbedDock, new Position(Position.CENTER));
		SplitDock rightSplitDock = new SplitDock();
		rightSplitDock.addChildDock(centerSplitDock, new Position(Position.CENTER));
		rightSplitDock.addChildDock(bottomSplitDock, new Position(Position.BOTTOM));
		rightSplitDock.setDividerLocation(400);
		SplitDock leftSplitDock = new SplitDock();
		leftSplitDock.addChildDock(leftTabbedDock, new Position(Position.CENTER));
		SplitDock totalSplitDock = new SplitDock();
		totalSplitDock.addChildDock(leftSplitDock, new Position(Position.LEFT));
		totalSplitDock.addChildDock(rightSplitDock, new Position(Position.RIGHT));
		totalSplitDock.setDividerLocation(180);
		
		// Add the root dock to the dock model.
		dockModel.addRootDock("totalDock", totalSplitDock, frame);

		// Dockable 10 should float. Add dockable 10 to the float dock of the dock model (this is a default root dock).
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		// Create a maximizer and add it to the dock model.
		SingleMaximizer maximizePanel = new SingleMaximizer(totalSplitDock);
		dockModel.addVisualizer("maximizer", maximizePanel, frame);
		
		// Create a docking minimizer and add it to the dock model.
		BorderDock minimizerBorderDock = new BorderDock(new ToolBarDockFactory());
		minimizerBorderDock.setMode(BorderDock.MODE_MINIMIZE_BAR);
		minimizerBorderDock.setCenterComponent(maximizePanel);
		BorderDocker borderDocker = new BorderDocker();
		borderDocker.setBorderDock(minimizerBorderDock);
		DockingMinimizer minimizer = new DockingMinimizer(borderDocker);
		dockModel.addVisualizer("minimizer", minimizer, frame);
		
		// Add an externalizer to the dock model.
		dockModel.addVisualizer("externalizer", new FloatExternalizer(frame), frame);
		
		// Create the tool bar border dock for the buttons.
		BorderDock toolBarBorderDock = new BorderDock(new CompositeToolBarDockFactory(), minimizerBorderDock);
		toolBarBorderDock.setMode(BorderDock.MODE_TOOL_BAR);
		CompositeLineDock compositeToolBarDock1 = new CompositeLineDock(CompositeLineDock.ORIENTATION_HORIZONTAL, false,
				new ToolBarDockFactory(), DockingMode.HORIZONTAL_TOOLBAR, DockingMode.VERTICAL_TOOLBAR);
		CompositeLineDock compositeToolBarDock2 = new CompositeLineDock(CompositeLineDock.ORIENTATION_VERTICAL, false,
				new ToolBarDockFactory(), DockingMode.HORIZONTAL_TOOLBAR, DockingMode.VERTICAL_TOOLBAR);
		toolBarBorderDock.setDock(compositeToolBarDock1, Position.TOP);
		toolBarBorderDock.setDock(compositeToolBarDock2, Position.LEFT);

		// Add this dock also as root dock to the dock model.
		dockModel.addRootDock("toolBarBorderDock", toolBarBorderDock, frame);
		
		// Add the tool bar border dock to this panel.
		this.add(toolBarBorderDock, BorderLayout.CENTER);

		// The line docks and one grid dock for the buttons.
		LineDock toolBarDock1 = new LineDock(LineDock.ORIENTATION_HORIZONTAL, false, DockingMode.HORIZONTAL_TOOLBAR, DockingMode.VERTICAL_TOOLBAR);
		LineDock toolBarDock2 = new LineDock(LineDock.ORIENTATION_HORIZONTAL, false, DockingMode.HORIZONTAL_TOOLBAR, DockingMode.VERTICAL_TOOLBAR);
		LineDock toolBarDock3 = new LineDock(LineDock.ORIENTATION_HORIZONTAL, false, DockingMode.HORIZONTAL_TOOLBAR, DockingMode.VERTICAL_TOOLBAR);
		LineDock toolBarDock4 = new LineDock(LineDock.ORIENTATION_HORIZONTAL, false, DockingMode.HORIZONTAL_TOOLBAR, DockingMode.VERTICAL_TOOLBAR);
		LineDock toolBarDock5 = new LineDock(LineDock.ORIENTATION_VERTICAL, false, DockingMode.HORIZONTAL_TOOLBAR, DockingMode.VERTICAL_TOOLBAR);
		LineDock toolBarDock6 = new LineDock(LineDock.ORIENTATION_VERTICAL, false, DockingMode.HORIZONTAL_TOOLBAR, DockingMode.VERTICAL_TOOLBAR);
		GridDock toolGridDock = new GridDock(DockingMode.TOOL_GRID);

		// Add the button dockables to the line docks.
		toolBarDock1.addDockable(buttonDockables[0],  new Position(0));


		// Add the line docks to their composite parents.
		compositeToolBarDock1.addChildDock(toolBarDock1, new Position(0));

		
		// Create the menubar.
		JMenuBar menuBar = createMenu(dockables);
		frame.setJMenuBar(menuBar);

		HashMap<DefaultMutableTreeNode, Dockable> node2Dockable = new HashMap<>();
        contactTree.tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode treeNode =(DefaultMutableTreeNode)(e.getPath().getLastPathComponent());
                System.out.println(treeNode.getUserObject());
                if(treeNode.getUserObject() instanceof CodeInfo) {
                    CodeInfo visitor = (CodeInfo) treeNode.getUserObject();
                    if (visitor != null) {
                        if(node2Dockable.containsKey(treeNode)){
                            Dockable dockable=node2Dockable.get(treeNode);
                           // dockable.getContent().getParent().add(dockable.getContent());
                            ///dockable.setState(DockableState.MAXIMIZED,dockable.getDock());
                            centerTabbedDock.removeDockable(dockable);
                            centerTabbedDock.addDockable(dockable,new Position(0));
                        }else {
                            CodeView codeView = new CodeView(visitor);
                            Dockable dockable = createDockable(treeNode.toString(), codeView, treeNode.toString(), new ImageIcon(getClass().getResource("/com/javadocking/resources/images/text12.gif")), "<html>De Bello Gallico: Liber 1<br><i>Gaius Julius Caesar</i><html>");
                            centerTabbedDock.addDockable(dockable, new Position(0));
                            node2Dockable.put(treeNode,dockable);
                        }
                    }
                }
            }

        });

    }
	
	/**
	 * Decorates the given dockable with all state actions.
	 * 
	 * @param dockable	The dockable to decorate.
	 * @return			The wrapper around the given dockable, with actions.
	 */
	private Dockable addAllActions(Dockable dockable)
	{
		
		Dockable wrapper = new StateActionDockable(dockable, new DefaultDockableStateActionFactory(), DockableState.statesClosed());
		wrapper = new StateActionDockable(wrapper, new DefaultDockableStateActionFactory(), DockableState.statesAllExceptClosed());
		return wrapper;

	}
	
	/**
	 * Decorates the given dockable with some state actions (not maximized).
	 * 
	 * @param dockable	The dockable to decorate.
	 * @return			The wrapper around the given dockable, with actions.
	 */
	private Dockable addLimitActions(Dockable dockable)
	{
		
		Dockable wrapper = new StateActionDockable(dockable, new DefaultDockableStateActionFactory(), DockableState.statesClosed());
		int[] limitStates = {DockableState.NORMAL, DockableState.MINIMIZED, DockableState.EXTERNALIZED};
		wrapper = new StateActionDockable(wrapper, new DefaultDockableStateActionFactory(), limitStates);
		return wrapper;

	}
	
	/**
	 * Creates a dockable with a button as content.
	 * 
	 * @param id			The ID of the dockable that has to be created.
	 * @param title			The title of the dialog that will be displayed.
	 * @param icon			The icon that will be put on the button.
	 * @param message		The message that will be displayed when the action is performed.
	 * @return				The dockable with a button as content.
	 */
	private Dockable createButtonDockable(String id, String title, Icon icon, String message)
	{
		
		// Create the action.
		MessageAction action = new MessageAction(this, title, icon, message);

		// Create the button.
		ToolBarButton button = new ToolBarButton(action);

		// Create the dockable with the button as component.
		ButtonDockable buttonDockable = new ButtonDockable(id, button);

		// Add a dragger to the individual dockable.
		createDockableDragger(buttonDockable);

		return buttonDockable;
		
	}
	
	/**
	 * Adds a drag listener on the content component of a dockable.
	 */
	private void createDockableDragger(Dockable dockable)
	{
		
		// Create the dragger for the dockable.
		DragListener dragListener = DockingManager.getDockableDragListenerFactory().createDragListener(dockable);
		dockable.getContent().addMouseListener(dragListener);
		dockable.getContent().addMouseMotionListener(dragListener);
		
	}
	
	/**
	 * Creates a dockable for a given content component.
	 * 
	 * @param 	id 		The ID of the dockable. The IDs of all dockables should be different.
	 * @param 	content The content of the dockable. 
	 * @param 	title 	The title of the dockable.
	 * @param 	icon 	The icon of the dockable.
	 * @return			The created dockable.
	 * @throws 	IllegalArgumentException	If the given ID is null.
	 */
	private Dockable createDockable(String id, Component content, String title, Icon icon, String description)
	{
		
		// Create the dockable.
		DefaultDockable dockable = new DefaultDockable(id, content, title, icon);
		
		// Add a description to the dockable. It will be displayed in the tool tip.
		dockable.setDescription(description);
		
		return dockable;
		
	}

	/**
	 * Creates the menubar with menus: File, Window, Look and Feel, and Drag Painting.
	 * 
	 * @param dockables		The dockables for which a menu item has to be created.
	 * @return				The created menu bar.
	 */
	private JMenuBar createMenu(Dockable[] dockables)
	{
		// Create the menu bar.
		JMenuBar menuBar = new JMenuBar();

		// Build the File menu.
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		fileMenu.getAccessibleContext().setAccessibleDescription("The File Menu");
		menuBar.add(fileMenu);
		
		// Build the Window menu.
		JMenu windowMenu = new JMenu("Window");
		windowMenu.setMnemonic(KeyEvent.VK_W);
		windowMenu.getAccessibleContext().setAccessibleDescription("The Window Menu");
		menuBar.add(windowMenu);
		
		// Build the Look and Feel menu.
		JMenu lookAndFeelMenu = new JMenu("Look and Feel");
		lookAndFeelMenu.setMnemonic(KeyEvent.VK_L);
		lookAndFeelMenu.getAccessibleContext().setAccessibleDescription("The Lool and Feel Menu");
		menuBar.add(lookAndFeelMenu);

		// The JMenuItem for File
		JMenuItem menuItem = new JMenuItem("Exit", KeyEvent.VK_E);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.ALT_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Exit te application");
		menuItem.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent arg0)
					{
						System.exit(0);
					}
				});
		fileMenu.add(menuItem);

		// The JMenuItems for the dockables.
		for (int index = 0; index < dockables.length; index++)
		{
			// Create the check box menu for the dockable.
			JCheckBoxMenuItem cbMenuItem = new DockableMenuItem(dockables[index]);
			windowMenu.add(cbMenuItem);			
		}
		
		// The JMenuItems for the look and feels.
		ButtonGroup group = new ButtonGroup();
		for (int index = 0; index < LAFS.length; index++)
		{
			LafMenuItem lafMenuItem = new LafMenuItem(LAFS[index]);
			lookAndFeelMenu.add(lafMenuItem);
			group.add(lafMenuItem);
		}
		return menuBar;
		
	} 
	
	/**
	 * Sets the look and feel on the application.
	 *
	 */
	private void setLookAndFeel(LAF laf)
	{
		
		try 
		{
	        UIManager.setLookAndFeel(laf.getClassName());
	        LookAndFeel lookAndFeel = UIManager.getLookAndFeel();
	        LAF.setTheme(lookAndFeel, laf.getTheme());
	        UIManager.setLookAndFeel(lookAndFeel);
	        
	        // Iterate over the owner windows of the dock model.
	        DockModel dockModel = DockingManager.getDockModel();
	        for (int index = 0; index < dockModel.getOwnerCount(); index++)
	        {
	        	
	        	// Set the LaF on the owner.
	        	Window owner = dockModel.getOwner(index);
	        	SwingUtilities.updateComponentTreeUI(owner);
	        	
	        	// Set the LaF on the floating windows.
	        	Set floatDocks = DockModelUtil.getVisibleFloatDocks(dockModel, owner);
	        	Iterator iterator = floatDocks.iterator();
	        	while(iterator.hasNext())
	        	{
		        	FloatDock floatDock = (FloatDock)iterator.next();
		        	for (int childIndex = 0; childIndex < floatDock.getChildDockCount(); childIndex++)
		        	{
		        		Component floatingComponent = (Component)floatDock.getChildDock(childIndex);
		        		SwingUtilities.updateComponentTreeUI(SwingUtilities.getWindowAncestor(floatingComponent));
		        	}
	        	}
	        	
	        	// Set the LaF on all the dockable components.
	        	for (int dockableIndex = 0; dockableIndex < dockables.length; dockableIndex++)
	        	{
	        		SwingUtilities.updateComponentTreeUI(dockables[dockableIndex].getContent());
	        	}
                for (Dockable buttonDockable : buttonDockables) {
                    SwingUtilities.updateComponentTreeUI(buttonDockable.getContent());
                }
	        	
	        }
	    } catch (Exception e) { }

	}
	
	/**
	 * Creates a docking path for the given dockable. It contains the information
	 * how the dockable is docked now. The docking path is added to the docking path
	 * model of the docking manager.
	 * 
	 * @param	 dockable	The dockable for which a docking path has to be created.
	 * @return				The docking path model. Null if the dockable is not docked.
	 */
	private DockingPath addDockingPath(Dockable dockable)
	{

		if (dockable.getDock() != null)
		{
			// Create the docking path of the dockable.
			DockingPath dockingPath = DefaultDockingPath.createDockingPath(dockable);
			DockingManager.getDockingPathModel().add(dockingPath);
			return dockingPath;
		}
		
		return null;

	}
	
	// Private classes.

	/**
	 * An action that shows a message in a dialog.
	 */
	private class MessageAction extends AbstractAction
	{

		private Component parentComponent;
		private String message;
		private String name;
		
		public MessageAction(Component parentComponent, String name, Icon icon, String message)
		{
			super(null, icon);
			putValue(Action.SHORT_DESCRIPTION, name);
			this.message = message;
			this.name = name;
			this.parentComponent = parentComponent;
		}

		public void actionPerformed(ActionEvent actionEvent)
		{
			JOptionPane.showMessageDialog(parentComponent,
					message, name, JOptionPane.INFORMATION_MESSAGE);
		}
		
	}

	/**
	 * A check box menu item to add or remove the dockable.
	 */
	private class DockableMenuItem extends JCheckBoxMenuItem
	{
		public DockableMenuItem(Dockable dockable)
		{
			super(dockable.getTitle(), dockable.getIcon());
			
			setSelected(dockable.getDock() != null);
			
			DockableMediator dockableMediator = new DockableMediator(dockable, this);
			dockable.addDockingListener(dockableMediator);
			addItemListener(dockableMediator);
		}
	}
	
	/**
	 * A check box menu item to enable a look and feel.
	 */
	private class LafMenuItem extends JRadioButtonMenuItem
	{
		
		public LafMenuItem(LAF laf)
		{
			super(laf.getTitle());
			
			// Is this look and feel supported?
			if (laf.isSupported())
			{
				LafListener lafItemListener = new LafListener(laf);
				addActionListener(lafItemListener);
			}
			else
			{
				setEnabled(false);
			}
			
			if (laf.isSelected())
			{
				setSelected(true);
			}
		}
	}
	
	/**
	 * A listener that installs its look and feel.
	 */
	private class LafListener implements ActionListener
	{

		// Fields.

		private LAF laf;
		
		// Constructors.

		public LafListener(LAF laf)
		{
			this.laf = laf;
		}
		
		// Implementations of ItemListener.

		public void actionPerformed(ActionEvent arg0)
		{
			for (int index = 0; index < LAFS.length; index++)
			{
				LAFS[index].setSelected(false);
			}
			setLookAndFeel(laf);
			laf.setSelected(true);
		}
		
	}
	
	/**
	 * A check box menu item to enable a dragger.
	 */
	private class DraggingMenuItem extends JRadioButtonMenuItem
	{
		
		// Constructor.

		public DraggingMenuItem(String title, DockableDragPainter basicDockableDragPainter, DockableDragPainter additionalDockableDragPainter, boolean selected)
		{
			super(title);
	
			// Create the dockable drag painter and dragger factory.
			CompositeDockableDragPainter compositeDockableDragPainter = new CompositeDockableDragPainter();
			compositeDockableDragPainter.addPainter(basicDockableDragPainter);
			if (additionalDockableDragPainter != null)
			{
				compositeDockableDragPainter.addPainter(additionalDockableDragPainter);
			}
			DraggerFactory draggerFactory 	= new StaticDraggerFactory(compositeDockableDragPainter);
			
			// Give this dragger factory to the docking manager.
			if (selected)
			{
				DockingManager.setDraggerFactory(draggerFactory);
				setSelected(true);
			}

			// Add a dragging listener as action listener.
			addActionListener(new DraggingListener(draggerFactory));
			
		}
		
	}
	
	/**
	 * A listener that installs a dragger factory.
	 */
	private class DraggingListener implements ActionListener
	{

		// Fields.

		private DraggerFactory draggerFactory;
		
		// Constructor.

		public DraggingListener(DraggerFactory draggerFactory)
		{
			this.draggerFactory = draggerFactory;
		}
		
		// Implementations of ItemListener.

		public void actionPerformed(ActionEvent actionEvent)
		{
			DockingManager.setDraggerFactory(draggerFactory);
		}
		
	}
	
	/**
	 * A listener that listens when menu items with dockables are selected and deselected.
	 * It also listens when dockables are closed or docked.
	 */
	private class DockableMediator implements ItemListener, DockingListener
	{
		
		private Dockable dockable;
		private Action closeAction;
		private Action restoreAction;
		private JMenuItem dockableMenuItem;
		
		public DockableMediator(Dockable dockable, JMenuItem dockableMenuItem) 
		{
			
			this.dockable = dockable;
			this.dockableMenuItem = dockableMenuItem;
			closeAction = new DefaultDockableStateAction(dockable, DockableState.CLOSED);
			restoreAction = new DefaultDockableStateAction(dockable, DockableState.NORMAL);

		}

		public void itemStateChanged(ItemEvent itemEvent)
		{
			
			dockable.removeDockingListener(this);
			if (itemEvent.getStateChange() == ItemEvent.DESELECTED)
			{
				// Close the dockable.
				closeAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Close"));
			} 
			else 
			{
				// Restore the dockable.
				restoreAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Restore"));
			}
			dockable.addDockingListener(this);

		}

		public void dockingChanged(DockingEvent dockingEvent) {
			if (dockingEvent.getDestinationDock() != null)
			{
				dockableMenuItem.removeItemListener(this);
				dockableMenuItem.setSelected(true);
				dockableMenuItem.addItemListener(this);	
			}
			else
			{
				dockableMenuItem.removeItemListener(this);
				dockableMenuItem.setSelected(false);
				dockableMenuItem.addItemListener(this);
			}
		}

		public void dockingWillChange(DockingEvent dockingEvent) {}

	}
	
	// Main method.
	
	public static void createAndShowGUI()
	{ 

		// Create the look and feels.

        UIManager.LookAndFeelInfo[] lookAndFeels= UIManager.getInstalledLookAndFeels();

		LAFS  = new LAF[lookAndFeels.length];
		for(int i=0;i<lookAndFeels.length;i++){
		    LAFS[i]=new LAF(lookAndFeels[i].getName(),lookAndFeels[i].getClassName(), LAF.THEME_DEAULT);
        }
		//LAFS[0] = new LAF("Substance", "org.jvnet.substance.skin.SubstanceModerateLookAndFeel", LAF.THEME_DEAULT);
		//LAFS[1] = new LAF("Mac", "javax.swing.plaf.mac.MacLookAndFeel", LAF.THEME_DEAULT);
		//LAFS[2] = new LAF("Metal", "javax.swing.plaf.metal.MetalLookAndFeel", LAF.THEME_DEAULT);
		//LAFS[3] = new LAF("Liquid", "com.birosoft.liquid.LiquidLookAndFeel", LAF.THEME_DEAULT);
		//LAFS[4] = new LAF("Windows", "com.sun.java.swing.plaf.windows.WindowsLookAndFeel", LAF.THEME_DEAULT);
		//LAFS[5] = new LAF("Nimrod Ocean", "com.nilo.plaf.nimrod.NimRODLookAndFeel", LAF.THEME_OCEAN);
		//LAFS[6] = new LAF("Nimrod Gold", "com.nilo.plaf.nimrod.NimRODLookAndFeel", LAF.THEME_GOLD);
		//LAFS[7] = new LAF("Nimbus", "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel", LAF.THEME_DEAULT);
		//LAFS[8] = new LAF("TinyLaF", "de.muntjak.tinylookandfeel.TinyLookAndFeel", LAF.THEME_DEAULT);
		
		// Set the first enabled look and feel.

			try
			{
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		    } catch (Exception e) { }

		 
		// Remove the borders from the split panes and the split pane dividers.
		LookAndFeelUtil.removeAllSplitPaneBorders();
		
		// Create the frame.
		JFrame frame = new JFrame("swfmixer");
		
		// Set the default location and size.
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation((screenSize.width - FRAME_WIDTH) / 2, (screenSize.height - FRAME_HEIGHT) / 2);
		frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);

		// Create the panel and add it to the frame.
		Main panel = new Main(frame);
		frame.getContentPane().add(panel);
		
		// Set the frame properties and show it.
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

        ArrayList<String> words= new ArrayList();
        try{
            InputStreamReader ois=new InputStreamReader(Main.class.getClass().getResourceAsStream("/res/words.txt"));
            BufferedReader inputStream=new BufferedReader(ois);
            String line=null;
            HashSet<String> wordsset=new HashSet<>();
            while (true){
                line=inputStream.readLine();
                if(line==null){
                    break;
                }
                line=line.toLowerCase();
                if(line.matches("[A-Za-z]+")&&!wordsset.contains(line)&&line.length()>2){
                    words.add(line);
                    wordsset.add(line);
                }
            }
            inputStream.close();
            ois.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        FileSystemView fsv = FileSystemView.getFileSystemView();
        File com=fsv.getHomeDirectory();
        //new CodeGen(com.getPath()+File.separator+"1",words,null,100);
	}

	public static void main(String args[]) 
	{
	    System.out.println("aa1".matches("[A-Za-z]+"));
        System.out.println("*ad*".replace("*","__"));
        Runnable doCreateAndShowGUI = new Runnable() 
        {
            public void run() 
            {
                createAndShowGUI();
            }
        };
        SwingUtilities.invokeLater(doCreateAndShowGUI);

       /* Gson gson=new GsonBuilder().disableHtmlEscaping().create();
        HashMap a=gson.fromJson("{'a':2,'b':{'c':{'d':3}}}", HashMap.class);
        LinkedTreeMap b=(LinkedTreeMap) a.get("b");
        LinkedTreeMap c=(LinkedTreeMap) b.get("c");
        System.out.println(c.get("d"));*/


        JSONNode node=new JSONNode("{'a':2,'b':{'c':{'d':3},'f':'55555'}}");
        //JSONNode node=new JSONNode("{'a':[1,'2']}");

        Object d=0;
        try{
            d=node.get("b").get("f").value;
        }catch (Exception e){

        }
        System.out.println(d);
    }
	
}

