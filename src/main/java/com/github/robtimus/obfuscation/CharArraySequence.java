/*
 * CharArraySequence.java
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

final class CharArraySequence implements CharSequence {

    private final char[] array;

    private int start;
    private int end;

    private String string;

    CharArraySequence(char[] array) {
        this.array = array;
        start = 0;
        end = array.length;
    }

    private CharArraySequence(char[] array, int start, int end) {
        this.array = array;
        this.start = start;
        this.end = end;
    }

    void reset(int s, int e) {
        start = s;
        end = e;
        string = null;
    }

    @Override
    public char charAt(int index) {
        checkIndex(this, index);
        return array[start + index];
    }

    @Override
    public int length() {
        return end - start;
    }

    @Override
    public CharArraySequence subSequence(int s, int e) {
        checkBounds(this, s, e);
        return s == 0 && e == length() ? this : new CharArraySequence(array, start + s, start + e);
    }

    @Override
    public String toString() {
        if (string == null) {
            string = new String(array, start, length());
        }
        return string;
    }
}
