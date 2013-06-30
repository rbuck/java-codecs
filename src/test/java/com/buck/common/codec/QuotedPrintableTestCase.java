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

import java.io.UnsupportedEncodingException;
import java.util.Set;

/**
 * Tests the quoted printable codec.
 *
 * @author Robert J. Buck
 */
public class QuotedPrintableTestCase {

    @Test
    public void testExistence() {
        {
            Assert.assertTrue(Codec.isSupported("quoted-printable"));
        }
        {
            Set<String> aliases = Codec.forName("quoted-printable").aliases();
            Assert.assertTrue(aliases.isEmpty());
        }
    }

    @Test
    public void testBasic() throws UnsupportedEncodingException {
        {
            Codec codec = Codec.forName("quoted-printable");
            CodecEncoder enc = codec.newEncoder();
            Assert.assertSame(codec, enc.codec());
            CodecDecoder dec = codec.newDecoder();
            Assert.assertSame(codec, dec.codec());
        }
        {
            Codec codec = Codec.forName("quoted-printable");
            CodecEncoder enc = codec.newEncoder();
            byte[] arre = enc.encode(null);
            Assert.assertNull(arre);
            CodecDecoder dec = codec.newDecoder();
            byte[] arrd = dec.decode(null);
        }
        {
            // safe chars test
            final String[] safeArr = {
                    "!\"#$%&'()*+,-./0123456789:;<>?@ABCDEFGHIJKLMNO",
                    "PQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~"
            };
            for (String safeDec : safeArr) {
                Codec codec = Codec.forName("quoted-printable");
                CodecEncoder enc = codec.newEncoder();
                byte[] ba = enc.encode(safeDec.getBytes("US-ASCII"));
                String encoded = new String(ba, "US-ASCII");
                Assert.assertEquals(safeDec, encoded);
                CodecDecoder dec = codec.newDecoder();
                String decoded = new String(dec.decode(ba), "US-ASCII");
                Assert.assertEquals(safeDec, decoded);
            }
        }
        {
            final String decoded = "If the data being encoded contains meaningful line breaks, they " +
                    "must be encoded as an ASCII CR LF sequence, not as their original byte values. " +
                    "Conversely if byte values 10 and 13 have meanings other than end of line then " +
                    "they must be encoded as =0A and =0D.\r\n" +
                    "This is paragraph two.";
            final String encoded = "If the data being encoded contains meaningful line breaks, they must be enco=\r\n" +
                    "ded as an ASCII CR LF sequence, not as their original byte values. Converse=\r\n" +
                    "ly if byte values 10 and 13 have meanings other than end of line then they =\r\n" +
                    "must be encoded as =3D0A and =3D0D.\r\n" +
                    "This is paragraph two.";
            Codec codec = Codec.forName("quoted-printable");
            CodecDecoder decoder = codec.newDecoder();
            byte[] dba = decoder.decode(encoded.getBytes("US-ASCII"));
            final String ds = new String(dba, "US-ASCII");
            Assert.assertEquals(decoded, ds);
        }
        {
            final String encoded = "1+1 =3D 2";
            final String decoded = "1+1 = 2";
            Codec codec = Codec.forName("quoted-printable");
            CodecEncoder encoder = codec.newEncoder();
            byte[] eba = encoder.encode(decoded.getBytes("US-ASCII"));
            CodecDecoder decoder = codec.newDecoder();
            byte[] dba = decoder.decode(eba);
            final String ds = new String(dba, "US-ASCII");
            final String es = new String(eba, "US-ASCII");
            Assert.assertEquals(encoded, es);
            Assert.assertEquals(decoded, ds);
        }
        {
            // malformed input: escape sequence
            byte[][] tests = {
            };
            Codec codec = Codec.forName("quoted-printable");
            for (byte[] test : tests) {
                boolean caught = false;
                try {
                    CodecEncoder dec = codec.newEncoder();
                    dec.encode(test);
                } catch (MalformedInputException e) {
                    caught = true;
                }
                Assert.assertTrue("malformed input escape sequence", caught);
            }
        }
        {
            // malformed input: escape sequence
            byte[][] tests = {
                    {'=', 'E', '@'},
                    {127},
                    {'='},
                    {'=', 'A'},
                    {'=', 'W', 'W'}
            };
            Codec codec = Codec.forName("quoted-printable");
            for (byte[] test : tests) {
                boolean caught = false;
                try {
                    CodecDecoder dec = codec.newDecoder();
                    dec.decode(test);
                } catch (MalformedInputException e) {
                    caught = true;
                }
                Assert.assertTrue("malformed input escape sequence", caught);
            }
        }
        {
            final String encoded = "hello= \t\r\n world";
            final String decoded = "hello world";
            Codec codec = Codec.forName("quoted-printable");
            CodecDecoder dec = codec.newDecoder();
            final String dr = new String(dec.decode(encoded.getBytes("US-ASCII")), "US-ASCII");
            Assert.assertEquals(decoded, dr);
        }
        {
            final String encoded = "=41=73=6B=20=4C=65=6F=21";
            final String decoded = "Ask Leo!";
            Codec codec = Codec.forName("quoted-printable");
            CodecDecoder dec = codec.newDecoder();
            final String dr = new String(dec.decode(encoded.getBytes("US-ASCII")), "US-ASCII");
            Assert.assertEquals(decoded, dr);
        }
        {
            final String ods = "Now's the time for all folk to come to the aid of their country.";
            final byte[] oda = ods.getBytes("US-ASCII");
            Codec codec = Codec.forName("quoted-printable");
            CodecEncoder encoder = codec.newEncoder();
            final byte[] fda = encoder.encode(oda);
            final String fds = new String(fda, "US-ASCII");
            Assert.assertEquals(ods, fds);
        }
        {
            final String ids = "Now's the time for all folk to come\r\n to the aid of their country.";
            final byte[] ida = ids.getBytes("US-ASCII");
            Codec codec = Codec.forName("quoted-printable");
            CodecEncoder encoder = codec.newEncoder();
            final byte[] eda = encoder.encode(ida);
            final String eds = new String(eda, "US-ASCII");
            Assert.assertEquals("Now's the time for all folk to come\r\n to the aid of their country.", eds);
            CodecDecoder decoder = codec.newDecoder();
            final byte[] oda = decoder.decode(eda);
            final String ods = new String(oda, "US-ASCII");
            Assert.assertEquals(ids, ods);
        }
        {
            final String ids = "0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF";
            final String eds = "0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789A=\r\nBCDEF";
            Codec codec = Codec.forName("quoted-printable");
            CodecEncoder encoder = codec.newEncoder();
            CodecDecoder decoder = codec.newDecoder();

            byte[] eda = encoder.encode(ids.getBytes("US-ASCII"));
            Assert.assertEquals(eds, new String(eda, "US-ASCII"));
            byte[] oda = decoder.decode(eda);
            Assert.assertEquals(ids, new String(oda, "US-ASCII"));
        }
        {
            final String ids = "0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789=ABCDEF";
            final String eds = "0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789=\r\n" +
                    "=3DABCDEF";
            Codec codec = Codec.forName("quoted-printable");
            CodecEncoder encoder = codec.newEncoder();
            CodecDecoder decoder = codec.newDecoder();

            byte[] eda = encoder.encode(ids.getBytes("US-ASCII"));
            Assert.assertEquals(eds, new String(eda, "US-ASCII"));
            byte[] oda = decoder.decode(eda);
            Assert.assertEquals(ids, new String(oda, "US-ASCII"));
        }
        {
            // should normalize to the expected data string (eds) after the round trip
            final String ids = "hello\rworld";
            final String eds = "hello\r\nworld";
            Codec codec = Codec.forName("quoted-printable");
            CodecEncoder encoder = codec.newEncoder();
            CodecDecoder decoder = codec.newDecoder();

            byte[] eda = encoder.encode(ids.getBytes("US-ASCII"));
            Assert.assertEquals(eds, new String(eda, "US-ASCII"));
            byte[] oda = decoder.decode(eda);
            Assert.assertEquals(eds, new String(oda, "US-ASCII"));
        }
        {
            // should normalize to the expected data string (eds) after the round trip
            final String ids = "hello\nworld";
            final String eds = "hello\r\nworld";
            Codec codec = Codec.forName("quoted-printable");
            CodecEncoder encoder = codec.newEncoder();
            CodecDecoder decoder = codec.newDecoder();

            byte[] eda = encoder.encode(ids.getBytes("US-ASCII"));
            Assert.assertEquals(eds, new String(eda, "US-ASCII"));
            byte[] oda = decoder.decode(eda);
            Assert.assertEquals(eds, new String(oda, "US-ASCII"));
        }
        {
            // should normalize to the expected data string (eds) after the round trip
            final String ids = "hello\r\nworld";
            final String eds = "hello\r\nworld";
            Codec codec = Codec.forName("quoted-printable");
            CodecEncoder encoder = codec.newEncoder();
            CodecDecoder decoder = codec.newDecoder();

            byte[] eda = encoder.encode(ids.getBytes("US-ASCII"));
            Assert.assertEquals(eds, new String(eda, "US-ASCII"));
            byte[] oda = decoder.decode(eda);
            Assert.assertEquals(eds, new String(oda, "US-ASCII"));
        }
    }
}
