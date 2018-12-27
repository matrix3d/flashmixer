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
	    SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				createAndShowGUI();
			}
		});

    }

    public static void createAndShowGUI(){
		try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}catch (Exception e){
			e.printStackTrace();
		}

		SwfToolUI ui=new SwfToolUI();
		ui.setSize(400,400);
		ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ui.setVisible(true);
	}
}
