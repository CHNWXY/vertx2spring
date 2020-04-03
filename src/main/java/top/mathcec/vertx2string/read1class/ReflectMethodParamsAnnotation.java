package top.mathcec.vertx2string.read1class;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import top.mathcec.vertx2string.pojo.reflect.ParameterReflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReflectMethodParamsAnnotation {
    Method method;
    List<Class<? extends Annotation>> annotationList = new ArrayList<>();
    @Getter
    List<ParameterReflect> parameterReflectArrayList = new ArrayList<>();

    public ReflectMethodParamsAnnotation(Method method, List<Class<? extends Annotation>> annotations) {
        annotationList.addAll(annotations);
        this.method = method;
        load();
    }

    public ReflectMethodParamsAnnotation(Method method, Class<? extends Annotation> annotation) {
        this.method = method;
        annotationList.add(annotation);
        load();
    }

    private void load() {
        Parameter[] parameters = method.getParameters();
        for (Parameter parameter : parameters) {
            boolean hasAnn = false;
            Annotation[] annotations = parameter.getDeclaredAnnotations();
            for (int i = 0; i < annotationList.size(); i++) {
                Class<? extends Annotation> annotationClass = annotationList.get(i);
                for (int j = 0; j < annotations.length; j++) {
                    hasAnn = true;
                    if (annotationClass == annotations[j].annotationType()) {
                        parameterReflectArrayList.add(new ParameterReflect(parameter, parameter.getAnnotation(annotationClass)));
                    }
                }
            }
            if (!hasAnn) {
                parameterReflectArrayList.add(new ParameterReflect(parameter, null));
            }
        }
    }
}
