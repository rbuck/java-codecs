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
import java.util.Set;

/**
 * Tests for URLEncoded encodings.
 *
 * @author Robert J. Buck
 */
public class URLEncodedTestCase {
    private void assertMalformed(byte[] encoded) {
        Codec codec = Codec.forName("www-form-urlencoded");
        boolean failed = false;
        try {
            codec.newDecoder().decode(encoded);
        } catch (MalformedInputException e) {
            failed = true;
        }
        Assert.assertTrue("Malformed URLEncoded", failed);
    }

    private void assertSymmetric(byte[] encoded) {
        Codec codec = Codec.forName("www-form-urlencoded");
        byte[] decoded = codec.newDecoder().decode(encoded);
        Assert.assertEquals(encoded.length, decoded.length);
        for (int i = 0; i < decoded.length; i++) {
            Assert.assertEquals(encoded[i], decoded[i]);
        }
    }

    @Test
    public void testExistence() {
        {
            Assert.assertTrue(Codec.isSupported("x-www-form-urlencoded"));
        }
        {
            Set<String> aliases = Codec.forName("x-www-form-urlencoded").aliases();
            Assert.assertTrue(aliases.contains("www-form-urlencoded"));
        }
    }

    @Test
    public void testBasic() throws UnsupportedEncodingException {
        {
            Codec codec = Codec.forName("x-www-form-urlencoded");
            CodecEncoder enc = codec.newEncoder();
            Assert.assertSame(codec, enc.codec());
            CodecDecoder dec = codec.newDecoder();
            Assert.assertSame(codec, dec.codec());
        }
        {
            Codec codec = Codec.forName("x-www-form-urlencoded");
            CodecEncoder enc = codec.newEncoder();
            byte[] arre = enc.encode(null);
            Assert.assertNull(arre);
            CodecDecoder dec = codec.newDecoder();
            byte[] arrd = dec.decode(null);
        }
        {
            final String in = "http://www.example.com/you & I 10%? wierd & wierder";
            final String ex = "http://www.example.com/you%20&%20I%2010%25?%20wierd%20&%20wierder";
            Codec codec = Codec.forName("x-www-form-urlencoded");
            CodecEncoder enc = codec.newEncoder();
            byte[] arren = enc.encode(in.getBytes("US-ASCII"));
            final String ens = new String(arren, "US-ASCII");
            CodecDecoder dec = codec.newDecoder();
        }
        {
            String tv1 = "%";
            assertMalformed(tv1.getBytes("US-ASCII"));
        }
        {
            String tv1 = "%A";
            assertMalformed(tv1.getBytes("US-ASCII"));
        }
        {
            String tv1 = "%xy";
            assertMalformed(tv1.getBytes("US-ASCII"));
        }
        {
            String tv1 = "#";
            assertSymmetric(tv1.getBytes("US-ASCII"));
        }
        {
            String tv1 = "X?Y";
            assertSymmetric(tv1.getBytes("US-ASCII"));
        }
        {
            // malformed input: escape sequence
            byte[][] tests = {
                    {'%', 'E', '@'},
            };
            Codec codec = Codec.forName("x-www-form-urlencoded");
            for (byte[] test : tests) {
                boolean caught = false;
                try {
                    CodecDecoder dec = codec.newDecoder();
                    dec.decode(test);
                } catch (MalformedInputException e) {
                    caught = true;
                }
                Assert.assertTrue("malformed input escape sequence", caught);
            }
        }
        {
            final String ids = "two+words%0D%0A";
            final String eds = "two words\r\n";
            Codec codec = Codec.forName("x-www-form-urlencoded");
            CodecDecoder dec = codec.newDecoder();
            final byte[] dda = dec.decode(ids.getBytes("US-ASCII"));
            Assert.assertEquals(eds, new String(dda, "US-ASCII"));
            CodecEncoder enc = codec.newEncoder();
            final byte[] eda = enc.encode(dda);
            Assert.assertEquals(ids, new String(eda, "US-ASCII"));
        }
    }
}
