package top.mathcec.vertx2string.config;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import lombok.Setter;
import lombok.experimental.Accessors;
import top.mathcec.vertx2string.annotation.http.*;
import top.mathcec.vertx2string.annotation.http.valid.*;
import top.mathcec.vertx2string.constant.RequestMethod;
import top.mathcec.vertx2string.exception.AutowiredParameterException;
import top.mathcec.vertx2string.filter.VertxBaseFilter;
import top.mathcec.vertx2string.pojo.ValidResult;
import top.mathcec.vertx2string.pojo.reflect.ClassReflect;
import top.mathcec.vertx2string.pojo.reflect.MethodReflect;
import top.mathcec.vertx2string.pojo.reflect.ParameterReflect;
import top.mathcec.vertx2string.read1class.ReflectClassWithAnnotation;
import top.mathcec.vertx2string.read1class.ReflectMethodParamsAnnotation;
import top.mathcec.vertx2string.read1class.ReflectMethodWithAnnotation;
import top.mathcec.vertx2string.utils.ArrayUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

@Accessors(chain = true)
public class ConfigHttpServer {
    @Setter
    Router router;

    public void process() throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        exception();
        filter();
        ReflectClassWithAnnotation reflectClassWithAnnotation = new ReflectClassWithAnnotation(VertxRestController.class, "top");
        //需要对这个类进行操作，获取相关的方法
        List<ClassReflect> classList = reflectClassWithAnnotation.getClassList();
        for (int i = 0; i < classList.size(); i++) {
            ClassReflect classReflect = classList.get(i);
            VertxRestController vertxRestController = ((VertxRestController) classReflect.getAnnotation());
            Class<?> clz = classReflect.getClz();
            String baseUrl = vertxRestController.value();
            ReflectMethodWithAnnotation reflectMethodWithAnnotation = new ReflectMethodWithAnnotation(VertxRequestMapping.class, clz);
            List<MethodReflect> methodReflectList = reflectMethodWithAnnotation.getMethodReflectList();
            for (int i1 = 0; i1 < methodReflectList.size(); i1++) {
                MethodReflect methodReflect = methodReflectList.get(i1);
                VertxRequestMapping vertxRequestMapping = (VertxRequestMapping) methodReflect.getAnnotation();
                Method method = methodReflect.getMethod();
                RequestMethod requestMethod = vertxRequestMapping.method();
                String extraUrl = vertxRequestMapping.value();
                String url = baseUrl + extraUrl;
                ReflectMethodParamsAnnotation reflectMethodParamsAnnotation = new ReflectMethodParamsAnnotation(method, List.of(VertxRequestHeader.class, VertxGetValue.class, VertxRequestBody.class));
                //在这之前获取过滤器
                doFilter(url, method);
                //获取参数校验结果
                ValidResult validResult = new ValidResult();
                if (requestMethod == RequestMethod.GET) {
                    get(url, method, clz, reflectMethodParamsAnnotation.getParameterReflectArrayList(), validResult);
                } else if (requestMethod == RequestMethod.POST) {
                    post(url, method, clz, reflectMethodParamsAnnotation.getParameterReflectArrayList(), validResult);
                }
            }
        }
    }

    private void post(String url, Method method, Class<?> clz
            , List<ParameterReflect> parameterReflects
            , ValidResult validResult) {
        router.post(url).handler(h -> {
            Future<Object> exec = execPost(h, method, clz, parameterReflects, validResult);
            exec.onSuccess(s -> {
                handlerResult(s, h);
            }).onFailure(f -> {
                respErr(f, h);
            });
        });
    }

    private Future<Object> execPost(RoutingContext h, Method method, Class<?> clz
            , List<ParameterReflect> parameterReflectList
            , ValidResult validResult) {
        return Future.future(p -> {
            try {
                Object[] items = new Object[parameterReflectList.size()];
                //防止多个body
                int bodyFlag = 0;
                //记录位置
                int bodyIndex = 0;
                //记录Parameter
                Parameter tempParameter = null;
                for (int i = 0; i < parameterReflectList.size(); i++) {
                    ParameterReflect parameterReflect = parameterReflectList.get(i);
                    tempParameter = parameterReflect.getParameter();
                    Annotation annotation = parameterReflect.getAnnotation();
                    if (annotation == null) {
                        makeRequestParam(i, items, parameterReflect, h, validResult);
                    } else if (annotation.annotationType() == VertxRequestBody.class) {
                        bodyIndex = i;
                        if (bodyFlag++ >= 1) {
                            //直接注入NULL
                            items[i] = null;
                        }
                    } else {
                        makeCommonAnnoParam(i, items, parameterReflect, h);
                    }
                }
                if (tempParameter != null) {
                    Future<?> getBody = handleBody(items, h, bodyIndex, tempParameter);
                    Class<?> bodyType = tempParameter.getType();
                    getBody.onSuccess(o -> {
                        try {
                            handlerValid(validResult, o, bodyType);
                        } catch (IllegalAccessException e) {
                            p.fail(e);
                        }
                        invoke(method, p, clz, items);
                    }).onFailure(p::fail);
                } else {
                    invoke(method, p, clz, items);
                }
            } catch (Exception e) {
                p.fail(e);
            }
        });
    }


    private void invoke(Method method, Promise<Object> p, Class<?> clz, Object[] items) {
        try {
            //只能自动注入一个，后面的忽略
            Object invoke = method.invoke(clz.getDeclaredConstructor().newInstance(), items);
            p.complete(invoke);
        } catch (Exception e) {
            p.fail(e);
        }
    }

    private void handlerValid(ValidResult validResult, Object o, Class<?> type) throws IllegalAccessException {
        Field[] declaredFields = type.getDeclaredFields();
        for (int i = 0; i < declaredFields.length; i++) {
            Field field = declaredFields[i];
            field.setAccessible(true);
            Object fieldVal = field.get(o);
            Class<?> fieldType = field.getType();
            //先判断是否允许为空，如果允许为空，则可以直接进行下面的判断；如果不允许为空就且为空 直接失败
            boolean notNull = field.isAnnotationPresent(VertxNotNull.class);
            VertxNotNull vertxNotNull = field.getDeclaredAnnotation(VertxNotNull.class);
            if (notNull && fieldVal == null) {
                validResult.error(type, fieldType, vertxNotNull.errorMsg());
                return;
            }
            if (!notNull && fieldVal == null) {
                continue;
            }
            //先判断值的符合情况
            VertxValue vertxValue = field.getDeclaredAnnotation(VertxValue.class);
            boolean hasVertxValue = field.isAnnotationPresent(VertxValue.class);

            VertxMin vertxMin = field.getDeclaredAnnotation(VertxMin.class);
            boolean hasVertxMin = field.isAnnotationPresent(VertxMin.class);

            VertxMax vertxMax = field.getDeclaredAnnotation(VertxMax.class);
            boolean hasVertxMax = field.isAnnotationPresent(VertxMax.class);

            VertxStringRegex vertxStringRegex = field.getDeclaredAnnotation(VertxStringRegex.class);
            boolean hasVertxStringRegex = field.isAnnotationPresent(VertxStringRegex.class);
            if (
                    fieldType == byte.class || fieldType == Byte.class ||
                            fieldType == short.class || fieldType == Short.class ||
                            fieldType == char.class || fieldType == Character.class ||
                            fieldType == int.class || fieldType == Integer.class ||
                            fieldType == long.class || fieldType == Long.class ||
                            fieldType == float.class || fieldType == Float.class ||
                            fieldType == double.class || fieldType == Double.class) {
//首先判断有没有value
                if (hasVertxValue) {
                    boolean hasVal = false;
                    for (int j = 0; j < vertxValue.value().length; j++) {
                        String val = vertxValue.value()[j];
                        if (fieldVal.toString().equals(val)) {
                            hasVal = true;
                            break;
                        }
                    }
                    if (!hasVal) {
                        validResult.error(type, fieldType, vertxValue.errorMsg());
                    } else {
                        continue;
                    }
                }
                if (hasVertxMin) {
                    double aDouble = Double.parseDouble(fieldVal.toString());
                    if (vertxMin.min() > aDouble) {
                        validResult.error(type, fieldType, vertxMin.errorMsg());
                        return;
                    }
                }
                if (hasVertxMax) {
                    double aDouble = Double.parseDouble(fieldVal.toString());
                    if (vertxMax.max() < aDouble) {
                        validResult.error(type, fieldType, vertxMax.errorMsg());
                        return;
                    }
                }
            }
            if (fieldType == String.class) {
                if (hasVertxValue) {
                    boolean hasVal = false;
                    for (int j = 0; j < vertxValue.value().length; j++) {
                        String val = vertxValue.value()[j];
                        if (fieldVal.toString().equals(val)) {
                            hasVal = true;
                            break;
                        }
                    }
                    if (!hasVal) {
                        validResult.error(type, fieldType, vertxValue.errorMsg());
                    } else {
                        continue;
                    }
                }
                if (hasVertxMin) {
                    int len = fieldVal.toString().length();
                    if (vertxMin.min() > len) {
                        validResult.error(type, fieldType, vertxMin.errorMsg());
                        return;
                    }
                }
                if (hasVertxMax) {
                    int len = fieldVal.toString().length();
                    if (vertxMax.max() < len) {
                        validResult.error(type, fieldType, vertxMax.errorMsg());
                        return;
                    }
                }
                if (hasVertxStringRegex) {
                    if (!fieldVal.toString().matches(vertxStringRegex.regex())) {
                        validResult.error(type, fieldType, vertxStringRegex.errorMsg());
                        return;
                    }
                }
            }
        }
    }

    private Future<Object> handleBody(Object[] items, RoutingContext h, int i, Parameter parameter) {
        return Future.future(p -> {
            try {
                h.request().bodyHandler(b -> {
                    try {
                        JsonObject bodyAsJson = b.toJsonObject();
                        Object o = bodyAsJson.mapTo(parameter.getType());
                        items[i] = o;
                        p.complete(o);
                    } catch (Exception e) {
                        p.fail(e);
                    }
                });
            } catch (Exception e) {
                p.fail(e);
            }

        });
    }

    private void get(String url, Method method, Class<?> clz
            , List<ParameterReflect> parameterReflectList
            , ValidResult validResult) {
        router.get(url).handler(h -> {
            Future<Object> exec = execGet(h, method, clz, parameterReflectList, validResult);
            exec.onSuccess(out -> {
                handlerResult(out, h);
            }).onFailure(f -> {
                respErr(f, h);
            });

        });
    }

    private Future<Object> execGet(RoutingContext h, Method method, Class<?> clz
            , List<ParameterReflect> parameterReflectList
            , ValidResult validResult) {
        return Future.future(p -> {
            try {
                Object[] items = new Object[parameterReflectList.size()];
                for (int i2 = 0; i2 < parameterReflectList.size(); i2++) {
                    ParameterReflect parameterReflect = parameterReflectList.get(i2);
                    Parameter parameter = parameterReflect.getParameter();
                    Annotation annotation = parameterReflect.getAnnotation();
                    if (annotation == null) {
                        makeRequestParam(i2, items, parameterReflect, h, validResult);
                    } else if (annotation.annotationType() == VertxGetValue.class) {
                        VertxGetValue vertxGetValue = (VertxGetValue) annotation;
                        String paramKey = vertxGetValue.value();
                        Object getValue = h.request().getParam(paramKey);
                        validGetVal(validResult, parameter, clz, getValue);
                        items[i2] = getValue;
                    } else {
                        makeCommonAnnoParam(i2, items, parameterReflect, h);
                    }
                }
                invoke(method, p, clz, items);
            } catch (Exception e) {
                p.fail(e);
            }
        });
    }

    private void validGetVal(ValidResult validResult, Parameter parameter, Class<?> classType, Object o) {
        Class<?> fieldType = parameter.getType();
        boolean notNull = parameter.isAnnotationPresent(VertxNotNull.class);
        VertxNotNull vertxNotNull = parameter.getDeclaredAnnotation(VertxNotNull.class);
        if (notNull && o == null) {
            validResult.error(classType, fieldType, vertxNotNull.errorMsg());
            return;
        }
        if (!notNull && o == null) {
            return;
        }
        //先判断值的符合情况
        VertxValue vertxValue = parameter.getDeclaredAnnotation(VertxValue.class);
        boolean hasVertxValue = parameter.isAnnotationPresent(VertxValue.class);

        VertxMin vertxMin = parameter.getDeclaredAnnotation(VertxMin.class);
        boolean hasVertxMin = parameter.isAnnotationPresent(VertxMin.class);

        VertxMax vertxMax = parameter.getDeclaredAnnotation(VertxMax.class);
        boolean hasVertxMax = parameter.isAnnotationPresent(VertxMax.class);

        VertxStringRegex vertxStringRegex = parameter.getDeclaredAnnotation(VertxStringRegex.class);
        boolean hasVertxStringRegex = parameter.isAnnotationPresent(VertxStringRegex.class);

        if (
                fieldType == byte.class || fieldType == Byte.class ||
                        fieldType == short.class || fieldType == Short.class ||
                        fieldType == char.class || fieldType == Character.class ||
                        fieldType == int.class || fieldType == Integer.class ||
                        fieldType == long.class || fieldType == Long.class ||
                        fieldType == float.class || fieldType == Float.class ||
                        fieldType == double.class || fieldType == Double.class) {
//首先判断有没有value
            if (hasVertxValue) {
                boolean hasVal = false;
                for (int j = 0; j < vertxValue.value().length; j++) {
                    String val = vertxValue.value()[j];
                    if (o.toString().equals(val)) {
                        hasVal = true;
                        break;
                    }
                }
                if (!hasVal) {
                    validResult.error(classType, fieldType, vertxValue.errorMsg());
                } else {
                    return;
                }
            }
            if (hasVertxMin) {
                double aDouble = Double.parseDouble(o.toString());
                if (vertxMin.min() > aDouble) {
                    validResult.error(classType, fieldType, vertxMin.errorMsg());
                    return;
                }
            }
            if (hasVertxMax) {
                double aDouble = Double.parseDouble(o.toString());
                if (vertxMax.max() < aDouble) {
                    validResult.error(classType, fieldType, vertxMax.errorMsg());
                    return;
                }
            }
        }
        if (fieldType == String.class) {
            if (hasVertxValue) {
                boolean hasVal = false;
                for (int j = 0; j < vertxValue.value().length; j++) {
                    String val = vertxValue.value()[j];
                    if (o.toString().equals(val)) {
                        hasVal = true;
                        break;
                    }
                }
                if (!hasVal) {
                    validResult.error(classType, fieldType, vertxValue.errorMsg());
                } else {
                    return;
                }
            }
            if (hasVertxMin) {
                int len = o.toString().length();
                if (vertxMin.min() > len) {
                    validResult.error(classType, fieldType, vertxMin.errorMsg());
                    return;
                }
            }
            if (hasVertxMax) {
                int len = o.toString().length();
                if (vertxMax.max() < len) {
                    validResult.error(classType, fieldType, vertxMax.errorMsg());
                    return;
                }
            }
            if (hasVertxStringRegex) {
                if (!o.toString().matches(vertxStringRegex.regex())) {
                    validResult.error(classType, fieldType, vertxStringRegex.errorMsg());
                    return;
                }
            }
        }
    }

    private void handlerResult(Object o, RoutingContext h) {
        if (o instanceof String
                || o instanceof Byte
                || o instanceof Short
                || o instanceof Integer
                || o instanceof Long
                || o instanceof Float
                || o instanceof Double
                || o instanceof Boolean) {
            h.response().end(o.toString());
        } else {
            h.response().end(JsonObject.mapFrom(o).toBuffer());
        }
    }

    private void makeRequestParam(int i, Object[] objects, ParameterReflect parameterReflect, RoutingContext h, ValidResult validResult
    ) throws AutowiredParameterException {
        Class<?> type = parameterReflect.getParameter().getType();
        if (type == HttpServerRequest.class) {
            objects[i] = h.request();
        } else if (type == HttpServerResponse.class) {
            objects[i] = h.response();
        } else if (type == RoutingContext.class) {
            objects[i] = h;
        } else if (type == ValidResult.class) {
            objects[i] = validResult;
        } else {
            throw new AutowiredParameterException(type);
        }
    }


    private void makeCommonAnnoParam(int i, Object[] items, ParameterReflect parameterReflect, RoutingContext h) {
        Class<? extends Annotation> aClass = parameterReflect.getAnnotation().annotationType();
        Annotation annotation = parameterReflect.getAnnotation();
        if (aClass == VertxRequestHeader.class) {
            items[i] = h.request().getHeader(((VertxRequestHeader) annotation).value());
        }
    }

    private void respErr(Throwable f, RoutingContext h) {
        f.printStackTrace();
        if (!h.response().ended()) {
            h.response().setStatusCode(500).end(ArrayUtils.throwArrayToString(f));
        }
    }

    private void exception() {
        this.router.errorHandler(500, err -> {
            respErr(err.failure(), err);
        });
    }

    private static final Map<String, LinkedList<VertxBaseFilter>> FILTER = new HashMap<>();

    private void filter() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Map<String, TreeMap<Integer, VertxBaseFilter>> mapMap = new HashMap<>();
        //找到所有的filter，加入到一个Map的LinkedList中去;
        ReflectClassWithAnnotation reflectClassWithAnnotation = new ReflectClassWithAnnotation(VertxFilter.class, "top");
        List<ClassReflect> classList = reflectClassWithAnnotation.getClassList();
        for (ClassReflect c :
                classList) {
            Annotation annotation = c.getAnnotation();
            Class<?> clz = c.getClz();
            VertxFilter vertxFilter = (VertxFilter) annotation;
            String name = vertxFilter.name();
            int priority = vertxFilter.priority();
            TreeMap<Integer, VertxBaseFilter> orDefault = mapMap.getOrDefault(name, new TreeMap<>());
            orDefault.put(priority, (VertxBaseFilter) clz.getDeclaredConstructor().newInstance());
            mapMap.put(name, orDefault);
        }
        Set<String> keySet = mapMap.keySet();
        for (String key : keySet) {
            TreeMap<Integer, VertxBaseFilter> treeMap = mapMap.get(key);
            LinkedList<VertxBaseFilter> vertxBaseFilterLinkedList = new LinkedList<>(treeMap.values());
            FILTER.put(key, vertxBaseFilterLinkedList);
        }
    }

    private void doFilter(String url, Method method) {
        //判断这个方法上面有没有DO_FILTER,判读这个方法的类上有没有DO_FILTER
        boolean annotationPresent = method.isAnnotationPresent(VertxApplyFilter.class);
        Class<?> declaringClass = method.getDeclaringClass();
        boolean annotationPresent1 = declaringClass.isAnnotationPresent(VertxApplyFilter.class);
        if (annotationPresent || annotationPresent1) {
            VertxApplyFilter annotation;
            if (annotationPresent) {
                annotation = method.getAnnotation(VertxApplyFilter.class);
            } else {
                annotation = declaringClass.getAnnotation(VertxApplyFilter.class);
            }
            LinkedList<VertxBaseFilter> vertxBaseFilterLinkedList = FILTER.get(annotation.filter());
            if (vertxBaseFilterLinkedList == null || vertxBaseFilterLinkedList.size() == 0) {
                return;
            }
            Iterator<VertxBaseFilter> vertxBaseFilterIterator = vertxBaseFilterLinkedList.descendingIterator();
            this.router.route(url).handler(h -> {
                replayFilter(vertxBaseFilterIterator, h);
            });

        }
    }

    private void replayFilter(Iterator<VertxBaseFilter> vertxBaseFilterIterator, RoutingContext h) {
        if (!vertxBaseFilterIterator.hasNext()) {
            h.next();
            return;
        }
        vertxBaseFilterIterator
                .next()
                .next(h)
                .onSuccess(s -> {
                    replayFilter(vertxBaseFilterIterator, h);

                })
                .onFailure(f -> {
                    respErr(f, h);
                });
    }
}
