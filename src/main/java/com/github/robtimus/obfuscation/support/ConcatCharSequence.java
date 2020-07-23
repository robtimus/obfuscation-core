/*
 * ConcatCharSequence.java
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
import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.checkStartAndEnd;

final class ConcatCharSequence implements CharSequence {

    private final CharSequence first;
    private final CharSequence second;

    ConcatCharSequence(CharSequence first, CharSequence second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public int length() {
        return first.length() + second.length();
    }

    @Override
    public char charAt(int index) {
        checkIndex(this, index);
        int splitAt = first.length();
        return index < splitAt
                ? first.charAt(index)
                : second.charAt(index - splitAt);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        checkStartAndEnd(this, start, end);
        if (start == end) {
            return ""; //$NON-NLS-1$
        }
        int splitAt = first.length();
        if (end <= splitAt) {
            return first.subSequence(start, end);
        }
        if (start >= splitAt) {
            return second.subSequence(start - splitAt, end - splitAt);
        }
        return new ConcatCharSequence(first.subSequence(start, splitAt), second.subSequence(0, end - splitAt));
    }

    @Override
    public String toString() {
        return first.toString() + second.toString();
    }
}
