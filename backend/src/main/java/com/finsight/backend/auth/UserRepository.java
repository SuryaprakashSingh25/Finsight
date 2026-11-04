package com.finsight.backend.auth;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepository {

    private final JdbcClient jdbcClient;

    public UserRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @PostConstruct
    public void initTable() {
        jdbcClient.sql("""
            CREATE TABLE IF NOT EXISTS app_users (
                id UUID PRIMARY KEY,
                email VARCHAR(255) UNIQUE NOT NULL,
                password VARCHAR(255) NOT NULL,
                full_name VARCHAR(255) NOT NULL,
                role VARCHAR(50) NOT NULL,
                created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
            );
        """).update();
    }

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT id, email, password, full_name, role, created_at FROM app_users WHERE LOWER(email) = LOWER(:email)";
        return jdbcClient.sql(sql)
                .param("email", email.trim())
                .query((rs, rowNum) -> {
                    Timestamp ts = rs.getTimestamp("created_at");
                    return new User(
                            rs.getObject("id", UUID.class),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("full_name"),
                            rs.getString("role"),
                            ts != null ? ts.toInstant() : null
                    );
                })
                .optional();
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM app_users WHERE LOWER(email) = LOWER(:email)";
        Long count = jdbcClient.sql(sql)
                .param("email", email.trim())
                .query(Long.class)
                .single();
        return count != null && count > 0;
    }

    public void save(UUID id, String email, String encodedPassword, String fullName, String role) {
        // Let PostgreSQL's DEFAULT CURRENT_TIMESTAMP populate created_at
        String sql = """
            INSERT INTO app_users (id, email, password, full_name, role, created_at)
            VALUES (:id, :email, :password, :fullName, :role, CURRENT_TIMESTAMP)
        """;
        jdbcClient.sql(sql)
                .param("id", id)
                .param("email", email.trim().toLowerCase())
                .param("password", encodedPassword)
                .param("fullName", fullName)
                .param("role", role)
                .update();
    }
}