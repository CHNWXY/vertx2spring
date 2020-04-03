package top.mathcec.vertx2string.annotation.http;

import java.lang.annotation.*;

@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface VertxGetValue {
    String value();
}
