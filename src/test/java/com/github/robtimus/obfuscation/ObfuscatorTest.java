/*
 * ObfuscatorTest.java
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

import static com.github.robtimus.obfuscation.Obfuscator.all;
import static com.github.robtimus.obfuscation.Obfuscator.fixedLength;
import static com.github.robtimus.obfuscation.Obfuscator.fixedValue;
import static com.github.robtimus.obfuscation.Obfuscator.fromFunction;
import static com.github.robtimus.obfuscation.Obfuscator.none;
import static com.github.robtimus.obfuscation.Obfuscator.portion;
import static com.github.robtimus.obfuscation.support.MessagesUtils.assertClosedException;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import com.github.robtimus.obfuscation.Obfuscator.PortionBuilder;
import com.github.robtimus.obfuscation.support.TestAppendable;

@SuppressWarnings("nls")
class ObfuscatorTest {

    @Nested
    @DisplayName("all()")
    @TestInstance(Lifecycle.PER_CLASS)
    class All extends NestedObfuscatorTest {

        All() {
            super(maskChar -> all(maskChar), new Arguments[] {
                    arguments("foo", '*', "***"),
                    arguments("foo", 'x', "xxx"),
                    arguments("hello", '*', "*****"),
                    arguments("hello", 'x', "xxxxx"),
            }, "", "****");
        }

        @ParameterizedTest(name = "{1}")
        @MethodSource
        @DisplayName("equals(Object)")
        void testEquals(Obfuscator obfuscator, Object object, boolean expected) {
            assertEquals(expected, obfuscator.equals(object));
        }

        Arguments[] testEquals() {
            Obfuscator obfuscator = all('x');
            return new Arguments[] {
                    arguments(obfuscator, obfuscator, true),
                    arguments(obfuscator, null, false),
                    arguments(obfuscator, all('x'), true),
                    arguments(obfuscator, all(), false),
                    arguments(obfuscator, "foo", false),
            };
        }

        @Test
        @DisplayName("hashCode()")
        void testHashCode() {
            Obfuscator obfuscator = all('x');
            assertEquals(obfuscator.hashCode(), obfuscator.hashCode());
            assertEquals(obfuscator.hashCode(), all('x').hashCode());
        }
    }

    @Nested
    @DisplayName("none()")
    @TestInstance(Lifecycle.PER_CLASS)
    class None extends NestedObfuscatorTest {

        None() {
            super(maskChar -> none(), new Arguments[] {
                    arguments("foo", '*', "foo"),
                    arguments("hello", '*', "hello"),
            }, "", "null");
        }

        @ParameterizedTest(name = "{1}")
        @MethodSource
        @DisplayName("equals(Object)")
        void testEquals(Obfuscator obfuscator, Object object, boolean expected) {
            assertEquals(expected, obfuscator.equals(object));
        }

        Arguments[] testEquals() {
            Obfuscator obfuscator = none();
            return new Arguments[] {
                    arguments(obfuscator, obfuscator, true),
                    arguments(obfuscator, null, false),
                    arguments(obfuscator, none(), true),
                    arguments(obfuscator, "foo", false),
            };
        }

        @Test
        @DisplayName("hashCode()")
        void testHashCode() {
            Obfuscator obfuscator = none();
            assertEquals(obfuscator.hashCode(), obfuscator.hashCode());
            assertEquals(obfuscator.hashCode(), none().hashCode());
        }
    }

    @Nested
    @DisplayName("fixedLength(8)")
    @TestInstance(Lifecycle.PER_CLASS)
    class FixedLength extends NestedObfuscatorTest {

        FixedLength() {
            super(maskChar -> fixedLength(8, maskChar), new Arguments[] {
                    arguments("foo", '*', "********"),
                    arguments("foo", 'x', "xxxxxxxx"),
                    arguments("hello", '*', "********"),
                    arguments("hello", 'x', "xxxxxxxx"),
            }, "********", "********");
        }

        @Test
        @DisplayName("negative length")
        void testNegativeLength() {
            assertThrows(IllegalArgumentException.class, () -> fixedLength(-1));
            assertThrows(IllegalArgumentException.class, () -> fixedLength(-1, 'x'));
        }

        @ParameterizedTest(name = "{1}")
        @MethodSource
        @DisplayName("equals(Object)")
        void testEquals(Obfuscator obfuscator, Object object, boolean expected) {
            assertEquals(expected, obfuscator.equals(object));
        }

        Arguments[] testEquals() {
            Obfuscator obfuscator = fixedLength(8, 'x');
            return new Arguments[] {
                    arguments(obfuscator, obfuscator, true),
                    arguments(obfuscator, null, false),
                    arguments(obfuscator, fixedLength(8, 'x'), true),
                    arguments(obfuscator, fixedLength(7, 'x'), false),
                    arguments(obfuscator, fixedLength(8), false),
                    arguments(obfuscator, "foo", false),
            };
        }

        @Test
        @DisplayName("hashCode()")
        void testHashCode() {
            Obfuscator obfuscator = fixedLength(8, 'x');
            assertEquals(obfuscator.hashCode(), obfuscator.hashCode());
            assertEquals(obfuscator.hashCode(), fixedLength(8, 'x').hashCode());
        }
    }

    @Nested
    @DisplayName("fixedLength(0)")
    @TestInstance(Lifecycle.PER_CLASS)
    class ZeroFixedLength extends NestedObfuscatorTest {

        ZeroFixedLength() {
            super(maskChar -> fixedLength(0, maskChar), new Arguments[] {
                    arguments("foo", '*', ""),
                    arguments("hello", '*', ""),
            }, "", "");
        }
    }

    @Nested
    @DisplayName("fixedValue(\"obfuscated\")")
    @TestInstance(Lifecycle.PER_CLASS)
    class FixedValue extends NestedObfuscatorTest {

        FixedValue() {
            super(maskChar -> fixedValue("obfuscated"), new Arguments[] {
                    arguments("foo", '*', "obfuscated"),
                    arguments("foo", 'x', "obfuscated"),
                    arguments("hello", '*', "obfuscated"),
                    arguments("hello", 'x', "obfuscated"),
            }, "obfuscated", "obfuscated");
        }

        @Test
        @DisplayName("null fixed value")
        void testNullFixedValue() {
            assertThrows(NullPointerException.class, () -> fixedValue(null));
        }

        @ParameterizedTest(name = "{1}")
        @MethodSource
        @DisplayName("equals(Object)")
        void testEquals(Obfuscator obfuscator, Object object, boolean expected) {
            assertEquals(expected, obfuscator.equals(object));
        }

        Arguments[] testEquals() {
            Obfuscator obfuscator = fixedValue("obfuscated");
            return new Arguments[] {
                    arguments(obfuscator, obfuscator, true),
                    arguments(obfuscator, null, false),
                    arguments(obfuscator, fixedValue("obfuscated"), true),
                    arguments(obfuscator, fixedValue("other"), false),
                    arguments(obfuscator, "foo", false),
            };
        }

        @Test
        @DisplayName("hashCode()")
        void testHashCode() {
            Obfuscator obfuscator = fixedValue("obfuscated");
            assertEquals(obfuscator.hashCode(), obfuscator.hashCode());
            assertEquals(obfuscator.hashCode(), fixedValue("obfuscated").hashCode());
        }
    }

    @Nested
    @DisplayName("fixedValue(\"\")")
    @TestInstance(Lifecycle.PER_CLASS)
    class EmptyFixedValue extends NestedObfuscatorTest {

        EmptyFixedValue() {
            super(maskChar -> fixedValue(""), new Arguments[] {
                    arguments("foo", '*', ""),
                    arguments("hello", '*', ""),
            }, "", "");
        }
    }

    @Nested
    @DisplayName("portion()")
    class Portion {

        @Nested
        @DisplayName("keepAtStart(4)")
        @TestInstance(Lifecycle.PER_CLASS)
        class KeepAtStart extends NestedObfuscatorTest {

            KeepAtStart() {
                super(maskChar -> portion().keepAtStart(4).withMaskChar(maskChar).build(), new Arguments[] {
                        arguments("foo", '*', "foo"),
                        arguments("hello", '*', "hell*"),
                        arguments("foobar", '*', "foob**"),
                        arguments("hello", 'x', "hellx"),
                        arguments("foobar", 'x', "foobxx"),
                }, "", "null");
            }

            @ParameterizedTest(name = "{1}")
            @MethodSource
            @DisplayName("equals(Object)")
            void testEquals(Obfuscator obfuscator, Object object, boolean expected) {
                assertEquals(expected, obfuscator.equals(object));
            }

            Arguments[] testEquals() {
                Obfuscator obfuscator = portion().keepAtStart(4).withMaskChar('x').build();
                return new Arguments[] {
                        arguments(obfuscator, obfuscator, true),
                        arguments(obfuscator, null, false),
                        arguments(obfuscator, portion().keepAtStart(4).withMaskChar('x').build(), true),
                        arguments(obfuscator, portion().keepAtStart(3).withMaskChar('x').build(), false),
                        arguments(obfuscator, portion().keepAtStart(4).withMaskChar('*').build(), false),
                        arguments(obfuscator, "foo", false),
                };
            }

            @Test
            @DisplayName("hashCode()")
            void testHashCode() {
                Obfuscator obfuscator = portion().keepAtStart(4).withMaskChar('x').build();
                assertEquals(obfuscator.hashCode(), obfuscator.hashCode());
                assertEquals(obfuscator.hashCode(), portion().keepAtStart(4).withMaskChar('x').build().hashCode());
            }
        }

        @Nested
        @DisplayName("keepAtEnd(4)")
        @TestInstance(Lifecycle.PER_CLASS)
        class KeepAtEnd extends NestedObfuscatorTest {

            KeepAtEnd() {
                super(maskChar -> portion().keepAtEnd(4).withMaskChar(maskChar).build(), new Arguments[] {
                        arguments("foo", '*', "foo"),
                        arguments("hello", '*', "*ello"),
                        arguments("foobar", '*', "**obar"),
                        arguments("hello", 'x', "xello"),
                        arguments("foobar", 'x', "xxobar"),
                }, "", "null");
            }

            @ParameterizedTest(name = "{1}")
            @MethodSource
            @DisplayName("equals(Object)")
            void testEquals(Obfuscator obfuscator, Object object, boolean expected) {
                assertEquals(expected, obfuscator.equals(object));
            }

            Arguments[] testEquals() {
                Obfuscator obfuscator = portion().keepAtEnd(4).withMaskChar('x').build();
                return new Arguments[] {
                        arguments(obfuscator, obfuscator, true),
                        arguments(obfuscator, null, false),
                        arguments(obfuscator, portion().keepAtEnd(4).withMaskChar('x').build(), true),
                        arguments(obfuscator, portion().keepAtEnd(3).withMaskChar('x').build(), false),
                        arguments(obfuscator, portion().keepAtEnd(4).withMaskChar('*').build(), false),
                        arguments(obfuscator, "foo", false),
                };
            }

            @Test
            @DisplayName("hashCode()")
            void testHashCode() {
                Obfuscator obfuscator = portion().keepAtEnd(4).withMaskChar('x').build();
                assertEquals(obfuscator.hashCode(), obfuscator.hashCode());
                assertEquals(obfuscator.hashCode(), portion().keepAtEnd(4).withMaskChar('x').build().hashCode());
            }
        }

        @Nested
        @DisplayName("keepAtStart(4).keepAtEnd(4)")
        @TestInstance(Lifecycle.PER_CLASS)
        class KeepAtStartAndEnd extends NestedObfuscatorTest {

            KeepAtStartAndEnd() {
                super(maskChar -> portion().keepAtStart(4).keepAtEnd(4).withMaskChar(maskChar).build(), new Arguments[] {
                        arguments("foo", '*', "foo"),
                        arguments("hello", '*', "hello"),
                        arguments("hello world", '*', "hell***orld"),
                        arguments("foobar", '*', "foobar"),
                        arguments("hello", 'x', "hello"),
                        arguments("foobar", 'x', "foobar"),
                        arguments("hello world", 'x', "hellxxxorld"),
                }, "", "null");
            }

            @ParameterizedTest(name = "{1}")
            @MethodSource
            @DisplayName("equals(Object)")
            void testEquals(Obfuscator obfuscator, Object object, boolean expected) {
                assertEquals(expected, obfuscator.equals(object));
            }

            Arguments[] testEquals() {
                Obfuscator obfuscator = portion().keepAtStart(4).keepAtEnd(4).withMaskChar('x').build();
                return new Arguments[] {
                        arguments(obfuscator, obfuscator, true),
                        arguments(obfuscator, null, false),
                        arguments(obfuscator, portion().keepAtStart(4).keepAtEnd(4).withMaskChar('x').build(), true),
                        arguments(obfuscator, portion().keepAtStart(3).keepAtEnd(4).withMaskChar('x').build(), false),
                        arguments(obfuscator, portion().keepAtStart(4).keepAtEnd(3).withMaskChar('x').build(), false),
                        arguments(obfuscator, portion().keepAtStart(4).keepAtEnd(4).withMaskChar('*').build(), false),
                        arguments(obfuscator, "foo", false),
                };
            }

            @Test
            @DisplayName("hashCode()")
            void testHashCode() {
                Obfuscator obfuscator = portion().keepAtStart(4).keepAtEnd(4).withMaskChar('x').build();
                assertEquals(obfuscator.hashCode(), obfuscator.hashCode());
                assertEquals(obfuscator.hashCode(), portion().keepAtStart(4).keepAtEnd(4).withMaskChar('x').build().hashCode());
            }
        }

        @Nested
        @DisplayName("keepAtStart(4).atLeastFromEnd(4)")
        @TestInstance(Lifecycle.PER_CLASS)
        class KeepAtStartAtLeastFromEnd extends NestedObfuscatorTest {

            KeepAtStartAtLeastFromEnd() {
                super(maskChar -> portion().keepAtStart(4).atLeastFromEnd(4).withMaskChar(maskChar).build(), new Arguments[] {
                        arguments("foo", '*', "***"),
                        arguments("hello", '*', "h****"),
                        arguments("hello world", '*', "hell*******"),
                        arguments("foobar", '*', "fo****"),
                        arguments("foo", 'x', "xxx"),
                        arguments("hello", 'x', "hxxxx"),
                        arguments("foobar", 'x', "foxxxx"),
                        arguments("hello world", 'x', "hellxxxxxxx"),
                }, "", "****");
            }

            @ParameterizedTest(name = "{1}")
            @MethodSource
            @DisplayName("equals(Object)")
            void testEquals(Obfuscator obfuscator, Object object, boolean expected) {
                assertEquals(expected, obfuscator.equals(object));
            }

            Arguments[] testEquals() {
                Obfuscator obfuscator = portion().keepAtStart(4).atLeastFromEnd(4).withMaskChar('x').build();
                return new Arguments[] {
                        arguments(obfuscator, obfuscator, true),
                        arguments(obfuscator, null, false),
                        arguments(obfuscator, portion().keepAtStart(4).atLeastFromEnd(4).withMaskChar('x').build(), true),
                        arguments(obfuscator, portion().keepAtStart(4).atLeastFromEnd(3).withMaskChar('x').build(), false),
                        arguments(obfuscator, portion().keepAtStart(3).atLeastFromEnd(4).withMaskChar('x').build(), false),
                        arguments(obfuscator, portion().keepAtStart(4).atLeastFromEnd(4).withMaskChar('*').build(), false),
                        arguments(obfuscator, "foo", false),
                };
            }

            @Test
            @DisplayName("hashCode()")
            void testHashCode() {
                Obfuscator obfuscator = portion().keepAtStart(4).atLeastFromEnd(4).withMaskChar('x').build();
                assertEquals(obfuscator.hashCode(), obfuscator.hashCode());
                assertEquals(obfuscator.hashCode(), portion().keepAtStart(4).atLeastFromEnd(4).withMaskChar('x').build().hashCode());
            }
        }

        @Nested
        @DisplayName("keepAtEnd(4).atLeastFromStart(4)")
        @TestInstance(Lifecycle.PER_CLASS)
        class KeepAtEndAtLeastFromStart extends NestedObfuscatorTest {

            KeepAtEndAtLeastFromStart() {
                super(maskChar -> portion().keepAtEnd(4).atLeastFromStart(4).withMaskChar(maskChar).build(), new Arguments[] {
                        arguments("foo", '*', "***"),
                        arguments("hello", '*', "****o"),
                        arguments("hello world", '*', "*******orld"),
                        arguments("foobar", '*', "****ar"),
                        arguments("foo", 'x', "xxx"),
                        arguments("hello", 'x', "xxxxo"),
                        arguments("foobar", 'x', "xxxxar"),
                        arguments("hello world", 'x', "xxxxxxxorld"),
                }, "", "****");
            }

            @ParameterizedTest(name = "{1}")
            @MethodSource
            @DisplayName("equals(Object)")
            void testEquals(Obfuscator obfuscator, Object object, boolean expected) {
                assertEquals(expected, obfuscator.equals(object));
            }

            Arguments[] testEquals() {
                Obfuscator obfuscator = portion().keepAtEnd(4).atLeastFromStart(4).withMaskChar('x').build();
                return new Arguments[] {
                        arguments(obfuscator, obfuscator, true),
                        arguments(obfuscator, null, false),
                        arguments(obfuscator, portion().keepAtEnd(4).atLeastFromStart(4).withMaskChar('x').build(), true),
                        arguments(obfuscator, portion().keepAtEnd(4).atLeastFromStart(3).withMaskChar('x').build(), false),
                        arguments(obfuscator, portion().keepAtEnd(3).atLeastFromStart(4).withMaskChar('x').build(), false),
                        arguments(obfuscator, portion().keepAtEnd(4).atLeastFromStart(4).withMaskChar('*').build(), false),
                        arguments(obfuscator, "foo", false),
                };
            }

            @Test
            @DisplayName("hashCode()")
            void testHashCode() {
                Obfuscator obfuscator = portion().keepAtEnd(4).atLeastFromStart(4).withMaskChar('x').build();
                assertEquals(obfuscator.hashCode(), obfuscator.hashCode());
                assertEquals(obfuscator.hashCode(), portion().keepAtEnd(4).atLeastFromStart(4).withMaskChar('x').build().hashCode());
            }
        }

        @Nested
        @DisplayName("keepAtStart(4).withFixedTotalLength(9)")
        @TestInstance(Lifecycle.PER_CLASS)
        class KeepAtStartWithFixedTotalLength extends NestedObfuscatorTest {

            KeepAtStartWithFixedTotalLength() {
                super(maskChar -> portion().keepAtStart(4).withFixedTotalLength(9).withMaskChar(maskChar).build(), new Arguments[] {
                        arguments("foo", '*', "foo******"),
                        arguments("hello", '*', "hell*****"),
                        arguments("foobar", '*', "foob*****"),
                        arguments("hello", 'x', "hellxxxxx"),
                        arguments("foobar", 'x', "foobxxxxx"),
                }, "*********", "null*****");
            }

            @ParameterizedTest(name = "{1}")
            @MethodSource
            @DisplayName("equals(Object)")
            void testEquals(Obfuscator obfuscator, Object object, boolean expected) {
                assertEquals(expected, obfuscator.equals(object));
            }

            Arguments[] testEquals() {
                Obfuscator obfuscator = portion().keepAtStart(4).withFixedTotalLength(9).withMaskChar('x').build();
                return new Arguments[] {
                        arguments(obfuscator, obfuscator, true),
                        arguments(obfuscator, null, false),
                        arguments(obfuscator, portion().keepAtStart(4).withFixedTotalLength(9).withMaskChar('x').build(), true),
                        arguments(obfuscator, portion().keepAtStart(3).withFixedTotalLength(9).withMaskChar('x').build(), false),
                        arguments(obfuscator, portion().keepAtStart(4).withFixedTotalLength(10).withMaskChar('x').build(), false),
                        arguments(obfuscator, portion().keepAtStart(4).withFixedTotalLength(9).withMaskChar('*').build(), false),
                        arguments(obfuscator, "foo", false),
                };
            }

            @Test
            @DisplayName("hashCode()")
            void testHashCode() {
                Obfuscator obfuscator = portion().keepAtStart(4).withFixedTotalLength(9).withMaskChar('x').build();
                assertEquals(obfuscator.hashCode(), obfuscator.hashCode());
                assertEquals(obfuscator.hashCode(), portion().keepAtStart(4).withFixedTotalLength(9).withMaskChar('x').build().hashCode());
            }
        }

        @Nested
        @DisplayName("keepAtEnd(4).withFixedTotalLength(9)")
        @TestInstance(Lifecycle.PER_CLASS)
        class KeepAtEndWithFixedTotalLength extends NestedObfuscatorTest {

            KeepAtEndWithFixedTotalLength() {
                super(maskChar -> portion().keepAtEnd(4).withFixedTotalLength(9).withMaskChar(maskChar).build(), new Arguments[] {
                        arguments("foo", '*', "******foo"),
                        arguments("hello", '*', "*****ello"),
                        arguments("foobar", '*', "*****obar"),
                        arguments("hello", 'x', "xxxxxello"),
                        arguments("foobar", 'x', "xxxxxobar"),
                }, "*********", "*****null");
            }

            @ParameterizedTest(name = "{1}")
            @MethodSource
            @DisplayName("equals(Object)")
            void testEquals(Obfuscator obfuscator, Object object, boolean expected) {
                assertEquals(expected, obfuscator.equals(object));
            }

            Arguments[] testEquals() {
                Obfuscator obfuscator = portion().keepAtEnd(4).withFixedTotalLength(9).withMaskChar('x').build();
                return new Arguments[] {
                        arguments(obfuscator, obfuscator, true),
                        arguments(obfuscator, null, false),
                        arguments(obfuscator, portion().keepAtEnd(4).withFixedTotalLength(9).withMaskChar('x').build(), true),
                        arguments(obfuscator, portion().keepAtEnd(3).withFixedTotalLength(9).withMaskChar('x').build(), false),
                        arguments(obfuscator, portion().keepAtEnd(4).withFixedTotalLength(10).withMaskChar('x').build(), false),
                        arguments(obfuscator, portion().keepAtEnd(4).withFixedTotalLength(9).withMaskChar('*').build(), false),
                        arguments(obfuscator, "foo", false),
                };
            }

            @Test
            @DisplayName("hashCode()")
            void testHashCode() {
                Obfuscator obfuscator = portion().keepAtEnd(4).withFixedTotalLength(9).withMaskChar('x').build();
                assertEquals(obfuscator.hashCode(), obfuscator.hashCode());
                assertEquals(obfuscator.hashCode(), portion().keepAtEnd(4).withFixedTotalLength(9).withMaskChar('x').build().hashCode());
            }
        }

        @Nested
        @DisplayName("keepAtStart(4).keepAtEnd(4).withFixedTotalLength(9)")
        @TestInstance(Lifecycle.PER_CLASS)
        class KeepAtStartAndEndWithFixedTotalLength extends NestedObfuscatorTest {

            KeepAtStartAndEndWithFixedTotalLength() {
                super(maskChar -> portion().keepAtStart(4).keepAtEnd(4).withFixedTotalLength(9).withMaskChar(maskChar).build(), new Arguments[] {
                        arguments("foo", '*', "foo***foo"),
                        arguments("hello", '*', "hell*ello"),
                        arguments("hello world", '*', "hell*orld"),
                        arguments("foobar", '*', "foob*obar"),
                        arguments("hello", 'x', "hellxello"),
                        arguments("foobar", 'x', "foobxobar"),
                        arguments("hello world", 'x', "hellxorld"),
                }, "*********", "null*null");
            }

            @ParameterizedTest(name = "{1}")
            @MethodSource
            @DisplayName("equals(Object)")
            void testEquals(Obfuscator obfuscator, Object object, boolean expected) {
                assertEquals(expected, obfuscator.equals(object));
            }

            Arguments[] testEquals() {
                Obfuscator obfuscator = portion().keepAtStart(4).keepAtEnd(4).withFixedTotalLength(9).withMaskChar('x').build();
                return new Arguments[] {
                        arguments(obfuscator, obfuscator, true),
                        arguments(obfuscator, null, false),
                        arguments(obfuscator, portion().keepAtStart(4).keepAtEnd(4).withFixedTotalLength(9).withMaskChar('x').build(), true),
                        arguments(obfuscator, portion().keepAtStart(3).keepAtEnd(4).withFixedTotalLength(9).withMaskChar('x').build(), false),
                        arguments(obfuscator, portion().keepAtStart(4).keepAtEnd(3).withFixedTotalLength(9).withMaskChar('x').build(), false),
                        arguments(obfuscator, portion().keepAtStart(4).keepAtEnd(4).withFixedTotalLength(10).withMaskChar('x').build(), false),
                        arguments(obfuscator, portion().keepAtStart(4).keepAtEnd(4).withFixedTotalLength(9).withMaskChar('*').build(), false),
                        arguments(obfuscator, "foo", false),
                };
            }

            @Test
            @DisplayName("hashCode()")
            void testHashCode() {
                Obfuscator obfuscator = portion().keepAtStart(4).keepAtEnd(4).withFixedTotalLength(9).withMaskChar('x').build();
                assertEquals(obfuscator.hashCode(), obfuscator.hashCode());
                assertEquals(obfuscator.hashCode(),
                        portion().keepAtStart(4).keepAtEnd(4).withFixedTotalLength(9).withMaskChar('x').build().hashCode());
            }
        }

        @Nested
        @DisplayName("keepAtStart(4).atLeastFromEnd(4).withFixedTotalLength(9)")
        @TestInstance(Lifecycle.PER_CLASS)
        class KeepAtStartAtLeastFromEndWithFixedTotalLength extends NestedObfuscatorTest {

            KeepAtStartAtLeastFromEndWithFixedTotalLength() {
                super(maskChar -> portion().keepAtStart(4).atLeastFromEnd(4).withFixedTotalLength(9).withMaskChar(maskChar).build(), new Arguments[] {
                        arguments("foo", '*', "*********"),
                        arguments("hello", '*', "h********"),
                        arguments("hello world", '*', "hell*****"),
                        arguments("foobar", '*', "fo*******"),
                        arguments("foo", 'x', "xxxxxxxxx"),
                        arguments("hello", 'x', "hxxxxxxxx"),
                        arguments("foobar", 'x', "foxxxxxxx"),
                        arguments("hello world", 'x', "hellxxxxx"),
                }, "*********", "*********");
            }

            @ParameterizedTest(name = "{1}")
            @MethodSource
            @DisplayName("equals(Object)")
            void testEquals(Obfuscator obfuscator, Object object, boolean expected) {
                assertEquals(expected, obfuscator.equals(object));
            }

            Arguments[] testEquals() {
                Obfuscator obfuscator = portion().keepAtStart(4).atLeastFromEnd(4).withFixedTotalLength(9).withMaskChar('x').build();
                return new Arguments[] {
                        arguments(obfuscator, obfuscator, true),
                        arguments(obfuscator, null, false),
                        arguments(obfuscator, portion().keepAtStart(4).atLeastFromEnd(4).withFixedTotalLength(9).withMaskChar('x').build(), true),
                        arguments(obfuscator, portion().keepAtStart(4).atLeastFromEnd(3).withFixedTotalLength(9).withMaskChar('x').build(), false),
                        arguments(obfuscator, portion().keepAtStart(3).atLeastFromEnd(4).withFixedTotalLength(9).withMaskChar('x').build(), false),
                        arguments(obfuscator, portion().keepAtStart(4).atLeastFromEnd(4).withFixedTotalLength(10).withMaskChar('x').build(), false),
                        arguments(obfuscator, portion().keepAtStart(4).atLeastFromEnd(4).withFixedTotalLength(9).withMaskChar('*').build(), false),
                        arguments(obfuscator, "foo", false),
                };
            }

            @Test
            @DisplayName("hashCode()")
            void testHashCode() {
                Obfuscator obfuscator = portion().keepAtStart(4).atLeastFromEnd(4).withFixedTotalLength(9).withMaskChar('x').build();
                assertEquals(obfuscator.hashCode(), obfuscator.hashCode());
                assertEquals(obfuscator.hashCode(),
                        portion().keepAtStart(4).atLeastFromEnd(4).withFixedTotalLength(9).withMaskChar('x').build().hashCode());
            }
        }

        @Nested
        @DisplayName("keepAtEnd(4).atLeastFromStart(4).withFixedTotalLength(9)")
        @TestInstance(Lifecycle.PER_CLASS)
        class KeepAtEndAtLeastFromStartWithFixedTotalLength extends NestedObfuscatorTest {

            KeepAtEndAtLeastFromStartWithFixedTotalLength() {
                super(maskChar -> portion().keepAtEnd(4).atLeastFromStart(4).withFixedTotalLength(9).withMaskChar(maskChar).build(), new Arguments[] {
                        arguments("foo", '*', "*********"),
                        arguments("hello", '*', "********o"),
                        arguments("hello world", '*', "*****orld"),
                        arguments("foobar", '*', "*******ar"),
                        arguments("foo", 'x', "xxxxxxxxx"),
                        arguments("hello", 'x', "xxxxxxxxo"),
                        arguments("foobar", 'x', "xxxxxxxar"),
                        arguments("hello world", 'x', "xxxxxorld"),
                }, "*********", "*********");
            }

            @ParameterizedTest(name = "{1}")
            @MethodSource
            @DisplayName("equals(Object)")
            void testEquals(Obfuscator obfuscator, Object object, boolean expected) {
                assertEquals(expected, obfuscator.equals(object));
            }

            Arguments[] testEquals() {
                Obfuscator obfuscator = portion().keepAtEnd(4).atLeastFromStart(4).withFixedTotalLength(9).withMaskChar('x').build();
                return new Arguments[] {
                        arguments(obfuscator, obfuscator, true),
                        arguments(obfuscator, null, false),
                        arguments(obfuscator, portion().keepAtEnd(4).atLeastFromStart(4).withFixedTotalLength(9).withMaskChar('x').build(), true),
                        arguments(obfuscator, portion().keepAtEnd(4).atLeastFromStart(3).withFixedTotalLength(9).withMaskChar('x').build(), false),
                        arguments(obfuscator, portion().keepAtEnd(3).atLeastFromStart(4).withFixedTotalLength(9).withMaskChar('x').build(), false),
                        arguments(obfuscator, portion().keepAtEnd(4).atLeastFromStart(4).withFixedTotalLength(10).withMaskChar('x').build(), false),
                        arguments(obfuscator, portion().keepAtEnd(4).atLeastFromStart(4).withFixedTotalLength(9).withMaskChar('*').build(), false),
                        arguments(obfuscator, "foo", false),
                };
            }

            @Test
            @DisplayName("hashCode()")
            void testHashCode() {
                Obfuscator obfuscator = portion().keepAtEnd(4).atLeastFromStart(4).withFixedTotalLength(9).withMaskChar('x').build();
                assertEquals(obfuscator.hashCode(), obfuscator.hashCode());
                assertEquals(obfuscator.hashCode(),
                        portion().keepAtEnd(4).atLeastFromStart(4).withFixedTotalLength(9).withMaskChar('x').build().hashCode());
            }
        }

        @Nested
        @DisplayName("keepAtStart(4).withFixedTotalLength(4)")
        @TestInstance(Lifecycle.PER_CLASS)
        class KeepAtStartWithSameFixedTotalLength extends NestedObfuscatorTest {

            KeepAtStartWithSameFixedTotalLength() {
                super(maskChar -> portion().keepAtStart(4).withFixedTotalLength(4).withMaskChar(maskChar).build(), new Arguments[] {
                        arguments("foo", '*', "foo*"),
                        arguments("hello", '*', "hell"),
                        arguments("foobar", '*', "foob"),
                        arguments("hello", 'x', "hell"),
                        arguments("foobar", 'x', "foob"),
                }, "****", "null");
            }
        }

        @Nested
        @DisplayName("keepAtEnd(4).withFixedTotalLength(4)")
        @TestInstance(Lifecycle.PER_CLASS)
        class KeepAtEndWithSameFixedTotalLength extends NestedObfuscatorTest {

            KeepAtEndWithSameFixedTotalLength() {
                super(maskChar -> portion().keepAtEnd(4).withFixedTotalLength(4).withMaskChar(maskChar).build(), new Arguments[] {
                        arguments("foo", '*', "*foo"),
                        arguments("hello", '*', "ello"),
                        arguments("foobar", '*', "obar"),
                        arguments("hello", 'x', "ello"),
                        arguments("foobar", 'x', "obar"),
                }, "****", "null");
            }
        }

        @Nested
        @DisplayName("keepAtStart(4).keepAtEnd(4).withFixedTotalLength(8)")
        @TestInstance(Lifecycle.PER_CLASS)
        class KeepAtStartAndEndWithSameFixedTotalLength extends NestedObfuscatorTest {

            KeepAtStartAndEndWithSameFixedTotalLength() {
                super(maskChar -> portion().keepAtStart(4).keepAtEnd(4).withFixedTotalLength(8).withMaskChar(maskChar).build(), new Arguments[] {
                        arguments("foo", '*', "foo**foo"),
                        arguments("hello", '*', "hellello"),
                        arguments("hello world", '*', "hellorld"),
                        arguments("foobar", '*', "foobobar"),
                        arguments("hello", 'x', "hellello"),
                        arguments("foobar", 'x', "foobobar"),
                        arguments("hello world", 'x', "hellorld"),
                }, "********", "nullnull");
            }
        }

        @Nested
        @DisplayName("keepAtStart(4).atLeastFromEnd(4).withFixedTotalLength(4)")
        @TestInstance(Lifecycle.PER_CLASS)
        class KeepAtStartAtLeastFromEndWithSameFixedTotalLength extends NestedObfuscatorTest {

            KeepAtStartAtLeastFromEndWithSameFixedTotalLength() {
                super(maskChar -> portion().keepAtStart(4).atLeastFromEnd(4).withFixedTotalLength(4).withMaskChar(maskChar).build(), new Arguments[] {
                        arguments("foo", '*', "****"),
                        arguments("hello", '*', "h***"),
                        arguments("hello world", '*', "hell"),
                        arguments("foobar", '*', "fo**"),
                        arguments("foo", 'x', "xxxx"),
                        arguments("hello", 'x', "hxxx"),
                        arguments("foobar", 'x', "foxx"),
                        arguments("hello world", 'x', "hell"),
                }, "****", "****");
            }
        }

        @Nested
        @DisplayName("keepAtEnd(4).atLeastFromStart(4).withFixedTotalLength(4)")
        @TestInstance(Lifecycle.PER_CLASS)
        class KeepAtEndAtLeastFromStartWithSameFixedTotalLength extends NestedObfuscatorTest {

            KeepAtEndAtLeastFromStartWithSameFixedTotalLength() {
                super(maskChar -> portion().keepAtEnd(4).atLeastFromStart(4).withFixedTotalLength(4).withMaskChar(maskChar).build(), new Arguments[] {
                        arguments("foo", '*', "****"),
                        arguments("hello", '*', "***o"),
                        arguments("hello world", '*', "orld"),
                        arguments("foobar", '*', "**ar"),
                        arguments("foo", 'x', "xxxx"),
                        arguments("hello", 'x', "xxxo"),
                        arguments("foobar", 'x', "xxar"),
                        arguments("hello world", 'x', "orld"),
                }, "****", "****");
            }
        }

        @Nested
        @DisplayName("keepAtStart(4).withFixedLength(3)")
        @TestInstance(Lifecycle.PER_CLASS)
        @SuppressWarnings("deprecation")
        class KeepAtStartWithFixedLength extends NestedObfuscatorTest {

            KeepAtStartWithFixedLength() {
                super(maskChar -> portion().keepAtStart(4).withFixedLength(3).withMaskChar(maskChar).build(), new Arguments[] {
                        arguments("foo", '*', "foo***"),
                        arguments("hello", '*', "hell***"),
                        arguments("foobar", '*', "foob***"),
                        arguments("hello", 'x', "hellxxx"),
                        arguments("foobar", 'x', "foobxxx"),
                }, "***", "null***");
            }

            @ParameterizedTest(name = "{1}")
            @MethodSource
            @DisplayName("equals(Object)")
            void testEquals(Obfuscator obfuscator, Object object, boolean expected) {
                assertEquals(expected, obfuscator.equals(object));
            }

            Arguments[] testEquals() {
                Obfuscator obfuscator = portion().keepAtStart(4).withFixedLength(3).withMaskChar('x').build();
                return new Arguments[] {
                        arguments(obfuscator, obfuscator, true),
                        arguments(obfuscator, null, false),
                        arguments(obfuscator, portion().keepAtStart(4).withFixedLength(3).withMaskChar('x').build(), true),
                        arguments(obfuscator, portion().keepAtStart(3).withFixedLength(3).withMaskChar('x').build(), false),
                        arguments(obfuscator, portion().keepAtStart(4).withFixedLength(4).withMaskChar('x').build(), false),
                        arguments(obfuscator, portion().keepAtStart(4).withFixedLength(3).withMaskChar('*').build(), false),
                        arguments(obfuscator, "foo", false),
                };
            }

            @Test
            @DisplayName("hashCode()")
            void testHashCode() {
                Obfuscator obfuscator = portion().keepAtStart(4).withFixedLength(3).withMaskChar('x').build();
                assertEquals(obfuscator.hashCode(), obfuscator.hashCode());
                assertEquals(obfuscator.hashCode(), portion().keepAtStart(4).withFixedLength(3).withMaskChar('x').build().hashCode());
            }
        }

        @Nested
        @DisplayName("keepAtEnd(4).withFixedLength(3)")
        @TestInstance(Lifecycle.PER_CLASS)
        @SuppressWarnings("deprecation")
        class KeepAtEndWithFixedLength extends NestedObfuscatorTest {

            KeepAtEndWithFixedLength() {
                super(maskChar -> portion().keepAtEnd(4).withFixedLength(3).withMaskChar(maskChar).build(), new Arguments[] {
                        arguments("foo", '*', "***foo"),
                        arguments("hello", '*', "***ello"),
                        arguments("foobar", '*', "***obar"),
                        arguments("hello", 'x', "xxxello"),
                        arguments("foobar", 'x', "xxxobar"),
                }, "***", "***null");
            }

            @ParameterizedTest(name = "{1}")
            @MethodSource
            @DisplayName("equals(Object)")
            void testEquals(Obfuscator obfuscator, Object object, boolean expected) {
                assertEquals(expected, obfuscator.equals(object));
            }

            Arguments[] testEquals() {
                Obfuscator obfuscator = portion().keepAtEnd(4).withFixedLength(3).withMaskChar('x').build();
                return new Arguments[] {
                        arguments(obfuscator, obfuscator, true),
                        arguments(obfuscator, null, false),
                        arguments(obfuscator, portion().keepAtEnd(4).withFixedLength(3).withMaskChar('x').build(), true),
                        arguments(obfuscator, portion().keepAtEnd(3).withFixedLength(3).withMaskChar('x').build(), false),
                        arguments(obfuscator, portion().keepAtEnd(4).withFixedLength(4).withMaskChar('x').build(), false),
                        arguments(obfuscator, portion().keepAtEnd(4).withFixedLength(3).withMaskChar('*').build(), false),
                        arguments(obfuscator, "foo", false),
                };
            }

            @Test
            @DisplayName("hashCode()")
            void testHashCode() {
                Obfuscator obfuscator = portion().keepAtEnd(4).withFixedLength(3).withMaskChar('x').build();
                assertEquals(obfuscator.hashCode(), obfuscator.hashCode());
                assertEquals(obfuscator.hashCode(), portion().keepAtEnd(4).withFixedLength(3).withMaskChar('x').build().hashCode());
            }
        }

        @Nested
        @DisplayName("keepAtStart(4).keepAtEnd(4).withFixedLength(3)")
        @TestInstance(Lifecycle.PER_CLASS)
        @SuppressWarnings("deprecation")
        class KeepAtStartAndEndWithFixedLength extends NestedObfuscatorTest {

            KeepAtStartAndEndWithFixedLength() {
                super(maskChar -> portion().keepAtStart(4).keepAtEnd(4).withFixedLength(3).withMaskChar(maskChar).build(), new Arguments[] {
                        arguments("foo", '*', "foo***"),
                        arguments("hello", '*', "hell***o"),
                        arguments("hello world", '*', "hell***orld"),
                        arguments("foobar", '*', "foob***ar"),
                        arguments("hello", 'x', "hellxxxo"),
                        arguments("foobar", 'x', "foobxxxar"),
                        arguments("hello world", 'x', "hellxxxorld"),
                }, "***", "null***");
            }

            @ParameterizedTest(name = "{1}")
            @MethodSource
            @DisplayName("equals(Object)")
            void testEquals(Obfuscator obfuscator, Object object, boolean expected) {
                assertEquals(expected, obfuscator.equals(object));
            }

            Arguments[] testEquals() {
                Obfuscator obfuscator = portion().keepAtStart(4).keepAtEnd(4).withFixedLength(3).withMaskChar('x').build();
                return new Arguments[] {
                        arguments(obfuscator, obfuscator, true),
                        arguments(obfuscator, null, false),
                        arguments(obfuscator, portion().keepAtStart(4).keepAtEnd(4).withFixedLength(3).withMaskChar('x').build(), true),
                        arguments(obfuscator, portion().keepAtStart(3).keepAtEnd(4).withFixedLength(3).withMaskChar('x').build(), false),
                        arguments(obfuscator, portion().keepAtStart(4).keepAtEnd(3).withFixedLength(3).withMaskChar('x').build(), false),
                        arguments(obfuscator, portion().keepAtStart(4).keepAtEnd(4).withFixedLength(4).withMaskChar('x').build(), false),
                        arguments(obfuscator, portion().keepAtStart(4).keepAtEnd(4).withFixedLength(3).withMaskChar('*').build(), false),
                        arguments(obfuscator, "foo", false),
                };
            }

            @Test
            @DisplayName("hashCode()")
            void testHashCode() {
                Obfuscator obfuscator = portion().keepAtStart(4).keepAtEnd(4).withFixedLength(3).withMaskChar('x').build();
                assertEquals(obfuscator.hashCode(), obfuscator.hashCode());
                assertEquals(obfuscator.hashCode(), portion().keepAtStart(4).keepAtEnd(4).withFixedLength(3).withMaskChar('x').build().hashCode());
            }
        }

        @Nested
        @DisplayName("keepAtStart(4).atLeastFromEnd(4).withFixedLength(3)")
        @TestInstance(Lifecycle.PER_CLASS)
        @SuppressWarnings("deprecation")
        class KeepAtStartAtLeastFromEndWithFixedLength extends NestedObfuscatorTest {

            KeepAtStartAtLeastFromEndWithFixedLength() {
                super(maskChar -> portion().keepAtStart(4).atLeastFromEnd(4).withFixedLength(3).withMaskChar(maskChar).build(), new Arguments[] {
                        arguments("foo", '*', "***"),
                        arguments("hello", '*', "h***"),
                        arguments("hello world", '*', "hell***"),
                        arguments("foobar", '*', "fo***"),
                        arguments("foo", 'x', "xxx"),
                        arguments("hello", 'x', "hxxx"),
                        arguments("foobar", 'x', "foxxx"),
                        arguments("hello world", 'x', "hellxxx"),
                }, "***", "***");
            }

            @ParameterizedTest(name = "{1}")
            @MethodSource
            @DisplayName("equals(Object)")
            void testEquals(Obfuscator obfuscator, Object object, boolean expected) {
                assertEquals(expected, obfuscator.equals(object));
            }

            Arguments[] testEquals() {
                Obfuscator obfuscator = portion().keepAtStart(4).atLeastFromEnd(4).withFixedLength(3).withMaskChar('x').build();
                return new Arguments[] {
                        arguments(obfuscator, obfuscator, true),
                        arguments(obfuscator, null, false),
                        arguments(obfuscator, portion().keepAtStart(4).atLeastFromEnd(4).withFixedLength(3).withMaskChar('x').build(), true),
                        arguments(obfuscator, portion().keepAtStart(4).atLeastFromEnd(3).withFixedLength(3).withMaskChar('x').build(), false),
                        arguments(obfuscator, portion().keepAtStart(3).atLeastFromEnd(4).withFixedLength(3).withMaskChar('x').build(), false),
                        arguments(obfuscator, portion().keepAtStart(4).atLeastFromEnd(4).withFixedLength(4).withMaskChar('x').build(), false),
                        arguments(obfuscator, portion().keepAtStart(4).atLeastFromEnd(4).withFixedLength(3).withMaskChar('*').build(), false),
                        arguments(obfuscator, "foo", false),
                };
            }

            @Test
            @DisplayName("hashCode()")
            void testHashCode() {
                Obfuscator obfuscator = portion().keepAtStart(4).atLeastFromEnd(4).withFixedLength(3).withMaskChar('x').build();
                assertEquals(obfuscator.hashCode(), obfuscator.hashCode());
                assertEquals(obfuscator.hashCode(),
                        portion().keepAtStart(4).atLeastFromEnd(4).withFixedLength(3).withMaskChar('x').build().hashCode());
            }
        }

        @Nested
        @DisplayName("keepAtEnd(4).atLeastFromStart(4).withFixedLength(3)")
        @TestInstance(Lifecycle.PER_CLASS)
        @SuppressWarnings("deprecation")
        class KeepAtEndAtLeastFromStartWithFixedLength extends NestedObfuscatorTest {

            KeepAtEndAtLeastFromStartWithFixedLength() {
                super(maskChar -> portion().keepAtEnd(4).atLeastFromStart(4).withFixedLength(3).withMaskChar(maskChar).build(), new Arguments[] {
                        arguments("foo", '*', "***"),
                        arguments("hello", '*', "***o"),
                        arguments("hello world", '*', "***orld"),
                        arguments("foobar", '*', "***ar"),
                        arguments("foo", 'x', "xxx"),
                        arguments("hello", 'x', "xxxo"),
                        arguments("foobar", 'x', "xxxar"),
                        arguments("hello world", 'x', "xxxorld"),
                }, "***", "***");
            }

            @ParameterizedTest(name = "{1}")
            @MethodSource
            @DisplayName("equals(Object)")
            void testEquals(Obfuscator obfuscator, Object object, boolean expected) {
                assertEquals(expected, obfuscator.equals(object));
            }

            Arguments[] testEquals() {
                Obfuscator obfuscator = portion().keepAtEnd(4).atLeastFromStart(4).withFixedLength(3).withMaskChar('x').build();
                return new Arguments[] {
                        arguments(obfuscator, obfuscator, true),
                        arguments(obfuscator, null, false),
                        arguments(obfuscator, portion().keepAtEnd(4).atLeastFromStart(4).withFixedLength(3).withMaskChar('x').build(), true),
                        arguments(obfuscator, portion().keepAtEnd(4).atLeastFromStart(3).withFixedLength(3).withMaskChar('x').build(), false),
                        arguments(obfuscator, portion().keepAtEnd(3).atLeastFromStart(4).withFixedLength(3).withMaskChar('x').build(), false),
                        arguments(obfuscator, portion().keepAtEnd(4).atLeastFromStart(4).withFixedLength(4).withMaskChar('x').build(), false),
                        arguments(obfuscator, portion().keepAtEnd(4).atLeastFromStart(4).withFixedLength(3).withMaskChar('*').build(), false),
                        arguments(obfuscator, "foo", false),
                };
            }

            @Test
            @DisplayName("hashCode()")
            void testHashCode() {
                Obfuscator obfuscator = portion().keepAtEnd(4).atLeastFromStart(4).withFixedLength(3).withMaskChar('x').build();
                assertEquals(obfuscator.hashCode(), obfuscator.hashCode());
                assertEquals(obfuscator.hashCode(),
                        portion().keepAtEnd(4).atLeastFromStart(4).withFixedLength(3).withMaskChar('x').build().hashCode());
            }
        }

        @Nested
        @DisplayName("keepAtStart(4).withFixedLength(0)")
        @TestInstance(Lifecycle.PER_CLASS)
        @SuppressWarnings("deprecation")
        class KeepAtStartWithZeroFixedLength extends NestedObfuscatorTest {

            KeepAtStartWithZeroFixedLength() {
                super(maskChar -> portion().keepAtStart(4).withFixedLength(0).withMaskChar(maskChar).build(), new Arguments[] {
                        arguments("foo", '*', "foo"),
                        arguments("hello", '*', "hell"),
                        arguments("foobar", '*', "foob"),
                        arguments("hello", 'x', "hell"),
                        arguments("foobar", 'x', "foob"),
                }, "", "null");
            }
        }

        @Nested
        @DisplayName("keepAtEnd(4).withFixedLength(0)")
        @TestInstance(Lifecycle.PER_CLASS)
        @SuppressWarnings("deprecation")
        class KeepAtEndWithZeroFixedLength extends NestedObfuscatorTest {

            KeepAtEndWithZeroFixedLength() {
                super(maskChar -> portion().keepAtEnd(4).withFixedLength(0).withMaskChar(maskChar).build(), new Arguments[] {
                        arguments("foo", '*', "foo"),
                        arguments("hello", '*', "ello"),
                        arguments("foobar", '*', "obar"),
                        arguments("hello", 'x', "ello"),
                        arguments("foobar", 'x', "obar"),
                }, "", "null");
            }
        }

        @Nested
        @DisplayName("keepAtStart(4).keepAtEnd(4).withFixedLength(0)")
        @TestInstance(Lifecycle.PER_CLASS)
        @SuppressWarnings("deprecation")
        class KeepAtStartAndEndWithZeroFixedLength extends NestedObfuscatorTest {

            KeepAtStartAndEndWithZeroFixedLength() {
                super(maskChar -> portion().keepAtStart(4).keepAtEnd(4).withFixedLength(0).withMaskChar(maskChar).build(), new Arguments[] {
                        arguments("foo", '*', "foo"),
                        arguments("hello", '*', "hello"),
                        arguments("hello world", '*', "hellorld"),
                        arguments("foobar", '*', "foobar"),
                        arguments("hello", 'x', "hello"),
                        arguments("foobar", 'x', "foobar"),
                        arguments("hello world", 'x', "hellorld"),
                }, "", "null");
            }
        }

        @Nested
        @DisplayName("keepAtStart(4).atLeastFromEnd(4).withFixedLength(0)")
        @TestInstance(Lifecycle.PER_CLASS)
        @SuppressWarnings("deprecation")
        class KeepAtStartAtLeastFromEndWithZeroFixedLength extends NestedObfuscatorTest {

            KeepAtStartAtLeastFromEndWithZeroFixedLength() {
                super(maskChar -> portion().keepAtStart(4).atLeastFromEnd(4).withFixedLength(0).withMaskChar(maskChar).build(), new Arguments[] {
                        arguments("foo", '*', ""),
                        arguments("hello", '*', "h"),
                        arguments("hello world", '*', "hell"),
                        arguments("foobar", '*', "fo"),
                        arguments("foo", 'x', ""),
                        arguments("hello", 'x', "h"),
                        arguments("foobar", 'x', "fo"),
                        arguments("hello world", 'x', "hell"),
                }, "", "");
            }
        }

        @Nested
        @DisplayName("keepAtEnd(4).atLeastFromStart(4).withFixedLength(0)")
        @TestInstance(Lifecycle.PER_CLASS)
        @SuppressWarnings("deprecation")
        class KeepAtEndAtLeastFromStartWithZeroFixedLength extends NestedObfuscatorTest {

            KeepAtEndAtLeastFromStartWithZeroFixedLength() {
                super(maskChar -> portion().keepAtEnd(4).atLeastFromStart(4).withFixedLength(0).withMaskChar(maskChar).build(), new Arguments[] {
                        arguments("foo", '*', ""),
                        arguments("hello", '*', "o"),
                        arguments("hello world", '*', "orld"),
                        arguments("foobar", '*', "ar"),
                        arguments("foo", 'x', ""),
                        arguments("hello", 'x', "o"),
                        arguments("foobar", 'x', "ar"),
                        arguments("hello world", 'x', "orld"),
                }, "", "");
            }
        }

        @Nested
        @DisplayName("obfuscate last 2 chars")
        @TestInstance(Lifecycle.PER_CLASS)
        class LastTwoChars extends NestedObfuscatorTest {

            LastTwoChars() {
                super(maskChar -> portion().keepAtStart(Integer.MAX_VALUE).atLeastFromEnd(2).withMaskChar(maskChar).build(), new Arguments[] {
                        arguments("foo", '*', "f**"),
                        arguments("hello", '*', "hel**"),
                        arguments("hello world", '*', "hello wor**"),
                        arguments("foobar", '*', "foob**"),
                        arguments("foo", 'x', "fxx"),
                        arguments("hello", 'x', "helxx"),
                        arguments("foobar", 'x', "foobxx"),
                        arguments("hello world", 'x', "hello worxx"),
                }, "", "nu**");
            }
        }

        @Nested
        @DisplayName("obfuscate first 2 chars")
        @TestInstance(Lifecycle.PER_CLASS)
        class FirstTwoChars extends NestedObfuscatorTest {

            FirstTwoChars() {
                super(maskChar -> portion().keepAtEnd(Integer.MAX_VALUE).atLeastFromStart(2).withMaskChar(maskChar).build(), new Arguments[] {
                        arguments("foo", '*', "**o"),
                        arguments("hello", '*', "**llo"),
                        arguments("hello world", '*', "**llo world"),
                        arguments("foobar", '*', "**obar"),
                        arguments("foo", 'x', "xxo"),
                        arguments("hello", 'x', "xxllo"),
                        arguments("foobar", 'x', "xxobar"),
                        arguments("hello world", 'x', "xxllo world"),
                }, "", "**ll");
            }
        }

        @Test
        @DisplayName("negative keepAtStart")
        void testNegativeKeepAtStart() {
            PortionBuilder builder = portion();
            assertThrows(IllegalArgumentException.class, () -> builder.keepAtStart(-1));
        }

        @Test
        @DisplayName("negative keepAtEnd")
        void testNegativeKeepAtEnd() {
            PortionBuilder builder = portion();
            assertThrows(IllegalArgumentException.class, () -> builder.keepAtEnd(-1));
        }

        @Test
        @DisplayName("negative atLeastFromStart")
        void testNegativeAtLeastFromStart() {
            PortionBuilder builder = portion();
            assertThrows(IllegalArgumentException.class, () -> builder.atLeastFromStart(-1));
        }

        @Test
        @DisplayName("negative atLeastFromEnd")
        void testNegativeAtLeastFromEnd() {
            PortionBuilder builder = portion();
            assertThrows(IllegalArgumentException.class, () -> builder.atLeastFromEnd(-1));
        }

        @Test
        @DisplayName("fixedTotalLength < keepAtStart + keepAtEnd")
        void testFixedTotalLengthSmallerThanKeepAtStartPlusKeepAtEnd() {
            assertDoesNotThrow(() -> portion().keepAtStart(1).keepAtEnd(1).withFixedTotalLength(2).build());
            PortionBuilder builder = portion().keepAtStart(1).keepAtEnd(1).withFixedTotalLength(1);
            assertThrows(IllegalStateException.class, () -> builder.build());
        }

        @Test
        @DisplayName("transform")
        void testTransform() {
            PortionBuilder builder = portion();
            @SuppressWarnings("unchecked")
            Function<PortionBuilder, String> f = mock(Function.class);
            when(f.apply(builder)).thenReturn("result");

            assertEquals("result", builder.transform(f));
            verify(f).apply(builder);
            verifyNoMoreInteractions(f);
        }
    }

    @Nested
    @DisplayName("fromFunction(s -> s.toString().toUpperCase())")
    @TestInstance(Lifecycle.PER_CLASS)
    class FromFunction extends NestedObfuscatorTest {

        FromFunction() {
            super(maskChar -> fromFunction(s -> s.toString().toUpperCase()), new Arguments[] {
                    arguments("foo", '*', "FOO"),
                    arguments("foo", 'x', "FOO"),
                    arguments("hello", '*', "HELLO"),
                    arguments("hello", 'x', "HELLO"),
            }, "", "NULL");
        }

        @Test
        @DisplayName("null function")
        void testNullFunction() {
            assertThrows(NullPointerException.class, () -> fromFunction(null));
        }

        @ParameterizedTest(name = "{1}")
        @MethodSource
        @DisplayName("equals(Object)")
        void testEquals(Obfuscator obfuscator, Object object, boolean expected) {
            assertEquals(expected, obfuscator.equals(object));
        }

        Arguments[] testEquals() {
            Function<CharSequence, String> function = s -> s.toString().toUpperCase();
            Obfuscator obfuscator = fromFunction(function);
            return new Arguments[] {
                    arguments(obfuscator, obfuscator, true),
                    arguments(obfuscator, null, false),
                    arguments(obfuscator, fromFunction(function), true),
                    arguments(obfuscator, fromFunction(s -> s.toString()), false),
                    arguments(obfuscator, "foo", false),
            };
        }

        @Test
        @DisplayName("hashCode()")
        void testHashCode() {
            Function<CharSequence, String> function = s -> s.toString().toUpperCase();
            Obfuscator obfuscator = fromFunction(function);
            assertEquals(obfuscator.hashCode(), obfuscator.hashCode());
            assertEquals(obfuscator.hashCode(), fromFunction(function).hashCode());
        }

        @Nested
        @DisplayName("Function returns null")
        class FunctionReturnsNull {

            private final Obfuscator obfuscator = fromFunction(s -> null);

            private void testThrowsNullPointerException(String input, Executable executable) {
                NullPointerException exception = assertThrows(NullPointerException.class, executable);
                assertEquals(Messages.fromFunction.functionReturnedNull(input), exception.getMessage());
            }

            @Test
            @DisplayName("obfuscateText(CharSequence)")
            void testObfuscateTextCharSequence() {
                testThrowsNullPointerException("Hello World", () -> obfuscator.obfuscateText("Hello World"));
            }

            @Test
            @DisplayName("obfuscateText(CharSequence, int, int)")
            void testObfuscateTextCharSequenceRange() {
                testThrowsNullPointerException("lo", () -> obfuscator.obfuscateText("Hello World", 3, 5));
            }

            @Test
            @DisplayName("obfuscateText(CharSequence, StringBuilder)")
            void testObfuscateTextCharSequenceToStringBuilder() {
                StringBuilder destination = new StringBuilder();
                testThrowsNullPointerException("Hello World", () -> obfuscator.obfuscateText("Hello World", destination));
                assertEquals("", destination.toString());
            }

            @Test
            @DisplayName("obfuscateText(CharSequence, int, int, StringBuilder)")
            void testObfuscateTextCharSequenceRangeToStringBuilder() {
                StringBuilder destination = new StringBuilder();
                testThrowsNullPointerException("lo", () -> obfuscator.obfuscateText("Hello World", 3, 5, destination));
                assertEquals("", destination.toString());
            }

            @Test
            @DisplayName("obfuscateText(CharSequence, StringBuffer)")
            void testObfuscateTextCharSequenceToStringBuffer() {
                StringBuffer destination = new StringBuffer();
                testThrowsNullPointerException("Hello World", () -> obfuscator.obfuscateText("Hello World", destination));
                assertEquals("", destination.toString());
            }

            @Test
            @DisplayName("obfuscateText(CharSequence, int, int, StringBuffer)")
            void testObfuscateTextCharSequenceRangeToStringBuffer() {
                StringBuffer destination = new StringBuffer();
                testThrowsNullPointerException("lo", () -> obfuscator.obfuscateText("Hello World", 3, 5, destination));
                assertEquals("", destination.toString());
            }

            @Test
            @DisplayName("obfuscateText(CharSequence, Appendable)")
            void testObfuscateTextCharSequenceToAppendable() {
                Writer destination = new StringWriter();
                testThrowsNullPointerException("Hello World", () -> obfuscator.obfuscateText("Hello World", destination));
                assertEquals("", destination.toString());
            }

            @Test
            @DisplayName("obfuscateText(CharSequence, int, int, Appendable)")
            void testObfuscateTextCharSequenceRangeToAppendable() {
                Writer destination = new StringWriter();
                testThrowsNullPointerException("lo", () -> obfuscator.obfuscateText("Hello World", 3, 5, destination));
                assertEquals("", destination.toString());
            }

            @Test
            @DisplayName("obfuscateText(Reader)")
            void testObfuscateTextReader() {
                testThrowsNullPointerException("Hello World", () -> obfuscator.obfuscateText(new StringReader("Hello World")));
            }

            @Test
            @DisplayName("obfuscateText(Reader, Appendable)")
            void testObfuscateTextReaderToAppendable() {
                StringBuilder destination = new StringBuilder();
                testThrowsNullPointerException("Hello World", () -> obfuscator.obfuscateText(new StringReader("Hello World"), destination));
                assertEquals("", destination.toString());
            }
        }
    }

    @Nested
    @DisplayName("untilLength")
    @TestInstance(Lifecycle.PER_CLASS)
    class UntilLength {

        @Nested
        @DisplayName("none().untilLength(4)")
        @TestInstance(Lifecycle.PER_CLASS)
        class UntilLengthWithNoFixedLengthPrefix {

            @Nested
            @DisplayName("then(all()).untilLength(12).then(none())")
            @TestInstance(Lifecycle.PER_CLASS)
            class WithNoFixedLength extends NestedObfuscatorTest {

                WithNoFixedLength() {
                    super(maskChar -> none().untilLength(4).then(all()).untilLength(12).then(none()), new Arguments[] {
                            arguments("0", '*', "0"),
                            arguments("01", '*', "01"),
                            arguments("012", '*', "012"),
                            arguments("0123", '*', "0123"),
                            arguments("01234", '*', "0123*"),
                            arguments("012345", '*', "0123**"),
                            arguments("0123456", '*', "0123***"),
                            arguments("01234567", '*', "0123****"),
                            arguments("012345678", '*', "0123*****"),
                            arguments("0123456789", '*', "0123******"),
                            arguments("0123456789A", '*', "0123*******"),
                            arguments("0123456789AB", '*', "0123********"),
                            arguments("0123456789ABC", '*', "0123********C"),
                            arguments("0123456789ABCD", '*', "0123********CD"),
                            arguments("0123456789ABCDE", '*', "0123********CDE"),
                            arguments("0123456789ABCDEF", '*', "0123********CDEF"),
                    }, "", "null");
                }
            }

            @Nested
            @DisplayName("then(fixedLength(3))")
            @TestInstance(Lifecycle.PER_CLASS)
            class WithFixedLength extends NestedObfuscatorTest {

                WithFixedLength() {
                    super(maskChar -> none().untilLength(4).then(fixedLength(3)), new Arguments[] {
                            arguments("0", '*', "0"),
                            arguments("01", '*', "01"),
                            arguments("012", '*', "012"),
                            arguments("0123", '*', "0123"),
                            arguments("01234", '*', "0123***"),
                            arguments("012345", '*', "0123***"),
                    }, "", "null");
                }
            }
        }

        @Nested
        @DisplayName("fixedLength(3).untilLength(4)")
        @TestInstance(Lifecycle.PER_CLASS)
        class UntilLengthWithFixedLengthPrefix {

            @Nested
            @DisplayName("then(none())")
            @TestInstance(Lifecycle.PER_CLASS)
            class WithNoFixedLength extends NestedObfuscatorTest {

                WithNoFixedLength() {
                    super(maskChar -> fixedLength(3).untilLength(4).then(none()), new Arguments[] {
                            arguments("0", '*', "***"),
                            arguments("01", '*', "***"),
                            arguments("012", '*', "***"),
                            arguments("0123", '*', "***"),
                            arguments("01234", '*', "***4"),
                            arguments("012345", '*', "***45"),
                    }, "***", "***");
                }
            }

            @Nested
            @DisplayName("then(fixedValue(xxx))")
            @TestInstance(Lifecycle.PER_CLASS)
            class WithFixedLength extends NestedObfuscatorTest {

                WithFixedLength() {
                    super(maskChar -> fixedLength(3).untilLength(4).then(fixedValue("xxx")), new Arguments[] {
                            arguments("0", '*', "***"),
                            arguments("01", '*', "***"),
                            arguments("012", '*', "***"),
                            arguments("0123", '*', "***"),
                            arguments("01234", '*', "***xxx"),
                            arguments("012345", '*', "***xxx"),
                    }, "***", "***");
                }
            }
        }

        @Test
        @DisplayName("invalid first prefix length")
        void testInvalidFirstPrefixLength() {
            Obfuscator obfuscator = none();
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> obfuscator.untilLength(0));
            assertEquals("0 <= 0", exception.getMessage());
        }

        @Test
        @DisplayName("invalid second prefix length")
        void testInvalidSecondPrefixLength() {
            Obfuscator obfuscator = none().untilLength(1).then(all());
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> obfuscator.untilLength(1));
            assertEquals("1 <= 1", exception.getMessage());
        }

        @Test
        @DisplayName("invalid third prefix length")
        void testInvalidThirdPrefixLength() {
            Obfuscator obfuscator = none().untilLength(1).then(all()).untilLength(2).then(none());
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> obfuscator.untilLength(2));
            assertEquals("2 <= 2", exception.getMessage());
        }

        @ParameterizedTest(name = "{1}")
        @MethodSource
        @DisplayName("equals(Object)")
        void testEquals(Obfuscator obfuscator, Object object, boolean expected) {
            assertEquals(expected, obfuscator.equals(object));
        }

        Arguments[] testEquals() {
            Obfuscator obfuscator = none().untilLength(4).then(all());
            return new Arguments[] {
                    arguments(obfuscator, obfuscator, true),
                    arguments(obfuscator, null, false),
                    arguments(obfuscator, none().untilLength(4).then(all()), true),
                    arguments(obfuscator, fixedLength(3).untilLength(4).then(all()), false),
                    arguments(obfuscator, none().untilLength(5).then(all()), false),
                    arguments(obfuscator, none().untilLength(4).then(fixedLength(3)), false),
                    arguments(obfuscator, "foo", false),
            };
        }

        @Test
        @DisplayName("hashCode()")
        void testHashCode() {
            Obfuscator obfuscator = none().untilLength(4).then(all());
            assertEquals(obfuscator.hashCode(), obfuscator.hashCode());
            assertEquals(obfuscator.hashCode(), none().untilLength(4).then(all()).hashCode());
        }
    }

    abstract static class NestedObfuscatorTest {

        private final Function<Character, Obfuscator> obfuscatorProvider;
        private final Arguments[] testData;
        private final String obfuscatedEmpty;
        private final String obfuscatedNull;

        NestedObfuscatorTest(Function<Character, Obfuscator> obfuscatorProvider, Arguments[] testData,
                String obfuscatedEmpty, String obfuscatedNull) {

            this.obfuscatorProvider = obfuscatorProvider;
            this.testData = testData;
            this.obfuscatedEmpty = obfuscatedEmpty;
            this.obfuscatedNull = obfuscatedNull;
        }

        @ParameterizedTest(name = "{0} with {1} -> {2}")
        @MethodSource("testData")
        @DisplayName("obfuscateText(CharSequence)")
        void testObfuscateTextCharSequence(String input, char maskChar, String expected) {
            Obfuscator obfuscator = obfuscatorProvider.apply(maskChar);
            assertEquals(expected, obfuscator.obfuscateText(input).toString());
        }

        @ParameterizedTest(name = "{0} with {1} -> {2}")
        @MethodSource("testData")
        @DisplayName("obfuscateText(CharSequence, int, int)")
        void testObfuscateTextCharSequenceRange(String input, char maskChar, String expected) {
            int length = input.length();

            final String prefix = "foo";
            final String postfix = "bar";

            Obfuscator obfuscator = obfuscatorProvider.apply(maskChar);

            assertEquals(expected, obfuscator.obfuscateText(input, 0, length).toString());
            assertEquals(expected, obfuscator.obfuscateText(prefix + input + postfix, prefix.length(), prefix.length() + length).toString());
            assertEquals(expected, obfuscator.obfuscateText(prefix + input, prefix.length(), prefix.length() + length).toString());
            assertEquals(expected, obfuscator.obfuscateText(input + postfix, 0, length).toString());

            assertThrows(IndexOutOfBoundsException.class, () -> obfuscator.obfuscateText(input, -1, length));
            assertThrows(IndexOutOfBoundsException.class, () -> obfuscator.obfuscateText(input, 0, length + 1));
            assertThrows(IndexOutOfBoundsException.class, () -> obfuscator.obfuscateText(input, 0, -1));
            assertThrows(IndexOutOfBoundsException.class, () -> obfuscator.obfuscateText(input, length + 1, length));
        }

        @ParameterizedTest(name = "{0} with {1} -> {2}")
        @MethodSource("testData")
        @DisplayName("obfuscateText(CharSequence, StringBuilder)")
        void testObfuscateTextCharSequenceToStringBuilder(String input, char maskChar, String expected) {
            Obfuscator obfuscator = obfuscatorProvider.apply(maskChar);

            StringBuilder sb = new StringBuilder();
            obfuscator.obfuscateText(input, sb);
            assertEquals(expected, sb.toString());

            sb.delete(0, sb.length());
            obfuscator.obfuscateText(new StringBuffer(input), sb);
            assertEquals(expected, sb.toString());
        }

        @ParameterizedTest(name = "{0} with {1} -> {2}")
        @MethodSource("testData")
        @DisplayName("obfuscateText(CharSequence, int, int, StringBuilder)")
        void testObfuscateTextCharSequenceRangeToStringBuilder(String input, char maskChar, String expected) {
            final String prefix = "foo";
            final String postfix = "bar";

            Obfuscator obfuscator = obfuscatorProvider.apply(maskChar);

            StringBuilder sb = new StringBuilder();
            obfuscator.obfuscateText(prefix + input + postfix, prefix.length(), prefix.length() + input.length(), sb);
            assertEquals(expected, sb.toString());

            sb.delete(0, sb.length());
            obfuscator.obfuscateText(new StringBuilder(prefix + input + postfix), prefix.length(), prefix.length() + input.length(), sb);
            assertEquals(expected, sb.toString());
        }

        @ParameterizedTest(name = "{0} with {1} -> {2}")
        @MethodSource("testData")
        @DisplayName("obfuscateText(CharSequence, StringBuffer)")
        void testObfuscateTextCharSequenceToStringBuffer(String input, char maskChar, String expected) {
            Obfuscator obfuscator = obfuscatorProvider.apply(maskChar);

            StringBuffer sb = new StringBuffer();
            obfuscator.obfuscateText(input, sb);
            assertEquals(expected, sb.toString());

            sb.delete(0, sb.length());
            obfuscator.obfuscateText(new StringBuffer(input), sb);
            assertEquals(expected, sb.toString());
        }

        @ParameterizedTest(name = "{0} with {1} -> {2}")
        @MethodSource("testData")
        @DisplayName("obfuscateText(CharSequence, int, int, StringBuffer)")
        void testObfuscateTextCharSequenceRangeToStringBuffer(String input, char maskChar, String expected) {
            final String prefix = "foo";
            final String postfix = "bar";

            Obfuscator obfuscator = obfuscatorProvider.apply(maskChar);

            StringBuffer sb = new StringBuffer();
            obfuscator.obfuscateText(prefix + input + postfix, prefix.length(), prefix.length() + input.length(), sb);
            assertEquals(expected, sb.toString());

            sb.delete(0, sb.length());
            obfuscator.obfuscateText(new StringBuilder(prefix + input + postfix), prefix.length(), prefix.length() + input.length(), sb);
            assertEquals(expected, sb.toString());
        }

        @ParameterizedTest(name = "{0} with {1} -> {2}")
        @MethodSource("testData")
        @DisplayName("obfuscateText(CharSequence, Appendable)")
        void testObfuscateTextCharSequenceToAppendable(String input, char maskChar, String expected) throws IOException {
            Obfuscator obfuscator = obfuscatorProvider.apply(maskChar);

            Writer writer = new StringWriter();
            obfuscator.obfuscateText(input, writer);
            assertEquals(expected, writer.toString());

            writer = new StringWriter();
            obfuscator.obfuscateText(new StringBuilder(input), writer);
            assertEquals(expected, writer.toString());

            StringBuilder stringBuilder = new StringBuilder();
            obfuscator.obfuscateText(input, (Appendable) stringBuilder);
            assertEquals(expected, stringBuilder.toString());

            stringBuilder.delete(0, stringBuilder.length());
            obfuscator.obfuscateText(new StringBuilder(input), (Appendable) stringBuilder);
            assertEquals(expected, stringBuilder.toString());

            StringBuffer stringBuffer = new StringBuffer();
            obfuscator.obfuscateText(input, (Appendable) stringBuffer);
            assertEquals(expected, stringBuffer.toString());

            stringBuffer.delete(0, stringBuffer.length());
            obfuscator.obfuscateText(new StringBuilder(input), (Appendable) stringBuffer);
            assertEquals(expected, stringBuffer.toString());
        }

        @ParameterizedTest(name = "{0} with {1} -> {2}")
        @MethodSource("testData")
        @DisplayName("obfuscateText(CharSequence, int, int, Appendable)")
        void testObfuscateTextCharSequenceRangeToAppendable(String input, char maskChar, String expected) throws IOException {
            final String prefix = "foo";
            final String postfix = "bar";

            Obfuscator obfuscator = obfuscatorProvider.apply(maskChar);

            Writer writer = new StringWriter();
            obfuscator.obfuscateText(prefix + input + postfix, prefix.length(), prefix.length() + input.length(), writer);
            assertEquals(expected, writer.toString());

            writer = new StringWriter();
            obfuscator.obfuscateText(new StringBuilder(prefix + input + postfix), prefix.length(), prefix.length() + input.length(), writer);
            assertEquals(expected, writer.toString());

            StringBuilder stringBuilder = new StringBuilder();
            obfuscator.obfuscateText(prefix + input + postfix, prefix.length(), prefix.length() + input.length(), (Appendable) stringBuilder);
            assertEquals(expected, stringBuilder.toString());

            stringBuilder.delete(0, stringBuilder.length());
            obfuscator.obfuscateText(new StringBuilder(prefix + input + postfix), prefix.length(), prefix.length() + input.length(),
                    (Appendable) stringBuilder);
            assertEquals(expected, stringBuilder.toString());

            StringBuffer stringBuffer = new StringBuffer();
            obfuscator.obfuscateText(prefix + input + postfix, prefix.length(), prefix.length() + input.length(), (Appendable) stringBuffer);
            assertEquals(expected, stringBuffer.toString());

            stringBuffer.delete(0, stringBuffer.length());
            obfuscator.obfuscateText(new StringBuilder(prefix + input + postfix), prefix.length(), prefix.length() + input.length(),
                    (Appendable) stringBuffer);
            assertEquals(expected, stringBuffer.toString());
        }

        @ParameterizedTest(name = "{0} with {1} -> {2}")
        @MethodSource("testData")
        @DisplayName("obfuscateText(Reader)")
        void testObfuscateTextReader(String input, char maskChar, String expected) throws IOException {
            Obfuscator obfuscator = obfuscatorProvider.apply(maskChar);

            assertEquals(expected, obfuscator.obfuscateText(new StringReader(input)).toString());
        }

        @ParameterizedTest(name = "{0} with {1} -> {2}")
        @MethodSource("testData")
        @DisplayName("obfuscateText(Reader, Appendable)")
        void testObfuscateTextReaderToAppendable(String input, char maskChar, String expected) throws IOException {
            Obfuscator obfuscator = obfuscatorProvider.apply(maskChar);

            Writer writer = new StringWriter();
            obfuscator.obfuscateText(new StringReader(input), writer);
            assertEquals(expected, writer.toString());

            StringBuilder stringBuilder = new StringBuilder();
            obfuscator.obfuscateText(new StringReader(input), stringBuilder);
            assertEquals(expected, stringBuilder.toString());

            StringBuffer stringBuffer = new StringBuffer();
            obfuscator.obfuscateText(new StringReader(input), stringBuffer);
            assertEquals(expected, stringBuffer.toString());
        }

        @ParameterizedTest(name = "{0} with {1} -> {2}")
        @MethodSource("testData")
        @DisplayName("obfuscateObject(Object)")
        void testObfuscateObject(String input, char maskChar, String expected) {
            Obfuscator obfuscator = obfuscatorProvider.apply(maskChar);

            Obfuscated<String> obfuscated = obfuscator.obfuscateObject(input);
            assertEquals(expected, obfuscated.toString());
            assertSame(input, obfuscated.value());
        }

        @ParameterizedTest(name = "{0} with {1} -> {2}")
        @MethodSource("testData")
        @DisplayName("obfuscateObject(Object, Supplier)")
        void testObfuscateObjectWithRepresentation(String input, char maskChar, String expected) {
            Obfuscator obfuscator = obfuscatorProvider.apply(maskChar);

            Object value = new Object();
            Obfuscated<?> obfuscated = obfuscator.obfuscateObject(value, () -> input);
            assertEquals(expected, obfuscated.toString());
            assertSame(value, obfuscated.value());
        }

        @Nested
        @TestInstance(Lifecycle.PER_CLASS)
        @DisplayName("streamTo(Appendable)")
        class StreamTo {

            @ParameterizedTest(name = "{0}: {2} with {3} -> {4}")
            @MethodSource("testData")
            @DisplayName("write(int)")
            void testWriteInt(@SuppressWarnings("unused") String appendableType, Supplier<Appendable> destinationSupplier,
                    String input, char maskChar, String expected) throws IOException {

                Obfuscator obfuscator = obfuscatorProvider.apply(maskChar);

                Appendable destination = destinationSupplier.get();
                try (Writer w = obfuscator.streamTo(destination)) {
                    for (int i = 0; i < input.length(); i++) {
                        w.write(input.charAt(i));
                    }
                }
                assertEquals(expected, destination.toString());

                IOException exception = assertThrows(IOException.class, () -> {
                    @SuppressWarnings("resource")
                    Writer w = obfuscator.streamTo(destination);
                    assertDoesNotThrow(w::close);
                    w.write('x');
                });
                assertClosedException(exception);
            }

            @ParameterizedTest(name = "{0}: {2} with {3} -> {4}")
            @MethodSource("testData")
            @DisplayName("write(char[])")
            void testWriteCharArray(@SuppressWarnings("unused") String appendableType, Supplier<Appendable> destinationSupplier,
                    String input, char maskChar, String expected) throws IOException {

                Obfuscator obfuscator = obfuscatorProvider.apply(maskChar);

                Appendable destination = destinationSupplier.get();
                try (Writer w = obfuscator.streamTo(destination)) {
                    w.write(input.toCharArray());
                }
                assertEquals(expected, destination.toString());

                IOException exception = assertThrows(IOException.class, () -> {
                    @SuppressWarnings("resource")
                    Writer w = obfuscator.streamTo(destination);
                    assertDoesNotThrow(w::close);
                    w.write(input.toCharArray());
                });
                assertClosedException(exception);
            }

            @ParameterizedTest(name = "{0}: {2} with {3} -> {4}")
            @MethodSource("testData")
            @DisplayName("write(char[], int, int)")
            void testWriteCharArrayRange(@SuppressWarnings("unused") String appendableType, Supplier<Appendable> destinationSupplier,
                    String input, char maskChar, String expected) throws IOException {

                Obfuscator obfuscator = obfuscatorProvider.apply(maskChar);

                Appendable destination = destinationSupplier.get();
                try (Writer w = obfuscator.streamTo(destination)) {
                    char[] content = input.toCharArray();
                    int index = 0;
                    while (index < input.length()) {
                        int to = Math.min(index + 5, input.length());
                        w.write(content, index, to - index);
                        index = to;
                    }

                    assertThrows(IndexOutOfBoundsException.class, () -> w.write(content, 0, content.length + 1));
                    assertThrows(IndexOutOfBoundsException.class, () -> w.write(content, -1, content.length));
                    assertThrows(IndexOutOfBoundsException.class, () -> w.write(content, 1, content.length));
                    assertThrows(IndexOutOfBoundsException.class, () -> w.write(content, 0, -1));
                }
                assertEquals(expected, destination.toString());

                IOException exception = assertThrows(IOException.class, () -> {
                    @SuppressWarnings("resource")
                    Writer w = obfuscator.streamTo(destination);
                    assertDoesNotThrow(w::close);
                    w.write(input.toCharArray(), 0, 1);
                });
                assertClosedException(exception);
            }

            @ParameterizedTest(name = "{0}: {2} with {3} -> {4}")
            @MethodSource("testData")
            @DisplayName("write(String)")
            void testWriteString(@SuppressWarnings("unused") String appendableType, Supplier<Appendable> destinationSupplier,
                    String input, char maskChar, String expected) throws IOException {

                Obfuscator obfuscator = obfuscatorProvider.apply(maskChar);

                Appendable destination = destinationSupplier.get();
                try (Writer w = obfuscator.streamTo(destination)) {
                    w.write(input);
                }
                assertEquals(expected, destination.toString());

                IOException exception = assertThrows(IOException.class, () -> {
                    @SuppressWarnings("resource")
                    Writer w = obfuscator.streamTo(destination);
                    assertDoesNotThrow(w::close);
                    w.write(input);
                });
                assertClosedException(exception);
            }

            @ParameterizedTest(name = "{0}: {2} with {3} -> {4}")
            @MethodSource("testData")
            @DisplayName("write(String, int, int)")
            void testWriteStringRange(@SuppressWarnings("unused") String appendableType, Supplier<Appendable> destinationSupplier,
                    String input, char maskChar, String expected) throws IOException {

                Obfuscator obfuscator = obfuscatorProvider.apply(maskChar);

                Appendable destination = destinationSupplier.get();
                try (Writer w = obfuscator.streamTo(destination)) {
                    int index = 0;
                    while (index < input.length()) {
                        int to = Math.min(index + 5, input.length());
                        w.write(input, index, to - index);
                        index = to;
                    }
                }
                assertEquals(expected, destination.toString());

                IOException exception = assertThrows(IOException.class, () -> {
                    @SuppressWarnings("resource")
                    Writer w = obfuscator.streamTo(destination);
                    assertDoesNotThrow(w::close);
                    w.write(input, 0, 1);
                });
                assertClosedException(exception);
            }

            @ParameterizedTest(name = "{0}: {2} with {3} -> {4}")
            @MethodSource("testDataWithNull")
            @DisplayName("append(CharSequence)")
            void testAppendCharSequence(@SuppressWarnings("unused") String appendableType, Supplier<Appendable> destinationSupplier,
                    String input, char maskChar, String expected) throws IOException {

                Obfuscator obfuscator = obfuscatorProvider.apply(maskChar);

                Appendable destination = destinationSupplier.get();
                try (Writer w = obfuscator.streamTo(destination)) {
                    w.append(input);
                }
                assertEquals(expected, destination.toString());

                IOException exception = assertThrows(IOException.class, () -> {
                    @SuppressWarnings("resource")
                    Writer w = obfuscator.streamTo(destination);
                    assertDoesNotThrow(w::close);
                    w.append(input);
                });
                assertClosedException(exception);
            }

            @ParameterizedTest(name = "{0}: {2} with {3} -> {4}")
            @MethodSource("testDataWithNull")
            @DisplayName("append(CharSequence, int, int)")
            void testAppendCharSequenceRange(@SuppressWarnings("unused") String appendableType, Supplier<Appendable> destinationSupplier,
                    String input, char maskChar, String expected) throws IOException {

                Obfuscator obfuscator = obfuscatorProvider.apply(maskChar);

                Appendable destination = destinationSupplier.get();
                try (Writer w = obfuscator.streamTo(destination)) {
                    int index = 0;
                    int length = input == null ? 4 : input.length();
                    while (index < length) {
                        int to = Math.min(index + 5, length);
                        w.append(input, index, to);
                        index = to;
                    }

                    assertThrows(IndexOutOfBoundsException.class, () -> w.append(input, 0, length + 1));
                    assertThrows(IndexOutOfBoundsException.class, () -> w.append(input, -1, length));
                    assertThrows(IndexOutOfBoundsException.class, () -> w.append(input, 1, 0));
                }
                assertEquals(expected, destination.toString());

                IOException exception = assertThrows(IOException.class, () -> {
                    @SuppressWarnings("resource")
                    Writer w = obfuscator.streamTo(destination);
                    assertDoesNotThrow(w::close);
                    w.append(input, 0, 1);
                });
                assertClosedException(exception);
            }

            @ParameterizedTest(name = "{0}: {2} with {3} -> {4}")
            @MethodSource("testData")
            @DisplayName("append(char)")
            void testAppendChar(@SuppressWarnings("unused") String appendableType, Supplier<Appendable> destinationSupplier,
                    String input, char maskChar, String expected) throws IOException {

                Obfuscator obfuscator = obfuscatorProvider.apply(maskChar);

                Appendable destination = destinationSupplier.get();
                try (Writer w = obfuscator.streamTo(destination)) {
                    for (int i = 0; i < input.length(); i++) {
                        w.append(input.charAt(i));
                    }
                }
                assertEquals(expected, destination.toString());

                IOException exception = assertThrows(IOException.class, () -> {
                    @SuppressWarnings("resource")
                    Writer w = obfuscator.streamTo(destination);
                    assertDoesNotThrow(w::close);
                    w.append('x');
                });
                assertClosedException(exception);
            }

            @ParameterizedTest(name = "{0}")
            @MethodSource("appendableArguments")
            @DisplayName("flush()")
            void testFlush(@SuppressWarnings("unused") String appendableType, Supplier<Appendable> destinationSupplier) throws IOException {
                Obfuscator obfuscator = obfuscatorProvider.apply('*');

                Appendable destination = destinationSupplier.get();
                try (Writer w = obfuscator.streamTo(destination)) {
                    w.flush();
                }

                IOException exception = assertThrows(IOException.class, () -> {
                    @SuppressWarnings("resource")
                    Writer w = obfuscator.streamTo(destination);
                    assertDoesNotThrow(w::close);
                    w.flush();
                });
                assertClosedException(exception);
            }

            Arguments[] testData() {
                return Arrays.stream(appendableArguments())
                        .flatMap(arg -> merge(arg, testData))
                        .toArray(Arguments[]::new);
            }

            Arguments[] testDataWithNull() {
                Arguments[] testDataWithNull = Arrays.copyOf(testData, testData.length + 1);
                testDataWithNull[testData.length] = arguments(null, '*', obfuscatedNull);
                return Arrays.stream(appendableArguments())
                        .flatMap(arg -> merge(arg, testDataWithNull))
                        .toArray(Arguments[]::new);
            }

            Arguments[] appendableArguments() {
                return new Arguments[] {
                        arguments(StringBuilder.class.getSimpleName(), (Supplier<Appendable>) StringBuilder::new),
                        arguments(StringBuffer.class.getSimpleName(), (Supplier<Appendable>) StringBuffer::new),
                        arguments(StringWriter.class.getSimpleName(), (Supplier<Appendable>) StringWriter::new),
                        arguments(Appendable.class.getSimpleName(), (Supplier<Appendable>) TestAppendable::new),
                };
            }

            private Stream<Arguments> merge(Arguments appendableArguments, Arguments[] dataArguments) {
                return Arrays.stream(dataArguments)
                        .map(arg -> merge(appendableArguments, arg));
            }

            private Arguments merge(Arguments appendableArguments, Arguments dataArguments) {
                Object[] appendableArgs = appendableArguments.get();
                Object[] dataArgs = dataArguments.get();
                Object[] args = new Object[appendableArgs.length + dataArgs.length];
                System.arraycopy(appendableArgs, 0, args, 0, appendableArgs.length);
                System.arraycopy(dataArgs, 0, args, appendableArgs.length, dataArgs.length);
                return arguments(args);
            }
        }

        Arguments[] testData() {
            Arguments[] result = Arrays.copyOf(testData, testData.length + 1);
            result[testData.length] = arguments("", '*', obfuscatedEmpty);
            return result;
        }
    }
}
