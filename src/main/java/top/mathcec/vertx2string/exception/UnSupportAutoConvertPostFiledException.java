package top.mathcec.vertx2string.exception;

import java.lang.reflect.Field;

public class UnSupportAutoConvertPostFiledException extends Exception {
    public UnSupportAutoConvertPostFiledException(Class<?> clz) {
        super("UnSupport FiledType " + clz.getName());
    }
}
