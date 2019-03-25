package swftool;

import com.adobe.flash.abc.ABCConstants;
import com.adobe.flash.abc.ABCLinker;
import com.adobe.flash.abc.ABCParser;
import com.adobe.flash.abc.semantics.*;
import com.adobe.flash.abc.visitors.IABCVisitor;
import com.adobe.flash.compiler.clients.COMPC;
import com.adobe.flash.swc.ISWCLibrary;
import com.adobe.flash.swc.ISWCScript;
import com.adobe.flash.swc.SWC;
import com.adobe.flash.swc.SWCScript;
import com.adobe.flash.swc.io.SWCReader;
import com.adobe.flash.swf.Header;
import com.adobe.flash.swf.SWF;
import com.adobe.flash.swf.SWFFrame;
import com.adobe.flash.swf.io.SWFDump;
import com.adobe.flash.swf.io.SWFReader;
import com.adobe.flash.swf.tags.DoABCTag;
import com.adobe.flash.swf.tags.ICharacterTag;
import com.adobe.flash.swf.tags.ITag;
import com.adobe.flash.swf.tags.SymbolClassTag;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.org.apache.xerces.internal.xs.StringList;
import org.apache.commons.io.FileUtils;
import swftool.gencode.CodeGen;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.net.URL;
import java.util.*;

import static java.lang.Integer.parseInt;

/**
 * ...
 * @author lizhi
 */
public class Mixer
{
    private String CHAR_SET_MAP = "~!@$%^&*()+-=<>?{}[]|,/;";
    private String CHAR_SET;
    private int characterIndex;
    private int charSetLength;
    public HashSet<String> stringMap;//=new HashMap<String,Boolean>();
    public Map<String,String> mixedMap;
    public Map<String,String> reservedStructureMap;
    private Set<String> nomixMap;
    private Set<String> nomixMap2;
    private Set<String> nomixStartsMap;
    private Set<String> nomixPackMap;
    private Set<String> waitMixs;
    private List<String> words;
    public Byte[] newbyte;
    private List<DoABCTag> abcs;
    private List syms;
    private SWF swf=null;
    public SWF outswf=null;
    private HashMap<DoABCTag, IABCVisitor> tag2abc = new HashMap<>();
    private boolean isMixClass;
    private boolean isMixPackage;
    private boolean isMixVar;
    private boolean isMixFunc;
    private Map<String,String> mixMap;
    private boolean atenable;
    private boolean hasAt;
    private boolean reservedStructure;
    private boolean isRubbish;
    private String mixcode;
    private String[] nomixpack;
    private File file;
    public Mixer(File file, boolean isMixClass, boolean isMixPackage,boolean isMixVar,boolean isMixFunc,Map<String,String> mixMap, String mixcode,boolean reservedStructure,String noMixPackStr,boolean isRubbish)
    {
        this.file=file;
        this.mixMap = mixMap;
        this.isMixPackage = isMixPackage;
        this.isMixClass = isMixClass;
        this.isMixVar=isMixVar;
        this.isMixFunc=isMixFunc;
        this.mixcode=mixcode;
        this.reservedStructure=reservedStructure;
        this.isRubbish=isRubbish;

        long time=System.currentTimeMillis();
        System.out.println("start gson"+time);
        Gson gson=new GsonBuilder().disableHtmlEscaping().create();
        InputStreamReader ois=null;
        try{
            ois=new InputStreamReader(this.getClass().getResourceAsStream("/res/airglobal_strs"));
            nomixMap=gson.fromJson(ois,HashSet.class);//(HashSet<String>)ois.readObject();
            ois.close();
        }catch (Exception e){
            e.printStackTrace();
            nomixMap=new HashSet<>();
        }
        try{
            ois=new InputStreamReader(this.getClass().getResourceAsStream("/res/words.txt"));
            BufferedReader inputStream=new BufferedReader(ois);
            String line=null;
            words= new ArrayList();
            HashSet<String> wordsset=new HashSet<>();
            while (true){
                line=inputStream.readLine();
                if(line==null){
                    break;
                }
                line=line.toLowerCase();
                if(line.matches("[A-Za-z]+")&&!wordsset.contains(line)&&line.length()>2){
                    words.add(line);
                    wordsset.add(line);
                }
            }
            inputStream.close();
            ois.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        if(noMixPackStr.length()>0){
            if(noMixPackStr.indexOf(",")==-1){
                nomixpack=new String[]{noMixPackStr};
            }else {
                nomixpack=noMixPackStr.split(",");
            }
        }

        System.out.println(System.currentTimeMillis()-time);
        time=System.currentTimeMillis();
        System.out.println("start reset"+time);
        reset();
        System.out.println(System.currentTimeMillis()-time);
        time=System.currentTimeMillis();
        System.out.println("start swfreader"+time);
        SWFReader reader=new SWFReader();
        try {
            URL url=file.toURI().toURL();
            swf = (SWF)reader.readFrom(new BufferedInputStream(url.openStream()), url.getPath());
            swf.getHeader().setSignature(Header.SIGNATURE_COMPRESSED_LZMA);

        }catch (Exception err){
            err.printStackTrace();
        }
        System.out.println(System.currentTimeMillis()-time);

        for(SWFFrame frame: swf.getFrames()){
            Iterator<ITag> it=frame.iterator();
            while (it.hasNext()){
                ITag iTag= it.next();
                if(iTag instanceof DoABCTag){
                    DoABCTag abcTag=(DoABCTag) iTag ;
                    findABCMethodBodies(abcTag);
                    abcs.add(abcTag);
                }
            }
        }
        time=System.currentTimeMillis();
        System.out.println("start findabc"+time);
        for(DoABCTag abcTag:abcs){
            findABC(abcTag);
        }
        System.out.println(System.currentTimeMillis()-time);
        time=System.currentTimeMillis();
        System.out.println("start domix"+time);
        doMix();
        System.out.println(System.currentTimeMillis()-time);
        outswf=swf;
    }

    private void reset(){
        stringMap = new HashSet<>();
        waitMixs = new HashSet<>();
        nomixMap2 = new HashSet<>();
        nomixStartsMap = new HashSet<>();
        abcs = new ArrayList();
        syms = new ArrayList();
        mixedMap = new HashMap<>();
        reservedStructureMap = new HashMap<>();
        nomixPackMap = new HashSet<>();
        characterIndex = 0;
        List<String> arr = new ArrayList();
        for (int i = 0; i < CHAR_SET_MAP.length();i++ ){
            arr.add(CHAR_SET_MAP.charAt(i)+"");
        }
        CHAR_SET = "";
        while (arr.size()>0){
            CHAR_SET += arr.remove((int)(arr.size()*Math.random()));
        }
        this.charSetLength = CHAR_SET.length();
    }

    private void findABCMethodBodies(DoABCTag doABCTag) {
        ABCParser abcParser=new ABCParser(doABCTag.getABCData());
        long time=System.currentTimeMillis();
        System.out.println("start parseABC"+time);
        ABCEmitter abc=new ABCEmitter();
        abc.setAllowBadJumps(true);
        abcParser.parseABC(abc);
        System.out.println(System.currentTimeMillis()-time);

        tag2abc.put(doABCTag,abc);

        /*ArrayList<String> listst=abc.stringPool.getValues();
        for(int i=0;i<listst.size();i++ ){
            listst.set(i,Math.random()+"");
        }
        try{
            doABCTag.setABCData(abc.emit());
        }catch (Exception e) {
            e.printStackTrace();
        }*/
        //System.out.println(1);

        for (MethodBodyInfo mb : abc.methodBodies){
            MethodInfo mi=mb.getMethodInfo();
            if(isMixFunc){
                waitMixs.add(mi.getMethodName());
            }
            Vector<Name> paramTypes= mi.getParamTypes();
            List<String> paramNames= mi.getParamNames();
            Vector<PooledValue> defaultValues= mi.getDefaultValues();
            //if(paramTypes.size()>0&&defaultValues.size()>0&&paramTypes.size()!=defaultValues.size()&&defaultValues.size()>1){
                //System.out.println(mi.getMethodName()+","+mi.getDefaultValues());
            //}

            for(PooledValue pooledValue : defaultValues){
                if (pooledValue.getKind()==1) {
                    nomixMap2.add(pooledValue.getStringValue());
                    //System.out.println(pooledValue.getStringValue());
                }
            }
            if(isMixFunc){
                waitMixs.add(mi.getMethodName());
            }
            boolean isGetDefing = false;

            if(mb.getInstructionList()!=null) {
                for (Instruction instruction : mb.getInstructionList().getInstructions()) {

                    if (instruction.getOpcode() == ABCConstants.OP_pushstring) {
                        String pstr = (String) instruction.getOperand(0);
                        nomixMap2.add(pstr);

                        if (isGetDefing) {
                            int index = pstr.lastIndexOf("::");
                            int index2 = index + 2;
                            if (index == -1) {
                                index = pstr.lastIndexOf(".");
                                if (index != -1) {
                                    index2 = index + 1;
                                }
                            }
                            if (index != -1) {
                                String nomixPackname = pstr.substring(0, index);
                                if (nomixPackname.length() > 0) {
                                    nomixPackMap.add(nomixPackname);
                                    nomixMap2.add(nomixPackname);
                                }
                                String nomixClsName = pstr.substring(index2);
                                if (nomixClsName.length() > 0) {
                                    nomixStartsMap.add(nomixClsName);
                                }
                            }
                            isGetDefing = false;
                        }
                    }

                    if(instruction.getOpcode()==ABCConstants.OP_getproperty||instruction.getOpcode()==ABCConstants.OP_setproperty){
                        if (instruction.getOperandCount() > 0 && instruction.getOperand(0) instanceof Name) {
                            Name name = (Name) instruction.getOperand(0);
                            if(name.getQualifiers()!=null) {
                                for (Namespace ns : name.getQualifiers()) {
                                    if ("Object".equals(ns.getName())) {
                                        nomixMap2.add(name.getBaseName());
                                        break;
                                    }
                                }
                            }else {
                                nomixMap2.add(name.getBaseName());
                            }
                        }
                    }

                    if (instruction.getOperandCount() > 0 && instruction.getOperand(0) instanceof Name) {
                        Name name = (Name) instruction.getOperand(0);
                        // System.out.println(name.getBaseName());
                        if ("getDefinitionByName".equals(name.getBaseName()) && "flash.utils".equals(name.getSingleQualifier().getName())) {
                            isGetDefing = true;
                        }
                    }
                }
            }
        }
    }
    private void findABC(DoABCTag doABCTag)
    {
        ABCEmitter abc=(ABCEmitter) tag2abc.get(doABCTag);
        for(ABCEmitter.EmitterClassVisitor ci : abc.definedClasses){
            boolean mixVarAndFun=true;
            if(ci.instanceInfo.superName!=null) {
                String superClassName = ci.instanceInfo.superName.getBaseName();
                String superPackName = ci.instanceInfo.superName.getSingleQualifier().getName();
                if (("View".equals(superClassName)||"Dialog".equals(superClassName))&&"morn.core.components".equals(superPackName)) {
                    mixVarAndFun=false;
                }
            }
            String nsname=ci.instanceInfo.name.getSingleQualifier().getName();
            String cname=ci.instanceInfo.name.getBaseName();
            if(nsname!=null&&(nsname.indexOf("morn.")==0||"morn".equals(nsname))){//不混淆morn.的函数和变量
                mixVarAndFun=false;
            }

            if(nomixpack.length>0&&nsname!=null){
                for(String s : nomixpack){
                    if(nsname.indexOf(s)==0){
                        nomixMap2.add(cname);
                        nomixMap2.add(nsname);
                        break;
                    }
                }
            }


            if (nomixPackMap.contains(nsname)){
                nomixMap2.add(cname);
                nomixMap2.add(nsname);
            }
            if(isMixClass){
                waitMixs.add(cname);
            }
            if(isMixPackage){
                waitMixs.add(nsname);
            }
            for(Trait trait : ci.instanceInfo.traits){
                //System.out.println(trait.getName().getBaseName()+":"+trait.getKind());
                if(mixVarAndFun){
                    if(isMixVar&&(trait.isSlot()||trait.isGetter()||trait.isSetter())){
                        waitMixs.add(trait.getName().getBaseName());
                    }
                    if(isMixFunc&&trait.isMethod()) {
                        waitMixs.add(trait.getName().getBaseName());
                    }
                }else {
                    nomixMap2.add(trait.getName().getBaseName());
                }
            }
        }
        for (String s : abc.stringPool.getValues()){
            stringMap.add(s);
        }for(Namespace ln : abc.nsPool.getValues()){
            if (ln.getKind()==5){//私有，不能混淆？
                nomixMap2.add(ln.getName());
            }
        }
    }



    private void doMix(){
       for (DoABCTag doABCTag : abcs){
           ABCEmitter abc=(ABCEmitter) tag2abc.get(doABCTag);

           ArrayList<String> listst=abc.stringPool.getValues();
           for(int i=0;i<listst.size();i++ ){
               String s = listst.get(i);// = sp[i] + "ddd";
               if(mixMap==null){
                   boolean flag = false;
                   for(String startName : nomixStartsMap){
                       if (s.indexOf(startName)==0){
                           flag = true;
                           break;
                       }
                   }
                   if (flag){
                       continue;
                   }
                   if (!nomixMap.contains(s)&& !nomixMap2.contains(s) && (!"".equals(s)) && !isNumeric(s)&&waitMixs.contains(s)){
                        String mixstr = getMixCharacter(s);
                        listst.set(i,mixstr);
                       mixedMap.put(s, mixstr);
                   }
               }else{
                   if (mixMap.get(s)!=null){
                       mixedMap.put(s, mixMap.get(s));
                       listst.set(i,mixMap.get(s));
                   }
               }
           }
           try{
               doABCTag.setABCData(abc.emit());
           }catch (Exception e) {
               e.printStackTrace();
           }

        }
        String sname=swf.getTopLevelClass();
       if(sname!=null) {
           swf.setTopLevelClass(getSymbolName(sname));
       }
        for(SWFFrame frame: swf.getFrames()){
            Iterator<ITag> it=frame.iterator();
            while (it.hasNext()){
                ITag iTag= it.next();
                if(iTag instanceof SymbolClassTag){
                    SymbolClassTag sym=(SymbolClassTag) iTag ;
                    Map<String, ICharacterTag> emap=new HashMap<>();
                    for(String syname :sym.getSymbolNames()){
                        emap.put(syname,sym.getSymbol(syname));
                    }
                    for(String syname:emap.keySet()){
                        sym.removeSymbol(syname);
                        sym.addSymbol(emap.get(syname),getSymbolName(syname));
                    }
                }
            }
            //frame.sy
        }

        //gen rubbish code
        //FileSystemView fsv = FileSystemView.getFileSystemView();
        //File com=fsv.getHomeDirectory();
        if(isRubbish) {
            int i=0;
            File rbfile;
            while (true) {
                rbfile = new File(file.getParent() + File.separator + "__rubbish" + i);
                i++;
                if (!rbfile.exists()) {
                    break;
                }
            }
            new CodeGen(rbfile.getPath(), words, stringMap, 1000);
            String swcfile=file.getParent()+File.separator+rbfile.getName()+".swc";
            try {
                //SWCWriter writer=new SWCWriter(file.getParent()+File.separator+"swc_"+file.getName()+".swc");
                System.setProperty("file.encoding","gb2312");
                System.setProperty("flexlib",Config.sdk+"/frameworks");
                //%FLEX%\bin\compc -load-config %FLEX%/frameworks/air-config.xml -sp ../lib3d/src -include-sources ../lib3d/src -external-library-path+=libin -inline -o lib.swc
                //batstr += "%FLEX%\\bin\\mxmlc -load-config=\"%FLEX%/frameworks/airmobile-config.xml\" -default-size 1440 810 -swf-version=35 -compress=true -omit-trace-statements=false -warnings=false -define=CONFIG::debug,false -define=CONFIG::release,true -define=CONFIG::mobile,true -define=CONFIG::air,true -define=CONFIG::timeStamp,%date:~0,4%-%date:~5,2%-%date:~8,2%/%time:~0,2%/%time:~3,2%/%time:~6,2% -define=CONFIG::browser,false -define=CONFIG::autosize,true -define=CONFIG::anfeng,true ";
                COMPC.staticMainNoExit(new String[]{"-sp", rbfile.getPath(),"-include-sources",rbfile.getPath(),"-define=CONFIG::js_only,false","-define=CONFIG::as_only,true","-o",swcfile});


            } catch (Exception e) {
                e.printStackTrace();
            }
            SWCReader reader=new SWCReader(swcfile);
            SWC swc= (SWC) reader.getSWC();
            Collection<ISWCLibrary> iswcLibraries= swc.getLibraries();
            ArrayList<byte[]> rbabcs=new ArrayList<>();
            for(ISWCLibrary swcLibrary: iswcLibraries){
                SWFReader swfReader=new SWFReader();
                swcLibrary.readSWFInputStream(swfReader,swc);

                SWF swf=(SWF)swfReader.getSWF();
                for(SWFFrame frame: swf.getFrames()){
                    Iterator<ITag> it=frame.iterator();
                    while (it.hasNext()){
                        ITag iTag= it.next();
                        if(iTag instanceof DoABCTag){
                            DoABCTag abcTag=(DoABCTag) iTag;
                            byte[] abcdata= abcTag.getABCData();
                            rbabcs.add(abcdata);
                        }
                    }
                }
            }

            for (DoABCTag doABCTag : abcs){
                ABCEmitter abc=(ABCEmitter) tag2abc.get(doABCTag);
                ArrayList<byte[]> abcbs=new ArrayList<>();
                abcbs.add(doABCTag.getABCData());
                for(byte[] rbabc:rbabcs){
                    abcbs.add(rbabc);
                }
                try{
                    doABCTag.setABCData(ABCLinker.linkABC(abcbs,abc.versionABCMajor,abc.versionABCMinor,new ABCLinker.ABCLinkerSettings()));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }


        }
    }

    private String getSymbolName(String sname){
        String[] snamearr = sname.split("\\.");
        if (snamearr.length>2){
            String[] t = new String[2];
            t[1] = snamearr[snamearr.length-1];

            String[] newt=new String[snamearr.length-1];
            for(int i=0;i<newt.length;i++){
                newt[i]=snamearr[i];
            }

            t[0] = String.join(".",newt);
            snamearr = t;
        }
        for (int ii = 0; ii < snamearr.length;ii++ ){
            if(mixedMap.get(snamearr[ii])!=null) {
                snamearr[ii] = mixedMap.get(snamearr[ii]);
            }
        }
        return  (String.join(".",snamearr));
    }

    public static boolean isNumeric (String str) {
        for (int i = str.length(); --i >=0;) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private String getMixCharacter(String source)
    {
        if (mixedMap.get(source)!=null){
            return mixedMap.get(source);
        }
        if(reservedStructureMap.get(source)!=null){
            return reservedStructureMap.get(source);
        }
        String r=null;
        if(source.indexOf(".")==-1||!reservedStructure){
            r = getRandomText(source);
        }else{
            String[] arr = source.split("\\.");
            for(int i=0;i<arr.length;i++){
                if(nomixMap.contains(arr[i])||nomixMap2.contains(arr[i])){
                    continue;
                }
                String s=reservedStructureMap.get(arr[i]);
                if(s==null){
                    s=mixedMap.get(arr[i]);
                }
                if(s==null){
                    s=getRandomText(arr[i]);
                    reservedStructureMap.put(arr[i],s);
                    arr[i]=s;
                }
            }
            r=String.join(".",arr);
        }
        return r;
    }

    private String getRandomText(String source){
        String r = "";
        int i = characterIndex;
        do {
            r += CHAR_SET.charAt(i % charSetLength);
            i = i / charSetLength >> 0;
        }while (i > 0);

        characterIndex++;
        return mixcode.replace("#",getRandomWord()).replace("@",source).replace("*",r);
    }

    private String getRandomWord(){
        while (true){
            String s1=getRandomWord1();
            int l=(int)(Math.random()*2)+1;
            for(int i=0;i<l;i++){
                String s=getRandomWord1();
                s1+=s.substring(0,1).toUpperCase();
                s1+=s.substring(1);
            }
            if(!stringMap.contains(s1)){
                return s1;
            }
        }
    }

    private String getRandomWord1(){
        //不能包含在现有的strings中 不能包含已经随机到的word中，只包含英文
        return words.remove((int)(Math.random()*words.size()));
    }
}

