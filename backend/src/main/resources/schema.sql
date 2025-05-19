-- 创建表单数据表
CREATE TABLE IF NOT EXISTS form_data (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT,
    id_number TEXT,
    phone_number TEXT,
    address TEXT,
    work_unit TEXT,
    position TEXT,
    remark TEXT,
    image_path TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);