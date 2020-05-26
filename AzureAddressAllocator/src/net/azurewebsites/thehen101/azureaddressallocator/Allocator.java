package net.azurewebsites.thehen101.azureaddressallocator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.azurewebsites.thehen101.azureaddressallocator.api.APIManager;
import net.azurewebsites.thehen101.azureaddressallocator.api.HTTPHandler;
import net.azurewebsites.thehen101.azureaddressallocator.file.FileRegistry;

/**
 * @author h
 * @since 24 May 2020
 * 
 */
public class Allocator {
	private static final Logger LOGGER = LoggerFactory.getLogger(Allocator.class);
	
	private final ExecutorService taskThread;
	private final ExecutorService requestThreads;
	
	private final APIManager apiManager;
	private final AzureSettings azureSettings;
	private final HTTPHandler httpHandler;
	
	public Allocator(final int bindPort) throws IOException, InterruptedException, ExecutionException {
		//quickly check to see if we're root
		if (new Integer(this.systemCommand("id -u").trim()) != 0)
			throw new ExceptionInInitializerError("Not running as root! Please start as root!");
		
		this.taskThread = Executors.newSingleThreadExecutor();
		this.requestThreads = Executors.newCachedThreadPool();
		
		this.azureSettings = this.loadSettings();
		if (this.azureSettings == null) {
			LOGGER.error("Failed to find settings file! An example has been created, please populate it...");
			this.taskThread.shutdown();
			this.requestThreads.shutdown();
			throw new ExceptionInInitializerError("No settings file found");
		}
		
		this.apiManager = new APIManager(this, FileRegistry.USERS);
		this.httpHandler = new HTTPHandler(this, bindPort);
		
		LOGGER.info("Allocator successfully initialised. Listening for requests...");
	}
	
	private final AzureSettings loadSettings() {
		if (AzureSettings.isSettingsBlank(this)) {
			final AzureSettings example = AzureSettings.createExampleSettings();
			AzureSettings.saveToFile(example, this);
			return null;
		} else {
			return AzureSettings.createFromFile(this);
		}
	}
	
	public <T> Future<T> submit(final Callable<T> callable) {
		return this.taskThread.submit(callable);
	}
	
	public String systemCommand(final String commandLine) {
		LOGGER.debug("Executing system command: " + commandLine);
		try {
			final Process p = Runtime.getRuntime().exec(commandLine);
			final InputStream is = p.getInputStream();
			final ByteArrayOutputStream boas = new ByteArrayOutputStream();
			int current = 0;
			
			while ((current = is.read()) != -1)
				boas.write(current);
			
			return new String(boas.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public ExecutorService getRequestThreads() {
		return this.requestThreads;
	}
	
	public APIManager getAPIManager() {
		return this.apiManager;
	}
	
	public AzureSettings getAzureSettings() {
		return this.azureSettings;
	}
	
	public HTTPHandler getHTTPHandler() {
		return this.httpHandler;
	}
}
