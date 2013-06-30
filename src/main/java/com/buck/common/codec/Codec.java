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

import com.buck.common.codec.spi.CodecProvider;
import com.buck.commons.i18n.ResourceBundle;
import sun.misc.ASCIICaseInsensitiveComparator;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;

/**
 * A named mapping between encoded sequences of bytes and raw binary data. This
 * class defines methods for creating decoders and encoders and for retrieving
 * the various names associated with a codec.  Instances of this class are
 * immutable.
 * <p/>
 * <a name="names"><a name="codec">
 * <h4>Codec names</h4>
 * <p/>
 * <p> Codecs are named by strings composed of the following characters:
 * <p/>
 * <ul>
 * <p/>
 * <li> The uppercase letters <tt>'A'</tt> through <tt>'Z'</tt>
 * (<tt>'&#92;u0041'</tt>&nbsp;through&nbsp;<tt>'&#92;u005a'</tt>),
 * <p/>
 * <li> The lowercase letters <tt>'a'</tt> through <tt>'z'</tt>
 * (<tt>'&#92;u0061'</tt>&nbsp;through&nbsp;<tt>'&#92;u007a'</tt>),
 * <p/>
 * <li> The digits <tt>'0'</tt> through <tt>'9'</tt>
 * (<tt>'&#92;u0030'</tt>&nbsp;through&nbsp;<tt>'&#92;u0039'</tt>),
 * <p/>
 * <li> The dash character <tt>'-'</tt>
 * (<tt>'&#92;u002d'</tt>,&nbsp;<small>HYPHEN-MINUS</small>),
 * <p/>
 * <li> The period character <tt>'.'</tt>
 * (<tt>'&#92;u002e'</tt>,&nbsp;<small>FULL STOP</small>),
 * <p/>
 * <li> The colon character <tt>':'</tt>
 * (<tt>'&#92;u003a'</tt>,&nbsp;<small>COLON</small>), and
 * <p/>
 * <li> The underscore character <tt>'_'</tt>
 * (<tt>'&#92;u005f'</tt>,&nbsp;<small>LOW&nbsp;LINE</small>).
 * <p/>
 * </ul>
 * <p/>
 * <h4>Standard codecs</h4>
 * <p/>
 * <p> Codecs supported by the Codecs Library support the
 * following standard codecs.  Consult the release documentation for your
 * implementation to see if any other codecs are supported.  The behavior
 * of such optional codecs may differ between implementations.
 * <p/>
 * <blockquote><table width="80%" summary="Description of standard codecs">
 * <tr><th><p align="left">Codec</p></th><th><p align="left">Description</p></th></tr>
 * <tr><td valign=top><tt>Base16</tt></td>
 * <td>Defined in <a href="http://ietf.org/rfc/rfc4648.txt"><i>RFC&nbsp;4648</i></a>,
 * this codec, referred to as "base16" or "hex", is the standard case-insensitive hex
 * encoding. Unlike base32 or base64, base16 requires no special padding since a
 * full code word is always available.</td></tr>
 * <tr><td valign=top><tt>Base32</tt></td>
 * <td>Defined in <a href="http://ietf.org/rfc/rfc4648.txt"><i>RFC&nbsp;4648</i></a>,
 * this codec, referred to as "base32", uses an alphabet that may be handled by humans;
 * where the characters "0" and "O" are easily confused, as are "1", "l", and "I",
 * the base32 alphabet omits 0 (zero) and 1 (one).</td></tr>
 * <tr><td valign=top><tt>Base32 Extended Hex Alphabet</tt></td>
 * <td>Defined in <a href="http://ietf.org/rfc/rfc4648.txt"><i>RFC&nbsp;4648</i></a>,
 * this codec, referred to as "base32hex", uses an alphabet that causes confusion by
 * humans due to its use of 0 (zero) and 1 (one). However, one property with this
 * alphabet, which the base64 and base32 alphabets lack, is that encoded data
 * maintains its sort order when the encoded data is compared bit-wise.</td></tr>
 * <tr><td valign=top><tt>Base64</tt></td>
 * <td>Defined in <a href="http://ietf.org/rfc/rfc4648.txt"><i>RFC&nbsp;4648</i></a>,
 * this codec, referred to as "base64", the encoding is designed to represent
 * arbitrary sequences of octets in a form that allows the use of both upper- and
 * lowercase letters but that need not be human readable.</td></tr>
 * <tr><td valign=top><tt>Base64 URL</tt></td>
 * <td>Defined in <a href="http://ietf.org/rfc/rfc4648.txt"><i>RFC&nbsp;4648</i></a>,
 * this codec, referred to as "base64url", is identical to base64, except that it uses
 * an alphabet that is safe for use in URL and filenames.</td></tr>
 * <tr><td valign=top><tt>Percent Encoded</tt></td>
 * <td>Defined in <a href="http://ietf.org/rfc/rfc3986.txt"><i>RFC&nbsp;3986</i></a>,
 * this codec, referred to as "percent-encoded", is similar to URL Encoded, except
 * that it uses an alphabet that is safe for use in URI, according to RFC 3986.
 * Percent-encoding may only be applied to octets prior to producing a URI from its
 * component parts. When encoding URI, percent encoding is preferable over URL
 * encoded schemes.</td></tr>
 * <tr><td valign=top><tt>Quoted Printable</tt></td>
 * <td>Defined in <a href="http://ietf.org/rfc/rfc2045.txt"><i>RFC&nbsp;2045</i></a>,
 * this codec, referred to as "quoted-printable", is intended to represent data that
 * largely consists of octets that correspond to printable characters in the US-ASCII
 * character set. It encodes the data in such a way that the resulting octets are
 * unlikely to be modified by mail transport. If the data being encoded are mostly
 * US-ASCII text, the encoded form of the data remains largely recognizable by humans.</td></tr>
 * <tr><td valign=top><tt>URL Encoded</tt></td>
 * <td>Defined in <a href="http://www.w3.org/MarkUp/html-spec/html-spec_8.html">
 * <i>HTML 2.0 Forms</i></a>, this codec, referred to as "x-www-form-urlencoded", is
 * used primarily for HTML form submission.</td></tr>
 * </table></blockquote>
 *
 * @author Robert J. Buck
 */
public abstract class Codec implements Comparable<Codec> {

    /**
     * The standard set of codecs.
     */
    private static final CodecProvider standardProvider = new StandardCodecs();

    /**
     * Cache of the most-recently-returned codec, along with the name that was
     * used to find it.
     */
    private static volatile Object[] cache = null;

    private static Codec cache(String codecName, Codec codec) {
        cache = new Object[]{codecName, codec};
        return codec;
    }

    /**
     * Prevents recursive provider lookups.
     */
    private static final ThreadLocal<Object> gate = new ThreadLocal<Object>();

    private static Codec lookupViaProviders(final String codecName) {
        if (gate.get() != null) {
            // Avoid recursive provider lookups
            return null;
        }
        try {
            gate.set(gate);
            return AccessController.doPrivileged(new PrivilegedAction<Codec>() {
                public Codec run() {
                    ServiceLoader<CodecProvider> sl = ServiceLoader.load(com.buck.common.codec.spi.CodecProvider.class);
                    for (CodecProvider cp : sl) {
                        Codec codec = cp.codecForName(codecName);
                        if (codec != null) {
                            return codec;
                        }
                    }
                    return null;
                }
            });
        } finally {
            gate.set(null);
        }
    }

    /**
     * Checks that the given string is a legal codec name. </p>
     *
     * @param s A purported codec name
     * @throws IllegalCodecNameException If the given name is not a legal codec
     *                                   name
     */
    private static void checkName(String s) {
        int n = s.length();
        if (n == 0) {
            Object[] arguments = {};
            String message = ResourceBundle.formatResourceBundleMessage(Codec.class,
                    "CODEC_ILLEGAL_CODEC_NAME_ZERO_LENGTH", arguments);
            throw new IllegalCodecNameException(message);
        }
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            if (c >= 'A' && c <= 'Z') continue;
            if (c >= 'a' && c <= 'z') continue;
            if (c >= '0' && c <= '9') continue;
            if (c == '-') continue;
            if (c == ':') continue;
            if (c == '_') continue;
            if (c == '.') continue;
            Object[] arguments = {s};
            String message = ResourceBundle.formatResourceBundleMessage(Codec.class,
                    "CODEC_ILLEGAL_CODEC_NAME", arguments);
            throw new IllegalCodecNameException(message);
        }
    }

    private static Codec lookup(String codecName) {
        if (codecName == null) {
            Object[] arguments = {};
            String message = ResourceBundle.formatResourceBundleMessage(Codec.class,
                    "CODEC_ILLEGAL_CODEC_NAME_IS_NULL", arguments);
            throw new IllegalCodecNameException(message);
        }
        Object[] ca = cache;
        if ((ca != null) && ca[0].equals(codecName)) {
            return (Codec) ca[1];
        }
        Codec codec = standardProvider.codecForName(codecName);
        if (codec != null) {
            return cache(codecName, codec);
        }
        codec = lookupViaProviders(codecName);
        if (codec != null) {
            return cache(codecName, codec);
        }
        // Only need to check the name if we didn't find a codec for it
        checkName(codecName);
        return null;
    }

    /**
     * Returns a codec object for the named codec. </p>
     *
     * @param codecName The name of the requested codec; may be either a
     *                  canonical name or an alias
     * @return A codec object for the named codec
     * @throws IllegalCodecNameException If the given codec name is illegal
     * @throws UnsupportedCodecException If no support for the given codec is
     *                                   available in this instance of the
     *                                   virtual machine; the name is legal.
     */
    public static Codec forName(String codecName) {
        Codec codec = lookup(codecName);
        if (codec != null) {
            return codec;
        }
        Object[] arguments = {codecName};
        String message = ResourceBundle.formatResourceBundleMessage(Codec.class,
                "CODEC_ILLEGAL_CODEC_NAME", arguments);
        throw new UnsupportedCodecException(message);
    }

    /**
     * Tells whether the named codec is supported. </p>
     *
     * @param codecName The name of the requested codec; may be either a
     *                  canonical name or an alias
     * @return <tt>true</tt> if, and only if, support for the named codec is
     *         available in the current Java virtual machine
     * @throws IllegalCodecNameException If the given codec name is illegal
     */
    public static boolean isSupported(String codecName) {
        return (lookup(codecName) != null);
    }

    private final String name;
    private final String[] aliases;
    private Set<String> aliasSet;

    /**
     * Initializes a new codec with the given canonical name and alias set.
     * </p>
     *
     * @param canonicalName The canonical name of this codec
     * @param aliases       An array of this codec's aliases, or null if it has
     *                      no aliases
     * @throws IllegalCodecNameException If the canonical name or any of the
     *                                   aliases are illegal
     */
    protected Codec(String canonicalName, String[] aliases) {
        checkName(canonicalName);
        String[] as = (aliases == null) ? new String[0] : aliases;
        for (String alias : as) {
            checkName(alias);
        }
        this.name = canonicalName;
        this.aliases = as;
    }

    /**
     * Returns a set containing this codec's aliases. </p>
     *
     * @return An immutable set of this codec's aliases
     */
    public final Set<String> aliases() {
        if (aliasSet != null) {
            return aliasSet;
        }
        int n = aliases.length;
        HashSet<String> hs = new HashSet<String>(n);
        for (int i = 0; i < n; i++) {
            hs.add(aliases[i]);
        }
        aliasSet = Collections.unmodifiableSet(hs);
        return aliasSet;
    }

    /**
     * Compares this codec to another. <p/> <p> Codecs are ordered by their
     * canonical names, without regard to case. </p>
     *
     * @param that The codec to which this codec is to be compared
     * @return A negative integer, zero, or a positive integer as this codec is
     *         less than, equal to, or greater than the specified codec
     */
    public final int compareTo(Codec that) {
        return (name().compareToIgnoreCase(that.name()));
    }

    /**
     * Computes a hashcode for this codec. </p>
     *
     * @return An integer hashcode
     */
    public final int hashCode() {
        return name().hashCode();
    }

    /**
     * Tells whether or not this object is equal to another. <p/> <p> Two codecs
     * are equal if, and only if, they have the same canonical names.  A codec
     * is never equal to any other type of object.  </p>
     *
     * @return <tt>true</tt> if, and only if, this codec is equal to the given
     *         object
     */
    @SuppressWarnings({"SimplifiableIfStatement"})
    public final boolean equals(Object ob) {
        if (!(ob instanceof Codec)) {
            return false;
        }
        if (this == ob) {
            return true;
        }
        return name.equals(((Codec) ob).name());
    }

    /**
     * Returns this codec's canonical name. </p>
     *
     * @return The canonical name of this codec
     */
    public final String name() {
        return name;
    }

    /**
     * Fold codecs from the given iterator into the given map, ignoring codecs
     * whose names already have entries in the map.
     *
     * @param i The Iterator over the codecs.
     * @param m The map of codec names to codec objects.
     */
    private static void put(Iterator<Codec> i, Map<String, Codec> m) {
        while (i.hasNext()) {
            Codec codec = i.next();
            if (!m.containsKey(codec.name()))
                m.put(codec.name(), codec);
        }
    }

    /**
     * Constructs a sorted map from canonical codec names to codec objects. <p/>
     * <p> The map returned by this method will have one entry for each codec
     * for which support is available in the current Java virtual machine.  If
     * two or more supported codecs have the same canonical name then the
     * resulting map will contain just one of them; which one it will contain is
     * not specified. </p> <p/> <p> The invocation of this method, and the
     * subsequent use of the resulting map, may cause time-consuming disk or
     * network I/O operations to occur.  This method is provided for
     * applications that need to enumerate all of the available codecs, for
     * example to allow user codec selection.  This method is not used by the
     * {@link #forName forName} method, which instead employs an efficient
     * incremental lookup algorithm. <p/> <p> This method may return different
     * results at different times if new codec providers are dynamically made
     * available to the current Java virtual machine.  In the absence of such
     * changes, the codecs returned by this method are exactly those that can be
     * retrieved via the {@link #forName forName} method.  </p>
     *
     * @return An immutable, case-insensitive map from canonical codec names to
     *         codec objects
     */
    public static SortedMap<String, Codec> availableCodecs() {
        return AccessController.doPrivileged(new PrivilegedAction<SortedMap<String, Codec>>() {
            public SortedMap<String, Codec> run() {
                TreeMap<String, Codec> m = new TreeMap<String, Codec>(ASCIICaseInsensitiveComparator.CASE_INSENSITIVE_ORDER);
                put(standardProvider.codecs(), m);
                ServiceLoader<CodecProvider> sl = ServiceLoader.load(com.buck.common.codec.spi.CodecProvider.class);
                for (CodecProvider cp : sl) {
                    put(cp.codecs(), m);
                }
                return Collections.unmodifiableSortedMap(m);
            }
        });
    }

    /**
     * Constructs a new decoder for this codec. </p>
     *
     * @return A new decoder for this codec
     */
    public abstract CodecDecoder newDecoder();

    /**
     * Constructs a new encoder for this codec. </p>
     *
     * @return A new encoder for this codec
     */
    public abstract CodecEncoder newEncoder();
}
