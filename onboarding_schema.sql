-- ============================================================================
-- Onboarding Schema - MySQL DDL Script
-- ============================================================================

-- Create the onboarding schema
CREATE SCHEMA IF NOT EXISTS onboarding;
USE onboarding;

-- ============================================================================
-- Status Table (Master Data)
-- ============================================================================
CREATE TABLE `status` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `status` VARCHAR(255) NOT NULL,
    `description` VARCHAR(1000),
    `created_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- Stage Table (Master Data)
-- ============================================================================
CREATE TABLE `stage` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `stage` VARCHAR(255) NOT NULL,
    `stage_desc` VARCHAR(1000),
    `created_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_stage (stage)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- Application Table
-- ============================================================================
CREATE TABLE `application` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `itam_id` VARCHAR(100),
    `itam_instance_id` VARCHAR(100),
    `sbia_rating` VARCHAR(100),
    `app_access_type` VARCHAR(100),
    `status_id` VARCHAR(100),
    `app_deployment_type` VARCHAR(100),
    `app_integration_type` VARCHAR(100),
    `app_notification_dl` VARCHAR(255),
    `auth_level_override` VARCHAR(100),
    `environment_id` VARCHAR(100),
    `target_app_id` VARCHAR(100),
    `target_service_principal_id` VARCHAR(100),
    `target_id` VARCHAR(100),
    `created_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `modified_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `cr_ref_id` VARCHAR(100),
    `authz_groups` TEXT,
    `fail_auth_with_group` TEXT,
    `validation_url` VARCHAR(2000),
    `validation_config_data` LONGTEXT,
    `request_id` VARCHAR(100),
    `planned_go_live_date` DATE,
    `go_live_date` DATE,
    `line_of_business` VARCHAR(100),
    `stage_id` VARCHAR(100),
    `target_claims_mapping_policie_id` VARCHAR(100),
    `tracking_id` VARCHAR(100),
    `test_release_branch` VARCHAR(100),
    `itam_name` VARCHAR(255),
    `work_item_id` VARCHAR(100),
    `created_by` VARCHAR(100),
    `modified_by` VARCHAR(100),
    `display_name` VARCHAR(255),
    `approver_comment` VARCHAR(500),
    `authentication_strength` VARCHAR(100),
    `sign_in_frequency` VARCHAR(100),
    `error_messages` VARCHAR(500),
    `dispensation_id` VARCHAR(100),
    `mfa_enablement` VARCHAR(100),
    `internet_facing` VARCHAR(100),
    
    -- Indexes for commonly searched columns
    INDEX idx_itam_id (itam_id),
    INDEX idx_status_id (status_id),
    INDEX idx_tracking_id (tracking_id),
    INDEX idx_stage_id (stage_id),
    INDEX idx_created_date (created_date),
    INDEX idx_go_live_date (go_live_date),
    INDEX idx_display_name (display_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- Request Table
-- ============================================================================
CREATE TABLE `request` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `itam_id` VARCHAR(100),
    `metadata` JSON,
    `status_id` VARCHAR(100),
    `tracking_id` VARCHAR(100),
    `workitem_id` VARCHAR(100),
    `type` VARCHAR(100),
    `stage_id` VARCHAR(100),
    `created_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `modified_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `parameters` JSON,
    `executed_times` INT DEFAULT 0,
    `created_by` VARCHAR(100),
    `modified_by` VARCHAR(100),
    
    -- Indexes
    INDEX idx_tracking_id (tracking_id),
    INDEX idx_status_id (status_id),
    INDEX idx_stage_id (stage_id),
    INDEX idx_created_date (created_date),
    INDEX idx_workitem_id (workitem_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- IAM Config Table
-- ============================================================================
CREATE TABLE `iam_config` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `config_name` VARCHAR(255),
    `config_type` VARCHAR(100),
    `config_data` LONGTEXT,
    `request_id` BIGINT,
    `created_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `status_id` BIGINT,
    
    -- Indexes
    INDEX idx_request_id (request_id),
    INDEX idx_status_id (status_id),
    INDEX idx_config_name (config_name),
    INDEX idx_config_type (config_type),
    
    -- Foreign Keys
    CONSTRAINT fk_iam_config_request FOREIGN KEY (request_id) REFERENCES `request` (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_iam_config_status FOREIGN KEY (status_id) REFERENCES `status` (id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- SAML Config Table
-- ============================================================================
CREATE TABLE `saml_config` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `application_id` BIGINT,
    `auth_initiation_type` VARCHAR(100),
    `request_signed` VARCHAR(100),
    `response_signed` VARCHAR(100),
    `response_encrypted` VARCHAR(100),
    `request_signed_cert` LONGTEXT,
    `response_encrypted_cert` LONGTEXT,
    `name_id_format` VARCHAR(100),
    `user_attri_in_assertion` LONGTEXT,
    `assertion_consumer_service_url` VARCHAR(500),
    `sp_entity_id` VARCHAR(500),
    `created_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `modified_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Indexes
    INDEX idx_application_id (application_id),
    INDEX idx_sp_entity_id (sp_entity_id),
    
    -- Foreign Keys
    CONSTRAINT fk_saml_config_application FOREIGN KEY (application_id) REFERENCES `application` (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- OpenID Config Table
-- ============================================================================
CREATE TABLE `openid_config` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `application_id` BIGINT,
    `require_user_consent` VARCHAR(100),
    `auth_type` VARCHAR(100),
    `mtls_cert` LONGTEXT,
    `claim_attributes` VARCHAR(500),
    `created_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `modified_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `redirect_url` VARCHAR(1000),
    `grant_type` VARCHAR(100),
    
    -- Indexes
    INDEX idx_application_id (application_id),
    INDEX idx_grant_type (grant_type),
    
    -- Foreign Keys
    CONSTRAINT fk_openid_config_application FOREIGN KEY (application_id) REFERENCES `application` (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- Additional Indexes for Performance
-- ============================================================================
-- All indexes are defined within their respective table definitions

-- ============================================================================
-- Views for Common Queries
-- ============================================================================

-- View: Application with related stage and status information
CREATE OR REPLACE VIEW vw_application_details AS
SELECT 
    a.id,
    a.display_name,
    a.itam_id,
    a.itam_name,
    a.status_id,
    s.status as status_name,
    a.stage_id,
    st.stage,
    a.created_date,
    a.modified_date,
    a.go_live_date,
    a.created_by,
    a.modified_by
FROM `application` a
LEFT JOIN `status` s ON a.status_id = s.id
LEFT JOIN `stage` st ON a.stage_id = st.id;

-- View: Request with related information
CREATE OR REPLACE VIEW vw_request_details AS
SELECT 
    r.id,
    r.tracking_id,
    r.type,
    r.status_id,
    s.status as status_name,
    r.stage_id,
    st.stage,
    r.created_date,
    r.modified_date,
    r.created_by
FROM `request` r
LEFT JOIN `status` s ON r.status_id = s.id
LEFT JOIN `stage` st ON r.stage_id = st.id;
