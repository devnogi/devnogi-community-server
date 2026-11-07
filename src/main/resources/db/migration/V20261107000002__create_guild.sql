CREATE TABLE guild (
 id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(50) NOT NULL, server VARCHAR(50) NOT NULL,
 master_id BIGINT NULL, register_id BIGINT NOT NULL, registered_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
 active_name VARCHAR(50) GENERATED ALWAYS AS (IF(is_deleted,NULL,name)) STORED,
 UNIQUE(server,active_name), FOREIGN KEY(master_id) REFERENCES user_summary(id), FOREIGN KEY(register_id) REFERENCES user_summary(id)
);
CREATE TABLE guild_registration_request (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(50) NOT NULL, description VARCHAR(2000) NOT NULL,
 server VARCHAR(50) NOT NULL, character_name VARCHAR(50) NOT NULL, applicant_id BIGINT NOT NULL,
 status ENUM('PENDING','APPROVED','REJECTED','CANCELLED') NOT NULL DEFAULT 'PENDING',
 created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, guild_id INT,
 pending_name VARCHAR(50) GENERATED ALWAYS AS (IF(status='PENDING',name,NULL)) STORED, UNIQUE(server,pending_name),
 FOREIGN KEY(applicant_id) REFERENCES user_summary(id), FOREIGN KEY(guild_id) REFERENCES guild(id)
);
CREATE TABLE guild_registration_review (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, request_id BIGINT NOT NULL UNIQUE, reviewer_id BIGINT NOT NULL,
 decision ENUM('APPROVED','REJECTED') NOT NULL, reason VARCHAR(2000), reviewed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY(request_id) REFERENCES guild_registration_request(id), FOREIGN KEY(reviewer_id) REFERENCES user_summary(id)
);
CREATE TABLE guild_member (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,guild_id INT NOT NULL,user_id BIGINT NOT NULL,role ENUM('MASTER','DEPUTY','MEMBER') NOT NULL,
 joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,left_at DATETIME NULL,
 active_user BIGINT GENERATED ALWAYS AS (IF(left_at IS NULL,user_id,NULL)) STORED UNIQUE,
 active_master INT GENERATED ALWAYS AS (IF(left_at IS NULL AND role='MASTER',guild_id,NULL)) STORED UNIQUE,
 INDEX(guild_id,user_id,left_at), FOREIGN KEY(guild_id) REFERENCES guild(id), FOREIGN KEY(user_id) REFERENCES user_summary(id)
);
CREATE TABLE guild_profile (
 guild_id INT PRIMARY KEY,tagline VARCHAR(200) NOT NULL DEFAULT '',description TEXT,emblem_key VARCHAR(255),banner_key VARCHAR(255),
 theme VARCHAR(32) NOT NULL DEFAULT 'FOREST',tags VARCHAR(1000),activity_time VARCHAR(200),updated_by BIGINT,updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 FOREIGN KEY(guild_id) REFERENCES guild(id),FOREIGN KEY(updated_by) REFERENCES user_summary(id)
);
CREATE TABLE guild_post (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,guild_id INT NOT NULL,author_id BIGINT NOT NULL,title VARCHAR(200) NOT NULL,body TEXT NOT NULL,photo_key VARCHAR(255),
 visibility ENUM('PUBLIC','MEMBERS') NOT NULL DEFAULT 'MEMBERS',created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 is_deleted BOOLEAN NOT NULL DEFAULT FALSE,INDEX(guild_id,visibility,created_at), FOREIGN KEY(guild_id) REFERENCES guild(id),FOREIGN KEY(author_id) REFERENCES user_summary(id)
);
CREATE TABLE guild_recruitment_post (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,guild_id INT NOT NULL,author_id BIGINT NOT NULL,title VARCHAR(200) NOT NULL,body TEXT NOT NULL,requirements TEXT,
 status ENUM('OPEN','CLOSED') NOT NULL DEFAULT 'OPEN',created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,closed_at DATETIME,
 is_deleted BOOLEAN NOT NULL DEFAULT FALSE,INDEX(guild_id,status,created_at),FOREIGN KEY(guild_id) REFERENCES guild(id),FOREIGN KEY(author_id) REFERENCES user_summary(id)
);
CREATE TABLE guild_join_application (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,recruitment_id BIGINT NOT NULL,applicant_id BIGINT NOT NULL,server VARCHAR(50) NOT NULL,character_name VARCHAR(50) NOT NULL,message TEXT,
 status ENUM('PENDING','ACCEPTED','REJECTED','CANCELLED') NOT NULL DEFAULT 'PENDING',created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,reviewer_id BIGINT,reviewed_at DATETIME,
 pending_user BIGINT GENERATED ALWAYS AS (IF(status='PENDING',applicant_id,NULL)) STORED, UNIQUE(recruitment_id,pending_user),INDEX(recruitment_id,status,created_at),
 FOREIGN KEY(recruitment_id) REFERENCES guild_recruitment_post(id),FOREIGN KEY(applicant_id) REFERENCES user_summary(id),FOREIGN KEY(reviewer_id) REFERENCES user_summary(id)
);
