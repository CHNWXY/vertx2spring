package top.mathcec.vertx2string.utils;

import java.util.Arrays;

public class ArrayUtils {
    public static String throwArrayToString(Throwable throwable) {
        StringBuilder error = new StringBuilder("Something Wrong").append("\r\n").append(throwable.getLocalizedMessage()).append("\r\n");
        Arrays.stream(throwable.getStackTrace()).forEach(t -> {
            error.append(t).append("\r\n");
        });
        return error.toString();
    }
}
