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
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

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

    /**
     * Test of a war deployment from the com.sun.ts.tests.servlet.api.jakarta_servlet.scinitializer.setsessiontrackingmodes
     * pkg.
     */
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

    /**
     * A test of a case where there is no deployment artifact, but there are JavaTest methods
     */
    @Test
    public void onlyAddTestAnnotation() {
        Logger.getLogger("onlyAddTestAnnotation").info("Start");

        rewriteRun(
                java(
                        """
                                    package com.sun.ts.tests.assembly.altDD;
                                    
                                    import java.util.Properties;
                                    
                                    import com.sun.javatest.Status;
                                    import com.sun.ts.lib.harness.EETest;
                                    import com.sun.ts.lib.util.TSNamingContext;
                                    import com.sun.ts.lib.util.TestUtil;
                                    
                                    public class Client extends EETest {
                                  
                                        /**
                                         * @testName: testAppClient
                                         *
                                         * @assertion_ids: JavaEE:SPEC:10260
                                         */
                                        public void testAppClient() throws Fault {
                                        }
                                    }
                                """,
                        """
                                    package com.sun.ts.tests.assembly.altDD;
                                    
                                    import java.util.Properties;
                                    
                                    import com.sun.javatest.Status;
                                    import com.sun.ts.lib.harness.EETest;
                                    import com.sun.ts.lib.util.TSNamingContext;
                                    import com.sun.ts.lib.util.TestUtil;
                                    import org.junit.jupiter.api.Test;
                                    
                                    public class Client extends EETest {
                                  
                                        /**
                                         * @testName: testAppClient
                                         *
                                         * @assertion_ids: JavaEE:SPEC:10260
                                         */
                                        @Test
                                        public void testAppClient() throws Fault {
                                        }
                                    }
                                """
                )
        );
        Logger.getLogger("onlyAddTestAnnotation").info("End");
    }
    /**
     * A test from the com.sun.ts.tests.servlet.api.jakarta_servlet_http.sessioncookieconfig pkg that has several
     * methods. The before and after source are read in from the LargeCaseBefore.java/LargeCaseAfter.java files
     *
     * @throws IOException
     */

    @Test
    public void testLargeCase() throws IOException {
        String className = "LargeCase";
        String pkg = "com.sun.ts.tests.servlet.api.jakarta_servlet_http.sessioncookieconfig";
        runTestFromSource(className, pkg);
    }

    @Test
    public void testClient() throws IOException {
        String className = "Client";
        String pkg = "com.sun.ts.tests.assembly.altDD";
        runTestFromSource(className, pkg);
    }

    private void runTestFromSource(String className, String pkg) throws IOException {
        // Assumes this is being run within the project, not as a bundled test artifact
        Path projectRoot = Paths.get("").toAbsolutePath();
        Path beforePath = projectRoot.resolve("src/test/java/tck/conversion/rewrite/"+className+"Before.java");
        String before = Files.readString(beforePath);
        before = before.replace(className+"Before", className)
                .replace("tck.conversion.rewrite", pkg);
        Path afterPath = projectRoot.resolve("src/test/java/tck/conversion/rewrite/"+className+"After.java");
        String after = Files.readString(afterPath);
        after = after.replace(className+"After", className)
                .replace("tck.conversion.rewrite", pkg);

        rewriteRun(java(before, after));
    }
}

