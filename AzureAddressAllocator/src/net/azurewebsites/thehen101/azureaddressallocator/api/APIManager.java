package net.azurewebsites.thehen101.azureaddressallocator.api;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.azurewebsites.thehen101.azureaddressallocator.Allocator;
import net.azurewebsites.thehen101.azureaddressallocator.file.FileRegistry;

/**
 * @author h
 * @since 24 May 2020
 * 
 */
public class APIManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(APIManager.class);
	private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();
	
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
	
	public void register(final String user) {
		final String key = this.generateKey();
		this.userToKey.put(user, key);
		this.keyToUser.put(key, user);
		this.saveEntries();
	}
	
	public void unregister(final String user) {
		final String key = this.userToKey.get(user);
		this.userToKey.remove(user);
		this.keyToUser.remove(key);
		this.saveEntries();
	}
	
	public String userToKey(final String user) {
		return this.userToKey.get(user);
	}
	
	public String keyToUser(final String key) {
		return this.keyToUser.get(key);
	}
	
	private void loadEntries() {
		this.userToKey.clear();
		this.keyToUser.clear();
		final String users = this.usersFile.getFileAsString(this.allocator);
		final Gson gson = new Gson();
		if (users.length() == 0) {
			//new file
			this.saveEntries();
		} else {
			final String[][] entries = gson.fromJson(users, String[][].class);
			
			for (int i = 0; i < entries.length; i++) {
				final String[] entry = entries[i];
				this.userToKey.put(entry[0], entry[1]);
				this.keyToUser.put(entry[1], entry[0]);
			}
		}
		LOGGER.debug(this.userToKey.size() + " API entries loaded");
	}
	
	private void saveEntries() {
		final String[][] users = new String[this.userToKey.size()][2];

		int index = 0;
		for (final Entry<String, String> e : this.userToKey.entrySet()) {
			users[index][0] = e.getKey();
			users[index][1] = e.getValue();
			index++;
		}

		this.usersFile.writeFile(this.allocator, new GsonBuilder().setPrettyPrinting().create().toJson(users));
		LOGGER.debug(this.userToKey.size() + " API entries saved");
	}
	
	private String generateKey() {
		final SecureRandom sr = new SecureRandom();
		final byte[] key = new byte[32];
		sr.nextBytes(key);
		return bytesToHex(key);
	}

	public static String bytesToHex(final byte[] bytes) {
		final char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			final int v = bytes[j] & 0xFF;
			hexChars[j * 2] = HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}
		return new String(hexChars);
	}
}
