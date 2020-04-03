package top.mathcec.vertx2string.exception;

public class AutowiredParameterException extends Exception {
    public AutowiredParameterException(Class<?> type) {
        super("不存在注入类型 + " + type.getTypeName());
    }
}
