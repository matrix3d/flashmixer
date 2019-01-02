package swftool.swftree;

import java.awt.*;
import java.util.HashMap;

/**
 * created by lizhi
 */
public class CodePrinterWriter {
    private int numindent=0;
    private String indentStr="";
    private CodeView codeView;
    private HashMap<Integer,Color> integerColorHashMap;
    private boolean isNewLine=true;
    public CodePrinterWriter(CodeView codeView){
        this.codeView=codeView;
        integerColorHashMap=new HashMap<>();
        integerColorHashMap.put(CodeType.key,Color.orange);
        integerColorHashMap.put(CodeType.norm,Color.black);
        integerColorHashMap.put(CodeType.Class,Color.blue);
        integerColorHashMap.put(CodeType.comment,Color.gray);
        integerColorHashMap.put(CodeType.str,Color.magenta);
    }
    public void print(String string,int type){
        if(isNewLine&&numindent>0){
            isNewLine=false;
            print(indentStr,type);
        }
        codeView.appendToPane(string, integerColorHashMap.get(type));
    }

    public void println(String string,int type){
        print(string,type);
        print("\n",type);
        isNewLine=true;
    }

    public void unindent(){
        numindent--;
        updateIndent();
    }

    public void indent(){
      numindent++;
      updateIndent();
    }

    private void updateIndent(){
        indentStr="";
        for(int i=0;i<numindent;i++){
            indentStr+="    ";
        }
    }
}
