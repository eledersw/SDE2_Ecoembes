package es.deusto.sd.ecoembes.entity;

import java.time.LocalDate;
import java.util.Objects;

public class DumpsterMeasurement {
	private LocalDate date;
	private int numContainers;
	private FillLevel fillLevel;

	// Constructor without parameters
	public DumpsterMeasurement() { }

	// Constructor with parameters
	public DumpsterMeasurement(LocalDate date, int numContainers, FillLevel fillLevel) {
		this.date = date;
		this.numContainers = numContainers;
		this.fillLevel = fillLevel;
	}

	// Getters and setters
	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
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

	// hashCode and equals
	@Override
	public int hashCode() {
		return Objects.hash(date, numContainers, fillLevel);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DumpsterMeasurement other = (DumpsterMeasurement) obj;
		return Objects.equals(date, other.date) &&
			   numContainers == other.numContainers &&
			   fillLevel == other.fillLevel;
	}
}
