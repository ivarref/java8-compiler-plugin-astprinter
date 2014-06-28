import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.LineMap;
import com.sun.source.tree.Tree;
import com.sun.source.util.*;

import javax.tools.Diagnostic;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicInteger;

public class ASTPrinter implements Plugin {

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public void init(final JavacTask task, String... args) {

        task.addTaskListener(new TaskListener() {
            @Override
            public void started(TaskEvent event) {
            }

            @Override
            public void finished(TaskEvent event) {
                if (event.getKind() == TaskEvent.Kind.ANALYZE) {
                    CompilationUnitTree compilationUnit = event.getCompilationUnit();

                    try (FileOutputStream f = new FileOutputStream("javac.log", true)) {
                        fout = f;
                        log("********** start process compilation unit in file " + compilationUnit.getSourceFile().getName() + " **********");
                        Trees trees = Trees.instance(task);
                        trees.printMessage(Diagnostic.Kind.WARNING, "Hello world", compilationUnit, compilationUnit);
                        ASTPrinterVisitor astPrinterVisitor = new ASTPrinterVisitor(trees, compilationUnit);
                        astPrinterVisitor.scan(compilationUnit, null);
                        log(">>> Number of imports found: " + astPrinterVisitor.importCount.get());
                        log("********** end process compilation unit in file " + compilationUnit.getSourceFile().getName() + " **********");
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    FileOutputStream fout;

    public void log(String message) {
        try {
            fout.write((message + "\n").getBytes("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void log(String message, Throwable t) {
        try (StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(sw)) {
            t.printStackTrace(pw);
            log(message);
            log(sw.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public class ASTPrinterVisitor extends TreePathScanner<Void, Void> {

        private final SourcePositions sourcePositions;
        private final CompilationUnitTree cu;
        final AtomicInteger indent = new AtomicInteger(0);
        public final AtomicInteger importCount = new AtomicInteger(0);
        private final Trees trees;

        public ASTPrinterVisitor(Trees trees, CompilationUnitTree compilationUnit) {
            this.sourcePositions = trees.getSourcePositions();
            this.cu = compilationUnit;
            this.trees = trees;
        }

        @Override
        public Void scan(Tree tree, Void aVoid) {
            if (!(tree instanceof CompilationUnitTree)) indent.incrementAndGet();
            if (tree != null) logPosition(tree);
            Void result = super.scan(tree, aVoid);
            if (!(tree instanceof CompilationUnitTree)) indent.decrementAndGet();
            return result;
        }

        @Override
        public Void visitImport(ImportTree node, Void aVoid) {
            importCount.incrementAndGet();
            return super.visitImport(node, aVoid);
        }

        private void logPosition(Tree tree) {
            try {
                long startPos = sourcePositions.getStartPosition(cu, tree);
                LineMap linemap = cu.getLineMap();

                String className = tree.getClass().getName();
                if (tree.getClass().getInterfaces().length == 1) {
                    className = tree.getClass().getInterfaces()[0].getSimpleName();
                }

                if (startPos == Diagnostic.NOPOS) {
                    log(adjust(className + " at unknown position"));
                } else {
                    long lineStart = linemap.getLineNumber(startPos);
                    long colStart = linemap.getColumnNumber(startPos);
                    String endInfo = "";
                    String actualSource = "";
                    long endPos = sourcePositions.getEndPosition(cu, tree);
                    if (endPos != Diagnostic.NOPOS) {
                        long lineEnd = linemap.getLineNumber(endPos);
                        long colEnd = linemap.getColumnNumber(endPos);
                        endInfo = String.format(" -> [%d:%d]", lineEnd, colEnd);
                        actualSource = tree.toString().replaceAll("\n", "").replaceAll(" +", " ");
                        // Warning: This will strip whitespace inside strings as well.
                        if (actualSource.length() > 80) {
                            actualSource = actualSource.substring(0, 75) + "(...)";
                        }

                    }
                    String locationInfo = className + " [" + lineStart + ":" + colStart + "]" + endInfo;
                    String message = adjust(locationInfo) + actualSource;
                    log(message);
                    trees.printMessage(Diagnostic.Kind.WARNING, className, tree, cu);
                }
            } catch (Exception e) {
                log("Error", e);
            }
        }

        private String adjust(String s) {
            StringBuilder adjusted = new StringBuilder();
            for (int i = 0; i < indent.get(); i++) {
                adjusted.append("  ");
            }

            adjusted.append(s);
            while (adjusted.length() < 80) adjusted.append(' ');

            adjusted.append(" | ");
            return adjusted.toString();
        }
    }
}
