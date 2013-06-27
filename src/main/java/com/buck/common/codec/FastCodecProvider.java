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

import com.buck.common.codec.spi.CodecProvider;

import java.util.Iterator;
import java.util.Map;

/**
 * Abstract base class for fast codec providers.
 *
 * @author Robert J. Buck
 */
public class FastCodecProvider extends CodecProvider {

    // Maps canonical names to class names
    private final Map<String, String> classMap;

    // Maps alias names to canonical names
    private final Map<String, String> aliasMap;

    // Maps canonical names to cached instances
    private final Map<String, Codec> cache;

    private final String packagePrefix;

    protected FastCodecProvider(String pp,
                                Map<String, String> am,
                                Map<String, String> cm,
                                Map<String, Codec> c) {
        packagePrefix = pp;
        aliasMap = am;
        classMap = cm;
        cache = c;
    }

    private String canonicalize(String cn) {
        String an = aliasMap.get(cn);
        return (an != null) ? an : cn;
    }

    private Codec lookup(String codecName) {
        // Canonicalize the name for comparison
        String cn = canonicalize(codecName.toLowerCase());

        // Check cache first
        Codec codec = cache.get(cn);
        if (codec != null) {
            return codec;
        }

        // Do we even support this codec?
        String cln = classMap.get(cn);
        if (cln == null) {
            return null;
        }

        // Instantiate the codec and cache it
        try {
            Class c = Class.forName(packagePrefix + "." + cln,
                    true,
                    this.getClass().getClassLoader());
            codec = (Codec) c.newInstance();
            cache.put(cn, codec);
            return codec;
        } catch (ClassNotFoundException x) {
            return null;
        } catch (IllegalAccessException x) {
            return null;
        } catch (InstantiationException x) {
            return null;
        }
    }

    public final Codec codecForName(String codecName) {
        synchronized (this) {
            return lookup(canonicalize(codecName));
        }
    }

    public final Iterator<Codec> codecs() {

        return new Iterator<Codec>() {

            final Iterator<String> i = classMap.keySet().iterator();

            public boolean hasNext() {
                return i.hasNext();
            }

            public Codec next() {
                String cn = i.next();
                return lookup(cn);
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
