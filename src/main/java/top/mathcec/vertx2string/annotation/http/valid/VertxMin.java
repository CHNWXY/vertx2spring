package top.mathcec.vertx2string.annotation.http.valid;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface VertxMin {
    //数字  大小；字符串 是最小长度；
    double min() default Long.MIN_VALUE;

    String errorMsg() default "";
}
