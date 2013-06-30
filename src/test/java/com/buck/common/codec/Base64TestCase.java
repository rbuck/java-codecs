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

import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.Random;
import java.util.Set;

/**
 * Tests for Base64 encodings.
 *
 * @author Robert J. Buck
 */
public class Base64TestCase {
    private void runTestVector(byte[] expected, byte[] inEncoded) {
        Codec codec = Codec.forName("Base64");
        byte[] toDecoded = codec.newDecoder().decode(inEncoded);
        for (int i = 0; i < toDecoded.length; i++) {
            Assert.assertEquals(expected[i], toDecoded[i]);
        }
        byte[] toEncoded = codec.newEncoder().encode(toDecoded);
        Assert.assertEquals(inEncoded.length, toEncoded.length);
        for (int i = 0; i < toDecoded.length; i++) {
            Assert.assertEquals(inEncoded[i], toEncoded[i]);
        }
    }

    private void runSymmetricTestVector(byte[] bytes) {
        Codec codec = Codec.forName("Base64");
        byte[] out = codec.newDecoder().decode(codec.newEncoder().encode(bytes));
        Assert.assertEquals(bytes.length, out.length);
        for (int i = 0; i < bytes.length; i++) {
            Assert.assertEquals(bytes[i], out[i]);
        }
    }

    @Test
    public void testExistence() {
        {
            Assert.assertTrue(Codec.isSupported("Base64"));
        }
        {
            Set<String> aliases = Codec.forName("Base64").aliases();
            Assert.assertTrue(aliases.contains("base64Binary"));
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    @Test
    public void testBasic() throws UnsupportedEncodingException {
        {
            Codec codec = Codec.forName("Base64");
            CodecEncoder enc = codec.newEncoder();
            Assert.assertSame(codec, enc.codec());
            CodecDecoder dec = codec.newDecoder();
            Assert.assertSame(codec, dec.codec());
        }
        {
            Codec codec = Codec.forName("Base64");
            CodecEncoder enc = codec.newEncoder();
            byte[] arre = enc.encode(null);
            Assert.assertNull(arre);
            CodecDecoder dec = codec.newDecoder();
            byte[] arrd = dec.decode(null);
        }
        {
            byte[] expected = {'f'};
            String inEncoded = "Zg==";
            runTestVector(expected, inEncoded.getBytes("US-ASCII"));
        }
        {
            byte[] expected = {'f', 'o'};
            String inEncoded = "Zm8=";
            runTestVector(expected, inEncoded.getBytes("US-ASCII"));
        }
        {
            byte[] expected = {'f', 'o', 'o'};
            String inEncoded = "Zm9v";
            runTestVector(expected, inEncoded.getBytes("US-ASCII"));
        }
        {
            byte[] expected = {'f', 'o', 'o', 'b'};
            String inEncoded = "Zm9vYg==";
            runTestVector(expected, inEncoded.getBytes("US-ASCII"));
        }
        {
            byte[] expected = {'f', 'o', 'o', 'b', 'a'};
            String inEncoded = "Zm9vYmE=";
            runTestVector(expected, inEncoded.getBytes("US-ASCII"));
        }
        {
            byte[] expected = {'f', 'o', 'o', 'b', 'a', 'r'};
            String inEncoded = "Zm9vYmFy";
            runTestVector(expected, inEncoded.getBytes("US-ASCII"));
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
