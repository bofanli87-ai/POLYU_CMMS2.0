package com.polyu.cmms.util.createdata;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class CreateTable {
    public static void main(String[] args) {
        // 1. Database connection parameters (obtained from TiDB Cloud, replace password
        String host = "gateway01.ap-southeast-1.prod.aws.tidbcloud.com";
        int port = 4000;
        String database = "test";
        String username = "3yZKtrYwuR4Coqh.root";
        String password = "p5e9zsWpB92ClYFW"; // Replace with your reset password
        // CA certificate path (relative path in resources directory, no need to write full path)
        String caPath = ConnectDB.class.getClassLoader().getResource("cert/isrgrootx1.pem").getPath();

        // 2. Construct JDBC URL (Windows path automatically compatible, no need to escape)
        String jdbcUrl = String.format(
                "jdbc:mysql://%s:%d/%s?sslMode=VERIFY_IDENTITY&sslCa=%s",
                host, port, database, caPath
        );

        // 3. Connect to database and create tables
        try (
                // Automatically close connection (try-with-resources syntax, no need to manually close)
                Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
                Statement stmt = conn.createStatement()
        ) {
            System.out.println("✅ Database connection successful!");
            try {
                // Create table SQL
                String createRole = "-- 1. 角色表（基础表，无外键依赖）\n" +
                        "CREATE TABLE Role (\n" +
                        "    role_id INT AUTO_INCREMENT COMMENT '角色唯一标识',\n" +
                        "    role_name VARCHAR(50) NOT NULL COMMENT '角色名称（行政官/中层经理/基层员工）',\n" +
                        "    role_level INT NOT NULL COMMENT '角色层级（1=行政官，2=中层经理，3=基层员工）',\n" +
                        "    description VARCHAR(200) COMMENT '角色职责描述',\n" +
                        "    active_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '角色启用状态（Y=启用，N=禁用）',\n" +
                        "    PRIMARY KEY (role_id),\n" +
                        "    UNIQUE KEY uk_role_name (role_name),\n" +
                        "    CHECK (role_name IN ('executive_officer', 'mid_level_manager', 'base_level_worker')),\n" +
                        "    CHECK (role_level IN (1, 2, 3)),\n" +
                        "    CHECK ((role_level=1 AND role_name='executive_officer') \n" +
                        "           OR (role_level=2 AND role_name='mid_level_manager') \n" +
                        "           OR (role_level=3 AND role_name='base_level_worker')),\n" +
                        "    CHECK (active_flag IN ('Y', 'N'))\n" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '存储员工角色信息，通过role_name与role_level强关联确保逻辑一致性';\n";

                stmt.executeUpdate(createRole);
                System.out.println("✅ Table `Role` created successfully!");
            } catch (Exception e) {
                System.err.println("❌ Table `Role` creation failed!");
                // e.printStackTrace();
            }
            try {
                // Create system configuration table
                String createSystemLimits = "-- 2. System configuration table (base table, no foreign key dependencies)\n" +
                        "CREATE TABLE SystemLimits (\n" +
                        "    system_limits_id INT AUTO_INCREMENT COMMENT 'Configuration unique identifier',\n" +
                        "    max_mid_level_managers INT NOT NULL COMMENT 'Maximum number limit for mid-level managers',\n" +
                        "    max_base_level_workers INT NOT NULL COMMENT 'Maximum number limit for base-level workers',\n" +
                        "    effective_date DATE NOT NULL COMMENT 'Configuration effective date',\n" +
                        "    PRIMARY KEY (system_limits_id),\n" +
                        "    CHECK (max_mid_level_managers >= 0),\n" +
                        "    CHECK (max_base_level_workers >= 0)\n" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Store employee number limit configurations, support effective date management';\n";
                stmt.executeUpdate(createSystemLimits);
                System.out.println("✅ Table `SystemLimits` created successfully!");
            } catch (Exception e) {
                System.err.println("❌ Table `SystemLimits` creation failed!");
                e.printStackTrace();
            }
            try {
                // Create address table
                String createAddress = "-- 3. Address table (base table, no foreign key dependencies, unified address information management)\n" +
                        "CREATE TABLE Address (\n" +
                        "    address_id INT AUTO_INCREMENT COMMENT 'Address unique identifier',\n" +
                        "    street VARCHAR(100) COMMENT 'Street address',\n" +
                        "    city VARCHAR(50) COMMENT 'City',\n" +
                        "    postal_code VARCHAR(20) COMMENT 'Postal code',\n" +
                        "    country VARCHAR(50) COMMENT 'Country/region',\n" +
                        "    detail VARCHAR(200) COMMENT 'Detailed address (such as building number, door number)',\n" +
                        "    active_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Address activation status (Y=enabled, N=disabled)',\n" +
                        "    PRIMARY KEY (address_id),\n" +
                        "    CHECK (active_flag IN ('Y', 'N'))\n" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Unified management of all entity address information, eliminate address redundancy across multiple tables';\n";
                stmt.executeUpdate(createAddress);
                System.out.println("✅ Table `Address` created successfully!");
            } catch (Exception e) {
                System.err.println("❌ Table `Address` creation failed!");
                e.printStackTrace();
            }
            try {
                // Create staff table
                String createStaff = "-- 4. Staff table (depends on Role table)\n" +
                        "CREATE TABLE Staff (\n" +
                        "    staff_id INT AUTO_INCREMENT COMMENT 'Staff unique identifier',\n" +
                        "    staff_number VARCHAR(20) NOT NULL COMMENT 'Staff number',\n" +
                        "    first_name VARCHAR(50) NOT NULL COMMENT 'First name',\n" +
                        "    last_name VARCHAR(50) NOT NULL COMMENT 'Last name',\n" +
                        "    date_of_birth DATE COMMENT 'Date of birth (replaces age field)',\n" +
                        "    gender CHAR(1) COMMENT 'Gender (F=female, M=male, O=other)',\n" +
                        "    role_id INT NOT NULL COMMENT 'Role ID (references Role table)',\n" +
                        "    email VARCHAR(100) COMMENT 'Email address',\n" +
                        "    phone VARCHAR(20) COMMENT 'Contact phone number',\n" +
                        "    hire_date DATE NOT NULL COMMENT 'Hire date',\n" +
                        "    emergency_contact VARCHAR(50) COMMENT 'Emergency contact person',\n" +
                        "    emergency_phone VARCHAR(20) COMMENT 'Emergency contact phone',\n" +
                        "    active_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Staff employment status (Y=active, N=inactive)',\n" +
                        "    PRIMARY KEY (staff_id),\n" +
                        "    UNIQUE KEY uk_staff_number (staff_number),\n" +
                        "    UNIQUE KEY uk_staff_email (email),\n" +
                        "    UNIQUE KEY uk_staff_phone (phone),\n" +
                        "    FOREIGN KEY (role_id) REFERENCES Role(role_id) ON DELETE RESTRICT,\n" +
                        "    CHECK (gender IN ('F', 'M', 'O')),\n" +
                        "    CHECK (active_flag IN ('Y', 'N')),\n" +
                        "    INDEX idx_staff_role (role_id),\n" +
                        "    INDEX idx_staff_active_role (active_flag, role_id)\n" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Store basic staff information, associate with role table to implement permission hierarchy management';\n";
                stmt.executeUpdate(createStaff);
                System.out.println("✅ Table `Staff` created successfully!");
            } catch (Exception e) {
                System.err.println("❌ Table `Staff` creation failed!");
                e.printStackTrace();
            }
            try {
                // Create staff skills table
                String createSkill = "-- 5. Staff skills table (base table, no foreign key dependencies)\n" +
                        "CREATE TABLE Skill (\n" +
                        "    skill_id INT AUTO_INCREMENT COMMENT 'Skill unique identifier',\n" +
                        "    skill_name VARCHAR(50) NOT NULL COMMENT 'Skill name (such as robot operation, chemical usage)',\n" +
                        "    description VARCHAR(200) COMMENT 'Skill description (such as cleaning robot operation, high-risk chemical usage)',\n" +
                        "    PRIMARY KEY (skill_id),\n" +
                        "    UNIQUE KEY uk_skill_name (skill_name),\n" +
                        "    CHECK (skill_name IN ('robot_operation', 'chemical_use', 'electrical_repair',\n" +
                        "                          'mechanical_maintenance', 'cleaning_service', 'safety_inspection'))\n" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Standardized staff skill classification, support skill enumeration management';\n";
                stmt.executeUpdate(createSkill);
                System.out.println("✅ Table `Skill` created successfully!");
            } catch (Exception e) {
                System.err.println("❌ Table `Skill` creation failed!");
                e.printStackTrace();
            }
            try {
                // Create staff-skill mapping table
                String createStaffSkillMap = "-- 6. Staff-skill mapping table (depends on Staff, Skill tables)\n" +
                        "CREATE TABLE Staff_Skill_Map (\n" +
                        "    staff_id INT NOT NULL COMMENT 'Staff ID (references Staff table)',\n" +
                        "    skill_id INT NOT NULL COMMENT 'Skill ID (references Skill table)',\n" +
                        "    proficiency VARCHAR(20) NOT NULL COMMENT 'Skill proficiency level (junior/intermediate/senior)',\n" +
                        "    PRIMARY KEY (staff_id, skill_id),\n" +
                        "    FOREIGN KEY (staff_id) REFERENCES Staff(staff_id) ON DELETE CASCADE,\n" +
                        "    FOREIGN KEY (skill_id) REFERENCES Skill(skill_id) ON DELETE CASCADE,\n" +
                        "    CHECK (proficiency IN ('junior', 'intermediate', 'senior')),\n" +
                        "    INDEX idx_skill_staff (skill_id, staff_id)\n" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Implement many-to-many relationship between staff and skills, record skill mastery level';\n";
                stmt.executeUpdate(createStaffSkillMap);
                System.out.println("✅ Table `Staff_Skill_Map` created successfully!");
            } catch (Exception e) {
                System.err.println("❌ Table `Staff_Skill_Map` creation failed!");
                e.printStackTrace();
            }
            try {
                // Create supervision relationship table
                String createSupervise = "-- 7. Supervision relationship table (depends on Staff table, self-referencing)\n" +
                        "CREATE TABLE Supervise (\n" +
                        "    supervise_id INT AUTO_INCREMENT COMMENT 'Supervision relationship unique identifier',\n" +
                        "    supervisor_staff_id INT NOT NULL COMMENT 'Supervisor staff ID (references Staff table)',\n" +
                        "    subordinate_staff_id INT NOT NULL COMMENT 'Subordinate staff ID (references Staff table)',\n" +
                        "    start_date DATE NOT NULL COMMENT 'Supervision relationship start date',\n" +
                        "    end_date DATE COMMENT 'Supervision relationship end date (NULL=currently active)',\n" +
                        "    PRIMARY KEY (supervise_id),\n" +
                        "    FOREIGN KEY (supervisor_staff_id) REFERENCES Staff(staff_id) ON DELETE CASCADE,\n" +
                        "    FOREIGN KEY (subordinate_staff_id) REFERENCES Staff(staff_id) ON DELETE CASCADE,\n" +
                        "    CHECK (supervisor_staff_id != subordinate_staff_id),\n" +
                        "    CHECK (end_date IS NULL OR end_date >= start_date),\n" +
                        "    UNIQUE KEY uk_supervisor_subordinate (supervisor_staff_id, subordinate_staff_id, end_date)\n" +
                        "        COMMENT 'Avoid duplicate active relationships between same supervisor-subordinate',\n" +
                        "    INDEX idx_subordinate_supervisor (subordinate_staff_id, supervisor_staff_id)\n" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Store hierarchical supervision relationships between staff, support relationship lifecycle management';\n";
                stmt.executeUpdate(createSupervise);
                System.out.println("✅ Table `Supervise` created successfully!");
            } catch (Exception e) {
                System.err.println("❌ Table `Supervise` creation failed!");
                e.printStackTrace();
            }
            try {
                // Create external company table
                String createCompany = "-- 8. External company table (depends on Address table)\n" +
                        "CREATE TABLE Company (\n" +
                        "    contractor_id INT AUTO_INCREMENT COMMENT 'Company unique identifier',\n" +
                        "    contractor_code VARCHAR(20) NOT NULL COMMENT 'Company code',\n" +
                        "    name VARCHAR(100) NOT NULL COMMENT 'Company name',\n" +
                        "    contact_name VARCHAR(50) COMMENT 'Contact person name',\n" +
                        "    contract_quote DECIMAL(10,2) COMMENT 'Standard quote (RMB)',\n" +
                        "    email VARCHAR(100) COMMENT 'Company email',\n" +
                        "    phone VARCHAR(20) COMMENT 'Company phone',\n" +
                        "    address_id INT COMMENT 'Address ID (references Address table)',\n" +
                        "    expertise VARCHAR(200) COMMENT 'Expertise field (such as building maintenance, cleaning services)',\n" +
                        "    tax_id VARCHAR(50) COMMENT 'Tax registration number',\n" +
                        "    bank_account VARCHAR(100) COMMENT 'Bank account',\n" +
                        "    active_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Cooperation status (Y=cooperating, N=terminated)',\n" +
                        "    PRIMARY KEY (contractor_id),\n" +
                        "    UNIQUE KEY uk_contractor_code (contractor_code),\n" +
                        "    UNIQUE KEY uk_company_email (email),\n" +
                        "    UNIQUE KEY uk_company_phone (phone),\n" +
                        "    UNIQUE KEY uk_company_taxid (tax_id),\n" +
                        "    FOREIGN KEY (address_id) REFERENCES Address(address_id) ON DELETE SET NULL,\n" +
                        "    CHECK (contract_quote >= 0),\n" +
                        "    CHECK (active_flag IN ('Y', 'N')),\n" +
                        "    INDEX idx_company_address (address_id)\n" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Store outsourcing partner company information, associate with address table to eliminate address redundancy';\n";
                stmt.executeUpdate(createCompany);
                System.out.println("✅ Table `Company` created successfully!");
            } catch (Exception e) {
                System.err.println("❌ Table `Company` creation failed!");
                e.printStackTrace();
            }
            try {
                // Create robot table
                String createRobot = "-- 9. Robot table (no foreign key dependencies)\n" +
                        "CREATE TABLE Robot (\n" +
                        "    robot_id INT AUTO_INCREMENT COMMENT 'Robot unique identifier',\n" +
                        "    type VARCHAR(50) NOT NULL COMMENT 'Robot type (cleaning robot/repair robot/inspection robot)',\n" +
                        "    robot_capability VARCHAR(200) COMMENT 'Function description (such as floor cleaning, pipeline inspection)',\n" +
                        "    create_date DATE NOT NULL COMMENT 'Manufacturing date',\n" +
                        "    last_maintained_date DATE COMMENT 'Last maintenance date',\n" +
                        "    maintenance_cycle INT NOT NULL COMMENT 'Maintenance cycle (days)',\n" +
                        "    active_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Activation status (Y=enabled, N=disabled)',\n" +
                        "    PRIMARY KEY (robot_id),\n" +
                        "    CHECK (type IN ('cleaning_robot', 'repair_robot', 'inspection_robot')),\n" +
                        "    CHECK (maintenance_cycle >= 1),\n" +
                        "    CHECK (active_flag IN ('Y', 'N')),\n" +
                        "    INDEX idx_robot_active_type (active_flag, type)\n" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Store basic robot information, supplement maintenance cycle to support regular maintenance reminders';\n";
                stmt.executeUpdate(createRobot);
                System.out.println("✅ Table `Robot` created successfully!");
            } catch (Exception e) {
                System.err.println("❌ Table `Robot` creation failed!");
                e.printStackTrace();
            }
            try {
                // Create robot maintenance record table (depends on Robot, Staff tables)
                String createRobotMaintenance = "-- 10. Robot maintenance record table (depends on Robot, Staff tables)\n" +
                        "CREATE TABLE Robot_Maintenance (\n" +
                        "    maintenance_id INT AUTO_INCREMENT COMMENT 'Maintenance record unique identifier',\n" +
                        "    robot_id INT NOT NULL COMMENT 'Robot ID (references Robot table)',\n" +
                        "    maintenance_date DATE NOT NULL COMMENT 'Maintenance date',\n" +
                        "    maintenance_type VARCHAR(20) NOT NULL COMMENT 'Maintenance type (routine maintenance/fault repair/overhaul)',\n" +
                        "    content TEXT COMMENT 'Maintenance content (such as part replacement, system upgrade)',\n" +
                        "    maintained_by_staff_id INT COMMENT 'Maintenance staff ID (references Staff table)',\n" +
                        "    cost DECIMAL(10,2) COMMENT 'Maintenance cost (RMB)',\n" +
                        "    notes TEXT COMMENT 'Maintenance notes (such as fault cause, usage suggestions)',\n" +
                        "    PRIMARY KEY (maintenance_id),\n" +
                        "    FOREIGN KEY (robot_id) REFERENCES Robot(robot_id) ON DELETE CASCADE,\n" +
                        "    FOREIGN KEY (maintained_by_staff_id) REFERENCES Staff(staff_id) ON DELETE SET NULL,\n" +
                        "    CHECK (maintenance_type IN ('routine', 'fault', 'overhaul')),\n" +
                        "    CHECK (cost IS NULL OR cost >= 0),\n" +
                        "    INDEX idx_robot_maintenance (robot_id, maintenance_date),\n" +
                        "    INDEX idx_maintenance_staff (maintained_by_staff_id)\n" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Record robot full lifecycle maintenance history, support maintenance cost statistics and fault tracing';\n";
                stmt.executeUpdate(createRobotMaintenance);
                System.out.println("✅ Table `Robot_Maintenance` created successfully!");
            } catch (Exception e) {
                System.err.println("❌ Table `Robot_Maintenance` creation failed!");
                e.printStackTrace();
            }
            try {
                // Create external area table (depends on Address table)
                String createArea = "-- 11. External area table (depends on Address table)\n" +
                        "CREATE TABLE Area (\n" +
                        "    area_id INT AUTO_INCREMENT COMMENT 'Area unique identifier',\n" +
                        "    area_type VARCHAR(50) NOT NULL COMMENT 'Area type (campus external area/temporary work area/other)',\n" +
                        "    description VARCHAR(200) COMMENT 'Area description (such as campus east gate outer square)',\n" +
                        "    address_id INT COMMENT 'Address ID (references Address table)',\n" +
                        "    active_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Activation status (Y=enabled, N=disabled)',\n" +
                        "    PRIMARY KEY (area_id),\n" +
                        "    CHECK (area_type IN ('campus_external', 'temporary_work_area', 'other')),\n" +
                        "    CHECK (active_flag IN ('Y', 'N')),\n" +
                        "    FOREIGN KEY (address_id) REFERENCES Address(address_id) ON DELETE SET NULL,\n" +
                        "    INDEX idx_area_address (address_id)\n" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Store campus external or temporary area information, associate with address table for unified address management';\n";
                stmt.executeUpdate(createArea);
                System.out.println("✅ Table `Area` created successfully!");
            } catch (Exception e) {
                System.err.println("❌ Table `Area` creation failed!");
                e.printStackTrace();
            }
            try {
                // Create buildings table (depends on Address, Staff tables)
                String createBuildings = "-- 12. Buildings table (depends on Address, Staff tables)\n" +
                        "CREATE TABLE Buildings (\n" +
                        "    building_id INT AUTO_INCREMENT COMMENT 'Building unique identifier',\n" +
                        "    building_code VARCHAR(20) NOT NULL COMMENT 'Building code (such as B1, Administration Building)',\n" +
                        "    construction_date DATE COMMENT 'Construction date (replaces usage period field)',\n" +
                        "    address_id INT COMMENT 'Address ID (references Address table)',\n" +
                        "    num_floors INT COMMENT 'Number of floors (including underground floors, e.g., -1=1 underground floor)',\n" +
                        "    supervisor_staff_id INT COMMENT 'Responsible manager ID (references Staff table, mid-level manager)',\n" +
                        "    active_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Usage status (Y=in use, N=out of use)',\n" +
                        "    PRIMARY KEY (building_id),\n" +
                        "    UNIQUE KEY uk_building_code (building_code),\n" +
                        "    FOREIGN KEY (address_id) REFERENCES Address(address_id) ON DELETE SET NULL,\n" +
                        "    FOREIGN KEY (supervisor_staff_id) REFERENCES Staff(staff_id) ON DELETE SET NULL,\n" +
                        "    CHECK (num_floors BETWEEN -5 AND 50),\n" +
                        "    CHECK (active_flag IN ('Y', 'N')),\n" +
                        "    -- Ensure supervisor is mid-level manager\n" +
                        "    CHECK (supervisor_staff_id IS NULL \n" +
                        "    OR (SELECT r.role_level FROM Staff s JOIN Role r ON s.role_id = r.role_id \n" +
                        "    WHERE s.staff_id = supervisor_staff_id) = 2),\n" +
                        "    INDEX idx_building_address (address_id),\n" +
                        "    INDEX idx_building_supervisor (supervisor_staff_id)\n" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Store campus building information, associate with address table to eliminate address redundancy';\n";
                stmt.executeUpdate(createBuildings);
                System.out.println("✅ Table `Buildings` created successfully!");
            } catch (Exception e) {
                System.err.println("❌ Table `Buildings` creation failed!");
                e.printStackTrace();
            }
            try {
                // Create levels table (depends on Buildings table)
                String createLevels = "-- 13. Levels table (depends on Buildings table)\n" +
                        "CREATE TABLE Levels (\n" +
                        "    building_id INT NOT NULL COMMENT 'Building ID (references Buildings table)',\n" +
                        "    level_id INT AUTO_INCREMENT COMMENT 'Level unique identifier',\n" +
                        "    level_number INT NOT NULL COMMENT 'Level number (e.g., 3=3rd floor, -1=underground 1st floor)',\n" +
                        "    active_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Usage status (Y=in use, N=out of use)',\n" +
                        "    PRIMARY KEY (building_id, level_id),\n" +
                        "    UNIQUE KEY uk_building_level (building_id, level_number),\n" +
                        "    FOREIGN KEY (building_id) REFERENCES Buildings(building_id) ON DELETE CASCADE,\n" +
                        "    CHECK (active_flag IN ('Y', 'N')),\n" +
                        "    INDEX idx_level_number (building_id, level_number)\n" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Store building level information, composite primary key ensures level uniqueness within same building';\n";
                stmt.executeUpdate(createLevels);
                System.out.println("✅ Table `Levels` created successfully!");
            } catch (Exception e) {
                System.err.println("❌ Table `Levels` creation failed!");
                e.printStackTrace();
            }
            try {
                // Create rooms table (depends on Buildings table)
                String createRooms = "-- 14. Rooms table (depends on Buildings table)\n" +
                        "CREATE TABLE Rooms (\n" +
                        "    building_id INT NOT NULL COMMENT 'Building ID (references Buildings table)',\n" +
                        "    room_id INT AUTO_INCREMENT COMMENT 'Room unique identifier',\n" +
                        "    name VARCHAR(50) NOT NULL COMMENT 'Room name (such as Room 302, Equipment Room)',\n" +
                        "    room_type VARCHAR(50) COMMENT 'Room type (such as classroom, office, laboratory)',\n" +
                        "    capacity INT COMMENT 'Capacity (number of people)',\n" +
                        "    room_features VARCHAR(200) COMMENT 'Room features (such as air conditioning, projector, ventilation system)',\n" +
                        "    active_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Usage status (Y=in use, N=out of use)',\n" +
                        "    PRIMARY KEY (building_id, room_id),\n" +
                        "    UNIQUE KEY uk_building_room (building_id, name),\n" +
                        "    FOREIGN KEY (building_id) REFERENCES Buildings(building_id) ON DELETE CASCADE,\n" +
                        "    CHECK (capacity IS NULL OR capacity >= 0),\n" +
                        "    CHECK (active_flag IN ('Y', 'N')),\n" +
                        "    INDEX idx_room_type (building_id, room_type)\n" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Store room information within buildings, room name unique within same building';\n";
                stmt.executeUpdate(createRooms);
                System.out.println("✅ Table `Rooms` created successfully!");
            } catch (Exception e) {
                System.err.println("❌ Table `Rooms` creation failed!");
                e.printStackTrace();
            }
            try {
                // Create Squares table (depends on Address table)
                String createSquares = "-- 15. Squares table (depends on Address table)\n" +
                        "CREATE TABLE Squares (\n" +
                        "    square_id INT AUTO_INCREMENT COMMENT 'Square unique identifier',\n" +
                        "    name VARCHAR(50) NOT NULL COMMENT 'Square name (e.g., Central Square, West Square)',\n" +
                        "    address_id INT COMMENT 'Address ID (references Address table)',\n" +
                        "    capacity INT COMMENT 'Maximum capacity',\n" +
                        "    square_features VARCHAR(200) COMMENT 'Square features (e.g., security zone, service facilities)',\n" +
                        "    active_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Usage status (Y=active, N=inactive)',\n" +
                        "    PRIMARY KEY (square_id),\n" +
                        "    UNIQUE KEY uk_square_name (name),\n" +
                        "    FOREIGN KEY (address_id) REFERENCES Address(address_id) ON DELETE SET NULL,\n" +
                        "    CHECK (capacity IS NULL OR capacity >= 0),\n" +
                        "    CHECK (active_flag IN ('Y', 'N')),\n" +
                        "    INDEX idx_square_address (address_id)\n" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Stores campus square information, linked to address table for unified address management';\n";
                stmt.executeUpdate(createSquares);
                System.out.println("✅ Table `Squares` created successfully!");
            } catch (Exception e) {
                System.err.println("❌ Table `Squares` creation failed!");
                e.printStackTrace();
            }
            try {
                // Create Gates table (depends on Address table)
                String createGates = "-- 16. Gates table (depends on Address table)\n" +
                        "CREATE TABLE Gates (\n" +
                        "    gate_id INT AUTO_INCREMENT COMMENT 'Gate unique identifier',\n" +
                        "    name VARCHAR(50) NOT NULL COMMENT 'Gate name (e.g., East Gate, South Gate, Emergency Passage)',\n" +
                        "    address_id INT COMMENT 'Address ID (references Address table)',\n" +
                        "    flow_capacity INT COMMENT 'Flow capacity (people/hour)',\n" +
                        "    gate_features VARCHAR(200) COMMENT 'Gate features (e.g., security zone, service facilities)',\n" +
                        "    active_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Usage status (Y=enabled, N=disabled)',\n" +
                        "    PRIMARY KEY (gate_id),\n" +
                        "    UNIQUE KEY uk_gate_name (name),\n" +
                        "    FOREIGN KEY (address_id) REFERENCES Address(address_id) ON DELETE SET NULL,\n" +
                        "    CHECK (flow_capacity IS NULL OR flow_capacity >= 0),\n" +
                        "    CHECK (active_flag IN ('Y', 'N')),\n" +
                        "    INDEX idx_gate_address (address_id)\n" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Stores campus gate information, linked to address table to eliminate address redundancy';\n";
                stmt.executeUpdate(createGates);
                System.out.println("✅ Table `Gates` created successfully!");
            } catch (Exception e) {
                System.err.println("❌ Table `Gates` creation failed!");
                e.printStackTrace();
            }
            try {
                // Create Canteen table (depends on Address table)
                String createCanteens = "-- 17. Canteen table (depends on Address table)\n" +
                        "CREATE TABLE Canteen (\n" +
                        "    canteen_id INT AUTO_INCREMENT COMMENT 'Canteen unique identifier',\n" +
                        "    name VARCHAR(50) NOT NULL COMMENT 'Canteen name (e.g., First Canteen, Halal Canteen)',\n" +
                        "    construction_date DATE COMMENT 'Construction date (replaces usage period field)',\n" +
                        "    address_id INT COMMENT 'Address ID (references Address table)',\n" +
                        "    capacity INT COMMENT 'Maximum capacity',\n" +
                        "    food_type VARCHAR(50) NOT NULL COMMENT 'Food type (Chinese, Western, Mixed)',\n" +
                        "    active_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Business status (Y=open, N=closed)',\n" +
                        "    PRIMARY KEY (canteen_id),\n" +
                        "    UNIQUE KEY uk_canteen_name (name),\n" +
                        "    FOREIGN KEY (address_id) REFERENCES Address(address_id) ON DELETE SET NULL,\n" +
                        "    CHECK (capacity IS NULL OR capacity >= 0),\n" +
                        "    CHECK (food_type IN ('Chinese', 'Western', 'Mixed')),\n" +
                        "    CHECK (active_flag IN ('Y', 'N')),\n" +
                        "    INDEX idx_canteen_address (address_id),\n" +
                        "    INDEX idx_canteen_foodtype (food_type)\n" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Stores campus canteen information, linked to address table for unified address management';\n";
                stmt.executeUpdate(createCanteens);
                System.out.println("✅ Table `Canteen` created successfully!");
            } catch (Exception e) {
                System.err.println("❌ Table `Canteen` creation failed!");
                e.printStackTrace();
            }
            try {
                // Create Weather Emergency table (no foreign key dependencies)
                String createWeatherEmergency = "-- 18. Weather Emergency table (no foreign key dependencies)\n" +
                        "CREATE TABLE Weather_Emergency (\n" +
                        "    weather_id INT AUTO_INCREMENT COMMENT 'Weather event unique identifier',\n" +
                        "    name VARCHAR(100) NOT NULL COMMENT 'Event name (e.g., October 15, 2023 rainfall)',\n" +
                        "    type VARCHAR(50) NOT NULL COMMENT 'Event type (e.g., rainfall, strong wind, low temperature)',\n" +
                        "    start_date DATETIME NOT NULL COMMENT 'Start time',\n" +
                        "    end_date DATETIME NOT NULL COMMENT 'End time',\n" +
                        "    active_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Event status (Y=active, N=ended)',\n" +
                        "    PRIMARY KEY (weather_id),\n" +
                        "    UNIQUE KEY uk_weather_name (name),\n" +
                        "    CHECK (type IN ('rainfall', 'strong_wind', 'low_temperature', 'typhoon', 'blizzard', 'other')),\n" +
                        "    CHECK (start_date <= end_date),\n" +
                        "    CHECK (active_flag IN ('Y', 'N')),\n" +
                        "    INDEX idx_weather_active (active_flag, start_date),\n" +
                        "    INDEX idx_weather_type (type)\n" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Stores weather emergency event information, supports precise time recording';\n";
                stmt.executeUpdate(createWeatherEmergency);
                System.out.println("✅ Table `Weather_Emergency` created successfully!");
            } catch (Exception e) {
                System.err.println("❌ Table `Weather_Emergency` creation failed!");
                e.printStackTrace();
            }
            try {
                // Create Chemical table (no foreign key dependencies)
                String createChemical = "-- 19. Chemical table (no foreign key dependencies)\n" +
                        "CREATE TABLE Chemical (\n" +
                        "    chemical_id INT AUTO_INCREMENT COMMENT 'Chemical unique identifier',\n" +
                        "    product_code VARCHAR(20) NOT NULL COMMENT 'Product code',\n" +
                        "    name VARCHAR(100) NOT NULL COMMENT 'Chemical name (e.g., disinfectant, cleaning agent)',\n" +
                        "    type VARCHAR(50) NOT NULL COMMENT 'Chemical type (e.g., disinfectant, solvent, lubricant)',\n" +
                        "    manufacturer VARCHAR(100) COMMENT 'Manufacturer',\n" +
                        "    msds_url VARCHAR(255) COMMENT 'Safety data sheet URL',\n" +
                        "    hazard_category VARCHAR(20) NOT NULL COMMENT 'Hazard category (low, medium, high)',\n" +
                        "    storage_requirements TEXT COMMENT 'Storage requirements (e.g., cool and dry, sealed storage)',\n" +
                        "    active_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Enabled status (Y=enabled, N=disabled)',\n" +
                        "    PRIMARY KEY (chemical_id),\n" +
                        "    UNIQUE KEY uk_product_code (product_code),\n" +
                        "    CHECK (type IN ('disinfectant', 'solvent', 'lubricant', 'detergent', 'other')),\n" +
                        "    CHECK (hazard_category IN ('low', 'medium', 'high')),\n" +
                        "    CHECK (active_flag IN ('Y', 'N')),\n" +
                        "    INDEX idx_chemical_hazard (hazard_category, active_flag),\n" +
                        "    INDEX idx_chemical_type (type)\n" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Stores basic chemical information used in activities, supports safety management';\n";
                stmt.executeUpdate(createChemical);
                System.out.println("✅ Table `Chemical` created successfully!");
            } catch (Exception e) {
                System.err.println("❌ Table `Chemical` creation failed!");
                e.printStackTrace();
            }
            try {
                // Create Chemical Inventory table (depends on Chemical, Company tables)
                String createChemicalInventory = "-- 20. Chemical Inventory table (depends on Chemical, Company tables)\n" +
                        "CREATE TABLE Chemical_Inventory (\n" +
                        "    inventory_id INT AUTO_INCREMENT COMMENT 'Inventory record unique identifier',\n" +
                        "    chemical_id INT NOT NULL COMMENT 'Chemical ID (references Chemical table)',\n" +
                        "    quantity DECIMAL(10,2) NOT NULL COMMENT 'Inventory quantity (unit: bottle/liter/kilogram)',\n" +
                        "    storage_location VARCHAR(100) NOT NULL COMMENT 'Storage location (e.g., Chemical Warehouse Area A)',\n" +
                        "    purchase_date DATE NOT NULL COMMENT 'Purchase date',\n" +
                        "    supplier_id INT COMMENT 'Supplier ID (references Company table)',\n" +
                        "    expiry_date DATE NOT NULL COMMENT 'Expiry date',\n" +
                        "    batch_number VARCHAR(50) COMMENT 'Purchase batch number',\n" +
                        "    active_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Inventory status (Y=available, N=unavailable)',\n" +
                        "    PRIMARY KEY (inventory_id),\n" +
                        "    FOREIGN KEY (chemical_id) REFERENCES Chemical(chemical_id) ON DELETE CASCADE,\n" +
                        "    FOREIGN KEY (supplier_id) REFERENCES Company(contractor_id) ON DELETE SET NULL,\n" +
                        "    CHECK (quantity >= 0),\n" +
                        "    CHECK (expiry_date >= purchase_date),\n" +
                        "    CHECK (active_flag IN ('Y', 'N')),\n" +
                        "    -- Automatically set to unavailable when expired (can be implemented via trigger, constraint here)\n" +
                        "    CHECK (active_flag = 'N' OR expiry_date >= CURRENT_DATE()),\n" +
                        "    INDEX idx_inventory_chemical (chemical_id, active_flag),\n" +
                        "    INDEX idx_inventory_expiry (expiry_date, active_flag),\n" +
                        "    INDEX idx_inventory_supplier (supplier_id)\n" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Tracks chemical inventory dynamics, supports inventory alerts and usage management';\n";
                stmt.executeUpdate(createChemicalInventory);
                System.out.println("✅ Table `Chemical_Inventory` created successfully!");

            } catch (Exception e) {
                System.err.println("❌ Table `Chemical_Inventory` creation failed!");
                e.printStackTrace();
            }
            try {
                // Create Activity table (depends on Staff, Weather_Emergency, Area, Buildings, etc.)
                String createActivity = "-- 21. Activity table (core business table, fixes composite foreign keys, status constraints, index optimization)\r\n" + //
                        "CREATE TABLE Activity (\r\n" + //
                        "    activity_id INT AUTO_INCREMENT COMMENT 'Activity unique identifier',\r\n" + //
                        "    activity_type VARCHAR(50) NOT NULL COMMENT 'Activity type (cleaning, repair, weather emergency response)',\r\n" + //
                        "    title VARCHAR(150) NOT NULL COMMENT 'Activity title (extended length for complex scenarios)',\r\n" + //
                        "    description TEXT COMMENT 'Activity details (e.g., cleaning scope, repair content)',\r\n" + //
                        "    status VARCHAR(20) NOT NULL DEFAULT 'planned' COMMENT 'Activity status (planned, in_progress, completed, cancelled)',\r\n" + //
                        "    priority VARCHAR(20) NOT NULL DEFAULT 'medium' COMMENT 'Activity priority (low, medium, high)',\r\n" + //
                        "    activity_datetime DATETIME NOT NULL COMMENT 'Activity execution time',\r\n" + //
                        "    expected_unavailable_duration DECIMAL(5,2) NOT NULL COMMENT 'Expected unavailable duration (hours)',\r\n" + //
                        "    actual_completion_datetime DATETIME COMMENT 'Actual completion time (NULL=not completed)',\r\n" + //
                        "    created_by_staff_id INT NOT NULL COMMENT 'Creator ID (references Staff table)',\r\n" + //
                        "    weather_id INT COMMENT 'Associated weather event ID (references Weather_Emergency table)',\r\n" + //
                        "    area_id INT COMMENT 'Associated external area ID (references Area table)',\r\n" + //
                        "    hazard_level VARCHAR(20) NOT NULL COMMENT 'Risk level (low, medium, high)',\r\n" + //
                        "    facility_type VARCHAR(20) NOT NULL COMMENT 'Associated facility type (building/room/level/square/gate/canteen/area/none)',\r\n" + //
                        "    building_id INT COMMENT 'Associated building ID (references Buildings table, part of composite foreign key)',\r\n" + //
                        "    room_id INT COMMENT 'Associated room ID (references Rooms table, part of composite foreign key)',\r\n" + //
                        "    level_id INT COMMENT 'Associated level ID (references Levels table, part of composite foreign key)',\r\n" + //
                        "    square_id INT COMMENT 'Associated square ID (references Squares table)',\r\n" + //
                        "    gate_id INT COMMENT 'Associated gate ID (references Gates table)',\r\n" + //
                        "    canteen_id INT COMMENT 'Associated canteen ID (references Canteen table)',\r\n" + //
                        "    active_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Activity status (Y=active, N=inactive)',\r\n" + //
                        "    PRIMARY KEY (activity_id),\r\n" + //
                        "    -- Foreign key constraints: Fix composite foreign keys, ensure consistency with composite primary key tables\r\n" + //
                        "    FOREIGN KEY (created_by_staff_id) REFERENCES Staff(staff_id) ON DELETE RESTRICT,\r\n" + //
                        "    FOREIGN KEY (weather_id) REFERENCES Weather_Emergency(weather_id) ON DELETE SET NULL,\r\n" + //
                        "    FOREIGN KEY (area_id) REFERENCES Area(area_id) ON DELETE SET NULL,\r\n" + //
                        "    FOREIGN KEY (building_id) REFERENCES Buildings(building_id) ON DELETE SET NULL,\r\n" + //
                        "    FOREIGN KEY (square_id) REFERENCES Squares(square_id) ON DELETE SET NULL,\r\n" + //
                        "    FOREIGN KEY (gate_id) REFERENCES Gates(gate_id) ON DELETE SET NULL,\r\n" + //
                        "    FOREIGN KEY (canteen_id) REFERENCES Canteen(canteen_id) ON DELETE SET NULL,\r\n" + //
                        "    -- Composite foreign key: Reference Rooms table (building_id + room_id corresponds to target table composite primary key)\r\n" + //
                        "    FOREIGN KEY (building_id, room_id) REFERENCES Rooms(building_id, room_id) ON DELETE SET NULL,\r\n" + //
                        "    -- Composite foreign key: Reference Levels table (building_id + level_id corresponds to target table composite primary key)\r\n" + //
                        "    FOREIGN KEY (building_id, level_id) REFERENCES Levels(building_id, level_id) ON DELETE SET NULL,\r\n" + //
                        "    -- Core business constraints: Ensure data logical consistency\r\n" + //
                        "    CHECK (activity_type IN ('cleaning', 'repair', 'weather_response')),\r\n" + //
                        "    CHECK (status IN ('planned', 'in_progress', 'completed', 'cancelled')),\r\n" + //
                        "    CHECK (priority IN ('low', 'medium', 'high')),\r\n" + //
                        "    CHECK (hazard_level IN ('low', 'medium', 'high')),\r\n" + //
                        "    CHECK (facility_type IN ('building', 'room', 'level', 'square', 'gate', 'canteen', 'area', 'none')),\r\n" + //
                        "    CHECK (expected_unavailable_duration >= 0),\r\n" + //
                        "    -- Status and actual completion time linkage constraints: Avoid logical contradictions\r\n" + //
                        "    CHECK (\r\n" + //
                        "        (status = 'completed' AND actual_completion_datetime IS NOT NULL AND actual_completion_datetime >= activity_datetime)\r\n" + //
                        "        OR (status IN ('planned', 'in_progress', 'cancelled') AND actual_completion_datetime IS NULL)\r\n" + //
                        "    ),\r\n" + //
                        "    -- Facility type and associated fields strict matching: Non-associated fields must be NULL, no redundancy\r\n" + //
                        "    CHECK (\r\n" + //
                        "        (facility_type = 'building' AND building_id IS NOT NULL \r\n" + //
                        "         AND room_id IS NULL AND level_id IS NULL AND square_id IS NULL AND gate_id IS NULL AND canteen_id IS NULL AND area_id IS NULL)\r\n" + //
                        "        OR (facility_type = 'room' AND building_id IS NOT NULL AND room_id IS NOT NULL \r\n" + //
                        "         AND level_id IS NULL AND square_id IS NULL AND gate_id IS NULL AND canteen_id IS NULL AND area_id IS NULL)\r\n" + //
                        "        OR (facility_type = 'level' AND building_id IS NOT NULL AND level_id IS NOT NULL \r\n" + //
                        "         AND room_id IS NULL AND square_id IS NULL AND gate_id IS NULL AND canteen_id IS NULL AND area_id IS NULL)\r\n" + //
                        "        OR (facility_type = 'square' AND square_id IS NOT NULL \r\n" + //
                        "         AND building_id IS NULL AND room_id IS NULL AND level_id IS NULL AND gate_id IS NULL AND canteen_id IS NULL AND area_id IS NULL)\r\n" + //
                        "        OR (facility_type = 'gate' AND gate_id IS NOT NULL \r\n" + //
                        "         AND building_id IS NULL AND room_id IS NULL AND level_id IS NULL AND square_id IS NULL AND canteen_id IS NULL AND area_id IS NULL)\r\n" + //
                        "        OR (facility_type = 'canteen' AND canteen_id IS NOT NULL \r\n" + //
                        "         AND building_id IS NULL AND room_id IS NULL AND level_id IS NULL AND square_id IS NULL AND gate_id IS NULL AND area_id IS NULL)\r\n" + //
                        "        OR (facility_type = 'area' AND area_id IS NOT NULL \r\n" + //
                        "         AND building_id IS NULL AND room_id IS NULL AND level_id IS NULL AND square_id IS NULL AND gate_id IS NULL AND canteen_id IS NULL)\r\n" + //
                        "        OR (facility_type = 'none' AND building_id IS NULL AND room_id IS NULL AND level_id IS NULL \r\n" + //
                        "         AND square_id IS NULL AND gate_id IS NULL AND canteen_id IS NULL AND area_id IS NULL)\r\n" + //
                        "    ),\r\n" + //
                        "    CHECK (active_flag IN ('Y', 'N')),\r\n" + //
                        "    -- Optimized indexes: Cover high-frequency query scenarios, reduce back-to-table queries\r\n" + //
                        "    INDEX idx_activity_datetime_status_type (activity_datetime, status, activity_type) COMMENT 'Support high-frequency queries by time period + status + type',\r\n" + //
                        "    INDEX idx_activity_facility (facility_type, building_id, room_id, level_id) COMMENT 'Support precise queries by facility type + associated facility',\r\n" + //
                        "    INDEX idx_activity_weather_status (weather_id, status) COMMENT 'Support emergency activity queries by weather event + status',\r\n" + //
                        "    INDEX idx_activity_creator_status (created_by_staff_id, status) COMMENT 'Support activity management queries by creator + status',\r\n" + //
                        "    INDEX idx_activity_hazard_status (hazard_level, status) COMMENT 'Support security audit queries by risk level + status'\r\n" + //
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Core campus maintenance activity table, fixes composite foreign key relationships, status linkage constraints, adapts to multi-scenario business requirements; high-risk activities require trigger association with Safety_Check table';";
                stmt.executeUpdate(createActivity);
                System.out.println("✅ Table `Activity` created successfully!");
            } catch (Exception e) {
                System.err.println("❌ Table `Activity` creation failed!");
                e.printStackTrace();
            }
            try {
                // Create Staff-Activity association table (depends on Staff, Activity tables)
                String createWorksFor = "-- 22. Staff-Activity association table (depends on Staff, Activity tables)\n" +
                        "CREATE TABLE WORKS_FOR (\n" +
                        "    works_for_id INT AUTO_INCREMENT COMMENT 'Association unique identifier',\n" +
                        "    staff_id INT NOT NULL COMMENT 'Staff ID (references Staff table)',\n" +
                        "    activity_id INT NOT NULL COMMENT 'Activity ID (references Activity table)',\n" +
                        "    activity_responsibility VARCHAR(200) NOT NULL COMMENT 'Staff responsibility in activity (e.g., operate robot, on-site supervision)',\n" +
                        "    assigned_datetime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Assignment time',\n" +
                        "    active_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Association status (Y=active, N=inactive)',\n" +
                        "    PRIMARY KEY (works_for_id),\n" +
                        "    FOREIGN KEY (staff_id) REFERENCES Staff(staff_id) ON DELETE CASCADE,\n" +
                        "    FOREIGN KEY (activity_id) REFERENCES Activity(activity_id) ON DELETE CASCADE,\n" +
                        "    UNIQUE KEY uk_staff_activity (staff_id, activity_id),\n" +
                        "    CHECK (active_flag IN ('Y', 'N')),\n" +
                        "    INDEX idx_works_for_staff_activity (staff_id, activity_id, active_flag),\n" +
                        "    INDEX idx_works_for_activity (activity_id, active_flag)\n" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Implements many-to-many relationship between staff and activities, records assignment time and responsibilities';\n";
                stmt.executeUpdate(createWorksFor);
                System.out.println("✅ Table `WORKS_FOR` created successfully!");
            } catch (Exception e) {
                System.err.println("❌ Table `WORKS_FOR` creation failed!");
                e.printStackTrace();
            }
            try {
                // Create outsourcing contract table (depends on Company, Activity tables)
                String createContract = "-- 23. Outsourcing contract table (depends on Company, Activity tables)\n" +
                        "CREATE TABLE Contract (\n" +
                        "    contract_id INT AUTO_INCREMENT COMMENT 'Contract unique identifier',\n" +
                        "    contractor_id INT NOT NULL COMMENT 'Contractor company ID (references Company table)',\n" +
                        "    activity_id INT NOT NULL COMMENT 'Associated activity ID (references Activity table)',\n" +
                        "    contract_date DATE NOT NULL COMMENT 'Contract signing date',\n" +
                        "    contract_amount DECIMAL(12,2) NOT NULL COMMENT 'Contract amount (RMB)',\n" +
                        "    end_date DATE COMMENT 'Contract end date (NULL=not ended)',\n" +
                        "    status VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT 'Contract status (active, completed, cancelled)',\n" +
                        "    payment_terms VARCHAR(200) COMMENT 'Payment terms (e.g., payment within 30 days after acceptance)',\n" +
                        "    notes TEXT COMMENT 'Contract notes (e.g., acceptance criteria, liability for breach)',\n" +
                        "    PRIMARY KEY (contract_id),\n" +
                        "    FOREIGN KEY (contractor_id) REFERENCES Company(contractor_id) ON DELETE CASCADE,\n" +
                        "    FOREIGN KEY (activity_id) REFERENCES Activity(activity_id) ON DELETE CASCADE,\n" +
                        "    UNIQUE KEY uk_contract_activity (activity_id),\n" +
                        "    CHECK (contract_amount >= 0),\n" +
                        "    CHECK (status IN ('active', 'completed', 'cancelled')),\n" +
                        "    CHECK (end_date IS NULL OR contract_date <= end_date),\n" +
                        "    INDEX idx_contract_company (contractor_id, status),\n" +
                        "    INDEX idx_contract_date (contract_date),\n" +
                        "    INDEX idx_contract_activity_status (activity_id, status)\n" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Stores activity outsourcing contract information, ensures one activity corresponds to only one outsourcing contract';\n";
                stmt.executeUpdate(createContract);
                System.out.println("✅ Table `Contract` created successfully!");
            } catch (Exception e) {
                System.err.println("❌ Table `Contract` creation failed!");
                e.printStackTrace();
            }
            try {
                // Create robot usage table (depends on Robot, Activity, Staff tables)
                String createRobotUsage = "-- 24. Robot usage table (depends on Robot, Activity, Staff tables)\n" +
                        "CREATE TABLE Robot_Usage (\n" +
                        "    robot_usage_id INT AUTO_INCREMENT COMMENT 'Usage record unique identifier',\n" +
                        "    robot_id INT NOT NULL COMMENT 'Robot ID (references Robot table)',\n" +
                        "    activity_id INT NOT NULL COMMENT 'Activity ID (references Activity table)',\n" +
                        "    usage_datetime DATETIME NOT NULL COMMENT 'Usage time',\n" +
                        "    operator_staff_id INT COMMENT 'Operator staff ID (references Staff table)',\n" +
                        "    usage_duration DECIMAL(5,2) NOT NULL COMMENT 'Usage duration (hours)',\n" +
                        "    usage_quantity INT NOT NULL COMMENT 'Usage count (e.g., startup times)',\n" +
                        "    notes TEXT COMMENT 'Usage notes (e.g., fault records, operation points)',\n" +
                        "    PRIMARY KEY (robot_usage_id),\n" +
                        "    FOREIGN KEY (robot_id) REFERENCES Robot(robot_id) ON DELETE CASCADE,\n" +
                        "    FOREIGN KEY (activity_id) REFERENCES Activity(activity_id) ON DELETE CASCADE,\n" +
                        "    FOREIGN KEY (operator_staff_id) REFERENCES Staff(staff_id) ON DELETE SET NULL,\n" +
                        "    -- Ensure robot is in enabled state\n" +
                        "    CHECK ((SELECT active_flag FROM Robot WHERE robot_id = Robot_Usage.robot_id) = 'Y'),\n" +
                        "    CHECK (usage_duration >= 0),\n" +
                        "    CHECK (usage_quantity >= 1),\n" +
                        "    UNIQUE KEY uk_robot_activity_datetime (robot_id, activity_id, usage_datetime),\n" +
                        "    INDEX idx_robot_usage_robot (robot_id, usage_datetime),\n" +
                        "    INDEX idx_robot_usage_activity (activity_id, robot_id),\n" +
                        "    INDEX idx_robot_usage_operator (operator_staff_id)\n" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Stores robot participation records in activities, ensures data integrity';\n";
                stmt.executeUpdate(createRobotUsage);
                System.out.println("✅ Table `Robot_Usage` created successfully!");
            } catch (Exception e) {
                System.err.println("❌ Table `Robot_Usage` creation failed!");
                e.printStackTrace();
            }
            try {
                // Create chemical safety check table (depends on Activity, Chemical, Staff tables)
                String createSafetyCheck = "-- 25. Chemical safety check table (depends on Activity, Chemical, Staff tables)\n" +
                        "CREATE TABLE Safety_Check (\n" +
                        "    safety_check_id INT AUTO_INCREMENT COMMENT 'Check record unique identifier',\n" +
                        "    activity_id INT NOT NULL COMMENT 'Activity ID (references Activity table)',\n" +
                        "    chemical_id INT NOT NULL COMMENT 'Chemical ID (references Chemical table)',\n" +
                        "    check_datetime DATETIME NOT NULL COMMENT 'Check time',\n" +
                        "    checked_by_staff_id INT NOT NULL COMMENT 'Checker staff ID (references Staff table)',\n" +
                        "    check_items TEXT NOT NULL COMMENT 'Check items details (e.g., concentration compliance, storage compliance)',\n" +
                        "    check_result VARCHAR(20) NOT NULL COMMENT 'Check result (passed, failed, pending)',\n" +
                        "    rectification_measures TEXT COMMENT 'Rectification measures (e.g., handling plan if failed)',\n" +
                        "    notes TEXT COMMENT 'Check notes (e.g., environmental conditions, special circumstances)',\n" +
                        "    PRIMARY KEY (safety_check_id),\n" +
                        "    FOREIGN KEY (activity_id) REFERENCES Activity(activity_id) ON DELETE CASCADE,\n" +
                        "    FOREIGN KEY (chemical_id) REFERENCES Chemical(chemical_id) ON DELETE CASCADE,\n" +
                        "    FOREIGN KEY (checked_by_staff_id) REFERENCES Staff(staff_id) ON DELETE RESTRICT,\n" +
                        "    CHECK (check_result IN ('passed', 'failed', 'pending')),\n" +
                        "    INDEX idx_safety_check_activity (activity_id, check_result),\n" +
                        "    INDEX idx_safety_check_chemical (chemical_id, check_datetime),\n" +
                        "    INDEX idx_safety_check_staff (checked_by_staff_id, check_datetime)\n" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Stores safety check records for chemical usage in activities, supports compliance tracking';\n";
                stmt.executeUpdate(createSafetyCheck);
                System.out.println("✅ Table `Safety_Check` created successfully!");
            } catch (Exception e) {
                System.err.println("❌ Table `Safety_Check` creation failed!");
                e.printStackTrace();
            }

        } catch (Exception e) {
            System.err.println("❌ Operation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}