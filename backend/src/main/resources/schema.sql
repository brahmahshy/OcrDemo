-- 创建表单数据表
CREATE TABLE IF NOT EXISTS form_data (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    logId TEXT,
    projectName TEXT,
    constructionUnit TEXT,
    projectAddress TEXT,
    constructionPart TEXT,
    strengthLevel TEXT,
    currentVolume TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);