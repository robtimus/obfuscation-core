/*
 * Obfuscator.java
 * Copyright 2020 Rob Spoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.robtimus.obfuscation;

import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.checkOffsetAndLength;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.checkStartAndEnd;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.concat;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.copyAll;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.discardAll;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.getChars;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.maskAll;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.readAll;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.readAtMost;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.repeatChar;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.wrapArray;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import com.github.robtimus.obfuscation.support.CachingObfuscatingWriter;
import com.github.robtimus.obfuscation.support.ObfuscatingWriter;
import com.github.robtimus.obfuscation.support.ObfuscatorUtils;

/**
 * An object that will obfuscate {@link CharSequence CharSequences} or the contents of {@link Reader Readers}.
 *
 * @author Rob Spoor
 */
public abstract class Obfuscator {

    private static final char DEFAULT_MASK_CHAR = '*';

    /**
     * Obfuscates the contents of a {@code CharSequence}.
     *
     * @param s The {@code CharSequence} with the contents to obfuscate.
     * @return The obfuscated contents.
     * @throws NullPointerException If the given {@code CharSequence} is {@code null}.
     */
    public CharSequence obfuscateText(CharSequence s) {
        return obfuscateText(s, 0, s.length());
    }

    /**
     * Obfuscates parts of the contents of a {@code CharSequence}.
     *
     * @param s The {@code CharSequence} with the contents to obfuscate.
     * @param start The index in the {@code CharSequence} to start obfuscating, inclusive.
     * @param end The index in the {@code CharSequence} to end obfuscating, exclusive.
     * @return The obfuscated contents.
     * @throws NullPointerException If the given {@code CharSequence} is {@code null}.
     * @throws IndexOutOfBoundsException If the given start index is negative or larger than the given end index,
     *                                       or if the given end index is larger than the given {@code CharSequence}'s length.
     */
    public abstract CharSequence obfuscateText(CharSequence s, int start, int end);

    /**
     * Obfuscates the contents of a {@code CharSequence}.
     *
     * @param s The {@code CharSequence} with the contents to obfuscate.
     * @param destination The {@code StringBuilder} to append the obfuscated contents to.
     * @throws NullPointerException If the given {@code CharSequence} or {@code StringBuilder} is {@code null}.
     */
    public void obfuscateText(CharSequence s, StringBuilder destination) {
        obfuscateText(s, 0, s.length(), destination);
    }

    /**
     * Obfuscates parts of the contents of a {@code CharSequence}.
     *
     * @param s The {@code CharSequence} with the contents to obfuscate.
     * @param start The index in the {@code CharSequence} to start obfuscating, inclusive.
     * @param end The index in the {@code CharSequence} to end obfuscating, exclusive.
     * @param destination The {@code StringBuilder} to append the obfuscated contents to.
     * @throws NullPointerException If the given {@code CharSequence} or {@code StringBuilder} is {@code null}.
     * @throws IndexOutOfBoundsException If the given start index is negative or larger than the given end index,
     *                                       or if the given end index is larger than the given {@code CharSequence}'s length.
     */
    public void obfuscateText(CharSequence s, int start, int end, StringBuilder destination) {
        try {
            obfuscateText(s, start, end, (Appendable) destination);
        } catch (IOException e) {
            // should not occur
            throw new IllegalStateException(e);
        }
    }

    /**
     * Obfuscates the contents of a {@code CharSequence}.
     *
     * @param s The {@code CharSequence} with the contents to obfuscate.
     * @param destination The {@code StringBuffer} to append the obfuscated contents to.
     * @throws NullPointerException If the given {@code CharSequence} or {@code StringBuffer} is {@code null}.
     */
    public void obfuscateText(CharSequence s, StringBuffer destination) {
        obfuscateText(s, 0, s.length(), destination);
    }

    /**
     * Obfuscates parts of the contents of a {@code CharSequence}.
     *
     * @param s The {@code CharSequence} with the contents to obfuscate.
     * @param start The index in the {@code CharSequence} to start obfuscating, inclusive.
     * @param end The index in the {@code CharSequence} to end obfuscating, exclusive.
     * @param destination The {@code StringBuffer} to append the obfuscated contents to.
     * @throws NullPointerException If the given {@code CharSequence} or {@code StringBuffer} is {@code null}.
     * @throws IndexOutOfBoundsException If the given start index is negative or larger than the given end index,
     *                                       or if the given end index is larger than the given {@code CharSequence}'s length.
     */
    public void obfuscateText(CharSequence s, int start, int end, StringBuffer destination) {
        try {
            obfuscateText(s, start, end, (Appendable) destination);
        } catch (IOException e) {
            // should not occur
            throw new IllegalStateException(e);
        }
    }

    /**
     * Obfuscates the contents of a {@code CharSequence}.
     *
     * @param s The {@code CharSequence} with the contents to obfuscate.
     * @param destination The {@code Appendable} to append the obfuscated contents to.
     * @throws NullPointerException If the given {@code CharSequence} or {@code Appendable} is {@code null}.
     * @throws IOException If an I/O error occurs.
     */
    public void obfuscateText(CharSequence s, Appendable destination) throws IOException {
        obfuscateText(s, 0, s.length(), destination);
    }

    /**
     * Obfuscates parts of the contents of a {@code CharSequence}.
     *
     * @param s The {@code CharSequence} with the contents to obfuscate.
     * @param start The index in the {@code CharSequence} to start obfuscating, inclusive.
     * @param end The index in the {@code CharSequence} to end obfuscating, exclusive.
     * @param destination The {@code Appendable} to append the obfuscated contents to.
     * @throws NullPointerException If the given {@code CharSequence} or {@code Appendable} is {@code null}.
     * @throws IndexOutOfBoundsException If the given start index is negative or larger than the given end index,
     *                                       or if the given end index is larger than the given {@code CharSequence}'s length.
     * @throws IOException If an I/O error occurs.
     */
    public abstract void obfuscateText(CharSequence s, int start, int end, Appendable destination) throws IOException;

    /**
     * Obfuscates the contents of a {@code Reader}.
     *
     * @param input The {@code Reader} with the contents to obfuscate.
     * @return The obfuscated contents.
     * @throws NullPointerException If the given {@code Reader} is {@code null}.
     * @throws IOException If an I/O error occurs.
     */
    public CharSequence obfuscateText(Reader input) throws IOException {
        StringBuilder sb = new StringBuilder();
        obfuscateText(input, sb);
        return sb;
    }

    /**
     * Obfuscates the contents of a {@code Reader}.
     *
     * @param input The {@code Reader} with the contents to obfuscate.
     * @param destination The {@code Appendable} to append the obfuscated contents to.
     * @throws NullPointerException If the given {@code Reader} or {@code Appendable} is {@code null}.
     * @throws IOException If an I/O error occurs.
     */
    public abstract void obfuscateText(Reader input, Appendable destination) throws IOException;

    /**
     * Obfuscates a value. The value's {@link Object#toString() string representation} will be used to obfuscate the value.
     *
     * @param <T> The type of value to obfuscate.
     * @param value The value to obfuscate.
     * @return An {@code Obfuscated} wrapper around the given value.
     * @throws NullPointerException If the given value is {@code null}.
     */
    public final <T> Obfuscated<T> obfuscateObject(T value) {
        return Obfuscated.of(value, this, value::toString);
    }

    /**
     * Obfuscates a value.
     *
     * @param <T> The type of value to obfuscate.
     * @param value The value to obfuscate.
     * @param representation A supplier for the string representation that will be used to obfuscate the value.
     *                           This can be used for values that don't have a sensible {@link Object#toString() string representation} of their own.
     * @return An {@code Obfuscated} wrapper around the given value.
     * @throws NullPointerException If the given value is or supplier is {@code null}.
     */
    public final <T> Obfuscated<T> obfuscateObject(T value, Supplier<? extends CharSequence> representation) {
        return Obfuscated.of(value, this, representation);
    }

    /**
     * Obfuscates a list. For each element, the element's {@link Object#toString() string representation} will be used to obfuscate the element.
     * <p>
     * The result will be a list that will behave exactly the same as the given list, except it will obfuscate each element when its
     * {@link Object#toString() toString()} method is called. This is different from {@link #obfuscateObject(Object)} because it will not obfuscate
     * the list structure or the number of elements.
     *
     * @param <E> The list's element type.
     * @param list The list to obfuscate.
     * @return An obfuscating list wrapper around the given list.
     * @throws NullPointerException If the given list is {@code null}.
     */
    public final <E> List<E> obfuscateList(List<E> list) {
        Objects.requireNonNull(list);
        return ObfuscatingList.of(list, this::obfuscateText);
    }

    /**
     * Obfuscates a set. For each element, the element's {@link Object#toString() string representation} will be used to obfuscate the element.
     * <p>
     * The result will be a set that will behave exactly the same as the given set, except it will obfuscate each element when its
     * {@link Object#toString() toString()} method is called. This is different from {@link #obfuscateObject(Object)} because it will not obfuscate
     * the set structure or the number of elements.
     *
     * @param <E> The set's element type.
     * @param set The set to obfuscate.
     * @return An obfuscating set wrapper around the given set.
     * @throws NullPointerException If the given set is {@code null}.
     */
    public final <E> Set<E> obfuscateSet(Set<E> set) {
        Objects.requireNonNull(set);
        return new ObfuscatingSet<>(set, this::obfuscateText);
    }

    /**
     * Obfuscates a collection. For each element, the element's {@link Object#toString() string representation} will be used to obfuscate the element.
     * <p>
     * The result will be a collection that will behave exactly the same as the given collection, except it will obfuscate each element when its
     * {@link Object#toString() toString()} method is called. This is different from {@link #obfuscateObject(Object)} because it will not obfuscate
     * the collection structure or the number of elements.
     *
     * @param <E> The collection's element type.
     * @param collection The collection to obfuscate.
     * @return An obfuscating collection wrapper around the given collection.
     * @throws NullPointerException If the given collection is {@code null}.
     */
    public final <E> Collection<E> obfuscateCollection(Collection<E> collection) {
        Objects.requireNonNull(collection);
        return new ObfuscatingCollection<>(collection, this::obfuscateText);
    }

    /**
     * Obfuscates a map. For each value, the value's {@link Object#toString() string representation} will be used to obfuscate the value.
     * <p>
     * The result will be a map that will behave exactly the same as the given map, except it will obfuscate each value when its
     * {@link Object#toString() toString()} method is called. This is different from {@link #obfuscateObject(Object)} because it will not obfuscate
     * the map structure or the number of entries.
     *
     * @param <K> The map's key type.
     * @param <V> The map's value type.
     * @param map The map to obfuscate.
     * @return An obfuscating map wrapper around the given list.
     * @throws NullPointerException If the given map is {@code null}.
     */
    public final <K, V> Map<K, V> obfuscateMap(Map<K, V> map) {
        Objects.requireNonNull(map);
        return new ObfuscatingMap<>(map, (k, v) -> obfuscateText(v));
    }

    /**
     * Returns a writer that, when written to, will obfuscate its contents.
     * The writer should be closed to ensure all obfuscation will take place.
     * Note that closing this writer should not close the given destination if it's {@link Closeable} or {@link AutoCloseable}.
     *
     * @param destination The destination to append the obfuscated contents to.
     * @return A writer that, when written to, will obfuscate its contents.
     */
    public abstract Writer streamTo(Appendable destination);

    /**
     * Creates a prefix that can be used to chain another obfuscator to this obfuscator.
     * For the part up to the given prefix length, this obfuscator will be used; for any remaining content another obfuscator will be used.
     * This makes it possible to easily create complex obfuscators that would otherwise be impossible using any of the other obfuscators provided
     * by this library. For instance, it would be impossible to use only {@link #portion()} to create an obfuscator that does not obfuscate the first
     * 4 characters, then obfuscates <em>at least</em> 8 characters, then does not obfuscate up to 4 characters at the end. With this method it's
     * possible to do that by combining {@link #none()} and {link {@link #portion()}:
     * <pre><code>
     * Obfuscator obfuscator = none().untilLength(4).then(portion()
     *         .keepAtEnd(4)
     *         .atLeastFromStart(8)
     *         .build();
     * </code></pre>
     *
     * @param prefixLength The length of the part to use this obfuscator.
     * @return A prefix that can be used to chain another obfuscator to this obfuscator.
     * @throws IllegalArgumentException If the prefix length is not larger than all previous prefix lengths in a method chain.
     *                                      In other words, each prefix length must be larger than its direct predecessor.
     * @since 1.2
     */
    public final Prefix untilLength(int prefixLength) {
        validatePrefixLength(prefixLength);
        return other -> new CombinedObfuscator(this, prefixLength, other);
    }

    void validatePrefixLength(int prefixLength) {
        if (prefixLength <= 0) {
            throw new IllegalArgumentException(prefixLength + " <= 0"); //$NON-NLS-1$
        }
    }

    /**
     * A prefix of a specific length that uses a specific obfuscator.
     * It can be used to create combined obfuscators that obfuscate {@link CharSequence CharSequences} or the contents of {@link Reader Readers} for
     * the part up to the length of this prefix using the prefix' obfuscator, then the rest with another.
     *
     * @author Rob Spoor
     * @since 1.2
     */
    public interface Prefix {

        /**
         * Returns an immutable obfuscator that first uses the source of this object for the length of this prefix, then another obfuscator.
         * If the length of text to obfuscate is smaller than or equal to the length of this prefix, the other obfuscator will be skipped.
         *
         * @param other The other obfuscator to use for {@link CharSequence CharSequences} or the contents of {@link Reader Readers}
         *                  after the length of this prefix has been exceeded.
         * @return An obfuscator that combines the two obfuscators.
         */
        Obfuscator then(Obfuscator other);
    }

    private static final class CombinedObfuscator extends Obfuscator {

        private final Obfuscator first;
        private final int lengthForFirst;
        private final Obfuscator second;

        private CombinedObfuscator(Obfuscator first, int lengthForFirst, Obfuscator second) {
            this.first = Objects.requireNonNull(first);
            this.lengthForFirst = lengthForFirst;
            this.second = Objects.requireNonNull(second);
        }

        @Override
        public CharSequence obfuscateText(CharSequence s, int start, int end) {
            int splitAt = Math.min(start + lengthForFirst, end);
            CharSequence firstResult = first.obfuscateText(s, start, splitAt);

            if (splitAt == end) {
                return firstResult;
            }
            CharSequence secondResult = second.obfuscateText(s, splitAt, end);
            return concat(firstResult, secondResult);
        }

        @Override
        public void obfuscateText(CharSequence s, int start, int end, StringBuilder destination) {
            int splitAt = Math.min(start + lengthForFirst, end);
            first.obfuscateText(s, start, splitAt, destination);

            if (splitAt < end) {
                second.obfuscateText(s, splitAt, end, destination);
            }
        }

        @Override
        public void obfuscateText(CharSequence s, int start, int end, StringBuffer destination) {
            int splitAt = Math.min(start + lengthForFirst, end);
            first.obfuscateText(s, start, splitAt, destination);

            if (splitAt < end) {
                second.obfuscateText(s, splitAt, end, destination);
            }
        }

        @Override
        public void obfuscateText(CharSequence s, int start, int end, Appendable destination) throws IOException {
            int splitAt = Math.min(start + lengthForFirst, end);
            first.obfuscateText(s, start, splitAt, destination);

            if (splitAt < end) {
                second.obfuscateText(s, splitAt, end, destination);
            }
        }

        @Override
        @SuppressWarnings("resource")
        public void obfuscateText(Reader input, Appendable destination) throws IOException {
            first.obfuscateText(readAtMost(input, lengthForFirst), destination);
            second.obfuscateText(input, destination);
        }

        @Override
        public Writer streamTo(Appendable destination) {
            return new ObfuscatingWriter() {
                private Writer writer = first.streamTo(destination);
                private int remainingForChange = lengthForFirst;

                @Override
                public void write(int c) throws IOException {
                    checkClosed();

                    writer.write(c);
                    changeWriterIfNeeded(1);
                }

                @Override
                public void write(char[] cbuf, int off, int len) throws IOException {
                    checkClosed();
                    checkOffsetAndLength(cbuf, off, len);

                    int remaining = len;
                    int written = 0;
                    if (remainingForChange > 0) {
                        written = Math.min(remainingForChange, len);
                        writer.write(cbuf, off, written);
                        changeWriterIfNeeded(written);
                        remaining -= written;
                    }
                    if (remaining > 0) {
                        writer.write(cbuf, off + written, remaining);
                    }
                }

                @Override
                public void write(String str, int off, int len) throws IOException {
                    checkClosed();
                    checkOffsetAndLength(str, off, len);

                    int remaining = len;
                    int written = 0;
                    if (remainingForChange > 0) {
                        written = Math.min(remainingForChange, len);
                        writer.write(str, off, written);
                        changeWriterIfNeeded(written);
                        remaining -= written;
                    }
                    if (remaining > 0) {
                        writer.write(str, off + written, remaining);
                    }
                }

                @Override
                public Writer append(CharSequence csq) throws IOException {
                    checkClosed();

                    CharSequence cs = csq == null ? "null" : csq; //$NON-NLS-1$
                    return append(cs, 0, cs.length());
                }

                @Override
                public Writer append(CharSequence csq, int start, int end) throws IOException {
                    checkClosed();
                    CharSequence cs = csq == null ? "null" : csq; //$NON-NLS-1$
                    checkStartAndEnd(cs, start, end);

                    int len = end - start;
                    int remaining = len;
                    int written = 0;
                    if (remainingForChange > 0) {
                        written = Math.min(remainingForChange, len);
                        writer.append(cs, start, start + written);
                        changeWriterIfNeeded(written);
                        remaining -= written;
                    }
                    if (remaining > 0) {
                        writer.append(cs, start + written, end);
                    }
                    return this;
                }

                @Override
                public Writer append(char c) throws IOException {
                    write(c);
                    return this;
                }

                private void changeWriterIfNeeded(int written) throws IOException {
                    if (remainingForChange > 0) {
                        remainingForChange -= written;
                        if (remainingForChange == 0) {
                            writer.close();
                            writer = second.streamTo(destination);
                        }
                    }
                }
            };
        }

        @Override
        void validatePrefixLength(int prefixLength) {
            if (prefixLength <= lengthForFirst) {
                throw new IllegalArgumentException(prefixLength + " <= " + lengthForFirst); //$NON-NLS-1$
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || o.getClass() != getClass()) {
                return false;
            }
            CombinedObfuscator other = (CombinedObfuscator) o;
            return first.equals(other.first)
                    && lengthForFirst == other.lengthForFirst
                    && second.equals(other.second);
        }

        @Override
        public int hashCode() {
            return first.hashCode() ^ lengthForFirst ^ second.hashCode();
        }

        @Override
        @SuppressWarnings("nls")
        public String toString() {
            return first + " until length " + lengthForFirst + ", then " + second;
        }
    }

    /**
     * Returns an immutable obfuscator that replaces all characters with {@code *}.
     * The length of obfuscated contents will be as long as the length of the source.
     *
     * @return An obfuscator that replaces all characters with {@code *}.
     */
    public static final Obfuscator all() {
        return AllObfuscator.DEFAULT;
    }

    /**
     * Returns an immutable obfuscator that replaces all characters with a specific character.
     * The length of obfuscated contents will be as long as the length of the source.
     *
     * @param maskChar The character to replace with.
     * @return An obfuscator that replaces all characters with the given mask character.
     */
    public static final Obfuscator all(char maskChar) {
        return maskChar == AllObfuscator.DEFAULT.maskChar ? AllObfuscator.DEFAULT : new AllObfuscator(maskChar);
    }

    private static final class AllObfuscator extends Obfuscator {

        private static final AllObfuscator DEFAULT = new AllObfuscator(DEFAULT_MASK_CHAR);

        private final char maskChar;

        private AllObfuscator(char maskChar) {
            this.maskChar = maskChar;
        }

        @Override
        public CharSequence obfuscateText(CharSequence s) {
            Objects.requireNonNull(s);
            return repeatChar(maskChar, s.length());
        }

        @Override
        public CharSequence obfuscateText(CharSequence s, int start, int end) {
            checkStartAndEnd(s, start, end);
            return repeatChar(maskChar, end - start);
        }

        @Override
        public void obfuscateText(CharSequence s, StringBuilder destination) {
            Objects.requireNonNull(s);
            Objects.requireNonNull(destination);
            int count = s.length();
            if (count > 0) {
                destination.append(repeatChar(maskChar, count));
            }
        }

        @Override
        public void obfuscateText(CharSequence s, int start, int end, StringBuilder destination) {
            checkStartAndEnd(s, start, end);
            if (start < end) {
                destination.append(repeatChar(maskChar, end - start));
            }
        }

        @Override
        public void obfuscateText(CharSequence s, StringBuffer destination) {
            Objects.requireNonNull(s);
            Objects.requireNonNull(destination);
            int count = s.length();
            if (count > 0) {
                destination.append(repeatChar(maskChar, count));
            }
        }

        @Override
        public void obfuscateText(CharSequence s, int start, int end, StringBuffer destination) {
            checkStartAndEnd(s, start, end);
            if (start < end) {
                destination.append(repeatChar(maskChar, end - start));
            }
        }

        @Override
        public void obfuscateText(CharSequence s, Appendable destination) throws IOException {
            Objects.requireNonNull(s);
            ObfuscatorUtils.append(maskChar, s.length(), destination);
        }

        @Override
        public void obfuscateText(CharSequence s, int start, int end, Appendable destination) throws IOException {
            checkStartAndEnd(s, start, end);
            ObfuscatorUtils.append(maskChar, end - start, destination);
        }

        @Override
        public CharSequence obfuscateText(Reader input) throws IOException {
            long count = discardAll(input);
            return repeatChar(maskChar, (int) Math.min(count, Integer.MAX_VALUE));
        }

        @Override
        public void obfuscateText(Reader input, Appendable destination) throws IOException {
            if (destination instanceof Writer) {
                obfuscateText(input, (Writer) destination);
                return;
            }

            maskAll(input, maskChar, destination);
        }

        private void obfuscateText(Reader input, Writer destination) throws IOException {
            char[] buffer = new char[1024];
            char[] mask = new char[buffer.length];
            Arrays.fill(mask, maskChar);

            int len;
            while ((len = input.read(buffer)) != -1) {
                destination.write(mask, 0, len);
            }
        }

        @Override
        public Writer streamTo(Appendable destination) {
            return new ObfuscatingWriter() {

                @Override
                public void write(int c) throws IOException {
                    checkClosed();
                    destination.append(maskChar);
                }

                @Override
                public void write(char[] cbuf, int off, int len) throws IOException {
                    checkClosed();
                    checkOffsetAndLength(cbuf, off, len);
                    ObfuscatorUtils.append(maskChar, len, destination);
                }

                @Override
                public void write(String str, int off, int len) throws IOException {
                    checkClosed();
                    checkOffsetAndLength(str, off, len);
                    ObfuscatorUtils.append(maskChar, len, destination);
                }

                @Override
                public Writer append(CharSequence csq) throws IOException {
                    checkClosed();
                    CharSequence cs = csq == null ? "null" : csq; //$NON-NLS-1$
                    ObfuscatorUtils.append(maskChar, cs.length(), destination);
                    return this;
                }

                @Override
                public Writer append(CharSequence csq, int start, int end) throws IOException {
                    checkClosed();
                    CharSequence cs = csq == null ? "null" : csq; //$NON-NLS-1$
                    checkStartAndEnd(cs, start, end);
                    ObfuscatorUtils.append(maskChar, end - start, destination);
                    return this;
                }

                @Override
                public Writer append(char c) throws IOException {
                    checkClosed();
                    destination.append(maskChar);
                    return this;
                }

                @Override
                public void flush() throws IOException {
                    super.flush();
                    if (destination instanceof Flushable) {
                        ((Flushable) destination).flush();
                    }
                }
            };
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || o.getClass() != getClass()) {
                return false;
            }
            AllObfuscator other = (AllObfuscator) o;
            return maskChar == other.maskChar;
        }

        @Override
        public int hashCode() {
            return maskChar;
        }

        @Override
        @SuppressWarnings("nls")
        public String toString() {
            return Obfuscator.class.getName() + "#all(" + maskChar + ")";
        }
    }

    /**
     * Returns an immutable obfuscator that does not obfuscate anything.
     * This can be used as default value to prevent having to check for {@code null}.
     *
     * @return An obfuscator that does not obfuscate anything.
     */
    public static final Obfuscator none() {
        return NoneObfuscator.INSTANCE;
    }

    private static final class NoneObfuscator extends Obfuscator {

        private static final NoneObfuscator INSTANCE = new NoneObfuscator();

        @Override
        public CharSequence obfuscateText(CharSequence s) {
            return s;
        }

        @Override
        public CharSequence obfuscateText(CharSequence s, int start, int end) {
            checkStartAndEnd(s, start, end);
            return start == 0 && end == s.length() ? s : s.subSequence(start, end);
        }

        @Override
        public void obfuscateText(CharSequence s, StringBuilder destination) {
            Objects.requireNonNull(s);
            destination.append(s);
        }

        @Override
        public void obfuscateText(CharSequence s, int start, int end, StringBuilder destination) {
            checkStartAndEnd(s, start, end);
            destination.append(s, start, end);
        }

        @Override
        public void obfuscateText(CharSequence s, StringBuffer destination) {
            Objects.requireNonNull(s);
            destination.append(s);
        }

        @Override
        public void obfuscateText(CharSequence s, int start, int end, StringBuffer destination) {
            checkStartAndEnd(s, start, end);
            destination.append(s, start, end);
        }

        @Override
        public void obfuscateText(CharSequence s, Appendable destination) throws IOException {
            Objects.requireNonNull(s);
            if (s instanceof String && destination instanceof Writer) {
                ((Writer) destination).write((String) s);
            } else {
                destination.append(s);
            }
        }

        @Override
        public void obfuscateText(CharSequence s, int start, int end, Appendable destination) throws IOException {
            checkStartAndEnd(s, start, end);
            if (s instanceof String && destination instanceof Writer) {
                ((Writer) destination).write((String) s, start, end - start);
            } else {
                destination.append(s, start, end);
            }
        }

        @Override
        public void obfuscateText(Reader input, Appendable destination) throws IOException {
            if (destination instanceof Writer) {
                obfuscateText(input, (Writer) destination);
                return;
            }

            copyAll(input, destination);
        }

        private void obfuscateText(Reader input, Writer destination) throws IOException {
            char[] buffer = new char[1024];

            int len;
            while ((len = input.read(buffer)) != -1) {
                destination.write(buffer, 0, len);
            }
        }

        @Override
        public Writer streamTo(Appendable destination) {
            return new ObfuscatingWriter() {

                @Override
                public void write(int c) throws IOException {
                    checkClosed();
                    ObfuscatorUtils.append(c, destination);
                }

                @Override
                public void write(char[] cbuf) throws IOException {
                    checkClosed();
                    ObfuscatorUtils.append(cbuf, destination);
                }

                @Override
                public void write(char[] cbuf, int off, int len) throws IOException {
                    checkClosed();
                    ObfuscatorUtils.append(cbuf, off, off + len, destination);
                }

                @Override
                public void write(String str) throws IOException {
                    checkClosed();
                    ObfuscatorUtils.append(str, destination);
                }

                @Override
                public void write(String str, int off, int len) throws IOException {
                    checkClosed();
                    ObfuscatorUtils.append(str, off, off + len, destination);
                }

                @Override
                public Writer append(CharSequence csq) throws IOException {
                    checkClosed();
                    destination.append(csq);
                    return this;
                }

                @Override
                public Writer append(CharSequence csq, int start, int end) throws IOException {
                    checkClosed();
                    destination.append(csq, start, end);
                    return this;
                }

                @Override
                public Writer append(char c) throws IOException {
                    checkClosed();
                    destination.append(c);
                    return this;
                }

                @Override
                public void flush() throws IOException {
                    super.flush();
                    if (destination instanceof Flushable) {
                        ((Flushable) destination).flush();
                    }
                }
            };
        }

        @Override
        public boolean equals(Object o) {
            return this == o;
        }

        @Override
        public int hashCode() {
            return getClass().hashCode();
        }

        @Override
        @SuppressWarnings("nls")
        public String toString() {
            return Obfuscator.class.getName() + "#none()";
        }
    }

    /**
     * Returns an immutable obfuscator that replaces all characters with a fixed number of {@code *}.
     *
     * @param fixedLength The fixed length.
     * @return An obfuscator that replaces all characters with the given number of {@code *}.
     * @throws IllegalArgumentException If the given fixed length is negative.
     */
    public static Obfuscator fixedLength(int fixedLength) {
        return fixedLength(fixedLength, DEFAULT_MASK_CHAR);
    }

    /**
     * Returns an immutable obfuscator that replaces all characters with a fixed number of a specific character.
     *
     * @param fixedLength The fixed length.
     * @param maskChar The character to replace with.
     * @return An obfuscator that replaces all characters with the given number of the given character.
     * @throws IllegalArgumentException If the given fixed length is negative.
     */
    public static Obfuscator fixedLength(int fixedLength, char maskChar) {
        return new FixedLengthObfuscator(fixedLength, maskChar);
    }

    private static final class FixedLengthObfuscator extends Obfuscator {

        private final int fixedLength;
        private final char maskChar;

        private final CharSequence fixedMask;

        private FixedLengthObfuscator(int fixedLength, char maskChar) {
            if (fixedLength < 0) {
                throw new IllegalArgumentException(fixedLength + " < 0"); //$NON-NLS-1$
            }
            this.fixedLength = fixedLength;
            this.maskChar = maskChar;
            fixedMask = repeatChar(maskChar, fixedLength);
        }

        @Override
        public CharSequence obfuscateText(CharSequence s) {
            Objects.requireNonNull(s);
            return fixedMask;
        }

        @Override
        public CharSequence obfuscateText(CharSequence s, int start, int end) {
            checkStartAndEnd(s, start, end);
            return fixedMask;
        }

        @Override
        public void obfuscateText(CharSequence s, StringBuilder destination) {
            Objects.requireNonNull(s);
            destination.append(fixedMask);
        }

        @Override
        public void obfuscateText(CharSequence s, int start, int end, StringBuilder destination) {
            checkStartAndEnd(s, start, end);
            destination.append(fixedMask);
        }

        @Override
        public void obfuscateText(CharSequence s, StringBuffer destination) {
            Objects.requireNonNull(s);
            destination.append(fixedMask);
        }

        @Override
        public void obfuscateText(CharSequence s, int start, int end, StringBuffer destination) {
            checkStartAndEnd(s, start, end);
            destination.append(fixedMask);
        }

        @Override
        public void obfuscateText(CharSequence s, Appendable destination) throws IOException {
            Objects.requireNonNull(s);
            destination.append(fixedMask);
        }

        @Override
        public void obfuscateText(CharSequence s, int start, int end, Appendable destination) throws IOException {
            checkStartAndEnd(s, start, end);
            destination.append(fixedMask);
        }

        @Override
        public CharSequence obfuscateText(Reader input) throws IOException {
            discardAll(input);
            return fixedMask;
        }

        @Override
        public void obfuscateText(Reader input, Appendable destination) throws IOException {
            discardAll(input);
            destination.append(fixedMask);
        }

        @Override
        public Writer streamTo(Appendable destination) {
            return new ObfuscatingWriter() {

                @Override
                public void write(int c) throws IOException {
                    checkClosed();
                    // discard
                }

                @Override
                public void write(char[] cbuf) throws IOException {
                    checkClosed();
                    // discard
                }

                @Override
                public void write(char[] cbuf, int off, int len) throws IOException {
                    checkClosed();
                    checkOffsetAndLength(cbuf, off, len);
                    // discard
                }

                @Override
                public void write(String str) throws IOException {
                    checkClosed();
                    // discard
                }

                @Override
                public void write(String str, int off, int len) throws IOException {
                    checkClosed();
                    checkOffsetAndLength(str, off, len);
                    // discard
                }

                @Override
                public Writer append(CharSequence csq) throws IOException {
                    checkClosed();
                    // discard
                    return this;
                }

                @Override
                public Writer append(CharSequence csq, int start, int end) throws IOException {
                    checkClosed();
                    CharSequence cs = csq == null ? "null" : csq; //$NON-NLS-1$
                    checkStartAndEnd(cs, start, end);
                    // discard
                    return this;
                }

                @Override
                public Writer append(char c) throws IOException {
                    checkClosed();
                    // discard
                    return this;
                }

                @Override
                protected void onClose() throws IOException {
                    destination.append(fixedMask);
                }
            };
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || o.getClass() != getClass()) {
                return false;
            }
            FixedLengthObfuscator other = (FixedLengthObfuscator) o;
            return fixedLength == other.fixedLength && maskChar == other.maskChar;
        }

        @Override
        public int hashCode() {
            return fixedLength ^ maskChar;
        }

        @Override
        @SuppressWarnings("nls")
        public String toString() {
            return Obfuscator.class.getName() + "#fixedLength(" + fixedLength + ", " + maskChar + ")";
        }
    }

    /**
     * Returns an immutable obfuscator that replaces all characters with a fixed value.
     *
     * @param fixedValue The fixed value.
     * @return An obfuscator that replaces all characters with the given fixed value.
     * @throws NullPointerException If the given fixed value is {@code null}.
     */
    public static Obfuscator fixedValue(String fixedValue) {
        return new FixedValueObfuscator(fixedValue);
    }

    private static final class FixedValueObfuscator extends Obfuscator {

        private final String fixedValue;

        private FixedValueObfuscator(String fixedValue) {
            this.fixedValue = Objects.requireNonNull(fixedValue);
        }

        @Override
        public CharSequence obfuscateText(CharSequence s) {
            Objects.requireNonNull(s);
            return fixedValue;
        }

        @Override
        public CharSequence obfuscateText(CharSequence s, int start, int end) {
            checkStartAndEnd(s, start, end);
            return fixedValue;
        }

        @Override
        public void obfuscateText(CharSequence s, StringBuilder destination) {
            Objects.requireNonNull(s);
            destination.append(fixedValue);
        }

        @Override
        public void obfuscateText(CharSequence s, int start, int end, StringBuilder destination) {
            checkStartAndEnd(s, start, end);
            destination.append(fixedValue);
        }

        @Override
        public void obfuscateText(CharSequence s, StringBuffer destination) {
            Objects.requireNonNull(s);
            destination.append(fixedValue);
        }

        @Override
        public void obfuscateText(CharSequence s, int start, int end, StringBuffer destination) {
            checkStartAndEnd(s, start, end);
            destination.append(fixedValue);
        }

        @Override
        public void obfuscateText(CharSequence s, Appendable destination) throws IOException {
            Objects.requireNonNull(s);
            destination.append(fixedValue);
        }

        @Override
        public void obfuscateText(CharSequence s, int start, int end, Appendable destination) throws IOException {
            checkStartAndEnd(s, start, end);
            destination.append(fixedValue);
        }

        @Override
        public CharSequence obfuscateText(Reader input) throws IOException {
            discardAll(input);
            return fixedValue;
        }

        @Override
        public void obfuscateText(Reader input, Appendable destination) throws IOException {
            discardAll(input);
            destination.append(fixedValue);
        }

        @Override
        public Writer streamTo(Appendable destination) {
            return new ObfuscatingWriter() {

                @Override
                public void write(int c) throws IOException {
                    checkClosed();
                    // discard
                }

                @Override
                public void write(char[] cbuf) throws IOException {
                    checkClosed();
                    // discard
                }

                @Override
                public void write(char[] cbuf, int off, int len) throws IOException {
                    checkClosed();
                    checkOffsetAndLength(cbuf, off, len);
                    // discard
                }

                @Override
                public void write(String str) throws IOException {
                    checkClosed();
                    // discard
                }

                @Override
                public void write(String str, int off, int len) throws IOException {
                    checkClosed();
                    checkOffsetAndLength(str, off, len);
                    // discard
                }

                @Override
                public Writer append(CharSequence csq) throws IOException {
                    checkClosed();
                    // discard
                    return this;
                }

                @Override
                public Writer append(CharSequence csq, int start, int end) throws IOException {
                    checkClosed();
                    CharSequence cs = csq == null ? "null" : csq; //$NON-NLS-1$
                    checkStartAndEnd(cs, start, end);
                    // discard
                    return this;
                }

                @Override
                public Writer append(char c) throws IOException {
                    checkClosed();
                    // discard
                    return this;
                }

                @Override
                protected void onClose() throws IOException {
                    destination.append(fixedValue);
                }
            };
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || o.getClass() != getClass()) {
                return false;
            }
            FixedValueObfuscator other = (FixedValueObfuscator) o;
            return fixedValue.equals(other.fixedValue);
        }

        @Override
        public int hashCode() {
            return fixedValue.hashCode();
        }

        @Override
        @SuppressWarnings("nls")
        public String toString() {
            return Obfuscator.class.getName() + "#fixedValue(" + fixedValue + ")";
        }
    }

    /**
     * Returns a builder for obfuscators that obfuscate a specific portion of their input.
     *
     * @return A builder for obfuscators that obfuscate a specific portion of their input.
     */
    public static PortionBuilder portion() {
        return new PortionBuilder();
    }

    /**
     * A builder for obfuscators that obfuscate a specific portion of their input.
     * An obfuscator created with {@link #keepAtStart(int) keepAtStart(x)} and {@link #keepAtEnd(int) keepAtEnd(y)} will, for input {@code s},
     * obfuscate all characters in the range {@code x} (inclusive) to {@link CharSequence#length() s.length()}{@code - y} (exclusive).
     * If this range is empty, such an obfuscator will not obfuscate anything, unless if {@link #withFixedTotalLength(int)} or
     * {@link #withFixedLength(int)} is specified.
     *
     * @author Rob Spoor
     */
    public static final class PortionBuilder {

        private int keepAtStart;
        private int keepAtEnd;
        private int atLeastFromStart;
        private int atLeastFromEnd;
        private int fixedTotalLength;
        private int fixedObfuscatedLength;
        private char maskChar;

        private PortionBuilder() {
            withDefaults();
        }

        /**
         * Sets the number of characters at the start that created obfuscators will skip when obfuscating.
         *
         * @param count The number of characters to skip. The default is {@code 0}.
         * @return This builder.
         * @throws IllegalArgumentException If the count is negative.
         */
        public PortionBuilder keepAtStart(int count) {
            if (count < 0) {
                throw new IllegalArgumentException(count + " < 0"); //$NON-NLS-1$
            }
            keepAtStart = count;
            return this;
        }

        /**
         * Sets the number of characters at the end that created obfuscators will skip when obfuscating.
         *
         * @param count The number of characters to skip. The default is {@code 0}.
         * @return This builder.
         * @throws IllegalArgumentException If the count is negative.
         */
        public PortionBuilder keepAtEnd(int count) {
            if (count < 0) {
                throw new IllegalArgumentException(count + " < 0"); //$NON-NLS-1$
            }
            keepAtEnd = count;
            return this;
        }

        /**
         * Sets the minimum number of characters from the start that need to be obfuscated.
         * This will overrule any value set with {@link #keepAtStart(int)} or {@link #keepAtEnd(int)}.
         *
         * @param count The minimum number of characters to obfuscate. The default is {@code 0}.
         * @return This builder.
         * @throws IllegalArgumentException If the count is negative.
         */
        public PortionBuilder atLeastFromStart(int count) {
            if (count < 0) {
                throw new IllegalArgumentException(count + " < 0"); //$NON-NLS-1$
            }
            atLeastFromStart = count;
            return this;
        }

        /**
         * Sets the minimum number of characters from the end that need to be obfuscated.
         * This will overrule any value set with {@link #keepAtStart(int)} or {@link #keepAtEnd(int)}.
         *
         * @param count The minimum number of characters to obfuscate. The default is {@code 0}.
         * @return This builder.
         * @throws IllegalArgumentException If the count is negative.
         */
        public PortionBuilder atLeastFromEnd(int count) {
            if (count < 0) {
                throw new IllegalArgumentException(count + " < 0"); //$NON-NLS-1$
            }
            atLeastFromEnd = count;
            return this;
        }

        /**
         * Sets or removes the fixed total length to use for obfuscated contents.
         * When obfuscating, the result will have {@link #withMaskChar(char) mask characters} added until this total length has been reached.
         * <p>
         * Note: when used in combination with {@link #keepAtStart(int)} and/or {@link #keepAtEnd(int)}, this total length must be at least the sum
         * of both other values. When used in combination with both, parts of the input may be repeated in the obfuscated content if the input's
         * length is less than the combined number of characters to keep.
         *
         * @param fixedTotalLength The fixed total length for obfuscated contents, or a negative value to use the actual length of the input.
         *                             The default is {@code -1}.
         * @return This builder.
         * @since 1.2
         */
        public PortionBuilder withFixedTotalLength(int fixedTotalLength) {
            this.fixedTotalLength = Math.max(-1, fixedTotalLength);
            return this;
        }

        /**
         * Sets or removes the fixed number of {@link #withMaskChar(char) mask characters} to use for obfuscating.
         * <p>
         * This setting will be ignored if the {@link #withFixedTotalLength(int) fixed total length} is set.
         *
         * @param fixedObfuscatedLength The fixed number of mask characters, or a negative value to use the actual length of the input.
         *                        The default is {@code -1}.
         * @return This builder.
         * @deprecated The total length of obfuscated contents can very when using this setting, making it possible in certain cases to find the
         *             original value that was obfuscated. Use {@link #withFixedTotalLength(int)} instead.
         */
        @Deprecated
        public PortionBuilder withFixedLength(int fixedObfuscatedLength) {
            this.fixedObfuscatedLength = Math.max(-1, fixedObfuscatedLength);
            return this;
        }

        /**
         * Sets the char that created obfuscators use for obfuscating.
         *
         * @param maskChar The mask character. The default is {@code *}.
         * @return This builder.
         */
        public PortionBuilder withMaskChar(char maskChar) {
            this.maskChar = maskChar;
            return this;
        }

        /**
         * Specifies that the default settings should be restored.
         * Calling this method is similar to calling the following:
         * <ul>
         * <li>{@link #keepAtStart(int) keepAtStart(0)}</li>
         * <li>{@link #keepAtEnd(int) keepAtEnd(0)}</li>
         * <li>{@link #atLeastFromStart(int) atLeastFromStart(0)}</li>
         * <li>{@link #atLeastFromEnd(int) atLeastFromEnd(0)}</li>
         * <li>{@link #withFixedTotalLength(int) withFixedTotalLength(-1)}</li>
         * <li>{@link #withFixedLength(int) withFixedLength(-1)}</li>
         * <li>{@link #withMaskChar(char) withMaskChar('*')}</li>
         * </ul>
         *
         * @return This builder.
         */
        public PortionBuilder withDefaults() {
            keepAtStart(0);
            keepAtEnd(0);
            atLeastFromStart(0);
            atLeastFromEnd(0);
            withFixedTotalLength(-1);
            withFixedLength(-1);
            withMaskChar(DEFAULT_MASK_CHAR);
            return this;
        }

        /**
         * This method allows the application of a function to this builder.
         * <p>
         * Any exception thrown by the function will be propagated to the caller.
         *
         * @param <R> The type of the result of the function.
         * @param f The function to apply.
         * @return The result of applying the function to this builder.
         */
        public <R> R transform(Function<? super PortionBuilder, ? extends R> f) {
            return f.apply(this);
        }

        /**
         * Creates an immutable obfuscator with the current settings of this builder.
         *
         * @return An obfuscator with the current settings of this builder object.
         * @throws IllegalStateException If this builder is in an invalid state, for example if {@link #withFixedTotalLength(int)} is smaller than
         *                                   {@link #keepAtStart(int)} and {@link #keepAtEnd(int)} combined.
         */
        public Obfuscator build() {
            return new PortionObfuscator(this);
        }
    }

    private static final class PortionObfuscator extends Obfuscator {

        private final int keepAtStart;
        private final int keepAtEnd;
        private final int atLeastFromStart;
        private final int atLeastFromEnd;
        private final int fixedTotalLength;
        private final int fixedObfuscatedLength;
        private final char maskChar;

        private PortionObfuscator(PortionBuilder builder) {
            this.keepAtStart = builder.keepAtStart;
            this.keepAtEnd = builder.keepAtEnd;
            this.atLeastFromStart = builder.atLeastFromStart;
            this.atLeastFromEnd = builder.atLeastFromEnd;
            this.fixedTotalLength = builder.fixedTotalLength;
            this.fixedObfuscatedLength = builder.fixedObfuscatedLength;
            this.maskChar = builder.maskChar;

            if (fixedTotalLength >= 0 && fixedTotalLength < keepAtStart + keepAtEnd) {
                throw new IllegalStateException(
                        Messages.portion.fixedTotalLengthSmallerThanKeepAtStartPlusKeepAtEnd.get(fixedTotalLength, keepAtStart, keepAtEnd));
            }
        }

        private int fromStart(int length) {
            if (atLeastFromStart > 0) {
                // the first characters need to be obfuscated so ignore keepAtStart
                return 0;
            }
            // 0 <= keepAtMost <= length, the maximum number of characters to not obfuscate taking into account atLeastFromEnd
            // 0 <= result <= length, the minimum of what we want to obfuscate and what we can obfuscate
            int keepAtMost = Math.max(0, length - atLeastFromEnd);
            return Math.min(keepAtStart, keepAtMost);
        }

        private int fromEnd(int length, int keepFromStart, boolean allowDuplicates) {
            if (atLeastFromEnd > 0) {
                // the last characters need to be obfuscated so ignore keepAtEnd
                return 0;
            }
            // 0 <= available <= length, the number of characters not already handled by fromStart (to prevent characters being appended twice)
            //                           if allowDuplicates then available == length
            // 0 <= keepAtMost <= length, the maximum number of characters to not obfuscate taking into account atLeastFromStart
            // 0 <= result <= length, the minimum of what we want to obfuscate and what we can obfuscate
            int available = allowDuplicates ? length : length - keepFromStart;
            int keepAtMost = Math.max(0, length - atLeastFromStart);
            return Math.min(keepAtEnd, Math.min(available, keepAtMost));
        }

        @Override
        public CharSequence obfuscateText(CharSequence s, int start, int end) {
            checkStartAndEnd(s, start, end);

            boolean allowDuplicates = fixedTotalLength >= 0;

            int length = end - start;
            int fromStart = fromStart(length);
            int fromEnd = fromEnd(length, fromStart, allowDuplicates);
            // 0 <= fromStart <= length == end - start, so start <= start + fromStart <= end
            // 0 <= fromEnd <= length == end - start, so 0 <= length - fromEnd and start <= end - fromEnd

            if (fixedTotalLength >= 0) {
                length = fixedTotalLength;
            } else if (fixedObfuscatedLength >= 0) {
                // length - fromStart - fromEnd needs to be fixedObfuscatedLength, so length needs to be fixedObfuscatedLength + fromStart + fromEnd
                length = fixedObfuscatedLength + fromStart + fromEnd;
            }

            char[] array = new char[length];

            // first build the content as expected: 0 to fromStart non-obfuscated, then obfuscated, then from end - fromEnd non-obfuscated
            getChars(s, start, start + fromStart, array, 0);
            for (int i = fromStart; i < length - fromEnd; i++) {
                array[i] = maskChar;
            }
            getChars(s, end - fromEnd, end, array, length - fromEnd);
            return wrapArray(array);
        }

        @Override
        public void obfuscateText(CharSequence s, int start, int end, Appendable destination) throws IOException {
            checkStartAndEnd(s, start, end);

            boolean allowDuplicates = fixedTotalLength >= 0;

            int length = end - start;
            int fromStart = fromStart(length);
            int fromEnd = fromEnd(length, fromStart, allowDuplicates);
            // 0 <= fromStart <= length == end - start, so start <= start + fromStart <= end
            // 0 <= fromEnd <= length == end - start, so 0 <= length - fromEnd and start <= end - fromEnd

            if (fixedTotalLength >= 0) {
                length = fixedTotalLength;
            } else if (fixedObfuscatedLength >= 0) {
                // length - fromStart - fromEnd needs to be fixedObfuscatedLength, so length needs to be fixedObfuscatedLength + fromStart + fromEnd
                length = fixedObfuscatedLength + fromStart + fromEnd;
            }

            // first build the content as expected: 0 to fromStart non-obfuscated, then obfuscated, then end - fromEnd non-obfuscated
            if (fromStart > 0) {
                destination.append(s, start, start + fromStart);
            }
            ObfuscatorUtils.append(maskChar, length - fromEnd - fromStart, destination);
            if (fromEnd > 0) {
                destination.append(s, end - fromEnd, end);
            }
        }

        @Override
        public CharSequence obfuscateText(Reader input) throws IOException {
            CharSequence s = readAll(input);
            return obfuscateText(s);
        }

        @Override
        public void obfuscateText(Reader input, Appendable destination) throws IOException {
            CharSequence s = readAll(input);
            obfuscateText(s, destination);
        }

        @Override
        public Writer streamTo(Appendable destination) {
            return new CachingObfuscatingWriter(this, destination);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || o.getClass() != getClass()) {
                return false;
            }
            PortionObfuscator other = (PortionObfuscator) o;
            return keepAtStart == other.keepAtStart && keepAtEnd == other.keepAtEnd
                    && atLeastFromStart == other.atLeastFromStart && atLeastFromEnd == other.atLeastFromEnd
                    && fixedObfuscatedLength == other.fixedObfuscatedLength
                    && fixedTotalLength == other.fixedTotalLength
                    && maskChar == other.maskChar;
        }

        @Override
        public int hashCode() {
            return keepAtStart ^ keepAtEnd ^ atLeastFromStart ^ atLeastFromEnd ^ fixedObfuscatedLength ^ fixedTotalLength ^ maskChar;
        }

        @Override
        @SuppressWarnings("nls")
        public String toString() {
            StringBuilder sb = new StringBuilder()
                    .append(Obfuscator.class.getName())
                    .append("#portion[");
            if (keepAtStart > 0) {
                sb.append("keepAtStart=").append(keepAtStart).append(',');
            }
            if (keepAtEnd > 0) {
                sb.append("keepAtEnd=").append(keepAtEnd).append(',');
            }
            if (atLeastFromStart > 0) {
                sb.append("atLeastFromStart=").append(atLeastFromStart).append(',');
            }
            if (atLeastFromEnd > 0) {
                sb.append("atLeastFromEnd=").append(atLeastFromEnd).append(',');
            }
            if (fixedObfuscatedLength >= 0) {
                sb.append("fixedLength=").append(fixedObfuscatedLength).append(',');
            }
            if (fixedTotalLength >= 0) {
                sb.append("fixedTotalLength=").append(fixedTotalLength).append(',');
            }
            return sb.append("maskChar=").append(maskChar).append(']')
                    .toString();
        }
    }

    /**
     * Returns an obfuscator that uses a function to obfuscate text. This method allows you to create an obfuscator using a predefined function.
     * The returned obfuscator is immutable if the given function is.
     * <p>
     * Note: the function should never return {@code null}. The returned obfuscator will throw a {@link NullPointerException} if the function returns
     * {@code null} when obfuscating text.
     *
     * @param function The function to use.
     * @return An obfuscator that uses the given function to obfuscate text.
     * @throws NullPointerException If the given function is {@code null}.
     */
    public static Obfuscator fromFunction(Function<? super CharSequence, ? extends CharSequence> function) {
        return new FromFunctionObfuscator(function);
    }

    private static final class FromFunctionObfuscator extends Obfuscator {

        private final Function<? super CharSequence, ? extends CharSequence> function;

        private FromFunctionObfuscator(Function<? super CharSequence, ? extends CharSequence> function) {
            this.function = Objects.requireNonNull(function);
        }

        @Override
        public CharSequence obfuscateText(CharSequence s) {
            Objects.requireNonNull(s);
            return applyFunction(s);
        }

        @Override
        public CharSequence obfuscateText(CharSequence s, int start, int end) {
            checkStartAndEnd(s, start, end);
            return applyFunction(s, start, end);
        }

        @Override
        public void obfuscateText(CharSequence s, StringBuilder destination) {
            Objects.requireNonNull(s);
            destination.append(applyFunction(s));
        }

        @Override
        public void obfuscateText(CharSequence s, int start, int end, StringBuilder destination) {
            checkStartAndEnd(s, start, end);
            destination.append(applyFunction(s, start, end));
        }

        @Override
        public void obfuscateText(CharSequence s, StringBuffer destination) {
            Objects.requireNonNull(s);
            destination.append(applyFunction(s));
        }

        @Override
        public void obfuscateText(CharSequence s, int start, int end, StringBuffer destination) {
            checkStartAndEnd(s, start, end);
            destination.append(applyFunction(s, start, end));
        }

        @Override
        public void obfuscateText(CharSequence s, Appendable destination) throws IOException {
            Objects.requireNonNull(s);
            destination.append(applyFunction(s));
        }

        @Override
        public void obfuscateText(CharSequence s, int start, int end, Appendable destination) throws IOException {
            checkStartAndEnd(s, start, end);
            destination.append(applyFunction(s, start, end));
        }

        @Override
        public CharSequence obfuscateText(Reader input) throws IOException {
            CharSequence s = readAll(input);
            return applyFunction(s);
        }

        @Override
        public void obfuscateText(Reader input, Appendable destination) throws IOException {
            CharSequence s = readAll(input);
            destination.append(applyFunction(s));
        }

        private CharSequence applyFunction(CharSequence s) {
            CharSequence result = function.apply(s);
            return Objects.requireNonNull(result, () -> Messages.fromFunction.functionReturnedNull.get(s));
        }

        private CharSequence applyFunction(CharSequence s, int start, int end) {
            checkStartAndEnd(s, start, end);
            return applyFunction(s.subSequence(start, end));
        }

        @Override
        public Writer streamTo(Appendable destination) {
            return new CachingObfuscatingWriter(this, destination);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || o.getClass() != getClass()) {
                return false;
            }
            FromFunctionObfuscator other = (FromFunctionObfuscator) o;
            return function.equals(other.function);
        }

        @Override
        public int hashCode() {
            return function.hashCode();
        }

        @Override
        @SuppressWarnings("nls")
        public String toString() {
            return Obfuscator.class.getName() + "#fromFunction(" + function + ")";
        }
    }
}
