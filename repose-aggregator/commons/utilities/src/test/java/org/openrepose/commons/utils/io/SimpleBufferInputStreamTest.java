/*
 *  Copyright (c) 2015 Rackspace US, Inc.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openrepose.commons.utils.io;

import org.openrepose.commons.utils.io.buffer.ByteBuffer;
import org.openrepose.commons.utils.io.buffer.CyclicByteBuffer;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;

@RunWith(Enclosed.class)
public class SimpleBufferInputStreamTest {

    public static class WhenReadingFromBufferStream {

        @Test
        public void shouldReadTillBufferIsEmpty() throws Exception {
             final ByteBuffer sbb = new CyclicByteBuffer();
             sbb.put("expected".getBytes());
             
             final InputStream is = new ByteBufferInputStream(sbb);
             
             final byte[] bytes = new byte[1024];
             int b, i;
             
             for (i = 0; (b = is.read()) != -1; i++) {
                 bytes[i] = (byte) b;
             }
             
             assertEquals("expected", new String(bytes, 0, i));
        }

        @Test
        public void shouldReadTillBufferIsEmptyUsingByteArrayRead() throws Exception {
             final ByteBuffer sbb = new CyclicByteBuffer();
             sbb.put("expected".getBytes());
             
             final InputStream is = new ByteBufferInputStream(sbb);
             
             final byte[] bytes = new byte[1024];
             int read = is.read(bytes);
             
             assertEquals("expected", new String(bytes, 0, read));
        }
    }
}
