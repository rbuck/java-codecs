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
 * An engine that can transform a sequence of bytes in a specific encoding into
 * a sequence of bytes.
 *
 * @author Robert J. Buck
 */
public abstract class CodecDecoder {

    private final Codec codec;

    /**
     * Initializes a new decoder.
     *
     * @param codec The codec for this decoder.
     */
    protected CodecDecoder(Codec codec) {
        this.codec = codec;
    }

    /**
     * Returns the codec that created this decoder.
     *
     * @return This decoder's codec
     */
    public final Codec codec() {
        return codec;
    }

    /**
     * Decodes encoded data using the codec.
     *
     * @param encoded the encoded data
     * @return the decoded byte array
     */
    public abstract byte[] decode(byte[] encoded);
}
