package com.thaleskirchner.workshopmongo.resources.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link StandardError}: the JSON error body returned to
 * clients by {@link ResourceExceptionHandler}.
 */
class StandardErrorTest {

	@Test
	void allArgsConstructorAndGettersExposeProvidedValues() {
		Instant timestamp = Instant.now();

		StandardError error = new StandardError(timestamp, 404, "Not Found", "Objeto não encontrado", "/users/1");

		assertThat(error.getTimestamp()).isEqualTo(timestamp);
		assertThat(error.getStatus()).isEqualTo(404);
		assertThat(error.getError()).isEqualTo("Not Found");
		assertThat(error.getMessage()).isEqualTo("Objeto não encontrado");
		assertThat(error.getPath()).isEqualTo("/users/1");
	}

	@Test
	void noArgsConstructorAndSettersAllowManualPopulation() {
		StandardError error = new StandardError();
		Instant timestamp = Instant.now();

		error.setTimestamp(timestamp);
		error.setStatus(500);
		error.setError("Internal Server Error");
		error.setMessage("boom");
		error.setPath("/posts/1");

		assertThat(error.getTimestamp()).isEqualTo(timestamp);
		assertThat(error.getStatus()).isEqualTo(500);
		assertThat(error.getError()).isEqualTo("Internal Server Error");
		assertThat(error.getMessage()).isEqualTo("boom");
		assertThat(error.getPath()).isEqualTo("/posts/1");
	}
}
