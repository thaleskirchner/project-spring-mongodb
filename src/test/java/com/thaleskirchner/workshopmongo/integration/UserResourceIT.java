package com.thaleskirchner.workshopmongo.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.thaleskirchner.workshopmongo.domain.Post;
import com.thaleskirchner.workshopmongo.domain.User;
import com.thaleskirchner.workshopmongo.dto.UserDTO;
import com.thaleskirchner.workshopmongo.repositories.PostRepository;
import com.thaleskirchner.workshopmongo.repositories.UserRepository;
import com.thaleskirchner.workshopmongo.resources.exceptions.StandardError;

/**
 * Integration test for the {@code /users} endpoints: boots the full Spring
 * context on a real HTTP port and drives {@link UserResource} through
 * {@link TestRestTemplate}, exercising the real chain
 * Servlet -&gt; Controller -&gt; Service -&gt; Repository -&gt; JSON, plus the
 * {@code @ControllerAdvice} error translation - everything except the
 * MongoDB driver itself, which is stood in for by a mocked
 * {@link UserRepository} so the suite runs without a live database.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class UserResourceIT {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@MockitoBean
	private UserRepository userRepository;

	@MockitoBean
	private PostRepository postRepository;

	private String url(String path) {
		return "http://localhost:" + port + path;
	}

	@Test
	void findAllReturnsUsersAsDTOs() {
		User maria = new User("1", "Maria Brown", "maria@gmail.com");
		User alex = new User("2", "Alex Green", "alex@gmail.com");
		when(userRepository.findAll()).thenReturn(List.of(maria, alex));

		ResponseEntity<UserDTO[]> response = restTemplate.getForEntity(url("/users"), UserDTO[].class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).hasSize(2);
		assertThat(response.getBody()[0].getName()).isEqualTo("Maria Brown");
		assertThat(response.getBody()[1].getEmail()).isEqualTo("alex@gmail.com");
	}

	@Test
	void findByIdReturnsUserWhenPresent() {
		User maria = new User("1", "Maria Brown", "maria@gmail.com");
		when(userRepository.findById("1")).thenReturn(Optional.of(maria));

		ResponseEntity<UserDTO> response = restTemplate.getForEntity(url("/users/1"), UserDTO.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getId()).isEqualTo("1");
		assertThat(response.getBody().getName()).isEqualTo("Maria Brown");
	}

	@Test
	void findByIdReturns404WithStandardErrorBodyWhenAbsent() {
		when(userRepository.findById("missing")).thenReturn(Optional.empty());

		ResponseEntity<StandardError> response = restTemplate.getForEntity(url("/users/missing"), StandardError.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getStatus()).isEqualTo(404);
		assertThat(response.getBody().getPath()).isEqualTo("/users/missing");
	}

	@Test
	void findPostsReturnsThePostsOwnedByTheUser() {
		User maria = new User("1", "Maria Brown", "maria@gmail.com");
		Post post = new Post("10", null, "Bom dia", "Acordei feliz hoje!", null);
		maria.getPosts().add(post);
		when(userRepository.findById("1")).thenReturn(Optional.of(maria));

		ResponseEntity<Post[]> response = restTemplate.getForEntity(url("/users/1/posts"), Post[].class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).hasSize(1);
		assertThat(response.getBody()[0].getTitle()).isEqualTo("Bom dia");
	}

	@Test
	void insertCreatesUserAndReturnsLocationHeader() {
		when(userRepository.insert(any(User.class))).thenAnswer(invocation -> {
			User user = invocation.getArgument(0);
			user.setId("99");
			return user;
		});
		UserDTO newUser = new UserDTO();
		newUser.setName("Bob Grey");
		newUser.setEmail("bob@gmail.com");

		ResponseEntity<Void> response = restTemplate.postForEntity(url("/users"), newUser, Void.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getHeaders().getLocation()).isNotNull();
		assertThat(response.getHeaders().getLocation().toString()).endsWith("/users/99");
	}

	@Test
	void deleteRemovesExistingUserAndReturnsNoContent() {
		User maria = new User("1", "Maria Brown", "maria@gmail.com");
		when(userRepository.findById("1")).thenReturn(Optional.of(maria));

		restTemplate.delete(url("/users/1"));

		verify(userRepository, times(1)).deleteById("1");
	}

	@Test
	void deleteReturnsNotFoundAndDoesNotCallRepositoryWhenUserIsMissing() {
		when(userRepository.findById("missing")).thenReturn(Optional.empty());

		ResponseEntity<StandardError> response = restTemplate.exchange(
				url("/users/missing"), org.springframework.http.HttpMethod.DELETE, null, StandardError.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		verify(userRepository, never()).deleteById(eq("missing"));
	}

	@Test
	void updateChangesNameAndEmailOfExistingUser() {
		User maria = new User("1", "Maria Brown", "maria@gmail.com");
		when(userRepository.findById("1")).thenReturn(Optional.of(maria));
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
		UserDTO updated = new UserDTO();
		updated.setName("Maria Brown Updated");
		updated.setEmail("maria.updated@gmail.com");

		ResponseEntity<Void> response = restTemplate.exchange(
				url("/users/1"), org.springframework.http.HttpMethod.PUT,
				new org.springframework.http.HttpEntity<>(updated), Void.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(maria.getName()).isEqualTo("Maria Brown Updated");
		assertThat(maria.getEmail()).isEqualTo("maria.updated@gmail.com");
	}
}
