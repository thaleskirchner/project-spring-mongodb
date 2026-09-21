package com.thaleskirchner.workshopmongo.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.junit.jupiter.api.Test;

import com.thaleskirchner.workshopmongo.dto.AuthorDTO;

/**
 * Unit tests for the {@link Post} domain entity: field access through the
 * getters/setters generated for persistence, and the identity contract
 * (equals/hashCode based solely on the MongoDB document id) that Spring Data
 * and collections such as {@code Set} rely on.
 */
class PostTest {

	@Test
	void noArgsConstructorStartsWithEmptyCommentList() {
		Post post = new Post();

		assertThat(post.getId()).isNull();
		assertThat(post.getComments()).isEmpty();
	}

	@Test
	void allArgsConstructorAndGettersExposeProvidedValues() {
		Date date = new Date();
		AuthorDTO author = new AuthorDTO(new User("1", "Maria", "maria@gmail.com"));

		Post post = new Post("100", date, "Title", "Body", author);

		assertThat(post.getId()).isEqualTo("100");
		assertThat(post.getDate()).isEqualTo(date);
		assertThat(post.getTitle()).isEqualTo("Title");
		assertThat(post.getBody()).isEqualTo("Body");
		assertThat(post.getAuthor()).isSameAs(author);
	}

	@Test
	void settersUpdateAllMutableFields() {
		Post post = new Post();
		Date date = new Date();
		AuthorDTO author = new AuthorDTO(new User("1", "Maria", "maria@gmail.com"));

		post.setId("200");
		post.setDate(date);
		post.setTitle("New title");
		post.setBody("New body");
		post.setAuthor(author);
		post.setComments(new java.util.ArrayList<>());

		assertThat(post.getId()).isEqualTo("200");
		assertThat(post.getDate()).isEqualTo(date);
		assertThat(post.getTitle()).isEqualTo("New title");
		assertThat(post.getBody()).isEqualTo("New body");
		assertThat(post.getAuthor()).isSameAs(author);
		assertThat(post.getComments()).isEmpty();
	}

	@Test
	void equalsAndHashCodeAreBasedOnlyOnId() {
		Post post1 = new Post("1", new Date(), "A", "B", null);
		Post post2 = new Post("1", new Date(1234L), "Other", "Other", null);
		Post post3 = new Post("2", new Date(), "A", "B", null);

		assertThat(post1).isEqualTo(post2).hasSameHashCodeAs(post2);
		assertThat(post1).isNotEqualTo(post3);
		assertThat(post1).isEqualTo(post1);
		assertThat(post1).isNotEqualTo(null);
		assertThat(post1).isNotEqualTo("not a post");
	}
}
