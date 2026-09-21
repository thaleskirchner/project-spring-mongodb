package com.thaleskirchner.workshopmongo.services.exceptions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link ObjectNotFoundException}: a plain {@link RuntimeException}
 * used by the service layer to signal a missing document, caught later by
 * {@link com.thaleskirchner.workshopmongo.resources.exceptions.ResourceExceptionHandler}.
 */
class ObjectNotFoundExceptionTest {

	@Test
	void carriesTheGivenMessageAndIsUnchecked() {
		ObjectNotFoundException exception = new ObjectNotFoundException("Objeto não encontrado");

		assertThat(exception.getMessage()).isEqualTo("Objeto não encontrado");
		assertThatThrownBy(() -> {
			throw exception;
		}).isInstanceOf(RuntimeException.class);
	}
}
