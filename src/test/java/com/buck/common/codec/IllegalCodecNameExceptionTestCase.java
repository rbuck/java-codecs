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

/**
 * Tests that illegal codec names are handled properly.
 *
 * @author Robert J. Buck
 */
public class IllegalCodecNameExceptionTestCase {
    @Test
    public void testIllegalCodecNames() {
        {
            boolean caught = false;
            try {
                Codec.forName(null);
            } catch (IllegalCodecNameException e) {
                caught = true;
            }
            Assert.assertTrue("No NULL", caught);
        }
        {
            boolean caught = false;
            try {
                Codec.forName("");
            } catch (IllegalCodecNameException e) {
                caught = true;
            }
            Assert.assertTrue("No EMPTY-STRINGS", caught);
        }
        {
            boolean caught = false;
            try {
                Codec.forName("SUPPORTS_NO_ATS_@");
            } catch (IllegalCodecNameException e) {
                caught = true;
            }
            Assert.assertTrue("No AT-SIGNS", caught);
        }
        {
            boolean caught = false;
            try {
                Codec.forName("SUPPORTS_NO_SLASH_/");
            } catch (IllegalCodecNameException e) {
                caught = true;
            }
            Assert.assertTrue("No SLASH", caught);
        }
        {
            boolean caught = false;
            try {
                Codec.forName("SUPPORTS_NO_STAR_*");
            } catch (IllegalCodecNameException e) {
                caught = true;
            }
            Assert.assertTrue("No STAR", caught);
        }
        {
            boolean caught = false;
            try {
                Codec.forName("SUPPORTS_NO_DOLLAR_$");
            } catch (IllegalCodecNameException e) {
                caught = true;
            }
            Assert.assertTrue("No DOLLAR", caught);
        }
        {
            boolean caught = false;
            try {
                Codec.forName("SUPPORTS_NO_LPAREN_(");
            } catch (IllegalCodecNameException e) {
                caught = true;
            }
            Assert.assertTrue("No LPAREN", caught);
        }
        {
            boolean caught = false;
            try {
                Codec.forName("SUPPORTS_NO_RPAREN_)");
            } catch (IllegalCodecNameException e) {
                caught = true;
            }
            Assert.assertTrue("No RPAREN", caught);
        }
        {
            boolean caught = false;
            try {
                Codec.forName("SUPPORTS_NO_EXCLAMATION_!");
            } catch (IllegalCodecNameException e) {
                caught = true;
            }
            Assert.assertTrue("No EXCLAMATION", caught);
        }
    }
}
