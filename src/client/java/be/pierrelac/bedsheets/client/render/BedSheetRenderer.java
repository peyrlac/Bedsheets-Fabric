package be.pierrelac.bedsheets.client.render;

import be.pierrelac.bedsheets.BedSheetsMod;
import be.pierrelac.bedsheets.data.SheetPattern;
import be.pierrelac.bedsheets.mixin.BedBlockEntityAccessor;
import be.pierrelac.bedsheets.util.BedNeighbors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client-side renderer for bed sheets.
 * Renders custom bed sheet textures based on banner patterns.
 */
@Environment(EnvType.CLIENT)
public class BedSheetRenderer {
    
    /**
     * Cache for generated sheet textures.
     */
    private static final Map<Integer, Identifier> textureCache = new HashMap<>();
    
    /**
     * Renders a bed sheet for the given bed block entity.
     * @param bedBlockEntity The bed block entity to render for
     * @param matrices Matrix stack for transformations
     * @param vertexConsumers Vertex consumer provider
     * @param light Light level
     * @param overlay Overlay UV
     */
    public static void renderSheet(BedBlockEntity bedBlockEntity, MatrixStack matrices, 
                                 VertexConsumerProvider vertexConsumers, int light, int overlay) {
        
        if (!(bedBlockEntity instanceof BedBlockEntityAccessor accessor)) {
            return;
        }
        
        SheetPattern pattern = accessor.bedsheets$getSheetPattern();
        if (pattern == null || !pattern.hasPattern()) {
            return; // No pattern to render
        }
        
        int messiness = accessor.bedsheets$getMessiness();
        BlockPos pos = bedBlockEntity.getPos();
        
        // TODO: Determine bed facing direction from block state
        Direction bedFacing = Direction.NORTH; // Placeholder
        
        // Find contiguous beds for merged rendering
        List<BlockPos> bedGroup = BedNeighbors.findGroup(bedBlockEntity.getWorld(), pos, bedFacing);
        
        // Check if there's a sleeping player for sheet deformation
        PlayerEntity sleepingPlayer = findSleepingPlayer(bedBlockEntity);
        
        // Render the sheet
        renderSheetMesh(matrices, vertexConsumers, pattern, messiness, bedGroup, 
                       sleepingPlayer, light, overlay);
    }
    
    /**
     * Renders the actual sheet mesh.
     */
    private static void renderSheetMesh(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                      SheetPattern pattern, int messiness, List<BlockPos> bedGroup,
                                      PlayerEntity sleepingPlayer, int light, int overlay) {
        
        // Get or generate texture for this pattern
        Identifier texture = getOrCreateSheetTexture(pattern, messiness);
        
        // Get vertex consumer for the texture
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(texture));
        
        matrices.push();
        
        try {
            // Calculate sheet bounds based on bed group
            int[] bounds = BedNeighbors.calculateGroupBounds(bedGroup, Direction.NORTH); // TODO: Use actual facing
            float minX = bounds[0];
            float minZ = bounds[1]; 
            float maxX = bounds[2] + 1.0f;
            float maxZ = bounds[3] + 1.0f;
            
            // Sheet height above mattress (slightly above to avoid z-fighting)
            float sheetY = 9.0f / 16.0f; // 9 pixels up from bottom in 16-pixel block
            
            // Adjust for player sleeping (create depression/deformation)
            if (sleepingPlayer != null) {
                // TODO: Calculate player position relative to beds and deform sheet accordingly
                sheetY += 1.0f / 16.0f; // Slightly higher when player is under
            }
            
            // Render the sheet quad
            renderSheetQuad(vertexConsumer, matrices, minX, maxX, minZ, maxZ, sheetY, 
                          messiness, light, overlay);
            
        } finally {
            matrices.pop();
        }
    }
    
    /**
     * Renders a single sheet quad.
     */
    private static void renderSheetQuad(VertexConsumer vertexConsumer, MatrixStack matrices,
                                      float minX, float maxX, float minZ, float maxZ, float y,
                                      int messiness, int light, int overlay) {
        
        // Calculate UV coordinates
        float u0 = 0.0f;
        float v0 = 0.0f;
        float u1 = 1.0f;
        float v1 = 1.0f;
        
        // Add wrinkle effect based on messiness
        float wrinkleAmount = messiness * 0.02f; // Up to 2% deformation
        
        // Define the four corners of the sheet
        // Bottom-left
        vertexConsumer.vertex(matrices.peek().getPositionMatrix(), minX, y - wrinkleAmount, minZ)
                     .color(255, 255, 255, 255)
                     .texture(u0, v0)
                     .overlay(overlay)
                     .light(light)
                     .normal(0, 1, 0)
                     .next();
        
        // Bottom-right
        vertexConsumer.vertex(matrices.peek().getPositionMatrix(), maxX, y + wrinkleAmount, minZ)
                     .color(255, 255, 255, 255)
                     .texture(u1, v0)
                     .overlay(overlay)
                     .light(light)
                     .normal(0, 1, 0)
                     .next();
        
        // Top-right
        vertexConsumer.vertex(matrices.peek().getPositionMatrix(), maxX, y - wrinkleAmount, maxZ)
                     .color(255, 255, 255, 255)
                     .texture(u1, v1)
                     .overlay(overlay)
                     .light(light)
                     .normal(0, 1, 0)
                     .next();
        
        // Top-left
        vertexConsumer.vertex(matrices.peek().getPositionMatrix(), minX, y + wrinkleAmount, maxZ)
                     .color(255, 255, 255, 255)
                     .texture(u0, v1)
                     .overlay(overlay)
                     .light(light)
                     .normal(0, 1, 0)
                     .next();
    }
    
    /**
     * Gets or creates a sheet texture for the given pattern and messiness.
     */
    private static Identifier getOrCreateSheetTexture(SheetPattern pattern, int messiness) {
        int cacheKey = pattern.getTextureHash() * 31 + messiness;
        
        return textureCache.computeIfAbsent(cacheKey, key -> {
            // Generate texture identifier
            // TODO: Implement dynamic texture generation based on banner patterns
            // For now, use a placeholder texture
            return new Identifier(BedSheetsMod.MOD_ID, 
                                "textures/block/bedsheet_" + pattern.getBaseColor().getName() + 
                                "_messiness_" + messiness + ".png");
        });
    }
    
    /**
     * Finds a sleeping player in the given bed.
     */
    private static PlayerEntity findSleepingPlayer(BedBlockEntity bedBlockEntity) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            return null;
        }
        
        BlockPos bedPos = bedBlockEntity.getPos();
        
        // Check nearby players for sleeping state
        return client.world.getPlayers().stream()
                   .filter(PlayerEntity::isSleeping)
                   .filter(player -> {
                       BlockPos sleepPos = player.getSleepingPosition().orElse(null);
                       return sleepPos != null && 
                              (sleepPos.equals(bedPos) || 
                               sleepPos.equals(bedPos.offset(Direction.NORTH)) || // TODO: Calculate foot position properly
                               sleepPos.equals(bedPos.offset(Direction.SOUTH)) ||
                               sleepPos.equals(bedPos.offset(Direction.EAST)) ||
                               sleepPos.equals(bedPos.offset(Direction.WEST)));
                   })
                   .findFirst()
                   .orElse(null);
    }
    
    /**
     * Clears the texture cache (useful when resource packs change).
     */
    public static void clearTextureCache() {
        textureCache.clear();
        BedSheetsMod.LOGGER.debug("Cleared bed sheet texture cache");
    }
}