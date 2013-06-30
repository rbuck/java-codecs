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
 * Codec for the quoted-printable encoding scheme.
 * <p/>
 * see: http://fisheye5.cenqua.com/browse/glassfish/mail/src/java/com/sun/mail/util/QPEncoderStream.java
 *
 * @author Robert J. Buck
 */
public class QuotedPrintable extends Codec {

    /**
     * BitSet of printable characters as defined in RFC 1521.
     */
    private static final BitSet PRINTABLE_CHARS = new BitSet(256);

    private static final BitSet CRLF_CHARS = new BitSet(256);

    private static final BitSet HEX_CHARS = new BitSet(256);

    private static final byte ESCAPE_CHAR = '=';

    private static final byte TAB = 9;

    private static final byte SPACE = 32;

    private static final byte CR = '\r';

    private static final byte LF = '\n';

    private static final int BYTES_PER_LINE = 76;

    // Static initializer for printable chars collection

    static {
        // alpha characters
        for (int i = 33; i <= 60; i++) {
            PRINTABLE_CHARS.set(i);
        }
        for (int i = 62; i <= 126; i++) {
            PRINTABLE_CHARS.set(i);
        }
        PRINTABLE_CHARS.set(TAB);
        PRINTABLE_CHARS.set(SPACE);

        CRLF_CHARS.set(CR);
        CRLF_CHARS.set(LF);

        // normalized hex is numeric & upper alpha
        for (int i = 48; i <= 57; i++) {
            HEX_CHARS.set(i);
        }
        for (int i = 65; i <= 70; i++) {
            HEX_CHARS.set(i);
        }
    }

    public QuotedPrintable() {
        super("quoted-printable", StandardCodecs.aliases_QuotedPrintable);
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
                if (PRINTABLE_CHARS.get(b) || CRLF_CHARS.get(b)) {
                    buffer.write(b);
                } else {
                    if (b == ESCAPE_CHAR) {
                        if ((++i) < encoded.length) {
                            byte b1 = encoded[i];
                            if (HEX_CHARS.get(b1)) {
                                int u = Character.digit((char) b1, 16);
                                if ((++i) < encoded.length) {
                                    byte b2 = encoded[i];
                                    if (HEX_CHARS.get(b2)) {
                                        int l = Character.digit((char) encoded[i], 16);
                                        buffer.write((char) ((u << 4) + l));
                                    } else {
                                        Object[] arguments = {"quoted-printable"};
                                        String message = ResourceBundle.formatResourceBundleMessage(QuotedPrintable.class,
                                                "CODEC_DECODER_MALFORMED_INPUT", arguments);
                                        throw new MalformedInputException(message);
                                    }
                                } else {
                                    Object[] arguments = {"quoted-printable"};
                                    String message = ResourceBundle.formatResourceBundleMessage(QuotedPrintable.class,
                                            "CODEC_DECODER_MALFORMED_INPUT", arguments);
                                    throw new MalformedInputException(message);
                                }
                            } else {
                                // remove mta extra whitespace
                                while (b1 != CR) {
                                    if (b1 != SPACE && b1 != TAB) {
                                        Object[] arguments = {"quoted-printable"};
                                        String message = ResourceBundle.formatResourceBundleMessage(QuotedPrintable.class,
                                                "CODEC_DECODER_MALFORMED_INPUT", arguments);
                                        throw new MalformedInputException(message);
                                    }
                                    b1 = encoded[++i]; // eats the CR and WS
                                }
                                b1 = encoded[++i]; // eats the LF
                                assert b1 == LF;
                            }
                        } else {
                            Object[] arguments = {"quoted-printable"};
                            String message = ResourceBundle.formatResourceBundleMessage(QuotedPrintable.class,
                                    "CODEC_DECODER_MALFORMED_INPUT", arguments);
                            throw new MalformedInputException(message);
                        }
                    } else {
                        Object[] arguments = {"quoted-printable"};
                        String message = ResourceBundle.formatResourceBundleMessage(QuotedPrintable.class,
                                "CODEC_DECODER_MALFORMED_INPUT", arguments);
                        throw new MalformedInputException(message);
                    }
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

        private void encodeQuotedPrintable(int b, ByteArrayOutputStream buffer) {
            buffer.write(ESCAPE_CHAR);
            char hex1 = Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, 16));
            char hex2 = Character.toUpperCase(Character.forDigit(b & 0xF, 16));
            buffer.write(hex1);
            buffer.write(hex2);
        }

        private void emitCRLF(ByteArrayOutputStream buffer) {
            buffer.write('\r');
            buffer.write('\n');
        }

        public byte[] encode(byte[] bytes) {
            if (bytes == null) {
                return null;
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            final byte[] SOFT_LINE_BREAK = {'=', '\r', '\n'};

            int count = 0;
            boolean inCRLF = false;
            for (int i = 0; i < bytes.length; i++) {
                int b = bytes[i];
                if (b < 0) {
                    b = 256 + b;
                }
                if (b == '\r') {
                    inCRLF = true;
                    emitCRLF(buffer);
                } else {
                    if (b == '\n') {
                        if (!inCRLF) {
                            emitCRLF(buffer);
                        }
                    } else if (PRINTABLE_CHARS.get(b)) {
                        // subtract one for the soft line break
                        if ((++count) > BYTES_PER_LINE - 1) {
                            buffer.write(SOFT_LINE_BREAK, 0, SOFT_LINE_BREAK.length);
                            count = 1;
                        }
                        buffer.write(b);
                    } else {
                        // subtract one for the soft line break
                        if ((count += 3) > BYTES_PER_LINE - 1) {
                            buffer.write(SOFT_LINE_BREAK, 0, SOFT_LINE_BREAK.length);
                            count = 3;
                        }
                        encodeQuotedPrintable(b, buffer);
                    }
                    inCRLF = false;
                }
            }
            return buffer.toByteArray();
        }
    }

    public CodecEncoder newEncoder() {
        return new Encoder(this);
    }
}
