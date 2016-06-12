package benblamey.core.json;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Serialize Java objects to JSON.
 *
 * GSON seems to be the only non-bloated sensible Java library for serializing
 * beans to JSON via reflection. Unfortunately, it does not support anonymous
 * classes (which I have used extensively). Therefore, I hereby create a new
 * JSON library, which uses GSON underneath.
 *
 * @author Ben Blamey ben@benblamey.com
 *
 */
public class JavaToJson {

    private static Gson _gson = new Gson();

    public static String toJSON(Object value) {

        if (value == null) {
            return null;
        }

        Class<?> clazz = value.getClass();

        if (//clazz.isPrimitive()
                //|| 
                value instanceof Integer
                || value instanceof Boolean
                || value instanceof Float
                || value instanceof Byte) {
            // GSON handles primitives without any issue.
            return _gson.toJson(value);
        } else if (value instanceof Double) {
            Double valueDouble = (Double)value;
            if (valueDouble.equals(Double.NEGATIVE_INFINITY)) {
                return _gson.toJson("-INF");
            } else if (valueDouble.equals(Double.POSITIVE_INFINITY)) {
                return _gson.toJson("+INF");
            } else if (valueDouble.equals(Double.NaN)) { 
                return _gson.toJson("NAN");
            } else {
                return _gson.toJson(value);    
            } 
        } else if (value instanceof String) {
            return _gson.toJson(value);
        } else if (value instanceof Map<?, ?>) {

            StringBuilder json = new StringBuilder();
            json.append("{\n");

            for (Entry<?, ?> foo : ((Map<?, ?>) value).entrySet()) {

                Object fieldValue = foo.getValue();

                String jsonValue = toJSON(fieldValue);
                if (jsonValue != null) {
                    // Field names are specified as JSON strings.
                    json.append(_gson.toJson(foo.getKey()));
                    json.append(":");
                    json.append(jsonValue);
                    json.append(",\n");
                } else {
                    ".".toString();
                }

            }

            json.append("}\n");

            System.out.println("toJSON(" + value + ")");
            System.out.println(json.toString());

            return json.toString();

        } else if (value instanceof Collection<?>) {
            throw new RuntimeException("not implemented");
        } else if (value instanceof Iterable<?>) {
            throw new RuntimeException("not implemented");
        } else {
            // Hopefully, its a bean. Lets try to serialise it.

            StringBuilder json = new StringBuilder();
            json.append("{\n");

            Class<?> clazz2 = clazz;
            while (clazz2 != null) {
                // The method below uses "getDeclaredFields" which only looks at fields from that class - not parents.
                // We need to iterate through the parents.
                exportFields(value, clazz2, json);
                clazz2 = clazz2.getSuperclass();
            }

            json.append("}\n");

            System.out.println("toJSON(" + value + ")");
            System.out.println(json.toString());

            return json.toString();
        }
    }

    private static void exportFields(Object value, Class<?> clazz, StringBuilder json) {

        //Map<String, Object> map = new HashMap<String, Object>();
        JsonObject map = new JsonObject();
        
        boolean first = true;
        
        // This ignores any fields declared in the parent type.
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field f : declaredFields) {

            System.out.println("Field: " + f.getName());

            // We don't want the 'this' field.
            if (f.getName().startsWith("this$")) {
                continue;
            }

            // Skip static fields.
            if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                continue;
            }

            // Skip transient fields.
            if (java.lang.reflect.Modifier.isTransient(f.getModifiers())) {
                continue;
            }

            // Incase it is private, give ourselves access.
            f.setAccessible(true);

            Object fieldValue;
            try {
                fieldValue = f.get(value);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException();
            } catch (IllegalAccessException e) {
                throw new RuntimeException();
            }

            String jsonValue = toJSON(fieldValue);
            
            // Field names are specified as JSON strings.
            
            if (!first) {
                json.append(",\n");
            }
            
            json.append(_gson.toJson(f.getName()));
            json.append(":");
            json.append(jsonValue);
            
            first = false;
        }
        // Newline if any fields were added for this class (we might go thru a lot of empty subclasses)
        if (!first) {
            json.append("\n");
        }
        
    }

}
