package top.mathcec.vertx2string.read1class;

import lombok.Getter;
import top.mathcec.vertx2string.pojo.reflect.MethodReflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ReflectMethodWithAnnotation {
    Class<? extends Annotation> annotation;
    Class<?> clz;
    @Getter
    List<MethodReflect> methodReflectList = new ArrayList<>();

    public ReflectMethodWithAnnotation(Class<? extends Annotation> annotation, Class<?> clz) {
        this.annotation = annotation;
        this.clz = clz;
        load();
    }

    private void load() {
        Method[] declaredMethods = clz.getDeclaredMethods();
        for (Method method : declaredMethods) {
            boolean annotationPresent = method.isAnnotationPresent(annotation);
            if (annotationPresent) {
                Annotation annotation = method.getAnnotation(this.annotation);
                methodReflectList.add(new MethodReflect(annotation, method));
            }
        }
    }


}
