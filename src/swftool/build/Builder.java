package swftool.build;

import com.adobe.flash.compiler.clients.MXMLC;
import com.adobe.flash.compiler.internal.projects.DefinitionPriority;
import com.adobe.flash.compiler.internal.targets.MySWFTarget;
import com.adobe.flash.compiler.internal.targets.SWFTarget;
import com.adobe.flash.compiler.internal.units.ResourceModuleCompilationUnit;
import com.adobe.flash.compiler.internal.units.SourceCompilationUnitFactory;
import com.adobe.flash.compiler.internal.units.StyleModuleCompilationUnit;
import com.adobe.flash.compiler.problems.*;
import com.adobe.flash.compiler.targets.ITargetProgressMonitor;
import com.adobe.flash.compiler.targets.ITargetSettings;
import com.adobe.flash.compiler.units.ICompilationUnit;
import com.adobe.flash.swf.SWF;
import com.adobe.flash.swf.io.SWFWriter;
import com.adobe.flash.utils.FilenameNormalization;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.*;

/**
 * created by lizhi
 */
public class Builder extends MXMLC{
    public Builder(File file) {
        this.project.getSourceCompilationUnitFactory().addHandler(MyASSourceFileHandler.INSTANCE);// = new MyFlexProject(this.workspace);
        System.setProperty("file.encoding","gb2312");
        System.setProperty("flexlib","D:\\sdk\\AIRSDK_Compiler30\\frameworks");
        Properties properties = System.getProperties();
        try{
            //MXMLC mxmlc=new MXMLC();
            //mxmlc.mainNoExit(new String[]{file.getPath()});
            //newBuild(file);
            mainNoExit(new String[]{file.getPath()});
            //MXMLC.staticMainNoExit(new String[]{file.getPath()});
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /*protected boolean setupTargetFile() throws InterruptedException {
        String mainFileName = this.config.getTargetFile();
        if (mainFileName != null) {
            String normalizedMainFileName = FilenameNormalization.normalize(mainFileName);
            File normalizedMainFile = new File(normalizedMainFileName);
            if (mainFileName.toLowerCase().endsWith(".css")) {
                if (!this.project.isFlex()) {
                    this.problems.add(new UnsupportedSourceFileProblem(normalizedMainFile));
                    return false;
                }

                this.mainCU = new StyleModuleCompilationUnit(this.project, this.workspace.getFileSpecification(normalizedMainFileName), DefinitionPriority.BasePriority.SOURCE_LIST);
                this.config.setMainDefinition("CSSModule2Main");
                this.project.addCompilationUnitsAndUpdateDefinitions(Collections.singleton(this.mainCU));
            } else {
                SourceCompilationUnitFactory compilationUnitFactory = this.project.getSourceCompilationUnitFactory();
                if (!compilationUnitFactory.canCreateCompilationUnit(normalizedMainFile)) {
                    this.problems.add(new UnsupportedSourceFileProblem(normalizedMainFile));
                    return false;
                }

                this.project.removeSourceFile(normalizedMainFile);
                this.project.addIncludeSourceFile(normalizedMainFile, true);
                Collection<ICompilationUnit> mainFileCompilationUnits = this.workspace.getCompilationUnits(normalizedMainFileName, this.project);

                assert mainFileCompilationUnits.size() == 1;

                this.mainCU = (ICompilationUnit) Iterables.getOnlyElement(mainFileCompilationUnits);
            }
        } else {
            List<ICompilerProblem> resourceBundleProblems = new ArrayList();
            Collection<ICompilationUnit> includedResourceBundles = this.target.getIncludedResourceBundlesCompilationUnits(resourceBundleProblems);
            this.problems.addAll(resourceBundleProblems);
            if (includedResourceBundles.size() > 0) {
                if (!this.project.isFlex()) {
                    ICompilationUnit unit = (ICompilationUnit)includedResourceBundles.iterator().next();
                    this.problems.add(new UnsupportedSourceFileProblem(new File(unit.getAbsoluteFilename())));
                    return false;
                }

                this.mainCU = new ResourceModuleCompilationUnit(this.project, "GeneratedResourceModule", includedResourceBundles, DefinitionPriority.BasePriority.SOURCE_LIST);
                this.config.setMainDefinition("GeneratedResourceModule");
                this.project.addCompilationUnitsAndUpdateDefinitions(Collections.singleton(this.mainCU));
            }
        }

        Preconditions.checkNotNull(this.mainCU, "Main compilation unit can't be null");
        if (this.getTargetSettings() == null) {
            return false;
        } else {
            this.target = (SWFTarget)this.project.createSWFTarget(this.getTargetSettings(), (ITargetProgressMonitor)null);
            this.target=new MySWFTarget(this.project, this.getTargetSettings(), (ITargetProgressMonitor)null);
            return true;
        }
    }
    private ITargetSettings getTargetSettings() {
        if (this.targetSettings == null) {
            this.targetSettings = this.projectConfigurator.getTargetSettings(this.getTargetType());
        }

        return this.targetSettings;
    }

    public void newBuild(File file){
        configure(new String[]{file.getPath()});
        try {
            this.setupTargetFile();
            List<ICompilerProblem> problemsBuildingSWF = new ArrayList();
            SWF swf = (SWF)this.target.build(problemsBuildingSWF);
            SWFWriter writer=new SWFWriter(swf,swf.getHeader().getCompression());
            File out=new File(getOutputFilePath());
            writer.writeTo(out);
            System.out.println(out.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getOutputFilePath() {
        return this.config.getOutput() == null ? FilenameUtils.removeExtension(this.config.getTargetFile()).concat(".swf") : this.config.getOutput();
    }*/
}
