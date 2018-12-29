package swftool;

import com.adobe.flash.swf.SWF;
import com.adobe.flash.swf.io.SWFReader;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * created by lizhi
 */
public class SWFTreeNode extends DefaultMutableTreeNode {
    public File file=null;
    public String treeName;
    public DefaultMutableTreeNode[] nodes=new DefaultMutableTreeNode[0];
    public SWFTreeNode(File file,String name,Object data){
        this.file=file;
        treeName=name;
        if(data!=null)
        setUserObject(data);
    }

    @Override
    public String toString() {
        if(treeName!=null){
            return treeName;
        }
        if(file!=null){
            return file.getName();
        }
        return super.toString();
    }
}
