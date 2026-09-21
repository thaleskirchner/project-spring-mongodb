package com.thaleskirchner.workshopmongo.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.thaleskirchner.workshopmongo.domain.User;

/**
 * Unit tests for {@link UserDTO}: the public-facing representation of a
 * {@link User} (no embedded posts) used by {@code UserResource}.
 */
class UserDTOTest {

	@Test
	void copiesIdNameAndEmailFromUser() {
		User user = new User("1", "Maria Brown", "maria@gmail.com");

		UserDTO dto = new UserDTO(user);

		assertThat(dto.getId()).isEqualTo("1");
		assertThat(dto.getName()).isEqualTo("Maria Brown");
		assertThat(dto.getEmail()).isEqualTo("maria@gmail.com");
	}

	@Test
	void noArgsConstructorAndSettersAllowManualPopulation() {
		UserDTO dto = new UserDTO();

		dto.setId("2");
		dto.setName("Alex Green");
		dto.setEmail("alex@gmail.com");

		assertThat(dto.getId()).isEqualTo("2");
		assertThat(dto.getName()).isEqualTo("Alex Green");
		assertThat(dto.getEmail()).isEqualTo("alex@gmail.com");
	}
}
