package be.pierrelac.bedsheets.event;

import be.pierrelac.bedsheets.BedSheetsMod;
import be.pierrelac.bedsheets.config.BedSheetsConfig;
import be.pierrelac.bedsheets.mixin.BedBlockEntityAccessor;
import be.pierrelac.bedsheets.net.BedSheetsPackets;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Handles sleep-related events and mechanics for BedSheets mod.
 * Manages health regeneration during sleep and messiness updates on wake.
 */
public class SleepHooks {
    
    /**
     * Tracks players currently sleeping and their regeneration rates.
     */
    private static final Map<UUID, SleepingPlayerData> sleepingPlayers = new HashMap<>();
    
    /**
     * Initialize sleep event handlers.
     */
    public static void initialize() {
        // Register server tick event for health regeneration
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickSleepingPlayers(server);
        });
        
        // Register entity events (though we'll mainly use mixins for sleep start/end)
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            if (entity instanceof ServerPlayerEntity player) {
                // Clean up if player leaves while sleeping
                sleepingPlayers.remove(player.getUuid());
            }
        });
        
        BedSheetsMod.LOGGER.info("Sleep event handlers registered");
    }
    
    /**
     * Called when a player starts sleeping.
     * @param player The player who started sleeping
     * @param bedHeadPos Position of the bed head block
     * @param messiness Current messiness level of the bed
     */
    public static void onSleepStart(ServerPlayerEntity player, BlockPos bedHeadPos, int messiness) {
        BedSheetsConfig config = BedSheetsMod.getConfig();
        float regenRate = config.getRegenRate(messiness);
        
        // Store sleeping player data
        SleepingPlayerData data = new SleepingPlayerData(bedHeadPos, messiness, regenRate);
        sleepingPlayers.put(player.getUuid(), data);
        
        BedSheetsMod.LOGGER.debug("Player {} started sleeping at {} with messiness {} (regen: {} HP/s)", 
                                player.getName().getString(), bedHeadPos, messiness, regenRate);
    }
    
    /**
     * Called when a player stops sleeping.
     * @param player The player who stopped sleeping
     */
    public static void onWake(ServerPlayerEntity player) {
        SleepingPlayerData data = sleepingPlayers.remove(player.getUuid());
        if (data == null) {
            return; // Player wasn't tracked as sleeping
        }
        
        // Check if player slept through the night successfully
        if (player.getWorld().isDay()) {
            // Increment bed messiness
            incrementBedMessiness(player.getWorld(), data.bedHeadPos, player);
        }
        
        BedSheetsMod.LOGGER.debug("Player {} woke up from bed at {}", 
                                player.getName().getString(), data.bedHeadPos);
    }
    
    /**
     * Applies health regeneration to sleeping players each tick.
     * @param server The server instance
     */
    private static void tickSleepingPlayers(net.minecraft.server.MinecraftServer server) {
        if (sleepingPlayers.isEmpty()) {
            return;
        }
        
        Iterator<Map.Entry<UUID, SleepingPlayerData>> iterator = sleepingPlayers.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<UUID, SleepingPlayerData> entry = iterator.next();
            UUID playerId = entry.getKey();
            SleepingPlayerData data = entry.getValue();
            
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerId);
            
            if (player == null || !player.isSleeping()) {
                // Player disconnected or stopped sleeping
                iterator.remove();
                continue;
            }
            
            // Apply health regeneration
            if (data.regenRate > 0.0f && player.getHealth() < player.getMaxHealth()) {
                // Regenerate health based on rate (per second, so divide by 20 ticks)
                float healAmount = data.regenRate / 20.0f;
                player.heal(healAmount);
                
                data.tickCounter++;
                if (data.tickCounter % 20 == 0) { // Log every second
                    BedSheetsMod.LOGGER.debug("Healed player {} for {} HP (total rate: {} HP/s)", 
                                            player.getName().getString(), healAmount, data.regenRate);
                }
            }
        }
    }
    
    /**
     * Increments the messiness level of a bed after successful sleep.
     * @param world The world containing the bed
     * @param bedHeadPos Position of the bed head block
     * @param player The player who slept in the bed
     */
    private static void incrementBedMessiness(net.minecraft.world.World world, BlockPos bedHeadPos, ServerPlayerEntity player) {
        BlockEntity blockEntity = world.getBlockEntity(bedHeadPos);
        if (!(blockEntity instanceof BedBlockEntity bedBlockEntity)) {
            return;
        }
        
        // Use mixin accessor to get and update messiness
        if (blockEntity instanceof BedBlockEntityAccessor accessor) {
            int currentMessiness = accessor.bedsheets$getMessiness();
            int newMessiness = Math.min(currentMessiness + 1, 3); // Cap at 3
            
            if (newMessiness != currentMessiness) {
                accessor.bedsheets$setMessiness(newMessiness);
                bedBlockEntity.markDirty();
                
                // Sync to clients
                BedSheetsPackets.sendMessinessUpdate(player, bedHeadPos, newMessiness);
                
                BedSheetsMod.LOGGER.debug("Bed at {} messiness increased from {} to {}", 
                                        bedHeadPos, currentMessiness, newMessiness);
            }
        }
    }
    
    /**
     * Data class to track sleeping player information.
     */
    private static class SleepingPlayerData {
        final BlockPos bedHeadPos;
        final int messiness;
        final float regenRate;
        int tickCounter = 0;
        
        SleepingPlayerData(BlockPos bedHeadPos, int messiness, float regenRate) {
            this.bedHeadPos = bedHeadPos;
            this.messiness = messiness;
            this.regenRate = regenRate;
        }
    }
}