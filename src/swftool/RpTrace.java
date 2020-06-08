package swftool;

import com.adobe.flash.abc.ABCConstants;
import com.adobe.flash.abc.ABCParser;
import com.adobe.flash.abc.semantics.*;
import com.adobe.flash.swf.Header;
import com.adobe.flash.swf.SWF;
import com.adobe.flash.swf.SWFFrame;
import com.adobe.flash.swf.io.SWFReader;
import com.adobe.flash.swf.tags.DoABCTag;
import com.adobe.flash.swf.tags.ITag;

import java.io.BufferedInputStream;
import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * created by lizhi
 */
public class RpTrace {
    public SWF outswf=null;
    public RpTrace(File file){
        SWFReader reader=new SWFReader();
        SWF swf=null;
        try {
            URL url=file.toURI().toURL();
            swf = (SWF)reader.readFrom(new BufferedInputStream(url.openStream()), url.getPath());
            outswf=swf;
            swf.getHeader().setSignature(Header.SIGNATURE_COMPRESSED_LZMA);

        }catch (Exception err){
            err.printStackTrace();
        }
        //System.out.println(System.currentTimeMillis()-time);

        for(SWFFrame frame: swf.getFrames()){
            Iterator<ITag> it=frame.iterator();
            while (it.hasNext()){
                ITag iTag= it.next();
                if(iTag instanceof DoABCTag){
                    DoABCTag doABCTag=(DoABCTag) iTag ;
                    ABCParser abcParser=new ABCParser(doABCTag.getABCData());
                    ABCEmitter abc=new ABCEmitter();
                    abc.setAllowBadJumps(true);
                    abcParser.parseABC(abc);

                    for (MethodBodyInfo mb : abc.methodBodies){
                        MethodInfo mi=mb.getMethodInfo();
                        Vector<Name> paramTypes= mi.getParamTypes();
                        List<String> paramNames= mi.getParamNames();
                        Vector<PooledValue> defaultValues= mi.getDefaultValues();

                        System.out.println(mi.getMethodName());

                        if(mb.getInstructionList()!=null&&mi.getMethodName()!=null&&mi.getMethodName().equals("test")) {
                            for (Instruction instruction : mb.getInstructionList().getInstructions()) {
                                System.out.println(instruction.getOpcode()+":"+Instruction.decodeOp(instruction.getOpcode()));
                                if(instruction.getOpcode()==ABCConstants.OP_getlex){
                                    if (instruction.getOperandCount() > 0 && instruction.getOperand(0) instanceof Name) {
                                        Name name = (Name) instruction.getOperand(0);
                                        System.out.println(name.getSingleQualifier().getName()+"::"+name.getBaseName()+"-------------");

                                        if(name.getSingleQualifier().getName().equals("")&&name.getBaseName().equals("trace")){
                                            Name name2=new Name(new Namespace(name.getSingleQualifier().getKind(),""),"trace2");
                                            OneOperandInstruction instruction1=(OneOperandInstruction)instruction;
                                            instruction1.operand=name2;
                                        }
                                        Name name3 = (Name) instruction.getOperand(0);
                                        System.out.println(name3.getSingleQualifier().getName()+"::"+name3.getBaseName()+"name3-------------");
                                    }
                                }
                            }

                            try{
                                doABCTag.setABCData(abc.emit());
                            }catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }
}
