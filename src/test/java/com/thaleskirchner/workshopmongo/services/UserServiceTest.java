package com.thaleskirchner.workshopmongo.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.thaleskirchner.workshopmongo.domain.User;
import com.thaleskirchner.workshopmongo.dto.UserDTO;
import com.thaleskirchner.workshopmongo.repositories.UserRepository;
import com.thaleskirchner.workshopmongo.services.exceptions.ObjectNotFoundException;

/**
 * Unit tests for {@link UserService}, with {@link UserRepository} mocked.
 * Covers every public operation - listing, lookup (found/not found), insert,
 * delete (including the "must exist first" rule), update and DTO conversion
 * - without touching MongoDB.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository repository;

	@InjectMocks
	private UserService service;

	@Test
	void findAllReturnsWhatRepositoryReturns() {
		User user = new User("1", "Maria", "maria@gmail.com");
		when(repository.findAll()).thenReturn(List.of(user));

		List<User> result = service.findAll();

		assertThat(result).containsExactly(user);
	}

	@Test
	void findByIdReturnsUserWhenPresent() {
		User user = new User("1", "Maria", "maria@gmail.com");
		when(repository.findById("1")).thenReturn(Optional.of(user));

		User found = service.findById("1");

		assertThat(found).isSameAs(user);
	}

	@Test
	void findByIdThrowsObjectNotFoundExceptionWhenAbsent() {
		when(repository.findById("missing")).thenReturn(Optional.empty());

		assertThatExceptionOfType(ObjectNotFoundException.class)
				.isThrownBy(() -> service.findById("missing"));
	}

	@Test
	void insertDelegatesToRepositoryInsert() {
		User user = new User(null, "Bob", "bob@gmail.com");
		User saved = new User("1", "Bob", "bob@gmail.com");
		when(repository.insert(user)).thenReturn(saved);

		User result = service.insert(user);

		assertThat(result).isSameAs(saved);
	}

	@Test
	void deleteRemovesUserWhenItExists() {
		User user = new User("1", "Maria", "maria@gmail.com");
		when(repository.findById("1")).thenReturn(Optional.of(user));

		service.delete("1");

		verify(repository, times(1)).deleteById("1");
	}

	@Test
	void deleteThrowsAndNeverCallsRepositoryDeleteWhenUserIsMissing() {
		when(repository.findById("missing")).thenReturn(Optional.empty());

		assertThatExceptionOfType(ObjectNotFoundException.class)
				.isThrownBy(() -> service.delete("missing"));
		verify(repository, never()).deleteById(any());
	}

	@Test
	void updateLoadsExistingUserCopiesNameAndEmailAndSaves() {
		User existing = new User("1", "Old name", "old@gmail.com");
		User changes = new User("1", "New name", "new@gmail.com");
		when(repository.findById("1")).thenReturn(Optional.of(existing));
		when(repository.save(existing)).thenReturn(existing);

		User result = service.update(changes);

		assertThat(result).isSameAs(existing);
		assertThat(existing.getName()).isEqualTo("New name");
		assertThat(existing.getEmail()).isEqualTo("new@gmail.com");
	}

	@Test
	void fromDTOBuildsUserWithSameIdNameAndEmail() {
		UserDTO dto = new UserDTO();
		dto.setId("1");
		dto.setName("Maria");
		dto.setEmail("maria@gmail.com");

		User user = service.fromDTO(dto);

		assertThat(user.getId()).isEqualTo("1");
		assertThat(user.getName()).isEqualTo("Maria");
		assertThat(user.getEmail()).isEqualTo("maria@gmail.com");
	}
}
