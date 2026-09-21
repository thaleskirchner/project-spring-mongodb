package com.thaleskirchner.workshopmongo.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Date;
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
import com.thaleskirchner.workshopmongo.dto.AuthorDTO;
import com.thaleskirchner.workshopmongo.repositories.PostRepository;
import com.thaleskirchner.workshopmongo.repositories.UserRepository;
import com.thaleskirchner.workshopmongo.resources.exceptions.StandardError;

/**
 * Integration test for the {@code /posts} endpoints: boots the full Spring
 * context on a real HTTP port and drives {@link PostResource} through
 * {@link TestRestTemplate}, checking the request-parameter decoding done by
 * {@code URL} (URL-decoding, date parsing), the delegation to
 * {@link PostRepository}'s custom queries, JSON (de)serialisation of nested
 * {@link AuthorDTO}/{@code CommentDTO} objects, and the 404 error path -
 * with {@link PostRepository} mocked so no live MongoDB is required.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class PostResourceIT {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@MockitoBean
	private PostRepository postRepository;

	@MockitoBean
	private UserRepository userRepository;

	private String url(String path) {
		return "http://localhost:" + port + path;
	}

	@Test
	void findByIdReturnsPostWhenPresent() {
		AuthorDTO author = new AuthorDTO(new User("1", "Maria Brown", "maria@gmail.com"));
		Post post = new Post("10", new Date(), "Bom dia", "Acordei feliz hoje!", author);
		when(postRepository.findById("10")).thenReturn(Optional.of(post));

		ResponseEntity<Post> response = restTemplate.getForEntity(url("/posts/10"), Post.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getTitle()).isEqualTo("Bom dia");
		assertThat(response.getBody().getAuthor().getName()).isEqualTo("Maria Brown");
	}

	@Test
	void findByIdReturns404WithStandardErrorBodyWhenAbsent() {
		when(postRepository.findById("missing")).thenReturn(Optional.empty());

		ResponseEntity<StandardError> response = restTemplate.getForEntity(url("/posts/missing"), StandardError.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getPath()).isEqualTo("/posts/missing");
	}

	@Test
	void findByTitleDecodesTheQueryParamAndDelegatesToRepository() {
		Post post = new Post("10", new Date(), "Bom dia", "Body", null);
		when(postRepository.searchTitle("Bom dia")).thenReturn(List.of(post));

		ResponseEntity<Post[]> response = restTemplate.getForEntity(url("/posts/titlesearch?text=Bom%20dia"), Post[].class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).hasSize(1);
		assertThat(response.getBody()[0].getTitle()).isEqualTo("Bom dia");
	}

	@Test
	void findByTitleDefaultsToEmptyTextWhenParamIsMissing() {
		when(postRepository.searchTitle("")).thenReturn(List.of());

		ResponseEntity<Post[]> response = restTemplate.getForEntity(url("/posts/titlesearch"), Post[].class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEmpty();
	}

	@Test
	void fullSearchParsesDatesAndDelegatesToRepository() {
		Post post = new Post("10", new Date(), "Bom dia", "Body", null);
		when(postRepository.fullSearch(eq("dia"), any(Date.class), any(Date.class))).thenReturn(List.of(post));

		ResponseEntity<Post[]> response = restTemplate.getForEntity(
				url("/posts/fullsearch?text=dia&minDate=2026-01-01&maxDate=2026-12-31"), Post[].class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).hasSize(1);
	}
}
