package top.mathcec.vertx2string.pojo.reflect;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@AllArgsConstructor
@Getter
@ToString
public class MethodReflect {
    private Annotation annotation;
    private Method method;
}
