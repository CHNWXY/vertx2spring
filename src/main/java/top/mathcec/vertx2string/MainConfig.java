package top.mathcec.vertx2string;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import top.mathcec.vertx2string.config.ConfigHttpServer;
import top.mathcec.vertx2string.exception.NullConfigException;
import top.mathcec.vertx2string.pojo.ConfigPojo;
import top.mathcec.vertx2string.pojo.HttpServerConfig;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class MainConfig {
    private static final Vertx VERTX = Vertx.vertx();

    private static final MainConfig MAIN_CONFIG = new MainConfig();

    private static final ConfigPojo CONFIG_POJO = new ConfigPojo();

    public static MainConfig vertx2Spring() {
        CONFIG_POJO.setHttpServerConfig(new HttpServerConfig().setServerPort(8080));
        CONFIG_POJO.setHttpServerOptions(new HttpServerOptions());
        return MAIN_CONFIG;
    }

    public void process() throws NullConfigException, IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        httpServer();
    }

    private void httpServer() throws NullConfigException, IOException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        HttpServerConfig httpServerConfig = CONFIG_POJO.getHttpServerConfig();
        HttpServerOptions httpServerOptions = CONFIG_POJO.getHttpServerOptions();
        if (httpServerConfig == null) {
            throw new NullConfigException("HttpServerOptions is NULL");
        }
        if (httpServerOptions == null) {
            throw new NullConfigException("HttpServerConfig is NULL");
        }

        HttpServer httpServer = VERTX.createHttpServer(httpServerOptions);
        Router router = Router.router(VERTX);
        //router直接反射获取所有的加注了RestController的类，并让其返回JSON值即可
        //可以创建一个对象，直接调用对应方法
        //遍历所有文件，获取有相关注解的类
        ConfigHttpServer configHttpServer = new ConfigHttpServer().setRouter(router);
        configHttpServer.process();
        httpServer.requestHandler(router).listen(httpServerConfig.getServerPort());
    }

    public static void main(String[] args) throws NullConfigException, IOException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        MainConfig mainConfig = MainConfig.vertx2Spring();
        mainConfig.process();
    }
}
