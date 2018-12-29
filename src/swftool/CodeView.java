package swftool;

import com.adobe.flash.abc.print.ABCDumpVisitor;

import javax.swing.*;
import java.awt.*;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

/**
 * created by lizhi
 */
public class CodeView extends JPanel {
    public ABCEmitter.EmitterClassVisitor visitor=null;
    public CodeView(ABCEmitter.EmitterClassVisitor visitor)  {
        this.visitor=visitor;
        setLayout(new BorderLayout());
        JTextArea area=new JTextArea();
        try {

            StringWriter buffer = new StringWriter();
            PrintWriter writer = new PrintWriter(buffer);
            MyABCDumpVisitor dumpVisitor = new MyABCDumpVisitor(writer);
            dumpVisitor.viInfo(visitor.instanceInfo);
            writer.flush();
            String contents = buffer.toString();
            writer.close();
            area.setText(contents);
            add(area,BorderLayout.CENTER);
        }catch (Exception e){

        }

    }
}
