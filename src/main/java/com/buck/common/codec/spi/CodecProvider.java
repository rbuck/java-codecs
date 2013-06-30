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

package com.buck.common.codec.spi;

import com.buck.common.codec.Codec;

import java.util.Iterator;

/**
 * Codec service-provider class.
 * <p/>
 * <p> A codec provider is a concrete subclass of this class that has a
 * zero-argument constructor and some number of associated codec
 * implementation classes.  Codec providers may be installed in an instance
 * of the Java platform as extensions, that is, jar files placed into any of
 * the usual extension directories.  Providers may also be made available by
 * adding them to the applet or application class path or by some other
 * platform-specific means.  Codec providers are looked up via the current
 * thread's {@link java.lang.Thread#getContextClassLoader() </code>context
 * class loader<code>}.
 * <p/>
 * <p> A codec provider identifies itself with a provider-configuration file
 * named <tt>com.buck.common.codec.spi.CodecProvider</tt> in the resource
 * directory <tt>META-INF/services</tt>.  The file should contain a list of
 * fully-qualified concrete codec-provider class names, one per line.  A line
 * is terminated by any one of a line feed (<tt>'\n'</tt>), a carriage return
 * (<tt>'\r'</tt>), or a carriage return followed immediately by a line feed.
 * Space and tab characters surrounding each name, as well as blank lines, are
 * ignored.  The comment character is <tt>'#'</tt> (<tt>'&#92;u0023'</tt>); on
 * each line all characters following the first comment character are ignored.
 * The file must be encoded in UTF-8.
 * <p/>
 * <p> If a particular concrete codec provider class is named in more than
 * one configuration file, or is named in the same configuration file more than
 * once, then the duplicates will be ignored.  The configuration file naming a
 * particular provider need not be in the same jar file or other distribution
 * unit as the provider itself.  The provider must be accessible from the same
 * class loader that was initially queried to locate the configuration file;
 * this is not necessarily the class loader that loaded the file. </p>
 *
 * @author Robert J. Buck
 * @see com.buck.common.codec.Codec
 */
public abstract class CodecProvider {

    /**
     * Initializes a new codec provider. </p>
     *
     * @throws SecurityException If a security manager has been installed and it denies
     *                           {@link RuntimePermission}<tt>("codecProvider")</tt>
     */
    protected CodecProvider() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("codecProvider"));
        }
    }

    /**
     * Creates an iterator that iterates over the codecs supported by this
     * provider.  This method is used in the implementation of the {@link
     * com.buck.common.codec.Codec#availableCodecs Codec.availableCodecs}
     * method. </p>
     *
     * @return The new iterator
     */
    public abstract Iterator<Codec> codecs();

    /**
     * Retrieves a codec for the given codec name. </p>
     *
     * @param codecName The name of the requested codec; may be either
     *                  a canonical name or an alias
     * @return A codec object for the named codec,
     *         or <tt>null</tt> if the named codec
     *         is not supported by this provider
     */
    public abstract Codec codecForName(String codecName);
}
