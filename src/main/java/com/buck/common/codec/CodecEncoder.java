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
 * An engine that can transform raw data, represented as a sequence of bytes,
 * to a specific encoding, into a sequence of bytes.
 *
 * @author Robert J. Buck
 */
public abstract class CodecEncoder {

    private final Codec codec;

    /**
     * Initializes a new encoder.
     *
     * @param codec The codec for this encoder.
     */
    protected CodecEncoder(Codec codec) {
        this.codec = codec;
    }

    /**
     * Returns the codec that created this encoder.
     *
     * @return This encoder's codec
     */
    public final Codec codec() {
        return codec;
    }

    /**
     * Encodes raw data using the codec.
     *
     * @param bytes the raw data
     * @return the encoded byte array
     */
    public abstract byte[] encode(byte[] bytes);
}
