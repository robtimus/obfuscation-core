/*
 * RequestParameterObfuscator.java
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;

final class RequestParameterObfuscator extends Obfuscator {

    private final Map<String, Obfuscator> obfuscators;
    private final boolean caseInsensitivePropertyNames;
    private final Charset encoding;

    RequestParameterObfuscator(PropertyObfuscatorBuilder builder, Charset encoding) {
        obfuscators = builder.obfuscators();
        caseInsensitivePropertyNames = builder.caseInsensitivePropertyNames();
        this.encoding = Objects.requireNonNull(encoding);
    }

    @Override
    public CharSequence obfuscateText(CharSequence s, int start, int end) {
        checkStartAndEnd(s, start, end);
        StringBuilder sb = new StringBuilder(end - start);
        obfuscateText(s, start, end, sb);
        return sb.toString();
    }

    @Override
    public void obfuscateText(CharSequence s, int start, int end, Appendable destination) throws IOException {
        checkStartAndEnd(s, start, end);

        int index;
        while ((index = indexOf(s, '&', start, end)) != -1) {
            maskKeyValue(s, start, index, destination);
            destination.append('&');
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
            if (c == '&') {
                maskKeyValue(sb, 0, sb.length(), destination);
                sb.delete(0, sb.length());
                destination.append('&');
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
            String name = URLDecoder.decode(s.subSequence(start, index).toString(), encoding.name());
            Obfuscator obfuscator = obfuscators.get(name);
            if (obfuscator == null) {
                destination.append(s, start, end);
            } else {
                String value = URLDecoder.decode(s.subSequence(index + 1, end).toString(), encoding.name());
                destination.append(s, start, index + 1);
                CharSequence obfuscated = obfuscator.obfuscateText(value);
                destination.append(URLEncoder.encode(obfuscated.toString(), encoding.name()));
            }
        }
    }

    @Override
    public Writer streamTo(Appendable destination) {
        return new CachingObfuscatingWriter(this, destination);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || o.getClass() != getClass()) {
            return false;
        }
        RequestParameterObfuscator other = (RequestParameterObfuscator) o;
        return obfuscators.equals(other.obfuscators)
                && caseInsensitivePropertyNames == other.caseInsensitivePropertyNames
                && encoding.equals(other.encoding);
    }

    @Override
    public int hashCode() {
        return obfuscators.hashCode() ^ encoding.hashCode();
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return Obfuscator.class.getName()
                + "#requestParameters[obfuscators=" + obfuscators
                + ",caseInsensitivePropertyNames=" + caseInsensitivePropertyNames
                + ",encoding=" + encoding
                + "]";
    }
}
