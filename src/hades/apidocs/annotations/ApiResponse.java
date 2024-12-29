package hades.apidocs.annotations;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(ApiResponses.class)
public @interface ApiResponse {
    int code();
    String message();
}
