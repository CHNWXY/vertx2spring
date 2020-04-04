package top.mathcec.vertx2string.test;

import lombok.Data;
import lombok.ToString;
import top.mathcec.vertx2string.annotation.http.valid.VertxMin;

@ToString
@Data
public class RealTimeReq {
    String a;
    @VertxMin(min = 100,errorMsg = "tai da le")
    int b;
}
