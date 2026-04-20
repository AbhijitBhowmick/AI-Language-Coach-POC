-- ============================================================================
-- AI-Language-Coach: Database Initialization
-- Phase 1 - Infrastructure Setup
-- ============================================================================
-- This script creates the initial tenant databases.
-- Run AFTER the PostgreSQL container is running.
-- ============================================================================

-- Function to create database if it doesn't exist
DO $db_check$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'language_coach') THEN
        CREATE DATABASE language_coach;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'stock_analytics') THEN
        CREATE DATABASE stock_analytics;
    END IF;
END $db_check$;

-- List databases to verify
-- SELECT datname FROM pg_database WHERE datname IN ('language_coach', 'stock_analytics');