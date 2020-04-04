package top.mathcec.vertx2string.annotation.http.valid;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface VertxNotNull {
    String errorMsg() default "";
}
