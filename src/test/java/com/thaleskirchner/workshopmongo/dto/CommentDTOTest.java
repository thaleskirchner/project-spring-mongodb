package com.thaleskirchner.workshopmongo.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.junit.jupiter.api.Test;

import com.thaleskirchner.workshopmongo.domain.User;

/**
 * Unit tests for {@link CommentDTO}: a comment embedded inside a
 * {@link com.thaleskirchner.workshopmongo.domain.Post}, holding its own text,
 * date and author snapshot.
 */
class CommentDTOTest {

	@Test
	void allArgsConstructorAndGettersExposeProvidedValues() {
		Date date = new Date();
		AuthorDTO author = new AuthorDTO(new User("1", "Alex", "alex@gmail.com"));

		CommentDTO comment = new CommentDTO("Boa viagem!", date, author);

		assertThat(comment.getText()).isEqualTo("Boa viagem!");
		assertThat(comment.getDate()).isEqualTo(date);
		assertThat(comment.getAuthor()).isSameAs(author);
	}

	@Test
	void noArgsConstructorAndSettersAllowManualPopulation() {
		CommentDTO comment = new CommentDTO();
		Date date = new Date();
		AuthorDTO author = new AuthorDTO();

		comment.setText("Aproveite");
		comment.setDate(date);
		comment.setAuthor(author);

		assertThat(comment.getText()).isEqualTo("Aproveite");
		assertThat(comment.getDate()).isEqualTo(date);
		assertThat(comment.getAuthor()).isSameAs(author);
	}
}
