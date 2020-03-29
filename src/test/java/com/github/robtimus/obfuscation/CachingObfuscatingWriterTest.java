/*
 * CachingObfuscatingWriterTest.java
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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@SuppressWarnings("javadoc")
public class CachingObfuscatingWriterTest {

    @ParameterizedTest(name = "{0}")
    @CsvSource({
        "0, true",
        "1, true",
        "100, true",
        "-1, false"
    })
    @DisplayName("capacity")
    @SuppressWarnings("resource")
    public void testNegativeCapacity(int capacity, boolean expectSuccess) {
        if (expectSuccess) {
            assertDoesNotThrow(() -> new CachingObfuscatingWriter(all(), new StringBuilder(), capacity));
        } else {
            assertThrows(NegativeArraySizeException.class, () -> new CachingObfuscatingWriter(all(), new StringBuilder(), capacity));
        }
    }
}
