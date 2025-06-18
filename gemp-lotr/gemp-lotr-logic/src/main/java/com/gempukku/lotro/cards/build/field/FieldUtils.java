package com.gempukku.lotro.cards.build.field;

import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.SitesBlock;
import org.hjson.JsonValue;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Set;

public class FieldUtils {

    public static int getInteger(Object value, String key) throws InvalidCardDefinitionException {
        return getInteger(value, key, 0);
    }

    public static int getInteger(Object value, String key, int defaultValue) throws InvalidCardDefinitionException {
        if (value == null)
            return defaultValue;
        if (!(value instanceof Number))
            throw new InvalidCardDefinitionException("Value in " + key + " field must be an integer, not an evaluated value.");
        return ((Number) value).intValue();
    }

    public static String[] getStringArray(Object value, String key) throws InvalidCardDefinitionException {
        if (value == null)
            return new String[0];
        else if (value instanceof String)
            return new String[]{(String) value};
        else if (value instanceof final JSONArray array) {
            return (String[]) array.toArray(new String[0]);
        }
        throw new InvalidCardDefinitionException("Unknown type in " + key + " field");
    }

    public static String getString(Object value, String key) throws InvalidCardDefinitionException {
        return getString(value, key, null);
    }

    public static String getString(Object value, String key, String defaultValue) throws InvalidCardDefinitionException {
        if (value == null)
            return defaultValue;
        if ((value instanceof Number))
            return value.toString();
        if (!(value instanceof String))
            throw new InvalidCardDefinitionException("Unknown type in " + key + " field");
        return (String) value;
    }

    public static boolean getBoolean(Object value, String key) throws InvalidCardDefinitionException {
        if (value == null)
            throw new InvalidCardDefinitionException("Value of " + key + " is required");
        if (!(value instanceof Boolean))
            throw new InvalidCardDefinitionException("Unknown type in " + key + " field");
        return (Boolean) value;
    }

    public static int getUniqueness(Object value, String key) throws InvalidCardDefinitionException {
        if (value == null)
            throw new InvalidCardDefinitionException("Value of " + key + " is required");
        if (!(value instanceof Number) && !(value instanceof Boolean))
            throw new InvalidCardDefinitionException("Value in " + key + " field must be an integer or boolean.");
        if (value instanceof Boolean) {
            return (Boolean) value ? 1 : 4;
        }
        return ((Number) value).intValue();
    }

    public static boolean getBoolean(Object value, String key, boolean defaultValue) throws InvalidCardDefinitionException {
        if (value == null)
            return defaultValue;
        if (!(value instanceof Boolean))
            throw new InvalidCardDefinitionException("Unknown type in " + key + " field");
        return (Boolean) value;
    }

    public static <T extends Enum<T>> T getEnum(Class<T> enumClass, Object value, String key) throws InvalidCardDefinitionException {
        if (value == null)
            return null;
        final String string = getString(value, key);
        try {
            return Enum.valueOf(enumClass, string.toUpperCase().replace(' ', '_').replace('-', '_'));
        } catch (IllegalArgumentException exp) {
            throw new InvalidCardDefinitionException("Unknown enum value - " + string + ", in " + key + " field");
        }
    }

    public static SitesBlock getBlock(Object value, String key) throws InvalidCardDefinitionException {
        final String string = getString(value, key);
        if (string == null)
            throw new InvalidCardDefinitionException("Unknown block '" + string + "' in " + key + " field");
        final SitesBlock block = SitesBlock.findBlock(string);
        if (block == null)
            throw new InvalidCardDefinitionException("Unknown block '" + string + "' in " + key + " field");
        return block;
    }

    public static Side getSide(Object value, String key) throws InvalidCardDefinitionException {
        final String string = getString(value, key);
        if (string == null)
            throw new InvalidCardDefinitionException("Unknown side '" + string + "' in " + key + " field");
        final Side side = Side.Parse(string);
        if (side == null)
            throw new InvalidCardDefinitionException("Unknown side '" + string + "' in " + key + " field");
        return side;
    }

    public static JSONArray getArray(Object value, String key) throws InvalidCardDefinitionException {
        if (value == null)
            return new JSONArray();
        else if (value instanceof JSONObject) {
            final JSONArray jsonArray = new JSONArray();
            jsonArray.add(value);
            return jsonArray;
        } else if (value instanceof JSONArray)
            return (JSONArray) value;
        throw new InvalidCardDefinitionException("Unknown type in " + key + " field");
    }

    public static JSONObject[] getObjectArray(Object value, String key) throws InvalidCardDefinitionException {
        if (value == null)
            return new JSONObject[0];
        else if (value instanceof JSONObject)
            return new JSONObject[]{(JSONObject) value};
        else if (value instanceof final JSONArray array) {
            return (JSONObject[]) array.toArray(new JSONObject[0]);
        }
        throw new InvalidCardDefinitionException("Unknown type in " + key + " field");
    }

    public static void validateAllowedFields(JSONObject object, String... fields) throws InvalidCardDefinitionException {
        Set<String> keys = object.keySet();
        for (String key : keys) {
            if (!key.equals("type") && !key.equals("ignoreCostCheckFailure") && !contains(fields, key))
                throw new InvalidCardDefinitionException("Unrecognized field: " + key);
        }
    }

    private static final JSONParser parser = new JSONParser();

    public static JSONObject parseSubObject(String jsonString) throws ParseException {
        String json = JsonValue.readHjson(jsonString).toString();
        var subObject = (JSONObject) parser.parse(json);
        return subObject;
    }

    private static boolean contains(String[] fields, String key) {
        for (String field : fields) {
            if (field.equals(key))
                return true;
        }
        return false;
    }
}
