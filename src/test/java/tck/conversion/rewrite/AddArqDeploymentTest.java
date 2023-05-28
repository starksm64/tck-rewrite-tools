package tck.conversion.rewrite;

import org.junit.jupiter.api.Test;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import tck.jakarta.platform.rewrite.AddArquillianDeployMethod;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.openrewrite.java.Assertions.java;

class LocalRecipe extends Recipe {
    @Override
    public String getDisplayName() {
        return "Add missing @Deployment";
    }

    @Override
    public String getDescription() {
        return getDisplayName() + ".";
    }

    @Override
    protected JavaIsoVisitor<ExecutionContext> getVisitor() {
        return new AddArquillianDeployMethod<>();
    }
}

public class AddArqDeploymentTest implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        Path testClasses = Paths.get("/Users/starksm/Dev/Jakarta/tck-rewrite-tools/target", "test-classes");

        spec
                .recipe(new LocalRecipe())
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
                                        public void someTestMethod() {
                                        }
                                    }
                                """
                )
        );
    }
}

