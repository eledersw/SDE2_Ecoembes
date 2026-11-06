package es.deusto.sd.ecoembes.entity;

/**
 * Fill level reported by a dumpster sensor.
 */
public enum FillLevel {
	GREEN,	// there is room for more containers
	ORANGE,	// limited space
	RED		// dumpster full
}
