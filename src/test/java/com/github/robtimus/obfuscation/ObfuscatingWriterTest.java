/*
 * ObfuscatingWriterTest.java
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SuppressWarnings("javadoc")
public class ObfuscatingWriterTest {

    @Test
    @DisplayName("close()")
    public void testClose() throws IOException {
        AtomicInteger closeCount = new AtomicInteger(0);
        @SuppressWarnings("resource")
        ObfuscatingWriter writer = new ObfuscatingWriter() {

            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                // ignore
            }

            @Override
            protected void onClose() throws IOException {
                super.onClose();
                closeCount.incrementAndGet();
            }
        };
        writer.close();
        assertEquals(1, closeCount.get());
        assertTrue(writer.closed());
        writer.close();
        assertEquals(1, closeCount.get());
        assertTrue(writer.closed());
    }
}
