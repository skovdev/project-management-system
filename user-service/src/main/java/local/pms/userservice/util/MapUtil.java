package local.pms.userservice.util;

import java.util.Map;

public class MapUtil {

    @SuppressWarnings("unchecked")
    public static <T> T getValue(Map<String, Object> dataMap, String key, Class<?> type) {

        Object value = dataMap.get(key);

        if (value == null) {
            throw new IllegalArgumentException("Value for key '" + key + "' is null.");
        }

        if (type.isInstance(value)) {
            return (T) type.cast(value);
        } else {
            throw new IllegalArgumentException("Value for key '" + key + "' must be of type " + type.getSimpleName() + ".");
        }
    }
}