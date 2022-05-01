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
