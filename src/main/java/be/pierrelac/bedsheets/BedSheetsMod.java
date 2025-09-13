package be.pierrelac.bedsheets;

import be.pierrelac.bedsheets.config.BedSheetsConfig;
import be.pierrelac.bedsheets.event.SleepHooks;
import be.pierrelac.bedsheets.net.BedSheetsPackets;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main mod initializer for BedSheets mod.
 * Implements custom bedsheet patterns, messiness states, and sleep mechanics.
 * 
 * @author be.pierrelac
 */
public class BedSheetsMod implements ModInitializer {
    /** Mod ID used throughout the mod */
    public static final String MOD_ID = "bedsheets";
    
    /** Logger for the mod */
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    /** Global config instance */
    private static BedSheetsConfig config;
    
    @Override
    public void onInitialize() {
        LOGGER.info("Initializing BedSheets mod");
        
        // Load configuration
        config = BedSheetsConfig.load();
        LOGGER.info("Loaded BedSheets configuration");
        
        // Initialize networking
        BedSheetsPackets.initServer();
        LOGGER.info("Registered server packets");
        
        // Initialize sleep event hooks
        SleepHooks.initialize();
        LOGGER.info("Registered sleep event handlers");
        
        LOGGER.info("BedSheets mod initialized successfully");
    }
    
    /**
     * Gets the global configuration instance
     * @return The configuration instance
     */
    public static BedSheetsConfig getConfig() {
        return config;
    }
}