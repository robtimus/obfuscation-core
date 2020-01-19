/*
 * ObfuscatorUtils.java
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

import static com.github.robtimus.obfuscation.CaseSensitivity.CASE_SENSITIVE;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Utility methods that can be used for implementing {@link Obfuscator Obfuscators}.
 *
 * @author Rob Spoor
 */
public final class ObfuscatorUtils {

    private ObfuscatorUtils() {
        throw new Error("cannot create instances of " + getClass().getName()); //$NON-NLS-1$
    }

    // CharSequence indexes

    /**
     * Returns the index within a {@code CharSequence} of the first occurrence of a specific character.
     * This method is like {@link String#indexOf(int, int)} but it works on any {@code CharSequence}, and it has an upper bound as well.
     *
     * @param s The {@code CharSequence} to search.
     * @param ch The character to search for.
     * @param fromIndex The index to start the search from.
     * @param toIndex The index to end the search at.
     * @return The index of the given character in the given {@code CharSequence}, or {@code -1} if the character does not occur in the given
     *         {@code CharSequence} in the specified range.
     * @throws NullPointerException If the given {@code CharSequence} is {@code null}.
     */
    public static int indexOf(CharSequence s, int ch, int fromIndex, int toIndex) {
        if (s instanceof String) {
            int index = ((String) s).indexOf(ch, fromIndex);
            return index == -1 || index >= toIndex ? -1 : index;
        }

        for (int i = Math.max(0, fromIndex), max = Math.min(toIndex, s.length()); i < max; i++) {
            if (s.charAt(i) == ch) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Skips leading {@link Character#isWhitespace(int) whitespace} in a range of a {@code CharSequence}.
     *
     * @param s The {@code CharSequence} to skip leading whitespace in.
     * @param fromIndex The start of the range to start skipping whitespace in, inclusive.
     * @param toIndex The end of the range to start skipping whitespace in, exclusive.
     * @return The index in the given {@code CharSequence} of the first non-whitespace character, or {@code toIndex} if the {@code CharSequence}
     *         contains only whitespace.
     * @throws NullPointerException If the given {@code CharSequence} is {@code null}.
     * @throws IndexOutOfBoundsException If the given indexes are invalid for the given {@code CharSequence}.
     */
    public static int skipLeadingWhitespace(CharSequence s, int fromIndex, int toIndex) {
        for (int i = fromIndex; i < toIndex; i++) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return i;
            }
        }
        return toIndex;
    }

    /**
     * Skips trailing {@link Character#isWhitespace(int) whitespace} in a range of a {@code CharSequence}.
     *
     * @param s The {@code CharSequence} to skip trailing whitespace in.
     * @param fromIndex The start of the range to start skipping whitespace in, inclusive.
     * @param toIndex The end of the range to start skipping whitespace in, exclusive.
     * @return The index in the given {@code CharSequence} of the first non-whitespace character, or {@code fromIndex} if the {@code CharSequence}
     *         contains only whitespace.
     * @throws IndexOutOfBoundsException If the given indexes are invalid for the given {@code CharSequence}.
     */
    public static int skipTrailingWhitespace(CharSequence s, int fromIndex, int toIndex) {
        for (int i = toIndex; i > fromIndex; i--) {
            if (!Character.isWhitespace(s.charAt(i - 1))) {
                return i;
            }
        }
        return fromIndex;
    }

    /**
     * Copies characters from a {@code CharSequence} into a destination character array.
     * This method is a more generic version of {@link String#getChars(int, int, char[], int)}, {@link StringBuilder#getChars(int, int, char[], int)}
     * and {@link StringBuffer#getChars(int, int, char[], int)}.
     *
     * @param src The {@code CharSequence} to copy characters from.
     * @param srcBegin The index of the first character in the {@code CharSequence} to copy.
     * @param srcEnd The index after the last character in the {@code CharSequence} to copy.
     * @param dst The destination array.
     * @param dstBegin The start offset in the destination array.
     * @throws NullPointerException If the given {@code CharSequence} or character array is {@code null}.
     * @throws IndexOutOfBoundsException If any of the indexes is invalid.
     */
    public static void getChars(CharSequence src, int srcBegin, int srcEnd, char[] dst, int dstBegin) {
        if (src instanceof String) {
            ((String) src).getChars(srcBegin, srcEnd, dst, dstBegin);
        } else if (src instanceof StringBuilder) {
            ((StringBuilder) src).getChars(srcBegin, srcEnd, dst, dstBegin);
        } else if (src instanceof StringBuffer) {
            ((StringBuffer) src).getChars(srcBegin, srcEnd, dst, dstBegin);
        } else {
            checkStartAndEnd(src, srcBegin, srcEnd);
            checkStartAndEnd(dst, dstBegin, dstBegin + srcEnd - srcBegin);
            for (int i = srcBegin, j = dstBegin; i < srcEnd; i++, j++) {
                dst[j] = src.charAt(i);
            }
        }
    }

    // index checking

    /**
     * Checks whether or not an index is valid for a character array.
     *
     * @param array The array to check for.
     * @param index The index to check.
     * @throws NullPointerException If the given array is {@code null}.
     * @throws IndexOutOfBoundsException If the given index is negative or exceeds the given array's length.
     */
    public static void checkIndex(char[] array, int index) {
        if (index < 0 || index >= array.length) {
            throw new IndexOutOfBoundsException(Messages.charSequence.invalidIndex.get(array.length, index));
        }
    }

    /**
     * Checks whether or not an offset and length are valid for a character array.
     *
     * @param array The array to check for.
     * @param offset The offset to check, inclusive.
     * @param length The length to check.
     * @throws NullPointerException If the given array is {@code null}.
     * @throws IndexOutOfBoundsException If the given offset is negative, the given length is negative,
     *                                       or the given offset and length exceed the given array's length.
     */
    public static void checkOffsetAndLength(char[] array, int offset, int length) {
        if (offset < 0 || length < 0 || offset + length > array.length) {
            throw new ArrayIndexOutOfBoundsException(Messages.array.invalidOffsetOrLength.get(array.length, offset, length));
        }
    }

    /**
     * Checks whether or not a start and end index are valid for a character array.
     *
     * @param array The array to check for.
     * @param start The start index to check, inclusive.
     * @param end The end index to check, exclusive.
     * @throws NullPointerException If the given array is {@code null}.
     * @throws IndexOutOfBoundsException If the given start index is negative,
     *                                       the given end index is larger than the given array's length,
     *                                       or the given start index is larger than the given end index.
     */
    public static void checkStartAndEnd(char[] array, int start, int end) {
        if (start < 0 || end > array.length || start > end) {
            throw new ArrayIndexOutOfBoundsException(Messages.array.invalidStartOrEnd.get(array.length, start, end));
        }
    }

    /**
     * Checks whether or not an index is valid for a {@code CharSequence}.
     *
     * @param s The {@code CharSequence} to check for.
     * @param index The index to check.
     * @throws NullPointerException If the given {@code CharSequence} is {@code null}.
     * @throws IndexOutOfBoundsException If the given index is negative or exceeds the given {@code CharSequence}'s length.
     */
    public static void checkIndex(CharSequence s, int index) {
        if (index < 0 || index >= s.length()) {
            throw new IndexOutOfBoundsException(Messages.charSequence.invalidIndex.get(s.length(), index));
        }
    }

    /**
     * Checks whether or not an offset and length are valid for a {@code CharSequence}.
     *
     * @param sequence The {@code CharSequence} to check for.
     * @param offset The offset to check, inclusive.
     * @param length The length to check.
     * @throws NullPointerException If the given {@code CharSequence} is {@code null}.
     * @throws IndexOutOfBoundsException If the given offset is negative, the given length is negative,
     *                                       or the given offset and length exceed the given {@code CharSequence}'s length.
     */
    public static void checkOffsetAndLength(CharSequence sequence, int offset, int length) {
        if (offset < 0 || length < 0 || offset + length > sequence.length()) {
            throw new ArrayIndexOutOfBoundsException(Messages.array.invalidOffsetOrLength.get(sequence.length(), offset, length));
        }
    }

    /**
     * Checks whether or not a start and end index are valid for a {@code CharSequence}.
     *
     * @param sequence The {@code CharSequence} to check for.
     * @param start The start index to check, inclusive.
     * @param end The end index to check, exclusive.
     * @throws NullPointerException If the given {@code CharSequence} is {@code null}.
     * @throws IndexOutOfBoundsException If the given start index is negative,
     *                                       the given end index is larger than the given {@code CharSequence}'s length,
     *                                       or the given start index is larger than the given end index.
     */
    public static void checkStartAndEnd(CharSequence sequence, int start, int end) {
        if (start < 0 || end > sequence.length() || start > end) {
            throw new ArrayIndexOutOfBoundsException(Messages.array.invalidStartOrEnd.get(sequence.length(), start, end));
        }
    }

    // CharSequence / String generation

    /**
     * Creates a {@code CharSequence} that wraps a character array.
     *
     * @param array The array to wrap.
     * @return a {@code CharSequence} wrapper around the given array.
     * @throws NullPointerException If the given array is {@code null}.
     */
    public static CharSequence wrapArray(char[] array) {
        Objects.requireNonNull(array);
        return new CharArraySequence(array);
    }

    /**
     * Creates an immutable {@code CharSequence} that repeats a single character.
     *
     * @param c The character to repeat.
     * @param count The number of times to repeat the character.
     * @return A {@code CharSequence} that repeats the given character the given number of times.
     * @throws IllegalArgumentException If the given number of times is negative.
     */
    public static CharSequence repeatChar(char c, int count) {
        if (count < 0) {
            throw new IllegalArgumentException(count + " < 0"); //$NON-NLS-1$
        }
        return RepeatingCharSequence.valueOf(c, count);
    }

    // Reader / Writer

    /**
     * Returns a {@code Reader} for a {@code CharSequence}.
     *
     * @param s The {@code CharSequence} to return a {@code Reader} for.
     * @return A {@code Reader} for the given {@code CharSequence}.
     */
    public static Reader reader(CharSequence s) {
        return new CharSequenceReader(s, 0, s.length());
    }

    /**
     * Returns a {@code Reader} for a portion of a {@code CharSequence}.
     *
     * @param s The {@code CharSequence} to return a {@code Reader} for.
     * @param start The start index of the portion, inclusive.
     * @param end The end index of the portion, inclusive.
     * @return A {@code Reader} for the given portion of the given {@code CharSequence}.
     * @throws IndexOutOfBoundsException If the given start index is negative,
     *                                       the given end index is larger than the given {@code CharSequence}'s length,
     *                                       or the given start index is larger than the given end index.
     */
    public static Reader reader(CharSequence s, int start, int end) {
        checkStartAndEnd(s, start, end);
        return new CharSequenceReader(s, start, end);
    }

    /**
     * Returns an {@code Appendable} as a {@code Writer}. If the given {@code Appendable} is a {@code Writer}, it is returned unmodified.
     * Otherwise, a wrapper is returned that will delegate all calls to the wrapped {@code Appendable}. This includes {@link Writer#flush() flush()}
     * if the wrapped {@code Appendable} implements {@link Flushable}, and {@link Writer#close() close()} if the wrapped {@code Appendable} implements
     * {@link Closeable} or {@link AutoCloseable}.
     * <p>
     * Note that the behaviour of closing a {@code Writer} wrapper depends on the wrapped {@code Appendable}. If it does not support closing,
     * or if it still allows text to be appended after closing, then the closed {@code AppendableWriter} allows text to be appended after closing.
     * If it does not allow text to be appended after closing, then neither will the closed {@code Writer} wrapper.
     *
     * @param appendable The {@code Appendable} to return a {@code Writer} for.
     * @return The given {@code Appendable} itself if it's already a {@code Writer}, otherwise a wrapper around the given {@code Appendable}.
     * @throws NullPointerException If the given {@code Appendable} is {@code null}.
     */
    public static Writer writer(Appendable appendable) {
        Objects.requireNonNull(appendable);
        return appendable instanceof Writer ? (Writer) appendable : new AppendableWriter(appendable);
    }

    /**
     * Returns a {@code Reader} that transparently appends all text read from another {@code Reader} to an {@code Appendable}.
     * If the returned {@code Reader} is closed, the given {@code Reader} will be closed as well. The {@code Appendable} will not be closed though.
     *
     * @param input The {@code Reader} to read from.
     * @param appendable The {@code Appendable} to write to.
     * @return A {@code Reader} that transparently appends all text read from the given {@code Reader} to the given {@code Appendable}.
     * @throws NullPointerException If the given {@code Reader} or {@code Appendable} is {@code null}.
     */
    public static Reader copyTo(Reader input, Appendable appendable) {
        Objects.requireNonNull(input);
        Objects.requireNonNull(appendable);
        return new CopyingReader(input, appendable);
    }

    // I/O

    /**
     * Reads the contents of a {@code Reader}.
     *
     * @param input The {@code Reader} to read the contents of.
     * @return A {@code CharSequence} with the contents of the given {@code Reader}.
     * @throws NullPointerException If the given {@code Reader} is {@code null}.
     * @throws IOException If an I/O error occurs.
     */
    public static CharSequence readAll(Reader input) throws IOException {
        Objects.requireNonNull(input);
        StringBuilder sb = new StringBuilder();
        char[] buffer = new char[1024];
        int len;
        while ((len = input.read(buffer)) != -1) {
            sb.append(buffer, 0, len);
        }
        return sb;
    }

    /**
     * Discards the contents of a {@code Reader}.
     *
     * @param input The {@code Reader} to discard the contents of.
     * @return The number of discarded characters.
     * @throws NullPointerException If the given {@code Reader} is {@code null}.
     * @throws IOException If an I/O error occurs.
     */
    public static long discardAll(Reader input) throws IOException {
        Objects.requireNonNull(input);
        char[] buffer = new char[1024];
        long count = 0;
        int len;
        while ((len = input.read(buffer)) != -1) {
            // discard
            count += len;
        }
        return count;
    }

    /**
     * Copies the contents of a {@code Reader} to an {@code Appendable}.
     *
     * @param input The {@code Reader} to copy the contents of.
     * @param destination The {@code Appendable} to copy the contents to.
     * @throws NullPointerException If the given {@code Reader} or {@code Appendable} is {@code null}.
     * @throws IOException If an I/O error occurs.
     */
    public static void copyAll(Reader input, Appendable destination) throws IOException {
        Objects.requireNonNull(input);
        Objects.requireNonNull(destination);
        @SuppressWarnings("resource")
        Writer writer = writer(destination);
        char[] buffer = new char[1024];
        int len;
        while ((len = input.read(buffer)) != -1) {
            writer.write(buffer, 0, len);
        }
    }

    /**
     * Appends a single character to an {@code Appendable}.
     * The character to be written is contained in the 16 low-order bits of the given integer value; the 16 high-order bits are ignored.
     *
     * @param c The character to append.
     * @param destination The {@code Appendable} to append to.
     * @throws NullPointerException If the given {@code Appendable} is {@code null}.
     * @throws IOException If an I/O error occurs.
     */
    public static void append(int c, Appendable destination) throws IOException {
        destination.append((char) c);
    }

    /**
     * Appends a single character a number of times to an {@code Appendable}.
     *
     * @param c The character to append.
     * @param count The number of times to append the character.
     * @param destination The {@code Appendable} to append to.
     * @throws IllegalArgumentException If the given number of times is negative.
     * @throws NullPointerException If the given {@code Appendable} is {@code null}.
     * @throws IOException If an I/O error occurs.
     */
    public static void append(char c, int count, Appendable destination) throws IOException {
        if (count < 0) {
            throw new IllegalArgumentException(count + " < 0"); //$NON-NLS-1$
        }
        Objects.requireNonNull(destination);
        if (count > 0) {
            if (destination instanceof Writer) {
                Writer writer = (Writer) destination;
                char[] array = new char[Math.min(count, 1024)];
                Arrays.fill(array, c);
                int remaining = count;
                while (remaining > 0) {
                    int n = Math.min(remaining, array.length);
                    writer.write(array, 0, n);
                    remaining -= n;
                }
            } else {
                destination.append(repeatChar(c, count));
            }
        }
    }

    /**
     * Appends an array of characters to an {@code Appendable}.
     *
     * @param array The array to append.
     * @param destination The {@code Appendable} to append to.
     * @throws NullPointerException If the given array or {@code Appendable} is {@code null}.
     * @throws IOException If an I/O error occurs.
     */
    public static void append(char[] array, Appendable destination) throws IOException {
        if (destination instanceof Writer) {
            ((Writer) destination).write(array);
        } else if (destination instanceof StringBuilder) {
            ((StringBuilder) destination).append(array);
        } else if (destination instanceof StringBuffer) {
            ((StringBuffer) destination).append(array);
        } else {
            destination.append(wrapArray(array));
        }
    }

    /**
     * Appends a portion of an array of characters to an {@code Appendable}.
     *
     * @param array The array to append a portion of.
     * @param destination The {@code Appendable} to append to.
     * @param start The start index of the portion to append, inclusive.
     * @param end The end index of the portion to append, exclusive.
     * @throws NullPointerException If the given array or {@code Appendable} is {@code null}.
     * @throws IndexOutOfBoundsException If the given start index is negative,
     *                                       the given end index is larger than the given array's length,
     *                                       or the given start index is larger than the given end index.
     * @throws IOException If an I/O error occurs.
     */
    public static void append(char[] array, int start, int end, Appendable destination) throws IOException {
        checkStartAndEnd(array, start, end);
        if (start < end) {
            if (destination instanceof Writer) {
                ((Writer) destination).write(array, start, end - start);
            } else if (destination instanceof StringBuilder) {
                ((StringBuilder) destination).append(array, start, end - start);
            } else if (destination instanceof StringBuffer) {
                ((StringBuffer) destination).append(array, start, end - start);
            } else {
                destination.append(wrapArray(array), start, end);
            }
        }
    }

    /**
     * Appends a string to an {@code Appendable}.
     *
     * @param str The string to append.
     * @param destination The {@code Appendable} to append to.
     * @throws NullPointerException If the given string or {@code Appendable} is {@code null}.
     * @throws IOException If an I/O error occurs.
     */
    public static void append(String str, Appendable destination) throws IOException {
        Objects.requireNonNull(str);
        destination.append(str);
    }

    /**
     * Appends a portion of a string to an {@code Appendable}.
     *
     * @param str The string to append a portion of.
     * @param destination The {@code Appendable} to append to.
     * @param start The start index of the portion to append, inclusive.
     * @param end The end index of the portion to append, exclusive.
     * @throws NullPointerException If the given string or {@code Appendable} is {@code null}.
     * @throws IndexOutOfBoundsException If the given start index is negative,
     *                                       the given end index is larger than the given string's length,
     *                                       or the given start index is larger than the given end index.
     * @throws IOException If an I/O error occurs.
     */
    public static void append(String str, int start, int end, Appendable destination) throws IOException {
        checkStartAndEnd(str, start, end);
        if (start < end) {
            if (destination instanceof Writer) {
                ((Writer) destination).write(str, start, end - start);
            } else {
                destination.append(str, start, end);
            }
        }
    }

    // maps

    /**
     * Returns a builder for maps that support both case sensitive and case insensitive mappings.
     * <p>
     * This method can be used to create builders for objects that use values (like obfuscators) per keys, where each key can be treated individually
     * as case sensitive or case insensitive. An example is {@link RequestParameterObfuscator.Builder}.
     * <p>
     * Note that like a {@link TreeMap} created with {@link String#CASE_INSENSITIVE_ORDER}, maps built with this builder fail to obey the general
     * contract of {@link Map#equals(Object)} if they contain any case insensitive mappings. Two maps built with the same settings will be equal to
     * each other though.
     *
     * @param <V> The value type for maps built by the returned builder.
     * @return A builder for maps that support both case sensitive and case insensitive mappings.
     */
    public static <V> MapBuilder<V> map() {
        return new MapBuilder<>();
    }

    /**
     * A builder for maps that support both case sensitive and case insensitive mappings.
     *
     * @author Rob Spoor
     * @param <V> The value type for built maps.
     */
    public static final class MapBuilder<V> {

        private final Map<String, V> caseSensitiveMap;
        private final Map<String, V> caseInsensitiveMap;

        private MapBuilder() {
            caseSensitiveMap = new HashMap<>();
            caseInsensitiveMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        }

        /**
         * Adds an entry.
         * This method is an alias for {@link #withEntry(String, Object, CaseSensitivity) withEntry(key, value, CASE_SENSITIVE)}.
         *
         * @param key The key for the entry.
         * @param value The value for the entry.
         * @return This object.
         * @throws NullPointerException If the key or value is {@code null}.
         * @throws IllegalArgumentException If an entry with the same key and the same case sensitivity was already added.
         */
        public MapBuilder<V> withEntry(String key, V value) {
            return withEntry(key, value, CASE_SENSITIVE);
        }

        /**
         * Adds an entry.
         *
         * @param key The key for the entry.
         * @param value The value for the entry.
         * @param caseSensitivity The case sensitivity for the key.
         * @return This object.
         * @throws NullPointerException If the key, value or case sensitivity is {@code null}.
         * @throws IllegalArgumentException If an entry with the same key and the same case sensitivity was already added.
         */
        public MapBuilder<V> withEntry(String key, V value, CaseSensitivity caseSensitivity) {
            Objects.requireNonNull(key);
            Objects.requireNonNull(value);
            Objects.requireNonNull(caseSensitivity);

            Map<String, V> map = caseSensitivity.isCaseSensitive() ? caseSensitiveMap : caseInsensitiveMap;
            if (map.containsKey(key)) {
                throw new IllegalArgumentException(Messages.stringMap.duplicateKey.get(key, caseSensitivity));
            }
            map.put(key, value);
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
        public <R> R transform(Function<? super MapBuilder<?>, ? extends R> f) {
            return f.apply(this);
        }

        /**
         * Returns an immutable map with the entries added to this builder. This map is serializable.
         *
         * @return An immutable map with the entries added to this builder.
         */
        public Map<String, V> build() {
            if (caseSensitiveMap.isEmpty() && caseInsensitiveMap.isEmpty()) {
                return Collections.emptyMap();
            }
            if (caseSensitiveMap.isEmpty()) {
                return caseInsensitiveMap();
            }
            if (caseInsensitiveMap.isEmpty()) {
                return caseSensitiveMap();
            }
            return new StringMap<>(this);
        }

        Map<String, V> caseSensitiveMap() {
            return Collections.unmodifiableMap(new HashMap<>(caseSensitiveMap));
        }

        Map<String, V> caseInsensitiveMap() {
            Map<String, V> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            result.putAll(caseInsensitiveMap);
            return Collections.unmodifiableMap(result);
        }
    }

    private static final class StringMap<V> extends AbstractMap<String, V> implements Serializable {

        private static final long serialVersionUID = -5691881236129965377L;

        private final Map<String, V> caseSensitiveMap;
        private final Map<String, V> caseInsensitiveMap;

        private transient Set<Entry<String, V>> entrySet;

        private StringMap(MapBuilder<V> builder) {
            caseSensitiveMap = builder.caseSensitiveMap();
            caseInsensitiveMap = builder.caseInsensitiveMap();
        }

        @Override
        public int size() {
            long size = (long) caseSensitiveMap.size() + caseInsensitiveMap.size();
            return (int) Math.min(size, Integer.MAX_VALUE);
        }

        @Override
        public boolean isEmpty() {
            // this should always return false since the StringMap instance is only created if neither map is empty
            return caseSensitiveMap.isEmpty() && caseInsensitiveMap.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return key instanceof String && (caseSensitiveMap.containsKey(key) || caseInsensitiveMap.containsKey(key));
        }

        @Override
        public boolean containsValue(Object value) {
            return value != null && (caseSensitiveMap.containsValue(value) || caseInsensitiveMap.containsValue(value));
        }

        @Override
        public V get(Object key) {
            if (key instanceof String) {
                V value = caseSensitiveMap.get(key);
                return value != null ? value : caseInsensitiveMap.get(key);
            }
            return null;
        }

        @Override
        public V put(String key, V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V remove(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putAll(Map<? extends String, ? extends V> m) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<Entry<String, V>> entrySet() {
            if (entrySet == null) {
                entrySet = new EntrySet();
            }
            return entrySet;
        }

        private class EntrySet extends AbstractSet<Entry<String, V>> {

            @Override
            public int size() {
                return StringMap.this.size();
            }

            @Override
            public boolean isEmpty() {
                return StringMap.this.isEmpty();
            }

            @Override
            public boolean contains(Object o) {
                return o instanceof Entry<?, ?> && contains((Entry<?, ?>) o);
            }

            private boolean contains(Entry<?, ?> entry) {
                Object key = entry.getKey();
                if (key instanceof String) {
                    V value = get(entry.getKey());
                    return value != null && value.equals(entry.getValue());
                }
                return false;
            }

            @Override
            public Iterator<Entry<String, V>> iterator() {
                return new Iterator<Entry<String, V>>() {
                    private final Iterator<Entry<String, V>> caseSensitiveIterator = caseSensitiveMap.entrySet().iterator();
                    private final Iterator<Entry<String, V>> caseInsensitiveIterator = caseInsensitiveMap.entrySet().iterator();

                    @Override
                    public boolean hasNext() {
                        return caseSensitiveIterator.hasNext() || caseInsensitiveIterator.hasNext();
                    }

                    @Override
                    public Entry<String, V> next() {
                        // Let caseInsensitiveIterator throw an exception if both iterators have been exhausted
                        return caseSensitiveIterator.hasNext() ? caseSensitiveIterator.next() : caseInsensitiveIterator.next();
                    }
                };
            }

            @Override
            public boolean add(Entry<String, V> e) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean remove(Object o) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean addAll(Collection<? extends Entry<String, V>> c) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean retainAll(Collection<?> c) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean removeAll(Collection<?> c) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void clear() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean removeIf(Predicate<? super Entry<String, V>> filter) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Stream<Entry<String, V>> stream() {
                return Stream.concat(caseSensitiveMap.entrySet().stream(), caseInsensitiveMap.entrySet().stream());
            }

            @Override
            public Stream<Entry<String, V>> parallelStream() {
                return Stream.concat(caseSensitiveMap.entrySet().parallelStream(), caseInsensitiveMap.entrySet().parallelStream());
            }

            @Override
            public void forEach(Consumer<? super Entry<String, V>> action) {
                Objects.requireNonNull(action);
                caseSensitiveMap.entrySet().forEach(action);
                caseInsensitiveMap.entrySet().forEach(action);
            }
        }

        @Override
        public V getOrDefault(Object key, V defaultValue) {
            V value = get(key);
            return value != null ? value : defaultValue;
        }

        @Override
        public void forEach(BiConsumer<? super String, ? super V> action) {
            Objects.requireNonNull(action);
            caseSensitiveMap.forEach(action);
            caseInsensitiveMap.forEach(action);
        }

        @Override
        public void replaceAll(BiFunction<? super String, ? super V, ? extends V> function) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V putIfAbsent(String key, V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object key, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean replace(String key, V oldValue, V newValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V replace(String key, V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int hashCode() {
            // The hash code is defined to be the sum of all entries.
            // That means that the result is the sum of the hash codes of both individual maps.
            return caseSensitiveMap.hashCode() + caseInsensitiveMap.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof StringMap<?>) {
                StringMap<?> other = (StringMap<?>) obj;
                // If both maps are equal then the objects are equal.
                // This matches mostly what Map.equals defines, apart from the case insensitive checks.
                // However, that is the same issue that TreeMap has when using a Comparator that is not consistent with equals.
                return caseSensitiveMap.equals(other.caseSensitiveMap) && caseInsensitiveMap.equals(other.caseInsensitiveMap);
            }
            // delegate to super.equals which performs entry-by-entry comparison
            return super.equals(obj);
        }
    }
}
