package top.mathcec.vertx2string.pojo;

import lombok.Getter;
import lombok.ToString;

@ToString
public class ValidResult {
    Class<?> target;
    Class<?> param;
    boolean error;
    @Getter
    String msg;

    public boolean hasError(){
        return error;
    }

    public void error(Class<?> target, Class<?> param, String msg) {
        this.target = target;
        this.param = param;
        this.error = true;
        this.msg = msg;
    }
}
