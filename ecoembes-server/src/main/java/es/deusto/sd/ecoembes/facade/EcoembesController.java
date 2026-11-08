package es.deusto.sd.ecoembes.facade;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.deusto.sd.ecoembes.dto.AssignmentDTO;
import es.deusto.sd.ecoembes.dto.DTOAssembler;
import es.deusto.sd.ecoembes.dto.DumpsterDTO;
import es.deusto.sd.ecoembes.dto.DumpsterStatusDTO;
import es.deusto.sd.ecoembes.dto.DumpsterUsageDTO;
import es.deusto.sd.ecoembes.dto.PlantDTO;
import es.deusto.sd.ecoembes.entity.Dumpster;
import es.deusto.sd.ecoembes.entity.FillLevel;
import es.deusto.sd.ecoembes.service.EcoembesAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/** Remote Façade: REST API of the Central Server. */
@RestController
@Tag(name = "Ecoembes Controller", description = "Central Server API: sessions, dumpsters and recycling plants")
public class EcoembesController {

	private final EcoembesAppService appService;
	private final DTOAssembler assembler;

	public EcoembesController(EcoembesAppService appService, DTOAssembler assembler) {
		this.appService = appService;
		this.assembler = assembler;
	}

	@Operation(
		summary = "Update dumpster information (sensor)",
		description = "Daily update sent by the dumpster sensor. Stores the current number of containers and fill level as today's reading. No token required.",
		responses = {
			@ApiResponse(responseCode = "204", description = "No Content: Reading stored"),
			@ApiResponse(responseCode = "400", description = "Bad Request: Invalid number of containers or fill level", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not Found: Dumpster not found", content = @Content)
		}
	)
	@PostMapping("/dumpsters/{dumpsterId}/measurements")
	public ResponseEntity<Void> updateDumpsterInfo(
			@Parameter(name = "dumpsterId", description = "Dumpster ID", required = true, example = "1")
			@PathVariable("dumpsterId") long dumpsterId,
			@Parameter(name = "numContainers", description = "Estimated number of containers in the dumpster", required = true, example = "500")
			@RequestParam("numContainers") int numContainers,
			@Parameter(name = "fillLevel", description = "Fill level: GREEN, ORANGE or RED", required = true, example = "ORANGE")
			@RequestParam("fillLevel") FillLevel fillLevel) {
		try {
			appService.updateDumpsterInfo(dumpsterId, numContainers, fillLevel);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(errorStatus(e));
		}
	}

	@Operation(
		summary = "Login",
		description = "Employee login with email and password. Returns the session token (server timestamp in milliseconds).",
		responses = {
			@ApiResponse(responseCode = "200", description = "OK: Login successful, returns the token"),
			@ApiResponse(responseCode = "401", description = "Unauthorized: Invalid credentials", content = @Content)
		}
	)
	@PostMapping("/sessions")
	public ResponseEntity<Long> login(
			@Parameter(name = "email", description = "Employee email", required = true, example = "ane@ecoembes.es")
			@RequestParam("email") String email,
			@Parameter(name = "password", description = "Employee password", required = true)
			@RequestParam("password") String password) {
		try {
			return new ResponseEntity<>(appService.login(email, password), HttpStatus.OK);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(errorStatus(e));
		}
	}

	@Operation(
		summary = "Logout",
		description = "Ends the employee session. The token can no longer be used.",
		responses = {
			@ApiResponse(responseCode = "204", description = "No Content: Logout successful"),
			@ApiResponse(responseCode = "401", description = "Unauthorized: Invalid token", content = @Content)
		}
	)
	@DeleteMapping("/sessions/{token}")
	public ResponseEntity<Void> logout(
			@Parameter(name = "token", description = "Session token returned by login", required = true)
			@PathVariable("token") long token) {
		try {
			appService.logout(token);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(errorStatus(e));
		}
	}

	@Operation(
		summary = "Create a new dumpster",
		description = "Adds a new dumpster with its identifier, location and initial capacity.",
		responses = {
			@ApiResponse(responseCode = "201", description = "Created: Dumpster created"),
			@ApiResponse(responseCode = "400", description = "Bad Request: Invalid dumpster data", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized: Missing or invalid token", content = @Content),
			@ApiResponse(responseCode = "409", description = "Conflict: A dumpster with this ID already exists", content = @Content)
		}
	)
	@PostMapping("/dumpsters")
	public ResponseEntity<DumpsterDTO> createDumpster(
			@Parameter(name = "token", description = "Session token returned by login", required = true)
			@RequestHeader(value = "token", required = false) Long token,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "New dumpster data", required = true)
			@RequestBody DumpsterDTO dumpster) {
		if (token == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		try {
			Dumpster created = appService.createDumpster(token, assembler.toDumpster(dumpster));
			return new ResponseEntity<>(assembler.toDumpsterDTO(created), HttpStatus.CREATED);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(errorStatus(e));
		}
	}

	@Operation(
		summary = "Get dumpster usage",
		description = "Returns the daily readings of a dumpster between two dates (both included).",
		responses = {
			@ApiResponse(responseCode = "200", description = "OK: Usage history returned"),
			@ApiResponse(responseCode = "400", description = "Bad Request: Invalid date range", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized: Missing or invalid token", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not Found: Dumpster not found", content = @Content)
		}
	)
	@GetMapping("/dumpsters/{dumpsterId}/usage")
	public ResponseEntity<List<DumpsterUsageDTO>> getDumpsterUsage(
			@Parameter(name = "token", description = "Session token returned by login", required = true)
			@RequestHeader(value = "token", required = false) Long token,
			@Parameter(name = "dumpsterId", description = "Dumpster ID", required = true, example = "1")
			@PathVariable("dumpsterId") long dumpsterId,
			@Parameter(name = "startDate", description = "Start date (yyyy-MM-dd)", required = true)
			@RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@Parameter(name = "endDate", description = "End date (yyyy-MM-dd)", required = true)
			@RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
		if (token == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		try {
			List<DumpsterUsageDTO> usage = appService.getDumpsterUsage(token, dumpsterId, startDate, endDate)
					.stream().map(assembler::toUsageDTO).toList();
			return new ResponseEntity<>(usage, HttpStatus.OK);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(errorStatus(e));
		}
	}

	@Operation(
		summary = "Get dumpster status by area",
		description = "Returns the status (containers and fill level) of the dumpsters of a postal code on a given day.",
		responses = {
			@ApiResponse(responseCode = "200", description = "OK: Status of the dumpsters returned"),
			@ApiResponse(responseCode = "400", description = "Bad Request: Invalid postal code or date", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized: Missing or invalid token", content = @Content)
		}
	)
	@GetMapping("/dumpsters/status")
	public ResponseEntity<List<DumpsterStatusDTO>> getDumpsterStatusByArea(
			@Parameter(name = "token", description = "Session token returned by login", required = true)
			@RequestHeader(value = "token", required = false) Long token,
			@Parameter(name = "postalCode", description = "Postal code of the area", required = true, example = "48001")
			@RequestParam("postalCode") String postalCode,
			@Parameter(name = "date", description = "Day to check (yyyy-MM-dd)", required = true)
			@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		if (token == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		try {
			List<DumpsterStatusDTO> status = appService.getDumpsterStatusByArea(token, postalCode, date)
					.stream().map(assembler::toStatusDTO).toList();
			return new ResponseEntity<>(status, HttpStatus.OK);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(errorStatus(e));
		}
	}

	@Operation(
		summary = "Get recycling plant capacity",
		description = "Returns the available capacity (tons) of a recycling plant on a given date.",
		responses = {
			@ApiResponse(responseCode = "200", description = "OK: Available capacity in tons returned"),
			@ApiResponse(responseCode = "400", description = "Bad Request: Invalid date", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized: Missing or invalid token", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not Found: Recycling plant not found", content = @Content)
		}
	)
	@GetMapping("/plants/{plantId}/capacity")
	public ResponseEntity<Double> getPlantCapacity(
			@Parameter(name = "token", description = "Session token returned by login", required = true)
			@RequestHeader(value = "token", required = false) Long token,
			@Parameter(name = "plantId", description = "Recycling plant ID", required = true, example = "1")
			@PathVariable("plantId") long plantId,
			@Parameter(name = "date", description = "Date (yyyy-MM-dd)", required = true)
			@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		if (token == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		try {
			return new ResponseEntity<>(appService.getPlantCapacity(token, plantId, date), HttpStatus.OK);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(errorStatus(e));
		}
	}

	@Operation(
		summary = "Assign dumpsters to a recycling plant",
		description = "Assigns dumpsters to a plant for today if the plant has enough capacity. The plant is notified and the employee is recorded.",
		responses = {
			@ApiResponse(responseCode = "201", description = "Created: Assignment created and plant notified"),
			@ApiResponse(responseCode = "400", description = "Bad Request: Empty or invalid list of dumpsters", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized: Missing or invalid token", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not Found: Recycling plant or dumpster not found", content = @Content),
			@ApiResponse(responseCode = "409", description = "Conflict: Not enough capacity in the recycling plant", content = @Content)
		}
	)
	@PostMapping("/plants/{plantId}/assignments")
	public ResponseEntity<AssignmentDTO> assignDumpstersToPlant(
			@Parameter(name = "token", description = "Session token returned by login", required = true)
			@RequestHeader(value = "token", required = false) Long token,
			@Parameter(name = "plantId", description = "Recycling plant ID", required = true, example = "1")
			@PathVariable("plantId") long plantId,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "IDs of the dumpsters to assign, e.g. [1, 3]", required = true)
			@RequestBody List<Long> dumpsterIds) {
		if (token == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		try {
			AssignmentDTO assignment = assembler.toAssignmentDTO(
					appService.assignDumpstersToPlant(token, plantId, dumpsterIds));
			return new ResponseEntity<>(assignment, HttpStatus.CREATED);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(errorStatus(e));
		}
	}

	@Operation(
		summary = "Get all dumpsters",
		description = "Returns all dumpsters with their location, number of containers and current fill level.",
		responses = {
			@ApiResponse(responseCode = "200", description = "OK: List of dumpsters returned"),
			@ApiResponse(responseCode = "401", description = "Unauthorized: Missing or invalid token", content = @Content)
		}
	)
	@GetMapping("/dumpsters")
	public ResponseEntity<List<DumpsterDTO>> getAllDumpsters(
			@Parameter(name = "token", description = "Session token returned by login", required = true)
			@RequestHeader(value = "token", required = false) Long token) {
		if (token == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		try {
			List<DumpsterDTO> dumpsters = appService.getAllDumpsters(token)
					.stream().map(assembler::toDumpsterDTO).toList();
			return new ResponseEntity<>(dumpsters, HttpStatus.OK);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(errorStatus(e));
		}
	}

	@Operation(
		summary = "Get all recycling plants",
		description = "Returns all recycling plants.",
		responses = {
			@ApiResponse(responseCode = "200", description = "OK: List of recycling plants returned"),
			@ApiResponse(responseCode = "401", description = "Unauthorized: Missing or invalid token", content = @Content)
		}
	)
	@GetMapping("/plants")
	public ResponseEntity<List<PlantDTO>> getAllPlants(
			@Parameter(name = "token", description = "Session token returned by login", required = true)
			@RequestHeader(value = "token", required = false) Long token) {
		if (token == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		try {
			List<PlantDTO> plants = appService.getAllPlants(token)
					.stream().map(assembler::toPlantDTO).toList();
			return new ResponseEntity<>(plants, HttpStatus.OK);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(errorStatus(e));
		}
	}

	private HttpStatus errorStatus(RuntimeException e) {
		if (e instanceof SecurityException) {
			return HttpStatus.UNAUTHORIZED;
		} else if (e instanceof IllegalArgumentException) {
			return HttpStatus.BAD_REQUEST;
		} else if (e instanceof NoSuchElementException) {
			return HttpStatus.NOT_FOUND;
		} else if (e instanceof IllegalStateException) {
			return HttpStatus.CONFLICT;
		} else {
			return HttpStatus.INTERNAL_SERVER_ERROR;
		}
	}
}
