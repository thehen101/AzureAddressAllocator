package net.azurewebsites.thehen101.azureaddressallocator.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
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
		LOGGER.debug("HttpServer created on port " + bindPort + ": " + this.httpServer.toString());
		
		this.httpServer.createContext("/register", (final HttpExchange exchange) -> {
			//only allow API registrations from localhost!
			if (!exchange.getRemoteAddress().getAddress().isLoopbackAddress()) {
				this.sendResponse(exchange, 403 /* FORBIDDEN */, 
						new APIResponse("error", "Only localhost can register users!"));
			} else {
				final String post = this.getRequestData(exchange);
				try {
					final Gson gson = new Gson();
					final String[] users = gson.fromJson(post, String[].class);
					
					//make sure we don't already have a registration
					for (int i = 0; i < users.length; i++) {
						if (this.allocator.getAPIManager().userToKey(users[i]) != null) {
							this.sendResponse(exchange, 400 /* BAD REQUEST */, new APIResponse("error",
									"One or more users are already registered. No users have been registered."));
							return;
						}
					}
					
					for (int i = 0; i < users.length; i++) {
						this.allocator.getAPIManager().register(users[i]);
						LOGGER.info("Registered API user: " + users[i] + " with API key "
								+ this.allocator.getAPIManager().userToKey(users[i]));
					}
					this.sendResponse(exchange, 200 /* OK */,
							new APIResponse("success", users.length + " users registered."));
				} catch (final JsonSyntaxException jse) {
					this.sendResponse(exchange, 400 /* BAD REQUEST */, 
							new APIResponse("error", "Malformed JSON. Requires a String array."));
				}
			}
		});

		this.httpServer.createContext("/unregister", (final HttpExchange exchange) -> {
			// only allow API UNregistrations from localhost!
			if (!exchange.getRemoteAddress().getAddress().isLoopbackAddress()) {
				this.sendResponse(exchange, 403 /* FORBIDDEN */,
						new APIResponse("error", "Only localhost can unregister users!"));
			} else {
				final String post = this.getRequestData(exchange);
				try {
					final Gson gson = new Gson();
					final String[] users = gson.fromJson(post, String[].class);

					for (int i = 0; i < users.length; i++) {
						if (this.allocator.getAPIManager().userToKey(users[i]) == null) {
							this.sendResponse(exchange, 400 /* BAD REQUEST */, new APIResponse("error",
									"One or more users are not registered. No users have been unregistered."));
							return;
						}
					}

					for (int i = 0; i < users.length; i++) {
						this.allocator.getAPIManager().unregister(users[i]);
						LOGGER.info("Unregistered API user: " + users[i]);
					}
					this.sendResponse(exchange, 200 /* OK */,
							new APIResponse("success", users.length + " users unregistered."));
				} catch (final JsonSyntaxException jse) {
					this.sendResponse(exchange, 400 /* BAD REQUEST */,
							new APIResponse("error", "Malformed JSON. Requires a String array."));
				}
			}
		});
		
		this.httpServer.createContext("/allocate", (final HttpExchange exchange) -> {
			final String post = this.getRequestData(exchange);
			try {
				final JsonObject allocationRequest = new Gson().fromJson(post, JsonObject.class);
				final String user = this.allocator.getAPIManager()
						.keyToUser(allocationRequest.get("key").getAsString());
				
				if (user == null) {
					this.sendResponse(exchange, 403 /* FORBIDDEN */,
							new APIResponse("error", "Invalid API key!"));
				} else {
					
				}
			} catch (final Exception e) {
				this.sendResponse(exchange, 400 /* BAD REQUEST */,
						new APIResponse("error", "Malformed request."));
			}
		});
		
		this.httpServer.setExecutor(this.allocator.getRequestThreads());
		this.httpServer.start();
		LOGGER.info("HTTPHandler initialised");
	}
	
	private String getRequestData(final HttpExchange exchange) {
		//if your IDE shows the following line as a warning, it's wrong, useDelimiter returns this
		final Scanner scanner = new Scanner(exchange.getRequestBody()).useDelimiter("\\A");
		try {
			if (!scanner.hasNext()) {
				scanner.close();
				return null;
			} else {
				final String post = URLDecoder.decode(scanner.next(), StandardCharsets.UTF_8.name());
				scanner.close();
				return post;
			}
		} catch (final UnsupportedEncodingException e) {
			e.printStackTrace();
			scanner.close();
			return null;
		}
	}
	
	private void sendResponse(final HttpExchange exchange, final int statusCode, final APIResponse response) {
		this.sendResponse(exchange, statusCode, new Gson().toJson(response));
	}

	private void sendResponse(final HttpExchange exchange, final int statusCode, final String response) {
		try {
			exchange.sendResponseHeaders(statusCode, response.length());
			exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));
			exchange.getResponseBody().close();
			LOGGER.debug(
					"Sent " + statusCode + " response to " + exchange.getRemoteAddress().getAddress().getHostName());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private class APIResponse {
		@SerializedName("status")
		private String status;
		
		@SerializedName("message")
		private String message;
		
		private APIResponse(final String status, final String message) {
			this.status = status;
			this.message = message;
		}
	}
}
