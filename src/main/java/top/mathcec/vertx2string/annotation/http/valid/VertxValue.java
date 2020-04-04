package top.mathcec.vertx2string.annotation.http.valid;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface VertxValue {
    //数字就是大小 字符串就是字符
    String[] value() default {};

    String errorMsg() default "";
}
