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
 * Codec for RFC 4648 Base64 URL Safe.
 * <p/>
 * This implementation does not encode/decode streaming
 * data. You need the data that you will encode/decode
 * already on a byte array.
 *
 * @author Robert J. Buck
 */
public class Base64URL extends Codec {

    static private final int BASELENGTH = 128;
    static private final int LOOKUPLENGTH = 64;
    static private final int TWENTYFOURBITGROUP = 24;
    static private final int EIGHTBIT = 8;
    static private final int SIXTEENBIT = 16;
    static private final int FOURBYTE = 4;
    static private final int SIGN = -128;
    static private final char PAD = '=';
    static final private byte[] base64Alphabet = new byte[BASELENGTH];
    static final private byte[] lookUpBase64Alphabet = new byte[LOOKUPLENGTH];

    static {

        for (int i = 0; i < BASELENGTH; ++i) {
            base64Alphabet[i] = -1;
        }
        for (int i = 'Z'; i >= 'A'; i--) {
            base64Alphabet[i] = (byte) (i - 'A');
        }
        for (int i = 'z'; i >= 'a'; i--) {
            base64Alphabet[i] = (byte) (i - 'a' + 26);
        }

        for (int i = '9'; i >= '0'; i--) {
            base64Alphabet[i] = (byte) (i - '0' + 52);
        }

        base64Alphabet['-'] = 62;
        base64Alphabet['_'] = 63;

        for (int i = 0; i <= 25; i++) {
            lookUpBase64Alphabet[i] = (byte) ('A' + i);
        }

        for (int i = 26, j = 0; i <= 51; i++, j++) {
            lookUpBase64Alphabet[i] = (byte) ('a' + j);
        }

        for (int i = 52, j = 0; i <= 61; i++, j++) {
            lookUpBase64Alphabet[i] = (byte) ('0' + j);
        }
        lookUpBase64Alphabet[62] = '-';
        lookUpBase64Alphabet[63] = '_';

    }

    private static boolean isWhiteSpace(byte octet) {
        return (octet == 0x20 || octet == 0xd || octet == 0xa || octet == 0x9);
    }

    private static boolean isPad(byte octet) {
        return (octet == PAD);
    }

    private static boolean isData(byte octet) {
        return ((0xff & octet) < BASELENGTH && base64Alphabet[(0xff & octet)] != -1);
    }

    protected static boolean isBase64(byte octet) {
        return (isWhiteSpace(octet) || isPad(octet) || isData(octet));
    }

    /**
     * remove WhiteSpace from MIME containing encoded Base64 data.
     *
     * @param data the byte array of base64 data (with WS)
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

    public Base64URL() {
        super("base64url", StandardCodecs.aliases_Base64URL);
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

            if (len % FOURBYTE != 0) {
                //should be divisible by four
                return null;
            }

            int numberQuadruple = (len / FOURBYTE);

            if (numberQuadruple == 0) {
                return new byte[0];
            }

            byte b1, b2, b3, b4;
            byte d1, d2, d3, d4;

            int i = 0;
            int encodedIndex = 0;
            int dataIndex = 0;
            byte[] decodedData = new byte[(numberQuadruple) * 3];

            for (; i < numberQuadruple - 1; i++) {

                if (!isData((d1 = encoded[dataIndex++])) ||
                        !isData((d2 = encoded[dataIndex++])) ||
                        !isData((d3 = encoded[dataIndex++])) ||
                        !isData((d4 = encoded[dataIndex++]))) {
                    return null;//if found "no data" just return null
                }

                b1 = base64Alphabet[d1];
                b2 = base64Alphabet[d2];
                b3 = base64Alphabet[d3];
                b4 = base64Alphabet[d4];

                decodedData[encodedIndex++] = (byte) (b1 << 2 | b2 >> 4);
                decodedData[encodedIndex++] = (byte) (((b2 & 0xf) << 4) | ((b3 >> 2) & 0xf));
                decodedData[encodedIndex++] = (byte) (b3 << 6 | b4);
            }

            if (!isData((d1 = encoded[dataIndex++])) ||
                    !isData((d2 = encoded[dataIndex++]))) {
                return null;//if found "no data" just return null
            }

            b1 = base64Alphabet[d1];
            b2 = base64Alphabet[d2];

            d3 = encoded[dataIndex++];
            d4 = encoded[dataIndex];
            if (!isData((d3)) || !isData((d4))) {
                //Check if they are PAD characters

                //Two PAD e.g. 3c[Pad][Pad]
                if (isPad(d3) && isPad(d4)) {
                    // last 4 bits should be zero
                    if ((b2 & 0xf) != 0) {
                        return null;
                    }
                    byte[] tmp = new byte[i * 3 + 1];
                    System.arraycopy(decodedData, 0, tmp, 0, i * 3);
                    tmp[encodedIndex] = (byte) (b1 << 2 | b2 >> 4);
                    return tmp;
                } else if (!isPad(d3) && isPad(d4)) {
                    // >> One PAD  e.g. 3cQ[Pad]
                    b3 = base64Alphabet[d3];
                    // last 2 bits should be zero
                    if ((b3 & 0x3) != 0) {
                        return null;
                    }
                    byte[] tmp = new byte[i * 3 + 2];
                    System.arraycopy(decodedData, 0, tmp, 0, i * 3);
                    tmp[encodedIndex++] = (byte) (b1 << 2 | b2 >> 4);
                    tmp[encodedIndex] = (byte) (((b2 & 0xf) << 4) | ((b3 >> 2) & 0xf));
                    return tmp;
                } else {
                    return null;//an error  like "3c[Pad]r", "3cdX", "3cXd", "3cXX" where X is non data
                }
            } else { //No PAD e.g 3cQl
                b3 = base64Alphabet[d3];
                b4 = base64Alphabet[d4];
                decodedData[encodedIndex++] = (byte) (b1 << 2 | b2 >> 4);
                decodedData[encodedIndex++] = (byte) (((b2 & 0xf) << 4) | ((b3 >> 2) & 0xf));
                decodedData[encodedIndex] = (byte) (b3 << 6 | b4);
            }

            return decodedData;
        }
    }

    public CodecDecoder newDecoder() {
        return new Decoder(this);
    }

    private static final byte[] EMPTY_STRING = {};

    /**
     * Encodes hex octets into Base64
     */
    private static class Encoder extends CodecEncoder {
        public Encoder(Codec codec) {
            super(codec);
        }

        public byte[] encode(byte[] binary) {
            if (binary == null) {
                return null;
            }

            int lengthDataBits = binary.length * EIGHTBIT;
            if (lengthDataBits == 0) {
                return EMPTY_STRING;
            }

            int fewerThan24bits = lengthDataBits % TWENTYFOURBITGROUP;
            int numberTriplets = lengthDataBits / TWENTYFOURBITGROUP;
            int numberQuartet = fewerThan24bits != 0 ? numberTriplets + 1 : numberTriplets;
            byte encoded[] = new byte[numberQuartet * 4];

            byte k, l, b1, b2, b3;

            int encodedIndex = 0;
            int dataIndex = 0;

            for (int i = 0; i < numberTriplets; i++) {
                b1 = binary[dataIndex++];
                b2 = binary[dataIndex++];
                b3 = binary[dataIndex++];

                l = (byte) (b2 & 0x0f);
                k = (byte) (b1 & 0x03);

                byte val1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 2) : (byte) ((b1) >> 2 ^ 0xc0);

                byte val2 = ((b2 & SIGN) == 0) ? (byte) (b2 >> 4) : (byte) ((b2) >> 4 ^ 0xf0);
                byte val3 = ((b3 & SIGN) == 0) ? (byte) (b3 >> 6) : (byte) ((b3) >> 6 ^ 0xfc);

                encoded[encodedIndex++] = lookUpBase64Alphabet[val1];
                encoded[encodedIndex++] = lookUpBase64Alphabet[val2 | (k << 4)];
                encoded[encodedIndex++] = lookUpBase64Alphabet[(l << 2) | val3];
                encoded[encodedIndex++] = lookUpBase64Alphabet[b3 & 0x3f];
            }

            // form integral number of 6-bit groups
            if (fewerThan24bits == EIGHTBIT) {
                b1 = binary[dataIndex];
                k = (byte) (b1 & 0x03);

                byte val1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 2) : (byte) ((b1) >> 2 ^ 0xc0);
                encoded[encodedIndex++] = lookUpBase64Alphabet[val1];
                encoded[encodedIndex++] = lookUpBase64Alphabet[k << 4];
                encoded[encodedIndex++] = PAD;
                encoded[encodedIndex] = PAD;
            } else if (fewerThan24bits == SIXTEENBIT) {
                b1 = binary[dataIndex];
                b2 = binary[dataIndex + 1];
                l = (byte) (b2 & 0x0f);
                k = (byte) (b1 & 0x03);

                byte val1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 2) : (byte) ((b1) >> 2 ^ 0xc0);
                byte val2 = ((b2 & SIGN) == 0) ? (byte) (b2 >> 4) : (byte) ((b2) >> 4 ^ 0xf0);

                encoded[encodedIndex++] = lookUpBase64Alphabet[val1];
                encoded[encodedIndex++] = lookUpBase64Alphabet[val2 | (k << 4)];
                encoded[encodedIndex++] = lookUpBase64Alphabet[l << 2];
                encoded[encodedIndex] = PAD;
            }

            return encoded;
        }
    }

    public CodecEncoder newEncoder() {
        return new Encoder(this);
    }
}
