package de.eisi05.npc.api.utils.serialize;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import de.eisi05.npc.api.interfaces.NpcClickAction;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Base64;
import java.util.Map;

public class NpcClickActionAdapter extends TypeAdapter<NpcClickAction>
{
    private static final String IMPL_CLASS_NAME = "NpcClickActionImpl";

    @Override
    public void write(JsonWriter out, NpcClickAction value) throws IOException
    {
        if(value == null)
        {
            out.nullValue();
            return;
        }

        Class<?> clazz = value.getClass();
        out.beginObject();

        if(clazz.isSynthetic() || clazz.getName().contains("$$Lambda$") || !Serializable.class.isAssignableFrom(clazz))
        {
            try(ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                ObjectOutputStream objOut = new ObjectOutputStream(byteOut))
            {
                objOut.writeObject(value);
                String base64Bytes = Base64.getEncoder().encodeToString(byteOut.toByteArray());
                out.name("bytes").value(base64Bytes);
            }
            catch(Exception e)
            {
                throw new IOException("Failed to natively serialize dynamic NpcClickAction lambda expression", e);
            }
        }
        else
        {
            out.name("className").value(clazz.getName());

            Object originalParent = null;
            boolean isImplInstance = clazz.getName().endsWith(IMPL_CLASS_NAME);
            Field parentField = null;

            if(isImplInstance)
            {
                try
                {
                    parentField = clazz.getDeclaredField("parent");
                    parentField.setAccessible(true);
                    originalParent = parentField.get(value);
                    parentField.set(value, null);
                }
                catch(Exception e)
                {
                    throw new IOException("Failed to temporarily clear parent field for migration", e);
                }
            }

            try
            {
                JsonElement delegateElement = ObjectSaver.CLEAN_GSON.toJsonTree(value, clazz);
                if(delegateElement.isJsonObject())
                {
                    JsonObject jsonObject = delegateElement.getAsJsonObject();
                    for(Map.Entry<String, JsonElement> entry : jsonObject.entrySet())
                    {
                        if(entry.getKey().equals("className") || entry.getKey().equals("bytes"))
                            continue;

                        out.name(entry.getKey());
                        ObjectSaver.CLEAN_GSON.toJson(entry.getValue(), out);
                    }
                }
            }
            finally
            {
                if(isImplInstance && parentField != null)
                {
                    try
                    {
                        parentField.set(value, originalParent);
                    }
                    catch(Exception ignored) {}
                }
            }
        }

        out.endObject();
    }

    @Override
    public NpcClickAction read(JsonReader in) throws IOException
    {
        if(in.peek() == com.google.gson.stream.JsonToken.NULL)
        {
            in.nextNull();
            return null;
        }

        JsonObject jsonObject = JsonParser.parseReader(in).getAsJsonObject();

        if(jsonObject.has("bytes"))
        {
            String base64Bytes = jsonObject.get("bytes").getAsString();
            byte[] bytes = Base64.getDecoder().decode(base64Bytes);

            try(ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
                ObjectInputStream objIn = new ObjectInputStream(byteIn))
            {
                NpcClickAction action = (NpcClickAction) objIn.readObject();
                tryTriggerInitialize(action);
                return action;
            }
            catch(ClassNotFoundException e)
            {
                throw new JsonParseException("Failed to map compiled runtime lambda target context dependencies.", e);
            }
        }

        if(jsonObject.has("className"))
        {
            String className = jsonObject.get("className").getAsString();
            try
            {
                Class<?> targetClass = Class.forName(className);
                NpcClickAction action = (NpcClickAction) ObjectSaver.CLEAN_GSON.fromJson(jsonObject, targetClass);

                if(className.equals(IMPL_CLASS_NAME) && action != null)
                    fixActionParamTypes(action);

                tryTriggerInitialize(action);
                return action;
            }
            catch(ClassNotFoundException e)
            {
                throw new JsonParseException("Could not track down registered class definition: " + className, e);
            }
        }

        throw new JsonParseException("Invalid NpcClickAction JSON object structure. Must contain either 'className' or 'bytes'.");
    }

    /**
     * Reflectively iterates over the loaded actions list and re-serializes any LinkedTreeMap parameters into their true concrete types defined by their
     * option.
     */
    @SuppressWarnings("unchecked")
    private void fixActionParamTypes(NpcClickAction action)
    {
        //TODO: Correct serialization
        try
        {
            Field actionsField = action.getClass().getDeclaredField("actions");
            actionsField.setAccessible(true);
            java.util.List<?> actionsList = (java.util.List<?>) actionsField.get(action);

            if(actionsList == null)
                return;

            for(Object pluginAction : actionsList)
            {
                Field optionField = pluginAction.getClass().getDeclaredField("option");
                Field paramField = pluginAction.getClass().getDeclaredField("param");
                optionField.setAccessible(true);
                paramField.setAccessible(true);

                Object optionObj = optionField.get(pluginAction);
                Object paramObj = paramField.get(pluginAction);

                // Only fix it if Gson parsed it as a generic, untyped LinkedTreeMap
                if(paramObj instanceof java.util.Map && optionObj != null)
                {
                    // Dynamically invoke the new getParamClass() method on the option instance
                    java.lang.reflect.Method getParamClassMethod = optionObj.getClass().getMethod("getParamClass");
                    getParamClassMethod.setAccessible(true);
                    Class<?> trueType = (Class<?>) getParamClassMethod.invoke(optionObj);

                    if(trueType != null)
                    {
                        // Re-serialize the generic map back into its true type-safe object form
                        JsonElement jsonTree = ObjectSaver.CLEAN_GSON.toJsonTree(paramObj);
                        Object typedParam = ObjectSaver.CLEAN_GSON.fromJson(jsonTree, trueType);

                        paramField.set(pluginAction, typedParam);
                    }
                }
            }
        }
        catch(Exception e)
        {
            System.err.println("[NpcPlugin] Failed to dynamically remap erased JSON generic parameters: " + e.getMessage());
        }
    }

    private void tryTriggerInitialize(NpcClickAction action)
    {
        if(action != null && action.getClass().getName().endsWith(IMPL_CLASS_NAME))
        {
            try
            {
                action.initialize();
            }
            catch(Exception ignored) {}
        }
    }
}