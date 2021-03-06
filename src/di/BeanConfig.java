package di;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import di.annotation.Config;
import di.convert.ConvertObject;
import di.utility.ReflectUtil;

public class BeanConfig {
	private static Logger LOGGER = LoggerFactory.getLogger(BeanConfig.class);
	private static Properties props = new Properties();
	
	/**
	 * 初始化
	 * @param fileName	文件名
	 */
	public static void initialize(String fileName) {
		try {
			InputStream is = BeanConfig.class.getClassLoader().getResourceAsStream(fileName);
			if (is == null) {
				LOGGER.error("[{}] file path not found.", fileName);
				return;
			}
			props.load(BeanConfig.class.getClassLoader().getResourceAsStream(fileName));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 获取值
	 * @param key
	 * @return
	 */
	public static String getValue(String key) {
		return props.getProperty(key);
	}
	
	public static Integer getInt(String key) {
		return Integer.valueOf(props.getProperty(key));
	}

	public static Boolean getBool(String key) {
		return Boolean.valueOf(props.getProperty(key));
	}

	/**
	 * 配置注入
	 * @param object	已实例化的对象
	 */
	public static void injectConfig(Object object) {
		if (object == null) {
			return;
		}

		Set<Field> fields = ReflectUtil.getFields(object.getClass(), Config.class);
		for (Field field : fields) {
			try {
				Config annotation = field.getAnnotation(Config.class);
				String key = annotation.value().isEmpty() ? field.getName() : annotation.value();
				String value = props.getProperty(key);
				if (value == null || value.isEmpty()) {
					throw new RuntimeException(String.format("class:[%s] key:['%s'] not found. from properties file.", key, object.getClass()));
				}

				field.setAccessible(true);
				field.set(object, ConvertObject.execute(value, field.getType()));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
