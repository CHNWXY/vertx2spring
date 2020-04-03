package top.mathcec.vertx2string.pojo.reflect;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

@AllArgsConstructor
@Getter
@ToString
public class ParameterReflect {
    Parameter parameter;
    //这个地方可能为空要注意判断
    Annotation annotation;
}
