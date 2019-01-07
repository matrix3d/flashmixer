package swftool.build;

import com.adobe.flash.compiler.internal.projects.ASSourceFileHandler;
import com.adobe.flash.compiler.internal.projects.CompilerProject;
import com.adobe.flash.compiler.internal.projects.DefinitionPriority;
import com.adobe.flash.compiler.internal.projects.ISourceFileHandler;
import com.adobe.flash.compiler.internal.units.ASCompilationUnit;
import com.adobe.flash.compiler.units.ICompilationUnit;

/**
 * created by lizhi
 */
public final class MyASSourceFileHandler implements ISourceFileHandler {
    public static final String EXTENSION = "as";
    public static final MyASSourceFileHandler INSTANCE = new MyASSourceFileHandler();

    private MyASSourceFileHandler() {
    }

    public String[] getExtensions() {
        return new String[]{"as"};
    }

    public ICompilationUnit createCompilationUnit(CompilerProject project, String path, DefinitionPriority.BasePriority basePriority, int order, String qname, String locale) {
        return new MyASCompilationUnit(project, path, basePriority, order, qname);
    }

    public boolean needCompilationUnit(CompilerProject project, String path, String qname, String locale) {
        return true;
    }

    public boolean canCreateInvisibleCompilationUnit() {
        return true;
    }
}
