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

import java.util.Set;

/**
 * Tests the base32hex codec.
 *
 * @author Robert J. Buck
 */
public class Base32HexTestCase {

    @Test
    public void testExistence() {
        {
            Assert.assertTrue(Codec.isSupported("base32Hex"));
        }
        {
            Set<String> aliases = Codec.forName("base32Hex").aliases();
            Assert.assertTrue(aliases.isEmpty());
        }
    }

    @Test
    public void testBasic() {
        {
            Codec codec = Codec.forName("base32Hex");
            CodecEncoder enc = codec.newEncoder();
            Assert.assertSame(codec, enc.codec());
            CodecDecoder dec = codec.newDecoder();
            Assert.assertSame(codec, dec.codec());
        }
        {
            Codec codec = Codec.forName("base32Hex");
            CodecEncoder enc = codec.newEncoder();
            byte[] arre = enc.encode(null);
            Assert.assertNull(arre);
            CodecDecoder dec = codec.newDecoder();
            byte[] arrd = dec.decode(null);
        }
    }
}
