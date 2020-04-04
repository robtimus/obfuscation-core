/*
 * CharArraySequence.java
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

import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.checkIndex;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.checkOffsetAndLength;
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.checkStartAndEnd;

final class CharArraySequence implements CharSequence {

    private char[] array;

    private int start;

    private int end;

    private String string;

    CharArraySequence() {
        this(null, 0, 0);
    }

    CharArraySequence(char[] array) {
        this(array, 0, array.length);
    }

    private CharArraySequence(char[] array, int start, int end) {
        this.array = array;
        this.start = start;
        this.end = end;
    }

    void resetWithStartAndEnd(int newStart, int newEnd) {
        checkStartAndEnd(array, newStart, newEnd);
        start = newStart;
        end = newEnd;
        string = null;
    }

    void resetWithOffsetAndLength(char[] newArray, int offset, int length) {
        checkOffsetAndLength(newArray, offset, length);
        array = newArray;
        start = offset;
        end = offset + length;
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
    public CharArraySequence subSequence(int subStart, int subEnd) {
        checkStartAndEnd(this, subStart, subEnd);
        return subStart == 0 && subEnd == length() ? this : new CharArraySequence(array, start + subStart, start + subEnd);
    }

    @Override
    public String toString() {
        if (string == null) {
            string = new String(array, start, length());
        }
        return string;
    }
}
