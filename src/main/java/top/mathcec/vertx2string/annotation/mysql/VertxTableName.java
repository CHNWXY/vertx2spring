package top.mathcec.vertx2string.annotation.mysql;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface VertxTableName {
    String tableName();
}
