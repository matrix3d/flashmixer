package swftool;

import com.adobe.flash.abc.print.ABCDumpVisitor;
import com.adobe.flash.abc.semantics.InstanceInfo;
import com.adobe.flash.abc.semantics.MethodInfo;

import java.io.PrintWriter;

/**
 * created by lizhi
 */
public class MyABCDumpVisitor extends ABCDumpVisitor {
    public MyABCDumpVisitor(PrintWriter p) {
        super(p);
    }

    public void viInfo(InstanceInfo info){
        traverseScriptTraits(info.traits,null);
        traverseScriptInit(info.iInit,null,0);
    }
}
