package com.edifika.notification.application.internal.commandservices;

import com.edifika.notification.domain.model.aggregates.DeviceToken;
import com.edifika.notification.domain.model.entities.DeviceTokenEntity;
import com.edifika.notification.infrastructure.persistence.jpa.repositories.JpaDeviceTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceTokenCommandServiceImplTest {

    @Mock
    private JpaDeviceTokenRepository jpaDeviceTokenRepository;

    private DeviceTokenCommandServiceImpl deviceTokenCommandService;

    @BeforeEach
    void setUp() {
        deviceTokenCommandService =
                new DeviceTokenCommandServiceImpl(jpaDeviceTokenRepository);
    }

    @Test
    void registerTokenShouldSaveEntityAndReturnMappedDeviceToken() {

        Long userId = 1L;
        String token = "firebase-device-token-123";

        DeviceTokenEntity savedEntity = new DeviceTokenEntity();
        savedEntity.setUserId(userId);
        savedEntity.setToken(token);

        when(jpaDeviceTokenRepository.save(any(DeviceTokenEntity.class)))
                .thenReturn(savedEntity);

        // Act
        DeviceToken result =
                deviceTokenCommandService.registerToken(userId, token);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(token, result.getToken());

        ArgumentCaptor<DeviceTokenEntity> captor =
                ArgumentCaptor.forClass(DeviceTokenEntity.class);

        verify(jpaDeviceTokenRepository).save(captor.capture());

        DeviceTokenEntity capturedEntity = captor.getValue();

        assertEquals(userId, capturedEntity.getUserId());
        assertEquals(token, capturedEntity.getToken());
    }

    @Test
    void registerTokenShouldPropagateExceptionWhenRepositoryFails() {
        // Arrange
        Long userId = 1L;
        String token = "firebase-device-token-123";

        when(jpaDeviceTokenRepository.save(any(DeviceTokenEntity.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> deviceTokenCommandService.registerToken(userId, token)
        );

        // Assert
        assertEquals("Database error", exception.getMessage());

        verify(jpaDeviceTokenRepository, times(1))
                .save(any(DeviceTokenEntity.class));
    }

    private static DeviceTokenEntity entity(Long id, Long userId, String token) {
        DeviceTokenEntity entity = spy(new DeviceTokenEntity(userId, token));
        lenient().when(entity.getId()).thenReturn(id);
        return entity;
    }

    @Test
    void registerTokenReplacesTheUsersPreviousToken() {
        Long userId = 1L;
        DeviceTokenEntity old = entity(10L, userId, "old-token");
        when(jpaDeviceTokenRepository.findByToken("new-token")).thenReturn(java.util.Optional.empty());
        when(jpaDeviceTokenRepository.findAllByUserId(userId)).thenReturn(java.util.List.of(old));
        when(jpaDeviceTokenRepository.save(any(DeviceTokenEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        DeviceToken result = deviceTokenCommandService.registerToken(userId, "new-token");

        assertEquals("new-token", result.getToken());
        verify(jpaDeviceTokenRepository).deleteAll(java.util.List.of(old));
    }

    @Test
    void registeringTheSameTokenAgainUpdatesInsteadOfInserting() {
        Long userId = 1L;
        DeviceTokenEntity existing = entity(10L, userId, "same-token");
        when(jpaDeviceTokenRepository.findByToken("same-token")).thenReturn(java.util.Optional.of(existing));
        when(jpaDeviceTokenRepository.findAllByUserId(userId)).thenReturn(java.util.List.of(existing));
        when(jpaDeviceTokenRepository.save(existing)).thenReturn(existing);

        deviceTokenCommandService.registerToken(userId, "same-token");

        verify(jpaDeviceTokenRepository).save(existing);
        verify(jpaDeviceTokenRepository, never()).deleteAll(any());
    }

    @Test
    void aTokenUsedByAnotherUserIsReassigned() {
        DeviceTokenEntity fromOtherUser = entity(10L, 2L, "shared-phone");
        when(jpaDeviceTokenRepository.findByToken("shared-phone")).thenReturn(java.util.Optional.of(fromOtherUser));
        when(jpaDeviceTokenRepository.findAllByUserId(1L)).thenReturn(java.util.List.of());
        when(jpaDeviceTokenRepository.save(fromOtherUser)).thenReturn(fromOtherUser);

        DeviceToken result = deviceTokenCommandService.registerToken(1L, "shared-phone");

        assertEquals(1L, result.getUserId());
        verify(jpaDeviceTokenRepository, never()).deleteAll(any());
    }
}