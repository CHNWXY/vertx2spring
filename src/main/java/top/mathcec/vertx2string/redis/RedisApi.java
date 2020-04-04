package top.mathcec.vertx2string.redis;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class RedisApi {
    private RedisAPI redisAPI;

    public RedisApi(RedisAPI redisAPI) {
        this.redisAPI = redisAPI;
    }

    public Future<Void> setObject(String key, Object o) {
        return Future.future(p -> {
            JsonObject object = JsonObject.mapFrom(o);
            List<String> list = new ArrayList<>();
            list.add(key);
            object.forEach(obj -> {
                String key1 = obj.getKey();
                Object value = obj.getValue();
                list.add(key1);
                list.add(value.toString());
            });
            redisAPI.hmset(list, h -> {
                if (h.succeeded()) {
                    p.complete();
                } else {
                    p.fail(h.cause());
                }
            });
        });
    }


    public Future<Void> setObject(String key, Object o, long seconds) {
        return Future.future(p -> {
            setObject(key, o).onSuccess(s -> {
                expire(key, seconds).onSuccess(s1 -> {
                    p.complete();
                }).onFailure(p::fail);
            }).onFailure(p::fail);
        });
    }

    public <T> Future<T> getObject(String key, Class<T> type) {
        return Future.future(p -> {
            redisAPI.hgetall(key, h -> {
                if (h.succeeded()) {
                    Response result = h.result();
                    if (result == null) {
                        p.complete(null);
                        return;
                    }
                    Set<String> keys = result.getKeys();
                    JsonObject object = new JsonObject();
                    for (String fieldKey :
                            keys) {
                        object.put(fieldKey, result.get(fieldKey).toBuffer());
                    }
                    p.complete(object.mapTo(type));
                } else {
                    p.fail(h.cause());
                }
            });
        });
    }

    public Future<Void> expire(String key, long seconds) {
        return Future.future(p -> {
            redisAPI.expire(key, seconds + "", h -> {
                if (h.succeeded()) {
                    p.complete();
                } else {
                    p.fail(h.cause());
                }
            });
        });
    }
}
