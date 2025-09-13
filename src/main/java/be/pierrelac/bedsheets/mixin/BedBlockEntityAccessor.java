package be.pierrelac.bedsheets.mixin;

import be.pierrelac.bedsheets.data.SheetPattern;
import net.minecraft.block.entity.BedBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.UUID;

/**
 * Accessor interface for BedBlockEntity mixin.
 * Provides access to custom bedsheet data added via mixin.
 */
@Mixin(BedBlockEntity.class)
public interface BedBlockEntityAccessor {
    
    /**
     * Gets the sheet pattern for this bed.
     * @return The current sheet pattern or null if none
     */
    SheetPattern bedsheets$getSheetPattern();
    
    /**
     * Sets the sheet pattern for this bed.
     * @param pattern New sheet pattern (null to clear)
     */
    void bedsheets$setSheetPattern(SheetPattern pattern);
    
    /**
     * Gets the messiness level of this bed.
     * @return Messiness level (0-3)
     */
    int bedsheets$getMessiness();
    
    /**
     * Sets the messiness level of this bed.
     * @param messiness New messiness level (0-3)
     */
    void bedsheets$setMessiness(int messiness);
    
    /**
     * Gets the owner UUID of this bed's sheet pattern.
     * @return Owner UUID or null if no owner
     */
    UUID bedsheets$getSheetOwner();
    
    /**
     * Sets the owner UUID of this bed's sheet pattern.
     * @param owner New owner UUID (null to clear)
     */
    void bedsheets$setSheetOwner(UUID owner);
    
    /**
     * Checks if this bed is currently occupied.
     * @return True if occupied by a sleeping player
     */
    boolean bedsheets$isOccupied();
}