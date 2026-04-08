ALTER TABLE doctors ADD CONSTRAINT fk_doc_user_soft_del
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE patients ADD CONSTRAINT fk_pat_user_soft_del
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;