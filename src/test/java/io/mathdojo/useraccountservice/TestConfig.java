package io.mathdojo.useraccountservice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import io.mathdojo.useraccountservice.model.User;

/**
 * 
 * Used for injecting mock mongodb repository beans into spring application context when
 * testing with @RunWith(SpringRunner.class)
 *
 */
@TestConfiguration
public class TestConfig {
	Map<String, User> createdUsers = new HashMap<>();

	@Bean
	MathDojoUserRepository userRepo() {
		// configured known user
		createdUsers.put("aKnownUser", new User("aKnownUser", false, "", "", "aKnownOrg"));
		MathDojoUserRepository userRepo = new MathDojoUserRepository() {

			@Override
			public <S extends User> Optional<S> findOne(Example<S> example) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public <S extends User> Page<S> findAll(Example<S> example, Pageable pageable) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public <S extends User> boolean exists(Example<S> example) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public <S extends User> long count(Example<S> example) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public <S extends User> S save(S entity) {
				createdUsers.put(entity.getId(), entity);
				return entity;
			}

			@Override
			public Optional<User> findById(String id) {
				return Optional.of(createdUsers.get(id));
			}

			@Override
			public Iterable<User> findAllById(Iterable<String> ids) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean existsById(String id) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void deleteById(String id) {
				// TODO Auto-generated method stub

			}

			@Override
			public void deleteAll(Iterable<? extends User> entities) {
				// TODO Auto-generated method stub

			}

			@Override
			public void deleteAll() {
				// TODO Auto-generated method stub

			}

			@Override
			public void delete(User entity) {
				// Do nothing for now

			}

			@Override
			public long count() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public Page<User> findAll(Pageable pageable) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public <S extends User> List<S> saveAll(Iterable<S> entities) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public <S extends User> List<S> insert(Iterable<S> entities) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public <S extends User> S insert(S entity) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public <S extends User> List<S> findAll(Example<S> example, Sort sort) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public <S extends User> List<S> findAll(Example<S> example) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public List<User> findAll(Sort sort) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public List<User> findAll() {
				// TODO Auto-generated method stub
				return null;
			}
		};
		return userRepo;
	}

}
