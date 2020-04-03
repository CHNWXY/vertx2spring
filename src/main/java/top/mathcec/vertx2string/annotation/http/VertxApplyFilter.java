package top.mathcec.vertx2string.annotation.http;

import java.lang.annotation.*;
import java.util.Arrays;

@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface VertxApplyFilter {
    String filter() default "FILTER";
}
