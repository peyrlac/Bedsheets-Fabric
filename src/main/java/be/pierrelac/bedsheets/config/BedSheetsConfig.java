package be.pierrelac.bedsheets.config;

import be.pierrelac.bedsheets.BedSheetsMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configuration class for BedSheets mod settings.
 * Handles loading/saving of mod configuration from JSON file.
 */
public class BedSheetsConfig {
    private static final String CONFIG_FILE = "bedsheets.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    // Health regeneration per second by messiness level (0=MADE, 1=MESSY_1, 2=MESSY_2, 3=MESSY_3)
    public float[] regenPerSecondByMessiness = {1.0f, 0.7f, 0.4f, 0.1f};
    
    // Whether to drop banner when breaking a patterned bed
    public boolean dropBannerOnBreak = true;
    
    // Whether shift+right-click with water bucket clears the pattern
    public boolean allowWaterClear = true;
    
    // Maximum number of beds that can be merged together
    public int maxMergeBeds = 3;
    
    /**
     * Loads configuration from file or creates default if not found.
     * @return Loaded or default configuration
     */
    public static BedSheetsConfig load() {
        Path configPath = getConfigPath();
        
        if (Files.exists(configPath)) {
            try {
                String json = Files.readString(configPath);
                BedSheetsConfig config = GSON.fromJson(json, BedSheetsConfig.class);
                BedSheetsMod.LOGGER.info("Loaded configuration from {}", configPath);
                return config;
            } catch (IOException e) {
                BedSheetsMod.LOGGER.error("Failed to load configuration from {}, using defaults", configPath, e);
            }
        }
        
        // Create default config and save it
        BedSheetsConfig defaultConfig = new BedSheetsConfig();
        defaultConfig.save();
        return defaultConfig;
    }
    
    /**
     * Saves the current configuration to file.
     */
    public void save() {
        Path configPath = getConfigPath();
        try {
            Files.createDirectories(configPath.getParent());
            String json = GSON.toJson(this);
            Files.writeString(configPath, json);
            BedSheetsMod.LOGGER.info("Saved configuration to {}", configPath);
        } catch (IOException e) {
            BedSheetsMod.LOGGER.error("Failed to save configuration to {}", configPath, e);
        }
    }
    
    /**
     * Gets the configuration file path.
     * @return Path to configuration file
     */
    private static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE);
    }
    
    /**
     * Gets regeneration rate for given messiness level.
     * @param messiness Messiness level (0-3)
     * @return Regeneration rate per second
     */
    public float getRegenRate(int messiness) {
        if (messiness < 0 || messiness >= regenPerSecondByMessiness.length) {
            return 0.0f;
        }
        return regenPerSecondByMessiness[messiness];
    }
}