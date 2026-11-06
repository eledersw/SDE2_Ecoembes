package es.deusto.sd.ecoembes.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A kerbside recycling dumpster. It keeps its current state (numContainers and
 * fillLevel) and owns its history of daily measurements (composition).
 */
public class Dumpster {
	private long id;
	private String address;
	private String postalCode;
	private int capacity;
	private int numContainers;
	private FillLevel fillLevel;
	private List<DumpsterMeasurement> measurements = new ArrayList<>();

	// Constructor without parameters
	public Dumpster() { }

	// Constructor with parameters
	public Dumpster(long id, String address, String postalCode, int capacity,
					int numContainers, FillLevel fillLevel) {
		this.id = id;
		this.address = address;
		this.postalCode = postalCode;
		this.capacity = capacity;
		this.numContainers = numContainers;
		this.fillLevel = fillLevel;
	}

	// Daily sensor update: stores the new current state and records it as
	// today's measurement. A second update on the same day replaces the first.
	public void updateInfo(int numContainers, FillLevel fillLevel) {
		this.numContainers = numContainers;
		this.fillLevel = fillLevel;

		LocalDate today = LocalDate.now();
		measurements.removeIf(m -> m.getDate().equals(today));
		measurements.add(new DumpsterMeasurement(today, numContainers, fillLevel));
	}

	// Getters and setters
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public int getNumContainers() {
		return numContainers;
	}

	public void setNumContainers(int numContainers) {
		this.numContainers = numContainers;
	}

	public FillLevel getFillLevel() {
		return fillLevel;
	}

	public void setFillLevel(FillLevel fillLevel) {
		this.fillLevel = fillLevel;
	}

	public List<DumpsterMeasurement> getMeasurements() {
		return measurements;
	}

	public void setMeasurements(List<DumpsterMeasurement> measurements) {
		this.measurements = measurements;
	}

	// hashCode and equals
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Dumpster other = (Dumpster) obj;
		return id == other.id;
	}
}
