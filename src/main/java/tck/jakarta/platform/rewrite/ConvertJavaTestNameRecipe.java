package tck.jakarta.platform.rewrite;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.java.JavaIsoVisitor;
import java.time.Duration;

public class ConvertJavaTestNameRecipe extends Recipe {

    @Override
    public String getDisplayName() {
        return "Convert JavaTest @testName to @Test";
    }

    @Override
    public String getDescription() {
        return "Expand the `CustomerInfo` class with new fields.";
    }

    @Override
    public Duration getEstimatedEffortPerOccurrence() {
        return Duration.ofMinutes(5);
    }

    @Override
    protected JavaIsoVisitor<ExecutionContext> getVisitor() {
        return new ConvertJavaTestNameVisitor<ExecutionContext>();
    }
}
