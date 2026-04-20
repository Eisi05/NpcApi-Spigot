package de.eisi05.npc.api.utils;

import java.io.Serializable;
import java.util.function.Predicate;

/**
 * A serializable predicate that can be used in contexts where the predicate needs to be persisted. This extends both {@link Predicate} and {@link Serializable}
 * to allow functional interfaces that test conditions to be serialized (e.g., for NPC AI goal target filtering).
 *
 * @param <T> The type of input to the predicate
 */
@FunctionalInterface
public interface SerializablePredicate<T> extends Predicate<T>, Serializable
{
}
