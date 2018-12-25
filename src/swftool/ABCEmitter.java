package swftool;

import com.adobe.flash.abc.ABCConstants;
import com.adobe.flash.abc.ClassDependencySort;
import com.adobe.flash.abc.EntryOrderedStore;
import com.adobe.flash.abc.Pool;
import com.adobe.flash.abc.graph.IBasicBlock;
import com.adobe.flash.abc.graph.IFlowgraph;
import com.adobe.flash.abc.instructionlist.InstructionList;
import com.adobe.flash.abc.semantics.ClassInfo;
import com.adobe.flash.abc.semantics.ExceptionInfo;
import com.adobe.flash.abc.semantics.Float4;
import com.adobe.flash.abc.semantics.InstanceInfo;
import com.adobe.flash.abc.semantics.Instruction;
import com.adobe.flash.abc.semantics.Label;
import com.adobe.flash.abc.semantics.Metadata;
import com.adobe.flash.abc.semantics.MethodBodyInfo;
import com.adobe.flash.abc.semantics.MethodInfo;
import com.adobe.flash.abc.semantics.Name;
import com.adobe.flash.abc.semantics.Namespace;
import com.adobe.flash.abc.semantics.Nsset;
import com.adobe.flash.abc.semantics.PooledValue;
import com.adobe.flash.abc.semantics.ScriptInfo;
import com.adobe.flash.abc.semantics.Trait;
import com.adobe.flash.abc.semantics.Traits;
import com.adobe.flash.abc.visitors.IABCVisitor;
import com.adobe.flash.abc.visitors.IClassVisitor;
import com.adobe.flash.abc.visitors.IDiagnosticsVisitor;
import com.adobe.flash.abc.visitors.IMetadataVisitor;
import com.adobe.flash.abc.visitors.IMethodBodyVisitor;
import com.adobe.flash.abc.visitors.IMethodVisitor;
import com.adobe.flash.abc.visitors.IScriptVisitor;
import com.adobe.flash.abc.visitors.ITraitVisitor;
import com.adobe.flash.abc.visitors.ITraitsVisitor;
import com.adobe.flash.abc.visitors.NilVisitors;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class ABCEmitter
        implements IABCVisitor
{
    private static final int VERSION_NONE = -1;
    private static final int SIZEOF_S24 = 3;
    private ABCWriter w;

    public ABCEmitter()
    {
        this(NilVisitors.NIL_DIAGNOSTICS_VISITOR);
    }

    public ABCEmitter(IDiagnosticsVisitor diagnosticsVisitor)
    {
        this.w = new ABCWriter();
        this.lock = new ReentrantLock();
        this.visitEndCalled = false;
        this.diagnosticsVisitor = diagnosticsVisitor;
    }

    public Pool<Name> namePool = new Pool(Pool.DefaultType.HasDefaultZero);
    public Pool<String> stringPool = new Pool(Pool.DefaultType.HasDefaultZero);
    public Pool<Integer> intPool = new Pool(Pool.DefaultType.HasDefaultZero);
    public Pool<Long> uintPool = new Pool(Pool.DefaultType.HasDefaultZero);
    public Pool<Double> doublePool = new Pool(Pool.DefaultType.HasDefaultZero);
    public Pool<Float> floatPool = new Pool(Pool.DefaultType.HasDefaultZero);
    public Pool<Float4> float4Pool = new Pool(Pool.DefaultType.HasDefaultZero);
    public Pool<Namespace> nsPool = new Pool(Pool.DefaultType.HasDefaultZero);
    public Pool<Nsset> nssetPool = new Pool(Pool.DefaultType.HasDefaultZero);
    public Pool<Metadata> metadataPool = new Pool(Pool.DefaultType.NoDefaultZero);
    public Collection<EmitterClassVisitor> definedClasses = new ArrayList();
    public final EntryOrderedStore<MethodInfo> methodInfos = new EntryOrderedStore();
    public final ReadWriteLock methodInfosLock = new ReentrantReadWriteLock();
    public final Vector<MethodBodyInfo> methodBodies = new Vector();
    public final Vector<ScriptInfo> scriptInfos = new Vector();
    public int versionABCMajor = -1;
    public int versionABCMinor = -1;
    private boolean allowBadJumps = false;
    private boolean eagerlyEmitMethodBodies = true;
    private final ReentrantLock lock;
    private boolean visitEndCalled;
    private final IDiagnosticsVisitor diagnosticsVisitor;

    public byte[] emit()
            throws Exception
    {
        if ((getMajorVersion() == -1) || (getMinorVersion() == -1)) {
            throw new IllegalStateException("No abc version specified");
        }
        this.definedClasses = ClassDependencySort.getSorted(this.definedClasses);

        this.w.writeU16(getMinorVersion());
        this.w.writeU16(getMajorVersion());

        this.w.writeU30(this.intPool.getNominalSize());
        for (Iterator i$ = this.intPool.getValues().iterator(); i$.hasNext();)
        {
            int x = ((Integer)i$.next()).intValue();

            this.w.writeU30(x);
        }
        this.w.writeU30(this.uintPool.getNominalSize());
        for (Iterator i$ = this.uintPool.getValues().iterator(); i$.hasNext();)
        {
            long x = ((Long)i$.next()).longValue();

            this.w.writeU30((int)x);
        }
        this.w.writeU30(this.doublePool.getNominalSize());
        for (Iterator i$ = this.doublePool.getValues().iterator(); i$.hasNext();)
        {
            double x = ((Double)i$.next()).doubleValue();

            this.w.write64(Double.doubleToLongBits(x));
        }
        if (hasFloat())
        {
            this.w.writeU30(this.floatPool.getNominalSize());
            for (Iterator i$ = this.floatPool.getValues().iterator(); i$.hasNext();)
            {
                float x = ((Float)i$.next()).floatValue();

                this.w.writeFloat(x);
            }
            this.w.writeU30(this.float4Pool.getNominalSize());
            for (Float4 x : this.float4Pool.getValues()) {
                for (float f : x.getValues()) {
                    this.w.writeFloat(f);
                }
            }
        }
        this.w.writeU30(this.stringPool.getNominalSize());
        for (String s : this.stringPool.getValues())
        {
            byte[] stringBytes = s.getBytes("UTF-8");
            this.w.writeU30(stringBytes.length);
            this.w.write(stringBytes);
        }
        this.w.writeU30(this.nsPool.getNominalSize());
        for (Namespace ns : this.nsPool.getValues()) {
            emitNamespace(ns);
        }
        this.w.writeU30(this.nssetPool.getNominalSize());
        for (Nsset nsset : this.nssetPool.getValues())
        {
            this.w.writeU30(nsset.length());
            for (Namespace ns : nsset) {
                this.w.writeU30(this.nsPool.id(ns));
            }
        }
        this.w.writeU30(this.namePool.getNominalSize());
        for (Name n : this.namePool.getValues())
        {
            this.w.write(n.getKind());
            switch (n.getKind())
            {
                case 7:
                case 13:
                    this.w.writeU30(this.nsPool.id(n.getSingleQualifier()));
                    this.w.writeU30(this.stringPool.id(n.getBaseName()));
                    break;
                case 9:
                case 14:
                    this.w.writeU30(this.stringPool.id(n.getBaseName()));
                    this.w.writeU30(this.nssetPool.id(n.getQualifiers()));
                    break;
                case 15:
                case 16:
                    this.w.writeU30(this.stringPool.id(n.getBaseName()));
                    break;
                case 27:
                case 28:
                    this.w.writeU30(this.nssetPool.id(n.getQualifiers()));
                    break;
                case 17:
                case 18:
                    break;
                case 29:
                    this.w.writeU30(this.namePool.id(n.getTypeNameBase()));
                    this.w.writeU30(1);
                    this.w.writeU30(this.namePool.id(n.getTypeNameParameter()));
                    break;
                case 8:
                case 10:
                case 11:
                case 12:
                case 19:
                case 20:
                case 21:
                case 22:
                case 23:
                case 24:
                case 25:
                case 26:
                default:
                    //if (!$assertionsDisabled) {
                        throw new AssertionError("Unimplemented name kind " + n.getKind());
                    //}
                   // throw new IllegalArgumentException("Not implemented.");
            }
        }
        Lock methodInfosReadLock = this.methodInfosLock.readLock();
        methodInfosReadLock.lock();
        try
        {
            this.w.writeU30(this.methodInfos.size());
            for (MethodInfo mi : this.methodInfos) {
                emitMethodInfo(mi);
            }
        }
        finally
        {
            methodInfosReadLock.unlock();
        }
        this.w.writeU30(this.metadataPool.getNominalSize());
        for (Metadata md : this.metadataPool.getValues())
        {
            this.w.writeU30(this.stringPool.id(md.getName()));

            assert (md.getKeys().length == md.getValues().length);
            this.w.writeU30(md.getKeys().length);
            for (String key : md.getKeys())
            {
                int string_index = this.stringPool.id(key);
                this.w.writeU30(string_index);
            }
            for (String value : md.getValues())
            {
                int string_index = this.stringPool.id(value);
                this.w.writeU30(string_index);
            }
        }
        this.w.writeU30(this.definedClasses.size());
        for (EmitterClassVisitor clz : this.definedClasses)
        {
            InstanceInfo ii = clz.instanceInfo;

            this.w.writeU30(this.namePool.id(ii.name));
            this.w.writeU30(this.namePool.id(ii.superName));
            this.w.write(ii.flags);
            if (ii.hasProtectedNs()) {
                this.w.writeU30(this.nsPool.id(ii.protectedNs));
            }
            this.w.writeU30(ii.interfaceNames.length);
            for (Name i : ii.interfaceNames) {
                this.w.writeU30(this.namePool.id(i));
            }
            this.w.writeU30(getMethodId(ii.iInit));

            emitTraits(clz.instanceTraits);
        }
        for (EmitterClassVisitor clz : this.definedClasses)
        {
            this.w.writeU30(getMethodId(clz.classInfo.cInit));
            emitTraits(clz.classTraits);
        }
        this.w.writeU30(this.scriptInfos.size());
        for (ScriptInfo s : this.scriptInfos) {
            emitScriptInfo(s);
        }
        this.w.writeU30(this.methodBodies.size());
        for (MethodBodyInfo mb : this.methodBodies) {
            emitMethodBody(mb);
        }
        return this.w.getDirectByteArray();
    }

    private int getMajorVersion()
    {
        return this.versionABCMajor;
    }

    private int getMinorVersion()
    {
        return this.versionABCMinor;
    }

    private boolean hasFloat()
    {
        return (getMajorVersion() >= 47) && (getMinorVersion() >= 16);
    }

    private void emitTraits(Traits traits)
    {
        this.w.writeU30(traits.getTraitCount());
        for (Trait t : traits)
        {
            this.w.writeU30(this.namePool.id(t.getNameAttr("name")));

            this.w.write(t.getFullKindByte());
            switch (t.getKind())
            {
                case 0:
                case 6:
                    this.w.writeU30(t.getIntAttr("slot_id"));
                    this.w.writeU30(this.namePool.id(t.getNameAttr("type")));

                    Object trait_value = t.getAttr("value");
                    if (trait_value != null)
                    {
                        if ((trait_value instanceof String))
                        {
                            this.w.writeU30(this.stringPool.id((String)trait_value));
                            this.w.write(1);
                        }
                        else if ((trait_value instanceof Namespace))
                        {
                            this.w.writeU30(this.nsPool.id((Namespace)trait_value));
                            this.w.write(8);
                        }
                        else if ((trait_value instanceof Double))
                        {
                            this.w.writeU30(this.doublePool.id((Double)trait_value));
                            this.w.write(6);
                        }
                        else if ((trait_value instanceof Integer))
                        {
                            this.w.writeU30(this.intPool.id((Integer)trait_value));
                            this.w.write(3);
                        }
                        else if ((trait_value instanceof Long))
                        {
                            this.w.writeU30(this.uintPool.id((Long)trait_value));
                            this.w.write(4);
                        }
                        else if ((trait_value instanceof Float))
                        {
                            this.w.writeU30(this.floatPool.id((Float)trait_value));
                            this.w.write(2);
                        }
                        else if (trait_value.equals(Boolean.TRUE))
                        {
                            this.w.writeU30(11);
                            this.w.write(11);
                        }
                        else if (trait_value.equals(Boolean.FALSE))
                        {
                            this.w.writeU30(10);
                            this.w.write(10);
                        }
                        else if (trait_value == ABCConstants.NULL_VALUE)
                        {
                            this.w.writeU30(12);
                            this.w.write(12);
                        }
                        else if (trait_value == ABCConstants.UNDEFINED_VALUE)
                        {
                            this.w.writeU30(0);
                        }
                        else
                        {
                            throw new IllegalStateException("Unrecognized initializer type: " + trait_value.getClass().toString());
                        }
                    }
                    else {
                        this.w.writeU30(0);
                    }
                    break;
                case 1:
                case 2:
                case 3:
                case 5:
                    if (t.hasAttr("disp_id")) {
                        this.w.writeU30(t.getIntAttr("disp_id"));
                    } else {
                        this.w.writeU30(0);
                    }
                    this.w.writeU30(getMethodId((MethodInfo)t.getAttr("method_id")));
                    break;
                case 4:
                    if (t.hasAttr("slot_id")) {
                        this.w.writeU30(t.getIntAttr("slot_id"));
                    } else {
                        this.w.writeU30(0);
                    }
                    this.w.writeU30(getClassId((ClassInfo)t.getAttr("class_id")));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown trait kind " + t.getKind());
            }
            if (t.hasMetadata())
            {
                Vector<Metadata> metadata = t.getMetadata();
                this.w.writeU30(metadata.size());
                for (Metadata m : metadata) {
                    this.w.writeU30(this.metadataPool.id(m));
                }
            }
        }
    }

    private int getMethodId(MethodInfo info)
    {
        Lock methodInfosReadLock = this.methodInfosLock.readLock();
        methodInfosReadLock.lock();
        try
        {
            return this.methodInfos.getId(info);
        }
        finally
        {
            methodInfosReadLock.unlock();
        }
    }

    private int getClassId(ClassInfo info)
    {
        int id_index = 0;
        for (EmitterClassVisitor candidate : this.definedClasses)
        {
            if (candidate.classInfo == info) {
                return id_index;
            }
            id_index++;
        }
        throw new IllegalArgumentException("Unable to find ClassInfo index for " + info);
    }

    private void emitScriptInfo(ScriptInfo info)
    {
        MethodInfo scriptInit = info.getInit();
        int nRequiredArguments = scriptInit.getParamTypes().size() - scriptInit.getDefaultValues().size();
        if (nRequiredArguments > 0) {
            this.diagnosticsVisitor.scriptInitWithRequiredArguments(info, scriptInit);
        }
        this.w.writeU30(getMethodId(scriptInit));
        emitTraits(info.getTraits());
    }

    private void emitMethodInfo(MethodInfo info)
    {
        Collection<Name> paramTypes = info.getParamTypes();
        int nParamTypes = paramTypes.size();
        this.w.writeU30(nParamTypes);
        this.w.writeU30(this.namePool.id(info.getReturnType()));
        for (Name n : paramTypes) {
            this.w.writeU30(this.namePool.id(n));
        }
        this.w.writeU30(this.stringPool.id(info.getMethodName()));
        this.w.write(info.getFlags());
        if (info.hasOptional())
        {
            Collection<PooledValue> defaults = info.getDefaultValues();
            int nDefaults = defaults.size();
            if (nDefaults > nParamTypes) {
                this.diagnosticsVisitor.tooManyDefaultParameters(info);
            }
            this.w.writeU30(nDefaults);
            for (PooledValue v : defaults)
            {
                this.w.writeU30(v.getPoolIndex());
                this.w.write(v.getKind());
            }
        }
        if (info.hasParamNames())
        {
            List<String> paramNames = info.getParamNames();
            int nParamNames = paramNames.size();
            if (nParamTypes != nParamNames) {
                this.diagnosticsVisitor.incorrectNumberOfParameterNames(info);
            }
            for (String param_name : info.getParamNames()) {
                this.w.writeU30(this.stringPool.id(param_name));
            }
        }
    }

    private void emitMethodBody(MethodBodyInfo f)
            throws Exception
    {
        MethodInfo signature = f.getMethodInfo();

        this.w.writeU30(getMethodId(signature));

        this.w.writeU30(f.getMaxStack());

        int max_local = f.getLocalCount();
        int param_count = signature.getParamCount();
        if (signature.needsRest()) {
            param_count++;
        }
        if (param_count > max_local) {
            max_local = param_count;
        }
        this.w.writeU30(max_local);

        this.w.writeU30(f.getInitScopeDepth());
        this.w.writeU30(f.getMaxScopeDepth());
        if (!f.hasBytecode()) {
            emitCode(f);
        }
        this.w.write(f.getBytecode());

        emitTraits(f.getTraits());
    }

    private void emitCode(MethodBodyInfo f)
            throws Exception
    {
        ABCWriter result = new ABCWriter();
        Map<IBasicBlock, ABCWriter> writers = new HashMap();

        Map<IBasicBlock, Integer> block_offsets = new HashMap();
        int code_len = 0;
        for (IBasicBlock b : f.getCfg().getBlocksInEntryOrder())
        {
            block_offsets.put(b, Integer.valueOf(code_len));
            ABCWriter blockWriter = new ABCWriter();
            writers.put(b, blockWriter);

            emitBlock(b, blockWriter);

            code_len += blockWriter.size();
            if (b.size() != 0)
            {
                Instruction last = b.get(b.size() - 1);
                if (last.isBranch()) {
                    if (last.getOpcode() == 27)
                    {
                        int switch_size = 1 + sizeOfU30(last.getOperandCount()) + 3 * last.getOperandCount();
                        code_len += switch_size;
                    }
                    else
                    {
                        assert (null != last.getTarget());

                        code_len += 4;
                    }
                }
            }
        }
        result.writeU30(code_len);
        int code_start = result.size();
        for (IBasicBlock b : f.getCfg().getBlocksInEntryOrder())
        {
            ((ABCWriter)writers.get(b)).writeTo(result);
            if (b.size() > 0)
            {
                Instruction last = b.get(b.size() - 1);
                if (last.isBranch()) {
                    if (27 == last.getOpcode())
                    {
                        emitLookupswitch(result, code_start, f, last, block_offsets);
                    }
                    else
                    {
                        assert (last.getTarget() != null);

                        emitBranch(result, last.getOpcode(), f.getBlock(last.getTarget(), !this.allowBadJumps), code_start, block_offsets, code_len);
                    }
                }
            }
        }
        emitExceptionInfo(f, result, block_offsets);

        f.setBytecode(result.getDirectByteArray());
    }

    private void emitBranch(ABCWriter writer, int opcode, IBasicBlock target, int code_start, Map<IBasicBlock, Integer> block_offsets, int code_len)
    {
        writer.write(opcode);

        int from = writer.size() + 3;

        int to = code_start + (target != null ? ((Integer)block_offsets.get(target)).intValue() : code_len + 1);

        writer.writeS24(to - from);
    }

    void emitBlock(IBasicBlock b, ABCWriter blockWriter)
    {
        for (int i = 0; (i < b.size()) && (!b.get(i).isBranch()); i++)
        {
            Instruction insn = b.get(i);

            blockWriter.write(insn.getOpcode());
            switch (insn.getOpcode())
            {
                case 50:
                    blockWriter.writeU30(((Integer)insn.getOperand(0)).intValue());
                    blockWriter.writeU30(((Integer)insn.getOperand(1)).intValue());
                    break;
                case 4:
                case 5:
                case 89:
                case 93:
                case 94:
                case 95:
                case 96:
                case 97:
                case 102:
                case 104:
                case 106:
                case 128:
                case 134:
                case 178:
                    blockWriter.writeU30(this.namePool.id((Name)insn.getOperand(0)));
                    break;
                case 69:
                case 70:
                case 74:
                case 76:
                case 78:
                case 79:
                    blockWriter.writeU30(this.namePool.id((Name)insn.getOperand(0)));
                    blockWriter.writeU30(((Integer)insn.getOperand(1)).intValue());
                    break;
                case 8:
                case 37:
                case 65:
                case 66:
                case 73:
                case 83:
                case 85:
                case 86:
                case 90:
                case 98:
                case 99:
                case 101:
                case 108:
                case 109:
                case 110:
                case 111:
                case 146:
                case 148:
                case 194:
                case 195:
                    blockWriter.writeU30(insn.getImmediate());
                    break;
                case 36:
                    blockWriter.write(insn.getImmediate());
                    break;
                case 88:
                    blockWriter.writeU30(getClassId((ClassInfo)insn.getOperand(0)));
                    break;
                case 64:
                    blockWriter.writeU30(getMethodId((MethodInfo)insn.getOperand(0)));
                    break;
                case 68:
                    blockWriter.writeU30(getMethodId((MethodInfo)insn.getOperand(0)));
                    blockWriter.writeU30(((Integer)insn.getOperand(1)).intValue());
                    break;
                case 6:
                case 44:
                case 241:
                    blockWriter.writeU30(this.stringPool.id(insn.getOperand(0).toString()));
                    break;
                case 49:
                    blockWriter.writeU30(this.nsPool.id((Namespace)insn.getOperand(0)));
                    break;
                case 45:
                    blockWriter.writeU30(this.intPool.id((Integer)insn.getOperand(0)));
                    break;
                case 46:
                    blockWriter.writeU30(this.uintPool.id((Long)insn.getOperand(0)));
                    break;
                case 47:
                    blockWriter.writeU30(this.doublePool.id((Double)insn.getOperand(0)));
                    break;
                case 34:
                    blockWriter.writeU30(this.floatPool.id((Float)insn.getOperand(0)));
                    break;
                case 84:
                    blockWriter.writeU30(this.float4Pool.id((Float4)insn.getOperand(0)));
                    break;
                case 240:
                case 242:
                    blockWriter.writeU30(insn.getImmediate());
                    break;
                case 239:
                    blockWriter.write(((Integer)insn.getOperand(0)).intValue());
                    blockWriter.writeU30(this.stringPool.id(insn.getOperand(1).toString()));
                    blockWriter.write(((Integer)insn.getOperand(2)).intValue());
                    blockWriter.writeU30(0);
            }
        }
    }

    private void emitNamespace(Namespace ns)
    {
        this.w.write(ns.getKind());
        this.w.writeU30(this.stringPool.id(ns.getVersionedName()));
    }

    private void emitExceptionInfo(MethodBodyInfo f, ABCWriter w, Map<IBasicBlock, Integer> pos)
    {
        w.writeU30(f.getExceptions().size());
        for (ExceptionInfo ex : f.getExceptions()) {
            if (ex.isLive())
            {
                w.writeU30(((Integer)pos.get(f.getBlock(ex.getFrom()))).intValue());
                w.writeU30(((Integer)pos.get(f.getBlock(ex.getTo()))).intValue());
                w.writeU30(((Integer)pos.get(f.getBlock(ex.getTarget()))).intValue());
                w.writeU30(this.namePool.id(ex.getExceptionType()));
                w.writeU30(this.namePool.id(ex.getCatchVar()));
            }
        }
    }

    void emitLookupswitch(ABCWriter out, int code_start, MethodBodyInfo f, Instruction switch_insn, Map<IBasicBlock, Integer> block_offsets)
    {
        int case_size = switch_insn.getOperandCount() - 1;

        int base_loc = out.size() - code_start;

        out.write(27);

        Label default_case = (Label)switch_insn.getOperand(case_size);
        int default_offset = ((Integer)block_offsets.get(f.getBlock(default_case))).intValue() - base_loc;
        out.writeS24(default_offset);

        out.writeU30(case_size - 1);
        for (int i = 0; i < case_size; i++)
        {
            int branch_offset = ((Integer)block_offsets.get(f.getBlock((Label)switch_insn.getOperand(i)))).intValue() - base_loc;
            out.writeS24(branch_offset);
        }
    }

    static class ABCWriter
            extends ByteArrayOutputStream
    {
        void rewind(int n)
        {
            this.count -= n;
        }

        void writeU16(int i)
        {
            write(i);
            write(i >> 8);
        }

        void writeS24(int i)
        {
            writeU16(i);
            write(i >> 16);
        }

        void write64(long i)
        {
            writeS24((int)i);
            writeS24((int)(i >> 24));
            writeU16((int)(i >> 48));
        }

        void writeU30(int v)
        {
            if ((v < 128) && (v >= 0))
            {
                write(v);
            }
            else if ((v < 16384) && (v >= 0))
            {
                write(v & 0x7F | 0x80);
                write(v >> 7);
            }
            else if ((v < 2097152) && (v >= 0))
            {
                write(v & 0x7F | 0x80);
                write(v >> 7 | 0x80);
                write(v >> 14);
            }
            else if ((v < 268435456) && (v >= 0))
            {
                write(v & 0x7F | 0x80);
                write(v >> 7 | 0x80);
                write(v >> 14 | 0x80);
                write(v >> 21);
            }
            else
            {
                write(v & 0x7F | 0x80);
                write(v >> 7 | 0x80);
                write(v >> 14 | 0x80);
                write(v >> 21 | 0x80);
                write(v >>> 28);
            }
        }

        int sizeOfU30(int v)
        {
            if ((v < 128) && (v >= 0)) {
                return 1;
            }
            if ((v < 16384) && (v >= 0)) {
                return 2;
            }
            if ((v < 2097152) && (v >= 0)) {
                return 3;
            }
            if ((v < 268435456) && (v >= 0)) {
                return 4;
            }
            return 5;
        }

        void writeFloat(float f)
        {
            int bits = Float.floatToIntBits(f);
            write((byte)bits);
            write((byte)(bits >> 8));
            write((byte)(bits >> 16));
            write((byte)(bits >> 24));
        }

        public byte[] getDirectByteArray()
        {
            if (this.buf.length == this.count) {
                return this.buf;
            }
            return super.toByteArray();
        }

        public byte[] toByteArray()
        {
            throw new UnsupportedOperationException();
        }
    }

    void poolOperands(MethodBodyInfo mbi)
    {
        for (IBasicBlock b : mbi.getCfg().getBlocksInEntryOrder()) {
            for (Instruction insn : b.getInstructions()) {
                switch (insn.getOpcode())
                {
                    case 4:
                    case 5:
                    case 89:
                    case 93:
                    case 94:
                    case 95:
                    case 96:
                    case 97:
                    case 102:
                    case 104:
                    case 106:
                    case 128:
                    case 134:
                    case 178:
                        visitPooledName((Name)insn.getOperand(0));
                        break;
                    case 69:
                    case 70:
                    case 74:
                    case 76:
                    case 78:
                    case 79:
                        visitPooledName((Name)insn.getOperand(0));
                        break;
                    case 6:
                    case 44:
                    case 241:
                        this.stringPool.add(insn.getOperand(0).toString());
                        break;
                    case 49:
                        visitPooledNamespace((Namespace)insn.getOperand(0));
                        break;
                    case 45:
                        this.intPool.add((Integer)insn.getOperand(0));
                        break;
                    case 46:
                        this.uintPool.add((Long)insn.getOperand(0));
                        break;
                    case 47:
                        this.doublePool.add((Double)insn.getOperand(0));
                        break;
                    case 34:
                        this.floatPool.add((Float)insn.getOperand(0));
                        break;
                    case 84:
                        this.float4Pool.add((Float4)insn.getOperand(0));
                        break;
                    case 239:
                        this.stringPool.add(insn.getOperand(1).toString());
                }
            }
        }
    }

    private void poolTraitsConstants(Traits ts)
    {
        for (Trait t : ts)
        {
            Name traitName = t.getNameAttr("name");
            visitPooledName(traitName);
            if (t.hasAttr("type")) {
                visitPooledName(t.getNameAttr("type"));
            }
            for (Metadata md : t.getMetadata()) {
                visitPooledMetadata(md);
            }
            if (t.hasAttr("value"))
            {
                Object trait_value = t.getAttr("value");
                if (trait_value != null) {
                    if ((trait_value instanceof String)) {
                        visitPooledString((String)trait_value);
                    } else if ((trait_value instanceof Namespace)) {
                        visitPooledNamespace((Namespace)trait_value);
                    } else if ((trait_value instanceof Double)) {
                        visitPooledDouble((Double)trait_value);
                    } else if ((trait_value instanceof Integer)) {
                        visitPooledInt((Integer)trait_value);
                    } else if ((trait_value instanceof Long)) {
                        visitPooledUInt((Long)trait_value);
                    } else if ((trait_value instanceof Float)) {
                        visitPooledFloat((Float)trait_value);
                    } else if ((!trait_value.equals(ABCConstants.UNDEFINED_VALUE)) && (!trait_value.equals(ABCConstants.NULL_VALUE)) && (!trait_value.equals(Boolean.TRUE)) && (!trait_value.equals(Boolean.FALSE))) {
                        throw new IllegalStateException("Unrecognized initializer type: " + trait_value.getClass().toString());
                    }
                }
            }
        }
    }

    public void visit(int majorVersion, int minorVersion)
    {
        verifyEmitterStatus();

        this.lock.lock();

        assert (this.lock.getHoldCount() == 1) : "The hold count should be 1, beacuse this method should only be called once!";
        if (this.versionABCMajor == -1)
        {
            this.versionABCMajor = majorVersion;
            this.versionABCMinor = minorVersion;
        }
        else if ((this.versionABCMajor != majorVersion) || (this.versionABCMinor != minorVersion))
        {
            throw new IllegalArgumentException("abc versions do not match");
        }
    }

    public void visitEnd()
    {
        verifyEmitterStatus();
        assertLockHeld();

        this.visitEndCalled = true;
    }

    public IClassVisitor visitClass(InstanceInfo iinfo, ClassInfo cinfo)
    {
        verifyEmitterStatus();

        EmitterClassVisitor result = new EmitterClassVisitor(iinfo, cinfo);
        result.visit();
        return result;
    }

    public IScriptVisitor visitScript()
    {
        verifyEmitterStatus();

        return new EmitterScriptInfo();
    }

    public IMethodVisitor visitMethod(MethodInfo minfo)
    {
        verifyEmitterStatus();

        return new EmitterMethodInfoVisitor(minfo);
    }

    public void visitPooledDouble(Double d)
    {
        verifyEmitterStatus();

        assertLockHeld();

        this.doublePool.add(d);
    }

    public void visitPooledInt(Integer i)
    {
        verifyEmitterStatus();

        assertLockHeld();

        this.intPool.add(i);
    }

    public void visitPooledFloat(Float f)
    {
        verifyEmitterStatus();

        assertLockHeld();

        this.floatPool.add(f);
    }

    public void visitPooledFloat4(Float4 f4)
    {
        verifyEmitterStatus();

        assertLockHeld();

        this.float4Pool.add(f4);
    }

    public void visitPooledMetadata(Metadata md)
    {
        verifyEmitterStatus();

        assertLockHeld();

        this.metadataPool.add(md);

        visitPooledString(md.getName());
        for (String key : md.getKeys()) {
            visitPooledString(key);
        }
        for (String value : md.getValues()) {
            visitPooledString(value);
        }
    }

    public void visitPooledName(Name n)
    {
        verifyEmitterStatus();

        assertLockHeld();

        this.namePool.add(n);
        if (null == n) {
            return;
        }
        int kind = n.getKind();
        if (kind != 29)
        {
            visitPooledString(n.getBaseName());
            if ((kind == 7) || (kind == 13)) {
                visitPooledNamespace(n.getSingleQualifier());
            } else {
                visitPooledNsSet(n.getQualifiers());
            }
        }
        else
        {
            visitPooledName(n.getTypeNameBase());
            visitPooledName(n.getTypeNameParameter());
        }
    }

    public void visitPooledNamespace(Namespace ns)
    {
        verifyEmitterStatus();

        assertLockHeld();

        this.nsPool.add(ns);
        if (ns != null) {
            visitPooledString(ns.getVersionedName());
        }
    }

    public void visitPooledNsSet(Nsset nss)
    {
        verifyEmitterStatus();

        assertLockHeld();

        this.nssetPool.add(nss);
        if (nss != null) {
            for (Namespace ns : nss) {
                visitPooledNamespace(ns);
            }
        }
    }

    public void visitPooledString(String s)
    {
        verifyEmitterStatus();

        assertLockHeld();

        this.stringPool.add(s);
    }

    public void visitPooledUInt(Long l)
    {
        verifyEmitterStatus();

        assertLockHeld();

        this.uintPool.add(l);
    }

    public static int sizeOfU30(int v)
    {
        if ((v < 128) && (v >= 0)) {
            return 1;
        }
        if ((v < 16384) && (v >= 0)) {
            return 2;
        }
        if ((v < 2097152) && (v >= 0)) {
            return 3;
        }
        if ((v < 268435456) && (v >= 0)) {
            return 4;
        }
        return 5;
    }

    public class EmitterClassVisitor
            implements IClassVisitor, ClassDependencySort.IInstanceInfoProvider
    {
        ClassInfo classInfo;
        Traits classTraits;
        InstanceInfo instanceInfo;
        Traits instanceTraits;

        EmitterClassVisitor(InstanceInfo iinfo, ClassInfo cinfo)
        {
            this.classInfo = cinfo;
            if (null == cinfo.classTraits) {
                cinfo.classTraits = new Traits();
            }
            this.classTraits = cinfo.classTraits;

            this.instanceInfo = iinfo;
            if (null == iinfo.traits) {
                iinfo.traits = new Traits();
            }
            this.instanceTraits = iinfo.traits;
            if (null == iinfo.interfaceNames) {
                iinfo.interfaceNames = new Name[0];
            }
        }

        public void visit()
        {
            ABCEmitter.this.verifyEmitterStatus();
        }

        public ITraitsVisitor visitClassTraits()
        {
            ABCEmitter.this.verifyEmitterStatus();
            return new ABCEmitter.EmitterTraitsVisitor(this.classTraits);
        }

        public ITraitsVisitor visitInstanceTraits()
        {
            ABCEmitter.this.verifyEmitterStatus();
            return new ABCEmitter.EmitterTraitsVisitor(this.instanceTraits);
        }

        public void visitEnd()
        {
            ABCEmitter.this.verifyEmitterStatus();

            ABCEmitter.this.assertLockHeld();
            ABCEmitter.this.definedClasses.add(this);
            if (null == this.classInfo.cInit)
            {
                this.classInfo.cInit = new MethodInfo();
                MethodBodyInfo m_cinit = new MethodBodyInfo();
                m_cinit.setMethodInfo(this.classInfo.cInit);

                IMethodVisitor mv = ABCEmitter.this.visitMethod(this.classInfo.cInit);
                mv.visit();
                IMethodBodyVisitor mbv = mv.visitBody(m_cinit);
                mbv.visit();
                mbv.visitInstruction(71);
                mbv.visitEnd();
                mv.visitEnd();
            }
            ABCEmitter.this.visitPooledName(this.instanceInfo.name);
            ABCEmitter.this.visitPooledName(this.instanceInfo.superName);
            if (this.instanceInfo.hasProtectedNs()) {
                ABCEmitter.this.visitPooledNamespace(this.instanceInfo.protectedNs);
            }
            if (null == this.instanceInfo.iInit)
            {
                this.instanceInfo.iInit = new MethodInfo();
                MethodBodyInfo iinit = new MethodBodyInfo();
                iinit.setMethodInfo(this.instanceInfo.iInit);

                IMethodVisitor mv = ABCEmitter.this.visitMethod(this.instanceInfo.iInit);
                mv.visit();
                if (0 == (this.instanceInfo.flags & 0x4))
                {
                    IMethodBodyVisitor mbv = mv.visitBody(iinit);
                    mbv.visit();
                    mbv.visitInstruction(208);
                    mbv.visitInstruction(48);
                    mbv.visitInstruction(208);
                    mbv.visitInstruction(73, 0);
                    mbv.visitInstruction(71);
                    mbv.visitEnd();
                }
                mv.visitEnd();
            }
            if (this.instanceInfo.interfaceNames != null) {
                for (Name interface_name : this.instanceInfo.interfaceNames) {
                    ABCEmitter.this.visitPooledName(interface_name);
                }
            }
        }

        public InstanceInfo getInstanceInfo()
        {
            return this.instanceInfo;
        }
    }

    private class EmitterTraitsVisitor
            implements ITraitsVisitor
    {
        Traits traits;

        EmitterTraitsVisitor(Traits traits)
        {
            this.traits = traits;
        }

        public ITraitVisitor visitClassTrait(int kind, Name name, int slot_id, ClassInfo clazz)
        {
            ABCEmitter.this.verifyEmitterStatus();

            Trait t = createTrait(kind, name);
            if (slot_id != 0) {
                t.addAttr("slot_id", Integer.valueOf(slot_id));
            }
            t.addAttr("class_id", clazz);

            return new ABCEmitter.EmitterTraitVisitor( t);
        }

        public ITraitVisitor visitMethodTrait(int kind, Name name, int dispId, MethodInfo method)
        {
            ABCEmitter.this.verifyEmitterStatus();

            Trait t = createTrait(kind, name);
            t.addAttr("method_id", method);
            if (dispId != 0) {
                t.addAttr("disp_id", Integer.valueOf(dispId));
            }
            return new ABCEmitter.EmitterTraitVisitor( t);
        }

        public ITraitVisitor visitSlotTrait(int kind, Name name, int slotId, Name slotType, Object slotValue)
        {
            ABCEmitter.this.verifyEmitterStatus();

            Trait t = createTrait(kind, name);
            t.addAttr("slot_id", Integer.valueOf(slotId));
            t.addAttr("type", slotType);
            t.addAttr("value", slotValue);

            return new ABCEmitter.EmitterTraitVisitor( t);
        }

        public void visit()
        {
            ABCEmitter.this.verifyEmitterStatus();
        }

        public void visitEnd()
        {
            ABCEmitter.this.verifyEmitterStatus();

            ABCEmitter.this.assertLockHeld();
            ABCEmitter.this.poolTraitsConstants(this.traits);
        }

        public Traits getTraits()
        {
            return this.traits;
        }

        private Trait createTrait(int kind, Name name)
        {
            ABCEmitter.this.verifyEmitterStatus();

            Trait t = new Trait(kind, name);
            this.traits.add(t);
            return t;
        }
    }

    private class EmitterTraitVisitor
            implements ITraitVisitor
    {
        Trait t;

        EmitterTraitVisitor(Trait t)
        {
            this.t = t;
        }

        public IMetadataVisitor visitMetadata(int count)
        {
            ABCEmitter.this.verifyEmitterStatus();

            return new IMetadataVisitor()
            {
                public void visit(Metadata md)
                {
                    ABCEmitter.this.verifyEmitterStatus();

                    ABCEmitter.EmitterTraitVisitor.this.t.addMetadata(md);
                }
            };
        }

        public void visitAttribute(String attr_name, Object attr_value)
        {
            ABCEmitter.this.verifyEmitterStatus();

            this.t.addAttr(attr_name, attr_value);
        }

        public void visitStart()
        {
            ABCEmitter.this.verifyEmitterStatus();
        }

        public void visitEnd()
        {
            ABCEmitter.this.verifyEmitterStatus();

            ABCEmitter.this.assertLockHeld();
        }
    }

    private class EmitterMethodBodyInfo
            implements IMethodBodyVisitor
    {
        MethodBodyInfo mbi;

        EmitterMethodBodyInfo(MethodBodyInfo mbinfo)
        {
            this.mbi = mbinfo;
        }

        public void visit()
        {
            ABCEmitter.this.verifyEmitterStatus();
        }

        public void visitEnd()
        {
            ABCEmitter.this.verifyEmitterStatus();

            ABCEmitter.this.assertLockHeld();
            ABCEmitter.this.poolOperands(this.mbi);
            ABCEmitter.this.methodBodies.add(this.mbi);
            for (ExceptionInfo exceptionInfo : this.mbi.getExceptions())
            {
                ABCEmitter.this.visitPooledName(exceptionInfo.getExceptionType());
                ABCEmitter.this.visitPooledName(exceptionInfo.getCatchVar());
            }
        }

        public void visitInstruction(int opcode)
        {
            ABCEmitter.this.verifyEmitterStatus();

            this.mbi.insn(opcode);
        }

        public void visitInstruction(int opcode, int immediate_operand)
        {
            ABCEmitter.this.verifyEmitterStatus();

            this.mbi.insn(opcode, immediate_operand);
        }

        public void visitInstruction(int opcode, Object single_operand)
        {
            ABCEmitter.this.verifyEmitterStatus();

            this.mbi.insn(opcode, single_operand);
        }

        public void visitInstruction(int opcode, Object[] operands)
        {
            ABCEmitter.this.verifyEmitterStatus();

            this.mbi.insn(opcode, operands);
        }

        public void visitInstruction(Instruction insn)
        {
            ABCEmitter.this.verifyEmitterStatus();

            this.mbi.insn(insn);
        }

        public ITraitsVisitor visitTraits()
        {
            ABCEmitter.this.verifyEmitterStatus();

            return new ABCEmitter.EmitterTraitsVisitor(this.mbi.getTraits());
        }

        public int visitException(Label from, Label to, Label target, Name ex_type, Name ex_var)
        {
            ABCEmitter.this.verifyEmitterStatus();

            return this.mbi.addExceptionInfo(new ExceptionInfo(from, to, target, ex_type, ex_var));
        }

        public void visitInstructionList(InstructionList new_list)
        {
            ABCEmitter.this.verifyEmitterStatus();

            this.mbi.setInstructionList(new_list);
        }

        public void labelCurrent(Label l)
        {
            this.mbi.labelCurrent(l);
        }

        public void labelNext(Label l)
        {
            this.mbi.labelNext(l);
        }
    }

    private class EmitterScriptInfo
            implements IScriptVisitor
    {
        final ScriptInfo si;

        EmitterScriptInfo()
        {
            this.si = new ScriptInfo();
        }

        public void visit()
        {
            ABCEmitter.this.verifyEmitterStatus();
        }

        public void visitEnd()
        {
            ABCEmitter.this.verifyEmitterStatus();
            ABCEmitter.this.assertLockHeld();
            ABCEmitter.this.scriptInfos.add(this.si);
        }

        public void visitInit(MethodInfo init_method)
        {
            ABCEmitter.this.verifyEmitterStatus();

            this.si.setInit(init_method);
        }

        public ITraitsVisitor visitTraits()
        {
            ABCEmitter.this.verifyEmitterStatus();

            return new ABCEmitter.EmitterTraitsVisitor(this.si.getTraits());
        }
    }

    private class EmitterMethodInfoVisitor
            implements IMethodVisitor
    {
        final MethodInfo mi;
        MethodBodyInfo mbi;

        EmitterMethodInfoVisitor(MethodInfo mi)
        {
            assert (mi != null);
            this.mi = mi;
        }

        public void visit()
        {
            ABCEmitter.this.verifyEmitterStatus();

            Lock methodInfosWriteLock = ABCEmitter.this.methodInfosLock.writeLock();
            methodInfosWriteLock.lock();
            try
            {
                ABCEmitter.this.methodInfos.add(this.mi);
            }
            finally
            {
                methodInfosWriteLock.unlock();
            }
        }

        public IMethodBodyVisitor visitBody(MethodBodyInfo mbi)
        {
            ABCEmitter.this.verifyEmitterStatus();

            this.mbi = mbi;
            return new ABCEmitter.EmitterMethodBodyInfo(mbi);
        }

        public void visitEnd()
        {
            ABCEmitter.this.verifyEmitterStatus();

            ABCEmitter.this.assertLockHeld();
            for (Name param_type_name : this.mi.getParamTypes()) {
                ABCEmitter.this.visitPooledName(param_type_name);
            }
            if ((this.mbi != null) && (this.mi.isNative())) {
                ABCEmitter.this.diagnosticsVisitor.nativeMethodWithMethodBody(this.mi, this.mbi);
            }
            ABCEmitter.this.visitPooledString(this.mi.getMethodName());
            ABCEmitter.this.visitPooledName(this.mi.getReturnType());
            for (Name ptype : this.mi.getParamTypes()) {
                ABCEmitter.this.visitPooledName(ptype);
            }
            if (this.mi.hasOptional()) {
                for (PooledValue v : this.mi.getDefaultValues()) {
                    v.setPoolIndex(ABCEmitter.this.visitPooledValue(v));
                }
            }
            if (this.mi.hasParamNames()) {
                for (String param_name : this.mi.getParamNames()) {
                    ABCEmitter.this.visitPooledString(param_name);
                }
            }
            if (this.mbi != null)
            {
                this.mbi.computeFrameCounts(ABCEmitter.this.diagnosticsVisitor);
                if ((ABCEmitter.this.eagerlyEmitMethodBodies) && (!this.mbi.hasNewclassInstruction())) {
                    try
                    {
                        ABCEmitter.this.emitCode(this.mbi);
                    }
                    catch (RuntimeException uncheckedSNAFU)
                    {
                        throw uncheckedSNAFU;
                    }
                    catch (Exception checkedSNAFU)
                    {
                        throw new IllegalStateException(checkedSNAFU);
                    }
                }
            }
        }
    }

    private int visitPooledValue(PooledValue value)
    {
        switch (value.getKind())
        {
            case 3:
                visitPooledInt(value.getIntegerValue());
                return this.intPool.id(value.getIntegerValue());
            case 4:
                visitPooledUInt(value.getLongValue());
                return this.uintPool.id(value.getLongValue());
            case 6:
                visitPooledDouble(value.getDoubleValue());
                return this.doublePool.id(value.getDoubleValue());
            case 1:
                visitPooledString(value.getStringValue());
                return this.stringPool.id(value.getStringValue());
            case 11:
                return 11;
            case 10:
                return 10;
            case 0:
                return 0;
            case 12:
                return 12;
            case 5:
            case 8:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
                visitPooledNamespace(value.getNamespaceValue());
                return this.nsPool.id(value.getNamespaceValue());
        }
        throw new IllegalStateException("Unrecognized initializer type: " + value.getKind());
    }

    public void setAllowBadJumps(boolean b)
    {
        this.allowBadJumps = b;
    }

    private void assertLockHeld()
    {
        assert (this.lock.isHeldByCurrentThread()) : "A visitEnd method was called from a thread other than the thread that called IABCVisitor.visit!";
    }

    private void verifyEmitterStatus()
    {
        if (this.visitEndCalled) {
            throw new IllegalStateException("An ABCEmitter can only emit once visitEnd has been called.");
        }
    }
}
