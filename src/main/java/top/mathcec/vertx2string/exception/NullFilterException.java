package top.mathcec.vertx2string.exception;

import top.mathcec.vertx2string.filter.VertxBaseFilter;

public class NullFilterException extends Exception {
    public NullFilterException(VertxBaseFilter vertxBaseFilter) {
        super("类" + vertxBaseFilter.getClass() + "next为NULL");
    }
}
