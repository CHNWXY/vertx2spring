package top.mathcec.vertx2string.exception;

public class TableNameNotExistException extends Exception {
    public TableNameNotExistException(String tableName, Class<?> type) {
        super("表名不存在：tableName:" + tableName + "; classType:" + type);
    }
}
