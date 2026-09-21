package com.thaleskirchner.workshopmongo.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link User} domain entity: field access and the
 * identity contract (equals/hashCode based solely on the MongoDB document
 * id), mirroring what {@link PostTest} verifies for {@link Post}.
 */
class UserTest {

	@Test
	void noArgsConstructorStartsWithEmptyPostList() {
		User user = new User();

		assertThat(user.getId()).isNull();
		assertThat(user.getPosts()).isEmpty();
	}

	@Test
	void allArgsConstructorAndGettersExposeProvidedValues() {
		User user = new User("1", "Maria Brown", "maria@gmail.com");

		assertThat(user.getId()).isEqualTo("1");
		assertThat(user.getName()).isEqualTo("Maria Brown");
		assertThat(user.getEmail()).isEqualTo("maria@gmail.com");
		assertThat(user.getPosts()).isEmpty();
	}

	@Test
	void settersUpdateNameAndEmail() {
		User user = new User();

		user.setId("2");
		user.setName("Alex Green");
		user.setEmail("alex@gmail.com");

		assertThat(user.getId()).isEqualTo("2");
		assertThat(user.getName()).isEqualTo("Alex Green");
		assertThat(user.getEmail()).isEqualTo("alex@gmail.com");
	}

	@Test
	void postsListIsMutableThroughGetter() {
		User user = new User("1", "Maria", "maria@gmail.com");
		Post post = new Post("10", null, "Title", "Body", null);

		user.getPosts().add(post);

		assertThat(user.getPosts()).containsExactly(post);
	}

	@Test
	void equalsAndHashCodeAreBasedOnlyOnId() {
		User user1 = new User("1", "Maria", "maria@gmail.com");
		User user2 = new User("1", "Other name", "other@gmail.com");
		User user3 = new User("2", "Maria", "maria@gmail.com");

		assertThat(user1).isEqualTo(user2).hasSameHashCodeAs(user2);
		assertThat(user1).isNotEqualTo(user3);
		assertThat(user1).isEqualTo(user1);
		assertThat(user1).isNotEqualTo(null);
		assertThat(user1).isNotEqualTo("not a user");
	}
}
