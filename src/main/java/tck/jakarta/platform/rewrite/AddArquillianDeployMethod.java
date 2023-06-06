package tck.jakarta.platform.rewrite;

import jakartatck.jar2shrinkwrap.Jar2ShrinkWrap;
import jakartatck.jar2shrinkwrap.JarProcessor;
import org.openrewrite.java.AnnotationMatcher;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.TreeVisitingPrinter;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

import java.io.StringWriter;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * If a class is a non-abstrct extension of com.sun.ts.lib.harness.EETest and it does not
 * already have an Arquillian @Deployment method, add one based on the war2jartool
 *
 * @param <ExecutionContext>
 */
public class AddArquillianDeployMethod<ExecutionContext> extends JavaIsoVisitor<ExecutionContext> {
    private final AnnotationMatcher TEST_ANN_MATCH = new AnnotationMatcher("@org.jboss.arquillian.container.test.api.Deployment");

    @Override
    public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext executionContext) {
        System.out.println(TreeVisitingPrinter.printTree(getCursor()));

        J.ClassDeclaration cd = super.visitClassDeclaration(classDecl, executionContext);
        Set<String> parentTypes = new HashSet<>();
        JavaType.FullyQualified currentFq = cd.getType();

        while (currentFq != null) {
            parentTypes.add(currentFq.getFullyQualifiedName());
            for (JavaType.FullyQualified i : currentFq.getInterfaces()) {
                parentTypes.add(i.getFullyQualifiedName());
            }
            currentFq = currentFq.getSupertype();
            if (currentFq != null && parentTypes.contains(currentFq.getFullyQualifiedName())) {
                break;
            }
        }

        boolean isEETest = parentTypes.contains("com.sun.ts.lib.harness.EETest");
        List<J.Modifier> modifiers = classDecl.getModifiers();
        boolean isAbstract = modifiers.stream().anyMatch(modifier -> modifier.getType() == J.Modifier.Type.Abstract);
        System.out.printf("%s isEETest=%s, isAbstract=%s\n", cd.getType().getClassName(), isEETest, isAbstract);

        // Check if the class already has a method marked with @Deployment
        boolean deploymentMethodExists = classDecl.getBody().getStatements().stream()
                .filter(statement -> statement instanceof J.MethodDeclaration)
                .map(J.MethodDeclaration.class::cast)
                .anyMatch(methodDeclaration -> methodDeclaration.getAllAnnotations().stream().anyMatch(TEST_ANN_MATCH::matches));
        // If the class already has a `@Deployment *()` method, don't make any changes to it.
        if (deploymentMethodExists) {
            System.out.println("@Deployment annotated method exists, return existing class def");
            return cd;
        }

        // If this is a concrete subclass of EETest, add an arq deployment method
        if(!isAbstract && isEETest) {
            String pkg = cd.getType().getPackageName();
            try {
                JarProcessor war = Jar2ShrinkWrap.fromPackage(pkg);
                StringWriter methodCodeWriter = new StringWriter();
                war.saveOutput(methodCodeWriter, false);
                String methodCode = methodCodeWriter.toString();
                if (methodCode.length() == 0) {
                    System.out.printf("No code generated for package: " + pkg);
                    return cd;
                }

                JavaTemplate deploymentTemplate =
                        JavaTemplate.builder(this::getCursor, methodCode)
                                .javaParser(JavaParser.fromJavaVersion().classpath(JavaParser.runtimeClasspath()))
                                .imports("org.jboss.arquillian.container.test.api.Deployment",
                                        "org.jboss.shrinkwrap.api.ShrinkWrap",
                                        "org.jboss.shrinkwrap.api.spec.WebArchive",
                                        "org.jboss.shrinkwrap.api.spec.JavaArchive",
                                        "jakartatck.jar2shrinkwrap.LibraryUtil",
                                        "java.util.List"
                                )
                                .build();

                String dotClassRef = classDecl.getType().getClassName()+".class";
                cd = classDecl.withBody(
                        classDecl.getBody().withTemplate(
                                deploymentTemplate,
                                classDecl.getBody().getCoordinates().firstStatement(),
                                dotClassRef
                        ));
                maybeAddImport("org.jboss.arquillian.container.test.api.Deployment");
                maybeAddImport("org.jboss.shrinkwrap.api.ShrinkWrap");
                maybeAddImport("org.jboss.shrinkwrap.api.spec.JavaArchive");
                maybeAddImport("org.jboss.shrinkwrap.api.spec.WebArchive");
                maybeAddImport("jakartatck.jar2shrinkwrap.LibraryUtil");
                maybeAddImport("java.util.List");
            } catch (RuntimeException e) {
                System.out.printf("No code generated for package: " + pkg);
                return cd;
            }

        }
        return cd;
    }

}