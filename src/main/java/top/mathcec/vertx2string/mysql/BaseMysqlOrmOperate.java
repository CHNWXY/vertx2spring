package top.mathcec.vertx2string.mysql;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.RowSet;
import top.mathcec.vertx2string.annotation.mysql.VertxTableName;
import top.mathcec.vertx2string.exception.TableNameNotExistException;

import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.util.*;

/**
 * 所有Orm类必须继承的一个类
 */
public class BaseMysqlOrmOperate<T> {

    MySQLPool mySQLPool;

    T obj;

    String tableName;

    Map<String, String> cols = new HashMap<>();

    public BaseMysqlOrmOperate(T t) throws TableNameNotExistException {
        this.obj = t;
        loadTableName();
    }

    public void loadTableName() throws TableNameNotExistException {
        boolean annotationPresent = this.obj.getClass().isAnnotationPresent(VertxTableName.class);
        if (!annotationPresent) {
            throw new TableNameNotExistException(null, obj.getClass());
        }
        VertxTableName declaredAnnotation = this.obj.getClass().getDeclaredAnnotation(VertxTableName.class);
        this.tableName = declaredAnnotation.tableName();
    }


    public Future<T> query() {
        return Future.future(p -> {
            StringBuilder colsSb = new StringBuilder();
            if (this.cols.size() == 0) {
                colsSb.append("* ");
            } else {
                Set<Map.Entry<String, String>> entries = this.cols.entrySet();
                int i = 0;
                for (Map.Entry<String, String> entry : entries) {
                    i++;
                    colsSb.append(entry.getValue()).append(" ").append("as ")
                            .append(entry.getKey());
                    if (i != entries.size() - 1) {
                        colsSb.append(", ");
                    } else {
                        colsSb.append(" ");
                    }
                }
            }
            String sql = String.format("SELECT %s FROM %s",
                    colsSb.toString(), this.tableName);
            mySQLPool.preparedQuery(sql, h -> {
                if (h.succeeded()) {
                    RowSet<Row> result = h.result();
                    if (result == null) {
                        p.complete(null);
                        return;
                    }
                    RowIterator<Row> iterator = result.iterator();
                } else {
                    p.fail(h.cause());
                }
            });
        });
    }

    public BaseMysqlOrmOperate<T> select(String[] addCols) {
        if (addCols != null) {
            for (int i = 0; i < addCols.length; i++) {
                String col = addCols[i];
                cols.put(col, col);
            }
        }
        return this;
    }

    public BaseMysqlOrmOperate<T> select(Map<String, String> addCols) {
        this.cols.putAll(addCols);
        return this;
    }
}
