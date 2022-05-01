/**
 *    Copyright 2019 Sven Loesekann
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
       http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.apache.kafka.clients;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class DefaultHostResolver implements HostResolver {

	@Override
	public InetAddress[] resolve(String host) throws UnknownHostException {
		if(host.startsWith("kafkaapp")) {
			InetAddress[] addressArr = new InetAddress[1];
			addressArr[0] = InetAddress.getByAddress(host, InetAddress.getByName("192.168.49.2").getAddress());
			return addressArr;
		}		
		return InetAddress.getAllByName(host);
	}

}
