package net.azurewebsites.thehen101.azureaddressallocator.api;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import net.azurewebsites.thehen101.azureaddressallocator.Allocator;

/**
 * @author h
 * @since 24 May 2020
 * 
 */
public class HTTPHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(HTTPHandler.class);
	
	private final Allocator allocator;
	private final HttpServer httpServer;
	
	public HTTPHandler(final Allocator allocator, final int bindPort) throws IOException {
		this.allocator = allocator;
		this.httpServer = HttpServer.create(new InetSocketAddress(bindPort), 0);
		LOGGER.debug("HttpServer created");
		
		this.httpServer.createContext("/register", (HttpExchange exchange) -> {
			//this.allocator.
			//exchange.
		});
		
		LOGGER.info("HTTPHandler initialised");
	}
}
