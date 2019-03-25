package swftool.gencode;

import org.apache.commons.io.FileUtils;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * created by lizhi
 */
public class CodeGen {
    //genTrees 类名不能重复

   // 随机继承 不能循环继承
    //生成若干变量 随机类型 私有公有
    //生成若干函数 不要构造函数

    //生成函数体代码
    //随机返回值
    //for循环操作变量

    private String path;
    private List<String> words;
    private HashSet<String> wordRemove;
    private static String[] ops=new String[]{"+","-","*","/"};
    public CodeGen(String path, List<String> words, HashSet<String> wordRemove, int gennum){
        this.path=path;
        this.words=words;
        this.wordRemove=wordRemove;
        System.out.println("var a:Number=0;");
        ArrayList<Node> stack=new ArrayList<>();
        int num=0;
        ArrayList<Node> leafs=new ArrayList<>();
        while (num<gennum){
            if(stack.size()==0){
                stack.add(genNode(null));
            }
            Node n=stack.remove(0);
            if(n.depth>4||Math.random()<.3) {
                n.isLeaf = true;
                leafs.add(n);
                num++;

            }else {
                int l=1+(int)(Math.random()*10);
                for (int i=0;i<l;i++){
                    Node c=genNode(n);
                    stack.add(c);
                }
            }
        }
        for(Node n : leafs){
            /*if(Math.random()<.5){防止出现循环引用
                while (true){
                    Node s=leafs.get((int)(Math.random()*leafs.size()));
                    if(s!=n){
                        n.superNode=s;
                        break;
                    }
                }
            }*/

            int l=2+(int)(3*Math.random());
            for(int i=0;i<l;i++){
                Var v=new Var();//生成变量
                if(Math.random()<.1){
                    v.typeName="Number";
                }else {
                    v.typeNode=leafs.get((int)(Math.random()*leafs.size()));
                    v.typeName=v.typeNode.name;
                }
                v.name=getRandomWord();
                n.vars.add(v);
            }

            l=2+(int)(3*Math.random());//生成函数
            for(int i=0;i<l;i++){
                Fun f=new Fun();//生成变量
                if(Math.random()<.1){
                    f.typeName="Number";
                }else if(Math.random()<.5){
                    f.typeName="void";
                }else {
                    f.typeNode=leafs.get((int)(Math.random()*leafs.size()));
                    f.typeName=f.typeNode.name;
                }
                f.name=getRandomWord();
                n.funs.add(f);
            }
        }
        for(Node n : leafs){
            for(Fun f:n.funs){
                StringBuilder bb=new StringBuilder();
                int l2=2+(int)(2*Math.random());
                for(int j=0;j<l2;j++){
                    bb.append(genCodeBlack(n));
                }
                f.body=bb.toString();
            }
        }

        for(Node n : leafs){
            File file=new File(path+n.getPath()+".as");
            try {
                FileUtils.writeStringToFile(file,n.genCode());
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String genCodeBlack(Node n){
        StringBuilder bb=new StringBuilder();
        Var var=n.vars.get((int)(Math.random()*n.vars.size()));//随机一个n的var
        if(var.typeNode==null){//如果是数字就for循环四则混合运算
            int l=4+(int)(10*Math.random());
            for(int i=0;i<l;i++){
                bb.append(var.name);
                bb.append(ops[(int)(ops.length*Math.random())]);
                bb.append("=");
                bb.append(Math.random());
                bb.append(";\n");
            }
        }else {//如果是类，就随机调用类的方法
            int l=4+(int)(10*Math.random());
            for(int i=0;i<l;i++){
                bb.append(var.name);
                bb.append(".");
                bb.append(var.typeNode.funs.get((int)(Math.random()*var.typeNode.funs.size())).name);
                bb.append("()");
                bb.append(";\n");
            }
        }
        return bb.toString();
    }

    private Node genNode(Node parent){
        Node node=new Node();
        if(parent!=null){
            node.parent=parent;
            if(parent.childs==null){
                parent.childs=new ArrayList<>();
            }
            parent.childs.add(node);
            node.depth=parent.depth+1;
        }
        node.name=getRandomWord();
        return  node;
    }

    private String getRandomWord(){
        while (true){
            String s1="";//getRandomWord1();
            int l=(int)(Math.random()*2)+1;
            for(int i=0;i<l;i++){
                String s=getRandomWord1();
                s1+=s.substring(0,1).toUpperCase();
                s1+=s.substring(1);
            }
            if(wordRemove==null||!wordRemove.contains(s1)){
                return s1;
            }
        }
    }

    private String getRandomWord1(){
        //不能包含在现有的strings中 不能包含已经随机到的word中，只包含英文
        return words.remove((int)(Math.random()*words.size()));
    }
}
