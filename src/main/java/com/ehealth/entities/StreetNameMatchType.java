package com.ehealth.entities;

/**
 * Street name match type.
 * @version 1.0
 */
public enum StreetNameMatchType {
    /**
     * Full match
     */
	FULL,
    /**
     * Partial match, e.g. "Pavla Tychini" vs "Tychini"
     */
	PARTIAL,
    /**
     * Not matched, e.g. "Vokzalna" vs "Shevchenka"
     */
	NOT_MATCHED,
    /**
     * When nothing is returned by GeoCoding API
     */
	MISSING
}
