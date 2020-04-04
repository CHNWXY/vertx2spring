package top.mathcec.vertx2string.filter;

import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;


public interface VertxBaseFilter {
    Future<Void> next(RoutingContext routingContext);
}
