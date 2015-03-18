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
package org.openrepose.commons.utils.net;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * @author zinic
 */
public final class StaticNetworkNameResolver implements NetworkNameResolver {

   private static final StaticNetworkNameResolver INSTANCE = new StaticNetworkNameResolver();
   
   public static StaticNetworkNameResolver getInstance() {
      return INSTANCE;
   }
   
   private StaticNetworkNameResolver() {
      
   }
   
   @Override
   public InetAddress lookupName(String host) throws UnknownHostException {
      return InetAddress.getByName(host);
   }
}
