import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class LaunchJavaC {
    public static void launchJavaCWithCompilerPlugin(Class clazz, String devFilename) throws IOException {
        URL location = clazz.getProtectionDomain().getCodeSource().getLocation();
        File classFolder = new File(location.getFile());
        File jar = new File(classFolder, "temp.jar");

        try (ZipOutputStream myjar = new ZipOutputStream(new FileOutputStream(jar))) {
            Files.walkFileTree(classFolder.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    URI relative = classFolder.toURI().relativize(file.toUri());
                    myjar.putNextEntry(new ZipEntry(relative.toString()));
                    Files.copy(file, myjar);
                    return FileVisitResult.CONTINUE;
                }
            });
        }

        Runtime runtime = Runtime.getRuntime();
        File projectFolder = classFolder.getParentFile().getParentFile();
        String file = String.join(File.separator, "src", "main", "java", devFilename);

        String[] cmdLine = {
            "javac",
            "-processorpath",
            jar.getAbsolutePath(),
            "-Xplugin:" + clazz.getSimpleName(),
            new File(projectFolder, file).getAbsolutePath(),
            "-d",
            classFolder.getAbsolutePath()
        };
        Process process = runtime.exec(cmdLine);
        System.err.println("command :: " + String.join(" ", Arrays.asList(cmdLine)));

        System.err.println("****************** START compiler ******************");
        try (InputStream is = process.getInputStream();
             OutputStream out = process.getOutputStream();
             InputStream err = process.getErrorStream()) {
            out.close();

            boolean inClosed = false;
            boolean errClosed = false;
            while (!inClosed || !errClosed) {
                int read = is.read();
                if (read == -1) inClosed = true;
                else System.out.write(read);

                read = err.read();
                if (read == -1) errClosed = true;
                else System.err.write(read);
            }
        }
        System.out.flush();
        System.err.flush();
        System.err.println("****************** END   compiler ******************");
        System.err.println(">> javac exited with value " + process.exitValue());
    }
}
