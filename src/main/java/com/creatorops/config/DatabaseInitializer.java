package com.creatorops.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Ensures a default organization exists in the database for foreign key constraints validation.
 * Safely handles environments where the organization table is not generated (e.g. H2 fallback).
 */
@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        if (jdbcTemplate != null) {
            try {
                // Attempt to insert a default organization for PostgreSQL V1 DDL references.
                Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM organization WHERE id = 1", Integer.class);
                if (count != null && count == 0) {
                    jdbcTemplate.execute(
                        "INSERT INTO organization (id, name, created_at, updated_at, is_deleted) " +
                        "VALUES (1, 'Default Organization', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false)");
                }
            } catch (Exception e) {
                // Ignore failures (e.g. when table is missing under H2 default profile)
            }
        }
    }
}
