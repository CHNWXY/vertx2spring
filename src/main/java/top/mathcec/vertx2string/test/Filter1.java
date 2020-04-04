package top.mathcec.vertx2string.test;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.ext.web.RoutingContext;
import top.mathcec.vertx2string.annotation.http.VertxFilter;
import top.mathcec.vertx2string.filter.VertxBaseFilter;


@VertxFilter(priority = 1)
public class Filter1 implements VertxBaseFilter {
    @Override
    public Future<Void> next(RoutingContext routingContext) {
        return Future.future(Promise::complete);
    }
}
