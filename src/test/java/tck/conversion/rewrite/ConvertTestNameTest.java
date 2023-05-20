package tck.conversion.rewrite;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import tck.jakarta.platform.rewrite.ConvertJavaTestNameRecipe;

import static org.openrewrite.java.Assertions.java;

public class ConvertTestNameTest implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ConvertJavaTestNameRecipe());
    }

    @Test
    void addTestAnnotation() {
        rewriteRun(
                java(
                        """
                            package test.somepkg;
        
        
                            public class SomeTestClass {
  
                                /**
                                @testName: someTestMethod
                                */
                                public void someTestMethod() {
                                }
                            }
                        """,
                        """
                            package test.somepkg;
                            
                            import org.junit.jupiter.api.Test;
        
                            public class SomeTestClass {
  
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

