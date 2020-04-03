package top.mathcec.vertx2string.test;

import io.vertx.ext.web.RoutingContext;
import top.mathcec.vertx2string.annotation.http.VertxFilter;
import top.mathcec.vertx2string.filter.VertxBaseFilter;

import java.util.concurrent.Future;

@VertxFilter(name = "a", priority = 1)
public class Filter1 implements VertxBaseFilter {
    @Override
    public Future<? super Object> next(RoutingContext routingContext) {
        return null;
    }
}
