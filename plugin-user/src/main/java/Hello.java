import java.io.IOException;
import java.util.zip.ZipInputStream;

public class Hello {
    public static void main(String[] args) throws IOException {
        System.err.println("hello world");
        try (ZipInputStream zin = new ZipInputStream(null)) {
        }

    }
}
