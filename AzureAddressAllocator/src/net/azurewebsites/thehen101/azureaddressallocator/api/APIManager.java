package net.azurewebsites.thehen101.azureaddressallocator.api;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import net.azurewebsites.thehen101.azureaddressallocator.Allocator;
import net.azurewebsites.thehen101.azureaddressallocator.file.FileRegistry;

/**
 * @author h
 * @since 24 May 2020
 * 
 */
public class APIManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(APIManager.class);
	
	private final HashMap<String, String> userToKey = new HashMap<String, String>();
	private final HashMap<String, String> keyToUser = new HashMap<String, String>();
	
	private final Allocator allocator;
	private final FileRegistry usersFile;
	
	public APIManager(final Allocator allocator, final FileRegistry usersFile) 
			throws InterruptedException, ExecutionException {
		this.allocator = allocator;
		this.usersFile = usersFile;
		
		this.loadEntries();
	}
	
	private void loadEntries() {
		this.userToKey.clear();
		this.keyToUser.clear();
		final String users = this.usersFile.getFileAsString(this.allocator);
		final Gson gson = new Gson();
		if (users.length() == 0)
			//new file
			this.usersFile.writeFile(this.allocator, gson.toJson(new String[0][2]));
		else {
			final String[][] entries = gson.fromJson(users, String[][].class);
			
			for (int i = 0; i < entries.length; i++) {
				final String[] entry = entries[i];
				this.userToKey.put(entry[0], entry[1]);
				this.keyToUser.put(entry[1], entry[0]);
			}
		}
		LOGGER.debug("API entries loaded");
	}
}
