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
import java.util.Set;

/**
 * Tests for PercentEncoded encodings.
 *
 * @author Robert J. Buck
 */
public class PercentEncodedTestCase {

    private void assertMalformed(byte[] encoded) {
        Codec codec = Codec.forName("pct-encoded");
        boolean failed = false;
        try {
            codec.newDecoder().decode(encoded);
        } catch (MalformedInputException e) {
            failed = true;
        }
        Assert.assertTrue("Malformed URLEncoded", failed);
    }

    private void assertSymmetric(byte[] encoded) {
        Codec codec = Codec.forName("pct-encoded");
        byte[] decoded = codec.newDecoder().decode(encoded);
        byte[] result = codec.newEncoder().encode(decoded);
        Assert.assertEquals(encoded.length, result.length);
        for (int i = 0; i < encoded.length; i++) {
            Assert.assertEquals(encoded[i], result[i]);
        }
    }

    @Test
    public void testExistence() {
        {
            Assert.assertTrue(Codec.isSupported("pct-encoded"));
        }
        {
            Set<String> aliases = Codec.forName("pct-encoded").aliases();
            Assert.assertTrue(aliases.contains("percent-encoded"));
        }
    }

    @Test
    public void testBasic() throws UnsupportedEncodingException {
        {
            Codec codec = Codec.forName("pct-encoded");
            CodecEncoder enc = codec.newEncoder();
            Assert.assertSame(codec, enc.codec());
            CodecDecoder dec = codec.newDecoder();
            Assert.assertSame(codec, dec.codec());
        }
        {
            Codec codec = Codec.forName("pct-encoded");
            CodecEncoder enc = codec.newEncoder();
            byte[] arre = enc.encode(null);
            Assert.assertNull(arre);
            CodecDecoder dec = codec.newDecoder();
            byte[] arrd = dec.decode(null);
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
            String tv1 = "%20";
            assertSymmetric(tv1.getBytes("US-ASCII"));
        }
        {
            String tv1 = "%F0%90%80%80%F4%8F%BF%BD";
            assertSymmetric(tv1.getBytes("US-ASCII"));
        }
        {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000000; i++) {
                String tv1 = "hello%94%80%EF%98%80%EF%9C%80%EF%A0%80%EF%A4%80%EF%A8";
                assertSymmetric(tv1.getBytes("US-ASCII"));
            }
            long end = System.currentTimeMillis();
            System.out.println("elapsed: " + (end - start));
        }
        {
            // malformed input: escape sequence
            byte[][] tests = {
                    {'%', 'E', '@'},
            };
            Codec codec = Codec.forName("pct-encoded");
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
    }
}
