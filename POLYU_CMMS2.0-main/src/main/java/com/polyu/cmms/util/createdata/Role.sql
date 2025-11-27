CREATE TABLE role (
    role_id INT GENERATED ALWAYS AS IDENTITY COMMENT '角色唯一标识',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称（admin/行政官/中层经理/基层员工）',
    role_level INT NOT NULL COMMENT '角色层级（0=admin, 1=executive_officer，2=mid_level_manager，3=base_level_worker）',
    description VARCHAR(200) COMMENT '角色职责描述',
    active_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '角色启用状态（Y=启用，N=禁用）',

    PRIMARY KEY (role_id),

    CONSTRAINT uk_role_name UNIQUE (role_name),

    CONSTRAINT chk_role_name CHECK (role_name IN ('admin', 'executive_officer', 'mid_level_manager', 'base_level_worker')),

    CONSTRAINT chk_role_level CHECK (role_level IN (0, 1, 2, 3)),

    CONSTRAINT chk_role_name_level CHECK (
        (role_level = 0 AND role_name = 'admin')
        OR (role_level = 1 AND role_name = 'executive_officer')
        OR (role_level = 2 AND role_name = 'mid_level_manager')
        OR (role_level = 3 AND role_name = 'base_level_worker')
    ),

    CONSTRAINT chk_active_flag CHECK (active_flag IN ('Y', 'N'))
) COMMENT '存储员工角色信息，通过role_name与role_level强关联确保逻辑一致性';