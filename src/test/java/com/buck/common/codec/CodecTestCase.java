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

import java.util.SortedMap;

/**
 * Tests the codec class.
 *
 * @author Robert J. Buck
 */
public class CodecTestCase {
    @Test
    public void testCodec() {
        {
            Codec.isSupported("Base32");
        }
        {
            Assert.assertTrue(Codec.forName("Base32").compareTo(Codec.forName("Base64")) < 0);
            Assert.assertTrue(Codec.forName("Base64").compareTo(Codec.forName("Base32")) > 0);
            Assert.assertTrue(Codec.forName("Base64").compareTo(Codec.forName("Base64")) == 0);
        }
        {
            Assert.assertTrue(Codec.forName("Base32").equals(Codec.forName("Base32")));
            Assert.assertFalse(Codec.forName("Base32").equals(Codec.forName("Base64")));
            Assert.assertFalse(Codec.forName("Base32").equals(new Object()));
        }
        {
            SortedMap<String, Codec> codecs = Codec.availableCodecs();
            Assert.assertTrue(codecs.containsKey("Base32"));
        }
    }
}
