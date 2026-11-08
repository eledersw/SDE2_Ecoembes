package es.deusto.sd.ecoembes.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Assignment {
	private long id;
	private LocalDate date;
	private int numDumpsters;
	private int totalContainers;
	private Employee employee;
	private RecyclingPlant plant;
	private List<Dumpster> dumpsters = new ArrayList<>();

	// Constructor without parameters
	public Assignment() { }

	// Constructor with parameters
	public Assignment(long id, LocalDate date, Employee employee,
					  RecyclingPlant plant, List<Dumpster> dumpsters) {
		this.id = id;
		this.date = date;
		this.employee = employee;
		this.plant = plant;
		this.dumpsters = new ArrayList<>(dumpsters);
		this.numDumpsters = dumpsters.size();
		this.totalContainers = dumpsters.stream().mapToInt(Dumpster::getNumContainers).sum();
	}

	// Getters and setters
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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

	public Employee getEmployee() {
		return employee;
	}

	public void setEmployee(Employee employee) {
		this.employee = employee;
	}

	public RecyclingPlant getPlant() {
		return plant;
	}

	public void setPlant(RecyclingPlant plant) {
		this.plant = plant;
	}

	public List<Dumpster> getDumpsters() {
		return dumpsters;
	}

	public void setDumpsters(List<Dumpster> dumpsters) {
		this.dumpsters = dumpsters;
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
		Assignment other = (Assignment) obj;
		return id == other.id;
	}
}
