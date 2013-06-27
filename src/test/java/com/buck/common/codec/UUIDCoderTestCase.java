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
import java.util.UUID;

/**
 * Tests the UUIDCoder class.
 *
 * @author Robert J. Buck
 */
public class UUIDCoderTestCase {
    @Test
    public void testUUIDCoder() throws UnsupportedEncodingException {
        final long msb = 5226711629596803800L;
        final long lsb = -6266244777592174095L;
        final String eus = "924G5279GL1DHA89QE9I7U69U4";
        UUIDCoder coder = new UUIDCoder(new Base32Hex());
        Assert.assertEquals(eus, new String(coder.encode(new UUID(msb, lsb)), "US-ASCII"));
    }
}
