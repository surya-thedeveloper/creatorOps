-- V3: Add title column to research_item table
ALTER TABLE research_item ADD COLUMN title VARCHAR(255) NOT NULL;
