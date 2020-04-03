package top.mathcec.vertx2string.pojo;

import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;
import lombok.Data;

@Data
public class ConfigPojo {
    VertxOptions vertxOptions;
    HttpServerOptions httpServerOptions;
    HttpServerConfig httpServerConfig;
}
