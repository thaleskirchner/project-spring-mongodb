package com.thaleskirchner.workshopmongo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.thaleskirchner.workshopmongo.repositories.PostRepository;
import com.thaleskirchner.workshopmongo.repositories.UserRepository;

/**
 * Smoke test: boots the full Spring application context and checks that
 * every bean - controllers, services and repositories - wires up correctly.
 * {@link UserRepository} and {@link PostRepository} are mocked so the
 * context can start (and the {@code Instantiation} startup seed can run)
 * without a MongoDB instance available; the query behaviour of the
 * repositories themselves is exercised separately in the integration tests.
 */
@SpringBootTest
class WorkshopmongoApplicationTests {

	@MockitoBean
	private UserRepository userRepository;

	@MockitoBean
	private PostRepository postRepository;

	@Test
	void contextLoads() {
	}

}
