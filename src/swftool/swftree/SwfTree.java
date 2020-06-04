package swftool.swftree;

import com.adobe.flash.abc.ABCLinker;
import com.adobe.flash.abc.ABCParser;
import com.adobe.flash.abc.semantics.ClassInfo;
import com.adobe.flash.abc.semantics.ScriptInfo;
import com.adobe.flash.abc.semantics.Trait;
import com.adobe.flash.swc.ISWCLibrary;
import com.adobe.flash.swc.ISWCScript;
import com.adobe.flash.swc.SWC;
import com.adobe.flash.swc.SWCScript;
import com.adobe.flash.swc.io.SWCReader;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

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

		JPopupMenu menu=new JPopupMenu();
		JMenuItem menuItem=new JMenuItem("export");
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("xxx");
                DefaultMutableTreeNode treeNode =(DefaultMutableTreeNode)(tree.getSelectionPath().getLastPathComponent());
                System.out.println(treeNode.getUserObject());
                DoABCTag abcTag=null;
                if(treeNode.getUserObject() instanceof List &&((List) treeNode.getUserObject()).size()>0&&((List) treeNode.getUserObject()).get(0) instanceof SWFFrame){
                    ABCEmitter abc=null;
                    List<byte[]> abcs=new ArrayList();
                    DoABCTag abcTag2=null;
                    for(SWFFrame frame: (List<SWFFrame>)treeNode.getUserObject()) {
                        Iterator<ITag> it = frame.iterator();
                        int i = 0;
                        while (it.hasNext()) {
                            ITag iTag = it.next();
                            if (iTag instanceof DoABCTag) {
                                abcTag2 = (DoABCTag) iTag;
                                abcs.add(abcTag2.getABCData());
                                if (i == 0) {
                                    ABCParser abcParser = new ABCParser(abcTag2.getABCData());
                                    abc = new ABCEmitter();
                                    abc.setAllowBadJumps(true);
                                    abcParser.parseABC(abc);
                                    i++;
                                }
                            }
                        }
                    }
                    try {
                        abcTag = new DoABCTag();
                        abcTag.setABCData(ABCLinker.linkABC(abcs, abc.versionABCMajor, abc.versionABCMinor, new ABCLinker.ABCLinkerSettings()));
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }

                }else if(treeNode.getUserObject() instanceof DoABCTag) {
                    abcTag = (DoABCTag) treeNode.getUserObject();
                }
                if(abcTag!=null){
                    FileNameExtensionFilter filter=new FileNameExtensionFilter("*.abc","abc");
                    JFileChooser fc=new JFileChooser();
                    fc.setFileFilter(filter);
                    fc.setMultiSelectionEnabled(false);
                    fc.setSelectedFile(new File("abc.abc"));
                    int result=fc.showSaveDialog(null);
                    if (result==JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        if (!file.getPath().endsWith(".abc")) {
                            file = new File(file.getPath() + ".abc");
                        }
                        System.out.println("file path=" + file.getPath());
                        FileOutputStream fos = null;
                        try {
                            if (!file.exists()) {//文件不存在 则创建一个
                                file.createNewFile();
                            }
                            fos = new FileOutputStream(file);
                            fos.write(abcTag.getABCData());
                            fos.flush();
                        } catch (IOException e2) {
                            System.err.println("文件创建失败：");
                            e2.printStackTrace();
                        } finally {
                            if (fos != null) {
                                try {
                                    fos.close();
                                } catch (IOException e2) {
                                    e2.printStackTrace();
                                }
                            }
                        }
                    }
                }
                else{
                    JOptionPane.showMessageDialog(null, "now just doabctag");
                }

            }
        });

		tree.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("1");
                int x=e.getX();
                int y=e.getY();
                if(e.getButton()==MouseEvent.BUTTON3){
                    TreePath treePath=tree.getPathForLocation(x,y);
                    tree.setSelectionPath(treePath);
                    menu.show(tree,x,y);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

		tree.setModel(new SwfTreeModel(new DefaultMutableTreeNode("root")));
		this.setLayout(new BorderLayout());
		this.add(new JScrollPane(tree), BorderLayout.CENTER);
	}

    private void updateswf(java.util.List<File> list){
		for(File file:list){
			DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
			DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();

                String suffix = file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase();
                if(suffix.equals("abc")){
                    DoABCTag abcTag=new DoABCTag();
                    try{
                        abcTag.setABCData(Files.readAllBytes(file.toPath()));

                        SWFTreeNode node=new SWFTreeNode(file,null,null);
                        //if(swf.getTopLevelClass()!=null){

                                    addAbcToTree(abcTag,node);

                        model.insertNodeInto(node,root,root.getChildCount());
                        tree.expandRow(tree.getRowCount()-1);
                    }catch (Exception e){

                    }
                }else if(suffix.equals("swc")){
                    SWCReader reader=new SWCReader(file);
                    SWC swc= (SWC) reader.getSWC();
                    Collection<ISWCLibrary> iswcLibraries= swc.getLibraries();
                    for(ISWCLibrary swcLibrary: iswcLibraries){
                        SWFReader swfReader=new SWFReader();
                        swcLibrary.readSWFInputStream(swfReader,swc);

                        SWF swf=(SWF)swfReader.getSWF();

                        addSwfToTree(swf,file,model,root);

                        for(ISWCScript iswcScript: swcLibrary.getScripts()){
                            SWCScript swcScript=(SWCScript)iswcScript;
                            byte[] source= swcScript.getSource();
                        }
                    }
                }else {
                    SWFReader reader=new SWFReader();
                    try {
                        SWF swf = (SWF)reader.readFrom(new FileInputStream(file), file.getPath());
                        addSwfToTree(swf,file,model,root);

                    }catch (Exception err){
                        err.printStackTrace();
                    }

                }

		}
	}

	private void addSwfToTree(SWF swf,File file,DefaultTreeModel model,DefaultMutableTreeNode root){

        SWFTreeNode node=new SWFTreeNode(file,null,null);
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
                    DoABCTag abcTag=(DoABCTag) iTag ;
                    addAbcToTree(abcTag,frameTreeNode);

                }else {
                    frameTreeNode.add(new SWFTreeNode(null,null,iTag));
                }
            }
        }
        // }

        model.insertNodeInto(node,root,root.getChildCount());
        tree.expandRow(tree.getRowCount()-1);
    }

    private void addAbcToTree(DoABCTag abcTag,SWFTreeNode pnode){
        SWFTreeNode abcnode=new SWFTreeNode(null,null,abcTag);
        pnode.add(abcnode);
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
