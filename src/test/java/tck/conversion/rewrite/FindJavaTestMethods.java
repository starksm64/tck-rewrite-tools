package tck.conversion.rewrite;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.JavadocVisitor;
import org.openrewrite.java.tree.Javadoc;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FindJavaTestMethods extends Recipe {
    @Override
    public String getDisplayName() {
        return "find @testName tagged methods";
    }

    @Override
    public String getDescription() {
        return "Identifies JavaTest test methods from @testName tag";
    }

    @Override
    public Duration getEstimatedEffortPerOccurrence() {
        return Duration.ofMinutes(1);
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaVisitor<ExecutionContext>() {
            @Override
            protected JavadocVisitor<ExecutionContext> getJavadocVisitor() {
                return new JavadocVisitor<ExecutionContext>(this) {
                    @Override
                    public Javadoc visitDocComment(Javadoc.DocComment javadoc, ExecutionContext executionContext) {
                        Javadoc.DocComment dc = (Javadoc.DocComment) super.visitDocComment(javadoc, executionContext);
                        List<Javadoc> newBody = new ArrayList<>();
                        boolean isChanged = false;
                        boolean removeNextLineBreak = false;
                        for (int i = javadoc.getBody().size() - 1; i >= 0; i--) {
                            Javadoc doc = javadoc.getBody().get(i);
                            if (removeNextLineBreak) {
                                if (doc instanceof Javadoc.LineBreak) {
                                    removeNextLineBreak = false;
                                }
                            } else if (doc instanceof Javadoc.UnknownBlock) {
                                String tagName = ((Javadoc.UnknownBlock) doc).getName();
                                if(tagName.equals("testName")) {

                                }
                            } else {
                                newBody.add(doc);
                            }
                        }

                        if (isChanged) {
                            Collections.reverse(newBody);
                            dc = dc.withBody(newBody);
                        }
                        return dc;
                    }
                };
            }
        };
    }
}
