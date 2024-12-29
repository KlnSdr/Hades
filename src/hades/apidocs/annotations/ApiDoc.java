package hades.apidocs.annotations;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ApiDoc {
    String summary() default "";
    String description() default "";
    String baseUrl() default "";
}
