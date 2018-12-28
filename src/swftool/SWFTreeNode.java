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
    public SWF swf=null;
    public File file=null;
    public DefaultMutableTreeNode[] nodes=new DefaultMutableTreeNode[9];
    public SWFTreeNode(File file){
        this.file=file;
        SWFReader reader=new SWFReader();
        try {
            swf = (SWF)reader.readFrom(new FileInputStream(file), file.getPath());
        }catch (Exception err){
            err.printStackTrace();
        }

    }

    @Override
    public String toString() {
        return file.getName();
    }
}
