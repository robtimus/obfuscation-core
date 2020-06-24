/*
 * MessagesUtils.java
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.IOException;

@SuppressWarnings({ "javadoc", "nls" })
public final class MessagesUtils {

    private MessagesUtils() {
        throw new IllegalStateException("cannot create instances of " + getClass().getName());
    }

    public static void assertClosedException(IOException exception) {
        assertEquals(Messages.stream.closed.get(), exception.getMessage());
    }
}
