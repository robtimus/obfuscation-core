/*
 * RepeatingCharSequence.java
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

import static com.github.robtimus.obfuscation.ObfuscatorUtils.checkBounds;
import static com.github.robtimus.obfuscation.ObfuscatorUtils.checkIndex;
import java.util.Arrays;

final class RepeatingCharSequence implements CharSequence {

    // the character doesn't matter here
    private static final RepeatingCharSequence EMPTY = new RepeatingCharSequence('*', 0);

    static {
        EMPTY.repeated = ""; //$NON-NLS-1$
    }

    private final char c;
    private final int count;
    private String repeated;

    private RepeatingCharSequence(char c, int count) {
        this.c = c;
        this.count = count;
    }

    static RepeatingCharSequence valueOf(char c, int count) {
        return count == 0 ? EMPTY : new RepeatingCharSequence(c, count);
    }

    @Override
    public char charAt(int index) {
        checkIndex(this, index);
        return c;
    }

    @Override
    public int length() {
        return count;
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        checkBounds(this, start, end);
        int newCount = end - start;
        return newCount == count ? this : new RepeatingCharSequence(c, newCount);
    }

    @Override
    public String toString() {
        if (repeated == null) {
            char[] array = new char[count];
            Arrays.fill(array, c);
            repeated = new String(array);
        }
        return repeated;
    }
}
