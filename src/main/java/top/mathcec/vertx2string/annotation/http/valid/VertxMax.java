package top.mathcec.vertx2string.annotation.http.valid;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface VertxMax {
    //数字 最大值   字符串最大长度
    double max() default Long.MAX_VALUE;
    String errorMsg() default "";
}
