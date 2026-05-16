CREATE DATABASE IF NOT EXISTS spring_ai_demo
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE spring_ai_demo;

-- user_profile
CREATE TABLE IF NOT EXISTS user_profile (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  nickname    VARCHAR(50)  NOT NULL,
  age         INT,
  gender      VARCHAR(10),
  occupation  VARCHAR(100),
  interests   TEXT,
  personality TEXT,
  extra_info  TEXT,
  created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- chat_session
CREATE TABLE IF NOT EXISTS chat_session (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id     BIGINT       NOT NULL,
  title       VARCHAR(100) DEFAULT 'New Chat',
  created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_user_id (user_id)
);

-- chat_message
CREATE TABLE IF NOT EXISTS chat_message (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  session_id  BIGINT       NOT NULL,
  role        VARCHAR(20)  NOT NULL,
  content     TEXT         NOT NULL,
  created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_session_id (session_id),
  INDEX idx_created_at (created_at)
);

-- ai_config (personality and prompt)
CREATE TABLE IF NOT EXISTS ai_config (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  config_key      VARCHAR(100) NOT NULL UNIQUE,
  config_value    TEXT NOT NULL,
  config_type     VARCHAR(50) NOT NULL,
  description     VARCHAR(255),
  created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_type (config_type)
);

-- ai_model_config (model connection settings)
CREATE TABLE IF NOT EXISTS ai_model_config (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  model_name    VARCHAR(100) NOT NULL,
  provider      VARCHAR(50)  NOT NULL,
  api_key       VARCHAR(500) NOT NULL DEFAULT '',
  base_url      VARCHAR(500) NOT NULL DEFAULT '',
  model_id      VARCHAR(100) NOT NULL,
  temperature   DOUBLE       DEFAULT 0.7,
  max_tokens    INT          DEFAULT 2000,
  is_default    TINYINT      DEFAULT 0,
  is_enabled    TINYINT      DEFAULT 1,
  created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_enabled (is_enabled)
);

-- default personality config
INSERT INTO ai_config (config_key, config_value, config_type, description) VALUES
('ai.role', 'AI Assistant', 'personality', 'AI role'),
('ai.personality', 'Friendly, professional, patient', 'personality', 'AI personality'),
('ai.speaking_style', 'Clear and concise', 'personality', 'AI speaking style'),
('ai.greeting', 'Hello! I am your AI assistant. How can I help you?', 'personality', 'AI greeting');

-- default prompt template
INSERT INTO ai_config (config_key, config_value, config_type, description) VALUES
('ai.system_prompt_template', 'You are a {role} with the following traits: {personality}.\nYour speaking style is: {speaking_style}.\n\n{user_info}\n\nPlease provide personalized and helpful responses based on the above information.', 'prompt', 'System prompt template');

-- default model configs
INSERT INTO ai_model_config (model_name, provider, api_key, base_url, model_id, temperature, max_tokens, is_default, is_enabled) VALUES
('Deepseek Chat', 'openai', '', 'https://api.deepseek.com', 'deepseek-chat', 0.7, 2000, 1, 1);
