package local.pms.userservice.util;

import java.util.Map;
import java.util.UUID;
import java.util.Optional;

public class UUIDUtil {

    public static UUID getUUIDValueFromMap(String key, Map<String, Object> dataMap) {
        return Optional.ofNullable(dataMap.get(key))
                .map(Object::toString)
                .map(UUID::fromString)
                .orElse(null);
    }
}