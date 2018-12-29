package swftool;

import com.adobe.flash.swf.SWF;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.*;
import java.io.File;

/**
 * created by lizhi
 */
public class SwfTreeModel extends DefaultTreeModel {
    public SwfTreeModel(TreeNode root){
        super(root);
    }

    @Override
    public boolean isLeaf(Object node) {
        if(node ==root){
            return false;
        }
        if(node instanceof SWFTreeNode&&((SWFTreeNode) node).getChildCount()==0){
            SWFTreeNode treeNode=(SWFTreeNode)node;
            File file=treeNode.file;
            if(file!=null&&file.isDirectory()){
                for(File f:file.listFiles()){
                    treeNode.add(new SWFTreeNode(f,null,null));
                }
                return false;
            }
        }
        return super.isLeaf(node);
    }
}
