/*
 * Obfuscator.java
 * Copyright 2019 Rob Spoor
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

import static com.github.robtimus.obfuscation.ObfuscatorUtils.checkOffsetAndLength;
import static com.github.robtimus.obfuscation.ObfuscatorUtils.checkStartAndEnd;
import static com.github.robtimus.obfuscation.ObfuscatorUtils.readAll;
import static com.github.robtimus.obfuscation.ObfuscatorUtils.repeatChar;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A class that will obfuscate {@link CharSequence CharSequences} or the contents of {@link Reader Readers}.
 *
 * @author Rob Spoor
 */
public abstract class Obfuscator {

    private static final char DEFAULT_MASK_CHAR = '*';

    /**
     * Obfuscates the contents a {@code CharSequence}.
     *
     * @param s The {@code CharSequence} with the contents to obfuscate.
     * @return The obfuscated contents.
     * @throws NullPointerException If the given {@code CharSequence} is {@code null}.
     */
    public final CharSequence obfuscateText(CharSequence s) {
        return obfuscateText(s, 0, s.length());
    }

    /**
     * Obfuscates parts of the contents a {@code CharSequence}.
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
     * Obfuscates the contents a {@code CharSequence}.
     *
     * @param s The {@code CharSequence} with the contents to obfuscate.
     * @param destination The {@code Appendable} to append the obfuscated contents to.
     * @throws NullPointerException If the given {@code CharSequence} or {@code Appendable} is {@code null}.
     * @throws IOException If an I/O error occurs.
     */
    public final void obfuscateText(CharSequence s, Appendable destination) throws IOException {
        obfuscateText(s, 0, s.length(), destination);
    }

    /**
     * Obfuscates parts of the contents a {@code CharSequence}.
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
    public final CharSequence obfuscateText(Reader input) throws IOException {
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
        return obfuscateList(list, Object::toString);
    }

    /**
     * Obfuscates a list.
     * <p>
     * The result will be a list that will behave exactly the same as the given list, except it will obfuscate each element when its
     * {@link Object#toString() toString()} method is called. This is different from {@link #obfuscateObject(Object, Supplier)} because it will not
     * obfuscate the list structure or the number of elements.
     *
     * @param <E> The list's element type.
     * @param list The list to obfuscate.
     * @param elementRepresentation A function to provide the string representation that will be used to obfuscate each element.
     * @return An obfuscating list wrapper around the given list.
     * @throws NullPointerException If the given list or function is {@code null}.
     */
    public final <E> List<E> obfuscateList(List<E> list, Function<? super E, ? extends CharSequence> elementRepresentation) {
        Objects.requireNonNull(list);
        Objects.requireNonNull(elementRepresentation);
        return ObfuscatingList.of(list, elementRepresentation, this::obfuscateText);
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
        return obfuscateSet(set, Object::toString);
    }

    /**
     * Obfuscates a set.
     * <p>
     * The result will be a set that will behave exactly the same as the given set, except it will obfuscate each element when its
     * {@link Object#toString() toString()} method is called. This is different from {@link #obfuscateObject(Object, Supplier)} because it will not
     * obfuscate the set structure or the number of elements.
     *
     * @param <E> The set's element type.
     * @param set The set to obfuscate.
     * @param elementRepresentation A function to provide the string representation that will be used to obfuscate each element.
     * @return An obfuscating set wrapper around the given set.
     * @throws NullPointerException If the given set or function is {@code null}.
     */
    public final <E> Set<E> obfuscateSet(Set<E> set, Function<? super E, ? extends CharSequence> elementRepresentation) {
        Objects.requireNonNull(set);
        Objects.requireNonNull(elementRepresentation);
        return new ObfuscatingSet<>(set, elementRepresentation, this::obfuscateText);
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
        return obfuscateCollection(collection, Object::toString);
    }

    /**
     * Obfuscates a collection.
     * <p>
     * The result will be a collection that will behave exactly the same as the given collection, except it will obfuscate each element when its
     * {@link Object#toString() toString()} method is called. This is different from {@link #obfuscateObject(Object, Supplier)} because it will not
     * obfuscate the collection structure or the number of elements.
     *
     * @param <E> The collection's element type.
     * @param collection The collection to obfuscate.
     * @param elementRepresentation A function to provide the string representation that will be used to obfuscate each element.
     * @return An obfuscating collection wrapper around the given collection.
     * @throws NullPointerException If the given collection or function is {@code null}.
     */
    public final <E> Collection<E> obfuscateCollection(Collection<E> collection, Function<? super E, ? extends CharSequence> elementRepresentation) {
        Objects.requireNonNull(collection);
        Objects.requireNonNull(elementRepresentation);
        return new ObfuscatingCollection<>(collection, elementRepresentation, this::obfuscateText);
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
        return obfuscateMap(map, Object::toString);
    }

    /**
     * Obfuscates a map.
     * <p>
     * The result will be a map that will behave exactly the same as the given map, except it will obfuscate each value when its
     * {@link Object#toString() toString()} method is called. This is different from {@link #obfuscateObject(Object)} because it will not obfuscate
     * the map structure or the number of entries.
     *
     * @param <K> The map's key type.
     * @param <V> The map's value type.
     * @param map The map to obfuscate.
     * @param valueRepresentation A function to provide the string representation that will be used to obfuscate each value.
     * @return An obfuscating map wrapper around the given map.
     * @throws NullPointerException If the given map or function is {@code null}.
     */
    public final <K, V> Map<K, V> obfuscateMap(Map<K, V> map, Function<? super V, ? extends CharSequence> valueRepresentation) {
        Objects.requireNonNull(map);
        Objects.requireNonNull(valueRepresentation);
        return new ObfuscatingMap<>(map, (k, v) -> valueRepresentation.apply(v), (k, v) -> obfuscateText(v));
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
        public CharSequence obfuscateText(CharSequence s, int start, int end) {
            checkStartAndEnd(s, start, end);
            return repeatChar(maskChar, end - start);
        }

        @Override
        public void obfuscateText(CharSequence s, int start, int end, Appendable destination) throws IOException {
            checkStartAndEnd(s, start, end);
            ObfuscatorUtils.append(maskChar, end - start, destination);
        }

        @Override
        public void obfuscateText(Reader input, Appendable destination) throws IOException {
            if (destination instanceof Writer) {
                obfuscateText(input, (Writer) destination);
                return;
            }

            char[] buffer = new char[1024];
            char[] mask = new char[buffer.length];
            Arrays.fill(mask, maskChar);
            CharArraySequence csq = new CharArraySequence(mask);

            int len;
            while ((len = input.read(buffer)) != -1) {
                csq.resetWithStartAndEnd(0, len);
                destination.append(csq);
            }
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
        public CharSequence obfuscateText(CharSequence s, int start, int end) {
            checkStartAndEnd(s, start, end);
            return start == 0 && end == s.length() ? s : s.subSequence(start, end);
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

            char[] buffer = new char[1024];
            CharArraySequence csq = new CharArraySequence(buffer);

            int len;
            while ((len = input.read(buffer)) != -1) {
                csq.resetWithStartAndEnd(0, len);
                destination.append(csq);
            }
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
            if (this == o) {
                return true;
            }
            if (o == null || o.getClass() != getClass()) {
                return false;
            }
            return true;
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
        public CharSequence obfuscateText(CharSequence s, int start, int end) {
            checkStartAndEnd(s, start, end);
            return fixedMask;
        }

        @Override
        public void obfuscateText(CharSequence s, int start, int end, Appendable destination) throws IOException {
            checkStartAndEnd(s, start, end);
            destination.append(fixedMask);
        }

        @Override
        public void obfuscateText(Reader input, Appendable destination) throws IOException {
            char[] buffer = new char[1024];
            while (input.read(buffer) != -1) {
                // do nothing
            }
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
        public CharSequence obfuscateText(CharSequence s, int start, int end) {
            checkStartAndEnd(s, start, end);
            return fixedValue;
        }

        @Override
        public void obfuscateText(CharSequence s, int start, int end, Appendable destination) throws IOException {
            checkStartAndEnd(s, start, end);
            destination.append(fixedValue);
        }

        @Override
        public void obfuscateText(Reader input, Appendable destination) throws IOException {
            char[] buffer = new char[1024];
            while (input.read(buffer) != -1) {
                // do nothing
            }
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
     * If this range is empty, such an obfuscator will not obfuscate anything, unless if {@link #withFixedLength(int)} is specified..
     *
     * @author Rob Spoor
     */
    public static final class PortionBuilder {

        private int keepAtStart;
        private int keepAtEnd;
        private int atLeastFromStart;
        private int atLeastFromEnd;
        private int fixedLength;
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
         * Sets or removes the fixed number of {@link #withMaskChar(char) mask characters} to use for obfuscating.
         *
         * @param fixedLength The fixed number of mask characters, or a negative value to use the actual length of the input.
         *                        The default is {@code -1}.
         * @return This builder.
         */
        public PortionBuilder withFixedLength(int fixedLength) {
            this.fixedLength = Math.max(-1, fixedLength);
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
        private final int fixedLength;
        private final char maskChar;

        private PortionObfuscator(PortionBuilder builder) {
            this.keepAtStart = builder.keepAtStart;
            this.keepAtEnd = builder.keepAtEnd;
            this.atLeastFromStart = builder.atLeastFromStart;
            this.atLeastFromEnd = builder.atLeastFromEnd;
            this.fixedLength = builder.fixedLength;
            this.maskChar = builder.maskChar;
        }

        private int from(int length) {
            if (atLeastFromStart > 0) {
                // the first characters need to be obfuscated so ignore fromStart
                return 0;
            }
            // 0 <= keepAtMost <= length, the maximum number of characters to not obfuscate taking into account atLeastFromEnd
            // 0 <= result <= length, the minimum of what we want to obfuscate and what we can obfuscate
            int keepAtMost = Math.max(0, length - atLeastFromEnd);
            return Math.min(keepAtStart, keepAtMost);
        }

        private int to(int length, int keepFromStart) {
            if (atLeastFromEnd > 0) {
                // the last characters need to be obfuscated so ignore fromEnd
                return 0;
            }
            // 0 <= available <= length, the number of characters not already handled by fromStart (to prevent characters being appended twice)
            // 0 <= keepAtMost <= length, the maximum number of characters to not obfuscate taking into account atLeastFromStart
            // 0 <= result <= length, the minimum of what we want to obfuscate and what we can obfuscate
            int available = length - keepFromStart;
            int keepAtMost = Math.max(0, length - atLeastFromStart);
            return Math.min(keepAtEnd, Math.min(available, keepAtMost));
        }

        @Override
        public CharSequence obfuscateText(CharSequence s, int start, int end) {
            checkStartAndEnd(s, start, end);

            int length = end - start;
            int from = from(length);
            int to = to(length, from);
            // 0 <= from <= length == end - start, so start <= from + start <= end
            // 0 <= to <= length == end - start, so 0 <= length - to and start <= end - to

            if (fixedLength > 0) {
                // length - to - from needs to be fixedLength, so length needs to be fixedLength + from + to
                length = fixedLength + from + to;
            }

            char[] array = new char[length];

            // first build the content as expected: fromStart non-obfuscated, then obfuscated, then fromEnd non-obfuscated
            for (int i = 0, j = start; i < from; i++, j++) {
                array[i] = s.charAt(j);
            }
            for (int i = from; i < length - to; i++) {
                array[i] = maskChar;
            }
            for (int i = length - to, j = end - to; i < length; i++, j++) {
                array[i] = s.charAt(j);
            }
            return new CharArraySequence(array);
        }

        @Override
        public void obfuscateText(CharSequence s, int start, int end, Appendable destination) throws IOException {
            checkStartAndEnd(s, start, end);

            int length = end - start;

            int from = from(length);
            int to = to(length, from);
            // 0 <= from <= length == end - start, so start <= from + start <= end
            // 0 <= to <= length == end - start, so 0 <= length - to and start <= end - to

            // first build the content as expected: fromStart non-obfuscated, then obfuscated, then fromEnd non-obfuscated
            if (from > 0) {
                destination.append(s, start, start + from);
            }
            ObfuscatorUtils.append(maskChar, fixedLength < 0 ? length - to - from : fixedLength, destination);
            if (to > 0) {
                destination.append(s, end - to, end);
            }
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
                    && fixedLength == other.fixedLength
                    && maskChar == other.maskChar;
        }

        @Override
        public int hashCode() {
            return keepAtStart ^ keepAtEnd ^ atLeastFromStart ^ atLeastFromEnd ^ fixedLength ^ maskChar;
        }

        @Override
        @SuppressWarnings("nls")
        public String toString() {
            return Obfuscator.class.getName() + "#portion[keepAtStart=" + keepAtStart + ",keepAtEnd=" + keepAtEnd
                    + ",atLeastFromStart=" + atLeastFromStart + ",atLeastFromEnd=" + atLeastFromEnd
                    + ",fixedLength=" + fixedLength
                    + ",maskChar=" + maskChar + "]";
        }
    }

    /**
     * Returns a builder that will create obfuscators that can handle UTF-8 encoded request parameter strings.
     * These can be used for both query strings and form data strings.
     *
     * @return A builder that will create obfuscators that can handle request parameter strings.
     */
    public static PropertyObfuscatorBuilder requestParameters() {
        return new PropertyObfuscatorBuilder(b -> new RequestParameterObfuscator(b, StandardCharsets.UTF_8));
    }

    /**
     * Returns a builder that will create obfuscators that can handle request parameter strings.
     * These can be used for both query strings and form data strings.
     *
     * @param encoding The encoding to use.
     * @return A builder that will create obfuscators that can handle request parameter strings.
     * @throws NullPointerException If the encoding is {@code null}.
     */
    public static PropertyObfuscatorBuilder requestParameters(Charset encoding) {
        Objects.requireNonNull(encoding);
        return new PropertyObfuscatorBuilder(b -> new RequestParameterObfuscator(b, encoding));
    }
}
