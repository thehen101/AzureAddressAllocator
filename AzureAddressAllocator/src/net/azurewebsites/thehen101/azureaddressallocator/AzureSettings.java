package net.azurewebsites.thehen101.azureaddressallocator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import net.azurewebsites.thehen101.azureaddressallocator.file.FileRegistry;

/**
 * @author h
 * @since 26 May 2020
 * 
 */
public final class AzureSettings {

	public static final AzureSettings createFromFile(final Allocator allocator) {
		return new Gson().fromJson(FileRegistry.AZURESETTINGS.getFileAsString(allocator), AzureSettings.class);
	}
	
	public static final void saveToFile(final AzureSettings settings, final Allocator allocator) {
		FileRegistry.AZURESETTINGS.writeFile(allocator,
				new GsonBuilder().setPrettyPrinting().create().toJson(settings));
	}

	public static final AzureSettings createExampleSettings() {
		return new AzureSettings("yourResourceGroup", "myNic123", "uksouth", "allocPublicIP0", "allocIpConfig0",
				"10.0.1.120/25");
	}
	
	public static final boolean isSettingsBlank(final Allocator allocator) {
		return FileRegistry.AZURESETTINGS.getFileAsString(allocator).trim().length() == 0;
	}

	private AzureSettings(final String resourceGroup, final String nicName, final String location,
			final String publicIpNamePrefix, final String ipConfigNamePrefix, final String ipRange) {
		this.resourceGroup = resourceGroup;
		this.nicName = nicName;
		this.location = location;
		this.publicIpNamePrefix = publicIpNamePrefix;
		this.ipConfigNamePrefix = ipConfigNamePrefix;
		this.ipRange = ipRange;
	}

	@SerializedName("resourceGroup")
	private final String resourceGroup;

	@SerializedName("nicName")
	private final String nicName;

	@SerializedName("location")
	private final String location;

	@SerializedName("publicIpNamePrefix")
	private final String publicIpNamePrefix;

	@SerializedName("ipConfigNamePrefix")
	private final String ipConfigNamePrefix;

	@SerializedName("ipRangeCIDR")
	private final String ipRange;

	public String getResourceGroup() {
		return this.resourceGroup;
	}

	public String getNicName() {
		return this.nicName;
	}

	public String getLocation() {
		return this.location;
	}

	public String getPublicIpNamePrefix() {
		return this.publicIpNamePrefix;
	}

	public String getIpConfigNamePrefix() {
		return this.ipConfigNamePrefix;
	}

	public String getIpRange() {
		return this.ipRange;
	}
}
