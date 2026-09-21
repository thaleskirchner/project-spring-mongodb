package com.thaleskirchner.workshopmongo.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.thaleskirchner.workshopmongo.domain.User;

/**
 * Unit tests for {@link AuthorDTO}: the lightweight projection of a
 * {@link User} (id + name only) embedded in posts and comments.
 */
class AuthorDTOTest {

	@Test
	void copiesIdAndNameFromUser() {
		User user = new User("1", "Maria Brown", "maria@gmail.com");

		AuthorDTO dto = new AuthorDTO(user);

		assertThat(dto.getId()).isEqualTo("1");
		assertThat(dto.getName()).isEqualTo("Maria Brown");
	}

	@Test
	void noArgsConstructorAndSettersAllowManualPopulation() {
		AuthorDTO dto = new AuthorDTO();

		dto.setId("2");
		dto.setName("Alex Green");

		assertThat(dto.getId()).isEqualTo("2");
		assertThat(dto.getName()).isEqualTo("Alex Green");
	}
}
