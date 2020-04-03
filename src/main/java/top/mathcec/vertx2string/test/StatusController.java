package top.mathcec.vertx2string.test;

import io.vertx.core.http.HttpServerRequest;
import top.mathcec.vertx2string.annotation.http.VertxRequestBody;
import top.mathcec.vertx2string.annotation.http.VertxRequestHeader;
import top.mathcec.vertx2string.annotation.http.VertxRequestMapping;
import top.mathcec.vertx2string.annotation.http.VertxRestController;
import top.mathcec.vertx2string.constant.RequestMethod;

import java.util.HashMap;
import java.util.Map;

@VertxRestController("/status")
public class StatusController {
    @VertxRequestMapping(value = "/real_time", method = RequestMethod.POST)
    public Map<String, Object> realTimeStatus(HttpServerRequest request, @VertxRequestHeader("a") String a, @VertxRequestBody RealTimeReq realTimeReq) {
        Map<String, Object> map = new HashMap<>();
        map.put("currentThread", Thread.currentThread().getName());
        map.put("time", System.currentTimeMillis());
        map.put("path", request.path());
        map.put("req", realTimeReq);
        map.put("header-a", a);
        return map;
    }
}
