package swftool.build;

import com.adobe.flash.compiler.internal.projects.CompilerProject;
import com.adobe.flash.compiler.internal.projects.DefinitionPriority;
import com.adobe.flash.compiler.internal.units.ASCompilationUnit;

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
}
