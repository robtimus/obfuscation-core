/*
 * CharArraySequenceTest.java
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.function.ToIntFunction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings({ "javadoc", "nls" })
public class CharArraySequenceTest {

    @Test
    @DisplayName("charAt(int) and length()")
    public void testCharAtAndLength() {
        String string = "hello world";
        CharArraySequence sequence = new CharArraySequence(string.toCharArray());

        testCharAtAndLength(string, sequence);
    }

    private void testCharAtAndLength(String string, CharArraySequence sequence) {
        assertEquals(string.length(), sequence.length());
        for (int i = 0; i < sequence.length(); i++) {
            assertEquals(string.charAt(i), sequence.charAt(i));
        }
        assertThrows(IndexOutOfBoundsException.class, () -> sequence.charAt(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> sequence.charAt(sequence.length()));
    }

    @Test
    @DisplayName("toString()")
    public void testToString() {
        String string = "hello world";
        CharArraySequence sequence = new CharArraySequence(string.toCharArray());

        assertEquals(string, sequence.toString());
        // check caching
        assertSame(sequence.toString(), sequence.toString());
    }

    @Nested
    @DisplayName("subSequence(int, int)")
    public class SubSequence {

        @Test
        @DisplayName("charAt(int) and length()")
        public void testCharAtAndLength() {
            String source = "hello world";
            testCharAtAndLength(source, 0, source.length());
            testCharAtAndLength(source, 3, source.length());
            testCharAtAndLength(source, 0, 8);
            testCharAtAndLength(source, 3, 8);
        }

        private void testCharAtAndLength(String source, int start, int end) {
            String string = source.substring(start, end);
            CharArraySequence sequence = new CharArraySequence(source.toCharArray()).subSequence(start, end);

            CharArraySequenceTest.this.testCharAtAndLength(string, sequence);
        }

        @Test
        @DisplayName("toString()")
        public void testToString() {
            String source = "hello world";
            String string = source.substring(3, 8);
            CharArraySequence sequence = new CharArraySequence(source.toCharArray()).subSequence(3, 8);

            assertEquals(string, sequence.toString());
        }

        @Nested
        @DisplayName("subSequence(int, int)")
        public class SubSubSequence {

            @Test
            @DisplayName("charAt(int) and length()")
            public void testCharAtAndLength() {
                String source = "hello world";
                testCharAtAndLength(source, 0, String::length);
                testCharAtAndLength(source, 1, String::length);
                testCharAtAndLength(source, 0, s -> 4);
                testCharAtAndLength(source, 1, s -> 4);
            }

            private void testCharAtAndLength(String source, int start, ToIntFunction<String> endMapper) {
                String substring = source.substring(3, 8);
                int end = endMapper.applyAsInt(substring);
                String string = substring.substring(start, end);
                CharArraySequence sequence = new CharArraySequence(source.toCharArray()).subSequence(3, 8).subSequence(start, end);

                CharArraySequenceTest.this.testCharAtAndLength(string, sequence);
            }

            @Test
            @DisplayName("toString()")
            public void testToString() {
                String source = "hello world";
                String string = source.substring(3, 8).substring(1, 4);
                CharArraySequence sequence = new CharArraySequence(source.toCharArray()).subSequence(3, 8).subSequence(1, 4);

                assertEquals(string, sequence.toString());
            }
        }
    }
}
