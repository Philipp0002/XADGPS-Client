package de.raffaelhahn.xadgps_client;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;

public class NotifyDevice {
    public String id;
    public String name;
    public String olat;
    public String olng;
    public String latitude;
    public String longitude;

    public NotifyDevice setFromJson(JSONObject jsonObject) {
        Field[] fields = getDeclaredFields(getClass());
        Iterator<String> keys = jsonObject.keys();
        while(keys.hasNext()) {
            String key = keys.next();
            try {
                Optional<Field> possibleField = Arrays.stream(fields).filter(x -> x.getName().equals(key)).findFirst();
                if(possibleField.isPresent()) {
                    Field f = possibleField.get();
                    f.setAccessible(true);
                    f.set(this, jsonObject.get(key));
                } else {
                    throw new RuntimeException("No field " + key + " in class " + getClass().getName());
                }
            } catch (Throwable e) {
                Log.w(getClass().getSimpleName(), "setFromJson: " + e.getMessage());
            }
        }
        return this;
    }

    public JSONObject getAsJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            for(Field field : getDeclaredFields(getClass())) {
                field.setAccessible(true);
                jsonObject.put(field.getName(), field.get(this));
            }
        } catch (Throwable e) {
            Log.w(getClass().getSimpleName(), "getAsJson: " + e.getMessage());
        }
        return jsonObject;
    }

    private Field[] getDeclaredFields(Class clazz) {
        final Field[] fields = clazz.getDeclaredFields();

        if ( clazz.getSuperclass() != Object.class ) {
            final Field[] pFields = getDeclaredFields(clazz.getSuperclass());
            final Field[] allFields = new Field[fields.length + pFields.length];
            Arrays.setAll(allFields, i -> (i < pFields.length ? pFields[i] : fields[i - pFields.length]));
            return allFields;
        } else
            return fields;
    }

}
