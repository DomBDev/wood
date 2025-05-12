package com.example.designsandbox.world;

import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;

public class RegionCalculator {
    private final Location center;
    private final int radius;
    private final Set<RegionCoordinate> regions;

    public RegionCalculator(Location center, int radius) {
        this.center = center;
        this.radius = radius;
        this.regions = calculateRegions();
    }

    /**
     * Calculates which region files need to be copied based on the radius
     */
    private Set<RegionCoordinate> calculateRegions() {
        Set<RegionCoordinate> regions = new HashSet<>();
        
        // Convert radius from blocks to regions (1 region = 512 blocks)
        int regionRadius = (radius / 512) + 1;
        
        // Get the center region coordinates
        int centerRegionX = center.getBlockX() >> 9; // Divide by 512
        int centerRegionZ = center.getBlockZ() >> 9;
        
        // Calculate a square of regions that encompasses our circle
        for (int rx = -regionRadius; rx <= regionRadius; rx++) {
            for (int rz = -regionRadius; rz <= regionRadius; rz++) {
                // Add the region if any corner is within the radius
                if (isRegionInRadius(rx, rz, regionRadius)) {
                    regions.add(new RegionCoordinate(centerRegionX + rx, centerRegionZ + rz));
                }
            }
        }
        
        return regions;
    }

    /**
     * Checks if a region should be included based on its distance from center
     */
    private boolean isRegionInRadius(int rx, int rz, int regionRadius) {
        // Check if any corner of the region is within the radius
        double radiusSquared = regionRadius * regionRadius;
        
        // Check all four corners of the region
        return isPointInRadius(rx, rz, radiusSquared) ||
               isPointInRadius(rx + 1, rz, radiusSquared) ||
               isPointInRadius(rx, rz + 1, radiusSquared) ||
               isPointInRadius(rx + 1, rz + 1, radiusSquared);
    }

    /**
     * Checks if a point is within the radius using distance squared
     */
    private boolean isPointInRadius(double x, double z, double radiusSquared) {
        return (x * x + z * z) <= radiusSquared;
    }

    /**
     * Gets the set of region coordinates that need to be copied
     */
    public Set<RegionCoordinate> getRegions() {
        return regions;
    }

    /**
     * Gets the center location
     */
    public Location getCenter() {
        return center;
    }

    /**
     * Gets the radius in blocks
     */
    public int getRadius() {
        return radius;
    }

    /**
     * Represents a region file coordinate pair
     */
    public static class RegionCoordinate {
        private final int x;
        private final int z;

        public RegionCoordinate(int x, int z) {
            this.x = x;
            this.z = z;
        }

        public int getX() {
            return x;
        }

        public int getZ() {
            return z;
        }

        /**
         * Gets the region filename (r.X.Z.mca)
         */
        public String getFileName() {
            return String.format("r.%d.%d.mca", x, z);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RegionCoordinate that = (RegionCoordinate) o;
            return x == that.x && z == that.z;
        }

        @Override
        public int hashCode() {
            return 31 * x + z;
        }

        @Override
        public String toString() {
            return String.format("Region(%d, %d)", x, z);
        }
    }
} 