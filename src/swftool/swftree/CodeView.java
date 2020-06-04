package swftool.swftree;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.text.*;
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
        tp=new TextPanelNoWarp();
        tp.setEditorKit(new ExtendedStyledEditorKit());
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
            //jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
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

class TextPanelNoWarp extends JTextPane{
    // Override getScrollableTracksViewportWidth
// to preserve the full width of the text
    /*public boolean getScrollableTracksViewportWidth() {
        Component parent = getParent();
        ComponentUI ui = getUI();

        return parent != null ? (ui.getPreferredSize(this).width <= parent
                .getSize().width) : true;
    }*/
}
//https://stackoverflow.com/questions/23149512/how-to-disable-wordwrap-in-java-jtextpane

/** To enable no wrap to JTextPane **/
 class ExtendedStyledEditorKit extends StyledEditorKit {
    private static final long serialVersionUID = 1L;

    private static final ViewFactory styledEditorKitFactory = (new StyledEditorKit()).getViewFactory();

    private static final ViewFactory defaultFactory = new ExtendedStyledViewFactory();

    public Object clone() {
        return new ExtendedStyledEditorKit();
    }

    public ViewFactory getViewFactory() {
        return defaultFactory;
    }

    /* The extended view factory */
    static class ExtendedStyledViewFactory implements ViewFactory {
        public View create(Element elem) {
            String elementName = elem.getName();
            if (elementName != null) {
                if (elementName.equals(AbstractDocument.ParagraphElementName)) {
                    return new ExtendedParagraphView(elem);
                }
            }

            // Delegate others to StyledEditorKit
            return styledEditorKitFactory.create(elem);
        }
    }

}

 class ExtendedParagraphView extends ParagraphView {
    public ExtendedParagraphView(Element elem) {
        super(elem);
    }

    @Override
    public float getMinimumSpan(int axis) {
        return super.getPreferredSpan(axis);
    }
}