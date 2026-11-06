package es.deusto.sd.ecoembes.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import es.deusto.sd.ecoembes.entity.Employee;

/**
 * State Management pattern: keeps in memory which employees are logged in,
 * as a map from session token to employee. It is owned by EcoembesAppService.
 */
public class SessionManager {

	// Thread-safe map: Spring serves concurrent HTTP requests on different threads.
	private final Map<Long, Employee> sessions = new ConcurrentHashMap<>();

	// Creates a session and returns its token: the server timestamp (millis) at
	// login time. If two logins happen in the same millisecond, the second one
	// gets the next free number so no session is overwritten.
	public long createSession(Employee emp) {
		long token = System.currentTimeMillis();

		while (sessions.putIfAbsent(token, emp) != null) {
			token++;
		}

		return token;
	}

	// Returns the logged-in employee for this token, or null if the token is not valid.
	public Employee getEmployee(long token) {
		return sessions.get(token);
	}

	// Ends the session (logout).
	public void removeSession(long token) {
		sessions.remove(token);
	}
}
