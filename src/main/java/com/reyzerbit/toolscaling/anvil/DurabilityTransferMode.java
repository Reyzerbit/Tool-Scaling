package com.reyzerbit.toolscaling.anvil;

public enum DurabilityTransferMode {
    /**
     * Transfer durability as-is (5/10 -> 5/100)
     */
    AS_IS,

    /**
     * Transfer durability as percentage (5/10 = 50% -> 50/100)
     */
    PERCENTAGE,

    /**
     * Fully repair the result item
     */
    FULL_REPAIR;

    public static DurabilityTransferMode fromString(String mode) {
        if (mode == null) {
            return PERCENTAGE; // Default
        }

        try {
            return valueOf(mode.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PERCENTAGE; // Default fallback
        }
    }
}
