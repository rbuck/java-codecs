/*
 * Copyright 2010 Robert J. Buck
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

// -- This file was mechanically generated: Do not edit! -- //

package com.buck.common.codec;

class StandardCodecs extends FastCodecProvider {

    static final String[] aliases_Base16 = new String[]{
            "hex",
            "hexBinary",
    };

    static final String[] aliases_Base32 = new String[]{
    };

    static final String[] aliases_Base32Hex = new String[]{
    };

    static final String[] aliases_Base64 = new String[]{
            "base64Binary",
    };

    static final String[] aliases_Base64URL = new String[]{
            "base64URLSafe",
    };

    static final String[] aliases_QuotedPrintable = new String[]{
    };

    static final String[] aliases_PercentEncoded = new String[]{
            "percent-encoded",
    };

    static final String[] aliases_URLEncoded = new String[]{
            "www-form-urlencoded",
    };

    private static final class Aliases
            extends sun.util.PreHashedMap<String> {

        private static final int ROWS = 4;
        private static final int SIZE = 6;
        private static final int SHIFT = 0;
        private static final int MASK = 0x3;

        private Aliases() {
            super(ROWS, SIZE, SHIFT, MASK);
        }

        protected void init(Object[] ht) {
            ht[0] = new Object[]{"base64binary", "base64",
                    new Object[]{"hexbinary", "base16"}};
            ht[1] = new Object[]{"base64urlsafe", "base64url"};
            ht[2] = new Object[]{"www-form-urlencoded", "x-www-form-urlencoded",
                    new Object[]{"percent-encoded", "pct-encoded"}};
            ht[3] = new Object[]{"hex", "base16"};
        }

    }

    private static final class Classes
            extends sun.util.PreHashedMap<String> {

        private static final int ROWS = 4;
        private static final int SIZE = 8;
        private static final int SHIFT = 0;
        private static final int MASK = 0x3;

        private Classes() {
            super(ROWS, SIZE, SHIFT, MASK);
        }

        protected void init(Object[] ht) {
            ht[0] = new Object[]{"base64url", "Base64URL",
                    new Object[]{"base32", "Base32"}};
            ht[1] = new Object[]{"x-www-form-urlencoded", "URLEncoded"};
            ht[2] = new Object[]{"pct-encoded", "PercentEncoded",
                    new Object[]{"quoted-printable", "QuotedPrintable",
                            new Object[]{"base16", "Base16"}}};
            ht[3] = new Object[]{"base64", "Base64",
                    new Object[]{"base32hex", "Base32Hex"}};
        }

    }

    private static final class Cache
            extends sun.util.PreHashedMap<Codec> {

        private static final int ROWS = 4;
        private static final int SIZE = 8;
        private static final int SHIFT = 0;
        private static final int MASK = 0x3;

        private Cache() {
            super(ROWS, SIZE, SHIFT, MASK);
        }

        protected void init(Object[] ht) {
            ht[0] = new Object[]{"base64url", null,
                    new Object[]{"base32", null}};
            ht[1] = new Object[]{"x-www-form-urlencoded", null};
            ht[2] = new Object[]{"pct-encoded", null,
                    new Object[]{"quoted-printable", null,
                            new Object[]{"base16", null}}};
            ht[3] = new Object[]{"base64", null,
                    new Object[]{"base32hex", null}};
        }

    }

    public StandardCodecs() {
        super("com.buck.common.codec", new Aliases(), new Classes(), new Cache());
    }
}
