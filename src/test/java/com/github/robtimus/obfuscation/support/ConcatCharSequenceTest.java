/*
 * ConcatCharSequenceTest.java
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

import static com.github.robtimus.obfuscation.support.ObfuscatorUtils.concat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ConcatCharSequenceTest {

    @ParameterizedTest(name = "{0} + {1}: {2}")
    @CsvSource({
            "'','',0",
            "first,second,11"
    })
    @DisplayName("length()")
    void testLength(CharSequence first, CharSequence second, int expected) {
        CharSequence concatenated = concat(first, second);
        assertEquals(expected, concatenated.length());
    }

    @ParameterizedTest(name = "{0} + {1}, {2}: {3}")
    @CsvSource({
            "first,second,0,f",
            "first,second,4,t",
            "first,second,5,s",
            "first,second,10,d"
    })
    @DisplayName("charAt(int)")
    void testCharAt(CharSequence first, CharSequence second, int index, char expected) {
        CharSequence concatenated = concat(first, second);
        assertEquals(expected, concatenated.charAt(index));
    }

    @ParameterizedTest(name = "{0} + {1}, {2}")
    @CsvSource({
            "'','',0",
            "first,second,-1",
            "first,second,11"
    })
    @DisplayName("charAt(int), invalid index")
    void testCharAtWithInvalidIndex(CharSequence first, CharSequence second, int index) {
        CharSequence concatenated = concat(first, second);
        IndexOutOfBoundsException exception = assertThrows(IndexOutOfBoundsException.class, () -> concatenated.charAt(index));
        assertEquals(Messages.charSequence.invalidIndex(concatenated.length(), index), exception.getMessage());
    }

    @ParameterizedTest(name = "{0} + {1}, {2} - {3}: {4}")
    @CsvSource({
            "'','',0,0,''",
            "'',second,0,6,second",
            "first,'',0,5,first",
            "first,second,0,5,first",
            "first,second,1,4,irs",
            "first,second,5,11,second",
            "first,second,6,10,econ",
            "first,second,4,10,tsecon",
            "first,second,0,11,firstsecond"
    })
    @DisplayName("subSequence(int, int)")
    void testSubSequence(CharSequence first, CharSequence second, int start, int end, String expected) {
        CharSequence concatenated = concat(first, second);
        assertEquals(expected, concatenated.subSequence(start, end).toString());
    }

    @ParameterizedTest(name = "{0} + {1}, {2} - {3}")
    @CsvSource({
            "'','',-1,0",
            "'','',-1,-1",
            "'','',0,1",
            "'','',1,1",
            "'','',1,0",
            "'',second,-1,6",
            "'',second,-1,-1",
            "'',second,0,7",
            "'',second,7,7",
            "'',second,7,6",
            "first,'',-1,5",
            "first,'',-1,-1",
            "first,'',0,6",
            "first,'',6,6",
            "first,'',6,5",
            "first,second,-1,0",
            "first,second,-1,-1",
            "first,second,0,12",
            "first,second,12,12",
            "first,second,12,11"
    })
    @DisplayName("subSequence(int, int), invalid range")
    void testSubSequenceWithInvalidRange(CharSequence first, CharSequence second, int start, int end) {
        CharSequence concatenated = concat(first, second);
        IndexOutOfBoundsException exception = assertThrows(IndexOutOfBoundsException.class, () -> concatenated.subSequence(start, end));
        assertEquals(Messages.charSequence.invalidStartOrEnd(concatenated.length(), start, end), exception.getMessage());
    }

    @ParameterizedTest(name = "{0} + {1}: {2}")
    @CsvSource({
            "'','',''",
            "'',second,second",
            "first,'',first",
            "first,second,firstsecond"
    })
    @DisplayName("toString()")
    void testToString(CharSequence first, CharSequence second, String expected) {
        CharSequence concatenated = concat(first, second);
        assertEquals(expected, concatenated.toString());
    }
}
