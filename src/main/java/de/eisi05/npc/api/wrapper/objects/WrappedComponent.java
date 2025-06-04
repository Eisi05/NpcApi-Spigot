package de.eisi05.npc.api.wrapper.objects;

import de.eisi05.npc.api.utils.Var;
import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.Wrapper;
import de.eisi05.npc.api.wrapper.enums.ChatFormat;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.UnaryOperator;

@Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_5), path = "net.minecraft.network.chat.IChatBaseComponent")
public class WrappedComponent extends Wrapper
{
    private WrappedComponent(Object handle)
    {
        super(handle);
    }

    public static WrappedComponent fromHandle(@Nullable Object handle)
    {
        return new WrappedComponent(handle);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_5), path = "a")
    public static @NotNull WrappedComponent create(@Nullable String text)
    {
        return new WrappedComponent(text == null ? CommonComponents.EMPTY : invokeStaticWrappedMethod(text));
    }

    public static @NotNull WrappedComponent parseFromLegacy(@Nullable String legacy)
    {
        if(legacy == null || legacy.isEmpty())
            return create(null);

        return Var.fromString(legacy);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_5), path = "a")
    public @NotNull WrappedComponent setFormats(@NotNull ChatFormat... format)
    {
        if(format.length == 0)
            return this;

        try
        {
            Method[] methods = getHandle().getClass().getMethods();

            String path = getPath();

            Method target = Arrays.stream(methods)
                    .filter(m -> m.getName().equals(path))
                    .filter(Method::isVarArgs)
                    .filter(method -> method.getParameterTypes()[0].isArray())
                    .filter(method -> method.getParameterTypes()[0].componentType().equals(format[0].getHandle().getClass()))
                    .findFirst()
                    .orElseThrow();

            target.invoke(getHandle(), (Object) Arrays.stream(format)
                    .map(ChatFormat::getHandle)
                    .toArray(i -> (Object[]) Array.newInstance(format[0].getHandle().getClass(), i)));
        } catch(IllegalAccessException | InvocationTargetException | NoSuchElementException e)
        {
        }
        return this;
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_18, to = Versions.V1_21_5), path = "a")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_17), path = "format")
    public @NotNull WrappedComponent withClickEvent(@NotNull ClickEvent clickEvent)
    {
        invokeWrappedMethod((UnaryOperator<Object>) o -> new Style(o).withClickEvent(clickEvent).getHandle());
        return this;
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_18, to = Versions.V1_21_5), path = "a")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_17), path = "format")
    public @NotNull WrappedComponent withHoverEvent(@NotNull HoverEvent hoverEvent)
    {
        invokeWrappedMethod((UnaryOperator<Object>) o -> new Style(o).withHoverEvent(hoverEvent).getHandle());
        return this;
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_19_1, to = Versions.V1_21_5), path = "b")
    @Mapping(range = @Mapping.Range(from = Versions.V1_18, to = Versions.V1_19), path = "a")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_17), path = "addSibling")
    public @NotNull WrappedComponent append(WrappedComponent wrappedComponent)
    {
        invokeWrappedMethod(wrappedComponent);
        return this;
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_20_4, to = Versions.V1_21_5), path = "f")
    @Mapping(range = @Mapping.Range(from = Versions.V1_18, to = Versions.V1_20_2), path = "e")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_17), path = "mutableCopy")
    public @NotNull WrappedComponent copy()
    {
        return new WrappedComponent(invokeWrappedMethod());
    }

    public @NotNull String toLegacy()
    {
        return Var.toString(this);
    }

    public @NotNull String toLegacyNoColor()
    {
        return ChatColor.stripColor(toLegacy());
    }

    public @NotNull SerializedComponent serialize()
    {
        return new SerializedComponent(toLegacy());
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_19, to = Versions.V1_21_5), path = "net.minecraft.network.chat.CommonComponents")
    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_18_2), path = "net.minecraft.network.chat.ChatComponentText")
    private static class CommonComponents extends Wrapper
    {
        @Mapping(range = @Mapping.Range(from = Versions.V1_19, to = Versions.V1_21_5), path = "a")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_18_2), path = "d")
        private static final Object EMPTY = getStaticWrappedFieldValue("EMPTY").orElse(null);

        private CommonComponents()
        {
            super(null);
        }
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_5), path = "net.minecraft.network.chat.ChatClickable")
    public static class ClickEvent extends Wrapper
    {
        private ClickEvent(Object handle)
        {
            super(handle);
        }

        public static @NotNull ClickEvent create(@NotNull ClickAction action, @NotNull Object value)
        {
            if(Versions.isCurrentVersionSmallerThan(Versions.V1_21_5))
                return createWrappedInstance(ClickEvent.class, action, value);
            return action.function.apply(value);
        }

        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_5),
                path = "net.minecraft.network.chat.ChatClickable$EnumClickAction")
        public enum ClickAction implements EnumWrapper
        {
            @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_5), path = "a")
            OPEN_URL(o -> new OpenUrl((URI) o)),

            @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_5), path = "b")
            OPEN_FILE(o -> new OpenFile((String) o)),

            @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_5), path = "c")
            RUN_COMMAND(o -> new RunCommand((String) o)),

            @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_5), path = "d")
            SUGGEST_COMMAND(o -> new SuggestCommand((String) o)),

            @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_5), path = "e")
            CHANGE_PAGE(o -> new ChangePage((int) o)),

            @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_5), path = "f")
            COPY_TO_CLIPBOARD(o -> new CopyToClipboard((String) o));

            private final Function<Object, ClickEvent> function;

            ClickAction(Function<Object, ClickEvent> function)
            {
                this.function = function;
            }

            @Override
            public @NotNull Object getHandle()
            {
                return cast(this);
            }
        }

        @Mapping(range = @Mapping.Range(from = Versions.V1_21_5, to = Versions.V1_21_5),
                path = "net.minecraft.network.chat.ChatClickable$OpenUrl")
        private static class OpenUrl extends WrappedComponent.ClickEvent
        {
            private OpenUrl(URI url)
            {
                super(createInstance(OpenUrl.class, url));
            }
        }

        @Mapping(range = @Mapping.Range(from = Versions.V1_21_5, to = Versions.V1_21_5),
                path = "net.minecraft.network.chat.ChatClickable$OpenFile")
        private static class OpenFile extends WrappedComponent.ClickEvent
        {
            private OpenFile(String file)
            {
                super(createInstance(OpenFile.class, file));
            }
        }

        @Mapping(range = @Mapping.Range(from = Versions.V1_21_5, to = Versions.V1_21_5),
                path = "net.minecraft.network.chat.ChatClickable$RunCommand")
        private static class RunCommand extends WrappedComponent.ClickEvent
        {
            private RunCommand(String command)
            {
                super(createInstance(RunCommand.class, command));
            }
        }

        @Mapping(range = @Mapping.Range(from = Versions.V1_21_5, to = Versions.V1_21_5),
                path = "net.minecraft.network.chat.ChatClickable$SuggestCommand")
        private static class SuggestCommand extends WrappedComponent.ClickEvent
        {
            private SuggestCommand(String command)
            {
                super(createInstance(SuggestCommand.class, command));
            }
        }

        @Mapping(range = @Mapping.Range(from = Versions.V1_21_5, to = Versions.V1_21_5),
                path = "net.minecraft.network.chat.ChatClickable$ChangePage")
        private static class ChangePage extends WrappedComponent.ClickEvent
        {
            private ChangePage(int page)
            {
                super(createInstance(ChangePage.class, page));
            }
        }

        @Mapping(range = @Mapping.Range(from = Versions.V1_21_5, to = Versions.V1_21_5),
                path = "net.minecraft.network.chat.ChatClickable$CopyToClipboard")
        private static class CopyToClipboard extends WrappedComponent.ClickEvent
        {
            private CopyToClipboard(String string)
            {
                super(createInstance(CopyToClipboard.class, string));
            }
        }
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_5), path = "net.minecraft.network.chat.ChatHoverable")
    public static class HoverEvent extends Wrapper
    {

        private HoverEvent(Object handle)
        {
            super(handle);
        }

        public static HoverEvent create(@NotNull WrappedComponent component)
        {
            if(Versions.isCurrentVersionSmallerThan(Versions.V1_21_5))
                return createWrappedInstance(HoverEvent.class, HoverAction.SHOW_TEXT, component);
            return new ShowText(component);
        }

        @Mapping(range = @Mapping.Range(from = Versions.V1_21_5, to = Versions.V1_21_5), path = "net.minecraft.network.chat.ChatHoverable$e")
        private static class ShowText extends HoverEvent
        {

            private ShowText(@NotNull WrappedComponent component)
            {
                super(createInstance(ShowText.class, component));
            }
        }

        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_5),
                path = "net.minecraft.network.chat.ChatHoverable$EnumHoverAction")
        private static class HoverAction extends Wrapper
        {
            @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_4), path = "a")
            private static final Object SHOW_TEXT = getStaticWrappedFieldValue("SHOW_TEXT").orElse(null);

            private HoverAction()
            {
                super(null);
            }
        }
    }

    public static class SerializedComponent implements Serializable
    {
        @Serial
        private static final long serialVersionUID = 1L;

        private final String text;

        public SerializedComponent(@Nullable String text)
        {
            this.text = text;
        }

        public @NotNull WrappedComponent deserialize()
        {
            return WrappedComponent.parseFromLegacy(text);
        }
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_5), path = "net.minecraft.network.chat.ChatModifier")
    public static class Style extends Wrapper
    {
        private Style(Object handle)
        {
            super(handle);
        }

        @Mapping(range = @Mapping.Range(from = Versions.V1_18, to = Versions.V1_21_5), path = "a")
        @Mapping(fixed = @Mapping.Fixed(Versions.V1_17), path = "setChatClickable")
        public @NotNull Style withClickEvent(@NotNull ClickEvent clickEvent)
        {
            return new Style(invokeWrappedMethod(clickEvent));
        }

        @Mapping(range = @Mapping.Range(from = Versions.V1_18, to = Versions.V1_21_5), path = "a")
        @Mapping(fixed = @Mapping.Fixed(Versions.V1_17), path = "setChatHoverable")
        public @NotNull Style withHoverEvent(@NotNull HoverEvent hoverEvent)
        {
            return new Style(invokeWrappedMethod(hoverEvent));
        }
    }
}
