package swftool;

import com.adobe.flash.abc.print.ABCDumpVisitor;
import com.adobe.flash.abc.semantics.ScriptInfo;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

/**
 * created by lizhi
 */
public class CodeView extends JPanel {
    public CodeInfo visitor=null;
    public CodeView(CodeInfo visitor)  {
        this.visitor=visitor;
        setLayout(new BorderLayout());
        JTextPane area=new JTextPane();
        try {

            StringWriter buffer = new StringWriter();
            PrintWriter writer = new PrintWriter(buffer);
            MyABCDumpVisitor dumpVisitor = new MyABCDumpVisitor(writer);
            dumpVisitor.viInfo(visitor);
            writer.flush();
            String contents = buffer.toString();
            writer.close();
            appendToPane(area,contents,Color.red);
            add(new JScrollPane(area),BorderLayout.CENTER);
        }catch (Exception e){

        }

    }

    private void appendToPane(JTextPane tp, String msg, Color c)
    {
        StyleContext sc = StyleContext.getDefaultStyleContext();

        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

        int len = tp.getDocument().getLength();
        tp.setCaretPosition(len);
        tp.setCharacterAttributes(aset, false);
        tp.replaceSelection(msg);
    }
}
