package com.thaleskirchner.workshopmongo.resources.exceptions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.thaleskirchner.workshopmongo.services.exceptions.ObjectNotFoundException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Unit test for {@link ResourceExceptionHandler}: verifies that an
 * {@link ObjectNotFoundException} raised anywhere in a controller is turned
 * into a {@code 404 Not Found} response carrying a {@link StandardError}
 * body with the failing request's path, independent of any HTTP dispatch.
 */
@ExtendWith(MockitoExtension.class)
class ResourceExceptionHandlerTest {

	@Mock
	private HttpServletRequest request;

	@Test
	void resourceNotFoundBuildsNotFoundResponseWithStandardErrorBody() {
		when(request.getRequestURI()).thenReturn("/users/123");
		ResourceExceptionHandler handler = new ResourceExceptionHandler();
		ObjectNotFoundException exception = new ObjectNotFoundException("Objeto não encontrado");

		ResponseEntity<StandardError> response = handler.resourceNotFound(exception, request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		StandardError body = response.getBody();
		assertThat(body).isNotNull();
		assertThat(body.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
		assertThat(body.getError()).isEqualTo("Not Found");
		assertThat(body.getMessage()).isEqualTo("Objeto não encontrado");
		assertThat(body.getPath()).isEqualTo("/users/123");
		assertThat(body.getTimestamp()).isNotNull();
	}
}
