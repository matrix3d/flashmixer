package swftool;

import com.adobe.flash.swf.SWF;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.*;

/**
 * created by lizhi
 */
public class SwfTreeModel extends DefaultTreeModel {
    public SwfTreeModel(TreeNode root){
        super(root);
    }

    @Override
    public int getChildCount(Object parent) {
        if (parent instanceof SWFTreeNode) {
            SWFTreeNode swfTreeNode = (SWFTreeNode) parent;
            return 9;
        }
        return super.getChildCount(parent);
    }

    @Override
    public Object getChild(Object parent, int index) {
        if(parent instanceof SWFTreeNode){
            SWFTreeNode swfTreeNode=(SWFTreeNode)parent;
            if(swfTreeNode.nodes[index]==null){
                DefaultMutableTreeNode node=null;
                switch (index){
                    case 0:
                        node=new DefaultMutableTreeNode(swfTreeNode.swf.getHeader());
                        break;
                    case 1:
                        node=new DefaultMutableTreeNode(swfTreeNode.swf.getFileAttributes());
                        break;
                    case 2:
                        node=new DefaultMutableTreeNode(swfTreeNode.swf.getMetadata());
                        break;
                    case 3:
                        node=new DefaultMutableTreeNode(swfTreeNode.swf.getBackgroundColor());
                        break;
                    case 4:
                        node=new DefaultMutableTreeNode("enableDebugger2:"+(swfTreeNode.swf.getEnableDebugger2()!=null));
                        break;
                    case 5:
                        node=new DefaultMutableTreeNode("enableTelemtry:"+(swfTreeNode.swf.getEnableTelemetry()!=null));
                        break;
                    case 6:
                        node=new DefaultMutableTreeNode("ScriptLimits:"+(swfTreeNode.swf.getScriptLimits()!=null));
                        break;
                    case 7:
                        node=new DefaultMutableTreeNode(swfTreeNode.swf.getTopLevelClass());
                        break;
                    case 8:
                        node=new DefaultMutableTreeNode(swfTreeNode.swf.getFrames());
                        break;
                }
                swfTreeNode.nodes[index]=node;
            }
            return swfTreeNode.nodes[index];
        }
        return super.getChild(parent, index);
    }

    @Override
    public boolean isLeaf(Object node) {
        if(node ==root){
            return false;
        }
        if(node instanceof SWFTreeNode){
            return false;
        }
        return super.isLeaf(node);
    }
}
