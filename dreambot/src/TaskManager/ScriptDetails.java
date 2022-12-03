package TaskManager;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.dreambot.api.script.Category;

@Retention(RUNTIME)
@Target(TYPE)
public abstract @interface ScriptDetails {
	public abstract String name();
    public abstract Category category();
    public abstract String author();
    public abstract double version();
    public abstract String description() default "";
    public abstract String image() default "";
}
