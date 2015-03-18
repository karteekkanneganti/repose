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
package org.openrepose.core.services.ratelimit.exception;

import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class OverLimitExceptionTest {

    public static class WhenCreatingExceptions {
        private OverLimitException instance;
        private static final String message = "message";
        private static final String user = "user";
        private static final Date date = new Date();
        private static final int limit = 10;
        private static final String configuredLimit = "5";
        

        @Before
        public void setUp() {
            instance = new OverLimitException(message, user, date, limit, configuredLimit);
        }
        
        @Test
        public void shouldStoreMessage() {
            assertEquals(message, instance.getMessage());
        }
        
        @Test
        public void shouldStoreUser() {
            assertEquals(user, instance.getUser());
        }
        
        @Test
        public void shouldStoreNextDate() {
            assertEquals(date, instance.getNextAvailableTime());
        }
        
        @Test
        public void shouldReturnCloneOfDate() {
            instance.getNextAvailableTime().setTime(100);
            assertEquals(date, instance.getNextAvailableTime());
        }
    }

}
