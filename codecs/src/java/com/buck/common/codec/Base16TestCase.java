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
import java.util.Random;
import java.util.Set;

/**
 * Tests for Base16 encodings.
 *
 * @author Robert J. Buck
 */
public class Base16TestCase {

    private void runTestVector(byte[] expected, byte[] inEncoded) {
        Codec codec = Codec.forName("Base16");
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
        Codec codec = Codec.forName("Base16");
        byte[] out = codec.newDecoder().decode(codec.newEncoder().encode(bytes));
        Assert.assertEquals(bytes.length, out.length);
        for (int i = 0; i < bytes.length; i++) {
            Assert.assertEquals(bytes[i], out[i]);
        }
    }

    @Test
    public void testExistence() {
        {
            Assert.assertTrue(Codec.isSupported("Base16"));
        }
        {
            Codec codec = Codec.forName("Base16");
            codec.aliases();
            Set<String> aliases = Codec.forName("Base16").aliases();
            Assert.assertTrue(aliases.contains("hex"));
            Assert.assertTrue(aliases.contains("hexBinary"));
        }
    }

    @Test
    public void testBasic() throws UnsupportedEncodingException {
        {
            Codec codec = Codec.forName("Base16");
            CodecEncoder enc = codec.newEncoder();
            Assert.assertSame(codec, enc.codec());
            CodecDecoder dec = codec.newDecoder();
            Assert.assertSame(codec, dec.codec());
        }
        {
            byte[] expected = {'f'};
            String inEncoded = "66";
            runTestVector(expected, inEncoded.getBytes("US-ASCII"));
        }
        {
            byte[] expected = {'f', 'o'};
            String inEncoded = "666F";
            runTestVector(expected, inEncoded.getBytes("US-ASCII"));
        }
        {
            byte[] expected = {'f', 'o', 'o'};
            String inEncoded = "666F6F";
            runTestVector(expected, inEncoded.getBytes("US-ASCII"));
        }
        {
            byte[] expected = {'f', 'o', 'o', 'b'};
            String inEncoded = "666F6F62";
            runTestVector(expected, inEncoded.getBytes("US-ASCII"));
        }
        {
            byte[] expected = {'f', 'o', 'o', 'b', 'a'};
            String inEncoded = "666F6F6261";
            runTestVector(expected, inEncoded.getBytes("US-ASCII"));
        }
        {
            byte[] expected = {'f', 'o', 'o', 'b', 'a', 'r'};
            String inEncoded = "666F6F626172";
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
}
