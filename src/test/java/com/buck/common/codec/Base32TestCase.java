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

package com.buck.common.codec;

import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.Random;
import java.util.Set;

/**
 * Tests for Base32 encodings.
 *
 * @author Robert J. Buck
 */
public class Base32TestCase {
    private void runTestVector(byte[] encoded, byte[] expected) {
        Codec codec = Codec.forName("Base32");
        byte[] decoded = codec.newDecoder().decode(encoded);
        for (int i = 0; i < decoded.length; i++) {
            Assert.assertEquals(expected[i], decoded[i]);
        }
        byte[] recoded = codec.newEncoder().encode(decoded);
        Assert.assertEquals(encoded.length, recoded.length);
        for (int i = 0; i < decoded.length; i++) {
            Assert.assertEquals(encoded[i], recoded[i]);
        }
    }

    private void runHexTestVector(byte[] encoded, byte[] expected) {
        Codec codec = Codec.forName("base32Hex");
        byte[] decoded = codec.newDecoder().decode(encoded);
        for (int i = 0; i < decoded.length; i++) {
            Assert.assertEquals(expected[i], decoded[i]);
        }
        byte[] recoded = codec.newEncoder().encode(decoded);
        Assert.assertEquals(encoded.length, recoded.length);
        for (int i = 0; i < decoded.length; i++) {
            Assert.assertEquals(encoded[i], recoded[i]);
        }
    }

    private void runSymmetricTestVector(byte[] bytes) {
        Codec codec = Codec.forName("base32");
        byte[] out = codec.newDecoder().decode(codec.newEncoder().encode(bytes));
        Assert.assertEquals(bytes.length, out.length);
        for (int i = 0; i < bytes.length; i++) {
            Assert.assertEquals(bytes[i], out[i]);
        }
    }

    @Test
    public void testExistence() {
        {
            Assert.assertTrue(Codec.isSupported("Base32"));
        }
        {
            Set<String> aliases = Codec.forName("Base32").aliases();
            Assert.assertTrue(aliases.isEmpty());
        }
    }

    @Test
    public void testBasic() throws UnsupportedEncodingException {
        {
            Codec codec = Codec.forName("Base32");
            CodecEncoder enc = codec.newEncoder();
            Assert.assertSame(codec, enc.codec());
            CodecDecoder dec = codec.newDecoder();
            Assert.assertSame(codec, dec.codec());
        }
        {
            Codec codec = Codec.forName("Base32");
            CodecEncoder enc = codec.newEncoder();
            byte[] arre = enc.encode(null);
            Assert.assertNull(arre);
            CodecDecoder dec = codec.newDecoder();
            byte[] arrd = dec.decode(null);
        }
        {
            String[][] testVector = {

                    {"", ""},
                    {"MY======", "f"},
                    {"MZXQ====", "fo"},
                    {"MZXW6===", "foo"},
                    {"MZXW6YQ=", "foob"},
                    {"MZXW6YTB", "fooba"},
                    {"MZXW6YTBOI======", "foobar"}
            };
            for (String[] aTestVector : testVector) {
                runTestVector(aTestVector[0].getBytes("US-ASCII"), aTestVector[1].getBytes("US-ASCII"));
            }
        }
        {
            String[][] testVector = {

                    {"", ""},
                    {"CO======", "f"},
                    {"CPNG====", "fo"},
                    {"CPNMU===", "foo"},
                    {"CPNMUOG=", "foob"},
                    {"CPNMUOJ1", "fooba"},
                    {"CPNMUOJ1E8======", "foobar"}
            };
            for (String[] aTestVector : testVector) {
                runHexTestVector(aTestVector[0].getBytes("US-ASCII"), aTestVector[1].getBytes("US-ASCII"));
            }
        }
        {
            Random rnd = new Random();
            for (int i = 0; i < 5000; i++) {
                byte[] bytes = new byte[i % 2029];
                rnd.nextBytes(bytes);
                runSymmetricTestVector(bytes);
            }
        }
    }

    public static byte[] longToByteArray(long l) {
        byte[] bArray = new byte[8];
        ByteBuffer bBuffer = ByteBuffer.wrap(bArray);
        LongBuffer lBuffer = bBuffer.asLongBuffer();
        lBuffer.put(0, l);
        return bArray;
    }
}
