package top.mathcec.vertx2string.annotation.http;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface VertxRequestHeader {
    String value() default "";
}
