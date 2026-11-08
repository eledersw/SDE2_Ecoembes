package es.deusto.sd.ecoembes.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import es.deusto.sd.ecoembes.entity.Assignment;
import es.deusto.sd.ecoembes.entity.Dumpster;
import es.deusto.sd.ecoembes.entity.DumpsterMeasurement;
import es.deusto.sd.ecoembes.entity.Employee;
import es.deusto.sd.ecoembes.entity.FillLevel;
import es.deusto.sd.ecoembes.entity.RecyclingPlant;

/** Application Service: business logic of the Central Server. */
@Service
public class EcoembesAppService {

	private static final Logger logger = LoggerFactory.getLogger(EcoembesAppService.class);

	// Assumption: 1 container = 0.01 tons
	private static final double TONS_PER_CONTAINER = 0.01;

	private final SessionManager sessionManager = new SessionManager();

	// AI-generated: Using ConcurrentHashMap for thread-safe access to shared data structures.
	private final Map<String, Employee> employees = new ConcurrentHashMap<>();
	private final Map<Long, Dumpster> dumpsters = new ConcurrentHashMap<>();
	private final Map<Long, RecyclingPlant> plants = new ConcurrentHashMap<>();
	private final List<Assignment> assignments = new ArrayList<>();

	private final Map<Long, Double> plantDailyCapacity = new ConcurrentHashMap<>();

	public EcoembesAppService() {
		loadSampleData();
	}

	// AI-generated: Synchronized methods to ensure thread safety when updating dumpster information.
	public synchronized void updateDumpsterInfo(long dumpsterId, int numContainers, FillLevel fillLevel) {
		Dumpster dumpster = findDumpster(dumpsterId);

		if (fillLevel == null || numContainers < 0 || numContainers > dumpster.getCapacity()) {
			throw new IllegalArgumentException("Invalid dumpster information");
		}

		dumpster.updateInfo(numContainers, fillLevel);
	}

	public long login(String email, String password) {
		Employee employee = (email != null) ? employees.get(email) : null;

		if (employee == null || !employee.getPassword().equals(password)) {
			throw new SecurityException("Invalid credentials");
		}

		return sessionManager.createSession(employee);
	}

	public void logout(long token) {
		checkToken(token);
		sessionManager.removeSession(token);
	}

	public synchronized Dumpster createDumpster(long token, Dumpster dumpster) {
		checkToken(token);

		if (dumpster == null || dumpster.getId() <= 0 || isBlank(dumpster.getAddress())
				|| isBlank(dumpster.getPostalCode()) || dumpster.getCapacity() <= 0
				|| dumpster.getNumContainers() < 0 || dumpster.getNumContainers() > dumpster.getCapacity()
				|| dumpster.getFillLevel() == null) {
			throw new IllegalArgumentException("Invalid dumpster data");
		}

		if (dumpsters.containsKey(dumpster.getId())) {
			throw new IllegalStateException("A dumpster with this ID already exists");
		}

		dumpsters.put(dumpster.getId(), dumpster);

		return dumpster;
	}

	public synchronized List<DumpsterMeasurement> getDumpsterUsage(long token, long dumpsterId,
			LocalDate startDate, LocalDate endDate) {
		checkToken(token);

		if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
			throw new IllegalArgumentException("Invalid date range");
		}

		Dumpster dumpster = findDumpster(dumpsterId);
		// AI-generated: Using stream() to filter and sort dumpster measurements by date range.
		return dumpster.getMeasurements().stream()
				.filter(m -> !m.getDate().isBefore(startDate) && !m.getDate().isAfter(endDate))
				.sorted(Comparator.comparing(DumpsterMeasurement::getDate))
				.toList();
	}

	public synchronized List<Dumpster> getDumpsterStatusByArea(long token, String postalCode, LocalDate date) {
		checkToken(token);

		if (isBlank(postalCode) || date == null) {
			throw new IllegalArgumentException("Postal code and date are required");
		}

		List<Dumpster> result = new ArrayList<>();

		for (Dumpster dumpster : getDumpstersSortedById()) {
			if (!dumpster.getPostalCode().equals(postalCode)) {
				continue;
			}

			for (DumpsterMeasurement m : dumpster.getMeasurements()) {
				if (m.getDate().equals(date)) {
					result.add(new Dumpster(dumpster.getId(), dumpster.getAddress(), dumpster.getPostalCode(),
							dumpster.getCapacity(), m.getNumContainers(), m.getFillLevel()));
				}
			}
		}

		return result;
	}

	public synchronized double getPlantCapacity(long token, long plantId, LocalDate date) {
		checkToken(token);

		if (date == null) {
			throw new IllegalArgumentException("Date is required");
		}

		return availableCapacity(findPlant(plantId), date);
	}

	public synchronized Assignment assignDumpstersToPlant(long token, long plantId, List<Long> dumpsterIds) {
		Employee employee = checkToken(token);
		RecyclingPlant plant = findPlant(plantId);

		if (dumpsterIds == null || dumpsterIds.isEmpty()) {
			throw new IllegalArgumentException("At least one dumpster is required");
		}

		List<Dumpster> selected = new ArrayList<>();

		for (Long dumpsterId : dumpsterIds.stream().distinct().toList()) {
			if (dumpsterId == null) {
				throw new IllegalArgumentException("Invalid dumpster ID");
			}

			selected.add(findDumpster(dumpsterId));
		}

		LocalDate today = LocalDate.now();
		Assignment assignment = new Assignment(assignments.size() + 1, today, employee, plant, selected);

		double requiredTons = round(assignment.getTotalContainers() * TONS_PER_CONTAINER);

		if (requiredTons > availableCapacity(plant, today)) {
			throw new IllegalStateException("Not enough capacity in the recycling plant");
		}

		assignments.add(assignment);

		logger.info("Notification to {}: {} dumpsters with {} containers will arrive on {} (assigned by {})",
				plant.getName(), assignment.getNumDumpsters(), assignment.getTotalContainers(),
				assignment.getDate(), employee.getEmail());

		return assignment;
	}

	public List<Dumpster> getAllDumpsters(long token) {
		checkToken(token);

		return getDumpstersSortedById();
	}

	public List<RecyclingPlant> getAllPlants(long token) {
		checkToken(token);
		// AI-generated: Using stream() to sort recycling plants by ID.
		return plants.values().stream()
				.sorted(Comparator.comparingLong(RecyclingPlant::getId))
				.toList();
	}

	private Employee checkToken(long token) {
		Employee employee = sessionManager.getEmployee(token);

		if (employee == null) {
			throw new SecurityException("Invalid token");
		}

		return employee;
	}

	private Dumpster findDumpster(long dumpsterId) {
		Dumpster dumpster = dumpsters.get(dumpsterId);

		if (dumpster == null) {
			throw new NoSuchElementException("Dumpster not found");
		}

		return dumpster;
	}

	private RecyclingPlant findPlant(long plantId) {
		RecyclingPlant plant = plants.get(plantId);

		if (plant == null) {
			throw new NoSuchElementException("Recycling plant not found");
		}

		return plant;
	}

	private List<Dumpster> getDumpstersSortedById() {
		return dumpsters.values().stream()
				.sorted(Comparator.comparingLong(Dumpster::getId))
				.toList();
	}

	private double availableCapacity(RecyclingPlant plant, LocalDate date) {
		double assignedTons = 0;

		for (Assignment a : assignments) {
			if (a.getPlant().equals(plant) && a.getDate().equals(date)) {
				assignedTons += a.getTotalContainers() * TONS_PER_CONTAINER;
			}
		}

		return round(plantDailyCapacity.get(plant.getId()) - assignedTons);
	}

	private static double round(double value) {
		return Math.round(value * 100) / 100.0;
	}

	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	private void loadSampleData() {
		// AI-generated: Sample data for testing purposes.
		addEmployee(new Employee(1, "Ane Etxeberria", "ane@ecoembes.es", "ane123"));
		addEmployee(new Employee(2, "Jon Garcia", "jon@ecoembes.es", "jon123"));

		addPlant(new RecyclingPlant(1, "PlasSB Ltd."), 20.0);
		addPlant(new RecyclingPlant(2, "ContSocket Ltd."), 12.0);

		addDumpster(1, "Calle Gran Via 1, Bilbao",              "48001", 1000, 300, 420, 510, 640, 720, 850, 910);
		addDumpster(2, "Calle Navarra 3, Bilbao",               "48001",  800, 150, 200, 260, 120, 180, 240, 350);
		addDumpster(3, "Avenida de las Universidades 24, Bilbao", "48007", 1200, 500, 610, 700, 820, 760, 880, 900);
		addDumpster(4, "Calle Lehendakari Aguirre 10, Bilbao",    "48007",  600, 200, 280, 350, 420, 470, 530, 580);
		addDumpster(5, "Calle Madariaga 5, Bilbao",              "48014", 1000, 100, 120, 90, 150, 170, 160, 200);

		logger.info("Sample data loaded: {} employees, {} plants, {} dumpsters",
				employees.size(), plants.size(), dumpsters.size());
	}

	private void addEmployee(Employee employee) {
		employees.put(employee.getEmail(), employee);
	}

	private void addPlant(RecyclingPlant plant, double dailyCapacityTons) {
		plants.put(plant.getId(), plant);
		plantDailyCapacity.put(plant.getId(), dailyCapacityTons);
	}

	// AI-assisted: Method to add a dumpster with daily container measurements and fill levels.
	private void addDumpster(long id, String address, String postalCode, int capacity, int... dailyContainers) {
		Dumpster dumpster = new Dumpster(id, address, postalCode, capacity, 0, FillLevel.GREEN);
		LocalDate firstDay = LocalDate.now().minusDays(dailyContainers.length - 1);

		for (int i = 0; i < dailyContainers.length; i++) {
			int numContainers = dailyContainers[i];
			FillLevel level = sampleFillLevel(numContainers, capacity);

			dumpster.getMeasurements().add(new DumpsterMeasurement(firstDay.plusDays(i), numContainers, level));
			dumpster.setNumContainers(numContainers);
			dumpster.setFillLevel(level);
		}

		dumpsters.put(id, dumpster);
	}

	private static FillLevel sampleFillLevel(int numContainers, int capacity) {
		double percentage = 100.0 * numContainers / capacity;

		if (percentage < 50) {
			return FillLevel.GREEN;
		} else if (percentage < 85) {
			return FillLevel.ORANGE;
		} else {
			return FillLevel.RED;
		}
	}
}
