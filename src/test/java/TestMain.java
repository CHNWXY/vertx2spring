import org.junit.Test;
import top.mathcec.vertx2string.MainConfig;
import top.mathcec.vertx2string.exception.NullConfigException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class TestMain {

    @Test
    public void testMainLoad() throws NullConfigException, IOException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        MainConfig mainConfig = MainConfig.vertx2Spring();
        mainConfig.process();
    }
}
