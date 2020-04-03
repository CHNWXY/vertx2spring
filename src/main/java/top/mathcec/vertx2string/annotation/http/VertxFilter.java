package top.mathcec.vertx2string.annotation.http;

import java.lang.annotation.*;

/**
 * 加在类上，类必须实现接口
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface VertxFilter {
    //优先级，如果优先级一样，那么随机，数字越小，优先级越高
    int priority() default Integer.MIN_VALUE;

    String name() default "FILTER";
}
