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
package org.openrepose.commons.utils.http;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA. User: joshualockwood Date: Apr 21, 2011 Time:
 * 11:50:58 AM
 */
@RunWith(Enclosed.class)
public class CommonHttpHeaderTest {

   public static class WhenGettingHeaderKeys {

      @Test
      public void shouldReturnExpectedKey() {
         assertEquals("retry-after", CommonHttpHeader.RETRY_AFTER.toString());
      }
   }
}
