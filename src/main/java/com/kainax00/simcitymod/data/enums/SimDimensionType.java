package com.kainax00.simcitymod.data.enums;

public enum SimDimensionType {
    WILD("wild_dimension", "minecraft:overworld", "minecraft:overworld"),
    NETHER("nether_dimension", "minecraft:the_nether", "minecraft:nether"),
    END("end_dimension", "minecraft:the_end", "minecraft:end");

    private final String idPrefix;
    private final String dimensionType;
    private final String generatorSettings;

    SimDimensionType(String idPrefix, String dimensionType, String generatorSettings) {
        this.idPrefix = idPrefix;
        this.dimensionType = dimensionType;
        this.generatorSettings = generatorSettings;
    }

    /**
     * Checks if the given dimension ID belongs to any SimCity custom dimensions.
     */
    public static SimDimensionType fromDimensionId(String dimId) {
        if (dimId == null) return null;
        for (SimDimensionType type : values()) {
            // Check if the dimension ID (e.g., "simcitymod:wild_dimension_v1") contains our prefix.
            if (dimId.contains(type.idPrefix)) {
                return type;
            }
        }
        return null;
    }

    // Helper to check if a dimension is handled by this enum
    public static boolean isSimCityDimension(String dimId) {
        return fromDimensionId(dimId) != null;
    }

    public String getIdPrefix() { return idPrefix; }
    public String getDimensionType() { return dimensionType; }
    public String getGeneratorSettings() { return generatorSettings; }
}