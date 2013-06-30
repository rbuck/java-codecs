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

/**
 * Codec for RFC 4648 Base32 Encoding with Extended Hex Alphabet
 * <p/>
 * This implementation does not encode/decode streaming data. You need the data
 * that you will encode/decode already on a byte arrray.
 *
 * @author Robert J. Buck
 */
public class Base32Hex extends Codec {

    static private final int BASELENGTH = 128;
    static private final int LOOKUPLENGTH = 32;
    static private final int FORTYBITGROUP = 40;
    static private final int EIGHTBIT = 8;
    static private final int SIXTEENBIT = 16;
    static private final int TWENTYFOURBIT = 24;
    static private final int THIRTYTWOBIT = 32;
    static private final int EIGHTBYTE = 8;
    static private final int SIGN = -128;

    static private final char PAD = '=';

    /**
     * The Base32 alphabet according to Section 6 of RFC 4648. The Base32 Hex
     * Extended alphabet detailed in Section 7 can replace this alphabet with
     * the consequences listed in Section 3.4. Since parity between collation
     * order of decoded and encoded data has been considered unnecessary, the
     * implementation uses the normative alphabet per Section 6 instead.
     */
    private static final byte[] base32Alphabet = new byte[BASELENGTH];

    /**
     * Table used to decode Base32 encoded data; the table is dynamically
     * created from the Base32 alphabet.
     */
    private static final byte[] lookUpBase32Alphabet = new byte[LOOKUPLENGTH];

    static {
        // decoder alphabet
        for (int i = 0; i < BASELENGTH; ++i) {
            base32Alphabet[i] = -1;
        }
        for (int i = '9'; i >= '0'; i--) {
            base32Alphabet[i] = (byte) (i - '0');
        }
        for (int i = 'V'; i >= 'A'; i--) {
            base32Alphabet[i] = (byte) (i - 'A' + 10);
        }

        // encode alphabet
        for (int i = 0; i <= 9; i++) {
            lookUpBase32Alphabet[i] = (byte) ('0' + i);
        }
        for (int i = 10, j = 0; i <= 31; i++, j++) {
            lookUpBase32Alphabet[i] = (byte) ('A' + j);
        }
    }

    private static boolean isWhiteSpace(byte octet) {
        return (octet == 0x20 || octet == 0xd || octet == 0xa || octet == 0x9);
    }

    private static boolean isPad(byte octet) {
        return (octet == PAD);
    }

    private static boolean isData(byte octet) {
        return (octet < BASELENGTH && base32Alphabet[octet] != -1);
    }

    protected static boolean isBase64(byte octet) {
        return (isWhiteSpace(octet) || isPad(octet) || isData(octet));
    }

    /**
     * Remove whitespace from MIME containing encoded Base32 data.
     *
     * @param data the byte array of base32 data (with WS)
     * @return the new length
     */
    private static int removeWhiteSpace(byte[] data) {
        if (data == null) {
            return 0;
        }

        // count characters that's not whitespace
        int newSize = 0;
        int len = data.length;
        for (int i = 0; i < len; i++) {
            if (!isWhiteSpace(data[i])) {
                data[newSize++] = data[i];
            }
        }
        return newSize;
    }

    public Base32Hex() {
        super("base32", StandardCodecs.aliases_Base32);
    }

    private static class Decoder extends CodecDecoder {
        public Decoder(Codec codec) {
            super(codec);
        }

        public byte[] decode(byte[] encoded) {
            if (encoded == null) {
                return null;
            }

            // remove white spaces
            int len = removeWhiteSpace(encoded);

            // must be divisible by eight
            if (len % EIGHTBYTE != 0) {
                return null;
            }

            int numberOfEights = (len / EIGHTBYTE);

            if (numberOfEights == 0) {
                // todo: error
                return EMPTY_STRING;
            }

            byte b1, b2, b3, b4, b5, b6, b7, b8;
            byte d1, d2, d3, d4, d5, d6, d7, d8;

            int i = 0;
            int encodedIndex = 0;
            int dataIndex = 0;
            byte[] decodedData = new byte[(numberOfEights) * 5];

            // encode all but last eight
            for (; i < numberOfEights - 1; i++) {

                if (!isData((d1 = encoded[dataIndex++])) ||
                        !isData((d2 = encoded[dataIndex++])) ||
                        !isData((d3 = encoded[dataIndex++])) ||
                        !isData((d4 = encoded[dataIndex++])) ||
                        !isData((d5 = encoded[dataIndex++])) ||
                        !isData((d6 = encoded[dataIndex++])) ||
                        !isData((d7 = encoded[dataIndex++])) ||
                        !isData((d8 = encoded[dataIndex++]))) {
                    // todo error
                    // if found "no data" just return null
                    return null;
                }

                b1 = base32Alphabet[d1];
                b2 = base32Alphabet[d2];
                b3 = base32Alphabet[d3];
                b4 = base32Alphabet[d4];
                b5 = base32Alphabet[d5];
                b6 = base32Alphabet[d6];
                b7 = base32Alphabet[d7];
                b8 = base32Alphabet[d8];

                decodedData[encodedIndex++] = (byte) (b1 << 3 | b2 >> 2);
                decodedData[encodedIndex++] = (byte) (((b2 & 0xf) << 6) | (b3 << 1) | ((b4 >> 4) & 0xf));
                decodedData[encodedIndex++] = (byte) (((b4 & 0xf) << 4) | ((b5 >> 1) & 0xf));
                decodedData[encodedIndex++] = (byte) (((b5 & 0xf) << 7) | (b6 << 2) | ((b7 >> 3) & 0xf));
                decodedData[encodedIndex++] = (byte) (b7 << 5 | b8);
            }

            // at least two characters must be data
            if (!isData((d1 = encoded[dataIndex++])) || !isData((d2 = encoded[dataIndex++]))) {
                // if found "no data" just return null
                // todo: error
                return null;
            }
            b1 = base32Alphabet[d1];
            b2 = base32Alphabet[d2];

            // inspect the remaining bits and determine how to handle
            d3 = encoded[dataIndex++];
            d4 = encoded[dataIndex++];
            d5 = encoded[dataIndex++];
            d6 = encoded[dataIndex++];
            d7 = encoded[dataIndex++];
            d8 = encoded[dataIndex];
            if (!isData((d3)) || !isData((d4)) || !isData((d5)) || !isData((d6)) || !isData((d7)) || !isData((d8))) {
                // Check if they are PAD characters
                if (isPad(d3) && isPad(d4) && isPad(d5) && isPad(d6) && isPad(d7) && isPad(d8)) {
                    // Six PAD characters; the last two bits should be zero
                    if ((b2 & 0x3) != 0) {
                        return null;
                    }
                    byte[] tmp = new byte[i * 5 + 1];
                    System.arraycopy(decodedData, 0, tmp, 0, i * 5);
                    tmp[encodedIndex] = (byte) (b1 << 3 | b2 >> 2);
                    return tmp;
                } else if (!isPad(d3) && !isPad(d4) && isPad(d5) && isPad(d6) && isPad(d7) && isPad(d8)) {
                    // Four PAD characters; the last four bits should be zero
                    b3 = base32Alphabet[d3];
                    b4 = base32Alphabet[d4];
                    if ((b4 & 0xf) != 0) {
                        return null;
                    }
                    byte[] tmp = new byte[i * 5 + 2];
                    System.arraycopy(decodedData, 0, tmp, 0, i * 5);
                    tmp[encodedIndex++] = (byte) (b1 << 3 | b2 >> 2);
                    tmp[encodedIndex] = (byte) (((b2 & 0xf) << 6) | (b3 << 1) | ((b4 >> 4) & 0xf));
                    return tmp;
                } else if (!isPad(d3) && !isPad(d4) && !isPad(d5) && isPad(d6) && isPad(d7) && isPad(d8)) {
                    // Three PAD characters; the last one bit should be zero
                    b3 = base32Alphabet[d3];
                    b4 = base32Alphabet[d4];
                    b5 = base32Alphabet[d5];
                    if ((b5 & 0x1) != 0) {
                        return null;
                    }
                    byte[] tmp = new byte[i * 5 + 3];
                    System.arraycopy(decodedData, 0, tmp, 0, i * 5);
                    tmp[encodedIndex++] = (byte) (b1 << 3 | b2 >> 2);
                    tmp[encodedIndex++] = (byte) (((b2 & 0xf) << 6) | (b3 << 1) | ((b4 >> 4) & 0xf));
                    tmp[encodedIndex] = (byte) (((b4 & 0xf) << 4) | ((b5 >> 1) & 0xf));
                    return tmp;
                } else if (!isPad(d3) && !isPad(d4) && !isPad(d5) && !isPad(d6) && !isPad(d7) && isPad(d8)) {
                    // One PAD character; the last three bits should be zero
                    b3 = base32Alphabet[d3];
                    b4 = base32Alphabet[d4];
                    b5 = base32Alphabet[d5];
                    b6 = base32Alphabet[d6];
                    b7 = base32Alphabet[d7];
                    if ((b7 & 0x7) != 0) {
                        return null;
                    }
                    byte[] tmp = new byte[i * 5 + 4];
                    System.arraycopy(decodedData, 0, tmp, 0, i * 5);
                    tmp[encodedIndex++] = (byte) (b1 << 3 | b2 >> 2);
                    tmp[encodedIndex++] = (byte) (((b2 & 0xf) << 6) | (b3 << 1) | ((b4 >> 4) & 0xf));
                    tmp[encodedIndex++] = (byte) (((b4 & 0xf) << 4) | ((b5 >> 1) & 0xf));
                    tmp[encodedIndex] = (byte) (((b5 & 0xf) << 7) | (b6 << 2) | ((b7 >> 3) & 0xf));
                    return tmp;
                } else {
                    // todo: an error like MZ=Q====
                    return null;
                }
            } else {
                // No PAD characters; e.g. MZXW6YTB
                b3 = base32Alphabet[d3];
                b4 = base32Alphabet[d4];
                b5 = base32Alphabet[d5];
                b6 = base32Alphabet[d6];
                b7 = base32Alphabet[d7];
                b8 = base32Alphabet[d8];

                decodedData[encodedIndex++] = (byte) (b1 << 3 | b2 >> 2);
                decodedData[encodedIndex++] = (byte) (((b2 & 0xf) << 6) | (b3 << 1) | ((b4 >> 4) & 0xf));
                decodedData[encodedIndex++] = (byte) (((b4 & 0xf) << 4) | ((b5 >> 1) & 0xf));
                decodedData[encodedIndex++] = (byte) (((b5 & 0xf) << 7) | (b6 << 2) | ((b7 >> 3) & 0xf));
                decodedData[encodedIndex] = (byte) (b7 << 5 | b8);
            }

            return decodedData;
        }
    }

    public CodecDecoder newDecoder() {
        return new Base32Hex.Decoder(this);
    }

    private static final byte[] EMPTY_STRING = {};

    /**
     * Encodes hex octects into Base64
     */
    private static class Encoder extends CodecEncoder {
        public Encoder(Codec codec) {
            super(codec);
        }

        public byte[] encode(byte[] bytes) {
            if (bytes == null)
                return null;

            int lengthDataBits = bytes.length * EIGHTBIT;
            if (lengthDataBits == 0) {
                return EMPTY_STRING;
            }

            int fewerThan40bits = lengthDataBits % FORTYBITGROUP;
            int numberQuintets = lengthDataBits / FORTYBITGROUP;
            int numberOctets = fewerThan40bits != 0 ? numberQuintets + 1 : numberQuintets;
            byte encoded[] = new byte[numberOctets * 8];

            byte b1, b2, b3, b4, b5;

            int encodedIndex = 0;
            int dataIndex = 0;

            for (int i = 0; i < numberQuintets; i++) {
                b1 = bytes[dataIndex++];
                b2 = bytes[dataIndex++];
                b3 = bytes[dataIndex++];
                b4 = bytes[dataIndex++];
                b5 = bytes[dataIndex++];

                final byte val1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 3) : (byte) ((b1) >> 3 ^ 0xe0);
                final byte r1 = (byte) (b1 & 0x07);
                final byte val2 = ((b2 & SIGN) == 0) ? (byte) (b2 >> 6) : (byte) ((b2) >> 6 ^ 0xfc);
                final byte val3 = ((b2 & SIGN) == 0) ? (byte) (b2 >> 1) : (byte) ((b2) >> 1 ^ 0xe0);
                final byte r2 = (byte) (b2 & 0x01);
                final byte val4 = ((b3 & SIGN) == 0) ? (byte) (b3 >> 4) : (byte) ((b3) >> 4 ^ 0xf0);
                final byte r3 = (byte) (b3 & 0x0f);
                final byte val5 = ((b4 & SIGN) == 0) ? (byte) (b4 >> 7) : (byte) ((b4) >> 7 ^ 0xfe);
                final byte val6 = ((b4 & SIGN) == 0) ? (byte) (b4 >> 2) : (byte) ((b4) >> 2 ^ 0xe0);
                final byte r4 = (byte) (b4 & 0x03);
                final byte val7 = ((b5 & SIGN) == 0) ? (byte) (b5 >> 5) : (byte) ((b5) >> 5 ^ 0xf8);
                final byte r5 = (byte) (b5 & 0x1f);

                encoded[encodedIndex++] = lookUpBase32Alphabet[val1 & 0x1f];
                encoded[encodedIndex++] = lookUpBase32Alphabet[(r1 << 2) | val2];
                encoded[encodedIndex++] = lookUpBase32Alphabet[val3 & 0x1f];
                encoded[encodedIndex++] = lookUpBase32Alphabet[(r2 << 4) | val4];
                encoded[encodedIndex++] = lookUpBase32Alphabet[(r3 << 1) | val5];
                encoded[encodedIndex++] = lookUpBase32Alphabet[val6 & 0x1f];
                encoded[encodedIndex++] = lookUpBase32Alphabet[(r4 << 3) | val7];
                encoded[encodedIndex++] = lookUpBase32Alphabet[r5 & 0x1f];
            }

            if (fewerThan40bits == EIGHTBIT) {
                // six pad characters
                b1 = bytes[dataIndex];

                final byte val1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 3) : (byte) ((b1) >> 3 ^ 0xe0);
                final byte r1 = (byte) (b1 & 0x07);

                encoded[encodedIndex++] = lookUpBase32Alphabet[val1 & 0x1f];
                encoded[encodedIndex++] = lookUpBase32Alphabet[r1 << 2];
                encoded[encodedIndex++] = PAD;
                encoded[encodedIndex++] = PAD;
                encoded[encodedIndex++] = PAD;
                encoded[encodedIndex++] = PAD;
                encoded[encodedIndex++] = PAD;
                encoded[encodedIndex] = PAD;
            } else if (fewerThan40bits == SIXTEENBIT) {
                // four pad characters
                b1 = bytes[dataIndex++];
                b2 = bytes[dataIndex];

                final byte val1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 3) : (byte) ((b1) >> 3 ^ 0xe0);
                final byte r1 = (byte) (b1 & 0x07);
                final byte val2 = ((b2 & SIGN) == 0) ? (byte) (b2 >> 6) : (byte) ((b2) >> 6 ^ 0xfc);
                final byte val3 = ((b2 & SIGN) == 0) ? (byte) (b2 >> 1) : (byte) ((b2) >> 1 ^ 0xe0);
                final byte r2 = (byte) (b2 & 0x01);

                encoded[encodedIndex++] = lookUpBase32Alphabet[val1 & 0x1f];
                encoded[encodedIndex++] = lookUpBase32Alphabet[(r1 << 2) | val2];
                encoded[encodedIndex++] = lookUpBase32Alphabet[val3 & 0x1f];
                encoded[encodedIndex++] = lookUpBase32Alphabet[r2 << 4];
                encoded[encodedIndex++] = PAD;
                encoded[encodedIndex++] = PAD;
                encoded[encodedIndex++] = PAD;
                encoded[encodedIndex] = PAD;
            } else if (fewerThan40bits == TWENTYFOURBIT) {
                // three pad characters
                b1 = bytes[dataIndex++];
                b2 = bytes[dataIndex++];
                b3 = bytes[dataIndex];

                final byte val1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 3) : (byte) ((b1) >> 3 ^ 0xe0);
                final byte r1 = (byte) (b1 & 0x07);
                final byte val2 = ((b2 & SIGN) == 0) ? (byte) (b2 >> 6) : (byte) ((b2) >> 6 ^ 0xfc);
                final byte val3 = ((b2 & SIGN) == 0) ? (byte) (b2 >> 1) : (byte) ((b2) >> 1 ^ 0xe0);
                final byte r2 = (byte) (b2 & 0x01);
                final byte val4 = ((b3 & SIGN) == 0) ? (byte) (b3 >> 4) : (byte) ((b3) >> 4 ^ 0xf0);
                final byte r3 = (byte) (b3 & 0x0f);

                encoded[encodedIndex++] = lookUpBase32Alphabet[val1 & 0x1f];
                encoded[encodedIndex++] = lookUpBase32Alphabet[(r1 << 2) | val2];
                encoded[encodedIndex++] = lookUpBase32Alphabet[val3 & 0x1f];
                encoded[encodedIndex++] = lookUpBase32Alphabet[(r2 << 4) | val4];
                encoded[encodedIndex++] = lookUpBase32Alphabet[(r3 << 1)];
                encoded[encodedIndex++] = PAD;
                encoded[encodedIndex++] = PAD;
                encoded[encodedIndex] = PAD;
            } else if (fewerThan40bits == THIRTYTWOBIT) {
                // one pad character
                b1 = bytes[dataIndex++];
                b2 = bytes[dataIndex++];
                b3 = bytes[dataIndex++];
                b4 = bytes[dataIndex];

                final byte val1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 3) : (byte) ((b1) >> 3 ^ 0xe0);
                final byte r1 = (byte) (b1 & 0x07);
                final byte val2 = ((b2 & SIGN) == 0) ? (byte) (b2 >> 6) : (byte) ((b2) >> 6 ^ 0xfc);
                final byte val3 = ((b2 & SIGN) == 0) ? (byte) (b2 >> 1) : (byte) ((b2) >> 1 ^ 0xe0);
                final byte r2 = (byte) (b2 & 0x01);
                final byte val4 = ((b3 & SIGN) == 0) ? (byte) (b3 >> 4) : (byte) ((b3) >> 4 ^ 0xf0);
                final byte r3 = (byte) (b3 & 0x0f);
                final byte val5 = ((b4 & SIGN) == 0) ? (byte) (b4 >> 7) : (byte) ((b4) >> 7 ^ 0xfe);
                final byte val6 = ((b4 & SIGN) == 0) ? (byte) (b4 >> 2) : (byte) ((b4) >> 2 ^ 0xe0);
                final byte r4 = (byte) (b4 & 0x03);

                encoded[encodedIndex++] = lookUpBase32Alphabet[val1 & 0x1f];
                encoded[encodedIndex++] = lookUpBase32Alphabet[(r1 << 2) | val2];
                encoded[encodedIndex++] = lookUpBase32Alphabet[val3 & 0x1f];
                encoded[encodedIndex++] = lookUpBase32Alphabet[(r2 << 4) | val4];
                encoded[encodedIndex++] = lookUpBase32Alphabet[(r3 << 1) | val5];
                encoded[encodedIndex++] = lookUpBase32Alphabet[val6 & 0x1f];
                encoded[encodedIndex++] = lookUpBase32Alphabet[(r4 << 3)];
                encoded[encodedIndex] = PAD;
            }
            return encoded;
        }
    }

    public CodecEncoder newEncoder() {
        return new Base32Hex.Encoder(this);
    }
}
