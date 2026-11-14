-- Видео-демонстрация упражнения хранится на сервере как обычный медиафайл.
-- ON DELETE SET NULL: удалили файл — упражнение просто остаётся без видео.

ALTER TABLE exercise ADD COLUMN video_media_id BIGINT;

ALTER TABLE exercise
    ADD CONSTRAINT fk_exercise_video_media
    FOREIGN KEY (video_media_id) REFERENCES media_entity (id) ON DELETE SET NULL;

CREATE INDEX idx_exercise_video_media ON exercise (video_media_id);
