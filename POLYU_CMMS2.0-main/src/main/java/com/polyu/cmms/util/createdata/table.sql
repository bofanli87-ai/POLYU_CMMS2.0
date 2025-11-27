/* Enter "USE {database};" to start exploring your data.
   Press Ctrl + I to try out AI-generated SQL queries or SQL rewrite using Chat2Query. */
use test1;
-- 1. 角色表（基础表，无外键依赖）
CREATE TABLE role (
    role_id INT AUTO_INCREMENT COMMENT '角色唯一标识',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称（行政官/中层经理/基层员工）',
    role_level INT NOT NULL COMMENT '角色层级（1=行政官，2=中层经理，3=基层员工）',
    description VARCHAR(200) COMMENT '角色职责描述',
    active_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '角色启用状态（Y=启用，N=禁用）',
    PRIMARY KEY (role_id),
    UNIQUE KEY uk_role_name (role_name),
    CHECK (role_name IN ('executive_officer', 'mid_level_manager', 'base_level_worker')),
    CHECK (role_level IN (1, 2, 3)),
    CHECK ((role_level=1 AND role_name='executive_officer') 
           OR (role_level=2 AND role_name='mid_level_manager') 
           OR (role_level=3 AND role_name='base_level_worker')),
    CHECK (active_flag IN ('Y', 'N'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '存储员工角色信息，通过role_name与role_level强关联确保逻辑一致性';

CREATE TABLE system_limits (
                               system_limits_id INT AUTO_INCREMENT COMMENT 'Configuration unique identifier',
                               max_mid_level_managers INT NOT NULL COMMENT 'Maximum number of mid-level managers limit',
                               max_base_level_workers INT NOT NULL COMMENT 'Maximum number of base-level workers limit',
                               effective_date DATE NOT NULL COMMENT 'Configuration effective date',
                               active_flag CHAR(1) NOT NULL DEFAULT 'N' COMMENT 'Configuration activation status (Y=active, N=inactive)',
                               PRIMARY KEY (system_limits_id),
                               CHECK (max_mid_level_managers >= 0),
                               CHECK (max_base_level_workers >= 0),
                               CHECK (active_flag IN ('Y', 'N'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Stores employee number limit configurations, supports effective date management';

-- Supplementary triggers: Ensure only one active configuration at a time (alternative to filtered index)
DELIMITER //
CREATE TRIGGER trg_system_limits_active_unique_insert
    BEFORE INSERT ON system_limits
    FOR EACH ROW
BEGIN
    -- When inserting with active flag set to 'Y', check if there's already an active configuration
    IF NEW.active_flag = 'Y' THEN
        IF EXISTS (SELECT 1 FROM system_limits WHERE active_flag = 'Y') THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Only one active system configuration is allowed, cannot add multiple active configurations';
END IF;
END IF;
END //

CREATE TRIGGER trg_system_limits_active_unique_update
    BEFORE UPDATE ON system_limits
    FOR EACH ROW
BEGIN
    -- When updating with active flag set to 'Y', check if other configurations are already active
    IF NEW.active_flag = 'Y' THEN
        IF EXISTS (SELECT 1 FROM system_limits WHERE active_flag = 'Y' AND system_limits_id != NEW.system_limits_id) THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Only one active system configuration is allowed, please deactivate the current active configuration first';
END IF;
END IF;
END //
DELIMITER ;

-- 3. Address table (base table, no foreign key dependencies, unified address information management)
CREATE TABLE address (
                         address_id INT AUTO_INCREMENT COMMENT 'Address unique identifier',
                         street VARCHAR(100) COMMENT 'Street address',
                         city VARCHAR(50) COMMENT 'City',
                         postal_code VARCHAR(20) COMMENT 'Postal code',
                         country VARCHAR(50) COMMENT 'Country/region',
                         detail VARCHAR(200) COMMENT 'Detailed address (e.g., building number, house number)',
                         active_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Address activation status (Y=enabled, N=disabled)',
                         PRIMARY KEY (address_id),
                         CHECK (active_flag IN ('Y', 'N'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Unified management of all entity address information, eliminates address redundancy across multiple tables';

-- 4. Staff table (depends on role table)
CREATE TABLE staff (
                       staff_id INT AUTO_INCREMENT COMMENT 'Staff unique identifier',
                       staff_number VARCHAR(20) NOT NULL COMMENT 'Staff number',
                       first_name VARCHAR(50) NOT NULL COMMENT 'First name',
                       last_name VARCHAR(50) NOT NULL COMMENT 'Last name',
                       date_of_birth DATE COMMENT 'Date of birth (replaces age field)',
                       gender CHAR(1) COMMENT 'Gender (F=female, M=male, O=other)',
                       role_id INT NOT NULL COMMENT 'Role ID (references role table)',
                       email VARCHAR(100) COMMENT 'Email address',
                       phone VARCHAR(20) COMMENT 'Phone number',
                       hire_date DATE NOT NULL COMMENT 'Hire date',
                       emergency_contact VARCHAR(50) COMMENT 'Emergency contact',
                       emergency_phone VARCHAR(20) COMMENT 'Emergency contact phone',
                       active_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Staff employment status (Y=active, N=inactive)',
                       PRIMARY KEY (staff_id),
                       UNIQUE KEY uk_staff_number (staff_number),
                       UNIQUE KEY uk_staff_email (email),
                       UNIQUE KEY uk_staff_phone (phone),
                       FOREIGN KEY (role_id) REFERENCES role(role_id) ON DELETE RESTRICT,
                       CHECK (gender IN ('F', 'M', 'O')),
                       CHECK (active_flag IN ('Y', 'N')),
                       INDEX idx_staff_role (role_id),
                       INDEX idx_staff_active_role (active_flag, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Stores basic staff information, linked to role table for permission hierarchy management';

-- 5. Staff skills table (base table, no foreign key dependencies)
CREATE TABLE skill (
                       skill_id INT AUTO_INCREMENT COMMENT 'Skill unique identifier',
                       skill_name VARCHAR(50) NOT NULL COMMENT 'Skill name (e.g., robot operation, chemical usage)',
                       description VARCHAR(200) COMMENT 'Skill description (e.g., cleaning robot operation, high-risk chemical usage)',
                       PRIMARY KEY (skill_id),
                       UNIQUE KEY uk_skill_name (skill_name),
                       CHECK (skill_name IN ('robot_operation', 'chemical_use', 'electrical_repair',
                                             'mechanical_maintenance', 'cleaning_service', 'safety_inspection'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Standardized staff skill classification, supports skill enumeration management';

-- 6. Staff-skill association table (depends on staff, skill tables)
CREATE TABLE staff_skill_map (
                                 staff_id INT NOT NULL COMMENT 'Staff ID (references staff table)',
                                 skill_id INT NOT NULL COMMENT 'Skill ID (references skill table)',
                                 proficiency VARCHAR(20) NOT NULL COMMENT 'Skill proficiency (junior/intermediate/senior)',
                                 PRIMARY KEY (staff_id, skill_id),
                                 FOREIGN KEY (staff_id) REFERENCES staff(staff_id) ON DELETE CASCADE,
                                 FOREIGN KEY (skill_id) REFERENCES skill(skill_id) ON DELETE CASCADE,
                                 CHECK (proficiency IN ('junior', 'intermediate', 'senior')),
                                 INDEX idx_skill_staff (skill_id, staff_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Implements many-to-many relationship between staff and skills, records skill mastery level';

-- 7. Supervision relationship table (depends on staff table, self-referencing)
CREATE TABLE supervise (
                           supervise_id INT AUTO_INCREMENT COMMENT 'Supervision relationship unique identifier',
                           supervisor_staff_id INT NOT NULL COMMENT 'Supervisor staff ID (references staff table)',
                           subordinate_staff_id INT NOT NULL COMMENT 'Subordinate staff ID (references staff table)',
                           start_date DATE NOT NULL COMMENT 'Supervision relationship start date',
                           end_date DATE COMMENT 'Supervision relationship end date (NULL=currently active)',
                           PRIMARY KEY (supervise_id),
                           FOREIGN KEY (supervisor_staff_id) REFERENCES staff(staff_id) ON DELETE CASCADE,
                           FOREIGN KEY (subordinate_staff_id) REFERENCES staff(staff_id) ON DELETE CASCADE,
                           CHECK (supervisor_staff_id != subordinate_staff_id),
    CHECK (end_date IS NULL OR end_date >= start_date),
    UNIQUE KEY uk_supervisor_subordinate (supervisor_staff_id, subordinate_staff_id, end_date)
        COMMENT 'Prevent duplicate active relationships between same supervisor-subordinate',
    INDEX idx_subordinate_supervisor (subordinate_staff_id, supervisor_staff_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Stores hierarchical supervision relationships between staff, supports relationship lifecycle management';

-- 8. External company table (depends on address table)
CREATE TABLE company (
                         contractor_id INT AUTO_INCREMENT COMMENT 'Company unique identifier',
                         contractor_code VARCHAR(20) NOT NULL COMMENT 'Company code',
                         name VARCHAR(100) NOT NULL COMMENT 'Company name',
                         contact_name VARCHAR(50) COMMENT 'Contact person name',
                         contract_quote DECIMAL(10,2) COMMENT 'Standard quote (RMB)',
                         email VARCHAR(100) COMMENT 'Company email',
                         phone VARCHAR(20) COMMENT 'Company phone',
                         address_id INT COMMENT 'Address ID (references address table)',
                         expertise VARCHAR(200) COMMENT 'Expertise area (e.g., building maintenance, cleaning services)',
                         tax_id VARCHAR(50) COMMENT 'Tax registration number',
                         bank_account VARCHAR(100) COMMENT 'Bank account',
                         active_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Cooperation status (Y=cooperating, N=terminated)',
                         PRIMARY KEY (contractor_id),
                         UNIQUE KEY uk_contractor_code (contractor_code),
                         UNIQUE KEY uk_company_email (email),
                         UNIQUE KEY uk_company_phone (phone),
                         UNIQUE KEY uk_company_taxid (tax_id),
                         FOREIGN KEY (address_id) REFERENCES address(address_id) ON DELETE SET NULL,
                         CHECK (contract_quote >= 0),
                         CHECK (active_flag IN ('Y', 'N')),
                         INDEX idx_company_address (address_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Stores outsourcing partner company information, linked to address table to eliminate address redundancy';

-- 9. Robot table (no foreign key dependencies)
CREATE TABLE robot (
                       robot_id INT AUTO_INCREMENT COMMENT 'Robot unique identifier',
                       type VARCHAR(50) NOT NULL COMMENT 'Robot type (cleaning robot/repair robot/inspection robot)',
                       robot_capability VARCHAR(200) COMMENT 'Function description (e.g., floor cleaning, pipeline inspection)',
                       create_date DATE NOT NULL COMMENT 'Manufacturing date',
                       last_maintained_date DATE COMMENT 'Last maintenance date',
                       maintenance_cycle INT NOT NULL COMMENT 'Maintenance cycle (days)',
                       active_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Activation status (Y=enabled, N=disabled)',
                       PRIMARY KEY (robot_id),
                       CHECK (type IN ('cleaning_robot', 'repair_robot', 'inspection_robot')),
                       CHECK (maintenance_cycle >= 1),
                       CHECK (active_flag IN ('Y', 'N')),
                       INDEX idx_robot_active_type (active_flag, type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Stores basic robot information, adds maintenance cycle to support regular maintenance reminders';

-- 10. Robot maintenance record table (depends on robot, staff tables)
CREATE TABLE robot_maintenance (
                                   maintenance_id INT AUTO_INCREMENT COMMENT 'Maintenance record unique identifier',
                                   robot_id INT NOT NULL COMMENT 'Robot ID (references robot table)',
                                   maintenance_date DATE NOT NULL COMMENT 'Maintenance date',
                                   maintenance_type VARCHAR(20) NOT NULL COMMENT 'Maintenance type (routine/fault/overhaul)',
                                   content TEXT COMMENT 'Maintenance content (e.g., part replacement, system upgrade)',
                                   maintained_by_staff_id INT COMMENT 'Maintenance staff ID (references staff table)',
                                   cost DECIMAL(10,2) COMMENT 'Maintenance cost (RMB)',
                                   notes TEXT COMMENT 'Maintenance notes (e.g., fault cause, usage suggestions)',
                                   PRIMARY KEY (maintenance_id),
                                   FOREIGN KEY (robot_id) REFERENCES robot(robot_id) ON DELETE CASCADE,
                                   FOREIGN KEY (maintained_by_staff_id) REFERENCES staff(staff_id) ON DELETE SET NULL,
                                   CHECK (maintenance_type IN ('routine', 'fault', 'overhaul')),
                                   CHECK (cost IS NULL OR cost >= 0),
                                   INDEX idx_robot_maintenance (robot_id, maintenance_date),
                                   INDEX idx_maintenance_staff (maintained_by_staff_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Records robot full lifecycle maintenance history, supports maintenance cost statistics and fault tracing';

-- 11. External area table (depends on address table)
CREATE TABLE area (
                      area_id INT AUTO_INCREMENT COMMENT 'Area unique identifier',
                      area_type VARCHAR(50) NOT NULL COMMENT 'Area type (campus external area/temporary work area/other)',
                      description VARCHAR(200) COMMENT 'Area description (e.g., square outside east campus gate)',
                      address_id INT COMMENT 'Address ID (references address table)',
                      active_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Activation status (Y=enabled, N=disabled)',
                      PRIMARY KEY (area_id),
                      CHECK (area_type IN ('campus_external', 'temporary_work_area', 'other')),
                      CHECK (active_flag IN ('Y', 'N')),
                      FOREIGN KEY (address_id) REFERENCES address(address_id) ON DELETE SET NULL,
                      INDEX idx_area_address (address_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Stores campus external or temporary area information, linked to address table for unified address management';

-- 12. Buildings table (depends on address, staff tables)
CREATE TABLE buildings (
                           building_id INT AUTO_INCREMENT COMMENT 'Building unique identifier',
                           building_code VARCHAR(20) NOT NULL COMMENT 'Building code (e.g., B1, Administration Building)',
                           construction_date DATE COMMENT 'Construction date (replaces usage period field)',
                           address_id INT COMMENT 'Address ID (references address table)',
                           num_floors INT COMMENT 'Number of floors (including underground floors, e.g., -1=1 underground floor)',
                           supervisor_staff_id INT COMMENT 'Responsible manager ID (references staff table, mid-level manager)',
                           active_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Usage status (Y=in use, N=deactivated)',
                           PRIMARY KEY (building_id),
                           UNIQUE KEY uk_building_code (building_code),
                           FOREIGN KEY (address_id) REFERENCES address(address_id) ON DELETE SET NULL,
                           FOREIGN KEY (supervisor_staff_id) REFERENCES staff(staff_id) ON DELETE SET NULL,
                           CHECK (num_floors BETWEEN -5 AND 50),
                           CHECK (active_flag IN ('Y', 'N')),
                           INDEX idx_building_address (address_id),
                           INDEX idx_building_supervisor (supervisor_staff_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Stores campus building information, linked to address table to eliminate address redundancy';

-- 13. Levels table (depends on buildings table) - Fixed auto-increment column exception
CREATE TABLE levels (
                        level_id INT AUTO_INCREMENT COMMENT 'Level unique identifier (globally unique)',
                        building_id INT NOT NULL COMMENT 'Building ID (references buildings table)',
                        level_number INT NOT NULL COMMENT 'Level number (e.g., 3=3rd floor, -1=underground 1st floor)',
                        active_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Usage status (Y=in use, N=deactivated)',
                        PRIMARY KEY (level_id),
                        UNIQUE KEY uk_building_level (building_id, level_number) COMMENT 'Ensure level uniqueness within same building',
                        FOREIGN KEY (building_id) REFERENCES buildings(building_id) ON DELETE CASCADE,
                        CHECK (active_flag IN ('Y', 'N')),
                        INDEX idx_building_level_number (building_id, level_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Stores building level information';

-- 14. Rooms table (depends on buildings table) - Fixed auto-increment column exception
CREATE TABLE rooms (
                       room_id INT AUTO_INCREMENT COMMENT 'Room unique identifier (globally unique)',
                       building_id INT NOT NULL COMMENT 'Building ID (references buildings table)',
                       name VARCHAR(50) NOT NULL COMMENT 'Room name (e.g., Room 302, Equipment Room)',
                       room_type VARCHAR(50) COMMENT 'Room type (e.g., classroom, office, laboratory)',
                       capacity INT COMMENT 'Capacity (number of people)',
                       room_features VARCHAR(200) COMMENT 'Room features (e.g., air conditioning, projector, ventilation system)',
                       active_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Usage status (Y=in use, N=deactivated)',
                       PRIMARY KEY (room_id),
                       UNIQUE KEY uk_building_room (building_id, name) COMMENT 'Ensure room name uniqueness within same building',
                       FOREIGN KEY (building_id) REFERENCES buildings(building_id) ON DELETE CASCADE,
                       CHECK (capacity IS NULL OR capacity >= 0),
                       CHECK (active_flag IN ('Y', 'N')),
                       INDEX idx_building_room_type (building_id, room_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Stores room information within buildings, room names unique within same building';

-- 15. Squares table (depends on address table)
CREATE TABLE squares (
                         square_id INT AUTO_INCREMENT COMMENT 'Square unique identifier',
                         name VARCHAR(50) NOT NULL COMMENT 'Square name (e.g., Central Square, West Square)',
                         address_id INT COMMENT 'Address ID (references address table)',
                         capacity INT COMMENT 'Maximum capacity',
                         active_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Usage status (Y=in use, N=deactivated)',
                         PRIMARY KEY (square_id),
                         UNIQUE KEY uk_square_name (name),
                         FOREIGN KEY (address_id) REFERENCES address(address_id) ON DELETE SET NULL,
                         CHECK (capacity IS NULL OR capacity >= 0),
                         CHECK (active_flag IN ('Y', 'N')),
                         INDEX idx_square_address (address_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Stores campus square information, linked to address table for unified address management';

-- 16. Gates table (depends on address table)
CREATE TABLE gates (
                       gate_id INT AUTO_INCREMENT COMMENT 'Gate unique identifier',
                       name VARCHAR(50) NOT NULL COMMENT 'Gate name (e.g., East Gate, South Gate, Emergency Passage)',
                       address_id INT COMMENT 'Address ID (references address table)',
                       flow_capacity INT COMMENT 'Flow capacity (people/hour)',
                       active_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Usage status (Y=enabled, N=closed)',
                       PRIMARY KEY (gate_id),
                       UNIQUE KEY uk_gate_name (name),
                       FOREIGN KEY (address_id) REFERENCES address(address_id) ON DELETE SET NULL,
                       CHECK (flow_capacity IS NULL OR flow_capacity >= 0),
                       CHECK (active_flag IN ('Y', 'N')),
                       INDEX idx_gate_address (address_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Stores campus gate information, linked to address table to eliminate address redundancy';

-- 17. Canteen table (depends on address table)
CREATE TABLE canteen (
                         canteen_id INT AUTO_INCREMENT COMMENT 'Canteen unique identifier',
                         name VARCHAR(50) NOT NULL COMMENT 'Canteen name (e.g., First Canteen, Halal Canteen)',
                         construction_date DATE COMMENT 'Construction date (replaces usage period field)',
                         address_id INT COMMENT 'Address ID (references address table)',
                         food_type VARCHAR(50) NOT NULL COMMENT 'Food type (Chinese, Western, Mixed)',
                         active_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Business status (Y=open, N=closed)',
                         PRIMARY KEY (canteen_id),
                         UNIQUE KEY uk_canteen_name (name),
                         FOREIGN KEY (address_id) REFERENCES address(address_id) ON DELETE SET NULL,
                         CHECK (food_type IN ('Chinese', 'Western', 'Mixed')),
                         CHECK (active_flag IN ('Y', 'N')),
                         INDEX idx_canteen_address (address_id),
                         INDEX idx_canteen_foodtype (food_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Stores campus canteen information, linked to address table for unified address management';
-- 18. Weather Emergency Event Table (no foreign key dependencies)
CREATE TABLE weather_emergency (
                                   weather_id INT AUTO_INCREMENT COMMENT 'Event unique identifier',
                                   name VARCHAR(100) NOT NULL COMMENT 'Event name (e.g., heavy rain, typhoon, blizzard)',
                                   type VARCHAR(50) NOT NULL COMMENT 'Event type (e.g., rainfall, strong wind, low temperature)',
                                   start_date DATETIME NOT NULL COMMENT 'Start time',
                                   end_date DATETIME NOT NULL COMMENT 'End time',
                                   active_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Event status (Y=active, N=ended)',
                                   PRIMARY KEY (weather_id),
                                   CHECK (type IN ('rainfall', 'strong_wind', 'low_temperature', 'typhoon', 'blizzard', 'other')),
                                   CHECK (start_date <= end_date),
                                   CHECK (active_flag IN ('Y', 'N')),
                                   INDEX idx_weather_active (active_flag, start_date),
                                   INDEX idx_weather_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Stores weather emergency event information, supports precise time recording';

-- 19. Chemical Table (no foreign key dependencies)
CREATE TABLE chemical (
                          chemical_id INT AUTO_INCREMENT COMMENT 'Chemical unique identifier',
                          product_code VARCHAR(20) NOT NULL COMMENT 'Product code',
                          name VARCHAR(100) NOT NULL COMMENT 'Chemical name (e.g., disinfectant, cleaning agent)',
                          type VARCHAR(50) NOT NULL COMMENT 'Chemical type (e.g., disinfectant, solvent, lubricant)',
                          manufacturer VARCHAR(100) COMMENT 'Manufacturer',
                          msds_url VARCHAR(255) COMMENT 'Safety data sheet URL',
                          hazard_category VARCHAR(20) NOT NULL COMMENT 'Hazard category (low, medium, high)',
                          storage_requirements TEXT COMMENT 'Storage requirements (e.g., cool and dry, sealed storage)',
                          active_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Enabled status (Y=enabled, N=disabled)',
                          PRIMARY KEY (chemical_id),
                          UNIQUE KEY uk_product_code (product_code),
                          CHECK (type IN ('disinfectant', 'solvent', 'lubricant', 'detergent', 'other')),
                          CHECK (hazard_category IN ('low', 'medium', 'high')),
                          CHECK (active_flag IN ('Y', 'N')),
                          INDEX idx_chemical_hazard (hazard_category, active_flag),
                          INDEX idx_chemical_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Stores basic chemical information used in activities, supports safety management';

-- 20. Chemical Inventory Table (depends on chemical, company tables)
CREATE TABLE chemical_inventory (
                                    inventory_id INT AUTO_INCREMENT COMMENT 'Inventory record unique identifier',
                                    chemical_id INT NOT NULL COMMENT 'Chemical ID (references chemical table)',
                                    quantity DECIMAL(10,2) NOT NULL COMMENT 'Inventory quantity (unit: bottle/liter/kilogram)',
                                    storage_location VARCHAR(100) NOT NULL COMMENT 'Storage location (e.g., Chemical Warehouse Area A)',
                                    purchase_date DATE NOT NULL COMMENT 'Purchase date',
                                    supplier_id INT COMMENT 'Supplier ID (references company table)',
                                    expiry_date DATE NOT NULL COMMENT 'Expiry date',
                                    batch_number VARCHAR(50) COMMENT 'Purchase batch number',
                                    active_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Inventory status (Y=available, N=unavailable)',
                                    PRIMARY KEY (inventory_id),
                                    FOREIGN KEY (chemical_id) REFERENCES chemical(chemical_id) ON DELETE CASCADE,
                                    FOREIGN KEY (supplier_id) REFERENCES company(contractor_id) ON DELETE SET NULL,
                                    CHECK (quantity >= 0),
                                    CHECK (expiry_date >= purchase_date),
                                    CHECK (active_flag IN ('Y', 'N')),
    -- Passive check: Ensure not expired when available during insert/update
                                    CHECK (active_flag = 'N' OR expiry_date >= CURRENT_DATE()),
                                    INDEX idx_inventory_chemical (chemical_id, active_flag),
                                    INDEX idx_inventory_expiry (expiry_date, active_flag),
                                    INDEX idx_inventory_supplier (supplier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Tracks chemical inventory dynamics, supports inventory alerts and usage management';

-- 21. Activity Table (depends on staff, weather_emergency, area, buildings, etc. tables)
CREATE TABLE activity (
                          activity_id INT AUTO_INCREMENT COMMENT 'Activity unique identifier',
                          activity_type VARCHAR(50) NOT NULL COMMENT 'Activity type (cleaning, repair, weather emergency)',
                          title VARCHAR(100) NOT NULL COMMENT 'Activity title (e.g., Building 3 Floor 3 Cleaning, East Gate Repair)',
                          description TEXT COMMENT 'Activity details (e.g., cleaning scope, repair content)',
                          status VARCHAR(20) NOT NULL DEFAULT 'planned' COMMENT 'Activity status (planned, in_progress, completed, cancelled)',
                          priority VARCHAR(20) NOT NULL DEFAULT 'medium' COMMENT 'Activity priority (low, medium, high)',
                          activity_datetime DATETIME NOT NULL COMMENT 'Activity execution time',
                          expected_unavailable_duration DECIMAL(5,2) NOT NULL COMMENT 'Expected unavailable duration (hours)',
                          actual_completion_datetime DATETIME COMMENT 'Actual completion time (NULL=not completed)',
                          created_by_staff_id INT NOT NULL COMMENT 'Creator ID (references staff table)',
                          weather_id INT COMMENT 'Associated weather event ID (references weather_emergency table)',
                          area_id INT COMMENT 'Associated external area ID (references area table)',
                          hazard_level VARCHAR(20) NOT NULL COMMENT 'Risk level (low, medium, high)',
                          facility_type VARCHAR(20) NOT NULL COMMENT 'Associated facility type (building/room/level/square/gate/canteen/none)',
                          building_id INT COMMENT 'Associated building ID (references buildings table)',
                          room_id INT COMMENT 'Associated room ID (references rooms table)',
                          level_id INT COMMENT 'Associated level ID (references levels table)',
                          square_id INT COMMENT 'Associated square ID (references squares table)',
                          gate_id INT COMMENT 'Associated gate ID (references gates table)',
                          canteen_id INT COMMENT 'Associated canteen ID (references canteen table)',
                          active_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Activity status (Y=active, N=inactive)',
                          PRIMARY KEY (activity_id),
                          FOREIGN KEY (created_by_staff_id) REFERENCES staff(staff_id) ON DELETE RESTRICT,
                          FOREIGN KEY (weather_id) REFERENCES weather_emergency(weather_id) ON DELETE SET NULL,
                          FOREIGN KEY (area_id) REFERENCES area(area_id) ON DELETE SET NULL,
                          FOREIGN KEY (building_id) REFERENCES buildings(building_id) ON DELETE SET NULL,
                          FOREIGN KEY (room_id) REFERENCES rooms(room_id) ON DELETE SET NULL,
                          FOREIGN KEY (level_id) REFERENCES levels(level_id) ON DELETE SET NULL,
                          FOREIGN KEY (square_id) REFERENCES squares(square_id) ON DELETE SET NULL,
                          FOREIGN KEY (gate_id) REFERENCES gates(gate_id) ON DELETE SET NULL,
                          FOREIGN KEY (canteen_id) REFERENCES canteen(canteen_id) ON DELETE SET NULL,
                          CHECK (activity_type IN ('cleaning', 'repair', 'weather_response')),
                          CHECK (status IN ('planned', 'in_progress', 'completed', 'cancelled')),
                          CHECK (priority IN ('low', 'medium', 'high')),
                          CHECK (hazard_level IN ('low', 'medium', 'high')),
                          CHECK (facility_type IN ('building', 'room', 'level', 'square', 'gate', 'canteen', 'none')),
                          CHECK (expected_unavailable_duration >= 0),
                          CHECK (actual_completion_datetime IS NULL OR actual_completion_datetime >= activity_datetime),
                          CHECK (active_flag IN ('Y', 'N')),
    -- Ensure facility type matches associated fields (only one facility field non-null)
                          CHECK (
                              (facility_type = 'building' AND building_id IS NOT NULL AND room_id IS NULL AND level_id IS NULL AND square_id IS NULL AND gate_id IS NULL AND canteen_id IS NULL)
                                  OR (facility_type = 'room' AND room_id IS NOT NULL AND building_id IS NOT NULL AND level_id IS NULL AND square_id IS NULL AND gate_id IS NULL AND canteen_id IS NULL)
                                  OR (facility_type = 'level' AND level_id IS NOT NULL AND building_id IS NOT NULL AND room_id IS NULL AND square_id IS NULL AND gate_id IS NULL AND canteen_id IS NULL)
                                  OR (facility_type = 'square' AND square_id IS NOT NULL AND building_id IS NULL AND room_id IS NULL AND level_id IS NULL AND gate_id IS NULL AND canteen_id IS NULL)
                                  OR (facility_type = 'gate' AND gate_id IS NOT NULL AND building_id IS NULL AND room_id IS NULL AND level_id IS NULL AND square_id IS NULL AND canteen_id IS NULL)
                                  OR (facility_type = 'canteen' AND canteen_id IS NOT NULL AND building_id IS NULL AND room_id IS NULL AND level_id IS NULL AND square_id IS NULL AND gate_id IS NULL)
                                  OR (facility_type = 'none' AND building_id IS NULL AND room_id IS NULL AND level_id IS NULL AND square_id IS NULL AND gate_id IS NULL AND canteen_id IS NULL)
                              ),
                          INDEX idx_activity_status_priority (status, priority),
                          INDEX idx_activity_datetime_type (activity_datetime, activity_type),
                          INDEX idx_activity_facility (facility_type, building_id, room_id, level_id),
                          INDEX idx_activity_creator (created_by_staff_id),
                          INDEX idx_activity_weather (weather_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Stores campus maintenance activity core information, supports multi-scenario activity management';

-- 22. Staff-Activity Association Table (depends on staff, activity tables)
CREATE TABLE works_for (
                           works_for_id INT AUTO_INCREMENT COMMENT 'Association unique identifier',
                           staff_id INT NOT NULL COMMENT 'Staff ID (references staff table)',
                           activity_id INT NOT NULL COMMENT 'Activity ID (references activity table)',
                           activity_responsibility VARCHAR(200) NOT NULL COMMENT 'Staff responsibility in activity (e.g., operate robot, on-site supervision)',
                           assigned_datetime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Assignment time',
                           active_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Association status (Y=active, N=inactive)',
                           PRIMARY KEY (works_for_id),
                           FOREIGN KEY (staff_id) REFERENCES staff(staff_id) ON DELETE CASCADE,
                           FOREIGN KEY (activity_id) REFERENCES activity(activity_id) ON DELETE CASCADE,
                           UNIQUE KEY uk_staff_activity (staff_id, activity_id),
                           CHECK (active_flag IN ('Y', 'N')),
                           INDEX idx_works_for_staff_activity (staff_id, activity_id, active_flag),
                           INDEX idx_works_for_activity (activity_id, active_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Implements many-to-many relationship between staff and activities, records assignment time and responsibilities';

-- 23. Outsourcing Contract Table (depends on company, activity tables)
CREATE TABLE contract (
                          contract_id INT AUTO_INCREMENT COMMENT 'Contract unique identifier',
                          contractor_id INT NOT NULL COMMENT 'Contractor company ID (references company table)',
                          activity_id INT NOT NULL COMMENT 'Associated activity ID (references activity table)',
                          contract_date DATE NOT NULL COMMENT 'Contract signing date',
                          contract_amount DECIMAL(12,2) NOT NULL COMMENT 'Contract amount (RMB)',
                          end_date DATE COMMENT 'Contract end date (NULL=not ended)',
                          status VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT 'Contract status (active, completed, cancelled)',
                          payment_terms VARCHAR(200) COMMENT 'Payment terms (e.g., payment within 30 days after acceptance)',
                          notes TEXT COMMENT 'Contract notes (e.g., acceptance criteria, liability for breach)',
                          PRIMARY KEY (contract_id),
                          FOREIGN KEY (contractor_id) REFERENCES company(contractor_id) ON DELETE CASCADE,
                          FOREIGN KEY (activity_id) REFERENCES activity(activity_id) ON DELETE CASCADE,
                          UNIQUE KEY uk_contract_activity (activity_id),
                          CHECK (contract_amount >= 0),
                          CHECK (status IN ('active', 'completed', 'cancelled')),
                          CHECK (end_date IS NULL OR contract_date <= end_date),
                          INDEX idx_contract_company (contractor_id, status),
                          INDEX idx_contract_date (contract_date),
                          INDEX idx_contract_activity_status (activity_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Stores activity outsourcing contract information, ensures one activity corresponds to only one outsourcing contract';

-- 24. Robot Usage Table (depends on robot, activity, staff tables)
CREATE TABLE robot_usage (
                             robot_usage_id INT AUTO_INCREMENT COMMENT 'Usage record unique identifier',
                             robot_id INT NOT NULL COMMENT 'Robot ID (references robot table)',
                             activity_id INT NOT NULL COMMENT 'Activity ID (references activity table)',
                             usage_datetime DATETIME NOT NULL COMMENT 'Usage time',
                             operator_staff_id INT COMMENT 'Operator staff ID (references staff table)',
                             usage_duration DECIMAL(5,2) NOT NULL COMMENT 'Usage duration (hours)',
                             usage_quantity INT NOT NULL COMMENT 'Usage count (e.g., startup times)',
                             notes TEXT COMMENT 'Usage notes (e.g., fault records, operation points)',
                             PRIMARY KEY (robot_usage_id),
                             FOREIGN KEY (robot_id) REFERENCES robot(robot_id) ON DELETE CASCADE,
                             FOREIGN KEY (activity_id) REFERENCES activity(activity_id) ON DELETE CASCADE,
                             FOREIGN KEY (operator_staff_id) REFERENCES staff(staff_id) ON DELETE SET NULL,
                             CHECK (usage_duration >= 0),
                             CHECK (usage_quantity >= 1),
                             UNIQUE KEY uk_robot_activity_datetime (robot_id, activity_id, usage_datetime),
                             INDEX idx_robot_usage_robot (robot_id, usage_datetime),
                             INDEX idx_robot_usage_activity (activity_id, robot_id),
                             INDEX idx_robot_usage_operator (operator_staff_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Stores robot participation records in activities, ensures data integrity';

-- 25. Chemical Safety Check Table (depends on activity, chemical, staff tables)
CREATE TABLE safety_check (
                              safety_check_id INT AUTO_INCREMENT COMMENT 'Check record unique identifier',
                              activity_id INT NOT NULL COMMENT 'Activity ID (references activity table)',
                              chemical_id INT NOT NULL COMMENT 'Chemical ID (references chemical table)',
                              check_datetime DATETIME NOT NULL COMMENT 'Check time',
                              checked_by_staff_id INT NOT NULL COMMENT 'Checker staff ID (references staff table)',
                              check_items TEXT NOT NULL COMMENT 'Check items details (e.g., concentration compliance, storage compliance)',
                              check_result VARCHAR(20) NOT NULL COMMENT 'Check result (passed, failed, pending)',
                              rectification_measures TEXT COMMENT 'Rectification measures (e.g., handling plan if failed)',
                              notes TEXT COMMENT 'Check notes (e.g., environmental conditions, special circumstances)',
                              PRIMARY KEY (safety_check_id),
                              FOREIGN KEY (activity_id) REFERENCES activity(activity_id) ON DELETE CASCADE,
                              FOREIGN KEY (chemical_id) REFERENCES chemical(chemical_id) ON DELETE CASCADE,
                              FOREIGN KEY (checked_by_staff_id) REFERENCES staff(staff_id) ON DELETE CASCADE,
                              CHECK (check_result IN ('passed', 'failed', 'pending')),
                              UNIQUE KEY uk_activity_chemical_checkdatetime (activity_id, chemical_id, check_datetime),
                              INDEX idx_safety_check_activity (activity_id, check_result),
                              INDEX idx_safety_check_chemical (chemical_id, check_datetime),
                              INDEX idx_safety_check_staff (checked_by_staff_id, check_datetime)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Stores safety check records for chemical usage in activities, supports compliance tracking';

-- ======================== TRIGGERS (replace subquery CHECK constraints) ========================
DELIMITER //

-- 1. Supervision relationship table: Verify supervisor role level is higher than subordinate
CREATE TRIGGER trg_supervise_role_level_check_insert
    BEFORE INSERT ON supervise
    FOR EACH ROW
BEGIN
    DECLARE supervisor_level INT;
    DECLARE subordinate_level INT;

    SELECT r.role_level INTO supervisor_level
    FROM staff s JOIN role r ON s.role_id = r.role_id
    WHERE s.staff_id = NEW.supervisor_staff_id;

    SELECT r.role_level INTO subordinate_level
    FROM staff s JOIN role r ON s.role_id = r.role_id
    WHERE s.staff_id = NEW.subordinate_staff_id;

    IF supervisor_level >= subordinate_level THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Supervisor role level must be higher than subordinate (Administrator → Mid-level Manager → Base-level Employee)';
END IF;
END //

CREATE TRIGGER trg_supervise_role_level_check_update
    BEFORE UPDATE ON supervise
    FOR EACH ROW
BEGIN
    DECLARE supervisor_level INT;
    DECLARE subordinate_level INT;

    SELECT r.role_level INTO supervisor_level
    FROM staff s JOIN role r ON s.role_id = r.role_id
    WHERE s.staff_id = NEW.supervisor_staff_id;

    SELECT r.role_level INTO subordinate_level
    FROM staff s JOIN role r ON s.role_id = r.role_id
    WHERE s.staff_id = NEW.subordinate_staff_id;

    IF supervisor_level >= subordinate_level THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Supervisor role level must be higher than subordinate (Administrator → Mid-level Manager → Base-level Employee)';
END IF;
END //

-- 2. Buildings table: Verify supervisor is a mid-level manager (role_level=2)
CREATE TRIGGER trg_building_supervisor_check_insert
    BEFORE INSERT ON buildings
    FOR EACH ROW
BEGIN
    DECLARE manager_level INT;
    IF NEW.supervisor_staff_id IS NOT NULL THEN
    SELECT r.role_level INTO manager_level
    FROM staff s JOIN role r ON s.role_id = r.role_id
    WHERE s.staff_id = NEW.supervisor_staff_id;

    IF manager_level != 2 THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Building supervisor must be a mid-level manager (role_level=2)';
END IF;
END IF;
END //

CREATE TRIGGER trg_building_supervisor_check_update
    BEFORE UPDATE ON buildings
    FOR EACH ROW
BEGIN
    DECLARE manager_level INT;
    IF NEW.supervisor_staff_id IS NOT NULL THEN
    SELECT r.role_level INTO manager_level
    FROM staff s JOIN role r ON s.role_id = r.role_id
    WHERE s.staff_id = NEW.supervisor_staff_id;

    IF manager_level != 2 THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Building supervisor must be a mid-level manager (role_level=2)';
END IF;
END IF;
END //
-- 3. Robot Usage Table: Verify robot is in enabled status
CREATE TRIGGER trg_robot_usage_active_check_insert
    BEFORE INSERT ON robot_usage
    FOR EACH ROW
BEGIN
    DECLARE robot_status CHAR(1);
    SELECT active_flag INTO robot_status FROM robot WHERE robot_id = NEW.robot_id;
    IF robot_status != 'Y' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Can only use robots in enabled status (active_flag=Y)';
END IF;
END //

-- 4. Chemical Safety Check Table: Verify chemical is in enabled status
CREATE TRIGGER trg_safety_check_chemical_active_insert
    BEFORE INSERT ON safety_check
    FOR EACH ROW
BEGIN
    DECLARE chemical_status CHAR(1);
    SELECT active_flag INTO chemical_status FROM chemical WHERE chemical_id = NEW.chemical_id;
    IF chemical_status != 'Y' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Can only check chemicals in enabled status (active_flag=Y)';
END IF;
END //

-- 5. Activity Table: Verify facility association consistency (room/level matches building)
CREATE TRIGGER trg_activity_facility_consistency_insert
    BEFORE INSERT ON activity
    FOR EACH ROW
BEGIN
    DECLARE room_building_id INT;
    DECLARE level_building_id INT;

    -- Verify room and building consistency
    IF NEW.facility_type = 'room' THEN
    SELECT building_id INTO room_building_id FROM rooms WHERE room_id = NEW.room_id;
    IF room_building_id != NEW.building_id THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Room''s building does not match the activity''s associated building';
END IF;
END IF;

    -- Verify level and building consistency
    IF NEW.facility_type = 'level' THEN
SELECT building_id INTO level_building_id FROM levels WHERE level_id = NEW.level_id;
IF level_building_id != NEW.building_id THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Level''s building does not match the activity''s associated building';
END IF;
END IF;
END //

CREATE TRIGGER trg_activity_facility_consistency_update
    BEFORE UPDATE ON activity
    FOR EACH ROW
BEGIN
    DECLARE room_building_id INT;
    DECLARE level_building_id INT;

    -- Verify room and building consistency
    IF NEW.facility_type = 'room' THEN
    SELECT building_id INTO room_building_id FROM rooms WHERE room_id = NEW.room_id;
    IF room_building_id != NEW.building_id THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Room''s building does not match the activity''s associated building';
END IF;
END IF;

    -- Verify level and building consistency
    IF NEW.facility_type = 'level' THEN
SELECT building_id INTO level_building_id FROM levels WHERE level_id = NEW.level_id;
IF level_building_id != NEW.building_id THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Level''s building does not match the activity''s associated building';
END IF;
END IF;
END //

DELIMITER ;

-- ======================== SCHEDULED EVENTS (automatically maintain data status) ========================
-- Enable event scheduler (if not already enabled)
SET GLOBAL event_scheduler = ON;

-- 1. Daily at 1:00 AM: Disable expired chemical inventory
CREATE EVENT evt_disable_expired_chemical
ON SCHEDULE EVERY 1 DAY STARTS '2025-01-01 01:00:00'
COMMENT 'Automatically set expired chemical inventory to unavailable'
DO
UPDATE chemical_inventory
SET active_flag = 'N'
WHERE active_flag = 'Y' AND expiry_date < CURRENT_DATE();

-- 2. Execute hourly: Update weather emergency event status
CREATE EVENT evt_update_weather_emergency_status
ON SCHEDULE EVERY 1 HOUR
COMMENT 'Automatically set ended weather emergency events to inactive status'
DO
UPDATE weather_emergency
SET active_flag = 'N'
WHERE active_flag = 'Y' AND end_date < NOW();

-- 3. Daily at 2:00 AM: Update robot maintenance status
CREATE EVENT evt_update_robot_maintenance_status
ON SCHEDULE EVERY 1 DAY STARTS '2025-01-01 02:00:00'
COMMENT 'Automatically update robot maintenance overdue status'
DO
UPDATE robot
SET active_flag = 'N'
WHERE active_flag = 'Y' AND last_maintained_date IS NOT NULL
  AND DATE_ADD(last_maintained_date, INTERVAL maintenance_cycle DAY) < CURRENT_DATE();

-- 4. Daily at 3:00 AM: Update supervision relationship status
CREATE EVENT evt_update_supervision_status
ON SCHEDULE EVERY 1 DAY STARTS '2025-01-01 03:00:00'
COMMENT 'Automatically end supervision relationships that have passed end date'
DO
UPDATE supervise
SET end_date = CURRENT_DATE()
WHERE end_date IS NULL AND end_date < CURRENT_DATE();

-- 5. Monthly at 1st day 4:00 AM: Generate maintenance reports
CREATE EVENT evt_generate_maintenance_report
ON SCHEDULE EVERY 1 MONTH STARTS '2025-01-01 04:00:00'
COMMENT 'Automatically generate monthly maintenance reports'
DO
BEGIN
    -- Insert monthly robot maintenance report
INSERT INTO maintenance_reports (report_type, report_period, generated_date, report_data)
SELECT 'robot_maintenance',
       DATE_FORMAT(CURRENT_DATE - INTERVAL 1 MONTH, '%Y-%m'),
       CURRENT_DATE,
       JSON_OBJECT(
               'total_maintenance_count', COUNT(*),
               'total_cost', SUM(COALESCE(cost, 0)),
               'average_cost', AVG(COALESCE(cost, 0)),
               'most_maintained_robot', (
                   SELECT robot_id FROM robot_maintenance
                   WHERE maintenance_date >= CURRENT_DATE - INTERVAL 1 MONTH
                   GROUP BY robot_id ORDER BY COUNT(*) DESC LIMIT 1
           )
           )
FROM robot_maintenance
WHERE maintenance_date >= CURRENT_DATE - INTERVAL 1 MONTH;

-- Insert monthly chemical usage report
INSERT INTO maintenance_reports (report_type, report_period, generated_date, report_data)
SELECT 'chemical_usage',
       DATE_FORMAT(CURRENT_DATE - INTERVAL 1 MONTH, '%Y-%m'),
       CURRENT_DATE,
       JSON_OBJECT(
               'total_chemical_checks', COUNT(*),
               'pass_rate', ROUND(SUM(CASE WHEN check_result = 'passed' THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2),
               'most_checked_chemical', (
                   SELECT chemical_id FROM safety_check
                   WHERE check_datetime >= CURRENT_DATE - INTERVAL 1 MONTH
                   GROUP BY chemical_id ORDER BY COUNT(*) DESC LIMIT 1
           )
           )
FROM safety_check
WHERE check_datetime >= CURRENT_DATE - INTERVAL 1 MONTH;
END;

-- 6. Daily at 5:00 AM: Clean up old activity records
CREATE EVENT evt_cleanup_old_activities
ON SCHEDULE EVERY 1 DAY STARTS '2025-01-01 05:00:00'
COMMENT 'Automatically archive or delete old completed activities'
DO
UPDATE activity
SET active_flag = 'N'
WHERE active_flag = 'Y'
  AND status = 'completed'
  AND activity_datetime < CURRENT_DATE - INTERVAL 365 DAY;

-- 7. Weekly Sunday at 6:00 AM: Update staff assignment statistics
CREATE EVENT evt_update_staff_assignment_stats
ON SCHEDULE EVERY 1 WEEK STARTS '2025-01-05 06:00:00' -- First Sunday of 2025
COMMENT 'Automatically update weekly staff assignment statistics'
DO
BEGIN
    -- Update or insert weekly assignment statistics
INSERT INTO staff_assignment_stats (staff_id, week_start, total_assignments, completed_assignments)
SELECT
    wf.staff_id,
    DATE_SUB(CURRENT_DATE, INTERVAL WEEKDAY(CURRENT_DATE) DAY) as week_start,
    COUNT(*) as total_assignments,
    SUM(CASE WHEN a.status = 'completed' THEN 1 ELSE 0 END) as completed_assignments
FROM works_for wf
         JOIN activity a ON wf.activity_id = a.activity_id
WHERE a.activity_datetime >= DATE_SUB(CURRENT_DATE, INTERVAL 7 DAY)
GROUP BY wf.staff_id
    ON DUPLICATE KEY UPDATE
                         total_assignments = VALUES(total_assignments),
                         completed_assignments = VALUES(completed_assignments);
END;

DELIMITER ;