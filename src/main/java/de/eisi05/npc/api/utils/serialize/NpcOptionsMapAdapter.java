package de.eisi05.npc.api.utils.serialize;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import de.eisi05.npc.api.ai.Goal;
import de.eisi05.npc.api.objects.NpcOption;
import de.eisi05.npc.api.objects.SkinData;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NpcOptionsMapAdapter extends TypeAdapter<Map<String, Object>>
{
    public static Object deepCleanMaps(Object value)
    {
        if(value instanceof Map<?, ?> nativeMap)
        {
            HashMap<Object, Object> cleanMap = new HashMap<>();
            for(Map.Entry<?, ?> entry : nativeMap.entrySet())
            {
                Object cleanKey = deepCleanMaps(entry.getKey());
                Object cleanVal = deepCleanMaps(entry.getValue());
                cleanMap.put(cleanKey, cleanVal);
            }
            return cleanMap;
        }
        else if(value instanceof List<?> nativeList)
        {
            ArrayList<Object> cleanList = new ArrayList<>();
            for(Object element : nativeList)
                cleanList.add(deepCleanMaps(element));
            return cleanList;
        }
        return value;
    }

    @Override
    public void write(JsonWriter out, Map<String, Object> map) throws IOException
    {
        if(map == null)
        {
            out.nullValue();
            return;
        }

        out.beginObject();
        for(Map.Entry<String, Object> entry : map.entrySet())
        {
            NpcOption<?, ?> option = NpcOption.getOption(entry.getKey()).orElse(null);
            Object value = entry.getValue();

            if(value == null || option == null)
                continue;

            out.name(option.getPath());

            Object serializedValue = option.serialize(value);
            ObjectSaver.GSON.toJson(serializedValue, serializedValue.getClass(), out);
        }
        out.endObject();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> read(JsonReader in) throws IOException
    {
        Map<String, Object> map = new HashMap<>();

        in.beginObject();
        while(in.hasNext())
        {
            String path = in.nextName();

            var optionOpt = NpcOption.getOption(path);
            if(optionOpt.isEmpty())
            {
                in.skipValue();
                continue;
            }

            NpcOption<?, Serializable> option = (NpcOption<?, Serializable>) optionOpt.get();
            Class<?> serializedClass = getSerializedClassForOption(option);

            Serializable serializedValue;
            if(option.getPath().equals("goals"))
                serializedValue = ObjectSaver.GSON.fromJson(in, new TypeToken<ArrayList<Goal>>() {}.getType());
            else if(serializedClass == Object.class || serializedClass.isInterface())
                serializedValue = (Serializable) ObjectSaver.GSON.fromJson(JsonParser.parseReader(in), Object.class);
            else if(Map.class.isAssignableFrom(serializedClass))
                serializedValue = ObjectSaver.GSON.fromJson(in, HashMap.class);
            else
                serializedValue = ObjectSaver.GSON.fromJson(in, serializedClass);

            Object usableValue = option.deserialize(serializedValue);
            map.put(option.getPath(), usableValue);
        }
        in.endObject();

        return map;
    }

    private Class<?> getSerializedClassForOption(NpcOption<?, ?> option)
    {
        Object defaultValue = option.getDefaultValue();
        if(defaultValue != null)
        {
            if(option == NpcOption.EQUIPMENT)
                return HashMap.class;
            return defaultValue.getClass();
        }

        if(option.getPath().equals("skin"))
            return SkinData.class;

        return Object.class;
    }
}