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

@SuppressWarnings({ "javadoc", "nls" })
public class ObfuscatorTest {

    @Nested
    @DisplayName("all()")
    @TestInstance(Lifecycle.PER_CLASS)
    public class All extends NestedObfuscatorTest {

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
        public void testEquals(Obfuscator obfuscator, Object object, boolean expected) {
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
        public void testHashCode() {
            Obfuscator obfuscator = all('x');
            assertEquals(obfuscator.hashCode(), obfuscator.hashCode());
            assertEquals(obfuscator.hashCode(), all('x').hashCode());
        }
    }

    @Nested
    @DisplayName("none()")
    @TestInstance(Lifecycle.PER_CLASS)
    public class None extends NestedObfuscatorTest {

        None() {
            super(maskChar -> none(), new Arguments[] {
                    arguments("foo", '*', "foo"),
                    arguments("hello", '*', "hello"),
            }, "", "null");
        }

        @ParameterizedTest(name = "{1}")
        @MethodSource
        @DisplayName("equals(Object)")
        public void testEquals(Obfuscator obfuscator, Object object, boolean expected) {
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
        public void testHashCode() {
            Obfuscator obfuscator = none();
            assertEquals(obfuscator.hashCode(), obfuscator.hashCode());
            assertEquals(obfuscator.hashCode(), none().hashCode());
        }
    }

    @Nested
    @DisplayName("fixedLength(8)")
    @TestInstance(Lifecycle.PER_CLASS)
    public class FixedLength extends NestedObfuscatorTest {

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
        public void testNegativeLength() {
            assertThrows(IllegalArgumentException.class, () -> fixedLength(-1));
            assertThrows(IllegalArgumentException.class, () -> fixedLength(-1, 'x'));
        }

        @ParameterizedTest(name = "{1}")
        @MethodSource
        @DisplayName("equals(Object)")
        public void testEquals(Obfuscator obfuscator, Object object, boolean expected) {
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
        public void testHashCode() {
            Obfuscator obfuscator = fixedLength(8, 'x');
            assertEquals(obfuscator.hashCode(), obfuscator.hashCode());
            assertEquals(obfuscator.hashCode(), fixedLength(8, 'x').hashCode());
        }
    }

    @Nested
    @DisplayName("fixedLength(0)")
    @TestInstance(Lifecycle.PER_CLASS)
    public class ZeroFixedLength extends NestedObfuscatorTest {

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
    public class FixedValue extends NestedObfuscatorTest {

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
        public void testNullFixedValue() {
            assertThrows(NullPointerException.class, () -> fixedValue(null));
        }

        @ParameterizedTest(name = "{1}")
        @MethodSource
        @DisplayName("equals(Object)")
        public void testEquals(Obfuscator obfuscator, Object object, boolean expected) {
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
        public void testHashCode() {
            Obfuscator obfuscator = fixedValue("obfuscated");
            assertEquals(obfuscator.hashCode(), obfuscator.hashCode());
            assertEquals(obfuscator.hashCode(), fixedValue("obfuscated").hashCode());
        }
    }

    @Nested
    @DisplayName("fixedValue(\"\")")
    @TestInstance(Lifecycle.PER_CLASS)
    public class EmptyFixedValue extends NestedObfuscatorTest {

        EmptyFixedValue() {
            super(maskChar -> fixedValue(""), new Arguments[] {
                    arguments("foo", '*', ""),
                    arguments("hello", '*', ""),
            }, "", "");
        }
    }

    @Nested
    @DisplayName("portion()")
    public class Portion {

        @Nested
        @DisplayName("keepAtStart(4)")
        @TestInstance(Lifecycle.PER_CLASS)
        public class KeepAtStart extends NestedObfuscatorTest {

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
            public void testEquals(Obfuscator obfuscator, Object object, boolean expected) {
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
            public void testHashCode() {
                Obfuscator obfuscator = portion().keepAtStart(4).withMaskChar('x').build();
                assertEquals(obfuscator.hashCode(), obfuscator.hashCode());
                assertEquals(obfuscator.hashCode(), portion().keepAtStart(4).withMaskChar('x').build().hashCode());
            }
        }

        @Nested
        @DisplayName("keepAtEnd(4)")
        @TestInstance(Lifecycle.PER_CLASS)
        public class KeepAtEnd extends NestedObfuscatorTest {

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
            public void testEquals(Obfuscator obfuscator, Object object, boolean expected) {
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
            public void testHashCode() {
                Obfuscator obfuscator = portion().keepAtEnd(4).withMaskChar('x').build();
                assertEquals(obfuscator.hashCode(), obfuscator.hashCode());
                assertEquals(obfuscator.hashCode(), portion().keepAtEnd(4).withMaskChar('x').build().hashCode());
            }
        }

        @Nested
        @DisplayName("keepAtStart(4).keepAtEnd(4)")
        @TestInstance(Lifecycle.PER_CLASS)
        public class KeepAtStartAndEnd extends NestedObfuscatorTest {

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
            public void testEquals(Obfuscator obfuscator, Object object, boolean expected) {
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
            public void testHashCode() {
                Obfuscator obfuscator = portion().keepAtStart(4).keepAtEnd(4).withMaskChar('x').build();
                assertEquals(obfuscator.hashCode(), obfuscator.hashCode());
                assertEquals(obfuscator.hashCode(), portion().keepAtStart(4).keepAtEnd(4).withMaskChar('x').build().hashCode());
            }
        }

        @Nested
        @DisplayName("keepAtStart(4).atLeastFromEnd(4)")
        @TestInstance(Lifecycle.PER_CLASS)
        public class KeepAtStartAtLeastFromEnd extends NestedObfuscatorTest {

            public KeepAtStartAtLeastFromEnd() {
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
            public void testEquals(Obfuscator obfuscator, Object object, boolean expected) {
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
            public void testHashCode() {
                Obfuscator obfuscator = portion().keepAtStart(4).atLeastFromEnd(4).withMaskChar('x').build();
                assertEquals(obfuscator.hashCode(), obfuscator.hashCode());
                assertEquals(obfuscator.hashCode(), portion().keepAtStart(4).atLeastFromEnd(4).withMaskChar('x').build().hashCode());
            }
        }

        @Nested
        @DisplayName("keepAtEnd(4).atLeastFromStart(4)")
        @TestInstance(Lifecycle.PER_CLASS)
        public class KeepAtEndAtLeastFromStart extends NestedObfuscatorTest {

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
            public void testEquals(Obfuscator obfuscator, Object object, boolean expected) {
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
            public void testHashCode() {
                Obfuscator obfuscator = portion().keepAtEnd(4).atLeastFromStart(4).withMaskChar('x').build();
                assertEquals(obfuscator.hashCode(), obfuscator.hashCode());
                assertEquals(obfuscator.hashCode(), portion().keepAtEnd(4).atLeastFromStart(4).withMaskChar('x').build().hashCode());
            }
        }

        @Nested
        @DisplayName("keepAtStart(4).withFixedLength(3)")
        @TestInstance(Lifecycle.PER_CLASS)
        public class KeepAtStartWithFixedLength extends NestedObfuscatorTest {

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
            public void testEquals(Obfuscator obfuscator, Object object, boolean expected) {
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
            public void testHashCode() {
                Obfuscator obfuscator = portion().keepAtStart(4).withFixedLength(3).withMaskChar('x').build();
                assertEquals(obfuscator.hashCode(), obfuscator.hashCode());
                assertEquals(obfuscator.hashCode(), portion().keepAtStart(4).withFixedLength(3).withMaskChar('x').build().hashCode());
            }
        }

        @Nested
        @DisplayName("keepAtEnd(4).withFixedLength(3)")
        @TestInstance(Lifecycle.PER_CLASS)
        public class KeepAtEndWithFixedLength extends NestedObfuscatorTest {

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
            public void testEquals(Obfuscator obfuscator, Object object, boolean expected) {
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
            public void testHashCode() {
                Obfuscator obfuscator = portion().keepAtEnd(4).withFixedLength(3).withMaskChar('x').build();
                assertEquals(obfuscator.hashCode(), obfuscator.hashCode());
                assertEquals(obfuscator.hashCode(), portion().keepAtEnd(4).withFixedLength(3).withMaskChar('x').build().hashCode());
            }
        }

        @Nested
        @DisplayName("keepAtStart(4).keepAtEnd(4).withFixedLength(3)")
        @TestInstance(Lifecycle.PER_CLASS)
        public class KeepAtStartAndEndWithFixedLength extends NestedObfuscatorTest {

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
            public void testEquals(Obfuscator obfuscator, Object object, boolean expected) {
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
            public void testHashCode() {
                Obfuscator obfuscator = portion().keepAtStart(4).keepAtEnd(4).withFixedLength(3).withMaskChar('x').build();
                assertEquals(obfuscator.hashCode(), obfuscator.hashCode());
                assertEquals(obfuscator.hashCode(), portion().keepAtStart(4).keepAtEnd(4).withFixedLength(3).withMaskChar('x').build().hashCode());
            }
        }

        @Nested
        @DisplayName("keepAtStart(4).atLeastFromEnd(4).withFixedLength(3)")
        @TestInstance(Lifecycle.PER_CLASS)
        public class KeepAtStartAtLeastFromEndWithFixedLength extends NestedObfuscatorTest {

            public KeepAtStartAtLeastFromEndWithFixedLength() {
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
            public void testEquals(Obfuscator obfuscator, Object object, boolean expected) {
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
            public void testHashCode() {
                Obfuscator obfuscator = portion().keepAtStart(4).atLeastFromEnd(4).withFixedLength(3).withMaskChar('x').build();
                assertEquals(obfuscator.hashCode(), obfuscator.hashCode());
                assertEquals(obfuscator.hashCode(),
                        portion().keepAtStart(4).atLeastFromEnd(4).withFixedLength(3).withMaskChar('x').build().hashCode());
            }
        }

        @Nested
        @DisplayName("keepAtEnd(4).atLeastFromStart(4).withFixedLength(3)")
        @TestInstance(Lifecycle.PER_CLASS)
        public class KeepAtEndAtLeastFromStartWithFixedLength extends NestedObfuscatorTest {

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
            public void testEquals(Obfuscator obfuscator, Object object, boolean expected) {
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
            public void testHashCode() {
                Obfuscator obfuscator = portion().keepAtEnd(4).atLeastFromStart(4).withFixedLength(3).withMaskChar('x').build();
                assertEquals(obfuscator.hashCode(), obfuscator.hashCode());
                assertEquals(obfuscator.hashCode(),
                        portion().keepAtEnd(4).atLeastFromStart(4).withFixedLength(3).withMaskChar('x').build().hashCode());
            }
        }

        @Nested
        @DisplayName("keepAtStart(4).withFixedLength(0)")
        @TestInstance(Lifecycle.PER_CLASS)
        public class KeepAtStartWithZeroFixedLength extends NestedObfuscatorTest {

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
        public class KeepAtEndWithZeroFixedLength extends NestedObfuscatorTest {

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
        public class KeepAtStartAndEndWithZeroFixedLength extends NestedObfuscatorTest {

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
        public class KeepAtStartAtLeastFromEndWithZeroFixedLength extends NestedObfuscatorTest {

            public KeepAtStartAtLeastFromEndWithZeroFixedLength() {
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
        public class KeepAtEndAtLeastFromStartWithZeroFixedLength extends NestedObfuscatorTest {

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
        public class LastTwoChars extends NestedObfuscatorTest {

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
        public class FirstTwoChars extends NestedObfuscatorTest {

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
        public void testNegativeKeepAtStart() {
            assertThrows(IllegalArgumentException.class, () -> portion().keepAtStart(-1));
        }

        @Test
        @DisplayName("negative keepAtEnd")
        public void testNegativeKeepAtEnd() {
            assertThrows(IllegalArgumentException.class, () -> portion().keepAtEnd(-1));
        }

        @Test
        @DisplayName("negative atLeastFomStart")
        public void testNegativeAtLeastFromStart() {
            assertThrows(IllegalArgumentException.class, () -> portion().atLeastFromStart(-1));
        }

        @Test
        @DisplayName("negative AtLeastFromEnd")
        public void testNegativeAtLeastFromEnd() {
            assertThrows(IllegalArgumentException.class, () -> portion().atLeastFromEnd(-1));
        }

        @Test
        @DisplayName("transform")
        public void testTransform() {
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
    public class FromFunction extends NestedObfuscatorTest {

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
        public void testNullFunction() {
            assertThrows(NullPointerException.class, () -> fromFunction(null));
        }

        @ParameterizedTest(name = "{1}")
        @MethodSource
        @DisplayName("equals(Object)")
        public void testEquals(Obfuscator obfuscator, Object object, boolean expected) {
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
        public void testHashCode() {
            Function<CharSequence, String> function = s -> s.toString().toUpperCase();
            Obfuscator obfuscator = fromFunction(function);
            assertEquals(obfuscator.hashCode(), obfuscator.hashCode());
            assertEquals(obfuscator.hashCode(), fromFunction(function).hashCode());
        }

        @Nested
        @DisplayName("Function returns null")
        public class FunctionReturnsNull {

            private final Obfuscator obfuscator = fromFunction(s -> null);

            private void testThrowsNullPointerException(String input, Executable executable) {
                NullPointerException exception = assertThrows(NullPointerException.class, executable);
                assertEquals(Messages.fromFunction.functionReturnedNull.get(input), exception.getMessage());
            }

            @Test
            @DisplayName("obfuscateText(CharSequence)")
            public void testObfuscateTextCharSequence() {
                testThrowsNullPointerException("Hello World", () -> obfuscator.obfuscateText("Hello World"));
            }

            @Test
            @DisplayName("obfuscateText(CharSequence, int, int)")
            public void testObfuscateTextCharSequenceRange() {
                testThrowsNullPointerException("lo", () -> obfuscator.obfuscateText("Hello World", 3, 5));
            }

            @Test
            @DisplayName("obfuscateText(CharSequence, StringBuilder)")
            public void testObfuscateTextCharSequenceToStringBuilder() {
                StringBuilder destination = new StringBuilder();
                testThrowsNullPointerException("Hello World", () -> obfuscator.obfuscateText("Hello World", destination));
                assertEquals("", destination.toString());
            }

            @Test
            @DisplayName("obfuscateText(CharSequence, int, int, StringBuilder)")
            public void testObfuscateTextCharSequenceRangeToStringBuilder() {
                StringBuilder destination = new StringBuilder();
                testThrowsNullPointerException("lo", () -> obfuscator.obfuscateText("Hello World", 3, 5, destination));
                assertEquals("", destination.toString());
            }

            @Test
            @DisplayName("obfuscateText(CharSequence, StringBuffer)")
            public void testObfuscateTextCharSequenceToStringBuffer() {
                StringBuffer destination = new StringBuffer();
                testThrowsNullPointerException("Hello World", () -> obfuscator.obfuscateText("Hello World", destination));
                assertEquals("", destination.toString());
            }

            @Test
            @DisplayName("obfuscateText(CharSequence, int, int, StringBuffer)")
            public void testObfuscateTextCharSequenceRangeToStringBuffer() {
                StringBuffer destination = new StringBuffer();
                testThrowsNullPointerException("lo", () -> obfuscator.obfuscateText("Hello World", 3, 5, destination));
                assertEquals("", destination.toString());
            }

            @Test
            @DisplayName("obfuscateText(CharSequence, Appendable)")
            public void testObfuscateTextCharSequenceToAppendable() {
                Writer destination = new StringWriter();
                testThrowsNullPointerException("Hello World", () -> obfuscator.obfuscateText("Hello World", destination));
                assertEquals("", destination.toString());
            }

            @Test
            @DisplayName("obfuscateText(CharSequence, int, int, Appendable)")
            public void testObfuscateTextCharSequenceRangeToAppendable() {
                Writer destination = new StringWriter();
                testThrowsNullPointerException("lo", () -> obfuscator.obfuscateText("Hello World", 3, 5, destination));
                assertEquals("", destination.toString());
            }

            @Test
            @DisplayName("obfuscateText(Reader)")
            public void testObfuscateTextReader() {
                testThrowsNullPointerException("Hello World", () -> obfuscator.obfuscateText(new StringReader("Hello World")));
            }

            @Test
            @DisplayName("obfuscateText(Reader, Appendable)")
            public void testObfuscateTextReaderToAppendable() {
                StringBuilder destination = new StringBuilder();
                testThrowsNullPointerException("Hello World", () -> obfuscator.obfuscateText(new StringReader("Hello World"), destination));
                assertEquals("", destination.toString());
            }
        }
    }

    private static class NestedObfuscatorTest {

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
        public void testObfuscateTextCharSequence(String input, char maskChar, String expected) {
            Obfuscator obfuscator = obfuscatorProvider.apply(maskChar);
            assertEquals(expected, obfuscator.obfuscateText(input).toString());
        }

        @ParameterizedTest(name = "{0} with {1} -> {2}")
        @MethodSource("testData")
        @DisplayName("obfuscateText(CharSequence, int, int)")
        public void testObfuscateTextCharSequenceRange(String input, char maskChar, String expected) {
            final String prefix = "foo";
            final String postfix = "bar";

            Obfuscator obfuscator = obfuscatorProvider.apply(maskChar);

            assertEquals(expected, obfuscator.obfuscateText(input, 0, input.length()).toString());
            assertEquals(expected, obfuscator.obfuscateText(prefix + input + postfix, prefix.length(), prefix.length() + input.length()).toString());
            assertEquals(expected, obfuscator.obfuscateText(prefix + input, prefix.length(), prefix.length() + input.length()).toString());
            assertEquals(expected, obfuscator.obfuscateText(input + postfix, 0, input.length()).toString());

            assertThrows(IndexOutOfBoundsException.class, () -> obfuscator.obfuscateText(input, -1, input.length()));
            assertThrows(IndexOutOfBoundsException.class, () -> obfuscator.obfuscateText(input, 0, input.length() + 1));
            assertThrows(IndexOutOfBoundsException.class, () -> obfuscator.obfuscateText(input, 0, -1));
            assertThrows(IndexOutOfBoundsException.class, () -> obfuscator.obfuscateText(input, input.length() + 1, input.length()));
        }

        @ParameterizedTest(name = "{0} with {1} -> {2}")
        @MethodSource("testData")
        @DisplayName("obfuscateText(CharSequence, StringBuilder)")
        public void testObfuscateTextCharSequenceToStringBuilder(String input, char maskChar, String expected) {
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
        public void testObfuscateTextCharSequenceRangeToStringBuilder(String input, char maskChar, String expected) {
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
        public void testObfuscateTextCharSequenceToStringBuffer(String input, char maskChar, String expected) {
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
        public void testObfuscateTextCharSequenceRangeToStringBuffer(String input, char maskChar, String expected) {
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
        public void testObfuscateTextCharSequenceToAppendable(String input, char maskChar, String expected) throws IOException {
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
        public void testObfuscateTextCharSequenceRangeToAppendable(String input, char maskChar, String expected) throws IOException {
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
        public void testObfuscateTextReader(String input, char maskChar, String expected) throws IOException {
            Obfuscator obfuscator = obfuscatorProvider.apply(maskChar);

            assertEquals(expected, obfuscator.obfuscateText(new StringReader(input)).toString());
        }

        @ParameterizedTest(name = "{0} with {1} -> {2}")
        @MethodSource("testData")
        @DisplayName("obfuscateText(Reader, Appendable)")
        public void testObfuscateTextReaderToAppendable(String input, char maskChar, String expected) throws IOException {
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
        public void testObfuscateObject(String input, char maskChar, String expected) {
            Obfuscator obfuscator = obfuscatorProvider.apply(maskChar);

            Obfuscated<String> obfuscated = obfuscator.obfuscateObject(input);
            assertEquals(expected, obfuscated.toString());
            assertSame(input, obfuscated.value());
        }

        @ParameterizedTest(name = "{0} with {1} -> {2}")
        @MethodSource("testData")
        @DisplayName("obfuscateObject(Object, Supplier)")
        public void testObfuscateObjectWithRepresentation(String input, char maskChar, String expected) {
            Obfuscator obfuscator = obfuscatorProvider.apply(maskChar);

            Object value = new Object();
            Obfuscated<?> obfuscated = obfuscator.obfuscateObject(value, () -> input);
            assertEquals(expected, obfuscated.toString());
            assertSame(value, obfuscated.value());
        }

        @Nested
        @TestInstance(Lifecycle.PER_CLASS)
        @DisplayName("streamTo(Appendable)")
        public class StreamTo {

            @ParameterizedTest(name = "{0} with {1} -> {2}")
            @MethodSource("testData")
            @DisplayName("write(int)")
            public void testWriteInt(String input, char maskChar, String expected) throws IOException {
                Obfuscator obfuscator = obfuscatorProvider.apply(maskChar);

                Writer writer = new StringWriter();
                try (Writer w = obfuscator.streamTo(writer)) {
                    for (int i = 0; i < input.length(); i++) {
                        w.write(input.charAt(i));
                    }
                }
                assertEquals(expected, writer.toString());

                IOException exception = assertThrows(IOException.class, () -> {
                    @SuppressWarnings("resource")
                    Writer w = obfuscator.streamTo(writer);
                    w.close();
                    w.write('x');
                });
                assertEquals(Messages.stream.closed.get(), exception.getMessage());

                StringBuilder sb = new StringBuilder();
                try (Writer w = obfuscator.streamTo(sb)) {
                    for (int i = 0; i < input.length(); i++) {
                        w.write(input.charAt(i));
                    }
                }
                assertEquals(expected, sb.toString());

                exception = assertThrows(IOException.class, () -> {
                    @SuppressWarnings("resource")
                    Writer w = obfuscator.streamTo(sb);
                    w.close();
                    w.write('x');
                });
                assertEquals(Messages.stream.closed.get(), exception.getMessage());
            }

            @ParameterizedTest(name = "{0} with {1} -> {2}")
            @MethodSource("testData")
            @DisplayName("write(char[])")
            public void testWriteCharArray(String input, char maskChar, String expected) throws IOException {
                Obfuscator obfuscator = obfuscatorProvider.apply(maskChar);

                Writer writer = new StringWriter();
                try (Writer w = obfuscator.streamTo(writer)) {
                    w.write(input.toCharArray());
                }
                assertEquals(expected, writer.toString());

                IOException exception = assertThrows(IOException.class, () -> {
                    @SuppressWarnings("resource")
                    Writer w = obfuscator.streamTo(writer);
                    w.close();
                    w.write(input.toCharArray());
                });
                assertEquals(Messages.stream.closed.get(), exception.getMessage());

                StringBuilder sb = new StringBuilder();
                try (Writer w = obfuscator.streamTo(sb)) {
                    w.write(input.toCharArray());
                }
                assertEquals(expected, sb.toString());

                exception = assertThrows(IOException.class, () -> {
                    @SuppressWarnings("resource")
                    Writer w = obfuscator.streamTo(sb);
                    w.close();
                    w.write(input.toCharArray());
                });
                assertEquals(Messages.stream.closed.get(), exception.getMessage());
            }

            @ParameterizedTest(name = "{0} with {1} -> {2}")
            @MethodSource("testData")
            @DisplayName("write(char[], int, int)")
            public void testWriteCharArrayRange(String input, char maskChar, String expected) throws IOException {
                Obfuscator obfuscator = obfuscatorProvider.apply(maskChar);

                Writer writer = new StringWriter();
                try (Writer w = obfuscator.streamTo(writer)) {
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
                assertEquals(expected, writer.toString());

                IOException exception = assertThrows(IOException.class, () -> {
                    @SuppressWarnings("resource")
                    Writer w = obfuscator.streamTo(writer);
                    w.close();
                    w.write(input.toCharArray(), 0, 1);
                });
                assertEquals(Messages.stream.closed.get(), exception.getMessage());

                StringBuilder sb = new StringBuilder();
                try (Writer w = obfuscator.streamTo(sb)) {
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
                assertEquals(expected, sb.toString());

                exception = assertThrows(IOException.class, () -> {
                    @SuppressWarnings("resource")
                    Writer w = obfuscator.streamTo(sb);
                    w.close();
                    w.write(input.toCharArray(), 0, 1);
                });
                assertEquals(Messages.stream.closed.get(), exception.getMessage());
            }

            @ParameterizedTest(name = "{0} with {1} -> {2}")
            @MethodSource("testData")
            @DisplayName("write(String)")
            public void testWriteString(String input, char maskChar, String expected) throws IOException {
                Obfuscator obfuscator = obfuscatorProvider.apply(maskChar);

                Writer writer = new StringWriter();
                try (Writer w = obfuscator.streamTo(writer)) {
                    w.write(input);
                }
                assertEquals(expected, writer.toString());

                IOException exception = assertThrows(IOException.class, () -> {
                    @SuppressWarnings("resource")
                    Writer w = obfuscator.streamTo(writer);
                    w.close();
                    w.write(input);
                });
                assertEquals(Messages.stream.closed.get(), exception.getMessage());

                StringBuilder sb = new StringBuilder();
                try (Writer w = obfuscator.streamTo(sb)) {
                    w.write(input);
                }
                assertEquals(expected, sb.toString());

                exception = assertThrows(IOException.class, () -> {
                    @SuppressWarnings("resource")
                    Writer w = obfuscator.streamTo(sb);
                    w.close();
                    w.write(input);
                });
                assertEquals(Messages.stream.closed.get(), exception.getMessage());
            }

            @ParameterizedTest(name = "{0} with {1} -> {2}")
            @MethodSource("testData")
            @DisplayName("write(String, int, int)")
            public void testWriteStringRange(String input, char maskChar, String expected) throws IOException {
                Obfuscator obfuscator = obfuscatorProvider.apply(maskChar);

                Writer writer = new StringWriter();
                try (Writer w = obfuscator.streamTo(writer)) {
                    int index = 0;
                    while (index < input.length()) {
                        int to = Math.min(index + 5, input.length());
                        w.write(input, index, to - index);
                        index = to;
                    }
                }
                assertEquals(expected, writer.toString());

                IOException exception = assertThrows(IOException.class, () -> {
                    @SuppressWarnings("resource")
                    Writer w = obfuscator.streamTo(writer);
                    w.close();
                    w.write(input, 0, 1);
                });
                assertEquals(Messages.stream.closed.get(), exception.getMessage());

                StringBuilder sb = new StringBuilder();
                try (Writer w = obfuscator.streamTo(sb)) {
                    int index = 0;
                    while (index < input.length()) {
                        int to = Math.min(index + 5, input.length());
                        w.write(input, index, to - index);
                        index = to;
                    }
                }
                assertEquals(expected, sb.toString());

                exception = assertThrows(IOException.class, () -> {
                    @SuppressWarnings("resource")
                    Writer w = obfuscator.streamTo(sb);
                    w.close();
                    w.write(input, 0, 1);
                });
                assertEquals(Messages.stream.closed.get(), exception.getMessage());
            }

            @ParameterizedTest(name = "{0} with {1} -> {2}")
            @MethodSource("testDataWithNull")
            @DisplayName("append(CharSequence)")
            public void testAppendCharSequence(String input, char maskChar, String expected) throws IOException {
                Obfuscator obfuscator = obfuscatorProvider.apply(maskChar);

                Writer writer = new StringWriter();
                try (Writer w = obfuscator.streamTo(writer)) {
                    w.append(input);
                }
                assertEquals(expected, writer.toString());

                IOException exception = assertThrows(IOException.class, () -> {
                    @SuppressWarnings("resource")
                    Writer w = obfuscator.streamTo(writer);
                    w.close();
                    w.append(input);
                });
                assertEquals(Messages.stream.closed.get(), exception.getMessage());

                StringBuilder sb = new StringBuilder();
                try (Writer w = obfuscator.streamTo(sb)) {
                    w.append(input);
                }
                assertEquals(expected, sb.toString());

                exception = assertThrows(IOException.class, () -> {
                    @SuppressWarnings("resource")
                    Writer w = obfuscator.streamTo(writer);
                    w.close();
                    w.append(input);
                });
                assertEquals(Messages.stream.closed.get(), exception.getMessage());
            }

            @ParameterizedTest(name = "{0} with {1} -> {2}")
            @MethodSource("testDataWithNull")
            @DisplayName("append(CharSequence, int, int)")
            public void testAppendCharSequenceRange(String input, char maskChar, String expected) throws IOException {
                Obfuscator obfuscator = obfuscatorProvider.apply(maskChar);

                Writer writer = new StringWriter();
                try (Writer w = obfuscator.streamTo(writer)) {
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
                assertEquals(expected, writer.toString());

                IOException exception = assertThrows(IOException.class, () -> {
                    @SuppressWarnings("resource")
                    Writer w = obfuscator.streamTo(writer);
                    w.close();
                    w.append(input, 0, 1);
                });
                assertEquals(Messages.stream.closed.get(), exception.getMessage());

                StringBuilder sb = new StringBuilder();
                try (Writer w = obfuscator.streamTo(sb)) {
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
                assertEquals(expected, sb.toString());

                exception = assertThrows(IOException.class, () -> {
                    @SuppressWarnings("resource")
                    Writer w = obfuscator.streamTo(writer);
                    w.close();
                    w.append(input, 0, 1);
                });
                assertEquals(Messages.stream.closed.get(), exception.getMessage());
            }

            @ParameterizedTest(name = "{0} with {1} -> {2}")
            @MethodSource("testData")
            @DisplayName("append(char)")
            public void testAppendChar(String input, char maskChar, String expected) throws IOException {
                Obfuscator obfuscator = obfuscatorProvider.apply(maskChar);

                Writer writer = new StringWriter();
                try (Writer w = obfuscator.streamTo(writer)) {
                    for (int i = 0; i < input.length(); i++) {
                        w.append(input.charAt(i));
                    }
                }
                assertEquals(expected, writer.toString());

                IOException exception = assertThrows(IOException.class, () -> {
                    @SuppressWarnings("resource")
                    Writer w = obfuscator.streamTo(writer);
                    w.close();
                    w.append('x');
                });
                assertEquals(Messages.stream.closed.get(), exception.getMessage());

                StringBuilder sb = new StringBuilder();
                try (Writer w = obfuscator.streamTo(sb)) {
                    for (int i = 0; i < input.length(); i++) {
                        w.append(input.charAt(i));
                    }
                }
                assertEquals(expected, sb.toString());

                exception = assertThrows(IOException.class, () -> {
                    @SuppressWarnings("resource")
                    Writer w = obfuscator.streamTo(sb);
                    w.close();
                    w.append('x');
                });
                assertEquals(Messages.stream.closed.get(), exception.getMessage());
            }

            @Test
            @DisplayName("flush()")
            public void testFlush() throws IOException {
                Obfuscator obfuscator = obfuscatorProvider.apply('*');

                Writer writer = new StringWriter();
                try (Writer w = obfuscator.streamTo(writer)) {
                    w.flush();
                }

                IOException exception = assertThrows(IOException.class, () -> {
                    @SuppressWarnings("resource")
                    Writer w = obfuscator.streamTo(writer);
                    w.close();
                    w.flush();
                });
                assertEquals(Messages.stream.closed.get(), exception.getMessage());

                StringBuilder sb = new StringBuilder();
                try (Writer w = obfuscator.streamTo(sb)) {
                    w.flush();
                }

                exception = assertThrows(IOException.class, () -> {
                    @SuppressWarnings("resource")
                    Writer w = obfuscator.streamTo(sb);
                    w.close();
                    w.flush();
                });
                assertEquals(Messages.stream.closed.get(), exception.getMessage());
            }

            Arguments[] testData() {
                return testData;
            }

            Arguments[] testDataWithNull() {
                Arguments[] testDataWithNull = Arrays.copyOf(testData, testData.length + 1);
                testDataWithNull[testData.length] = arguments(null, '*', obfuscatedNull);
                return testDataWithNull;
            }
        }

        Arguments[] testData() {
            Arguments[] result = Arrays.copyOf(testData, testData.length + 1);
            result[testData.length] = arguments("", '*', obfuscatedEmpty);
            return result;
        }
    }
}
