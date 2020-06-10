package swftool.swftree;

import com.adobe.flash.abc.ABCConstants;
import com.adobe.flash.abc.graph.IBasicBlock;
import com.adobe.flash.abc.graph.IFlowgraph;
import com.adobe.flash.abc.print.ABCDumpUtils;
import com.adobe.flash.abc.semantics.*;
import com.adobe.flash.utils.StringUtils;
import swftool.ABCEmitter;

import java.io.PrintWriter;
import java.util.*;

/**
 * created by lizhi
 */
public class MyABCDumpVisitor  {
    public CodeInfo codeInfo;
    private CodePrinterWriter printer=null;
    public MyABCDumpVisitor(CodePrinterWriter p) {
        //super(p);
        printer=p;
    }

    protected void traverseScriptTraits(Traits traits, ScriptInfo si) {
        Iterator i$ = traits.iterator();

        while(i$.hasNext()) {
            Trait t = (Trait)i$.next();
            switch(t.getKind()) {
                case 0:
                    this.traverseScriptSlotTrait(t, si);
                    break;
                case 1:
                    this.traverseScriptMethodTrait(t, si);
                    break;
                case 2:
                    this.traverseScriptGetterTrait(t, si);
                    break;
                case 3:
                    this.traverseScriptSetterTrait(t, si);
                    break;
                case 4:
                    this.traverseScriptClassTrait(t, si);
                    break;
                case 5:
                    this.traverseScriptFunctionTrait(t, si);
                    break;
                case 6:
                    this.traverseScriptConstTrait(t, si);
            }
        }

    }

    protected void traverseScriptSlotTrait(Trait trait, ScriptInfo scriptInfo) {
        this.writeSlotTrait("var", trait, false);
    }

    protected void traverseScriptConstTrait(Trait trait, ScriptInfo scriptInfo) {
        this.writeSlotTrait("const", trait, false);
    }

    protected void traverseScriptMethodTrait(Trait trait, ScriptInfo scriptInfo) {
        this.writeMethodTrait("function", trait, false);
    }

    protected void traverseScriptGetterTrait(Trait trait, ScriptInfo scriptInfo) {
        this.writeMethodTrait("function get", trait, false);
    }

    protected void traverseScriptSetterTrait(Trait trait, ScriptInfo scriptInfo) {
        this.writeMethodTrait("function set", trait, false);
    }

    protected void traverseScriptFunctionTrait(Trait trait, ScriptInfo scriptInfo) {
        this.writeMethodTrait("function", trait, false);
    }

    private void writeMethodTrait(String kindStr, Trait t, boolean isStatic) {
        String qual = ABCDumpUtils.nsQualifierForName(t.getName());
        String nameStr = ABCDumpUtils.nameToString(t.getName());
        MethodInfo methodInfo = (MethodInfo)t.getAttr("method_id");
        this.printer.println("",CodeType.norm);
        this.writeMetaData(t);
        this.writeMethodInfo(qual, nameStr, kindStr, methodInfo, isStatic, t.getBooleanAttr("override"), t.getBooleanAttr("final"));
    }

    public void viInfo(CodeInfo visitor){
        codeInfo=visitor;
        traverseScriptTraits(visitor.scriptInfo.getTraits(),visitor.scriptInfo);
    }

    protected void traverseScriptClassTrait(Trait trait, ScriptInfo scriptInfo) {
        ClassInfo ci = (ClassInfo)trait.getAttr("class_id");
        ABCEmitter.EmitterClassVisitor cv = codeInfo.classVisitor;//(ClassVisitor)this.getDefinedClasses().get(classIndex);
        InstanceInfo iinfo = cv.getInstanceInfo();
        this.traverseScriptClassTrait(0, iinfo, ci, trait, scriptInfo);
    }

    protected void traverseScriptClassTrait(int classId, InstanceInfo instanceInfo, ClassInfo classInfo, Trait trait, ScriptInfo scriptInfo) {
        this.printer.println("",CodeType.norm);
        int slotId = 0;
        if (trait.hasAttr("slot_id")) {
            slotId = trait.getIntAttr("slot_id");
        }

        this.printer.println("// class_id=" + classId + " slot_id=" + slotId,CodeType.comment);
        String def;
        if (instanceInfo.isInterface()) {
            def = "interface";
        } else {
            def = "class";
            if (!instanceInfo.isSealed()) {
                def = "dynamic " + def;
            }

            if (instanceInfo.isFinal()) {
                def = "final " + def;
            }
        }

        this.writeMetaData(trait);
        this.printer.print(ABCDumpUtils.nsQualifierForName(trait.getName()),CodeType.key);
        printer.print(def,CodeType.key);
        printer.print(" ",CodeType.norm);
        printer.print(ABCDumpUtils.nameToString(trait.getName()),CodeType.Class);
        printer.print(" extends ",CodeType.key);
        printer.println(ABCDumpUtils.nameToString(instanceInfo.superName),CodeType.Class);
        if (instanceInfo.interfaceNames.length > 0) {
            this.printer.indent();
            List<String> interfaceNames = new ArrayList();
            Name[] arr$ = instanceInfo.interfaceNames;
            int len$ = arr$.length;

            for(int i$ = 0; i$ < len$; ++i$) {
                Name interfaceName = arr$[i$];
                interfaceNames.add(ABCDumpUtils.nameToString(interfaceName));
            }

            this.printer.println(StringUtils.joinOn(",", interfaceNames),CodeType.Class);
            this.printer.unindent();
        }

        this.printer.println("{",CodeType.norm);
        this.printer.indent();
        this.traverseInstanceInit(instanceInfo.iInit, instanceInfo, trait, scriptInfo);
        this.traverseInstanceTraits(instanceInfo.traits);
        this.traverseClassInit(classInfo.cInit, classInfo, trait, scriptInfo);
        this.traverseClassTraits(classInfo.classTraits);
        this.printer.unindent();
        this.printer.println("}",CodeType.norm);
    }

    protected void traverseClassTraits(Traits traits) {
        Iterator i$ = traits.iterator();

        while(i$.hasNext()) {
            Trait t = (Trait)i$.next();
            switch(t.getKind()) {
                case 0:
                    this.traverseClassSlotTrait(t);
                    break;
                case 1:
                    this.traverseClassMethodTrait(t);
                    break;
                case 2:
                    this.traverseClassGetterTrait(t);
                    break;
                case 3:
                    this.traverseClassSetterTrait(t);
                case 4:
                default:
                    break;
                case 5:
                    this.traverseClassFunctionTrait(t);
                    break;
                case 6:
                    this.traverseClassConstTrait(t);
            }
        }

    }

    protected void traverseClassSlotTrait(Trait trait) {
        this.writeSlotTrait("var", trait, true);
    }

    protected void traverseClassConstTrait(Trait trait) {
        this.writeSlotTrait("const", trait, true);
    }

    protected void traverseClassMethodTrait(Trait trait) {
        this.writeMethodTrait("function", trait, true);
    }

    protected void traverseClassGetterTrait(Trait trait) {
        this.writeMethodTrait("function get", trait, true);
    }

    protected void traverseClassSetterTrait(Trait trait) {
        this.writeMethodTrait("function set", trait, true);
    }

    protected void traverseClassFunctionTrait(Trait trait) {
        this.writeMethodTrait("function", trait, true);
    }


    protected void traverseInstanceTraits(Traits traits) {
        Iterator i$ = traits.iterator();

        while(i$.hasNext()) {
            Trait t = (Trait)i$.next();
            switch(t.getKind()) {
                case 0:
                    this.traverseInstanceSlotTrait(t);
                    break;
                case 1:
                    this.traverseInstanceMethodTrait(t);
                    break;
                case 2:
                    this.traverseInstanceGetterTrait(t);
                    break;
                case 3:
                    this.traverseInstanceSetterTrait(t);
                case 4:
                default:
                    break;
                case 5:
                    this.traverseInstanceFunctionTrait(t);
                    break;
                case 6:
                    this.traverseInstanceConstTrait(t);
            }
        }

    }

    protected void traverseInstanceSlotTrait(Trait trait) {
        this.writeSlotTrait("var", trait, false);
    }

    protected void traverseInstanceConstTrait(Trait trait) {
        this.writeSlotTrait("const", trait, false);
    }

    protected void traverseInstanceMethodTrait(Trait trait) {
        this.writeMethodTrait("function", trait, false);
    }

    protected void traverseInstanceGetterTrait(Trait trait) {
        this.writeMethodTrait("function get", trait, false);
    }

    protected void traverseInstanceSetterTrait(Trait trait) {
        this.writeMethodTrait("function set", trait, false);
    }

    protected void traverseInstanceFunctionTrait(Trait trait) {
        this.writeMethodTrait("function", trait, false);
    }


    protected void traverseClassInit(MethodInfo init, ClassInfo classInfo, Trait classTrait, ScriptInfo scriptInfo) {
        this.printer.println("",CodeType.norm);
        this.writeMethodInfo("public ", ABCDumpUtils.nameToString(classTrait.getName()) + "$", "function", init, true, false, false);
    }

    protected void traverseInstanceInit(MethodInfo init, InstanceInfo instanceInfo, Trait classTrait, ScriptInfo scriptInfo) {
        printer.println("",CodeType.norm);
        //this.printer.println("// method_id=" + this.getMethodInfos().getId(instanceInfo.iInit));
        this.writeMethodInfo("public ", ABCDumpUtils.nameToString(classTrait.getName()), "function", init, false, false, false);

    }

    protected MethodBodyInfo getMethodBodyForMethodInfo(MethodInfo mi) {
        return codeInfo.abc.methodMap.get(mi);
    }

    protected void writeMethodInfo(String qualStr, String nameStr, String kindStr, MethodInfo methodInfo, boolean isStatic, boolean isOverride, boolean isFinal) {
        //this.dumpedMethods.add(methodInfo);
        //List<String> paramTypeStrings = new Vector();
        //Iterator i$ = methodInfo.getParamTypes().iterator();

        //methodInfo.getParamTypes().

        //while(i$.hasNext()) {
        //    Name paramTypeName = (Name)i$.next();
        //    paramTypeStrings.add(ABCDumpUtils.nameToString(paramTypeName));
        //}

        String staticStr = isStatic ? "static " : "";
        String overrideStr = isOverride ? "override " : "";
        String nativeStr = methodInfo.isNative() ? "native " : "";
        String finalStr = isFinal ? "final " : "";
        if (nameStr == "") {
            nameStr = methodInfo.getMethodName();
        }

        this.printer.print(qualStr + staticStr,CodeType.key);
        this.printer.print(nativeStr + finalStr + overrideStr + kindStr + " ",CodeType.key);
        this.printer.print(nameStr ,CodeType.norm);
        this.printer.print("(",CodeType.norm) ;

        for(int i=0;i<methodInfo.getParamTypes().size();i++){
            if(i!=0){
                printer.print(",",CodeType.norm);
            }
            this.printer.print(methodInfo.getParamNames().get(i),CodeType.norm);
            this.printer.print(":",CodeType.norm);
            this.printer.print(ABCDumpUtils.nameToString(methodInfo.getParamTypes().get(i)),CodeType.Class);
            if(i<methodInfo.getDefaultValues().size()){
                this.printer.print("=",CodeType.norm);
                Object dvalue=methodInfo.getDefaultValues().get(i).getValue();
                if(dvalue instanceof String){
                    dvalue="\""+StringUtils.stringToEscapedString((String) dvalue)+"\"";
                }
                if(dvalue==ABCConstants.NULL_VALUE){
                    dvalue="null";
                }
                if(dvalue==ABCConstants.UNDEFINED_VALUE){
                    dvalue="undefined";
                }
                this.printer.print(dvalue.toString(),CodeType.norm);
            }
        }
        //this.printer.print(StringUtils.joinOn(",", paramTypeStrings),CodeType.Class);

        this.printer.print( "):",CodeType.norm);
        this.printer.println(ABCDumpUtils.nameToString(methodInfo.getReturnType()),CodeType.Class);
        MethodBodyInfo mb = this.getMethodBodyForMethodInfo(methodInfo);
        if (mb != null) {
            this.printer.println("{",CodeType.norm);
            this.printer.indent();
            TablePrinter tablePrinter = new TablePrinter(3, 2);
            tablePrinter.addRow(new String[]{"//", "derivedName", methodInfo.getMethodName()},CodeType.comment);
            //tablePrinter.addRow(new String[]{"//", "method_info", String.valueOf(this.getMethodInfos().getId(mb.getMethodInfo()))});
            tablePrinter.addRow(new String[]{"//", "max_stack", String.valueOf(mb.max_stack)},CodeType.comment);
            tablePrinter.addRow(new String[]{"//", "max_regs", String.valueOf(mb.max_local)},CodeType.comment);
            tablePrinter.addRow(new String[]{"//", "scope_depth", String.valueOf(mb.initial_scope)},CodeType.comment);
            tablePrinter.addRow(new String[]{"//", "max_scope", String.valueOf(mb.max_scope)},CodeType.comment);
            tablePrinter.addRow(new String[]{"//", "code_length", String.valueOf(mb.code_len)},CodeType.comment);
            tablePrinter.print(this.printer);
            if (mb.getTraits() != null && mb.getTraits().getTraitCount() > 0) {
                this.printer.println("activation_traits {",CodeType.norm);
                this.printer.indent();

                Trait trait;
                for(Iterator i$4 = mb.getTraits().iterator(); i$4.hasNext(); this.writeSlotTrait(kindStr, trait, false)) {
                    trait = (Trait)i$4.next();
                    switch(trait.getKind()) {
                        case 0:
                            kindStr = "var";
                            break;
                        case 6:
                            kindStr = "const";
                            break;
                        default:
                            throw new Error("Illegal activation trait in " + methodInfo.getMethodName());
                    }
                }

                this.printer.unindent();
                this.printer.println("}",CodeType.norm);
            }

            IFlowgraph cfg = mb.getCfg();
            Map<IBasicBlock, String> blockNames = new HashMap();
            int i = 0;
            Iterator i$3 = cfg.getBlocksInEntryOrder().iterator();

            while(i$3.hasNext()) {
                IBasicBlock block = (IBasicBlock)i$3.next();
                blockNames.put(block, "bb" + i++);
            }

            int offset = 0;
            Iterator i$5 = cfg.getBlocksInEntryOrder().iterator();

            while(i$5.hasNext()) {
                IBasicBlock block = (IBasicBlock)i$5.next();
                this.printer.println((String)blockNames.get(block),CodeType.norm);
                this.printer.indent();
                Collection<? extends IBasicBlock> succs = block.getSuccessors();
                List<String> succNames = new ArrayList();
                Iterator i$2 = succs.iterator();

                while(i$2.hasNext()) {
                    IBasicBlock s = (IBasicBlock)i$2.next();
                    succNames.add(blockNames.get(s));
                }

                this.printer.println("succs=[" + StringUtils.joinOn(",", succNames) + "]",CodeType.norm);
                tablePrinter = new TablePrinter(4, 2);

                for(int j = 0; j < block.size(); ++j) {
                    Instruction inst = block.get(j);
                    String constantStr = "";
                    if(j==6){
                        //System.out.println(inst.hasOperands());
                        //S/ystem.out.println(inst.getOperand(0) instanceof Name);
                        //System.out.println(1);
                    }
                    if (inst.getOperandCount()>0 && inst.getOperand(0) instanceof Name) {
                        constantStr = ABCDumpUtils.nameToString((Name)inst.getOperand(0));
                    } else if (inst.isBranch() && inst.getOpcode() != 27) {
                        constantStr = (String)blockNames.get(cfg.getBlock((Label)inst.getOperand(0)));
                    } else {
                        switch(inst.getOpcode()) {
                            case 27:
                                constantStr = this.stringForLookupSwitch(inst, mb, blockNames, cfg);
                                break;
                            case 44:
                            case 241:
                                constantStr = "\"" + StringUtils.stringToEscapedString((String)inst.getOperand(0)) + "\"";
                                break;
                            case 240:
                                constantStr = String.valueOf(inst.getImmediate());
                        }
                    }

                    tablePrinter.addRow(new String[]{offset + "    ", Instruction.decodeOp(inst.getOpcode()), constantStr, inst.isImmediate() ? String.valueOf(inst.getImmediate()) : ""},CodeType.norm);
                    ++offset;
                }

                tablePrinter.print(this.printer);
                this.printer.unindent();
            }

            this.printer.unindent();
            this.printer.println("}",CodeType.norm);
            if (mb.getExceptions().size() > 0) {
                tablePrinter = new TablePrinter(7, 2);
                tablePrinter.addRow(new String[]{"//", "exception", "start", "end", "target", "type string", "name string"},CodeType.comment);

                for(i = 0; i < mb.getExceptions().size(); ++i) {
                    ExceptionInfo exception = (ExceptionInfo)mb.getExceptions().get(i);
                    tablePrinter.addRow(new String[]{"//", String.valueOf(i), String.valueOf(exception.getFrom().getPosition()), String.valueOf(exception.getTo().getPosition()), String.valueOf(exception.getTarget().getPosition()), ABCDumpUtils.nameToString(exception.getExceptionType()), ABCDumpUtils.nameToString(exception.getCatchVar())},CodeType.norm);
                }

                tablePrinter.print(this.printer);
                this.printer.println("",CodeType.norm);
            }
        }
    }

    public static class TablePrinter {
        private int cols;
        private int minPadding;
        private Vector<Row> m_rows;

        public TablePrinter(int nCols, int minPadding) {
            this.cols = nCols;
            this.minPadding = minPadding;
            this.m_rows = new Vector();
        }

        public void addRow(String[] r,int type) {
            if (r.length != this.cols) {
                throw new Error("Invalid row");
            } else {
                this.m_rows.add(new Row(r,type));
            }
        }

        public void print(CodePrinterWriter p) {
            int[] colWidths = new int[this.cols];

            for(int i = 0; i < this.cols; ++i) {
                colWidths[i] = 0;
            }

            Iterator i$ = this.m_rows.iterator();

            Row r;
            while(i$.hasNext()) {
                r = (Row)i$.next();
                r.measure(colWidths, this.minPadding);
            }

            i$ = this.m_rows.iterator();

            while(i$.hasNext()) {
                r = (Row)i$.next();
                r.print(p, colWidths);
            }

        }

        private class Row {
            private String[] cells;
            private int type;
            public Row(String[] cells,int type) {
                this.cells = cells;
                this.type=type;
            }

            public void measure(int[] colWidths, int minPadding) {
                for(int i = 0; i < this.cells.length; ++i) {
                    colWidths[i] = Math.max(colWidths[i], this.getRowItemStr(i).length() + minPadding);
                }

            }

            public void print(CodePrinterWriter p, int[] colWidths) {
                String rowStr = "";

                for(int i = 0; i < this.cells.length; ++i) {
                    int colW=colWidths[i];
                    rowStr = rowStr + this.padString(this.getRowItemStr(i), colWidths[i]);
                }

                p.println(rowStr,type);
            }

            private String getRowItemStr(int i) {
                if (this.cells[i] == null) {
                    return "null";
                } else {
                    return i < this.cells.length ? this.cells[i] : "error - out of range " + i;
                }
            }

            private String padString(String s, int minLength) {
                while(s.length() < minLength) {
                    s = s + " ";
                }

                return s;
            }
        }
    }

    private void writeSlotTrait(String kindStr, Trait t, boolean isStatic) {
        this.printer.println("",CodeType.norm);
        String qual = ABCDumpUtils.nsQualifierForName(t.getName());
        String nameStr = ABCDumpUtils.nameToString(t.getName());
        Object value = null;
        if (t.hasAttr("value")) {
            value = t.getAttr("value");
        }

        String staticStr = isStatic ? "static " : "";
        this.writeMetaData(t);
        this.printer.print(qual + staticStr + kindStr,CodeType.key);
        this.printer.print(         " " + nameStr + ":" ,CodeType.norm);
        this.printer.print(                ABCDumpUtils.nameToString((Name)t.getAttr("type")) ,CodeType.Class);
        //String valueStr = "";
        if (value instanceof String) {
            this.printer.print(" = ",CodeType.norm);
            this.printer.print("\"" + value + "\"",CodeType.str);
            //valueStr = " = \"" + value + "\"";
        } else if (value instanceof Namespace) {
            this.printer.print(" = ",CodeType.norm);
            this.printer.print(((Namespace)value).getName(),CodeType.Class);
            //valueStr = " = " + ((Namespace)value).getName();
        } else if (value == ABCConstants.NULL_VALUE) {
            this.printer.print(" = ",CodeType.norm);
            this.printer.print("null",CodeType.key);
            //valueStr = " = null";
        } else if (value == ABCConstants.UNDEFINED_VALUE) {
            //valueStr = "";
        } else if (value != null) {
            this.printer.print(" = " + value.toString(),CodeType.norm);
        }
        this.printer.println("",CodeType.norm);
    }

    private void writeMetaData(Trait t) {
        if (t.hasMetadata()) {
            Iterator i$ = t.getMetadata().iterator();

            while(i$.hasNext()) {
                Metadata mid = (Metadata)i$.next();
                List<String> entries = new Vector();
                String[] keys = mid.getKeys();

                for(int i = 0; i < keys.length; ++i) {
                    String key = keys[i];
                    String value = mid.getValues()[i];
                    if (key != null && key.length() != 0) {
                        entries.add(key + "=\"" + value + "\"");
                    } else {
                        entries.add("\"" + value + "\"");
                    }
                }

                StringBuilder sb = new StringBuilder();

                for(int i = 0; i < entries.size(); ++i) {
                    sb.append((String)entries.get(i));
                    if (i < entries.size() - 1) {
                        sb.append(", ");
                    }
                }

                this.printer.println("[" + mid.getName() + "(" + sb.toString() + ")]" + " // metadata_id=" + mid,CodeType.norm);
            }

        }
    }

    private String stringForLookupSwitch(Instruction inst, MethodBodyInfo mb, Map<IBasicBlock, String> blockNames, IFlowgraph cfg) {
        int case_size = inst.getOperandCount() - 1;
        String defaultStr = "default: " + (String)blockNames.get(cfg.getBlock((Label)inst.getOperand(case_size)));
        String maxCaseStr = "maxcase: " + case_size;
        List<String> result = new Vector();
        result.add(defaultStr);
        result.add(maxCaseStr);

        for(int i = 0; i < case_size; ++i) {
            result.add(blockNames.get(cfg.getBlock((Label)inst.getOperand(i))));
        }

        return StringUtils.joinOn(" ", result);
    }
}
