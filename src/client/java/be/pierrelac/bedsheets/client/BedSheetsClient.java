package be.pierrelac.bedsheets.client;

import be.pierrelac.bedsheets.BedSheetsMod;
import be.pierrelac.bedsheets.net.BedSheetsPackets;
import net.fabricmc.api.ClientModInitializer;

/**
 * Client-side mod initializer for BedSheets mod.
 * Handles client-specific initialization like rendering and networking.
 */
public class BedSheetsClient implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        BedSheetsMod.LOGGER.info("Initializing BedSheets client");
        
        // Initialize client networking
        BedSheetsPackets.initClient();
        BedSheetsMod.LOGGER.info("Registered client packets");
        
        // TODO: Initialize client-side rendering when mixins are ready
        // BedSheetRenderer.initialize();
        
        BedSheetsMod.LOGGER.info("BedSheets client initialized successfully");
    }
}