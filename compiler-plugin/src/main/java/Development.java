import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Development {

    public static void main(String[] args) throws IOException {
        ZipInputStream zin2 = new SubZipStream(null);

        try (ZipInputStream zin3 = new SubZipStream(null)) {
        }

        ZipOutputStream zout = new ZipOutputStream(null);

        InputStream is = (Math.random() > 0.5) ? new ZipInputStream(null) : new FileInputStream("hello.txt");
    }

    public static class SubZipStream extends ZipInputStream {
        public SubZipStream(InputStream in) {
            super(in);
        }

        public SubZipStream(InputStream in, Charset charset) {
            super(in, charset);
        }
    }

}
