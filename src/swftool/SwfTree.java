package swftool;

import com.adobe.flash.swf.Header;
import com.adobe.flash.swf.SWF;
import com.adobe.flash.swf.io.SWFReader;
import com.adobe.flash.swf.io.SWFWriter;

import java.awt.BorderLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

/**
 * A pane with a tree with the contact persons in the different countries.
 * 
 * @author Heidi Rakels.
 */
public class SwfTree extends JPanel
{
	JTree tree=null;
	/**
	 * Constructs a tree with the contact persons in the different countries.
	 *
	 */
	public SwfTree()
	{
		// Create the JTree from the tree model.
		//JTree tree = new JTree();
		new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE,new DropTargetAdapter() {
			@Override
			public void drop(DropTargetDropEvent dtde) {
				try{
					if(dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)){
						dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
						java.util.List<File> list = (java.util.List<File>)(dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor));
						updateswf(list);
						dtde.dropComplete(true);
					}else{
						dtde.rejectDrop();
					}
				}catch (Exception e){

				}
			}
		});

		// Expand the tree.
		//
		
		// Add the tree in a scroll pane.
		tree=new JTree();
		tree.setModel(new SwfTreeModel(new DefaultMutableTreeNode("root")));
		this.setLayout(new BorderLayout());
		this.add(new JScrollPane(tree), BorderLayout.CENTER);
	}

	private void updateswf(java.util.List<File> list){
		for(File file:list){
			DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
			DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
			model.insertNodeInto(new SWFTreeNode(file),root,root.getChildCount());
			//root.add(new DefaultMutableTreeNode());
			//model.reload(root);
		}

		//showMessageDialog(null,"over");
	}
	
}
