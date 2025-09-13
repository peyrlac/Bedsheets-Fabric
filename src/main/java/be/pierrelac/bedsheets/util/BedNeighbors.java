package be.pierrelac.bedsheets.util;

import be.pierrelac.bedsheets.BedSheetsMod;
import be.pierrelac.bedsheets.data.SheetPattern;
import be.pierrelac.bedsheets.mixin.BedBlockEntityAccessor;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for finding contiguous beds with matching sheet patterns.
 * Used for rendering continuous bed sheets across multiple beds.
 */
public class BedNeighbors {
    
    /**
     * Finds all beds in a contiguous group that share the same pattern and orientation.
     * @param world The world to search in
     * @param headPos Position of the head block of a bed to start from
     * @param bedFacing Direction the bed is facing
     * @return List of head block positions for all beds in the group
     */
    public static List<BlockPos> findGroup(World world, BlockPos headPos, Direction bedFacing) {
        List<BlockPos> group = new ArrayList<>();
        
        // Get the pattern of the starting bed
        SheetPattern startPattern = getBedPattern(world, headPos);
        if (startPattern == null || !startPattern.hasPattern()) {
            // No pattern or empty pattern, just return the single bed
            group.add(headPos);
            return group;
        }
        
        // Add the starting bed
        group.add(headPos);
        
        // Get perpendicular directions for scanning
        Direction leftDir = bedFacing.rotateYCounterclockwise();
        Direction rightDir = bedFacing.rotateYClockwise();
        
        int maxBeds = BedSheetsMod.getConfig().maxMergeBeds;
        
        // Scan left
        BlockPos current = headPos;
        while (group.size() < maxBeds) {
            current = current.offset(leftDir);
            if (isMatchingBed(world, current, bedFacing, startPattern)) {
                group.add(0, current); // Add to beginning to maintain order
            } else {
                break;
            }
        }
        
        // Scan right
        current = headPos;
        while (group.size() < maxBeds) {
            current = current.offset(rightDir);
            if (isMatchingBed(world, current, bedFacing, startPattern)) {
                group.add(current); // Add to end
            } else {
                break;
            }
        }
        
        return group;
    }
    
    /**
     * Checks if there's a bed at the given position with matching pattern and orientation.
     * @param world World to check in
     * @param pos Position to check
     * @param expectedFacing Expected bed facing direction
     * @param expectedPattern Expected sheet pattern
     * @return True if there's a matching bed
     */
    private static boolean isMatchingBed(World world, BlockPos pos, Direction expectedFacing, SheetPattern expectedPattern) {
        BlockState state = world.getBlockState(pos);
        
        // Must be a bed block
        if (!(state.getBlock() instanceof BedBlock)) {
            return false;
        }
        
        // Must be a head block
        if (state.get(BedBlock.PART) != BedPart.HEAD) {
            return false;
        }
        
        // Must face the same direction
        Direction bedFacing = state.get(BedBlock.FACING);
        if (bedFacing != expectedFacing) {
            return false;
        }
        
        // Must have matching pattern
        SheetPattern bedPattern = getBedPattern(world, pos);
        if (bedPattern == null || !bedPattern.equals(expectedPattern)) {
            return false;
        }
        
        // Bed must not be occupied
        if (isBedOccupied(world, pos)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Gets the sheet pattern from a bed block entity.
     * @param world World containing the bed
     * @param headPos Position of the bed head block
     * @return Sheet pattern or null if not found
     */
    private static SheetPattern getBedPattern(World world, BlockPos headPos) {
        BlockEntity blockEntity = world.getBlockEntity(headPos);
        if (blockEntity instanceof BedBlockEntity bedBlockEntity) {
            // Use accessor mixin to get the pattern
            if (blockEntity instanceof BedBlockEntityAccessor accessor) {
                return accessor.bedsheets$getSheetPattern();
            }
        }
        return null;
    }
    
    /**
     * Checks if a bed is currently occupied by a player.
     * @param world World containing the bed
     * @param headPos Position of the bed head block
     * @return True if bed is occupied
     */
    private static boolean isBedOccupied(World world, BlockPos headPos) {
        BlockEntity blockEntity = world.getBlockEntity(headPos);
        if (blockEntity instanceof BedBlockEntity bedBlockEntity) {
            // Use accessor mixin to check occupancy
            if (blockEntity instanceof BedBlockEntityAccessor accessor) {
                return accessor.bedsheets$isOccupied();
            }
        }
        return false;
    }
    
    /**
     * Calculates the bounding box for a group of beds.
     * @param group List of bed head positions
     * @param bedFacing Direction the beds are facing
     * @return Array of {minX, minZ, maxX, maxZ} in block coordinates
     */
    public static int[] calculateGroupBounds(List<BlockPos> group, Direction bedFacing) {
        if (group.isEmpty()) {
            return new int[]{0, 0, 0, 0};
        }
        
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;
        
        for (BlockPos pos : group) {
            // Include both head and foot positions
            BlockPos footPos = pos.offset(bedFacing.getOpposite());
            
            minX = Math.min(minX, Math.min(pos.getX(), footPos.getX()));
            maxX = Math.max(maxX, Math.max(pos.getX(), footPos.getX()));
            minZ = Math.min(minZ, Math.min(pos.getZ(), footPos.getZ()));
            maxZ = Math.max(maxZ, Math.max(pos.getZ(), footPos.getZ()));
        }
        
        return new int[]{minX, minZ, maxX, maxZ};
    }
}