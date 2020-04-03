package top.mathcec.vertx2string.annotation.http;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface VertxRestController {
    String value();
}
