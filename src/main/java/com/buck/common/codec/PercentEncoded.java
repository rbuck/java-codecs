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

/**
 * Codec for the percent-encoded encoding scheme. <p/> See the RFC-3986
 * specification for more details. </p>
 *
 * @author Robert J. Buck
 */
public class PercentEncoded extends Codec {

    private static final boolean pooled = false;

    private static final byte ESCAPE_CHAR = '%';

    private static final int BASELENGTH = 128;
    private static final byte[] decoderAlphabet = new byte[BASELENGTH];
    private static final int LOOKUPLENGTH = 16;
    private static final byte[] encoderAlphabet = new byte[LOOKUPLENGTH];
    private static final int UNRESERVED_LENGTH = 256;
    private static final byte[] isUnreserved = new byte[UNRESERVED_LENGTH];

    static {
        // decoder alphabet
        for (int i = 0; i < BASELENGTH; ++i) {
            decoderAlphabet[i] = -1;
        }
        for (int i = '0'; i <= '9'; i++) {
            decoderAlphabet[i] = (byte) (i - '0');
        }
        for (int i = 'A'; i <= 'F'; i++) {
            decoderAlphabet[i] = (byte) (i - 'A' + 10);
        }
        for (int i = 'a'; i <= 'f'; i++) {
            decoderAlphabet[i] = (byte) (i - 'A' + 10);
        }

        // encoder alphabet
        for (int i = 0; i < LOOKUPLENGTH; ++i) {
            encoderAlphabet[i] = -1;
        }
        for (int i = 0x0; i <= 0x9; ++i) {
            encoderAlphabet[i] = (byte) ('0' + i);
        }
        for (int i = 0xA; i <= 0xF; ++i) {
            encoderAlphabet[i] = (byte) ('A' + i - 0xA);
        }

        // unreserved alphabet
        for (int i = 0; i < UNRESERVED_LENGTH; ++i) {
            isUnreserved[i] = 0;
        }
        for (int i = 'a'; i <= 'z'; i++) {
            isUnreserved[i] = 1;
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            isUnreserved[i] = 1;
        }
        for (int i = '0'; i <= '9'; i++) {
            isUnreserved[i] = 1;
        }
        isUnreserved['-'] = 1;
        isUnreserved['.'] = 1;
        isUnreserved['_'] = 1;
        isUnreserved['~'] = 1;
    }

    public PercentEncoded() {
        super("pct-encoded", StandardCodecs.aliases_PercentEncoded);
    }

    private static class Decoder extends CodecDecoder {
        public Decoder(Codec codec) {
            super(codec);
        }

        public byte[] decode(byte[] encoded) {
            if (encoded == null) {
                return null;
            }

            ByteArrayOutputStream buffer = new ByteArrayOutputStream(encoded.length);
            for (int i = 0; i < encoded.length; i++) {
                int b = encoded[i];
                if (b == ESCAPE_CHAR) {
                    try {
                        int u = decoderAlphabet[encoded[++i]];
                        int l = decoderAlphabet[encoded[++i]];
                        if (u == -1 || l == -1) {
                            Object[] arguments = {"percent-encoded"};
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

            int size = bytes.length;
            for (byte bv : bytes) {
                int b = bv;
                if (b < 0) {
                    b = 256 + b;
                }
                if (isUnreserved[b] == 0) {
                    size += 2;
                }
            }
            byte[] buffer = new byte[size];
            for (int i = 0, j = 0; i < bytes.length; i++, j++) {
                int b = bytes[i];
                if (b < 0) {
                    b = 256 + b;
                }
                if (isUnreserved[b] != 0) {
                    buffer[j] = (byte) b;
                } else {
                    buffer[j++] = (byte) '%';
                    buffer[j++] = encoderAlphabet[(b >> 4) & 0xF];
                    buffer[j] = encoderAlphabet[b & 0xF];
                }
            }
            return buffer;
        }
    }

    public CodecEncoder newEncoder() {
        return new Encoder(this);
    }
}
