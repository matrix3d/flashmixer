package swftool.build;

import com.adobe.flash.compiler.internal.as.codegen.CodeGeneratorManager;
import com.adobe.flash.compiler.internal.projects.CompilerProject;
import com.adobe.flash.compiler.internal.projects.DefinitionPriority;
import com.adobe.flash.compiler.internal.tree.as.ClassNode;
import com.adobe.flash.compiler.internal.units.ASCompilationUnit;
import com.adobe.flash.compiler.tree.as.IASNode;
import com.adobe.flash.compiler.units.ICompilationUnit;
import com.adobe.flash.compiler.units.requests.IABCBytesRequestResult;
import com.adobe.flash.compiler.units.requests.IRequest;
import com.adobe.flash.compiler.units.requests.ISyntaxTreeRequestResult;

/**
 * created by lizhi
 */
public class MyASCompilationUnit extends ASCompilationUnit {
    public MyASCompilationUnit(CompilerProject project, String path, DefinitionPriority.BasePriority basePriority) {
        super(project, path, basePriority);
    }

    public MyASCompilationUnit(CompilerProject project, String path, DefinitionPriority.BasePriority basePriority, int order) {
        super(project, path, basePriority, order);
    }

    public MyASCompilationUnit(CompilerProject project, String path, DefinitionPriority.BasePriority basePriority, int order, String qname) {
        super(project, path, basePriority, order, qname);
        System.out.println(path);
    }

    @Override
    protected IABCBytesRequestResult handleABCBytesRequest() throws InterruptedException {
        ISyntaxTreeRequestResult fsr = (ISyntaxTreeRequestResult)this.getSyntaxTreeRequest().get();
        IASNode rootNode = fsr.getAST();

        for(int i=0;i<rootNode.getChildCount();i++){
            IASNode node=rootNode.getChild(i);
            if(node instanceof ClassNode){
                ClassNode classNode=(ClassNode)node;

            }
        }


        CompilerProject project = this.getProject();
        this.startProfile(Operation.GET_ABC_BYTES);
        IABCBytesRequestResult result = CodeGeneratorManager.getCodeGenerator().generate(project.getWorkspace().getExecutorService(), project.getUseParallelCodeGeneration(), this.getFilenameNoPath(), rootNode, this.getProject(), this.isInvisible(), this.getEncodedDebugFiles());
        this.stopProfile(Operation.GET_ABC_BYTES);
        return result;
    }
}
