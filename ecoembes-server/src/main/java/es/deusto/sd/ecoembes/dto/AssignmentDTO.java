package es.deusto.sd.ecoembes.dto;

import java.time.LocalDate;

public class AssignmentDTO {
	private long assignmentId;
	private long plantId;
	private LocalDate date;
	private int numDumpsters;
	private int totalContainers;
	private String empEmail;

	// Constructor without parameters
	public AssignmentDTO() { }

	// Constructor with parameters
	public AssignmentDTO(long assignmentId, long plantId, LocalDate date, int numDumpsters,
						 int totalContainers, String empEmail) {
		this.assignmentId = assignmentId;
		this.plantId = plantId;
		this.date = date;
		this.numDumpsters = numDumpsters;
		this.totalContainers = totalContainers;
		this.empEmail = empEmail;
	}

	// Getters and setters
	public long getAssignmentId() {
		return assignmentId;
	}

	public void setAssignmentId(long assignmentId) {
		this.assignmentId = assignmentId;
	}

	public long getPlantId() {
		return plantId;
	}

	public void setPlantId(long plantId) {
		this.plantId = plantId;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public int getNumDumpsters() {
		return numDumpsters;
	}

	public void setNumDumpsters(int numDumpsters) {
		this.numDumpsters = numDumpsters;
	}

	public int getTotalContainers() {
		return totalContainers;
	}

	public void setTotalContainers(int totalContainers) {
		this.totalContainers = totalContainers;
	}

	public String getEmpEmail() {
		return empEmail;
	}

	public void setEmpEmail(String empEmail) {
		this.empEmail = empEmail;
	}
}
