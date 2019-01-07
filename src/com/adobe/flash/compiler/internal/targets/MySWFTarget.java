package com.adobe.flash.compiler.internal.targets;

import com.adobe.flash.abc.ABCLinker;
import com.adobe.flash.compiler.exceptions.BuildCanceledException;
import com.adobe.flash.compiler.internal.projects.CompilerProject;
import com.adobe.flash.compiler.internal.targets.AppSWFTarget;
import com.adobe.flash.compiler.internal.targets.ITargetAttributes;
import com.adobe.flash.compiler.internal.targets.SWFTarget;
import com.adobe.flash.compiler.problems.ICompilerProblem;
import com.adobe.flash.compiler.targets.ITargetProgressMonitor;
import com.adobe.flash.compiler.targets.ITargetSettings;
import com.adobe.flash.compiler.units.ICompilationUnit;
import com.adobe.flash.swf.ISWF;
import com.adobe.flash.swf.SWF;
import com.adobe.flash.swf.SWFFrame;
import com.adobe.flash.swf.tags.DoABCTag;
import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * created by lizhi
 */
public class MySWFTarget extends AppSWFTarget {
    public MySWFTarget(CompilerProject project, ITargetSettings targetSettings, ITargetProgressMonitor progressMonitor) {
        super(project, targetSettings, progressMonitor);
    }

    public ISWF build(Collection<ICompilerProblem> problems) {
        this.buildStarted();

        HashSet compilationUnitSet;
        try {
            Iterable<ICompilerProblem> fatalProblems = this.getFatalProblems();
            if (!Iterables.isEmpty(fatalProblems)) {
                Iterables.addAll(problems, fatalProblems);
                compilationUnitSet = null;
                return null;
            }

            compilationUnitSet = new HashSet();
            RootedCompilationUnits rootedCompilationUnits = this.getRootedCompilationUnits();
            if (!rootedCompilationUnits.getUnits().isEmpty()) {
                compilationUnitSet.addAll(rootedCompilationUnits.getUnits());
                //this.problemCollection = problems;
                SWFTarget.FramesInformation frames = this.getFramesInformation();
                BuiltCompilationUnitSet builtCompilationUnits = this.getBuiltCompilationUnitSet();
                Iterables.addAll(problems, builtCompilationUnits.problems);
                this.doPostBuildWork(builtCompilationUnits.compilationUnits, problems);
                ISWF swf = this.initializeSWF(this.getReachableCompilationUnitsInSWFOrder(rootedCompilationUnits.getUnits()));
                Set<ICompilationUnit> emittedCompilationUnits = new HashSet();
                frames.createFrames(this, swf, builtCompilationUnits.compilationUnits, emittedCompilationUnits, problems);
                this.createLinkReport(problems);
                ISWF var9 = this.linkSWF(swf);
                return var9;
            }

            SWF var5 = this.buildEmptySWF();
            return var5;
        } catch (BuildCanceledException var14) {
            compilationUnitSet = null;
        } catch (InterruptedException var15) {
            compilationUnitSet = null;
            return null;
        } finally {
            this.buildFinished();
        }

        return null;
    }
}
