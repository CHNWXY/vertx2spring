package top.mathcec.vertx2string.read1class;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import top.mathcec.vertx2string.pojo.reflect.ClassReflect;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ReflectClassWithAnnotation {
    //是需要读取哪里的注解
    Class<? extends Annotation> annotation;
    //需要读取的报名是什么，默认是空就是
    String packageName;
    //ClassList
    @Getter
    List<ClassReflect> classList = new ArrayList<>();

    public ReflectClassWithAnnotation(Class<? extends Annotation> annotation, String packageName) throws IOException, ClassNotFoundException {
        this.annotation = annotation;
        this.packageName = packageName;
        load();
    }

    public ReflectClassWithAnnotation(Class<? extends Annotation> annotation) throws IOException, ClassNotFoundException {
        this(annotation, ".");
    }

    public void load() throws IOException, ClassNotFoundException {
        //获取路径
        String path = packageName.replace(".", "/");
        String tempPackageName = packageName;
        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(path);
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            String protocol = url.getProtocol();
            String filePath = URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8);
            if ("file".equals(protocol)) {
                getFromFile(tempPackageName, filePath);
            }
        }
    }

    private void getFromFile(String packageName, String path) throws ClassNotFoundException {
        File[] files = fileFilterClass(path);
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    addClass(getClassName(packageName, file.getName()));
                } else {
                    String subPackageName = packageName + "." + file.getName();
                    String subPath = path + "/" + file.getName();
                    getFromFile(subPackageName, subPath);
                }
            }
        }
    }


    private File[] fileFilterClass(String filePath) {
        if (filePath == null || "".equals(filePath)) {
            return null;
        } else {
            return new File(filePath).listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return ((file.isFile() && file.getName().endsWith(".class")) || file.isDirectory());
                }
            });
        }
    }

    private void addClass(String className) throws ClassNotFoundException {
        if (className != null && !"".equals(className)) {
            Class<?> classObject = Class.forName(className);
            //类对象存在
            if (classObject != null) {
                //是否需要判断该类是否包含此注解
                if (annotation == null) {
                    classList.add(new ClassReflect(classObject, null));
                } else if (classObject.isAnnotationPresent(annotation)) {
                    Annotation annotation = classObject.getAnnotation(this.annotation);
                    classList.add(new ClassReflect(classObject, annotation));
                }
            }
        }
    }

    private String getClassName(String packageName, String fileName) {
        int lastIndex = fileName.lastIndexOf(".");
        if (lastIndex > 0) {
            return packageName + "." + fileName.substring(0, lastIndex);
        } else {
            return "";
        }
    }


}
