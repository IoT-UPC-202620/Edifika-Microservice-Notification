-- Datos de prueba locales (solo corre con SEED_MODE=always). Idempotente.
-- user_id = usuarios de IAM.

INSERT INTO notifications (id, created_at, updated_at, content, status, title, user_id) VALUES
  (1, now() - interval '2 days', now(), 'Tu pago de mantenimiento de octubre fue confirmado.', 'READ',   'Pago confirmado', 2),
  (2, now() - interval '1 day',  now(), 'Tienes una deuda pendiente de S/ 350.00 que vence el 30/11.', 'UNREAD', 'Deuda pendiente', 2),
  (3, now() - interval '3 days', now(), 'Tu reserva del Salon de fiestas fue registrada.', 'UNREAD', 'Reserva registrada', 3),
  (4, now() - interval '1 day',  now(), 'Tu reserva del Gimnasio para manana a las 07:00 esta activa.', 'UNREAD', 'Recordatorio de reserva', 4),
  (5, now() - interval '6 hours', now(), 'Tu tarjeta de acceso fue suspendida por deudas vencidas.', 'UNREAD', 'Acceso suspendido', 5),
  (6, now() - interval '5 hours', now(), 'El lector del lobby de Torre Miraflores esta desconectado.', 'UNREAD', 'Dispositivo IoT offline', 1)
ON CONFLICT DO NOTHING;

INSERT INTO device_tokens (id, created_at, updated_at, token, user_id) VALUES
  (1, now(), now(), 'seed-fcm-token-user-2', 2),
  (2, now(), now(), 'seed-fcm-token-user-3', 3)
ON CONFLICT DO NOTHING;

SELECT setval(pg_get_serial_sequence('notifications', 'id'), (SELECT MAX(id) FROM notifications));
SELECT setval(pg_get_serial_sequence('device_tokens', 'id'), (SELECT MAX(id) FROM device_tokens));
