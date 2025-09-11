package de.eisi05.npc.api.wrapper.objects;

import de.eisi05.npc.api.utils.Reflections;
import de.eisi05.npc.api.utils.Var;
import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.joml.Vector3f;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_21_6), path = "net.minecraft.world.entity.Display$TextDisplay")
public class WrappedTextDisplay extends WrappedEntity.WrappedNameTag<Entity>
{
    private final Map<WrappedEntityData.EntityDataAccessor<?>, Object> dataMap = new LinkedHashMap<>();

    protected WrappedTextDisplay(Object handle)
    {
        super(handle);
    }

    public static WrappedTextDisplay create(World world)
    {
        return new WrappedTextDisplay(
                createInstance(WrappedTextDisplay.class, EntityTypes.TEXT_DISPLAY, Reflections.invokeMethod(world, "getHandle").get()));
    }

    private <T> WrappedTextDisplay set(WrappedEntityData.EntityDataAccessor<T> accessor, T value)
    {
        dataMap.put(accessor, value);
        return this;
    }

    /**
     * Sets the translation offset of the nametag.
     * Default: (0.0, 0.25, 0.0)
     *
     * @param vector Translation vector.
     * @return This instance for chaining.
     */
    public WrappedTextDisplay translation(Vector vector)
    {
        return set(WrappedEntityData.EntityDataSerializers.VECTOR3.create(11),
                new Vector3f(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ()));
    }

    /**
     * Sets the scale of the nametag.
     * Default: (1.0, 1.0, 1.0)
     *
     * @param vector Scale vector.
     * @return This instance for chaining.
     */
    public WrappedTextDisplay scale(Vector vector)
    {
        return set(WrappedEntityData.EntityDataSerializers.VECTOR3.create(12),
                new Vector3f(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ()));
    }

    /**
     * Sets the billboard alignment constraints.
     * Default: CENTER
     *
     * @param constraints BillboardConstraints enum.
     * @return This instance for chaining.
     */
    public WrappedTextDisplay billboardConstraints(BillboardConstraints constraints)
    {
        return set(WrappedEntityData.EntityDataSerializers.BYTE.create(15), (byte) constraints.ordinal());
    }

    /**
     * Sets brightness override.
     * Default: -1
     *
     * @param brightness Brightness value.
     * @return This instance for chaining.
     */
    public WrappedTextDisplay brightnessOverride(int brightness)
    {
        return set(WrappedEntityData.EntityDataSerializers.INT.create(16), brightness);
    }

    /**
     * Sets the viewing range of the nametag.
     * Default: 1.0
     *
     * @param range View range.
     * @return This instance for chaining.
     */
    public WrappedTextDisplay viewRange(float range)
    {
        return set(WrappedEntityData.EntityDataSerializers.FLOAT.create(17), range);
    }

    /**
     * Sets the shadow radius.
     * Default: 0.0
     *
     * @param radius Shadow radius.
     * @return This instance for chaining.
     */
    public WrappedTextDisplay shadowRadius(float radius)
    {
        return set(WrappedEntityData.EntityDataSerializers.FLOAT.create(18), radius);
    }

    /**
     * Sets the shadow strength.
     * Default: 1.0
     *
     * @param strength Shadow strength.
     * @return This instance for chaining.
     */
    public WrappedTextDisplay shadowStrength(float strength)
    {
        return set(WrappedEntityData.EntityDataSerializers.FLOAT.create(19), strength);
    }

    /**
     * Sets the width of the nametag.
     * Default: 0.0
     *
     * @param width Width value.
     * @return This instance for chaining.
     */
    public WrappedTextDisplay width(float width)
    {
        return set(WrappedEntityData.EntityDataSerializers.FLOAT.create(20), width);
    }

    /**
     * Sets the height of the nametag.
     * Default: 1.0
     *
     * @param height Height value.
     * @return This instance for chaining.
     */
    public WrappedTextDisplay height(float height)
    {
        return set(WrappedEntityData.EntityDataSerializers.FLOAT.create(21), height);
    }

    /**
     * Sets a glow color override.
     * Default: -1
     *
     * @param color Glow color as integer.
     * @return This instance for chaining.
     */
    public WrappedTextDisplay glowColorOverride(int color)
    {
        return set(WrappedEntityData.EntityDataSerializers.INT.create(22), color);
    }

    /**
     * Sets the line width of the text.
     * Default: 200
     *
     * @param width Line width.
     * @return This instance for chaining.
     */
    public WrappedTextDisplay lineWidth(int width)
    {
        return set(WrappedEntityData.EntityDataSerializers.INT.create(24), width);
    }

    /**
     * Sets the background color.
     * Default: 1073741824 (0x40000000)
     *
     * @param color Background color as integer.
     * @return This instance for chaining.
     */
    public WrappedTextDisplay backgroundColor(int color)
    {
        return set(WrappedEntityData.EntityDataSerializers.INT.create(25), color);
    }

    /**
     * Sets the text opacity.
     * Default: -1 (fully opaque)
     *
     * @param opacity Text opacity.
     * @return This instance for chaining.
     */
    public WrappedTextDisplay textOpacity(byte opacity)
    {
        return set(WrappedEntityData.EntityDataSerializers.BYTE.create(26), opacity);
    }

    /**
     * Sets flags including shadow, see-through, background color, and alignment.
     * Default: NONE
     *
     * @param flags Varargs of TextDisplayFlags.
     * @return This instance for chaining.
     */
    public WrappedTextDisplay flags(TextDisplayFlags... flags)
    {
        return set(WrappedEntityData.EntityDataSerializers.BYTE.create(27), TextDisplayFlags.combineFlags(flags));
    }

    /**
     * Applies all configured data to the given TextDisplay and component.
     *
     * @param component The text component to display.
     * @return The WrappedEntityData after applying values.
     */
    public WrappedEntityData applyData(WrappedComponent component)
    {
        WrappedEntityData data = getEntityData();
        data.set(WrappedEntityData.EntityDataSerializers.OPTIONAL_CHAT_COMPONENT.create(2), Optional.of(component.getHandle()));
        data.set(WrappedEntityData.EntityDataSerializers.BOOLEAN.create(4), true);
        data.set(WrappedEntityData.EntityDataSerializers.VECTOR3.create(11), new Vector3f(0, 0.25f, 0));
        data.set(WrappedEntityData.EntityDataSerializers.BYTE.create(15), (byte) 3);
        data.set(WrappedEntityData.EntityDataSerializers.CHAT_COMPONENT.create(23), Var.unsafeCast(component.getHandle()));

        dataMap.forEach((accessor, value) -> data.set(accessor, Var.unsafeCast(value)));

        return data;
    }

    /**
     * Alignment constraints for TextDisplay nametags.
     */
    public enum BillboardConstraints
    {
        FIXED,
        VERTICAL,
        HORIZONTAL,
        CENTER
    }

    /**
     * Flags for text display, including shadow, see-through, background, and alignment.
     */
    public enum TextDisplayFlags
    {
        NONE((byte) 0x00),
        HAS_SHADOW((byte) 0x01),
        IS_SEE_THROUGH((byte) 0x02),
        USE_DEFAULT_BACKGROUND_COLOR((byte) 0x04),
        CENTER_ALIGNMENT((byte) 0x00),
        LEFT_ALIGNMENT((byte) 0x01),
        RIGHT_ALIGNMENT((byte) 0x02);

        private final byte flag;

        TextDisplayFlags(byte flag) {this.flag = flag;}

        public static byte combineFlags(TextDisplayFlags... flags)
        {
            byte result = 0;
            for(TextDisplayFlags flag : flags)
                result |= flag.flag;
            return result;
        }
    }
}
