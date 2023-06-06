package tck.conversion.rewrite;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import tck.jakarta.platform.rewrite.JavaTestToArquillianShrinkwrap;

import static org.openrewrite.java.Assertions.java;

class JavaTestToArqTest implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec
                .recipe(new JavaTestToArquillianShrinkwrap())
                .parser(JavaParser.fromJavaVersion()
                        .classpath(JavaParser.runtimeClasspath())
                );
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
}

