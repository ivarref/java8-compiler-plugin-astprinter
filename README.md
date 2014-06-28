Java 8 compiler plugin: AST printer
===================================

Those wild at heart may want to have a look at CheckTryWithResourcesOnZipStreams.java.

Example usage:

    (cd compiler-plugin/ ; mvn clean package) && (cd plugin-user/ ; rm javac.log ; mvn clean compile; cat javac.log)
    
Example input (in plugin-user project):
    
    import java.io.IOException;
    import java.util.zip.ZipInputStream;
    
    public class Hello {
        public static void main(String[] args) throws IOException {
            System.err.println("hello world");
            try (ZipInputStream zin = new ZipInputStream(null)) {
    
            }
        }
    }

Output:

    ********** start process compilation unit in file .../src/main/java/Hello.java **********
    CompilationUnitTree [1:1] -> [11:2]                                              | import java.io.IOException;import java.util.zip.ZipInputStream;public class(...)
      ImportTree [1:1] -> [1:28]                                                     | import java.io.IOException;
        MemberSelectTree [1:8] -> [1:27]                                             | java.io.IOException
          MemberSelectTree [1:8] -> [1:15]                                           | java.io
            IdentifierTree [1:8] -> [1:12]                                           | java
      ImportTree [2:1] -> [2:37]                                                     | import java.util.zip.ZipInputStream;
        MemberSelectTree [2:8] -> [2:36]                                             | java.util.zip.ZipInputStream
          MemberSelectTree [2:8] -> [2:21]                                           | java.util.zip
            MemberSelectTree [2:8] -> [2:17]                                         | java.util
              IdentifierTree [2:8] -> [2:12]                                         | java
      ClassTree [4:1] -> [11:2]                                                      | public class Hello { public Hello() { super(); } public static void main(St(...)
        ModifiersTree [4:1] -> [4:7]                                                 | public 
        MethodTree [4:8]                                                             | 
          ModifiersTree [4:8]                                                        | 
          BlockTree [4:8]                                                            | 
            ExpressionStatementTree [4:8]                                            | 
              MethodInvocationTree [4:8]                                             | 
                IdentifierTree [4:8]                                                 | 
        MethodTree [5:5] -> [10:6]                                                   | public static void main(String[] args) throws IOException { System.err.prin(...)
          ModifiersTree [5:5] -> [5:18]                                              | public static 
          PrimitiveTypeTree [5:19] -> [5:23]                                         | void
          VariableTree [5:29] -> [5:42]                                              | String[] args
            ModifiersTree at unknown position                                        | 
            ArrayTypeTree [5:29] -> [5:37]                                           | String[]
              IdentifierTree [5:29] -> [5:35]                                        | String
          IdentifierTree [5:51] -> [5:62]                                            | IOException
          BlockTree [5:63] -> [10:6]                                                 | { System.err.println("hello world"); try (final ZipInputStream zin = new Zi(...)
            ExpressionStatementTree [6:9] -> [6:43]                                  | System.err.println("hello world");
              MethodInvocationTree [6:9] -> [6:42]                                   | System.err.println("hello world")
                MemberSelectTree [6:9] -> [6:27]                                     | System.err.println
                  MemberSelectTree [6:9] -> [6:19]                                   | System.err
                    IdentifierTree [6:9] -> [6:15]                                   | System
                LiteralTree [6:28] -> [6:41]                                         | "hello world"
            TryTree [7:9] -> [9:10]                                                  | try (final ZipInputStream zin = new ZipInputStream(null);) {}
              VariableTree [7:14] -> [7:59]                                          | final ZipInputStream zin = new ZipInputStream(null)
                ModifiersTree at unknown position                                    | 
                IdentifierTree [7:14] -> [7:28]                                      | ZipInputStream
                NewClassTree [7:35] -> [7:59]                                        | new ZipInputStream(null)
                  IdentifierTree [7:39] -> [7:53]                                    | ZipInputStream
                  LiteralTree [7:54] -> [7:58]                                       | null
              BlockTree [7:61] -> [9:10]                                             | {}
    >>> Number of imports found: 2
    ********** end process compilation unit in file .../src/main/java/Hello.java **********


[More information on java 8 compiler plugins](http://techbitsfromsridhar.blogspot.no/2013/02/java-compiler-plug-ins-in-java-8-use.html)

