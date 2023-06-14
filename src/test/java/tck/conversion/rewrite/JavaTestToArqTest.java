package tck.conversion.rewrite;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import tck.jakarta.platform.rewrite.JavaTestToArquillianShrinkwrap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.openrewrite.java.Assertions.java;

class JavaTestToArqTest implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec
                .recipe(new JavaTestToArquillianShrinkwrap())
                .parser(JavaParser.fromJavaVersion()
                        .classpath(JavaParser.runtimeClasspath())
                )
        ;
    }

    @Test
    void addDeploymentMethod() {
        rewriteRun(
                java(
                        """
                                    package com.sun.ts.tests.servlet.api.jakarta_servlet.scinitializer.setsessiontrackingmodes;
                                    
                                    import com.sun.ts.tests.servlet.common.client.AbstractUrlClient;
                                    
                                    public class SomeTestClass extends AbstractUrlClient {
                                  
                                        /**
                                        @testName: someTestMethod
                                        */
                                        public void someTestMethod() {
                                        }
                                    }
                                """,
                        """
                                    package com.sun.ts.tests.servlet.api.jakarta_servlet.scinitializer.setsessiontrackingmodes;
                                    
                                    import com.sun.ts.tests.servlet.common.client.AbstractUrlClient;
                                    import jakartatck.jar2shrinkwrap.LibraryUtil;
                                    import org.jboss.arquillian.container.test.api.Deployment;
                                    import org.jboss.shrinkwrap.api.ShrinkWrap;
                                    import org.jboss.shrinkwrap.api.spec.JavaArchive;
                                    import org.jboss.shrinkwrap.api.spec.WebArchive;
                                    import org.junit.jupiter.api.Test;

                                    import java.util.List;
                                    
                                    public class SomeTestClass extends AbstractUrlClient {
                                    
                                        @Deployment(testable = false)
                                        public static WebArchive getTestArchive() throws Exception {
                                            // TODO, check the library jar classes
                                        
                                    /*
                                            WEB-INF/lib/initilizer.jar
                                                /META-INF/MANIFEST.MF
                                                /com/sun/ts/tests/servlet/api/jakarta_servlet/scinitializer/setsessiontrackingmodes/TCKServletContainerInitializer.class
                                                /META-INF/services/jakarta.servlet.ServletContainerInitializer
                                    */
                                            List<JavaArchive> warJars = LibraryUtil.getJars(SomeTestClass.class);

                                            return ShrinkWrap.create(WebArchive.class, "Client.war")
                                                    .addAsLibraries(warJars)
                                                    .addClass(com.sun.ts.tests.servlet.api.jakarta_servlet.scinitializer.setsessiontrackingmodes.TCKServletContainerInitializer.class)
                                                    .addClass(com.sun.ts.tests.servlet.api.jakarta_servlet.scinitializer.setsessiontrackingmodes.TestListener.class)
                                                    .addClass(com.sun.ts.tests.servlet.api.jakarta_servlet.scinitializer.setsessiontrackingmodes.TestServlet.class)
                                                    .addClass(com.sun.ts.tests.servlet.common.servlets.GenericTCKServlet.class)
                                                    .addClass(com.sun.ts.tests.servlet.common.util.Data.class)
                                                    .addClass(com.sun.ts.tests.servlet.common.util.ServletTestUtil.class)
                                                    .addAsWebInfResource("web.xml");
                                        }
                                  
                                        /**
                                        @testName: someTestMethod
                                        */
                                        @Test
                                        public void someTestMethod() {
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    public void testLargeCase() throws IOException {
        // Assumes this is being run within the project, not as a bundled test artifact
        Path projectRoot = Paths.get("").toAbsolutePath();
        Path beforePath = projectRoot.resolve("src/test/java/tck/conversion/rewrite/LargeCaseBefore.java");
        String before = Files.readString(beforePath);
        before = before.replace("LargeCaseBefore", "LargeCase")
                .replace("tck.conversion.rewrite", "com.sun.ts.tests.servlet.api.jakarta_servlet_http.sessioncookieconfig");
        Path afterPath = projectRoot.resolve("src/test/java/tck/conversion/rewrite/LargeCaseAfter.java");
        String after = Files.readString(afterPath);
        after = after.replace("LargeCaseAfter", "LargeCase")
                .replace("tck.conversion.rewrite", "com.sun.ts.tests.servlet.api.jakarta_servlet_http.sessioncookieconfig");

        rewriteRun(java(before, after));
    }
}

