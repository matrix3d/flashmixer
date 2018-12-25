import com.adobe.flash.compiler.clients.MXMLC;
import com.adobe.flash.swf.Header;
import com.adobe.flash.swf.ISWF;
import com.adobe.flash.swf.SWF;
import com.adobe.flash.swf.io.SWFReader;
import com.adobe.flash.swf.io.SWFWriter;
import com.google.gson.Gson;
import jdk.nashorn.internal.parser.JSONParser;
import swftool.SwfToolUI;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class Main {

    public static void main(String[] args){
	    try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch (Exception e){
	        e.printStackTrace();
        }

        SwfToolUI ui=new SwfToolUI();
	    ui.setSize(400,400);
	    ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    ui.setVisible(true);

	    String s="a[___mix___][___source___][___mix___]";
	    String mix="mix";
	    String sour="123";
	    System.out.println(s.replace("[___mix___]",mix).replace("[___source___]",sour));

	    HashMap<String,String> map1=new HashMap<>();
	    map1.put("d","3");
	    map1.put("d2","3");

		Gson gson=new Gson();
		String ss= gson.toJson(map1);
		HashMap<String,String> map= gson.fromJson(ss, HashMap.class);
		System.out.println(1);

		HashSet<String> set1=new HashSet<>();
		set1.add("333");
		set1.add("3334");
		String ss2= gson.toJson(set1);
		HashSet<String> set2=gson.fromJson(ss2,HashSet.class);
		System.out.println(2);
		// write your code here
        //MXMLC.staticMainNoExit(new String[]{"Test.as"});
       // mxmlc.main(null);
        /*SWFReader reader=new SWFReader();
        SWF swf=null;
        try {
            swf = (SWF)reader.readFrom(new FileInputStream("C:\\Users\\li456\\IdeaProjects\\asc2\\Test.swf"), "C:\\Users\\li456\\IdeaProjects\\asc2\\Test.swf");
            swf.getHeader().setSignature(Header.SIGNATURE_COMPRESSED_LZMA);

        }catch (Exception err){
            err.printStackTrace();
        }
        SWFWriter writer=new SWFWriter(swf,swf.getHeader().getCompression());
        try {
            writer.writeTo(new File("C:\\Users\\li456\\IdeaProjects\\asc2\\Test2.swf"));
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }
}
