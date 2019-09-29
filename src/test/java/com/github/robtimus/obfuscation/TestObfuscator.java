/*
 * TestObfuscator.java
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

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

final class TestObfuscator extends PropertyObfuscator {

    TestObfuscator(Builder builder) {
        super(builder);
    }

    @Override
    public CharSequence obfuscateText(CharSequence s, int start, int end) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void obfuscateText(CharSequence s, int start, int end, Appendable destination) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void obfuscateText(Reader input, Appendable destination) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Writer streamTo(Appendable destination) {
        throw new UnsupportedOperationException();
    }
}
