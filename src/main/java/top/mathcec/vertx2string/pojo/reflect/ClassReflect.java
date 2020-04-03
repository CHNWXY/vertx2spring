package top.mathcec.vertx2string.pojo.reflect;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.lang.annotation.Annotation;

@AllArgsConstructor
@Getter
@ToString
public class ClassReflect {
    private Class<?> clz;
    private Annotation annotation;
}
