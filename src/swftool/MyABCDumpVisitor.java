package swftool;

import com.adobe.flash.abc.ABCConstants;
import com.adobe.flash.abc.PoolingABCVisitor;
import com.adobe.flash.abc.graph.IBasicBlock;
import com.adobe.flash.abc.graph.IFlowgraph;
import com.adobe.flash.abc.print.ABCDumpUtils;
import com.adobe.flash.abc.print.ABCDumpVisitor;
import com.adobe.flash.abc.semantics.*;
import com.adobe.flash.utils.StringUtils;

import java.io.PrintWriter;
import java.util.*;

/**
 * created by lizhi
 */
public class MyABCDumpVisitor extends ABCDumpVisitor {
    public CodeInfo codeInfo;
    private PrintWriter printer=null;
    public MyABCDumpVisitor(PrintWriter p) {
        super(p);
        printer=p;
    }

    public void viInfo(CodeInfo visitor){
        codeInfo=visitor;
        traverseScriptTraits(visitor.scriptInfo.getTraits(),visitor.scriptInfo);
    }

    @Override
    protected void traverseScriptClassTrait(Trait trait, ScriptInfo scriptInfo) {
        ClassInfo ci = (ClassInfo)trait.getAttr("class_id");
        ABCEmitter.EmitterClassVisitor cv = codeInfo.classVisitor;//(ClassVisitor)this.getDefinedClasses().get(classIndex);
        InstanceInfo iinfo = cv.getInstanceInfo();
        this.traverseScriptClassTrait(0, iinfo, ci, trait, scriptInfo);
    }

    @Override
    protected void traverseInstanceInit(MethodInfo init, InstanceInfo instanceInfo, Trait classTrait, ScriptInfo scriptInfo) {
        printer.println("");
        //this.printer.println("// method_id=" + this.getMethodInfos().getId(instanceInfo.iInit));
        this.writeMethodInfo("public ", ABCDumpUtils.nameToString(classTrait.getName()), "function", init, false, false, false);

    }

    @Override
    protected MethodBodyInfo getMethodBodyForMethodInfo(MethodInfo mi) {
        return codeInfo.abc.methodMap.get(mi);
    }

    @Override
    protected void writeMethodInfo(String qualStr, String nameStr, String kindStr, MethodInfo methodInfo, boolean isStatic, boolean isOverride, boolean isFinal) {
        //this.dumpedMethods.add(methodInfo);
        List<String> paramTypeStrings = new Vector();
        Iterator i$ = methodInfo.getParamTypes().iterator();

        while(i$.hasNext()) {
            Name paramTypeName = (Name)i$.next();
            paramTypeStrings.add(ABCDumpUtils.nameToString(paramTypeName));
        }

        String staticStr = isStatic ? "static " : "";
        String overrideStr = isOverride ? "override " : "";
        String nativeStr = methodInfo.isNative() ? "native " : "";
        String finalStr = isFinal ? "final " : "";
        if (nameStr == "") {
            nameStr = methodInfo.getMethodName();
        }

        this.printer.println(qualStr + staticStr + nativeStr + finalStr + overrideStr + kindStr + " " + nameStr + "(" + StringUtils.joinOn(",", paramTypeStrings) + "):" + ABCDumpUtils.nameToString(methodInfo.getReturnType()));
        MethodBodyInfo mb = this.getMethodBodyForMethodInfo(methodInfo);
        if (mb != null) {
            this.printer.println("{");
            //this.printer.indent();
            TablePrinter tablePrinter = new TablePrinter(3, 2);
            tablePrinter.addRow(new String[]{"//", "derivedName", methodInfo.getMethodName()});
            //tablePrinter.addRow(new String[]{"//", "method_info", String.valueOf(this.getMethodInfos().getId(mb.getMethodInfo()))});
            tablePrinter.addRow(new String[]{"//", "max_stack", String.valueOf(mb.max_stack)});
            tablePrinter.addRow(new String[]{"//", "max_regs", String.valueOf(mb.max_local)});
            tablePrinter.addRow(new String[]{"//", "scope_depth", String.valueOf(mb.initial_scope)});
            tablePrinter.addRow(new String[]{"//", "max_scope", String.valueOf(mb.max_scope)});
            tablePrinter.addRow(new String[]{"//", "code_length", String.valueOf(mb.code_len)});
            tablePrinter.print(this.printer);
            if (mb.getTraits() != null && mb.getTraits().getTraitCount() > 0) {
                this.printer.println("activation_traits {");
                //this.printer.indent();

                Trait trait;
                for(Iterator i$4 = mb.getTraits().iterator(); i$.hasNext(); this.writeSlotTrait(kindStr, trait, false)) {
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

                //this.printer.unindent();
                this.printer.println("}");
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
                this.printer.println((String)blockNames.get(block));
                //this.printer.indent();
                Collection<? extends IBasicBlock> succs = block.getSuccessors();
                List<String> succNames = new ArrayList();
                Iterator i$2 = succs.iterator();

                while(i$2.hasNext()) {
                    IBasicBlock s = (IBasicBlock)i$2.next();
                    succNames.add(blockNames.get(s));
                }

                this.printer.println("succs=[" + StringUtils.joinOn(",", succNames) + "]");
                tablePrinter = new TablePrinter(4, 2);

                for(int j = 0; j < block.size(); ++j) {
                    Instruction inst = block.get(j);
                    String constantStr = "";
                    if (inst.hasOperands() && inst.getOperand(0) instanceof Name) {
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

                    tablePrinter.addRow(new String[]{offset + "    ", Instruction.decodeOp(inst.getOpcode()), constantStr, inst.isImmediate() ? String.valueOf(inst.getImmediate()) : ""});
                    ++offset;
                }

                tablePrinter.print(this.printer);
                //this.printer.unindent();
            }

            //this.printer.unindent();
            this.printer.println("}");
            if (mb.getExceptions().size() > 0) {
                tablePrinter = new TablePrinter(7, 2);
                tablePrinter.addRow(new String[]{"//", "exception", "start", "end", "target", "type string", "name string"});

                for(i = 0; i < mb.getExceptions().size(); ++i) {
                    ExceptionInfo exception = (ExceptionInfo)mb.getExceptions().get(i);
                    tablePrinter.addRow(new String[]{"//", String.valueOf(i), String.valueOf(exception.getFrom().getPosition()), String.valueOf(exception.getTo().getPosition()), String.valueOf(exception.getTarget().getPosition()), ABCDumpUtils.nameToString(exception.getExceptionType()), ABCDumpUtils.nameToString(exception.getCatchVar())});
                }

                tablePrinter.print(this.printer);
                this.printer.println("");
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

        public void addRow(String[] r) {
            if (r.length != this.cols) {
                throw new Error("Invalid row");
            } else {
                this.m_rows.add(new Row(r));
            }
        }

        public void print(PrintWriter p) {
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

            public Row(String[] cells) {
                this.cells = cells;
            }

            public void measure(int[] colWidths, int minPadding) {
                for(int i = 0; i < this.cells.length; ++i) {
                    colWidths[i] = Math.max(colWidths[i], this.getRowItemStr(i).length() + minPadding);
                }

            }

            public void print(PrintWriter p, int[] colWidths) {
                String rowStr = "";

                for(int i = 0; i < this.cells.length; ++i) {
                    rowStr = rowStr + this.padString(this.getRowItemStr(i), colWidths[i]);
                }

                p.println(rowStr);
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
        this.printer.println("");
        String qual = ABCDumpUtils.nsQualifierForName(t.getName());
        String nameStr = ABCDumpUtils.nameToString(t.getName());
        Object value = null;
        if (t.hasAttr("value")) {
            value = t.getAttr("value");
        }

        String valueStr = "";
        if (value instanceof String) {
            valueStr = " = \"" + value + "\"";
        } else if (value instanceof Namespace) {
            valueStr = " = " + ((Namespace)value).getName();
        } else if (value == ABCConstants.NULL_VALUE) {
            valueStr = " = null";
        } else if (value == ABCConstants.UNDEFINED_VALUE) {
            valueStr = "";
        } else if (value != null) {
            valueStr = " = " + value.toString();
        }

        String staticStr = isStatic ? "static " : "";
        this.writeMetaData(t);
        this.printer.println(qual + staticStr + kindStr + " " + nameStr + ":" + ABCDumpUtils.nameToString((Name)t.getAttr("type")) + valueStr);
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

                this.printer.println("[" + mid.getName() + "(" + sb.toString() + ")]" + " // metadata_id=" + mid);
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
