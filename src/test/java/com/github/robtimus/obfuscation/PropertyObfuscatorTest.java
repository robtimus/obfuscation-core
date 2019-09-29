/*
 * PropertyObfuscatorTest.java
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
import static com.github.robtimus.obfuscation.Obfuscator.none;
import static com.github.robtimus.obfuscation.PropertyObfuscator.withFactory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import com.github.robtimus.obfuscation.PropertyObfuscator.Builder;

@SuppressWarnings({ "javadoc", "nls" })
public class PropertyObfuscatorTest {

    @Nested
    @DisplayName("getObfuscator()")
    public class GetObfuscator {

        @Test
        @DisplayName("case sensitive")
        public void testCaseSensitive() {
            PropertyObfuscator obfuscator = withFactory(TestObfuscator::new)
                    .withProperty("test", all())
                    .build();
            assertEquals(all(), obfuscator.getObfuscator("test"));
            assertNull(obfuscator.getObfuscator("TEST"));
            assertNull(obfuscator.getObfuscator("other"));
        }

        @Test
        @DisplayName("case insensitive")
        public void testCaseInsensitive() {
            PropertyObfuscator obfuscator = withFactory(TestObfuscator::new)
                    .withProperty("test", all())
                    .withCaseInsensitivePropertyNames(true)
                    .build();
            assertEquals(all(), obfuscator.getObfuscator("test"));
            assertEquals(all(), obfuscator.getObfuscator("TEST"));
            assertNull(obfuscator.getObfuscator("other"));
        }
    }

    @Nested
    @DisplayName("getNonNullObfuscator()")
    public class GetNonNullObfuscator {

        @Test
        @DisplayName("case sensitive")
        public void testCaseSensitive() {
            PropertyObfuscator obfuscator = withFactory(TestObfuscator::new)
                    .withProperty("test", all())
                    .build();
            assertEquals(all(), obfuscator.getNonNullObfuscator("test"));
            assertEquals(none(), obfuscator.getNonNullObfuscator("TEST"));
            assertEquals(none(), obfuscator.getNonNullObfuscator("other"));
        }

        @Test
        @DisplayName("case insensitive")
        public void testCaseInsensitive() {
            PropertyObfuscator obfuscator = withFactory(TestObfuscator::new)
                    .withProperty("test", all())
                    .withCaseInsensitivePropertyNames(true)
                    .build();
            assertEquals(all(), obfuscator.getNonNullObfuscator("test"));
            assertEquals(all(), obfuscator.getNonNullObfuscator("TEST"));
            assertEquals(none(), obfuscator.getNonNullObfuscator("other"));
        }
    }

    @Test
    @DisplayName("withFactory")
    public void testWithFactory() {
        PropertyObfuscator obfuscator = withFactory(TestObfuscator::new).build();
        assertThat(obfuscator, instanceOf(TestObfuscator.class));
    }

    @Nested
    @DisplayName("Builder")
    public class BuilderTest {

        @Test
        @DisplayName("transform")
        public void testTransform() {
            Builder builder = withFactory(TestObfuscator::new);
            @SuppressWarnings("unchecked")
            Function<Builder, String> f = mock(Function.class);
            when(f.apply(builder)).thenReturn("result");

            assertEquals("result", builder.transform(f));
            verify(f).apply(builder);
            verifyNoMoreInteractions(f);
        }
    }
}
