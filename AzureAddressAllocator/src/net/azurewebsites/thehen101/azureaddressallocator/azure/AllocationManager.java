package net.azurewebsites.thehen101.azureaddressallocator.azure;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.azurewebsites.thehen101.azureaddressallocator.Allocator;

/**
 * @author h
 * @since 27 May 2020
 * 
 */
public class AllocationManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(AllocationManager.class);

	private int maxIPAddresses;
	private int allocatedIPAddresses;
	private final Allocator allocator;
	private final ArrayDeque<String> unallocatedIPPool = new ArrayDeque<String>();
	private final HashMap<String, String> allocationMap = new HashMap<String, String>(); // username to private IP

	public AllocationManager(final Allocator allocator) {
		this.allocator = allocator;

		this.unallocatedIPPool.addAll(this.cidrToIPRange(allocator.getAzureSettings().getIpRange()));
		this.allocatedIPAddresses = 0;
		this.maxIPAddresses = this.unallocatedIPPool.size();
		LOGGER.debug(this.maxIPAddresses + " private IP addresses ready for use");

		LOGGER.info("AllocationManager initialised");
	}
	
	public void allocatePublicIP() {
		
	}
	
	public int getAllocatedAddressCount() {
		return this.allocatedIPAddresses;
	}
	
	public int getAllocatedAddressCountMax() {
		return this.maxIPAddresses;
	}

	private ArrayList<String> cidrToIPRange(final String cidr) throws ExceptionInInitializerError {
		if (!cidr.contains("/") || cidr.split("\\.").length != 4)
			throw new ExceptionInInitializerError("Invalid CIDR notation!");

		final String[] split = cidr.split("/");
		final String[] startIPString = split[0].split("\\.");
		int startIP = 0;
		// convert String IP to big-endian integer
		for (int i = 0; i < 4; i++) {
			final int b = new Integer(startIPString[i]);
			if (b < 0 || b > 255)
				throw new ExceptionInInitializerError("Invalid IP address");
			startIP |= ((b & 0xFF) << (24 - (8 * i)));
		}

		final int bits = new Integer(split[1]);
		if (bits < 24 || bits > 32)
			throw new ExceptionInInitializerError("Azure does not support IP ranges of this size!");

		final int size = 1 << (32 - bits); // Math.pow(2, 32 - bits);
		final ArrayList<String> ipAddresses = new ArrayList<String>(size);
		for (int i = 0; i < size; i++) {
			final int ip = startIP + i;
			final StringBuilder sb = new StringBuilder();
			for (int j = 0; j < 4; j++) {
				sb.append(((byte) (ip >> 24 - (8 * j))) & 0xFF);
				if (j != 3)
					sb.append(".");
			}
			ipAddresses.add(sb.toString());
		}

		return ipAddresses;
	}
}
