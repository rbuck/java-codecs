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
 * Encodes and decodes data using the Base16 encoding. See RFC 4648 which is
 * available at http://www.ietf.org/rfc/rfc4648.txt
 *
 * @author Robert J. Buck
 */
public class Base16 extends Codec {

    /**
     * The Base16 alphabet according to Section 8 of RFC 4648.
     */
    private static final byte[] base16Alphabet = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    private static final byte[] base16DecodeTable;

    static {
        base16DecodeTable = new byte[128];
        for (int i = 0; i < base16DecodeTable.length; i++) {
            base16DecodeTable[i] = (byte) 0xFF;
        }
        for (int i = 0; i < base16Alphabet.length; i++) {
            base16DecodeTable[base16Alphabet[i]] = (byte) i;
        }
    }

    public Base16() {
        super("base16", StandardCodecs.aliases_Base16);
    }

    /**
     * Decodes a byte array using the Base16 codec.
     */
    private static class Decoder extends CodecDecoder {
        public Decoder(Codec codec) {
            super(codec);
        }

        public byte[] decode(byte[] encoded) {
            final int size = encoded.length;
            byte[] decoded = new byte[size / 2];
            int baIdx = 0;
            for (int caIdx = 0; caIdx < size;) {
                byte c0 = base16DecodeTable[encoded[caIdx++]];
                byte c1 = base16DecodeTable[encoded[caIdx++]];
                decoded[baIdx++] = (byte) ((c0 << 4) | c1);
            }
            return decoded;
        }
    }

    public CodecDecoder newDecoder() {
        return new Decoder(this);
    }

    /**
     * Encodes a byte array using the Base16 codec.
     */
    private static class Encoder extends CodecEncoder {
        public Encoder(Codec codec) {
            super(codec);
        }

        public byte[] encode(byte[] bytes) {
            final int size = bytes.length;
            byte[] encoded = new byte[size * 2];
            int caIdx = 0;
            for (int baIdx = 0; baIdx < size; baIdx++) {
                encoded[caIdx++] = base16Alphabet[((bytes[baIdx] >> 4) & 0x0F)];
                encoded[caIdx++] = base16Alphabet[((bytes[baIdx]) & 0x0F)];
            }
            return encoded;
        }
    }

    public CodecEncoder newEncoder() {
        return new Encoder(this);
    }
}
