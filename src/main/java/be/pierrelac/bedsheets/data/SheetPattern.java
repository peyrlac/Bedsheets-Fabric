package be.pierrelac.bedsheets.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.DyeColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a bedsheet pattern data based on banner patterns.
 * Contains base color and pattern layers that can be rendered on beds.
 */
public class SheetPattern {
    private DyeColor baseColor;
    private List<PatternLayer> layers;
    
    public SheetPattern() {
        this.baseColor = DyeColor.WHITE;
        this.layers = new ArrayList<>();
    }
    
    public SheetPattern(DyeColor baseColor, List<PatternLayer> layers) {
        this.baseColor = baseColor;
        this.layers = new ArrayList<>(layers);
    }
    
    /**
     * Gets the base color of the pattern.
     * @return Base dye color
     */
    public DyeColor getBaseColor() {
        return baseColor;
    }
    
    /**
     * Sets the base color of the pattern.
     * @param baseColor New base color
     */
    public void setBaseColor(DyeColor baseColor) {
        this.baseColor = baseColor;
    }
    
    /**
     * Gets the pattern layers list.
     * @return Immutable copy of pattern layers
     */
    public List<PatternLayer> getLayers() {
        return new ArrayList<>(layers);
    }
    
    /**
     * Sets the pattern layers.
     * @param layers New pattern layers
     */
    public void setLayers(List<PatternLayer> layers) {
        this.layers = new ArrayList<>(layers);
    }
    
    /**
     * Adds a pattern layer.
     * @param layer Pattern layer to add
     */
    public void addLayer(PatternLayer layer) {
        this.layers.add(layer);
    }
    
    /**
     * Checks if this pattern has any layers or non-white base color.
     * @return True if pattern has custom content
     */
    public boolean hasPattern() {
        return baseColor != DyeColor.WHITE || !layers.isEmpty();
    }
    
    /**
     * Writes this pattern to NBT.
     * @param nbt NBT compound to write to
     */
    public void writeToNbt(NbtCompound nbt) {
        nbt.putInt("BaseColor", baseColor.getId());
        
        NbtList layerList = new NbtList();
        for (PatternLayer layer : layers) {
            NbtCompound layerNbt = new NbtCompound();
            layer.writeToNbt(layerNbt);
            layerList.add(layerNbt);
        }
        nbt.put("Patterns", layerList);
    }
    
    /**
     * Reads this pattern from NBT.
     * @param nbt NBT compound to read from
     */
    public void readFromNbt(NbtCompound nbt) {
        this.baseColor = DyeColor.byId(nbt.getInt("BaseColor"));
        this.layers.clear();
        
        if (nbt.contains("Patterns")) {
            NbtList layerList = nbt.getList("Patterns", 10); // 10 = compound type
            for (int i = 0; i < layerList.size(); i++) {
                NbtCompound layerNbt = layerList.getCompound(i);
                PatternLayer layer = PatternLayer.fromNbt(layerNbt);
                if (layer != null) {
                    this.layers.add(layer);
                }
            }
        }
    }
    
    /**
     * Creates a copy of this pattern.
     * @return New SheetPattern instance with same data
     */
    public SheetPattern copy() {
        List<PatternLayer> copiedLayers = new ArrayList<>();
        for (PatternLayer layer : layers) {
            copiedLayers.add(layer.copy());
        }
        return new SheetPattern(baseColor, copiedLayers);
    }
    
    /**
     * Generates a hash code for caching textures.
     * @return Hash code based on base color and layers
     */
    public int getTextureHash() {
        int hash = baseColor.getId();
        for (PatternLayer layer : layers) {
            hash = hash * 31 + layer.hashCode();
        }
        return hash;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        SheetPattern that = (SheetPattern) obj;
        return baseColor == that.baseColor && Objects.equals(layers, that.layers);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(baseColor, layers);
    }
    
    @Override
    public String toString() {
        return String.format("SheetPattern{baseColor=%s, layers=%d}", baseColor, layers.size());
    }
}