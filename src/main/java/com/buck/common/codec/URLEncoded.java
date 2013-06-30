/*
 * Copyright 2010-2013 Robert J. Buck
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

package com.buck.common.codec;

import com.buck.commons.i18n.ResourceBundle;

import java.io.ByteArrayOutputStream;
import java.util.BitSet;

/**
 * Codec for the www-form-urlencoded encoding scheme. This encoding scheme
 * should not be used to encode URI; instead users should use the {@link
 * com.buck.common.codec.PercentEncoded} class for encoding URI components. <p/>
 * This codec is meant to be a replacement for standard Java classes {@link
 * java.net.URLEncoder} and {@link java.net.URLDecoder} on older Java platforms,
 * as these classes in Java versions below 1.4 rely on the platform's default
 * charset encoding. </p>
 *
 * @author Robert J. Buck
 */
public class URLEncoded extends Codec {

    private static final byte ESCAPE_CHAR = '%';

    /**
     * BitSet of www-form-url safe characters.
     */
    private static final BitSet WWW_FORM_URL = new BitSet(256);

    // Static initializer for www_form_url

    static {
        // alpha characters
        for (int i = 'a'; i <= 'z'; i++) {
            WWW_FORM_URL.set(i);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            WWW_FORM_URL.set(i);
        }
        // numeric characters
        for (int i = '0'; i <= '9'; i++) {
            WWW_FORM_URL.set(i);
        }
        // special chars
        WWW_FORM_URL.set('-');
        WWW_FORM_URL.set('_');
        WWW_FORM_URL.set('.');
        WWW_FORM_URL.set('*');
        // blank to be replaced with +
        WWW_FORM_URL.set(' ');
    }

    public URLEncoded() {
        super("x-www-form-urlencoded", StandardCodecs.aliases_URLEncoded);
    }

    private static class Decoder extends CodecDecoder {
        public Decoder(Codec codec) {
            super(codec);
        }

        public byte[] decode(byte[] encoded) {
            if (encoded == null) {
                return null;
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            for (int i = 0; i < encoded.length; i++) {
                int b = encoded[i];
                if (b == '+') {
                    buffer.write(' ');
                } else if (b == ESCAPE_CHAR) {
                    try {
                        int u = Character.digit((char) encoded[++i], 16);
                        int l = Character.digit((char) encoded[++i], 16);
                        if (u == -1 || l == -1) {
                            Object[] arguments = {"www-form-urlencoded"};
                            String message = ResourceBundle.formatResourceBundleMessage(PercentEncoded.class,
                                    "CODEC_DECODER_MALFORMED_INPUT", arguments);
                            throw new MalformedInputException(message);
                        }
                        buffer.write((char) ((u << 4) + l));
                    } catch (ArrayIndexOutOfBoundsException e) {
                        Object[] arguments = {ESCAPE_CHAR};
                        String message = ResourceBundle.formatResourceBundleMessage(PercentEncoded.class,
                                "CODEC_DECODER_INCOMPLETE_ESCAPE", arguments);
                        throw new MalformedInputException(message, e);
                    }
                } else {
                    buffer.write(b);
                }
            }
            return buffer.toByteArray();
        }
    }

    public CodecDecoder newDecoder() {
        return new Decoder(this);
    }

    private static class Encoder extends CodecEncoder {
        public Encoder(Codec codec) {
            super(codec);
        }

        public byte[] encode(byte[] bytes) {
            if (bytes == null) {
                return null;
            }

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            for (byte bv : bytes) {
                int b = bv;
                if (b < 0) {
                    b = 256 + b;
                }
                if (WWW_FORM_URL.get(b)) {
                    if (b == ' ') {
                        b = '+';
                    }
                    buffer.write(b);
                } else {
                    buffer.write('%');
                    char hex1 = Character.toUpperCase(
                            Character.forDigit((b >> 4) & 0xF, 16));
                    char hex2 = Character.toUpperCase(
                            Character.forDigit(b & 0xF, 16));
                    buffer.write(hex1);
                    buffer.write(hex2);
                }
            }
            return buffer.toByteArray();
        }
    }

    public CodecEncoder newEncoder() {
        return new Encoder(this);
    }
}
