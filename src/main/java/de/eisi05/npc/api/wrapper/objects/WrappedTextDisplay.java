package de.eisi05.npc.api.wrapper.objects;

import de.eisi05.npc.api.objects.NameDisplayOptions;
import de.eisi05.npc.api.utils.Reflections;
import de.eisi05.npc.api.utils.Var;
import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V26_2), path = "net.minecraft.world.entity.Display$TextDisplay")
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
     * <p>
     * Default: (0.0, 0.25, 0.0)
     *
     * @param x the X offset
     * @param y the Y offset
     * @param z the Z offset
     * @return this instance for chaining
     */
    public WrappedTextDisplay translation(float x, float y, float z)
    {
        return set(WrappedEntityData.EntityDataSerializers.VECTOR3.create(11), new Vector3f(x, y, z));
    }

    /**
     * Sets the scale of the nametag.
     * <p>
     * Default: (1.0, 1.0, 1.0)
     *
     * @param x the X scale
     * @param y the Y scale
     * @param z the Z scale
     * @return this instance for chaining
     */
    public WrappedTextDisplay scale(float x, float y, float z)
    {
        return set(WrappedEntityData.EntityDataSerializers.VECTOR3.create(12), new Vector3f(x, y, z));
    }

    /**
     * Sets the billboard alignment constraints. Default: CENTER
     *
     * @param constraints BillboardConstraints enum.
     * @return This instance for chaining.
     */
    public WrappedTextDisplay billboardConstraints(BillboardConstraints constraints)
    {
        return set(WrappedEntityData.EntityDataSerializers.BYTE.create(15), (byte) constraints.ordinal());
    }

    /**
     * Sets brightness override. Default: -1
     *
     * @param brightness Brightness value.
     * @return This instance for chaining.
     */
    public WrappedTextDisplay brightnessOverride(int brightness)
    {
        return set(WrappedEntityData.EntityDataSerializers.INT.create(16), brightness);
    }

    /**
     * Sets the viewing range of the nametag. Default: 1.0
     *
     * @param range View range.
     * @return This instance for chaining.
     */
    public WrappedTextDisplay viewRange(float range)
    {
        return set(WrappedEntityData.EntityDataSerializers.FLOAT.create(17), range);
    }

    /**
     * Sets the shadow radius. Default: 0.0
     *
     * @param radius Shadow radius.
     * @return This instance for chaining.
     */
    public WrappedTextDisplay shadowRadius(float radius)
    {
        return set(WrappedEntityData.EntityDataSerializers.FLOAT.create(18), radius);
    }

    /**
     * Sets the shadow strength. Default: 1.0
     *
     * @param strength Shadow strength.
     * @return This instance for chaining.
     */
    public WrappedTextDisplay shadowStrength(float strength)
    {
        return set(WrappedEntityData.EntityDataSerializers.FLOAT.create(19), strength);
    }

    /**
     * Sets the line width of the text. Default: 200
     *
     * @param width Line width.
     * @return This instance for chaining.
     */
    public WrappedTextDisplay lineWidth(int width)
    {
        return set(WrappedEntityData.EntityDataSerializers.INT.create(24), width);
    }

    /**
     * Sets the background color. Default: 1073741824 (0x40000000)
     *
     * @param color Background color as integer.
     * @return This instance for chaining.
     */
    public WrappedTextDisplay backgroundColor(int color)
    {
        return set(WrappedEntityData.EntityDataSerializers.INT.create(25), color);
    }

    /**
     * Sets the text opacity. Default: -1 (fully opaque)
     *
     * @param opacity Text opacity.
     * @return This instance for chaining.
     */
    public WrappedTextDisplay textOpacity(byte opacity)
    {
        return set(WrappedEntityData.EntityDataSerializers.BYTE.create(26), opacity);
    }

    /**
     * Sets flags including shadow, see-through, background color, and alignment. Default: NONE
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
     * @param displayOptions The display options to apply.
     * @return The WrappedEntityData after applying values.
     */
    public WrappedEntityData applyData(WrappedComponent component, @NotNull NameDisplayOptions displayOptions)
    {
        WrappedEntityData data = getEntityData();
        data.set(WrappedEntityData.EntityDataSerializers.OPTIONAL_CHAT_COMPONENT.create(2), Optional.of(component.getHandle()));
        data.set(WrappedEntityData.EntityDataSerializers.BOOLEAN.create(4), true);

        translation(0, 0.25f + displayOptions.getHeight(), 0);
        billboardConstraints(BillboardConstraints.CENTER);

        float[] scale = displayOptions.getScale();
        scale(scale[0], scale[1], scale[2]);

        int brightness = displayOptions.getBrightness();
        brightnessOverride(brightness);

        float viewRange = displayOptions.getViewRange();
        viewRange(viewRange);

        int lineWidth = displayOptions.getLineWidth();
        lineWidth(lineWidth);

        int backgroundColor = displayOptions.getBackgroundColor();
        backgroundColor(backgroundColor);

        byte textOpacity = displayOptions.getTextOpacity();
        textOpacity(textOpacity);


        boolean isSeeThrough = displayOptions.isSeeThrough();
        TextDisplay.TextAlignment alignment = displayOptions.getAlignment();
        TextDisplayFlags alignmentFlag = switch(alignment)
        {
            case LEFT -> TextDisplayFlags.LEFT_ALIGNMENT;
            case CENTER -> TextDisplayFlags.CENTER_ALIGNMENT;
            case RIGHT -> TextDisplayFlags.RIGHT_ALIGNMENT;
        };

        if(isSeeThrough)
           flags(alignmentFlag, TextDisplayFlags.IS_SEE_THROUGH);
        else
            flags(alignmentFlag);

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
        LEFT_ALIGNMENT((byte) 0x08),
        RIGHT_ALIGNMENT((byte) 0x10);

        private static final byte ALIGNMENT_MASK = (byte) 0x18;
        private final byte flag;

        TextDisplayFlags(byte flag) {this.flag = flag;}

        public static byte combineFlags(TextDisplayFlags... flags)
        {
            byte result = 0;
            boolean alignmentSet = false;

            for(TextDisplayFlags flag : flags)
            {
                boolean isAlignment = (flag == CENTER_ALIGNMENT || flag == LEFT_ALIGNMENT || flag == RIGHT_ALIGNMENT);

                if(isAlignment)
                {
                    if(alignmentSet)
                        throw new IllegalArgumentException("You cannot clear or set multiple alignments at once!");
                    result = (byte) ((result & ~ALIGNMENT_MASK) | flag.flag);
                    alignmentSet = true;
                }
                else
                    result |= flag.flag;
            }
            return result;
        }
    }
}
