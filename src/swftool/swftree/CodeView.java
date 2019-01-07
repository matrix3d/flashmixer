package swftool.swftree;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * created by lizhi
 */
public class CodeView extends JPanel {
    public CodeInfo visitor=null;
    public JTextPane tp;
    public CodeView(CodeInfo visitor)  {
        this.visitor=visitor;
        setLayout(new BorderLayout());
        tp=new JTextPane();
        try {

            //StringWriter buffer = new StringWriter();
            //PrintWriter writer = new PrintWriter(buffer);
            MyABCDumpVisitor dumpVisitor = new MyABCDumpVisitor(new CodePrinterWriter(this));
            dumpVisitor.viInfo(visitor);
            //writer.flush();
            //String contents = buffer.toString();
            //writer.close();
            //appendToPane(area,contents,Color.red);
            JScrollPane jScrollPane=new JScrollPane(tp);
            jScrollPane.getVerticalScrollBar().setUnitIncrement(12);
            add(jScrollPane,BorderLayout.CENTER);
        }catch (Exception e){

        }

    }

    public void appendToPane(String msg, Color c)
    {
        StyleContext sc = StyleContext.getDefaultStyleContext();

        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);
        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Consolas");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);
        int len = tp.getDocument().getLength();
        tp.setCaretPosition(len);
        tp.setCharacterAttributes(aset, false);
        tp.replaceSelection(msg);
    }
}
