-- ============================================================
-- PIMS Swing 고객상담 회원정보시스템 MySQL 8.0 스키마
-- (앱 최초 실행 시 DatabaseManager가 자동으로 실행합니다.
--  MySQL Workbench에서 직접 실행하셔도 동일하게 동작합니다.)
-- ============================================================

CREATE DATABASE IF NOT EXISTS pims_tdd
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE pims_tdd;

-- 1. 역할(Role) 테이블
CREATE TABLE IF NOT EXISTS roles (
    role_name   VARCHAR(20)  PRIMARY KEY,
    description VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 사용자/상담사(Users) 테이블
CREATE TABLE IF NOT EXISTS users (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    email        VARCHAR(150) NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,
    role         VARCHAR(20)  NOT NULL,
    phone_number VARCHAR(30)  NULL,
    birth_day    VARCHAR(20)  NULL,
    address      VARCHAR(255) NULL,
    status       VARCHAR(30)  NULL,
    CONSTRAINT fk_users_role FOREIGN KEY (role) REFERENCES roles(role_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 상담 업무(Jobs) 테이블
CREATE TABLE IF NOT EXISTS jobs (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    job_name            VARCHAR(100) NOT NULL,
    description         VARCHAR(255) NULL,
    status              VARCHAR(30)  NOT NULL,
    start_date_time     DATETIME     NOT NULL,
    end_date_time       DATETIME     NULL,
    caller_phone_number VARCHAR(30)  NULL,
    user_id             INT          NULL,
    agent_id            INT          NOT NULL,
    target_agent_id     INT          NULL,
    CONSTRAINT fk_jobs_user         FOREIGN KEY (user_id)         REFERENCES users(id),
    CONSTRAINT fk_jobs_agent        FOREIGN KEY (agent_id)        REFERENCES users(id),
    CONSTRAINT fk_jobs_target_agent FOREIGN KEY (target_agent_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. 상담 로그(Logs) 테이블 (임의검색 로그는 job_id, agent_id, user_id가 NULL)
CREATE TABLE IF NOT EXISTS logs (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    job_id          INT          NULL,
    agent_id        INT          NULL,
    user_id         INT          NULL,
    log_name        VARCHAR(100) NOT NULL,
    log_description VARCHAR(500) NULL,
    created_at      DATETIME     NOT NULL,
    CONSTRAINT fk_logs_job   FOREIGN KEY (job_id)   REFERENCES jobs(id),
    CONSTRAINT fk_logs_agent FOREIGN KEY (agent_id) REFERENCES users(id),
    CONSTRAINT fk_logs_user  FOREIGN KEY (user_id)  REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. 상담사 업무기록(Agent Work Logs) 테이블 (로그인, 로그아웃, 업무중, 자리비움)
CREATE TABLE IF NOT EXISTS agent_work_logs (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    agent_id    INT         NOT NULL,
    category    VARCHAR(50) NOT NULL,
    detail      VARCHAR(50) NOT NULL,
    recorded_at DATETIME    NOT NULL,
    CONSTRAINT fk_agent_work_logs_agent FOREIGN KEY (agent_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 초기 시드 데이터 (중복 실행 안전: INSERT IGNORE)
-- ============================================================
INSERT IGNORE INTO roles (role_name, description) VALUES
    ('상담사', '고객센터(CS) 상담사'),
    ('사용자', '등록된 일반 회원'),
    ('일반',   '비회원 또는 일반 사용자');

-- 샘플 상담사 계정 (비밀번호: 1234) — 3가지 상태(업무 중 / 자리 비움 / 업무중: 전화) 모두 테스트 가능
INSERT IGNORE INTO users (name, email, password, role, phone_number, birth_day, address, status) VALUES
    ('첼시', 'chelsea@pims.com', '1234', '상담사', '02-1000-0001', '950310', '서울시 중구 고객센터', '업무 중'),
    ('미키', 'mickey@pims.com',  '1234', '상담사', '02-1000-0002', '930822', '서울시 중구 고객센터', '자리 비움'),
    ('루시', 'lucy@pims.com',    '1234', '상담사', '02-1000-0003', '970105', '서울시 중구 고객센터', '업무 중'),
    ('제니', 'jenny@pims.com',   '1234', '상담사', '02-1000-0004', '960618', '서울시 중구 고객센터', '업무중: 전화');

-- 샘플 회원 계정
INSERT IGNORE INTO users (name, email, password, role, phone_number, birth_day, address, status) VALUES
    ('John Doe', 'john@example.com',    'pass1234', '사용자', '010-user-xxxx', '99011x', '부산시 해운대구 xx동', '정상'),
    ('김철수',    'chulsoo@example.com', 'pass1234', '사용자', '010-1234-5678', '920515', '서울시 강남구 역삼동 101호', '정상'),
    ('이영희',    'younghee@example.com','pass1234', '사용자', '010-2222-3333', '951103', '부산시 수영구 광안동 202호', '정상'),
    ('박민수',    'minsoo@example.com',  'pass1234', '사용자', '010-5555-7777', '880720', '대전시 서구 둔산동 303호', '정상');
