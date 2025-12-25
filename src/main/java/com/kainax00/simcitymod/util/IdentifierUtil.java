package com.kainax00.simcitymod.util;

import net.minecraft.resources.Identifier;

/**
 * Utility class to wrap obfuscated Identifier methods.
 * This class serves as a bridge to handle ResourceLocation functionality in the 1.21 environment.
 */
public class IdentifierUtil {

    /**
     * Parses a string into an Identifier.
     * Example: "minecraft:iron_ingot" -> Identifier(minecraft, iron_ingot)
     * * @param location The string representation of the location.
     * @return The parsed Identifier object.
     * @see Identifier#m_441146_(String)
     */
    public static Identifier parse(String location) {
        return Identifier.m_441146_(location);
    }

    /**
     * Creates an Identifier from a specific namespace and path.
     * Example: create("simcitymod", "chunk_claimer")
     * * @param namespace The namespace (Mod ID or "minecraft").
     * @param path The path of the resource.
     * @return The created Identifier object.
     * @see Identifier#m_445280_(String, String)
     */
    public static Identifier create(String namespace, String path) {
        return Identifier.m_445280_(namespace, path);
    }

    /**
     * Creates an Identifier using the default "minecraft" namespace.
     * Example: withDefaultNamespace("iron_ingot") -> "minecraft:iron_ingot"
     * * @param path The path of the resource.
     * @return The created Identifier with "minecraft" namespace.
     * @see Identifier#m_438827_(String)
     */
    public static Identifier withDefaultNamespace(String path) {
        return Identifier.m_438827_(path);
    }

    /**
     * Retrieves the namespace part of the Identifier.
     * * @param id The target Identifier.
     * @return The namespace string (e.g., "minecraft").
     * @see Identifier#m_442187_()
     */
    public static String getNamespace(Identifier id) {
        return id.m_442187_();
    }

    /**
     * Retrieves the path part of the Identifier.
     * * @param id The target Identifier.
     * @return The path string (e.g., "iron_ingot").
     * @see Identifier#m_445092_()
     */
    public static String getPath(Identifier id) {
        return id.m_445092_();
    }
    
    /**
     * Checks if the Identifier is valid.
     * Useful for validating config inputs.
     * * @param location The string to check.
     * @return True if valid, false otherwise.
     */
    public static boolean isValid(String location) {
        try {
            Identifier.m_441146_(location);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}