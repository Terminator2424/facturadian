package com.facturadian.infrastructure.adapter.out.persistence;

import com.facturadian.domain.model.AppUser;
import com.facturadian.domain.port.output.UserRepositoryPort;
import com.facturadian.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.facturadian.infrastructure.adapter.out.persistence.mapper.UserMapper;
import com.facturadian.infrastructure.adapter.out.persistence.repository.UserPanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Optional;

@ApplicationScoped
@Transactional
public class UserRepositoryAdapter implements UserRepositoryPort {

    @Inject
    UserPanacheRepository repository;

    @Override
    public AppUser save(AppUser user) {
        UserEntity entity = UserMapper.toEntity(user);
        if (entity.getId() == null) {
            repository.persist(entity);
        } else {
            entity = repository.getEntityManager().merge(entity);
        }
        return UserMapper.toDomain(entity);
    }

    @Override
    public Optional<AppUser> findByUsername(String username) {
        return repository.find("username", username).firstResultOptional().map(UserMapper::toDomain);
    }

    @Override
    public Optional<AppUser> findByEmail(String email) {
        return repository.find("email", email).firstResultOptional().map(UserMapper::toDomain);
    }
}