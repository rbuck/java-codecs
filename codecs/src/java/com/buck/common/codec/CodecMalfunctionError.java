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

/**
 * Error thrown when the {@link com.buck.common.codec.CodecDecoder#decode decode}
 * method of a {@link com.buck.common.codec.CodecDecoder CodecDecoder}, or the
 * {@link com.buck.common.codec.CodecEncoder#encode encode} method of a
 * {@link com.buck.common.codec.CodecEncoder CodecEncoder}, throws an unexpected
 * exception.
 *
 * @author Robert J. Buck
 */
public class CodecMalfunctionError extends Error {

    /**
     * Serialization version number.
     */
    private static final long serialVersionUID = 9008969166219190293L;

    /**
     * Initializes an instance of this class.
     *
     * @param cause The unexpected exception that was thrown
     */
    public CodecMalfunctionError(Exception cause) {
        super(cause);
    }
}
