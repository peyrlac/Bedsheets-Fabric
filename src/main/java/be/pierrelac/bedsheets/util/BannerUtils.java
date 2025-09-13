package be.pierrelac.bedsheets.util;

import be.pierrelac.bedsheets.data.PatternLayer;
import be.pierrelac.bedsheets.data.SheetPattern;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.item.BannerItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.DyeColor;

/**
 * Utility class for converting between banner items and sheet patterns.
 * Handles NBT data extraction and banner item creation.
 */
public class BannerUtils {
    
    /**
     * Extracts a sheet pattern from a banner item stack.
     * @param banner Banner item stack
     * @return SheetPattern representing the banner's design, or null if invalid
     */
    public static SheetPattern fromBanner(ItemStack banner) {
        if (banner.isEmpty() || !(banner.getItem() instanceof BannerItem bannerItem)) {
            return null;
        }
        
        // Get base color from banner item
        DyeColor baseColor = bannerItem.getColor();
        SheetPattern pattern = new SheetPattern();
        pattern.setBaseColor(baseColor);
        
        // Extract pattern layers from NBT
        NbtCompound nbt = banner.getNbt();
        if (nbt != null && nbt.contains("BlockEntityTag")) {
            NbtCompound blockEntityTag = nbt.getCompound("BlockEntityTag");
            if (blockEntityTag.contains("Patterns")) {
                NbtList patternList = blockEntityTag.getList("Patterns", 10); // 10 = compound type
                
                for (int i = 0; i < patternList.size(); i++) {
                    NbtCompound patternNbt = patternList.getCompound(i);
                    
                    String patternId = patternNbt.getString("Pattern");
                    int colorId = patternNbt.getInt("Color");
                    
                    BannerPattern bannerPattern = BannerPattern.byId(patternId);
                    DyeColor dyeColor = DyeColor.byId(colorId);
                    
                    if (bannerPattern != null && dyeColor != null) {
                        pattern.addLayer(new PatternLayer(bannerPattern, dyeColor));
                    }
                }
            }
        }
        
        return pattern;
    }
    
    /**
     * Creates a banner item stack from a sheet pattern.
     * @param pattern Sheet pattern to convert
     * @return Banner item stack with the pattern, or empty stack if pattern is null
     */
    public static ItemStack toBanner(SheetPattern pattern) {
        if (pattern == null || !pattern.hasPattern()) {
            return ItemStack.EMPTY;
        }
        
        // Create banner item with base color
        DyeColor baseColor = pattern.getBaseColor();
        ItemStack banner = new ItemStack(BannerItem.getForColor(baseColor));
        
        // Add pattern layers to NBT
        if (!pattern.getLayers().isEmpty()) {
            NbtCompound nbt = banner.getOrCreateNbt();
            NbtCompound blockEntityTag = new NbtCompound();
            NbtList patternList = new NbtList();
            
            for (PatternLayer layer : pattern.getLayers()) {
                NbtCompound patternNbt = new NbtCompound();
                patternNbt.putString("Pattern", layer.getPattern().getId());
                patternNbt.putInt("Color", layer.getColor().getId());
                patternList.add(patternNbt);
            }
            
            blockEntityTag.put("Patterns", patternList);
            nbt.put("BlockEntityTag", blockEntityTag);
        }
        
        return banner;
    }
    
    /**
     * Builds a texture cache key for a sheet pattern.
     * Used for caching rendered textures on the client.
     * @param pattern Sheet pattern
     * @return String key for texture caching
     */
    public static String buildTextureKey(SheetPattern pattern) {
        if (pattern == null || !pattern.hasPattern()) {
            return "empty";
        }
        
        StringBuilder key = new StringBuilder();
        key.append("base_").append(pattern.getBaseColor().getName());
        
        for (PatternLayer layer : pattern.getLayers()) {
            key.append("_").append(layer.getPattern().getId())
               .append("_").append(layer.getColor().getName());
        }
        
        return key.toString();
    }
    
    /**
     * Generates a hash code for a sheet pattern for texture caching.
     * @param pattern Sheet pattern
     * @return Hash code for the pattern
     */
    public static int getPatternHash(SheetPattern pattern) {
        if (pattern == null) {
            return 0;
        }
        return pattern.getTextureHash();
    }
}