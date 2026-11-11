package com.edifika.notification.application.internal.commandservices;

import com.edifika.notification.domain.model.aggregates.DeviceToken;
import com.edifika.notification.domain.model.entities.DeviceTokenEntity;
import com.edifika.notification.domain.services.DeviceTokenCommandService;
import com.edifika.notification.infrastructure.persistence.jpa.repositories.JpaDeviceTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class DeviceTokenCommandServiceImpl implements DeviceTokenCommandService {

    private final JpaDeviceTokenRepository jpaDeviceTokenRepository;

    public DeviceTokenCommandServiceImpl(JpaDeviceTokenRepository jpaDeviceTokenRepository) {
        this.jpaDeviceTokenRepository = jpaDeviceTokenRepository;
    }

    /**
     * Registra el token FCM del teléfono del usuario (idempotente).
     * <ul>
     *     <li>La app lo envía en cada inicio de sesión y cuando Firebase lo rota.</li>
     *     <li>Un usuario tiene un solo dispositivo vigente: sus tokens anteriores se eliminan.</li>
     *     <li>Si el token ya existía (otro usuario usó el mismo teléfono) se reasigna,
     *         evitando la violación de la restricción única de {@code token}.</li>
     * </ul>
     */
    @Override
    @Transactional
    public DeviceToken registerToken(Long userId, String token) {
        DeviceTokenEntity entity = jpaDeviceTokenRepository.findByToken(token)
                .orElseGet(DeviceTokenEntity::new);

        List<DeviceTokenEntity> previous = jpaDeviceTokenRepository.findAllByUserId(userId).stream()
                .filter(other -> !Objects.equals(other.getId(), entity.getId()))
                .toList();
        if (!previous.isEmpty()) {
            jpaDeviceTokenRepository.deleteAll(previous);
        }

        entity.setUserId(userId);
        entity.setToken(token);

        DeviceTokenEntity savedEntity = jpaDeviceTokenRepository.save(entity);
        return mapToDomain(savedEntity);
    }

    private DeviceToken mapToDomain(DeviceTokenEntity entity) {
        return new DeviceToken(
                entity.getId(),
                entity.getUserId(),
                entity.getToken(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
