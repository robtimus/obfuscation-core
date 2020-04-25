/*
 * MapObfuscatorTest.java
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

import static com.github.robtimus.obfuscation.MapObfuscator.builder;
import static com.github.robtimus.obfuscation.Obfuscator.all;
import static com.github.robtimus.obfuscation.Obfuscator.fixedLength;
import static com.github.robtimus.obfuscation.Obfuscator.none;
import static com.github.robtimus.obfuscation.Obfuscator.portion;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import com.github.robtimus.obfuscation.MapObfuscator.Builder;

@SuppressWarnings({ "javadoc", "nls" })
public class MapObfuscatorTest {
    @Nested
    @DisplayName("obfuscateMap(Map<K, V>)")
    public class ObfuscateMap {

        @Test
        @DisplayName("without default obfuscator")
        public void testObfuscateWithoutDefaultObfuscator() {
            MapObfuscator<Integer, String> obfuscator = createBuilder()
                    .build();
            Map<Integer, String> map = createMap();
            Map<Integer, String> obfuscated = obfuscator.obfuscateMap(map);
            assertEquals("{0=***, 1=value1, 2=******, 3=val***, 4=value4, -1=null, null=<***>}", obfuscated.toString());
            assertEquals("[0, 1, 2, 3, 4, -1, null]", obfuscated.keySet().toString());
            assertEquals("[***, value1, ******, val***, value4, null, <***>]", obfuscated.values().toString());
            assertEquals("[0=***, 1=value1, 2=******, 3=val***, 4=value4, -1=null, null=<***>]", obfuscated.entrySet().toString());
        }

        @Test
        @DisplayName("with default obfuscator")
        public void testObfuscateWithDefaultObfuscator() {
            Obfuscator defaultObfuscator = portion()
                    .keepAtStart(1)
                    .keepAtEnd(1)
                    .withFixedLength(3)
                    .withMaskChar('x')
                    .build();
            MapObfuscator<Integer, String> obfuscator = createBuilder()
                    .withDefaultObfuscator(defaultObfuscator)
                    .build();
            Map<Integer, String> map = createMap();
            Map<Integer, String> obfuscated = obfuscator.obfuscateMap(map);
            assertEquals("{0=***, 1=value1, 2=******, 3=val***, 4=vxxx4, -1=nxxxl, null=<***>}", obfuscated.toString());
            assertEquals("[0, 1, 2, 3, 4, -1, null]", obfuscated.keySet().toString());
            assertEquals("[***, value1, ******, val***, vxxx4, nxxxl, <***>]", obfuscated.values().toString());
            assertEquals("[0=***, 1=value1, 2=******, 3=val***, 4=vxxx4, -1=nxxxl, null=<***>]", obfuscated.entrySet().toString());
        }

        private Builder<Integer, String> createBuilder() {
            return MapObfuscator.<Integer, String>builder()
                    .withKey(0, fixedLength(3))
                    .withKey(1, none())
                    .withKey(2, all())
                    .withKey(3, portion().keepAtStart(3).withFixedLength(3).build())
                    .withKey(null, portion().keepAtStart(1).keepAtEnd(1).withFixedLength(3).build())
                    ;
        }

        private Map<Integer, String> createMap() {
            Map<Integer, String> map = new LinkedHashMap<>();
            for (int i = 0; i < 5; i++) {
                map.put(i, "value" + i);
            }
            map.put(-1, null);
            map.put(null, "<null>");
            return map;
        }
    }

    @Test
    @DisplayName("transform")
    public void testTransform() {
        Builder<Integer, String> builder = builder();
        @SuppressWarnings("unchecked")
        Function<Builder<Integer, String>, String> f = mock(Function.class);
        when(f.apply(builder)).thenReturn("result");

        assertEquals("result", builder.transform(f));
        verify(f).apply(builder);
        verifyNoMoreInteractions(f);
    }
}
