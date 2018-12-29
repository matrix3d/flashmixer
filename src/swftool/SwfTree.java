package swftool;

import com.adobe.flash.abc.ABCParser;
import com.adobe.flash.abc.semantics.Trait;
import com.adobe.flash.swf.Header;
import com.adobe.flash.swf.SWF;
import com.adobe.flash.swf.SWFFrame;
import com.adobe.flash.swf.io.SWFReader;
import com.adobe.flash.swf.io.SWFWriter;
import com.adobe.flash.swf.tags.DoABCTag;
import com.adobe.flash.swf.tags.ITag;

import java.awt.BorderLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;

/**
 * @author lizhi.
 */
public class SwfTree extends JPanel
{
	public JTree tree=null;
	public SwfTree()
	{
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
		tree=new JTree();

		tree.setModel(new SwfTreeModel(new DefaultMutableTreeNode("root")));
		this.setLayout(new BorderLayout());
		this.add(new JScrollPane(tree), BorderLayout.CENTER);
	}

    private void updateswf(java.util.List<File> list){
		for(File file:list){
			DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
			DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
			SWFTreeNode node=new SWFTreeNode(file,null,null);
			model.insertNodeInto(node,root,root.getChildCount());
            SWFReader reader=new SWFReader();
            try {
                SWF swf = (SWF)reader.readFrom(new FileInputStream(file), file.getPath());
                if(swf.getTopLevelClass()!=null){
                    SWFTreeNode swfnode=node;
                    swfnode.setUserObject(swf);
                    swfnode.add(new SWFTreeNode(null,null,swf.getHeader()));
                    swfnode.add(new SWFTreeNode(null,null,swf.getFileAttributes()));
                    swfnode.add(new SWFTreeNode(null,"meteadata",swf.getMetadata()));
                    swfnode.add(new SWFTreeNode(null,null,swf.getBackgroundColor()));
                    swfnode.add(new SWFTreeNode(null,"enableDebugger2:"+(swf.getEnableDebugger2()!=null),null));
                    swfnode.add(new SWFTreeNode(null,"enableTelemtry:"+(swf.getEnableTelemetry()!=null),null));
                    swfnode.add(new SWFTreeNode(null,"ScriptLimits:"+(swf.getScriptLimits()!=null),null));
                    swfnode.add(new SWFTreeNode(null,null,swf.getTopLevelClass()));
                    SWFTreeNode frameTreeNode=new SWFTreeNode(null,null,swf.getFrames());
                    swfnode.add(frameTreeNode);
                    for(SWFFrame frame: swf.getFrames()){
                        Iterator<ITag> it=frame.iterator();
                        while (it.hasNext()){
                            ITag iTag= it.next();
                            if(iTag instanceof DoABCTag){
                                SWFTreeNode abcnode=new SWFTreeNode(null,null,iTag);
                                frameTreeNode.add(abcnode);
                                DoABCTag abcTag=(DoABCTag) iTag ;
                                ABCParser abcParser=new ABCParser(abcTag.getABCData());
                                ABCEmitter abc=new ABCEmitter();
                                abc.setAllowBadJumps(true);
                                abcParser.parseABC(abc);
                                HashMap<String,Object> map=new HashMap<>();

                                for(ABCEmitter.EmitterClassVisitor ci : abc.definedClasses){
                                    String nsname=ci.instanceInfo.name.getSingleQualifier().getName();
                                    String cname=ci.instanceInfo.name.getBaseName();
                                    SWFTreeNode classNode=new SWFTreeNode(null,cname,ci);
                                    HashMap<String,Object> currentMap=map;
                                    if(nsname==null){

                                    }else{
                                        String[] parr= nsname.split("\\.");
                                        for(String pa:parr){
                                            if(!currentMap.containsKey(pa)){
                                                currentMap.put(pa,new HashMap<String,Object>());
                                            }
                                            currentMap=(HashMap<String, Object>) currentMap.get(pa);
                                        }
                                    }
                                    currentMap.put(cname+".as",classNode);
                                }

                                doFile(map,abcnode);
                            }else {
                                frameTreeNode.add(new SWFTreeNode(null,null,iTag));
                            }
                        }
                    }
                }
            }catch (Exception err){
                err.printStackTrace();
            }
		}
	}

	private void doFile(HashMap<String,Object> map,SWFTreeNode node){
	    for(String s:map.keySet()){
	        Object o=map.get(s);
	        if(o instanceof SWFTreeNode){
	            node.add((MutableTreeNode) o);
            }else {
	            SWFTreeNode nn=new SWFTreeNode(null,s,null);
	            node.add(nn);
	            doFile((HashMap<String, Object>) o,nn);
            }
        }
    }
	
}
