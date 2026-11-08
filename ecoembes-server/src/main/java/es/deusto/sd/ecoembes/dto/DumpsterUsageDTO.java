package es.deusto.sd.ecoembes.dto;

import java.time.LocalDate;

import es.deusto.sd.ecoembes.entity.FillLevel;

public class DumpsterUsageDTO {
	private LocalDate date;
	private int numContainers;
	private FillLevel fillLevel;

	// Constructor without parameters
	public DumpsterUsageDTO() { }

	// Constructor with parameters
	public DumpsterUsageDTO(LocalDate date, int numContainers, FillLevel fillLevel) {
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
}
