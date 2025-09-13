package be.pierrelac.bedsheets.data;

import net.minecraft.block.entity.BannerPattern;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.DyeColor;

import java.util.Objects;

/**
 * Represents a single pattern layer for a bedsheet pattern.
 * Based on banner pattern layers but adapted for bedsheet rendering.
 */
public class PatternLayer {
    private final BannerPattern pattern;
    private final DyeColor color;
    
    public PatternLayer(BannerPattern pattern, DyeColor color) {
        this.pattern = pattern;
        this.color = color;
    }
    
    /**
     * Gets the banner pattern type.
     * @return The banner pattern
     */
    public BannerPattern getPattern() {
        return pattern;
    }
    
    /**
     * Gets the dye color for this layer.
     * @return The dye color
     */
    public DyeColor getColor() {
        return color;
    }
    
    /**
     * Writes this layer to NBT.
     * @param nbt NBT compound to write to
     */
    public void writeToNbt(NbtCompound nbt) {
        nbt.putString("Pattern", pattern.getId());
        nbt.putInt("Color", color.getId());
    }
    
    /**
     * Creates a pattern layer from NBT data.
     * @param nbt NBT compound to read from
     * @return New PatternLayer instance or null if invalid
     */
    public static PatternLayer fromNbt(NbtCompound nbt) {
        try {
            String patternId = nbt.getString("Pattern");
            int colorId = nbt.getInt("Color");
            
            BannerPattern pattern = BannerPattern.byId(patternId);
            DyeColor color = DyeColor.byId(colorId);
            
            if (pattern != null && color != null) {
                return new PatternLayer(pattern, color);
            }
        } catch (Exception e) {
            // Handle any parsing errors
        }
        return null;
    }
    
    /**
     * Creates a copy of this pattern layer.
     * @return New PatternLayer instance with same data
     */
    public PatternLayer copy() {
        return new PatternLayer(pattern, color);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        PatternLayer that = (PatternLayer) obj;
        return pattern == that.pattern && color == that.color;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(pattern, color);
    }
    
    @Override
    public String toString() {
        return String.format("PatternLayer{pattern=%s, color=%s}", pattern.getId(), color.getName());
    }
}