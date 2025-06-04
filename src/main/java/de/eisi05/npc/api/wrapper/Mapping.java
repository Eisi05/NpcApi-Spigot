package de.eisi05.npc.api.wrapper;

import de.eisi05.npc.api.utils.Versions;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * The {@link Mapping} annotation is used to define version-specific paths or behaviors
 * for methods, fields, constructors, or types. It allows specifying either a fixed
 * version or a range of versions for which a particular mapping is valid.
 * This annotation is {@link Repeatable}, meaning multiple {@code @Mapping} annotations
 * can be applied to the same element.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE_USE, ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR})
@Repeatable(Mapping.WrapList.class)
public @interface Mapping
{
    /**
     * Specifies a fixed {@link Versions} enum entry for which this mapping is valid.
     * If set to {@link Versions#NONE}, the mapping is considered to be defined by a range instead.
     *
     * @return The fixed version for this mapping. Defaults to {@link Versions#NONE}.
     */
    @NotNull Fixed fixed() default @Fixed(Versions.NONE);

    /**
     * Specifies a range of {@link Versions} enum entries for which this mapping is valid.
     * This is used when the mapping applies to a continuous set of versions.
     * If both {@code fixed()} and {@code range()} are specified, {@code fixed()} takes precedence
     * if its value is not {@link Versions#NONE}.
     *
     * @return The version range for this mapping. Defaults to a range from {@link Versions#NONE} to {@link Versions#NONE}.
     */
    @NotNull Range range() default @Range(from = Versions.NONE, to = Versions.NONE);

    /**
     * The version-specific path or string associated with this mapping.
     * This could be a class name, a method name, a field name, or any other string
     * that changes across different Minecraft versions.
     *
     * @return The path string for this mapping. Must not be {@code null}.
     */
    @NotNull String path();

    /**
     * A container annotation for repeatable {@link Mapping} annotations.
     * Java automatically uses this when multiple {@code @Mapping} annotations
     * are applied to the same program element.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE_USE, ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR})
    @interface WrapList
    {
        /**
         * The array of {@link Mapping} annotations.
         *
         * @return An array of {@link Mapping} instances. Must not be {@code null}.
         */
        @NotNull Mapping[] value();
    }

    /**
     * Defines a fixed {@link Versions} enum entry for a {@link Mapping} annotation.
     * This nested annotation is used to specify that a mapping is valid for one particular version.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE_USE, ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR})
    @interface Fixed
    {
        /**
         * The specific {@link Versions} enum entry for which the mapping is valid.
         *
         * @return The fixed version. Must not be {@code null}.
         */
        @NotNull Versions value();
    }

    /**
     * Defines a range of {@link Versions} enum entries for a {@link Mapping} annotation.
     * This nested annotation is used to specify that a mapping is valid for a continuous
     * set of versions.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE_USE, ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR})
    @interface Range
    {
        /**
         * The starting {@link Versions} enum entry of the range (inclusive).
         *
         * @return The starting version of the range. Must not be {@code null}.
         */
        @NotNull Versions from();

        /**
         * The ending {@link Versions} enum entry of the range (inclusive).
         *
         * @return The ending version of the range. Must not be {@code null}.
         */
        @NotNull Versions to();
    }
}
