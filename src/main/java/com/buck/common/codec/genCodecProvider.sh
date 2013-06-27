#! /bin/sh
# @(#)genCodecProvider.sh
# Generate a codec provider class

# Required environment variables
#   NAWK	awk tool
#   TMPDIR      temporary directory
#   JAVA	bootstrap JRE launcher

SPEC=$1; shift
DST=$1; shift

eval `$NAWK <$SPEC '
  /^[ \t]*package / { printf "PKG=%s\n", $2; }
  /^[ \t]*class / { printf "CLASS=%s\n", $2; }
  /^[ \t]*id / { printf "ID=%s\n", $2; }
  /^[ \t]*date / { printf "DATE=%s\n", $2; }
'`

OUT=$DST/$CLASS.java
echo '-->' $OUT


# Header
#
cat <<__END__ >$OUT
/**
 * @(#)$CLASS.java	$ID $DATE
 */

// -- This file was mechanically generated: Do not edit! -- //

package $PKG;

import com.buck.common.codec.*;


public class $CLASS extends FastCodecProvider
{

__END__


# Alias tables
#
$NAWK <$SPEC >>$OUT '
  BEGIN { n = 1; m = 1; }

  /^[ \t]*codec / {
    csn = $2; cln = $3;
    lcsn = tolower(csn);
    lcsns[n++] = lcsn;
    csns[lcsn] = csn;
    classMap[lcsn] = cln;
    if (n > 2)
      printf "    };\n\n";
    printf "    static final String[] aliases_%s = new String[] {\n", cln;
  }

  /^[ \t]*alias / {
    acsns[m++] = tolower($2);
    aliasMap[tolower($2)] = lcsn;
    printf "        \"%s\",\n", $2;
  }

  END {
    printf "    };\n\n";
  }
'


# Prehashed alias and class maps
#
$NAWK <$SPEC >$TMPDIR/aliases '
  /^[ \t]*codec / {
    csn = $2;
    lcsn = tolower(csn);
  }
  /^[ \t]*alias / {
    an = tolower($2);
    printf "%-20s \"%s\"\n", an, lcsn;
  }
'

$NAWK <$SPEC >$TMPDIR/classes '
  /^[ \t]*codec / {
    csn = $2; cln = $3;
    lcsn = tolower(csn);
    printf "%-20s \"%s\"\n", lcsn, cln;
  }
'

$JAVA -cp $TMPDIR Hasher -i Aliases <$TMPDIR/aliases >>$OUT
$JAVA -cp $TMPDIR Hasher -i Classes <$TMPDIR/classes >>$OUT
$JAVA -cp $TMPDIR Hasher -i -e Cache -t Codec <$TMPDIR/classes >>$OUT


# Constructor
#
cat <<__END__ >>$OUT
    public $CLASS() {
        super("$PKG", new Aliases(), new Classes(), new Cache());
    }
}
__END__
