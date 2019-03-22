package swftool;

import antlr.TokenBuffer;
import com.adobe.flash.abc.ABCLinker;
import com.adobe.flash.abc.ABCParser;
import com.adobe.flash.compiler.clients.MXMLC;
import com.adobe.flash.compiler.filespecs.FileSpecification;
import com.adobe.flash.compiler.internal.parsing.as.ASParser;
import com.adobe.flash.swc.*;
import com.adobe.flash.swc.io.SWCReader;
import com.adobe.flash.swf.Header;
import com.adobe.flash.swf.SWF;
import com.adobe.flash.swf.SWFFrame;
import com.adobe.flash.swf.io.SWFDump;
import com.adobe.flash.swf.io.SWFReader;
import com.adobe.flash.swf.io.SWFWriter;
import com.adobe.flash.swf.tags.DoABCTag;
import com.adobe.flash.swf.tags.ITag;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.scene.layout.VBox;
import swftool.build.Builder;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.io.*;
import java.util.*;
import java.util.List;

import static javax.swing.JOptionPane.*;

public class SwfToolUI extends JPanel {
    JCheckBox mixclass;
    JCheckBox mixpackage;
    JCheckBox mixmap;
    JCheckBox negMixmap;
    JCheckBox mixvar;
    JCheckBox mixfunc;
    JCheckBox reservedStructure;
    JTextField mixcode;
    JTextField nomixpack;
    public SwfToolUI(){
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));


         mixclass=new JCheckBox("mixclass",true);
        add(mixclass);
         mixpackage=new JCheckBox("mixpackage",true);
        add(mixpackage);
         mixmap=new JCheckBox("mixmap",true);
        add(mixmap);
        negMixmap=new JCheckBox("negmixmap",false);
        add(negMixmap);
         mixvar=new JCheckBox("mixvar",true);
        add(mixvar);
         mixfunc=new JCheckBox("mixfunc",true);
        add(mixfunc);
        reservedStructure=new JCheckBox("Reserved structure",true);
        add(reservedStructure);
         mixcode=new JTextField("#");
        add(mixcode);
        nomixpack=new JTextField("morn.core.components");
        add(nomixpack);

        JPanel jPanel=new JPanel();
        add(jPanel);
        jPanel.setLayout(new GridLayout());

        JButton btn1=new JButton("lzma swf");
        btn1.setPreferredSize(new Dimension(100,100));
        jPanel.add(btn1);

        JButton btn2=new JButton("mix swf");
        btn2.setPreferredSize(new Dimension(100,100));
        jPanel.add(btn2);
        JButton btn3=new JButton("dump swf");
        btn3.setPreferredSize(new Dimension(100,100));
        jPanel.add(btn3);
        JButton btn4=new JButton("build swf");
        btn4.setPreferredSize(new Dimension(100,100));
        jPanel.add(btn4);
        JButton btn5=new JButton("link swf");
        btn5.setPreferredSize(new Dimension(100,100));
        jPanel.add(btn5);

        JButton btn6=new JButton("at enable");
        btn6.setPreferredSize(new Dimension(100,100));
        jPanel.add(btn6);

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
        new DropTarget(btn6, DnDConstants.ACTION_COPY_OR_MOVE,new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try{
                    if(dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)){
                        dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                        java.util.List<File> list = (java.util.List<File>)(dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor));
                        atenableswf(list);
                        dtde.dropComplete(true);
                    }else{
                        dtde.rejectDrop();
                    }
                }catch (Exception e){

                }
            }
        });
        new DropTarget(btn5, DnDConstants.ACTION_COPY_OR_MOVE,new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try{
                    if(dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)){
                        dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                        java.util.List<File> list = (java.util.List<File>)(dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor));
                        linkswf(list);
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

        new DropTarget(btn4, DnDConstants.ACTION_COPY_OR_MOVE,new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try{
                    if(dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)){
                        dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                        java.util.List<File> list = (java.util.List<File>)(dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor));
                        build(list);
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

                if(negMixmap.isSelected()){
                    HashMap<String,String> negmap=new HashMap<>();
                    for(String key : mixMap.keySet()){
                        negmap.put(mixMap.get(key),key);
                    }
                    mixMap=negmap;
                }

            }catch (Exception e){
                //e.printStackTrace();
            }

            Mixer mixer= new Mixer(file,mixclass.isSelected(),mixpackage.isSelected(),mixvar.isSelected(),mixfunc.isSelected(),mixMap,mixcode.getText(),reservedStructure.isSelected(),nomixpack.getText());
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
    private void atenableswf(java.util.List<File> list){
        for(File file:list){
            SWFReader reader=new SWFReader();
            SWF swf=null;
            try {
                swf = (SWF)reader.readFrom(new FileInputStream(file), file.getPath());
               // swf.addFrame();new

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
    private void linkswf(java.util.List<File> list){
        List<byte[]> abcs=new ArrayList();
        List<DoABCTag> abcts=new ArrayList();
        List<ABCEmitter> abcps=new ArrayList();
        List<SWF> swfs=new ArrayList();
        List<File> swffiles=new ArrayList<>();
        for(File file:list){
            System.out.println(file.getName());
            String suffix = file.getName().substring(file.getName().lastIndexOf(".") + 1);
            if(suffix.toLowerCase().equals("swc")){
                SWCReader reader=new SWCReader(file);
                SWC swc= (SWC) reader.getSWC();
                Collection<ISWCLibrary> iswcLibraries= swc.getLibraries();
                for(ISWCLibrary swcLibrary: iswcLibraries){
                    SWFReader swfReader=new SWFReader();
                    swcLibrary.readSWFInputStream(swfReader,swc);

                    SWF swf=(SWF)swfReader.getSWF();
                    for(SWFFrame frame: swf.getFrames()){
                        Iterator<ITag> it=frame.iterator();
                        while (it.hasNext()){
                            ITag iTag= it.next();
                            if(iTag instanceof DoABCTag){
                                DoABCTag abcTag=(DoABCTag) iTag;
                                abcs.add(abcTag.getABCData());
                            }
                        }
                    }

                   for(ISWCScript iswcScript: swcLibrary.getScripts()){
                       SWCScript swcScript=(SWCScript)iswcScript;
                       byte[] source= swcScript.getSource();
                   }
                }
            }else {
                SWFReader reader=new SWFReader();
                SWF swf=null;
                try {
                    swf = (SWF)reader.readFrom(new FileInputStream(file), file.getPath());
                    swfs.add(swf);
                    swffiles.add(file);
                    for(SWFFrame frame: swf.getFrames()){
                        Iterator<ITag> it=frame.iterator();
                        int i=0;
                        while (it.hasNext()){
                            ITag iTag= it.next();
                            if(iTag instanceof DoABCTag){
                                DoABCTag abcTag=(DoABCTag) iTag;
                                abcs.add(abcTag.getABCData());
                                if(i==0) {
                                    abcts.add(abcTag);
                                    ABCParser abcParser = new ABCParser(abcTag.getABCData());
                                    ABCEmitter abc = new ABCEmitter();
                                    abc.setAllowBadJumps(true);
                                    abcParser.parseABC(abc);
                                    abcps.add(abc);
                                    i++;
                                }
                            }
                        }
                    }
                }catch (Exception err){
                    err.printStackTrace();
                }
            }


        }
        for(int i=0;i<swfs.size();i++){
            SWF swf=swfs.get(i);
            File file=swffiles.get(i);
            ABCEmitter abc=abcps.get(i);
            DoABCTag abcTag=abcts.get(i);

            try{
                abcTag.setABCData(ABCLinker.linkABC(abcs,abc.versionABCMajor,abc.versionABCMinor,new ABCLinker.ABCLinkerSettings()));
            }catch (Exception e){
                e.printStackTrace();
            }

            SWFWriter writer=new SWFWriter(swf,swf.getHeader().getCompression());
            try {
               writer.writeTo(new File(file.getParent()+File.separator+"link_"+file.getName()));
            } catch (IOException e) {
               e.printStackTrace();
            }
        }


        //showMessageDialog(null,"over");
    }
    private void dump(java.util.List<File> list){

        for(File file:list){
            try{
                SWFDump.main(new String[]{"-decompile","-abc",file.getPath()});
            }catch (Exception e){
                e.printStackTrace();
            }

        }
        //showMessageDialog(null,"over");
    }
    private void build(java.util.List<File> list){
        for(File file:list){
            new Builder(file);

        }
        //showMessageDialog(null,"over");
    }
}
