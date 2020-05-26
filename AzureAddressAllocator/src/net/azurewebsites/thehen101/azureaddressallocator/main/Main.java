package net.azurewebsites.thehen101.azureaddressallocator.main;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;

import net.azurewebsites.thehen101.azureaddressallocator.Allocator;

/**
 * @author h
 * @since 24 May 2020
 * 
 */
public class Main {
	
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage: <port>");
			System.exit(1);
		}
		
		//Set log4j level
		Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.INFO);
		
		try {
			final Allocator a = new Allocator(Integer.parseInt(args[0]));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}
}
