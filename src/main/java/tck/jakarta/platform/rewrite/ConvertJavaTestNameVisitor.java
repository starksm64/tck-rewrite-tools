package tck.jakarta.platform.rewrite;

import org.openrewrite.java.AnnotationMatcher;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.Comment;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Javadoc;
import org.openrewrite.java.tree.TextComment;

import java.util.Comparator;
import java.util.List;

/**
 * Find methods marked with a @testName javadoc comment and add a Junit5 @Test annotation
 * @param <ExecutionContext>
 */
public class ConvertJavaTestNameVisitor<ExecutionContext> extends JavaIsoVisitor<ExecutionContext> {
    private final AnnotationMatcher TEST_ANN_MATCH = new AnnotationMatcher("@org.junit.jupiter.api.Test");

    private final JavaTemplate testAnnotationTemplate =
            JavaTemplate.builder(this::getCursor, "@Test")
                    .javaParser(JavaParser.fromJavaVersion().classpath(JavaParser.runtimeClasspath()))
                    .imports("org.junit.jupiter.api.Test")
                    .build();

    @Override
    public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext ec) {
        String methodName = method.getSimpleName();
        if(method.getAllAnnotations().stream().anyMatch(TEST_ANN_MATCH::matches)) {
            System.out.printf("CJTN: Visting(%s) skipped due to @Test\n", methodName);
            return super.visitMethodDeclaration(method, ec);
        }

        method = super.visitMethodDeclaration(method, ec);
        List<Comment> comments = method.getComments();
        System.out.printf("CJTN: Visting(%s), comments=%d\n", methodName, comments.size());

        for(Comment c : comments) {
            if(c instanceof Javadoc.DocComment) {
                for(Javadoc jd : ((Javadoc.DocComment) c).getBody()) {
                    if(jd instanceof Javadoc.UnknownBlock) {
                        String name = ((Javadoc.UnknownBlock) jd).getName();
                        if(name.equals("testName:")) {
                            method = method.withTemplate(testAnnotationTemplate,
                                    method.getCoordinates().addAnnotation(Comparator.comparing(J.Annotation::getSimpleName)));
                            maybeAddImport("org.junit.jupiter.api.Test");
                            System.out.println("Added @Test annotation?\n"+method);
                        } else {
                            System.out.printf("Unknown block tag: %s\n", name);
                        }
                    }
                }
            } else if(c instanceof TextComment) {
                String text = ((TextComment)c).getText();
                int testNameIndex = text.indexOf("testName:");
                if(testNameIndex >= 0) {
                    String name = text.substring(testNameIndex+9).strip();
                    String[] parts = name.split("[\s\n\t]+");
                    method = method.withTemplate(testAnnotationTemplate,
                            method.getCoordinates().addAnnotation(Comparator.comparing(J.Annotation::getSimpleName)));
                    maybeAddImport("org.junit.jupiter.api.Test");
                    System.out.println("Added @Test annotation?\n"+method);
                }
            }
        }

        return method;
    }

}