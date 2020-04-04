package top.mathcec.vertx2string.test;

import io.vertx.core.http.HttpServerRequest;
import top.mathcec.vertx2string.annotation.http.*;
import top.mathcec.vertx2string.constant.RequestMethod;
import top.mathcec.vertx2string.pojo.ValidResult;

import java.util.HashMap;
import java.util.Map;

@VertxRestController("/status")
public class StatusController {
    @VertxRequestMapping(value = "/real_time", method = RequestMethod.POST)
    @VertxApplyFilter
    public Map<String, Object> realTimeStatus(ValidResult validResult, HttpServerRequest request, @VertxRequestHeader("a") String a, @VertxRequestBody RealTimeReq realTimeReq) {
        System.out.println(validResult);
        Map<String, Object> map = new HashMap<>();
        map.put("currentThread", Thread.currentThread().getName());
        map.put("time", System.currentTimeMillis());
        map.put("path", request.path());
        map.put("req", realTimeReq);
        map.put("header-a", a);
        return map;
    }
}
