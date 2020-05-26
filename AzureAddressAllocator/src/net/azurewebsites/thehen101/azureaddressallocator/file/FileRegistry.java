package net.azurewebsites.thehen101.azureaddressallocator.file;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import net.azurewebsites.thehen101.azureaddressallocator.Allocator;

/**
 * @author h
 * @since 24 May 2020
 * 
 */
public enum FileRegistry {
	USERS("users.json"),
	AZURESETTINGS("azuresettings.json");
	
	private final File file;
	
	FileRegistry(final String fileName) {
		this.file = new File(fileName);
		if (!this.file.exists())
			try {
				this.file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	public File getFile() {
		return this.file;
	}
	
	public Path writeFile(final Allocator allocator, final String contents) {
		Future<Path> result = allocator.submit(() -> {
			return Files.write(this.file.toPath(), contents.getBytes());
		});
		try {
			return result.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getFileAsString(final Allocator allocator) {
		Future<String> result = allocator.submit(() -> {
			return new String(Files.readAllBytes(this.file.toPath()), StandardCharsets.UTF_8);
		});
		try {
			return result.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}
}
