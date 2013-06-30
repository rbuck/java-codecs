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

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.UUID;

/**
 * Encodes and decodes UUID objects using a supplied codec.
 *
 * @author Robert J. Buck
 */
public class UUIDCoder {
    /**
     * The base codec to use.
     */
    private final Codec codec;

    /**
     * Constructs a codec to encode UUID object.
     *
     * @param codec the base codec to use.
     */
    public UUIDCoder(Codec codec) {
        this.codec = codec;
    }

    /**
     * Encodes a UUID using the specified codec.
     *
     * @param uuid the UUID to encode
     * @return an encoded UUID
     */
    public byte[] encode(UUID uuid) {
        byte[] bArray = new byte[16];
        ByteBuffer bBuffer = ByteBuffer.wrap(bArray);
        LongBuffer lBuffer = bBuffer.asLongBuffer();
        lBuffer.put(0, uuid.getMostSignificantBits());
        lBuffer.put(1, uuid.getLeastSignificantBits());
        byte[] coded = codec.newEncoder().encode(bArray);
        int idx;
        for (idx = 0; idx < coded.length; idx++) {
            if (coded[idx] == '=') {
                break;
            }
        }
        byte[] result = coded;
        if (idx < coded.length) {
            result = new byte[idx];
            System.arraycopy(coded, 0, result, 0, idx);
        }
        return result;
    }
}
