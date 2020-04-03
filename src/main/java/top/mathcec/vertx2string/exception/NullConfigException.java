package top.mathcec.vertx2string.exception;

public class NullConfigException extends Exception{
    public NullConfigException() {
    }

    public NullConfigException(String message) {
        super(message);
    }

    public NullConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public NullConfigException(Throwable cause) {
        super(cause);
    }

    public NullConfigException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
