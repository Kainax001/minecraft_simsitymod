package com.kainax00.simcitymod.data.info;

import com.kainax00.simcitymod.util.IdentifierUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * HomeInfo stores dimension and coordinate data for serialization.
 * It acts as a bridge between Minecraft's GlobalPos and JSON data.
 */
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

    /**
     * Converts a Minecraft GlobalPos to a HomeInfo object.
     * * Note on Obfuscation:
     * In this environment, ResourceKey uses obfuscated names.
     * - f_435021_: The Identifier field (location).
     * - m_447358_(): The getter method that returns f_435021_.
     * We use m_447358_() to retrieve the clean dimension ID (e.g., "minecraft:overworld").
     */
    public static HomeInfo fromGlobalPos(GlobalPos pos) {
        // m_447358_() returns the Identifier (location) of the ResourceKey
        String dim = pos.dimension().m_447358_().toString();
        
        BlockPos p = pos.pos();
        return new HomeInfo(dim, p.getX(), p.getY(), p.getZ());
    }

    /**
     * Converts the stored String data back into a Minecraft GlobalPos.
     * This is used when loading spawn points from the JSON file.
     */
    public GlobalPos toGlobalPos() {
        String[] parts = this.dimension.split(":");

        // Reconstruct the Identifier (ResourceLocation)
        // Defaults to minecraft:overworld if the stored string is invalid
        var location = (parts.length == 2) 
            ? IdentifierUtil.create(parts[0], parts[1]) 
            : IdentifierUtil.create("minecraft", "overworld");

        // Create the ResourceKey for the dimension
        ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, location);
        BlockPos pos = new BlockPos(this.x, this.y, this.z);
        
        return GlobalPos.of(dimKey, pos);
    }

    // Getters used by GSON for JSON serialization
    public String getDimension() { return dimension; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
}