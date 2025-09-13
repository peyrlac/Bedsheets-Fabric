package be.pierrelac.bedsheets.mixin;

import be.pierrelac.bedsheets.data.SheetPattern;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/**
 * Mixin for BedBlockEntity to add custom bedsheet data storage.
 * Adds sheet pattern, messiness level, and owner tracking.
 */
@Mixin(BedBlockEntity.class)
public class BedBlockEntityNbtMixin implements BedBlockEntityAccessor {
    
    @Unique
    private SheetPattern bedsheets$sheetPattern = null;
    
    @Unique 
    private int bedsheets$messiness = 0;
    
    @Unique
    private UUID bedsheets$sheetOwner = null;
    
    /**
     * Inject into readNbt to load our custom data.
     */
    @Inject(method = "readNbt", at = @At("TAIL"))
    private void bedsheets$readNbt(NbtCompound nbt, CallbackInfo ci) {
        // Read sheet pattern
        if (nbt.contains("BedSheets_Pattern")) {
            this.bedsheets$sheetPattern = new SheetPattern();
            this.bedsheets$sheetPattern.readFromNbt(nbt.getCompound("BedSheets_Pattern"));
        } else {
            this.bedsheets$sheetPattern = null;
        }
        
        // Read messiness level
        this.bedsheets$messiness = nbt.getInt("BedSheets_Messiness");
        if (this.bedsheets$messiness < 0 || this.bedsheets$messiness > 3) {
            this.bedsheets$messiness = 0; // Clamp to valid range
        }
        
        // Read sheet owner
        if (nbt.contains("BedSheets_Owner")) {
            try {
                this.bedsheets$sheetOwner = UUID.fromString(nbt.getString("BedSheets_Owner"));
            } catch (IllegalArgumentException e) {
                this.bedsheets$sheetOwner = null;
            }
        } else {
            this.bedsheets$sheetOwner = null;
        }
    }
    
    /**
     * Inject into writeNbt to save our custom data.
     */
    @Inject(method = "writeNbt", at = @At("TAIL"))
    private void bedsheets$writeNbt(NbtCompound nbt, CallbackInfo ci) {
        // Write sheet pattern
        if (this.bedsheets$sheetPattern != null && this.bedsheets$sheetPattern.hasPattern()) {
            NbtCompound patternNbt = new NbtCompound();
            this.bedsheets$sheetPattern.writeToNbt(patternNbt);
            nbt.put("BedSheets_Pattern", patternNbt);
        }
        
        // Write messiness level
        if (this.bedsheets$messiness > 0) {
            nbt.putInt("BedSheets_Messiness", this.bedsheets$messiness);
        }
        
        // Write sheet owner
        if (this.bedsheets$sheetOwner != null) {
            nbt.putString("BedSheets_Owner", this.bedsheets$sheetOwner.toString());
        }
    }
    
    // Implement BedBlockEntityAccessor interface
    
    @Override
    public SheetPattern bedsheets$getSheetPattern() {
        return this.bedsheets$sheetPattern != null ? this.bedsheets$sheetPattern.copy() : null;
    }
    
    @Override
    public void bedsheets$setSheetPattern(SheetPattern pattern) {
        this.bedsheets$sheetPattern = pattern != null ? pattern.copy() : null;
        
        // Sync to foot block if this is head block
        bedsheets$syncToOtherHalf();
    }
    
    @Override
    public int bedsheets$getMessiness() {
        return this.bedsheets$messiness;
    }
    
    @Override
    public void bedsheets$setMessiness(int messiness) {
        this.bedsheets$messiness = Math.max(0, Math.min(3, messiness)); // Clamp to 0-3
        
        // Sync to foot block if this is head block
        bedsheets$syncToOtherHalf();
    }
    
    @Override
    public UUID bedsheets$getSheetOwner() {
        return this.bedsheets$sheetOwner;
    }
    
    @Override
    public void bedsheets$setSheetOwner(UUID owner) {
        this.bedsheets$sheetOwner = owner;
        
        // Sync to foot block if this is head block
        bedsheets$syncToOtherHalf();
    }
    
    @Override
    public boolean bedsheets$isOccupied() {
        BedBlockEntity self = (BedBlockEntity) (Object) this;
        World world = self.getWorld();
        BlockPos pos = self.getPos();
        
        if (world == null) {
            return false;
        }
        
        // TODO: Check if any player is sleeping in this bed
        // This will need additional tracking or checking player sleep positions
        return false; // Placeholder implementation
    }
    
    /**
     * Syncs bedsheet data to the other half of the bed.
     */
    @Unique
    private void bedsheets$syncToOtherHalf() {
        BedBlockEntity self = (BedBlockEntity) (Object) this;
        World world = self.getWorld();
        BlockPos pos = self.getPos();
        
        if (world == null || world.isClient()) {
            return; // Only sync on server
        }
        
        // TODO: Find the other half and sync data
        // This requires determining which half this is and finding the other
        // Will be implemented when BedBlock mixin is ready
    }
}