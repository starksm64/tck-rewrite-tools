package tck.jakarta.platform.rewrite;

import org.openrewrite.java.AnnotationMatcher;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.Comment;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Javadoc;

import java.util.Comparator;

/**
 * Find methods marked with a @testName javadoc comment and add a Junit5 @Test annotation
 * @param <ExecutionContext>
 */
public class ConvertJavaTestNameVisitor<ExecutionContext> extends JavaIsoVisitor<ExecutionContext> {
    private final AnnotationMatcher TEST_ANN_MATCH = new AnnotationMatcher("@org.junit.jupiter.api.Test");

    private final JavaTemplate testAnnotationTemplate =
            JavaTemplate.builder(this::getCursor, "@Test")
                    .imports("org.junit.jupiter.api.Test")
                    .build();


    public ConvertJavaTestNameVisitor() {
    }

    @Override
    public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext ec) {
        String methodName = method.getSimpleName();
        if(method.getAllAnnotations().stream().anyMatch(TEST_ANN_MATCH::matches)) {
            System.out.printf("Visting(%s) skipped due to @Test\n", methodName);
            return super.visitMethodDeclaration(method, ec);
        }
        System.out.printf("Visting(%s)\n", methodName);
        for(Comment c : method.getComments()) {
            if(c instanceof Javadoc.DocComment) {
                for(Javadoc jd : ((Javadoc.DocComment) c).getBody()) {
                    if(jd instanceof Javadoc.UnknownBlock) {
                        String name = ((Javadoc.UnknownBlock) jd).getName();
                        System.out.printf("Found unknown block tag: %s\n", name);
                        if(name.equals("testName:")) {
                            method = method.withTemplate(testAnnotationTemplate,
                                    method.getCoordinates().addAnnotation(Comparator.comparing(J.Annotation::getSimpleName)));
                            System.out.println("Added @Test annotation?\n"+method);
                        }
                    }
                }
            }
        }

        return super.visitMethodDeclaration(method, ec);
    }

}