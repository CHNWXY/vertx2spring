package top.mathcec.vertx2string.annotation.http;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.PARAMETER)
public @interface VertxRequestBody {

    //是否需要进行前置校验
    boolean needValid() default false;
}
