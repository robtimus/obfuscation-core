/*
 * CommaSeparatedObfuscator.java
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

import static com.github.robtimus.obfuscation.ObfuscatorUtils.checkStartAndEnd;
import static com.github.robtimus.obfuscation.ObfuscatorUtils.indexOf;
import static com.github.robtimus.obfuscation.ObfuscatorUtils.skipLeadingWhitespace;
import static com.github.robtimus.obfuscation.ObfuscatorUtils.skipTrailingWhitespace;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

final class CommaSeparatedObfuscator extends PropertyObfuscator {

    CommaSeparatedObfuscator(Builder builder) {
        super(builder);
    }

    @Override
    public CharSequence obfuscateText(CharSequence s, int start, int end) {
        checkStartAndEnd(s, start, end);
        StringBuilder sb = new StringBuilder(end - start);
        try {
            obfuscateText(s, start, end, sb);
            return sb.toString();
        } catch (IOException e) {
            // will not occur
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void obfuscateText(CharSequence s, int start, int end, Appendable destination) throws IOException {
        checkStartAndEnd(s, start, end);

        int index;
        while ((index = indexOf(s, ',', start, end)) != -1) {
            maskKeyValue(s, start, index, destination);
            destination.append(',');
            start = index + 1;
        }
        // remainder
        maskKeyValue(s, start, end, destination);
    }

    @Override
    public void obfuscateText(Reader input, Appendable destination) throws IOException {
        BufferedReader br = input instanceof BufferedReader ? (BufferedReader) input : new BufferedReader(input);
        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = br.read()) != -1) {
            if (c == ',') {
                maskKeyValue(sb, 0, sb.length(), destination);
                sb.delete(0, sb.length());
                destination.append(',');
            } else {
                sb.append((char) c);
            }
        }
        // remainder
        maskKeyValue(sb, 0, sb.length(), destination);
    }

    private void maskKeyValue(CharSequence s, int start, int end, Appendable destination) throws IOException {
        int index = indexOf(s, '=', start, end);
        if (index == -1) {
            // no value so nothing to mask
            destination.append(s, start, end);
        } else {
            int nameStart = skipLeadingWhitespace(s, start, index);
            int nameEnd = skipTrailingWhitespace(s, nameStart, index);
            String name = s.subSequence(nameStart, nameEnd).toString();
            Obfuscator obfuscator = getObfuscator(name);
            if (obfuscator == null) {
                destination.append(s, start, end);
            } else {
                int valueStart = skipLeadingWhitespace(s, index + 1, end);
                destination.append(s, start, valueStart);
                obfuscator.obfuscateText(s, valueStart, end, destination);
            }
        }
    }

    @Override
    public Writer streamTo(Appendable destination) {
        return new CachingObfuscatingWriter(this, destination);
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return PropertyObfuscator.class.getName() + "#commaSeparated[obfuscators=" + obfuscators() + "]";
    }
}
