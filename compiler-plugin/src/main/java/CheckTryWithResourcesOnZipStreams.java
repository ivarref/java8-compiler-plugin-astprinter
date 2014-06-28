import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.TryTree;
import com.sun.source.util.*;
import com.sun.tools.javac.tree.JCTree;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class CheckTryWithResourcesOnZipStreams implements Plugin {

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    public final static String devFilename = "Development.java";

    public static void main(String[] args) throws IOException {
        LaunchJavaC.launchJavaCWithCompilerPlugin(CheckTryWithResourcesOnZipStreams.class, devFilename);
    }

    @Override
    public void init(JavacTask task, String... args) {
        task.addTaskListener(new TaskListener() {
            @Override
            public void started(TaskEvent e) {

            }

            @Override
            public void finished(TaskEvent e) {
                if (e.getKind() == TaskEvent.Kind.ANALYZE) {
                    CompilationUnitTree compilationUnit = e.getCompilationUnit();
                    boolean devMode = getClass().getProtectionDomain().getCodeSource().toString().contains("temp.jar");
                    boolean runOnFile = devMode ? e.getSourceFile().getName().endsWith(devFilename) : true;
                    if (runOnFile) {
                        try {
                            new Visitor(task, compilationUnit).scan(compilationUnit, null);
                        } catch (Throwable t) {
                            t.printStackTrace();
                            System.err.println(t.getMessage());
                        }
                    }
                }
            }
        });
    }

    public class Visitor extends TreePathScanner<Void, Void> {

        private final SourcePositions sourcePositions;
        private final CompilationUnitTree cu;
        private final Elements elements;
        private final Types types;
        private final Trees trees;

        public Visitor(JavacTask task, CompilationUnitTree compilationUnit) {
            this.trees = Trees.instance(task);
            this.sourcePositions = trees.getSourcePositions();
            this.elements = task.getElements();
            this.types = task.getTypes();
            this.cu = compilationUnit;
        }

        boolean insideTryWith = false;

        @Override
        public Void visitCompilationUnit(CompilationUnitTree node, Void aVoid) {
            System.err.println("processing file " + node.getSourceFile().getName());
            return super.visitCompilationUnit(node, aVoid);
        }

        @Override
        public Void visitTry(TryTree node, Void aVoid) {
            insideTryWith = true;
            Void result = super.visitTry(node, aVoid);
            insideTryWith = false;
            return result;
        }

        @Override
        public Void visitBlock(BlockTree node, Void aVoid) {
            insideTryWith = false;
            return super.visitBlock(node, aVoid);
        }

        @Override
        public Void visitNewClass(NewClassTree node, Void aVoid) {
            if (!insideTryWith) {
                if (node.getIdentifier() instanceof JCTree.JCIdent) {
                    TypeMirror zin = elements.getTypeElement(ZipInputStream.class.getCanonicalName()).asType();
                    TypeMirror zout = elements.getTypeElement(ZipOutputStream.class.getCanonicalName()).asType();

                    TypeMirror clazz = trees.getTypeMirror(TreePath.getPath(cu, node.getIdentifier()));
                    boolean isZipStream = types.isAssignable(types.erasure(clazz), types.erasure(zin))
                        || types.isAssignable(types.erasure(clazz), types.erasure(zout));
                    if (isZipStream) {
                        trees.printMessage(Diagnostic.Kind.WARNING, "Using Zip(Input|Output)Stream, but not inside with-part of try-with-resources statement.", node, cu);
                    }
                }
            }
            return super.visitNewClass(node, aVoid);
        }
    }
}
