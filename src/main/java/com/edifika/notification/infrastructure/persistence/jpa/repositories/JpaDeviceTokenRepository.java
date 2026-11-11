package com.edifika.notification.infrastructure.persistence.jpa.repositories;

import com.edifika.notification.domain.model.entities.DeviceTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaDeviceTokenRepository extends JpaRepository<DeviceTokenEntity, Long> {

    /** Token vigente del usuario: el último registrado. */
    Optional<DeviceTokenEntity> findFirstByUserIdOrderByIdDesc(Long userId);

    List<DeviceTokenEntity> findAllByUserId(Long userId);

    Optional<DeviceTokenEntity> findByToken(String token);
}
