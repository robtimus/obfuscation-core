/*
 * PropertyAwareBuilderTest.java
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

import static com.github.robtimus.obfuscation.Obfuscator.all;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings({ "javadoc", "nls" })
public class PropertyAwareBuilderTest {

    @Test
    @DisplayName("transform")
    public void testTransform() {
        TestBuilder builder = new TestBuilder();
        @SuppressWarnings("unchecked")
        Function<PropertyAwareBuilder<?, ?>, String> f = mock(Function.class);
        when(f.apply(builder)).thenReturn("result");

        assertEquals("result", builder.transform(f));
        verify(f).apply(builder);
        verifyNoMoreInteractions(f);
    }

    @Nested
    @DisplayName("obfuscators()")
    public class Obfuscators {

        @Test
        @DisplayName("case sensitive")
        public void testCaseSensitive() {
            Map<String, Obfuscator> obfuscators = new TestBuilder()
                    .withProperty("test", all())
                    .obfuscators();
            assertEquals(all(), obfuscators.get("test"));
            assertNull(obfuscators.get("TEST"));
            assertNull(obfuscators.get("other"));
        }

        @Test
        @DisplayName("case insensitive")
        public void testCaseInsensitive() {
            Map<String, Obfuscator> obfuscators = new TestBuilder()
                    .withProperty("test", all())
                    .withCaseInsensitivePropertyNames(true)
                    .obfuscators();
            assertEquals(all(), obfuscators.get("test"));
            assertEquals(all(), obfuscators.get("TEST"));
            assertNull(obfuscators.get("other"));
        }
    }

    private static final class TestBuilder extends PropertyAwareBuilder<TestBuilder, Void> {

        @Override
        public Void build() {
            throw new UnsupportedOperationException();
        }
    }
}
