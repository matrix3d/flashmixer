package swftool.gencode;

import java.io.File;
import java.util.ArrayList;

/**
 * created by lizhi
 */
public class Node {
    public int depth=0;
    public boolean isLeaf;
    public Node parent;
    public ArrayList<Node> childs;
    public String name;
    public Node superNode;
    public ArrayList<Var> vars=new ArrayList<>();
    public ArrayList<Fun> funs=new ArrayList<>();
    public String getPath(){
        String p="";
        Node n=this;
        while (n!=null){
            p= File.separator +n.name+p;
            n=n.parent;
        }
        return p;
    }

    public String getClassPath(){
        String p="";
        Node n=this;
        while (n!=null){
            p= "." +n.name+p;
            n=n.parent;
        }
        return p.substring(1);
    }

    public String genCode() {
        StringBuilder builder=new StringBuilder();
        builder.append("package ");
        if(parent!=null){
            builder.append(parent.getClassPath());
        }
        builder.append("{\n");
        if(superNode!=null){
            builder.append("import ");
            builder.append(superNode.getClassPath());
            builder.append(";\n");
        }
        for (Var v:vars){
            if(v.typeNode!=null){
                builder.append("import ");
                builder.append(v.typeNode.getClassPath());
                builder.append(";\n");
            }
        }
        for (Fun f:funs){
            if(f.typeNode!=null){
                builder.append("import ");
                builder.append(f.typeNode.getClassPath());
                builder.append(";\n");
            }
        }
        builder.append("public class ");
        builder.append(name);
        if(superNode!=null){
            builder.append(" extends ");
            builder.append(superNode.name);
        }
        builder.append("{\n");

        for (Var v:vars){
            if(v.typeName!=null){
                builder.append("public var ");
                builder.append(v.name);
                builder.append(":");
                builder.append(v.typeName);
                builder.append(";\n");
            }
        }

        for (Fun f:funs){
            if(f.typeName!=null){
                builder.append("public function ");
                builder.append(f.name);
                builder.append("():");
                builder.append(f.typeName);
                builder.append("{\n");
                builder.append(f.body);
                if(f.typeName.equals("void")){

                }else if(f.typeName.equals("Number")){
                    builder.append("return ");
                    builder.append(Math.random());
                    builder.append(";");
                }else {
                    builder.append("return null;");
                }
                builder.append("}\n");
            }
        }

        builder.append("}\n");
        builder.append("}\n");
        return  builder.toString();
    }
}
