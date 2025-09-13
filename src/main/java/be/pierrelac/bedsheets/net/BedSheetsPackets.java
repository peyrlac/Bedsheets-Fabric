package be.pierrelac.bedsheets.net;

import be.pierrelac.bedsheets.BedSheetsMod;
import be.pierrelac.bedsheets.data.SheetPattern;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ClientPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Handles networking packets for BedSheets mod.
 * Manages communication between client and server for pattern updates.
 */
public class BedSheetsPackets {
    
    // Packet identifiers
    public static final Identifier PATTERN_UPDATE = new Identifier(BedSheetsMod.MOD_ID, "pattern_update");
    public static final Identifier MESSINESS_UPDATE = new Identifier(BedSheetsMod.MOD_ID, "messiness_update");
    public static final Identifier CLEAR_PATTERN = new Identifier(BedSheetsMod.MOD_ID, "clear_pattern");
    
    /**
     * Initialize server-side packet handlers.
     */
    public static void initServer() {
        // Server doesn't need to register receivers for client-to-server packets in Fabric
        // The server handlers will be implemented in the mixin classes
        BedSheetsMod.LOGGER.info("Server packet handlers initialized");
    }
    
    /**
     * Initialize client-side packet handlers.
     */
    @Environment(EnvType.CLIENT)
    public static void initClient() {
        // Register client packet receivers
        ClientPlayNetworking.registerGlobalReceiver(PATTERN_UPDATE, (client, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            boolean hasPattern = buf.readBoolean();
            SheetPattern pattern = null;
            
            if (hasPattern) {
                NbtCompound patternNbt = buf.readNbt();
                if (patternNbt != null) {
                    pattern = new SheetPattern();
                    pattern.readFromNbt(patternNbt);
                }
            }
            
            // Execute on main thread
            final SheetPattern finalPattern = pattern;
            client.execute(() -> {
                handlePatternUpdate(client, pos, finalPattern);
            });
        });
        
        ClientPlayNetworking.registerGlobalReceiver(MESSINESS_UPDATE, (client, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            int messiness = buf.readVarInt();
            
            client.execute(() -> {
                handleMessinessUpdate(client, pos, messiness);
            });
        });
        
        ClientPlayNetworking.registerGlobalReceiver(CLEAR_PATTERN, (client, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            
            client.execute(() -> {
                handlePatternClear(client, pos);
            });
        });
        
        BedSheetsMod.LOGGER.info("Client packet handlers initialized");
    }
    
    /**
     * Sends a pattern update packet to clients.
     * @param player Target player (null for all players in chunk)
     * @param pos Bed position
     * @param pattern New pattern (null to clear)
     */
    public static void sendPatternUpdate(net.minecraft.server.network.ServerPlayerEntity player, BlockPos pos, SheetPattern pattern) {
        PacketByteBuf buf = ServerPlayNetworking.createS2CPacket(PATTERN_UPDATE);
        buf.writeBlockPos(pos);
        buf.writeBoolean(pattern != null && pattern.hasPattern());
        
        if (pattern != null && pattern.hasPattern()) {
            NbtCompound patternNbt = new NbtCompound();
            pattern.writeToNbt(patternNbt);
            buf.writeNbt(patternNbt);
        }
        
        if (player != null) {
            ServerPlayNetworking.send(player, PATTERN_UPDATE, buf);
        } else {
            // TODO: Send to all players in chunk when chunk loading is implemented
            BedSheetsMod.LOGGER.warn("Broadcast pattern update not yet implemented");
        }
    }
    
    /**
     * Sends a messiness update packet to clients.
     * @param player Target player (null for all players in chunk)
     * @param pos Bed position
     * @param messiness New messiness level
     */
    public static void sendMessinessUpdate(net.minecraft.server.network.ServerPlayerEntity player, BlockPos pos, int messiness) {
        PacketByteBuf buf = ServerPlayNetworking.createS2CPacket(MESSINESS_UPDATE);
        buf.writeBlockPos(pos);
        buf.writeVarInt(messiness);
        
        if (player != null) {
            ServerPlayNetworking.send(player, MESSINESS_UPDATE, buf);
        } else {
            // TODO: Send to all players in chunk when chunk loading is implemented
            BedSheetsMod.LOGGER.warn("Broadcast messiness update not yet implemented");
        }
    }
    
    /**
     * Sends a pattern clear packet to clients.
     * @param player Target player (null for all players in chunk)
     * @param pos Bed position
     */
    public static void sendPatternClear(net.minecraft.server.network.ServerPlayerEntity player, BlockPos pos) {
        PacketByteBuf buf = ServerPlayNetworking.createS2CPacket(CLEAR_PATTERN);
        buf.writeBlockPos(pos);
        
        if (player != null) {
            ServerPlayNetworking.send(player, CLEAR_PATTERN, buf);
        } else {
            // TODO: Send to all players in chunk when chunk loading is implemented
            BedSheetsMod.LOGGER.warn("Broadcast pattern clear not yet implemented");
        }
    }
    
    // Client-side packet handlers
    @Environment(EnvType.CLIENT)
    private static void handlePatternUpdate(net.minecraft.client.MinecraftClient client, BlockPos pos, SheetPattern pattern) {
        if (client.world != null) {
            // TODO: Update client-side bed block entity with new pattern
            // This will be implemented when the mixin is ready
            BedSheetsMod.LOGGER.debug("Received pattern update for bed at {}: {}", pos, pattern);
        }
    }
    
    @Environment(EnvType.CLIENT)
    private static void handleMessinessUpdate(net.minecraft.client.MinecraftClient client, BlockPos pos, int messiness) {
        if (client.world != null) {
            // TODO: Update client-side bed block entity with new messiness
            // This will be implemented when the mixin is ready
            BedSheetsMod.LOGGER.debug("Received messiness update for bed at {}: {}", pos, messiness);
        }
    }
    
    @Environment(EnvType.CLIENT)
    private static void handlePatternClear(net.minecraft.client.MinecraftClient client, BlockPos pos) {
        if (client.world != null) {
            // TODO: Clear pattern on client-side bed block entity
            // This will be implemented when the mixin is ready
            BedSheetsMod.LOGGER.debug("Received pattern clear for bed at {}", pos);
        }
    }
}