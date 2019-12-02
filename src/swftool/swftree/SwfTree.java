package swftool.swftree;

import com.adobe.flash.abc.ABCParser;
import com.adobe.flash.abc.semantics.ClassInfo;
import com.adobe.flash.abc.semantics.ScriptInfo;
import com.adobe.flash.abc.semantics.Trait;
import com.adobe.flash.swf.SWF;
import com.adobe.flash.swf.SWFFrame;
import com.adobe.flash.swf.io.SWFReader;
import com.adobe.flash.swf.tags.DoABCTag;
import com.adobe.flash.swf.tags.ITag;
import swftool.ABCEmitter;

import java.awt.BorderLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

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
            SWFReader reader=new SWFReader();
            try {
                SWF swf = (SWF)reader.readFrom(new FileInputStream(file), file.getPath());
                //if(swf.getTopLevelClass()!=null){
                    SWFTreeNode swfnode=node;
                    swfnode.setUserObject(swf);
                    swfnode.add(new SWFTreeNode(null,null,swf.getHeader()));
                    swfnode.add(new SWFTreeNode(null,null,swf.getFileAttributes()+" as3:"+swf.getFileAttributes().isAS3()));
                    swfnode.add(new SWFTreeNode(null,"meteadata",swf.getMetadata()));
                    swfnode.add(new SWFTreeNode(null,null,swf.getBackgroundColor()));
                    swfnode.add(new SWFTreeNode(null,"enableDebugger2:"+(swf.getEnableDebugger2()!=null),null));
                    swfnode.add(new SWFTreeNode(null,"enableTelemtry:"+(swf.getEnableTelemetry()!=null),null));
                    swfnode.add(new SWFTreeNode(null,"ScriptLimits:"+(swf.getScriptLimits()!=null),null));
                    if(swf.getTopLevelClass()!=null) {
                        swfnode.add(new SWFTreeNode(null, null, swf.getTopLevelClass()));
                    }
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

                                for(ScriptInfo ci : abc.scriptInfos){
                                    for(Trait trait :ci.getTraits()){
                                        if(trait.isClass()){
                                            if(trait.getName().getSingleQualifier().getKind()==5){
                                                continue;
                                            }
                                            String nsname= trait.getName().getSingleQualifier().getName();
                                            String cname=trait.getName().getBaseName();
                                            CodeInfo codeInfo=new CodeInfo();
                                            codeInfo.abc=abc;
                                            codeInfo.scriptInfo=ci;
                                            ClassInfo cii = (ClassInfo)trait.getAttr("class_id");
                                            for(ABCEmitter.EmitterClassVisitor classVisitor : abc.definedClasses) {
                                                if (classVisitor.classInfo == cii) {
                                                    codeInfo.classVisitor=classVisitor;
                                                    break;
                                                }
                                            }
                                            SWFTreeNode classNode=new SWFTreeNode(null,cname,codeInfo);
                                            HashMap<String,Object> currentMap=map;
                                            if(nsname==null||"".equals(nsname)){

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
                                    }

                                }

                                doFile(map,abcnode);
                            }else {
                                frameTreeNode.add(new SWFTreeNode(null,null,iTag));
                            }
                        }
                    }
               // }
            }catch (Exception err){
                err.printStackTrace();
            }

            model.insertNodeInto(node,root,root.getChildCount());
            tree.expandRow(tree.getRowCount()-1);
		}
	}

	private void doFile(HashMap<String,Object> map,SWFTreeNode node){
        List<Map.Entry<String, Object>> infoIds = new ArrayList<Map.Entry<String, Object>>(map.entrySet());

        // 对HashMap中的key 进行排序
        Collections.sort(infoIds, new Comparator<Map.Entry<String, Object>>() {
            public int compare(Map.Entry<String, Object> o1,
                               Map.Entry<String, Object> o2) {
                if(o1.getValue().getClass()!=o2.getValue().getClass()){
                    if(o1.getValue() instanceof HashMap){
                        return -1;
                    }else {
                        return 1;
                    }
                }
                return (o1.getKey()).toString().compareTo(o2.getKey().toString());
            }
        });


        for(Map.Entry<String,Object> iid:infoIds){
	        Object o=iid.getValue();
	        String s=iid.getKey();
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
