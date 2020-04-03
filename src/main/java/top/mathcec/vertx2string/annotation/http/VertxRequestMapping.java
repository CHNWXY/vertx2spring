package top.mathcec.vertx2string.annotation.http;

import top.mathcec.vertx2string.constant.RequestMethod;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface VertxRequestMapping {
    String value() default "/";

    RequestMethod method() default RequestMethod.GET;
}
