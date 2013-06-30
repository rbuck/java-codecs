Java Codecs
===========

[<img src="https://api.travis-ci.org/rbuck/java-codecs.png?branch=master" alt="Build Status" />](http://travis-ci.org/rbuck/java-codecs)

# Description

Codecs provide a named mapping between encoded sequences of bytes and binary
representations. The Codecs class defines methods for creating decoders and
encoders and for retrieving the various names associated with a codec.

This project provides out of the box several codecs, and a facility to extend
its capabilities through an SPI interface.

## Codec names

Codecs are named by strings composed of the following characters:

    The uppercase letters 'A' through 'Z' ('\u0041' through '\u005a'),
    The lowercase letters 'a' through 'z' ('\u0061' through '\u007a'),
    The digits '0' through '9' ('\u0030' through '\u0039'),
    The dash character '-' ('\u002d', HYPHEN-MINUS),
    The period character '.' ('\u002e', FULL STOP),
    The colon character ':' ('\u003a', COLON), and
    The underscore character '_' ('\u005f', LOW LINE).

## Standard codecs

Codecs supported by the Codecs Library support the following standard codecs.
Consult the release documentation for your implementation to see if any other
codecs are supported. The behavior of such optional codecs may differ between
implementations.

Codec | Description
------|------------
Base16 | Defined in RFC 4648, this codec, referred to as "base16" or "hex", is the standard case-insensitive hex encoding. Unlike base32 or base64, base16 requires no special padding since a full code word is always available. |
Base32 | Defined in RFC 4648, this codec, referred to as "base32", uses an alphabet that may be handled by humans; where the characters "0" and "O" are easily confused, as are "1", "l", and "I", the base32 alphabet omits 0 (zero) and 1 (one).
Base32 Extended Hex Alphabet | Defined in RFC 4648, this codec, referred to as "base32hex", uses an alphabet that causes confusion by humans due to its use of 0 (zero) and 1 (one). However, one property with this alphabet, which the base64 and base32 alphabets lack, is that encoded data maintains its sort order when the encoded data is compared bit-wise.
Base64 | Defined in RFC 4648, this codec, referred to as "base64", the encoding is designed to represent arbitrary sequences of octets in a form that allows the use of both upper- and lowercase letters but that need not be human readable.
Base64 URL |Defined in RFC 4648, this codec, referred to as "base64url", is identical to base64, except that it uses an alphabet that is safe for use in URL and filenames.
Percent Encoded | Defined in RFC 3986, this codec, referred to as "percent-encoded", is similar to URL Encoded, except that it uses an alphabet that is safe for use in URI, according to RFC 3986. Percent-encoding may only be applied to octets prior to producing a URI from its component parts. When encoding URI, percent encoding is preferable over URL encoded schemes.
Quoted Printable | Defined in RFC 2045, this codec, referred to as "quoted-printable", is intended to represent data that largely consists of octets that correspond to printable characters in the US-ASCII character set. It encodes the data in such a way that the resulting octets are unlikely to be modified by mail transport. If the data being encoded are mostly US-ASCII text, the encoded form of the data remains largely recognizable by humans. |
URL Encoded | Defined in HTML 2.0 Forms, this codec, referred to as "x-www-form-urlencoded", is used primarily for HTML form submission.

## User Defined Codecs

Codecs may be added by users by implementing the SPI interface;
additional codecs found on the classpath will be picked up
automatically and returned by the Codecs class.

## Using Codecs

Encoding your data is really simple:

        Codec codec = Codec.forName("Base64");
        byte[] toEncoded = codec.newEncoder().encode(inDecoded);

Decoding your data is really simple:

        Codec codec = Codec.forName("Base64");
        byte[] toDecoded = codec.newDecoder().decode(inEncoded);

You can also easily encode UUID, a common use case to create
more compact representations of UUID:

        final long msb = 5226711629596803800L;
        final long lsb = -6266244777592174095L;
        final String eus = "924G5279GL1DHA89QE9I7U69U4";
        UUIDCoder coder = new UUIDCoder(new Base32Hex());
        Assert.assertEquals(eus, new String(coder.encode(new UUID(msb, lsb)), "US-ASCII"));

# Dependencies

The project has the following dependencies:

    Log4j 1.2.17
    Buck Commons 1.0.2

# Build Procedure

To compile and test the project issue the following commands:

    mvn clean install

To release the project issue the following commands:

    mvn release:clean
    mvn release:prepare -Dgpg.passphrase= -Dgpg.keyname=
    mvn release:perform

# License

See the LICENSE file herein.
