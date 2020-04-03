package top.mathcec.vertx2string.pojo;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class HttpServerConfig {
    int serverPort = 8080;
}
