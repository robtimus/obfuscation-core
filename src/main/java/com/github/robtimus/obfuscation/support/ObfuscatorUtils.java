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

package com.github.robtimus.obfuscation.support;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Objects;
import com.github.robtimus.obfuscation.Obfuscator;

/**
 * Utility methods that can be used for implementing {@link Obfuscator Obfuscators}.
 *
 * @author Rob Spoor
 */
public final class ObfuscatorUtils {

    private ObfuscatorUtils() {
        throw new IllegalStateException("cannot create instances of " + getClass().getName()); //$NON-NLS-1$
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
            throw new IndexOutOfBoundsException(Messages.charSequence.invalidOffsetOrLength.get(sequence.length(), offset, length));
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
            throw new IndexOutOfBoundsException(Messages.charSequence.invalidStartOrEnd.get(sequence.length(), start, end));
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

    /**
     * Concatenates two {@code CharSequence}s into one.
     *
     * @param first The first {@code CharSequence}.
     * @param second The second {@code CharSequence}.
     * @return The concatenated {@code CharSequence}.
     * @throws NullPointerException If either {@code CharSequence} is {@code null}.
     * @since 1.2
     */
    public static CharSequence concat(CharSequence first, CharSequence second) {
        Objects.requireNonNull(first);
        Objects.requireNonNull(second);
        return new ConcatCharSequence(first, second);
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
     * Returns a {@code Reader} that counts all text read from another {@code Reader}.
     *
     * @param input The {@code Reader} to read from.
     * @return A {@code Reader} that counts all text read from the given {@code Reader}.
     * @throws NullPointerException If the given {@code Reader} is {@code null}.
     * @since 1.4
     */
    @SuppressWarnings("resource")
    public static CountingReader counting(Reader input) {
        Objects.requireNonNull(input);
        return new CountingReader(input);
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
    @SuppressWarnings("resource")
    public static Reader copyTo(Reader input, Appendable appendable) {
        Objects.requireNonNull(input);
        Objects.requireNonNull(appendable);
        return new CopyingReader(input, appendable);
    }

    /**
     * Returns a {@code Reader} that is able to read only a portion of text from another {@code Reader}.
     *
     * @param input The {@code Reader} to read from.
     * @param limit The maximum number of characters to read from the given {@code Reader}.
     * @return A {@code Reader} that is able to read at most {@code limit} characters from the given {@code Reader}.
     * @throws NullPointerException If the given {@code Reader} is {@code null}.
     * @throws IllegalArgumentException If the given limit is negative.
     * @since 1.2
     */
    @SuppressWarnings("resource")
    public static Reader readAtMost(Reader input, int limit) {
        Objects.requireNonNull(input);
        if (limit < 0) {
            throw new IllegalArgumentException(limit + " < 0"); //$NON-NLS-1$
        }
        return new LimitReader(input, limit);
    }

    // Appendable

    /**
     * Returns an {@code Appendable} that will discard text after a specific amount of text has been appended.
     *
     * @param appendable The {@code Appendable} to append to.
     * @param limit The maximum number of characters to append to the given {@code Appendable}.
     * @return An {@code Appendable} that will discard text after a specific amount of text has been appended.
     * @throws NullPointerException If the given {@code Appendable} is {@code null}.
     * @throws IllegalArgumentException If the given limit is negative.
     * @since 1.4
     */
    public static LimitAppendable appendAtMost(Appendable appendable, long limit) {
        Objects.requireNonNull(appendable);
        if (limit < 0) {
            throw new IllegalArgumentException(limit + " < 0"); //$NON-NLS-1$
        }
        return new LimitAppendable(appendable, limit);
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
    @SuppressWarnings("resource")
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
    @SuppressWarnings("resource")
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
    @SuppressWarnings("resource")
    public static void copyAll(Reader input, Appendable destination) throws IOException {
        Objects.requireNonNull(input);
        Objects.requireNonNull(destination);

        char[] buffer = new char[1024];
        if (destination instanceof StringBuilder) {
            copyAll(input, (StringBuilder) destination, buffer);
        } else if (destination instanceof StringBuffer) {
            copyAll(input, (StringBuffer) destination, buffer);
        } else if (destination instanceof Writer) {
            copyAll(input, (Writer) destination, buffer);
        } else {
            CharArraySequence csq = new CharArraySequence(buffer);
            int len;
            while ((len = input.read(buffer)) != -1) {
                csq.resetWithStartAndEnd(0, len);
                destination.append(csq);
            }
        }
    }

    private static void copyAll(Reader input, StringBuilder destination, char[] buffer) throws IOException {
        int len;
        while ((len = input.read(buffer)) != -1) {
            destination.append(buffer, 0, len);
        }
    }

    private static void copyAll(Reader input, StringBuffer destination, char[] buffer) throws IOException {
        int len;
        while ((len = input.read(buffer)) != -1) {
            destination.append(buffer, 0, len);
        }
    }

    private static void copyAll(Reader input, Writer destination, char[] buffer) throws IOException {
        int len;
        while ((len = input.read(buffer)) != -1) {
            destination.write(buffer, 0, len);
        }
    }

    // masking

    /**
     * Copies the contents of a {@code Reader} to an {@code Appendable}, masking each character.
     *
     * @param input The {@code Reader} to copy the contents of.
     * @param maskChar The character to replace with.
     * @param destination The {@code Appendable} to copy the contents to.
     * @throws NullPointerException If the given {@code Reader} or {@code Appendable} is {@code null}.
     * @throws IOException If an I/O error occurs.
     */
    @SuppressWarnings("resource")
    public static void maskAll(Reader input, char maskChar, Appendable destination) throws IOException {
        Objects.requireNonNull(input);
        Objects.requireNonNull(destination);

        char[] buffer = new char[1024];
        char[] mask = new char[1024];
        Arrays.fill(mask, maskChar);
        if (destination instanceof StringBuilder) {
            maskAll(input, (StringBuilder) destination, buffer, mask);
        } else if (destination instanceof StringBuffer) {
            maskAll(input, (StringBuffer) destination, buffer, mask);
        } else if (destination instanceof Writer) {
            maskAll(input, (Writer) destination, buffer, mask);
        } else {
            CharArraySequence csq = new CharArraySequence(mask);
            int len;
            while ((len = input.read(buffer)) != -1) {
                csq.resetWithStartAndEnd(0, len);
                destination.append(csq);
            }
        }
    }

    private static void maskAll(Reader input, StringBuilder destination, char[] buffer, char[] mask) throws IOException {
        int len;
        while ((len = input.read(buffer)) != -1) {
            destination.append(mask, 0, len);
        }
    }

    private static void maskAll(Reader input, StringBuffer destination, char[] buffer, char[] mask) throws IOException {
        int len;
        while ((len = input.read(buffer)) != -1) {
            destination.append(mask, 0, len);
        }
    }

    private static void maskAll(Reader input, Writer destination, char[] buffer, char[] mask) throws IOException {
        int len;
        while ((len = input.read(buffer)) != -1) {
            destination.write(mask, 0, len);
        }
    }

    // appending

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
}
