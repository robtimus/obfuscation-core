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
import static com.github.robtimus.obfuscation.PropertyObfuscator.availableTypes;
import static com.github.robtimus.obfuscation.PropertyObfuscator.ofType;
import static com.github.robtimus.obfuscation.PropertyObfuscator.reloadTypes;
import static com.github.robtimus.obfuscation.PropertyObfuscator.withFactory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import com.github.robtimus.obfuscation.PropertyObfuscator.Builder;
import com.github.robtimus.obfuscation.TestPropertyFactory.TestObfuscator;

@SuppressWarnings({ "javadoc", "nls" })
public class PropertyObfuscatorTest {

    private static final String INVALID_TYPE = "invalid";

    @Test
    @DisplayName("getObfuscator()")
    public void testGetObfuscator() {
        PropertyObfuscator obfuscator = withFactory(new TestPropertyFactory())
                .withProperty("test", all())
                .build();
        assertEquals(all(), obfuscator.getObfuscator("test"));
        assertNull(obfuscator.getObfuscator("other"));
    }

    @Test
    @DisplayName("getNonNullObfuscator()")
    public void testGetNonNullObfuscator() {
        PropertyObfuscator obfuscator = withFactory(new TestPropertyFactory())
                .withProperty("test", all())
                .build();
        assertEquals(all(), obfuscator.getNonNullObfuscator("test"));
        assertEquals(none(), obfuscator.getNonNullObfuscator("other"));
    }

    @Test
    @DisplayName("withFactory")
    public void testWithFactory() {
        PropertyObfuscator obfuscator = withFactory(new TestPropertyFactory()).build();
        assertThat(obfuscator, instanceOf(TestObfuscator.class));
    }

    @TestFactory
    @DisplayName("ofType(String)")
    public DynamicTest[] testOfType() {
        reloadTypes();
        return new DynamicTest[] {
                dynamicTest(TestPropertyFactory.TYPE, () -> {
                    PropertyObfuscator obfuscator = ofType(TestPropertyFactory.TYPE).build();
                    assertThat(obfuscator, instanceOf(TestObfuscator.class));
                }),
                dynamicTest(INVALID_TYPE, () -> {
                    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> ofType(INVALID_TYPE));
                    assertEquals(Messages.PropertyObfuscator.invalidType.get(INVALID_TYPE), exception.getMessage());
                }),
        };
    }

    @Test
    @DisplayName("availableTypes()")
    public void testAvailableTypes() {
        reloadTypes();
        assertEquals(Collections.singleton(TestPropertyFactory.TYPE), availableTypes());
    }

    @Nested
    @DisplayName("Builder")
    public class BuilderTest {

        @Test
        @DisplayName("Builder(PropertyObfuscatorFactory")
        public void testConstructor() {
            PropertyObfuscator obfuscator = new Builder(new TestPropertyFactory()).build();
            assertThat(obfuscator, instanceOf(TestObfuscator.class));
        }
    }
}
