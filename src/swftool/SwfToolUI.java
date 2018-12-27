package swftool;

import com.adobe.flash.swf.Header;
import com.adobe.flash.swf.SWF;
import com.adobe.flash.swf.io.SWFDump;
import com.adobe.flash.swf.io.SWFReader;
import com.adobe.flash.swf.io.SWFWriter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.scene.layout.VBox;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;

import static javax.swing.JOptionPane.*;

public class SwfToolUI extends JFrame {
    private JTree tree;
    JCheckBox mixclass;
    JCheckBox mixpackage;
    JCheckBox mixmap;
    JCheckBox mixvar;
    JCheckBox mixfunc;
    JTextField mixcode;
    JTextPane output=null;
    public SwfToolUI(){
        super("swftool");
        JMenuBar menuBar=new JMenuBar();
        JMenu menu=new JMenu("ddd");
        menuBar.add(menu);
        JMenuItem item=new JMenuItem("item");
        menu.add(item);
        setJMenuBar(menuBar);
        JToolBar toolBar=new JToolBar();
        toolBar.setEnabled(false);
        JButton button=new JButton("btn");
        toolBar.add(button);
        add(toolBar, BorderLayout.PAGE_START);

        JSplitPane splitPane = new JSplitPane();
        add(splitPane, BorderLayout.CENTER);
        tree = new JTree();
        JScrollPane jp=new JScrollPane(tree);
        splitPane.add(jp,JSplitPane.LEFT);

        JSplitPane right=new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        right.setDividerLocation(250);
        splitPane.add(right,JSplitPane.RIGHT);

        JPanel top=new JPanel();
        right.add(top, JSplitPane.TOP);
        top.setLayout(new BoxLayout(top,BoxLayout.Y_AXIS));


         mixclass=new JCheckBox("mixclass",true);
        top.add(mixclass);
         mixpackage=new JCheckBox("mixpackage",true);
        top.add(mixpackage);
         mixmap=new JCheckBox("mixmap");
        top.add(mixmap);
         mixvar=new JCheckBox("mixvar");
        top.add(mixvar);
         mixfunc=new JCheckBox("mixfunc");
        top.add(mixfunc);
         mixcode=new JTextField("[___mix___]");
        top.add(mixcode);

        JButton btn1=new JButton("lzma swf");
        btn1.setPreferredSize(new Dimension(100,100));
        top.add(btn1);

        JButton btn2=new JButton("mix swf");
        btn2.setPreferredSize(new Dimension(100,100));
        top.add(btn2);
        JButton btn3=new JButton("dump swf");
        btn3.setPreferredSize(new Dimension(100,100));
        top.add(btn3);

        JPanel bot=new JPanel();
        bot.setLayout(new BoxLayout(bot, BoxLayout.Y_AXIS));
        output=new JTextPane();
        bot.add(output);
        output.setText("drop files to left panel");
        right.add(bot,JSplitPane.BOTTOM);

        new DropTarget(btn1, DnDConstants.ACTION_COPY_OR_MOVE,new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try{
                    if(dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)){
                        dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                        java.util.List<File> list = (java.util.List<File>)(dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor));
                        lzmaswf(list);
                        dtde.dropComplete(true);
                    }else{
                        dtde.rejectDrop();
                    }
                }catch (Exception e){

                }
            }
        });

        new DropTarget(btn2, DnDConstants.ACTION_COPY_OR_MOVE,new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try{
                    if(dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)){
                        dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                        java.util.List<File> list = (java.util.List<File>)(dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor));
                        mixswf(list);
                        dtde.dropComplete(true);
                    }else{
                        dtde.rejectDrop();
                    }
                }catch (Exception e){

                }
            }
        });


        new DropTarget(btn3, DnDConstants.ACTION_COPY_OR_MOVE,new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try{
                    if(dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)){
                        dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                        java.util.List<File> list = (java.util.List<File>)(dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor));
                        dump(list);
                        dtde.dropComplete(true);
                    }else{
                        dtde.rejectDrop();
                    }
                }catch (Exception e){

                }
            }
        });
    }

    private void mixswf(java.util.List<File> list){
        for(File file:list){
            Gson gson=new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

            FileReader ois=null;
            HashMap<String,String> mixMap=null;
            try{
                ois=new FileReader(new File(file.getParent()+File.separator+file.getName()+"_map"));
                mixMap=gson.fromJson(ois, HashMap.class);//(HashSet<String>)ois.readObject();
                ois.close();
            }catch (Exception e){
                //e.printStackTrace();
            }

            Mixer mixer= new Mixer(file,mixclass.isSelected(),mixpackage.isSelected(),mixvar.isSelected(),mixfunc.isSelected(),mixMap,mixcode.getText());
           long time=System.currentTimeMillis();
            System.out.println("start writeswf"+time);
            SWFWriter writer=new SWFWriter(mixer.outswf,mixer.outswf.getHeader().getCompression());
            try {
                writer.writeTo(new File(file.getParent()+File.separator+"mix_"+file.getName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(System.currentTimeMillis()-time);

            //Files.writeByte(f.parent.resolvePath(nameBase+"_strings"), stringMapByte);
            time=System.currentTimeMillis();
            System.out.println("start writestr"+time);
            FileWriter oos=null;
            try {
                oos = new FileWriter(file.getParent() + File.separator + file.getName()+"_strs");
                oos.write(gson.toJson(mixer.stringMap));
                oos.flush();
                oos.close();
            }catch (Exception e){
                e.printStackTrace();
            }
            try {
                oos = new FileWriter(file.getParent() + File.separator + file.getName()+"_mixed");
                String string=gson.toJson(mixer.mixedMap);
                oos.write(string);
                oos.flush();
                oos.close();
            }catch (Exception e){
                e.printStackTrace();
            }
            System.out.println(System.currentTimeMillis()-time);
            output.setText(output.getText()+"\ncomp");
            //Files.writeString(f.parent.resolvePath(nameBase+"_strings.txt"), JSON.stringify(mixer.stringMap,null,4));
            //Files.writeString(f.parent.resolvePath(nameBase+"_mixed.txt"), JSON.stringify(mixer.mixedMap, null, 4));

            /*SWFReader reader=new SWFReader();
            SWF swf=null;
            try {
                swf = (SWF)reader.readFrom(new FileInputStream(file), file.getPath());
                swf.getHeader().setSignature(Header.SIGNATURE_COMPRESSED_LZMA);

            }catch (Exception err){
                err.printStackTrace();
            }
            SWFWriter writer=new SWFWriter(swf,swf.getHeader().getCompression());
            try {
                writer.writeTo(new File(file.getParent()+File.separator+"lzma_"+file.getName()));
            } catch (IOException e) {
                e.printStackTrace();
            }*/
        }
    }

    private void lzmaswf(java.util.List<File> list){
        for(File file:list){
            SWFReader reader=new SWFReader();
            SWF swf=null;
            try {
                swf = (SWF)reader.readFrom(new FileInputStream(file), file.getPath());
                swf.getHeader().setSignature(Header.SIGNATURE_COMPRESSED_LZMA);

            }catch (Exception err){
                err.printStackTrace();
            }
            SWFWriter writer=new SWFWriter(swf,swf.getHeader().getCompression());
            try {
                writer.writeTo(new File(file.getParent()+File.separator+"lzma_"+file.getName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //showMessageDialog(null,"over");
    }
    private void dump(java.util.List<File> list){

        for(File file:list){
            try{
                SWFDump.main(new String[]{"-decompile","-abc","-asm",file.getPath()});
            }catch (Exception e){
                e.printStackTrace();
            }

        }
        //showMessageDialog(null,"over");
    }
}
