/*
 * LimitAppendableTest.java
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

import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.appendAtMost;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings("nls")
class LimitAppendableTest extends StreamTestBase {

    @Nested
    @DisplayName("zero limit")
    class ZeroLimit {

        @Test
        @DisplayName("append(char)")
        void testAppendChar() throws IOException {
            Writer writer = new StringWriter();
            LimitAppendable appendable = appendAtMost(writer, 0);

            assertTrue(appendable.limitReached());
            assertFalse(appendable.limitExceeded());

            for (int i = 0; i < SOURCE.length(); i++) {
                appendable.append(SOURCE.charAt(i));
                assertTrue(appendable.limitReached());
                assertTrue(appendable.limitExceeded());
            }
            assertEquals("", writer.toString());
            assertTrue(appendable.limitReached());
            assertTrue(appendable.limitExceeded());
        }

        @Test
        @DisplayName("append(CharSequence)")
        void testAppendCharSequence() throws IOException {
            Writer writer = new StringWriter();
            LimitAppendable appendable = appendAtMost(writer, 0);

            assertTrue(appendable.limitReached());
            assertFalse(appendable.limitExceeded());

            for (int i = 0; i < SOURCE.length(); i += 4) {
                appendable.append(SOURCE.subSequence(i, Math.min(SOURCE.length(), i + 4)));
                assertTrue(appendable.limitReached());
                assertTrue(appendable.limitExceeded());
            }
            assertEquals("", writer.toString());
            assertTrue(appendable.limitReached());
            assertTrue(appendable.limitExceeded());
        }

        @Test
        @DisplayName("append(null)")
        void testAppendNullCharSequence() throws IOException {
            Writer writer = new StringWriter();
            LimitAppendable appendable = appendAtMost(writer, 0);

            assertTrue(appendable.limitReached());
            assertFalse(appendable.limitExceeded());

            appendable.append(null);

            assertEquals("", writer.toString());
            assertTrue(appendable.limitReached());
            assertTrue(appendable.limitExceeded());
        }

        @Test
        @DisplayName("append(CharSequence, int, int)")
        void testAppendCharSequencePortion() throws IOException {
            Writer writer = new StringWriter();
            LimitAppendable appendable = appendAtMost(writer, 0);

            assertTrue(appendable.limitReached());
            assertFalse(appendable.limitExceeded());

            for (int i = 0; i < SOURCE.length(); i += 4) {
                appendable.append(SOURCE, i, Math.min(SOURCE.length(), i + 4));
                assertTrue(appendable.limitReached());
                assertTrue(appendable.limitExceeded());
            }
            assertEquals("", writer.toString());
            assertTrue(appendable.limitReached());
            assertTrue(appendable.limitExceeded());
        }

        @Test
        @DisplayName("append(null, int, int)")
        void testAppendNullCharSequencePortion() throws IOException {
            Writer writer = new StringWriter();
            LimitAppendable appendable = appendAtMost(writer, 0);

            assertTrue(appendable.limitReached());
            assertFalse(appendable.limitExceeded());

            appendable.append(null, 0, 2);

            assertEquals("", writer.toString());
            assertTrue(appendable.limitReached());
            assertTrue(appendable.limitExceeded());
        }
    }

    @Nested
    @DisplayName("limit smaller than input size")
    class LimitSmallerThanInputSize {

        @Test
        @DisplayName("append(char)")
        void testAppendChar() throws IOException {
            Writer writer = new StringWriter();
            LimitAppendable appendable = appendAtMost(writer, 5);

            assertFalse(appendable.limitReached());
            assertFalse(appendable.limitExceeded());

            for (int i = 0; i < SOURCE.length(); i++) {
                appendable.append(SOURCE.charAt(i));
                // Limit reached: i + 1 >= 5
                // Limit exceeded: i + 1 > 5
                assertEquals(i >= 4, appendable.limitReached());
                assertEquals(i > 4, appendable.limitExceeded());
            }
            assertEquals(SOURCE.substring(0, 5), writer.toString());
            assertTrue(appendable.limitReached());
            assertTrue(appendable.limitExceeded());
        }

        @Test
        @DisplayName("append(CharSequence)")
        void testAppendCharSequence() throws IOException {
            Writer writer = new StringWriter();
            LimitAppendable appendable = appendAtMost(writer, 5);

            assertFalse(appendable.limitReached());
            assertFalse(appendable.limitExceeded());

            for (int i = 0; i < SOURCE.length(); i += 4) {
                appendable.append(SOURCE.subSequence(i, Math.min(SOURCE.length(), i + 4)));
                // Limit reached: i + 4 >= 5
                // Limit exceeded: i + 4 > 5
                assertEquals(i >= 1, appendable.limitReached());
                assertEquals(i > 1, appendable.limitExceeded());
            }
            assertEquals(SOURCE.substring(0, 5), writer.toString());
            assertTrue(appendable.limitReached());
            assertTrue(appendable.limitExceeded());
        }

        @Test
        @DisplayName("append(null)")
        void testAppendNullCharSequence() throws IOException {
            Writer writer = new StringWriter();
            LimitAppendable appendable = appendAtMost(writer, 3);

            assertFalse(appendable.limitReached());
            assertFalse(appendable.limitExceeded());

            appendable.append(null);

            assertEquals("nul", writer.toString());
            assertTrue(appendable.limitReached());
            assertTrue(appendable.limitExceeded());
        }

        @Test
        @DisplayName("append(CharSequence, int, int)")
        void testAppendCharSequencePortion() throws IOException {
            Writer writer = new StringWriter();
            LimitAppendable appendable = appendAtMost(writer, 5);

            assertFalse(appendable.limitReached());
            assertFalse(appendable.limitExceeded());

            for (int i = 0; i < SOURCE.length(); i += 4) {
                appendable.append(SOURCE, i, Math.min(SOURCE.length(), i + 4));
                // Limit reached: i + 4 >= 5
                // Limit exceeded: i + 4 > 5
                assertEquals(i >= 1, appendable.limitReached());
                assertEquals(i > 1, appendable.limitExceeded());
            }
            assertEquals(SOURCE.substring(0, 5), writer.toString());
            assertTrue(appendable.limitReached());
            assertTrue(appendable.limitExceeded());
        }

        @Test
        @DisplayName("append(null, int, int)")
        void testAppendNullCharSequencePortion() throws IOException {
            Writer writer = new StringWriter();
            LimitAppendable appendable = appendAtMost(writer, 2);

            assertFalse(appendable.limitReached());
            assertFalse(appendable.limitExceeded());

            appendable.append(null, 0, 3);

            assertEquals("nu", writer.toString());
            assertTrue(appendable.limitReached());
            assertTrue(appendable.limitExceeded());
        }
    }

    @Nested
    @DisplayName("limit equal to input size")
    class LimitEqualToInputSize {

        @Test
        @DisplayName("append(char)")
        void testAppendChar() throws IOException {
            Writer writer = new StringWriter();
            LimitAppendable appendable = appendAtMost(writer, SOURCE.length());

            assertFalse(appendable.limitReached());
            assertFalse(appendable.limitExceeded());

            for (int i = 0; i < SOURCE.length(); i++) {
                appendable.append(SOURCE.charAt(i));
                // Limit reached: i + 1 == SOURCE.length()
                assertEquals(i == SOURCE.length() - 1, appendable.limitReached());
                assertFalse(appendable.limitExceeded());
            }
            assertEquals(SOURCE, writer.toString());
            assertTrue(appendable.limitReached());
            assertFalse(appendable.limitExceeded());
        }

        @Test
        @DisplayName("append(CharSequence)")
        void testAppendCharSequence() throws IOException {
            Writer writer = new StringWriter();
            LimitAppendable appendable = appendAtMost(writer, SOURCE.length());

            assertFalse(appendable.limitReached());
            assertFalse(appendable.limitExceeded());

            for (int i = 0; i < SOURCE.length(); i += 4) {
                appendable.append(SOURCE.subSequence(i, Math.min(SOURCE.length(), i + 4)));
                // Limit reached: i + 4 >= SOURCE.length()
                assertEquals(i >= SOURCE.length() - 4, appendable.limitReached());
                assertFalse(appendable.limitExceeded());
            }
            assertEquals(SOURCE, writer.toString());
            assertTrue(appendable.limitReached());
            assertFalse(appendable.limitExceeded());
        }

        @Test
        @DisplayName("append(null)")
        void testAppendNullCharSequence() throws IOException {
            Writer writer = new StringWriter();
            LimitAppendable appendable = appendAtMost(writer, 4);

            assertFalse(appendable.limitReached());
            assertFalse(appendable.limitExceeded());

            appendable.append(null);

            assertEquals("null", writer.toString());
            assertTrue(appendable.limitReached());
            assertFalse(appendable.limitExceeded());
        }

        @Test
        @DisplayName("append(CharSequence, int, int)")
        void testAppendCharSequencePortion() throws IOException {
            Writer writer = new StringWriter();
            LimitAppendable appendable = appendAtMost(writer, SOURCE.length());

            assertFalse(appendable.limitReached());
            assertFalse(appendable.limitExceeded());

            for (int i = 0; i < SOURCE.length(); i += 4) {
                appendable.append(SOURCE, i, Math.min(SOURCE.length(), i + 4));
                // Limit reached: i + 4 >= SOURCE.length()
                assertEquals(i >= SOURCE.length() - 4, appendable.limitReached());
                assertFalse(appendable.limitExceeded());
            }
            assertEquals(SOURCE, writer.toString());
            assertTrue(appendable.limitReached());
            assertFalse(appendable.limitExceeded());
        }

        @Test
        @DisplayName("append(null, int, int)")
        void testAppendNullCharSequencePortion() throws IOException {
            Writer writer = new StringWriter();
            LimitAppendable appendable = appendAtMost(writer, 4);

            assertFalse(appendable.limitReached());
            assertFalse(appendable.limitExceeded());

            appendable.append(null, 0, 2);
            appendable.append(null, 2, 4);

            assertEquals("null", writer.toString());
            assertTrue(appendable.limitReached());
            assertFalse(appendable.limitExceeded());
        }
    }

    @Nested
    @DisplayName("limit larger than input size")
    class LimitLargerThanInputSize {

        @Test
        @DisplayName("append(char)")
        void testAppendChar() throws IOException {
            Writer writer = new StringWriter();
            LimitAppendable appendable = appendAtMost(writer, SOURCE.length() + 1);

            assertFalse(appendable.limitReached());
            assertFalse(appendable.limitExceeded());

            for (int i = 0; i < SOURCE.length(); i++) {
                appendable.append(SOURCE.charAt(i));
                assertFalse(appendable.limitReached());
                assertFalse(appendable.limitExceeded());
            }
            assertEquals(SOURCE, writer.toString());
            assertFalse(appendable.limitReached());
            assertFalse(appendable.limitExceeded());
        }

        @Test
        @DisplayName("append(CharSequence)")
        void testAppendCharSequence() throws IOException {
            Writer writer = new StringWriter();
            LimitAppendable appendable = appendAtMost(writer, SOURCE.length() + 1);

            assertFalse(appendable.limitReached());
            assertFalse(appendable.limitExceeded());

            for (int i = 0; i < SOURCE.length(); i += 4) {
                appendable.append(SOURCE.subSequence(i, Math.min(SOURCE.length(), i + 4)));
                assertFalse(appendable.limitReached());
                assertFalse(appendable.limitExceeded());
            }
            assertEquals(SOURCE, writer.toString());
            assertFalse(appendable.limitReached());
            assertFalse(appendable.limitExceeded());
        }

        @Test
        @DisplayName("append(null)")
        void testAppendNullCharSequence() throws IOException {
            Writer writer = new StringWriter();
            LimitAppendable appendable = appendAtMost(writer, 5);

            assertFalse(appendable.limitReached());
            assertFalse(appendable.limitExceeded());

            appendable.append(null);

            assertEquals("null", writer.toString());
            assertFalse(appendable.limitReached());
            assertFalse(appendable.limitExceeded());
        }

        @Test
        @DisplayName("append(CharSequence, int, int)")
        void testAppendCharSequencePortion() throws IOException {
            Writer writer = new StringWriter();
            LimitAppendable appendable = appendAtMost(writer, SOURCE.length() + 1);

            assertFalse(appendable.limitReached());
            assertFalse(appendable.limitExceeded());

            for (int i = 0; i < SOURCE.length(); i += 4) {
                appendable.append(SOURCE, i, Math.min(SOURCE.length(), i + 4));
                assertFalse(appendable.limitReached());
                assertFalse(appendable.limitExceeded());
            }
            assertEquals(SOURCE, writer.toString());
            assertFalse(appendable.limitReached());
            assertFalse(appendable.limitExceeded());
        }

        @Test
        @DisplayName("append(null, int, int)")
        void testAppendNullCharSequencePortion() throws IOException {
            Writer writer = new StringWriter();
            LimitAppendable appendable = appendAtMost(writer, 5);

            assertFalse(appendable.limitReached());
            assertFalse(appendable.limitExceeded());

            appendable.append(null, 0, 2);
            appendable.append(null, 2, 4);

            assertEquals("null", writer.toString());
            assertFalse(appendable.limitReached());
            assertFalse(appendable.limitExceeded());
        }
    }
}
