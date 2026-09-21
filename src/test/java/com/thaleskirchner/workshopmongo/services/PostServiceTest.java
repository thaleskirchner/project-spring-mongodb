package com.thaleskirchner.workshopmongo.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.thaleskirchner.workshopmongo.domain.Post;
import com.thaleskirchner.workshopmongo.repositories.PostRepository;
import com.thaleskirchner.workshopmongo.services.exceptions.ObjectNotFoundException;

/**
 * Unit tests for {@link PostService}, with {@link PostRepository} mocked so
 * the service's own logic - id lookup, delegation of the title/full-text
 * searches, and the one-day inclusive adjustment applied to {@code maxDate}
 * in {@link PostService#fullSearch} - is verified in isolation from MongoDB.
 */
@ExtendWith(MockitoExtension.class)
class PostServiceTest {

	@Mock
	private PostRepository repository;

	@InjectMocks
	private PostService service;

	@Test
	void findByIdReturnsPostWhenPresent() {
		Post post = new Post("1", new Date(), "Title", "Body", null);
		when(repository.findById("1")).thenReturn(Optional.of(post));

		Post found = service.findById("1");

		assertThat(found).isSameAs(post);
	}

	@Test
	void findByIdThrowsObjectNotFoundExceptionWhenAbsent() {
		when(repository.findById("missing")).thenReturn(Optional.empty());

		assertThatExceptionOfType(ObjectNotFoundException.class)
				.isThrownBy(() -> service.findById("missing"));
	}

	@Test
	void findByTitleDelegatesToRepositorySearchTitle() {
		Post post = new Post("1", new Date(), "Bom dia", "Body", null);
		when(repository.searchTitle("Bom dia")).thenReturn(List.of(post));

		List<Post> result = service.findByTitle("Bom dia");

		assertThat(result).containsExactly(post);
	}

	@Test
	void fullSearchExtendsMaxDateByOneDayAndDelegatesToRepository() {
		Date minDate = new Date(0L);
		Date maxDate = new Date(1_000_000_000L);
		Post post = new Post("1", new Date(), "Title", "Body", null);
		when(repository.fullSearch(eq("viagem"), any(Date.class), any(Date.class))).thenReturn(List.of(post));

		List<Post> result = service.fullSearch("viagem", minDate, maxDate);

		assertThat(result).containsExactly(post);
		ArgumentCaptor<Date> maxDateCaptor = ArgumentCaptor.forClass(Date.class);
		verify(repository).fullSearch(eq("viagem"), eq(minDate), maxDateCaptor.capture());
		long oneDayInMillis = 24 * 60 * 60 * 1000;
		assertThat(maxDateCaptor.getValue().getTime()).isEqualTo(maxDate.getTime() + oneDayInMillis);
	}
}
