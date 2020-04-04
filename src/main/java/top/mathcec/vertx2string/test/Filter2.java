package top.mathcec.vertx2string.test;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.ext.web.RoutingContext;
import top.mathcec.vertx2string.annotation.http.VertxFilter;
import top.mathcec.vertx2string.filter.VertxBaseFilter;

@VertxFilter(priority = 2)
public class Filter2 implements VertxBaseFilter {
    @Override
    public Future<Void> next(RoutingContext routingContext) {
        //            routingContext.response().end("234");
        //            p.fail("123");
        return Future.future(Promise::complete);
    }
}
