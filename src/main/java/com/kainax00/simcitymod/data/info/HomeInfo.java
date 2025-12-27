package com.kainax00.simcitymod.data.info;

import com.kainax00.simcitymod.util.IdentifierUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class HomeInfo {
    private String dimension;
    private int x;
    private int y;
    private int z;

    public HomeInfo(String dimension, int x, int y, int z) {
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static HomeInfo fromGlobalPos(GlobalPos pos) {
        String rawString = pos.dimension().toString();
        String dim = parseDimensionId(rawString);
        
        BlockPos p = pos.pos();
        return new HomeInfo(dim, p.getX(), p.getY(), p.getZ());
    }

    public GlobalPos toGlobalPos() {
        String[] parts = this.dimension.split(":");

        var location = (parts.length == 2) 
            ? IdentifierUtil.create(parts[0], parts[1]) 
            : IdentifierUtil.create("minecraft", "overworld");

        ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, location);
        BlockPos pos = new BlockPos(this.x, this.y, this.z);
        
        return GlobalPos.of(dimKey, pos);
    }

    private static String parseDimensionId(String resourceKeyString) {
        try {
            if (resourceKeyString.contains(" / ")) {
                String[] split = resourceKeyString.split(" / ");
                if (split.length > 1) {
                    return split[1].replace("]", "").trim();
                }
            }
            if (resourceKeyString.contains("minecraft:")) {
                 int start = resourceKeyString.indexOf("minecraft:");
                 int end = resourceKeyString.indexOf("]", start);
                 if (end == -1) end = resourceKeyString.length();
                 return resourceKeyString.substring(start, end);
            }
            return "minecraft:overworld";
        } catch (Exception e) {
            return "minecraft:overworld";
        }
    }

    // Getter
    public String getDimension() { return dimension; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
}