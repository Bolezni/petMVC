ALTER TABLE user_entity ADD COLUMN avatar_id BIGINT;

ALTER TABLE user_entity
    ADD CONSTRAINT fk_user_avatar
    FOREIGN KEY (avatar_id) REFERENCES media_file(id) ON DELETE SET NULL;

CREATE INDEX idx_user_entity_avatar_id ON user_entity(avatar_id);