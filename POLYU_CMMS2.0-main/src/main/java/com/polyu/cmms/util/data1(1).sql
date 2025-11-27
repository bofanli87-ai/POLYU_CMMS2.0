/* Enter "USE {database};" to start exploring your data.
   Press Ctrl + I to try out AI-generated SQL queries or SQL rewrite using Chat2Query. */
USE test;

-- 1. Role Table (Role) - 3 records (covering all roles)
INSERT INTO Role (role_name, role_level, description, active_flag)
VALUES 
('executive_officer', 1, 'Responsible for overall campus operation management, policy formulation', 'Y'),
('mid_level_manager', 2, 'Responsible for specific building/area operation management, staff scheduling', 'Y'),
('base_level_worker', 3, 'Perform specific tasks like cleaning, maintenance, equipment operation', 'Y');

-- 2. System Configuration Table (SystemLimits) - 12 records (covering different effective dates, activation status)
INSERT INTO SystemLimits (max_mid_level_managers, max_base_level_workers, effective_date)
VALUES 
(10, 50, '2024-01-01'),
(12, 60, '2024-07-01'),
(15, 70, '2025-01-01'),
(12, 65, '2025-07-01'),
(14, 68, '2023-01-01'),
(11, 55, '2023-07-01'),
(13, 62, '2026-01-01'),
(12, 60, '2026-07-01'),
(10, 58, '2022-01-01'),
(9, 52, '2022-07-01'),
(15, 72, '2027-01-01'),
(16, 75, '2027-07-01');

-- 3. Address Table (Address) - 15 records (covering different regions, detailed addresses)
INSERT INTO Address (street, city, postal_code, country, detail, active_flag)
VALUES 
('Central Street', 'Beijing', '100001', 'China', 'No.123, Building A', 'Y'),
('West Avenue', 'Beijing', '100002', 'China', 'No.45, Industrial Park', 'Y'),
('East Road', 'Shanghai', '200001', 'China', 'No.67, Commercial District', 'Y'),
('South Street', 'Guangzhou', '510000', 'China', 'No.89, Residential Area', 'Y'),
('North Boulevard', 'Shenzhen', '518000', 'China', 'No.10, High-tech Zone', 'Y'),
('Lake Road', 'Chengdu', '610000', 'China', 'No.11, Lakeside Community', 'Y'),
('Mountain Avenue', 'Chongqing', '400000', 'China', 'No.13, Hilltop Area', 'Y'),
('River Street', 'Wuhan', '430000', 'China', 'No.15, Riverside Building', 'Y'),
('Park Road', 'Nanjing', '210000', 'China', 'No.17, Park North Gate', 'Y'),
('Garden Avenue', 'Hangzhou', '310000', 'China', 'No.19, Garden District', 'Y'),
('Forest Street', 'Qingdao', '266000', 'China', 'No.21, Coastal Zone', 'Y'),
('Ocean Road', 'Xiamen', '361000', 'China', 'No.23, Seaside Building', 'Y'),
('Desert Avenue', 'Urumqi', '830000', 'China', 'No.25, Urban Center', 'Y'),
('Snow Road', 'Harbin', '150000', 'China', 'No.27, Ice City District', 'Y'),
('Cloud Street', 'Kunming', '650000', 'China', 'No.29, Plateau Area', 'Y');

-- 4. Staff Skills Table (Skill) - 12 records (covering different skill types)
INSERT INTO Skill (skill_name, description)
VALUES 
('robot_operation', 'Cleaning/maintenance/inspection robot operation and basic troubleshooting'),
('chemical_use', 'Standardized use and storage of low/medium/high risk chemicals'),
('electrical_repair', 'Building circuit, lighting equipment repair and maintenance'),
('mechanical_maintenance', 'Mechanical equipment, pipeline system repair and maintenance'),
('cleaning_service', 'Indoor/outdoor cleaning, disinfection, waste disposal'),
('safety_inspection', 'Activity safety risk assessment and compliance inspection'),
('emergency_response', 'Weather emergency, equipment failure emergency response'),
('building_inspection', 'Building structure, facility daily inspection'),
('inventory_management', 'Chemical, consumables inventory management and requisition registration'),
('contract_management', 'Outsourcing contract negotiation, execution and acceptance'),
('robot_maintenance', 'Robot regular maintenance, parts replacement and system upgrade'),
('hazardous_waste_disposal', 'Hazardous waste classification and compliant disposal');

-- 5. Robot Table (Robot) - 15 records (covering different types, maintenance cycles)
INSERT INTO Robot (type, robot_capability, create_date, last_maintained_date, maintenance_cycle, active_flag)
VALUES 
('cleaning_robot', 'Ground sweeping, vacuuming, mopping integration', '2022-01-15', '2024-10-20', 30, 'Y'),
('repair_robot', 'Pipeline inspection, small parts replacement', '2022-03-20', '2024-10-15', 60, 'Y'),
('inspection_robot', 'Building exterior wall, high-altitude facility inspection', '2022-05-10', '2024-10-10', 45, 'Y'),
('cleaning_robot', 'Indoor formaldehyde purification, air disinfection', '2022-07-05', '2024-10-25', 30, 'Y'),
('repair_robot', 'Circuit fault location, emergency repair', '2022-09-12', '2024-10-05', 60, 'Y'),
('inspection_robot', 'Fire facilities, safety passage inspection', '2022-11-18', '2024-10-30', 45, 'Y'),
('cleaning_robot', 'Square, playground large area cleaning', '2023-01-20', '2024-10-18', 25, 'Y'),
('repair_robot', 'Air conditioning system, ventilation equipment repair', '2023-03-15', '2024-10-12', 50, 'Y'),
('inspection_robot', 'Chemical warehouse environment, safety compliance inspection', '2023-05-22', '2024-10-08', 40, 'Y'),
('cleaning_robot', 'Canteen, restaurant grease cleaning and disinfection', '2023-07-10', '2024-10-22', 20, 'Y'),
('repair_robot', 'Gate access system, passage equipment repair', '2023-09-05', '2024-10-03', 55, 'Y'),
('inspection_robot', 'Floor, room facility integrity inspection', '2023-11-12', '2024-10-28', 35, 'Y'),
('cleaning_robot', 'Underground garage cleaning, drainage system cleaning', '2024-01-08', '2024-10-24', 28, 'Y'),
('repair_robot', 'Square lighting, landscape facility repair', '2024-03-10', '2024-10-16', 48, 'Y'),
('inspection_robot', 'Emergency passage, evacuation sign inspection', '2024-05-15', '2024-10-01', 38, 'Y');

-- 6. Weather Emergency Event Table (Weather_Emergency) - 10 records (covering different event types)
INSERT INTO Weather_Emergency (name, type, start_date, end_date, active_flag)
VALUES 
('Beijing Heavy Rain 2024', 'rainfall', '2024-07-20 08:00:00', '2024-07-22 12:00:00', 'N'),
('Typhoon "Doksuri" 2024', 'typhoon', '2024-08-01 14:00:00', '2024-08-03 09:00:00', 'N'),
('Northern Cold Wave 2024', 'low_temperature', '2024-11-10 00:00:00', '2024-11-15 18:00:00', 'N'),
('Shanghai Strong Wind 2024', 'strong_wind', '2024-06-15 10:00:00', '2024-06-15 16:00:00', 'N'),
('Guangzhou Heavy Rain 2024', 'rainfall', '2024-05-25 09:00:00', '2024-05-26 11:00:00', 'N'),
('Shenzhen Blizzard 2024', 'blizzard', '2024-02-05 03:00:00', '2024-02-06 07:00:00', 'N'),
('Expected Heavy Rain 2025', 'rainfall', '2025-06-01 00:00:00', '2025-06-03 23:59:59', 'Y'),
('Expected Typhoon 2025', 'typhoon', '2025-07-15 00:00:00', '2025-07-18 23:59:59', 'Y'),
('Expected Cold Wave 2025', 'low_temperature', '2025-12-01 00:00:00', '2025-12-07 23:59:59', 'Y'),
('Expected Strong Wind 2025', 'strong_wind', '2025-04-10 00:00:00', '2025-04-12 23:59:59', 'Y');

-- 7. Chemical Table (Chemical) - 18 records (covering different types, hazard levels)
INSERT INTO Chemical (product_code, name, type, manufacturer, msds_url, hazard_category, storage_requirements, active_flag)
VALUES 
('CHEM-001', 'Medical Alcohol (75%)', 'disinfectant', 'Beijing Medical Supplies Co.', 'https://msds.example.com/chem001', 'medium', 'Cool and dry, sealed storage, away from fire sources', 'Y'),
('CHEM-002', 'Industrial Cleaner', 'detergent', 'Shanghai Cleaning Products Co.', 'https://msds.example.com/chem002', 'low', 'Room temperature storage, avoid direct sunlight', 'Y'),
('CHEM-003', 'Mechanical Lubricant', 'lubricant', 'Guangzhou Machinery Co.', 'https://msds.example.com/chem003', 'low', 'Sealed storage, away from high temperature', 'Y'),
('CHEM-004', 'Strong Oxidizing Agent', 'other', 'Shenzhen Chemical Co.', 'https://msds.example.com/chem004', 'high', 'Cool and ventilated, separate storage, avoid contact with reducing agents', 'Y'),
('CHEM-005', 'Formaldehyde Disinfectant', 'disinfectant', 'Chengdu Chemical Supplies Co.', 'https://msds.example.com/chem005', 'high', 'Sealed and light-proof, ventilated storage, wear protective equipment', 'Y'),
('CHEM-006', 'Glass Cleaner', 'detergent', 'Chongqing Daily Supplies Co.', 'https://msds.example.com/chem006', 'low', 'Room temperature storage, avoid children contact', 'Y'),
('CHEM-007', 'Circuit Cleaner', 'solvent', 'Wuhan Electronic Co.', 'https://msds.example.com/chem007', 'medium', 'Sealed storage, away from fire sources and static electricity', 'Y'),
('CHEM-008', 'Pipe Unblocker', 'detergent', 'Nanjing Hardware Co.', 'https://msds.example.com/chem008', 'medium', 'Room temperature storage, avoid mixing with acidic substances', 'Y'),
('CHEM-009', 'Rust Inhibitor', 'lubricant', 'Hangzhou Metal Co.', 'https://msds.example.com/chem009', 'low', 'Sealed storage, moisture-proof', 'Y'),
('CHEM-010', 'High Concentration Disinfectant', 'disinfectant', 'Qingdao Sanitary Co.', 'https://msds.example.com/chem010', 'high', 'Professional storage area, dual-person dual-lock management', 'Y'),
('CHEM-011', 'Rubber Solvent', 'solvent', 'Xiamen Chemical Co.', 'https://msds.example.com/chem011', 'medium', 'Ventilated storage, away from fire sources', 'Y'),
('CHEM-012', 'Floor Wax', 'detergent', 'Urumqi Daily Co.', 'https://msds.example.com/chem012', 'low', 'Room temperature storage, avoid direct sunlight', 'Y'),
('CHEM-013', 'Metal Cleaner', 'detergent', 'Harbin Hardware Co.', 'https://msds.example.com/chem013', 'low', 'Room temperature storage, sealed storage', 'Y'),
('CHEM-014', 'Laboratory Alcohol (95%)', 'solvent', 'Kunming Laboratory Supplies Co.', 'https://msds.example.com/chem014', 'medium', 'Cool and dry, away from fire sources, professional area storage', 'Y'),
('CHEM-015', 'Descaling Agent', 'detergent', 'Beijing Daily Co.', 'https://msds.example.com/chem015', 'low', 'Room temperature storage, avoid mixing with alkaline substances', 'Y'),
('CHEM-016', 'Industrial Solvent', 'solvent', 'Shanghai Industrial Co.', 'https://msds.example.com/chem016', 'high', 'Explosion-proof storage area, good ventilation, wear protective equipment', 'Y'),
('CHEM-017', 'Plant Disinfectant', 'disinfectant', 'Guangzhou Green Co.', 'https://msds.example.com/chem017', 'low', 'Room temperature storage, use quickly after opening', 'Y'),
('CHEM-018', 'Mechanical Rust Remover', 'other', 'Shenzhen Hardware Co.', 'https://msds.example.com/chem018', 'medium', 'Ventilated storage, avoid skin contact', 'Y');

-- #################################### Related Table Data (depends on base tables) ####################################
-- 8. Staff Table (Staff) - 20 records (covering different roles, contact methods)
INSERT INTO Staff (staff_number, first_name, last_name, date_of_birth, gender, role_id, email, phone, hire_date, emergency_contact, emergency_phone, active_flag)
VALUES 
('EMP-001', 'Zhang', 'San', '1980-01-15', 'M', 1, 'zhang.san@cmms.com', '13800138001', '2010-03-01', 'Li Si', '13900139001', 'Y'),
('EMP-002', 'Li', 'Si', '1985-03-20', 'M', 2, 'li.si@cmms.com', '13800138002', '2012-05-10', 'Wang Wu', '13900139002', 'Y'),
('EMP-003', 'Wang', 'Wu', '1990-05-10', 'F', 2, 'wang.wu@cmms.com', '13800138003', '2014-07-15', 'Zhao Liu', '13900139003', 'Y'),
('EMP-004', 'Zhao', 'Liu', '1995-07-05', 'M', 3, 'zhao.liu@cmms.com', '13800138004', '2016-09-20', 'Chen Qi', '13900139004', 'Y'),
('EMP-005', 'Chen', 'Qi', '1992-09-12', 'F', 3, 'chen.qi@cmms.com', '13800138005', '2015-11-01', 'Yang Ba', '13900139005', 'Y'),
('EMP-006', 'Yang', 'Ba', '1988-11-18', 'M', 3, 'yang.ba@cmms.com', '13800138006', '2013-01-10', 'Huang Jiu', '13900139006', 'Y'),
('EMP-007', 'Huang', 'Jiu', '1993-01-20', 'F', 3, 'huang.jiu@cmms.com', '13800138007', '2017-03-15', 'Zhou Shi', '13900139007', 'Y'),
('EMP-008', 'Zhou', 'Shi', '1987-03-15', 'M', 2, 'zhou.shi@cmms.com', '13800138008', '2011-05-20', 'Wu Bai', '13900139008', 'Y'),
('EMP-009', 'Wu', 'Bai', '1994-05-22', 'F', 3, 'wu.bai@cmms.com', '13800138009', '2018-07-25', 'Xu Shan', '13900139009', 'Y'),
('EMP-010', 'Xu', 'Shan', '1991-07-10', 'M', 3, 'xu.shan@cmms.com', '13800138010', '2016-09-30', 'Hu Ya', '13900139010', 'Y'),
('EMP-011', 'Hu', 'Ya', '1989-09-05', 'F', 3, 'hu.ya@cmms.com', '13800138011', '2014-11-05', 'Guo Jia', '13900139011', 'Y'),
('EMP-012', 'Guo', 'Jia', '1996-11-12', 'M', 3, 'guo.jia@cmms.com', '13800138012', '2019-01-10', 'He Min', '13900139012', 'Y'),
('EMP-013', 'He', 'Min', '1986-01-08', 'F', 2, 'he.min@cmms.com', '13800138013', '2010-03-15', 'Lin Tao', '13900139013', 'Y'),
('EMP-014', 'Lin', 'Tao', '1993-03-10', 'M', 3, 'lin.tao@cmms.com', '13800138014', '2020-05-15', 'Zheng Hua', '13900139014', 'Y'),
('EMP-015', 'Zheng', 'Hua', '1990-05-15', 'F', 3, 'zheng.hua@cmms.com', '13800138015', '2018-07-20', 'Xie Jun', '13900139015', 'Y'),
('EMP-016', 'Xie', 'Jun', '1984-07-20', 'M', 2, 'xie.jun@cmms.com', '13800138016', '2009-09-01', 'Wang Hong', '13900139016', 'Y'),
('EMP-017', 'Wang', 'Hong', '1992-09-25', 'F', 3, 'wang.hong@cmms.com', '13800138017', '2021-01-25', 'Chen Ming', '13900139017', 'Y'),
('EMP-018', 'Chen', 'Ming', '1995-11-30', 'M', 3, 'chen.ming@cmms.com', '13800138018', '2022-03-10', 'Li Ying', '13900139018', 'Y'),
('EMP-019', 'Li', 'Ying', '1983-12-05', 'F', 2, 'li.ying@cmms.com', '13800138019', '2008-05-15', 'Zhang Wei', '13900139019', 'Y'),
('EMP-020', 'Zhang', 'Wei', '1994-02-10', 'M', 3, 'zhang.wei@cmms.com', '13800138020', '2023-07-01', 'Liu Fang', '13900139020', 'Y');

-- 9. External Company Table (Company) - 15 records (covering different expertise areas)
INSERT INTO Company (contractor_code, name, contact_name, contract_quote, email, phone, address_id, expertise, tax_id, bank_account, active_flag)
VALUES 
('COM-001', 'Beijing Construction Co.', 'Li Ming', 1500.00, 'contact@beijing-construction.com', '010-12345678', 1, 'Building repair, structural reinforcement', '91110000MA0001', '11001010200012345678', 'Y'),
('COM-002', 'Shanghai Cleaning Service Co.', 'Wang Hong', 800.00, 'contact@shanghai-cleaning.com', '021-87654321', 2, 'Campus cleaning, garbage removal', '91310000MA0002', '11001010200087654321', 'Y'),
('COM-003', 'Guangzhou Mechanical Maintenance Co.', 'Zhao Wei', 1200.00, 'contact@guangzhou-mech.com', '020-13579246', 3, 'Mechanical equipment repair, pipeline maintenance', '91440100MA0003', '11001010200013579246', 'Y'),
('COM-004', 'Shenzhen High-tech Co.', 'Chen Fang', 2000.00, 'contact@shenzhen-hightech.com', '0755-24681357', 4, 'Robot maintenance, smart equipment debugging', '91440300MA0004', '11001010200024681357', 'Y'),
('COM-005', 'Chengdu Sanitary Co.', 'Yang Tao', 750.00, 'contact@chengdu-sanitary.com', '028-11223344', 5, 'Disinfection service, environmental testing', '91510100MA0005', '11001010200011223344', 'Y'),
('COM-006', 'Chongqing Road Co.', 'Huang Qiang', 1600.00, 'contact@chongqing-road.com', '023-55667788', 6, 'Road repair, square maintenance', '91500000MA0006', '11001010200055667788', 'Y'),
('COM-007', 'Wuhan Electrical Co.', 'Zhou Min', 1300.00, 'contact@wuhan-electrical.com', '027-99887766', 7, 'Circuit repair, lighting equipment installation', '91420100MA0007', '11001010200099887766', 'Y'),
('COM-008', 'Nanjing Gardening Co.', 'Wu Jie', 900.00, 'contact@nanjing-gardening.com', '025-33445566', 8, 'Greening maintenance, landscape facility maintenance', '91320100MA0008', '11001010200033445566', 'Y'),
('COM-009', 'Hangzhou Environmental Co.', 'Xu Feng', 1100.00, 'contact@hangzhou-env.com', '0571-66778899', 9, 'Environmental testing, waste disposal', '91330100MA0009', '11001010200066778899', 'Y'),
('COM-010', 'Qingdao Coastal Co.', 'Hu Li', 1400.00, 'contact@qingdao-coastal.com', '0532-88990011', 10, 'Coastal facility repair, anti-corrosion treatment', '91370200MA0010', '11001010200088990011', 'Y'),
('COM-011', 'Xiamen Seaside Co.', 'Guo Hong', 1000.00, 'contact@xiamen-seaside.com', '0592-22334455', 11, 'Seawater desalination equipment maintenance, seaside cleaning', '91350200MA0011', '11001010200022334455', 'Y'),
('COM-012', 'Urumqi Engineering Co.', 'Xie Lin', 1800.00, 'contact@urumqi-eng.com', '0991-33445566', 12, 'Gobi area facility repair, insulation engineering', '91650100MA0012', '11001010200033445566', 'Y'),
('COM-013', 'Harbin Ice Co.', 'Lin Qiang', 1700.00, 'contact@harbin-ice.com', '0451-44556677', 13, 'Ice and snow facility maintenance, anti-freeze engineering', '91230100MA0013', '11001010200044556677', 'Y'),
('COM-014', 'Kunming Plateau Co.', 'Zheng Hua', 950.00, 'contact@kunming-plateau.com', '0871-55667788', 14, 'Plateau facility maintenance, greening maintenance', '91530100MA0014', '11001010200055667788', 'Y'),
('COM-015', 'Beijing Emergency Co.', 'Wang Tao', 2200.00, 'contact@beijing-emergency.com', '010-66778899', 15, 'Emergency rescue, post-disaster repair', '91110000MA0015', '11001010200066778899', 'Y');

-- 10. Buildings Table (Buildings) - 12 records (covering different floor counts, supervisors)
INSERT INTO Buildings (building_code, construction_date, address_id, num_floors, supervisor_staff_id, active_flag)
VALUES 
('B-001', '2000-01-15', 1, 10, 2, 'Y'), -- Supervisor: Li Si (mid-level manager, staff_id=2)
('B-002', '2005-03-20', 2, 8, 3, 'Y'), -- Supervisor: Wang Wu (mid-level manager, staff_id=3)
('B-003', '2010-05-10', 3, 12, 8, 'Y'), -- Supervisor: Zhou Shi (mid-level manager, staff_id=8)
('B-004', '2015-07-05', 4, 6, 13, 'Y'), -- Supervisor: He Min (mid-level manager, staff_id=13)
('B-005', '2008-09-12', 5, 15, 16, 'Y'), -- Supervisor: Xie Jun (mid-level manager, staff_id=16)
('B-006', '2012-11-18', 6, 7, 19, 'Y'), -- Supervisor: Li Ying (mid-level manager, staff_id=19)
('B-007', '2018-01-20', 7, 9, 2, 'Y'), -- Supervisor: Li Si (mid-level manager, staff_id=2)
('B-008', '2003-03-15', 8, 5, 3, 'Y'), -- Supervisor: Wang Wu (mid-level manager, staff_id=3)
('B-009', '2013-05-22', 9, 11, 8, 'Y'), -- Supervisor: Zhou Shi (mid-level manager, staff_id=8)
('B-010', '2007-07-10', 10, 4, 13, 'Y'), -- Supervisor: He Min (mid-level manager, staff_id=13)
('B-011', '2016-09-05', 11, 8, 16, 'Y'), -- Supervisor: Xie Jun (mid-level manager, staff_id=16)
('B-012', '2011-11-12', 12, 10, 19, 'Y'); -- Supervisor: Li Ying (mid-level manager, staff_id=19)

-- 11. Levels Table (Levels) - 18 records (1-2 levels per building)
INSERT INTO Levels (building_id, level_number, active_flag)
VALUES 
(1, 1, 'Y'), (1, 2, 'Y'), (1, 3, 'Y'), -- Building 1: 3 levels
(2, 1, 'Y'), (2, 2, 'Y'), -- Building 2: 2 levels
(3, 1, 'Y'), (3, 2, 'Y'), (3, 3, 'Y'), (3, 4, 'Y'), -- Building 3: 4 levels
(4, 1, 'Y'), (4, 2, 'Y'), -- Building 4: 2 levels
(5, 1, 'Y'), (5, 2, 'Y'), (5, 3, 'Y'), -- Building 5: 3 levels
(6, 1, 'Y'), (6, 2, 'Y'), -- Building 6: 2 levels
(7, 1, 'Y'), (7, 2, 'Y'); -- Building 7: 2 levels

SELECT * FROM Levels;

-- 12. Rooms Table (Rooms) - 20 records (2-3 rooms per building)
INSERT INTO Rooms (building_id, name, room_type, capacity, room_features, active_flag)
VALUES 
(1, 'Room 101', 'classroom', 50, 'Air conditioning, projector, blackboard', 'Y'),
(1, 'Room 102', 'office', 10, 'Air conditioning, computer, printer', 'Y'),
(1, 'Room 201', 'laboratory', 20, 'Ventilation system, lab bench, instrument equipment', 'Y'),
(2, 'Room 101', 'meeting_room', 30, 'Air conditioning, projector, audio equipment', 'Y'),
(2, 'Room 102', 'equipment_room', 5, 'Power distribution equipment, air conditioning main unit', 'Y'),
(3, 'Room 101', 'canteen', 100, 'Dining tables, kitchen equipment, disinfection equipment', 'Y'),
(3, 'Room 201', 'office', 8, 'Air conditioning, computer, filing cabinet', 'Y'),
(3, 'Room 301', 'classroom', 40, 'Air conditioning, projector, multimedia', 'Y'),
(4, 'Room 101', 'maintenance_room', 6, 'Tools, spare parts, workbench', 'Y'),
(4, 'Room 102', 'warehouse', 20, 'Shelving, forklift, ventilation equipment', 'Y'),
(5, 'Room 101', 'hall', 200, 'Lighting, monitoring, elevator', 'Y'),
(5, 'Room 201', 'office', 12, 'Air conditioning, computer, conference table', 'Y'),
(5, 'Room 301', 'activity_room', 80, 'Audio equipment, stage, seating', 'Y'),
(6, 'Room 101', 'guard_room', 2, 'Monitoring, communication equipment', 'Y'),
(6, 'Room 102', 'rest_room', 8, 'Sofa, water dispenser, television', 'Y'),
(7, 'Room 101', 'laboratory', 25, 'Laboratory equipment, ventilation system', 'Y'),
(7, 'Room 201', 'office', 10, 'Air conditioning, computer, printer', 'Y'),
(8, 'Room 101', 'classroom', 45, 'Air conditioning, projector, blackboard', 'Y'),
(8, 'Room 102', 'office', 9, 'Air conditioning, computer, filing cabinet', 'Y'),
(9, 'Room 101', 'meeting_room', 25, 'Air conditioning, projector, whiteboard', 'Y');

-- 13. Squares Table (Squares) - 10 records (covering different names, capacities)
INSERT INTO Squares (name, address_id, capacity, active_flag)
VALUES 
('Central Square', 1, 500, 'Y'),
('West Square', 2, 300, 'Y'),
('East Square', 3, 250, 'Y'),
('South Square', 4, 400, 'Y'),
('North Square', 5, 350, 'Y'),
('Lakeside Square', 6, 200, 'Y'),
('Hilltop Square', 7, 150, 'Y'),
('Riverside Square', 8, 450, 'Y'),
('Park Square', 9, 300, 'Y'),
('Garden Square', 10, 280, 'Y');

-- 14. Gates Table (Gates) - 12 records (covering different names, flow capacities)
INSERT INTO Gates (name, address_id, flow_capacity, active_flag)
VALUES 
('East Gate', 1, 200, 'Y'),
('West Gate', 2, 150, 'Y'),
('South Gate', 3, 250, 'Y'),
('North Gate', 4, 180, 'Y'),
('Emergency East Gate', 5, 100, 'Y'),
('Emergency West Gate', 6, 80, 'Y'),
('VIP Gate', 7, 50, 'Y'),
('Goods Gate', 8, 120, 'Y'),
('Student Gate', 9, 300, 'Y'),
('Staff Gate', 10, 100, 'Y'),
('Back Gate', 11, 80, 'Y'),
('Side Gate', 12, 60, 'Y');

-- 15. Canteen Table (Canteen) - 10 records (covering different food types)
INSERT INTO Canteen (name, construction_date, address_id, food_type, active_flag)
VALUES 
('First Canteen', '2000-01-15', 1, 'Chinese', 'Y'),
('Second Canteen', '2005-03-20', 2, 'Western', 'Y'),
('Third Canteen', '2010-05-10', 3, 'Mixed', 'Y'),
('Halal Canteen', '2015-07-05', 4, 'Chinese', 'Y'),
('Staff Canteen', '2008-09-12', 5, 'Mixed', 'Y'),
('Student Canteen', '2012-11-18', 6, 'Chinese', 'Y'),
('Lakeside Canteen', '2018-01-20', 7, 'Western', 'Y'),
('Hilltop Canteen', '2003-03-15', 8, 'Mixed', 'Y'),
('Riverside Canteen', '2013-05-22', 9, 'Chinese', 'Y'),
('Park Canteen', '2007-07-10', 10, 'Western', 'Y');

-- 16. External Area Table (Area) - 10 records (covering different area types)
INSERT INTO Area (area_type, description, address_id, active_flag)
VALUES 
('campus_external', 'Square outside campus east gate', 1, 'Y'),
('campus_external', 'Road outside campus west gate', 2, 'Y'),
('temporary_work_area', 'Temporary construction area for Building B1', 3, 'Y'),
('temporary_work_area', 'Temporary maintenance area for Central Square', 4, 'Y'),
('campus_external', 'Parking lot outside campus south gate', 5, 'Y'),
('campus_external', 'Green belt outside campus north gate', 6, 'Y'),
('temporary_work_area', 'Temporary area for canteen renovation', 7, 'Y'),
('other', 'Campus waste disposal station area', 8, 'Y'),
('other', 'External buffer zone for chemical warehouse', 9, 'Y'),
('temporary_work_area', 'Temporary area for gate maintenance', 10, 'Y');

-- #################################### Business Related Table Data (depends on base/related tables) ####################################
-- 17. Staff-Skill Association Table (Staff_Skill_Map) - 20 records (1-2 skills per staff)
INSERT INTO Staff_Skill_Map (staff_id, skill_id, proficiency)
VALUES 
(4, 1, 'intermediate'), (4, 5, 'senior'), -- Staff 4: Robot operation, cleaning service
(5, 2, 'junior'), (5, 10, 'intermediate'), -- Staff 5: Chemical use, contract management
(6, 3, 'senior'), (6, 4, 'intermediate'), -- Staff 6: Electrical repair, mechanical maintenance
(7, 5, 'intermediate'), (7, 7, 'junior'), -- Staff 7: Cleaning service, emergency response
(8, 6, 'senior'), (8, 11, 'intermediate'), -- Staff 8: Safety inspection, robot maintenance
(9, 1, 'junior'), (9, 9, 'intermediate'), -- Staff 9: Robot operation, inventory management
(10, 2, 'intermediate'), (10, 12, 'senior'), -- Staff 10: Chemical use, hazardous waste disposal
(11, 3, 'intermediate'), (11, 7, 'junior'), -- Staff 11: Electrical repair, emergency response
(12, 4, 'senior'), (12, 8, 'intermediate'), -- Staff 12: Mechanical maintenance, pipeline unblocking
(13, 6, 'intermediate'), (13, 10, 'senior'); -- Staff 13: Safety inspection, contract management

-- 18. Supervision Relationship Table (Supervise) - 15 records (supervisor: mid-level/executive, subordinate: base-level/mid-level)
INSERT INTO Supervise (supervisor_staff_id, subordinate_staff_id, start_date, end_date)
VALUES 
(2, 4, '2016-09-20', NULL), (2, 5, '2015-11-01', NULL), (2, 6, '2013-01-10', NULL), -- Mid-level manager 2 (Li Si) supervises base-level staff 4-6
(3, 7, '2017-03-15', NULL), (3, 8, '2011-05-20', NULL), (3, 9, '2018-07-25', NULL), -- Mid-level manager 3 (Wang Wu) supervises base-level staff 7-9
(8, 10, '2016-09-30', NULL), (8, 11, '2014-11-05', NULL), (8, 12, '2019-01-10', NULL), -- Mid-level manager 8 (Zhou Shi) supervises base-level staff 10-12
(13, 14, '2020-05-15', NULL), (13, 15, '2018-07-20', NULL), -- Mid-level manager 13 (He Min) supervises base-level staff 14-15
(16, 17, '2021-01-25', NULL), (16, 18, '2022-03-10', NULL), -- Mid-level manager 16 (Xie Jun) supervises base-level staff 17-18
(1, 2, '2012-05-10', NULL), (1, 3, '2014-07-15', NULL); -- Executive officer 1 (Zhang San) supervises mid-level managers 2-3

-- 19. Chemical Inventory Table (Chemical_Inventory) - 18 records (1 inventory record per chemical)
INSERT INTO Chemical_Inventory (chemical_id, quantity, storage_location, purchase_date, supplier_id, expiry_date, batch_number, active_flag)
VALUES 
(1, 50.00, 'Chemical Warehouse Area A', '2024-01-15', 5, '2025-01-15', 'B20240115', 'Y'),
(2, 100.00, 'Chemical Warehouse Area B', '2024-02-20', 2, '2026-02-20', 'B20240220', 'Y'),
(3, 80.00, 'Chemical Warehouse Area C', '2024-03-10', 3, '2027-03-10', 'B20240310', 'Y'),
(4, 30.00, 'Hazardous Chemicals Special Area', '2024-04-05', 4, '2025-04-05', 'B20240405', 'Y'),
(5, 20.00, 'Hazardous Chemicals Special Area', '2024-05-12', 5, '2025-05-12', 'B20240512', 'Y'),
(6, 90.00, 'Chemical Warehouse Area B', '2024-06-18', 6, '2026-06-18', 'B20240618', 'Y'),
(7, 40.00, 'Chemical Warehouse Area A', '2024-07-22', 7, '2025-07-22', 'B20240722', 'Y'),
(8, 60.00, 'Chemical Warehouse Area C', '2024-08-30', 8, '2026-08-30', 'B20240830', 'Y'),
(9, 70.00, 'Chemical Warehouse Area B', '2024-09-15', 9, '2027-09-15', 'B20240915', 'Y'),
(10, 15.00, 'Hazardous Chemicals Special Area', '2024-10-01', 10, '2025-10-01', 'B20241001', 'Y'),
(11, 45.00, 'Chemical Warehouse Area A', '2024-10-10', 11, '2025-10-10', 'B20241010', 'Y'),
(12, 85.00, 'Chemical Warehouse Area C', '2024-10-15', 12, '2026-10-15', 'B20241015', 'Y'),
(13, 55.00, 'Chemical Warehouse Area B', '2024-10-20', 13, '2027-10-20', 'B20241020', 'Y'),
(14, 35.00, 'Chemical Warehouse Area A', '2024-10-25', 14, '2025-10-25', 'B20241025', 'Y'),
(15, 65.00, 'Chemical Warehouse Area C', '2024-10-30', 15, '2026-10-30', 'B20241030', 'Y'),
(16, 10.00, 'Hazardous Chemicals Special Area', '2024-11-05', 4, '2025-11-05', 'B20241105', 'Y'),
(17, 75.00, 'Chemical Warehouse Area B', '2024-11-10', 5, '2026-11-10', 'B20241110', 'Y'),
(18, 25.00, 'Chemical Warehouse Area A', '2024-11-15', 3, '2025-11-15', 'B20241115', 'Y');

-- Verify Levels table (3,2) exists
SELECT * FROM Levels;
-- Verify all Rooms table association combinations exist (using first record as example)
SELECT * FROM Rooms WHERE building_id=1 AND room_id=1;
-- Verify all Gates/Canteen associations exist (using 5th record as example)
SELECT * FROM Gates WHERE gate_id=2;

-- 20. Activity Table (Activity) - 20 records (final fix for foreign key conflicts, ensure all association combinations exist)
INSERT INTO Activity (activity_type, title, description, status, priority, activity_datetime, expected_unavailable_duration, actual_completion_datetime, created_by_staff_id, weather_id, area_id, hazard_level, facility_type, building_id, room_id, level_id, square_id, gate_id, canteen_id, active_flag)
VALUES 
-- Cleaning activities
('cleaning', 'Building B-001 Room 101 Cleaning and Disinfection', 'Daily cleaning + alcohol disinfection, covering tables, floors, doors and windows', 'completed', 'medium', '2024-10-01 08:00:00', 2.00, '2024-10-01 10:00:00', 4, NULL, NULL, 'low', 'room', 1, 1, NULL, NULL, NULL, NULL, 'Y'),
('cleaning', 'Central Square Large Area Cleaning', 'Cleaning leaves, garbage, washing ground', 'completed', 'low', '2024-10-02 09:00:00', 3.00, '2024-10-02 12:00:00', 5, NULL, NULL, 'low', 'square', NULL, NULL, NULL, 1, NULL, NULL, 'Y'),
('cleaning', 'East Gate External Square Post-Typhoon Cleaning', 'Cleaning branches, garbage, accumulated water after typhoon', 'completed', 'high', '2024-08-02 10:00:00', 4.00, '2024-08-02 14:00:00', 6, 2, 1, 'medium', 'area', NULL, NULL, NULL, NULL, NULL, NULL, 'Y'),
-- Repair activities
('repair', 'Building B-002 Room 102 Circuit Repair', 'Repair power distribution equipment failure, replace aging lines', 'completed', 'high', '2024-10-03 14:00:00', 2.50, '2024-10-03 16:30:00', 7, NULL, NULL, 'medium', 'room', 2, 5, NULL, NULL, NULL, NULL, 'Y'),
('repair', 'West Gate Access System Repair', 'Repair access recognition failure, test passage function', 'in_progress', 'medium', '2024-10-10 09:00:00', 1.50, NULL, 8, NULL, NULL, 'low', 'gate', NULL, NULL, NULL, NULL, 2, NULL, 'Y'),
-- Key fix: Change level_id=3 to 2 (confirmed (3,2) exists in Levels table)
('repair', 'Building B-003 Air Conditioning System Repair', 'Repair 2nd floor air conditioning cooling failure, replace filter', 'planned', 'high', '2024-11-01 13:00:00', 3.00, NULL, 9, NULL, NULL, 'medium', 'level', 3, NULL, 6, NULL, NULL, NULL, 'Y'),
-- Emergency activities
('weather_response', 'Post-Heavy Rain Building B-005 Drainage System Cleaning', 'Clean basement accumulated water, unblock drainage pipes', 'completed', 'high', '2024-07-21 09:00:00', 5.00, '2024-07-21 14:00:00', 10, 1, NULL, 'high', 'building', 5, NULL, NULL, NULL, NULL, NULL, 'Y'),
('weather_response', 'Pre-Cold Wave Campus Water Pipe Anti-Freeze Treatment', 'Insulate exposed water pipes, check anti-freeze measures', 'planned', 'high', '2025-12-02 08:00:00', 6.00, NULL, 11, 9, NULL, 'medium', 'building', NULL, NULL, NULL, NULL, NULL, NULL, 'Y'),
-- Other activities
('cleaning', 'First Canteen Daily Cleaning and Disinfection', 'Dining tables, floors, kitchen area cleaning and disinfection', 'completed', 'medium', '2024-10-04 15:00:00', 2.00, '2024-10-04 17:00:00', 12, NULL, NULL, 'low', 'canteen', NULL, NULL, NULL, NULL, NULL, 1, 'Y'),
('repair', 'Building B-004 Room 101 Maintenance Room Tool Organization', 'Maintenance tool classification storage, check tool integrity', 'completed', 'low', '2024-10-05 10:00:00', 1.00, '2024-10-05 11:00:00', 13, NULL, NULL, 'low', 'room', 4, 9, NULL, NULL, NULL, NULL, 'Y'),
('cleaning', 'Chemical Warehouse External Buffer Zone Cleaning', 'Clean debris, check safety passages', 'in_progress', 'medium', '2024-10-15 14:00:00', 1.50, NULL, 14, NULL, 9, 'medium', 'area', NULL, NULL, NULL, NULL, NULL, NULL, 'Y'),
('repair', 'Central Square Lighting Facility Repair', 'Replace damaged street lights, test lighting effect', 'planned', 'medium', '2024-11-05 16:00:00', 2.00, NULL, 15, NULL, NULL, 'low', 'square', NULL, NULL, NULL, 1, NULL, NULL, 'Y'),
('weather_response', 'Pre-Strong Wind Outdoor Facility Reinforcement', 'Reinforce square sunshades, billboards and other outdoor facilities', 'completed', 'high', '2024-06-14 15:00:00', 3.00, '2024-06-14 18:00:00', 16, 4, NULL, 'medium', 'square', NULL, NULL, NULL, 1, NULL, NULL, 'Y'),
('cleaning', 'Building B-006 Room 101 Guard Room Cleaning', 'Daily cleaning, organize duty records', 'completed', 'low', '2024-10-06 08:30:00', 0.50, '2024-10-06 09:00:00', 17, NULL, NULL, 'low', 'room', 6, 14, NULL, NULL, NULL, NULL, 'Y'),
('repair', 'Building B-007 Room 201 Office Computer Repair', 'Fix computer boot failure, data backup', 'in_progress', 'medium', '2024-10-20 10:30:00', 2.00, NULL, 18, NULL, NULL, 'low', 'room', 7, 17, NULL, NULL, NULL, NULL, 'Y'),
('weather_response', 'Post-Blizzard Campus Road Snow Removal', 'Clear main passage snow, spread deicing agent', 'completed', 'high', '2024-02-05 08:00:00', 4.00, '2024-02-05 12:00:00', 19, 6, NULL, 'high', 'area', NULL, NULL, NULL, NULL, NULL, NULL, 'Y'),
('cleaning', 'Building B-008 Room 101 Classroom Cleaning', 'Desk, floor cleaning, blackboard wiping', 'planned', 'low', '2024-11-10 09:00:00', 1.00, NULL, 4, NULL, NULL, 'low', 'room', 8, 18, NULL, NULL, NULL, NULL, 'Y'),
('repair', 'South Gate Passage Equipment Repair', 'Fix barrier gate lifting failure', 'planned', 'high', '2024-11-08 14:00:00', 2.00, NULL, 5, NULL, NULL, 'medium', 'gate', NULL, NULL, NULL, NULL, 3, NULL, 'Y'),
('weather_response', 'Expected Typhoon Pre-Emergency Material Preparation', 'Prepare sandbags, water pumps, first aid materials, etc.', 'planned', 'high', '2025-07-14 10:00:00', 3.00, NULL, 6, 8, NULL, 'medium', 'building', 5, NULL, NULL, NULL, NULL, NULL, 'Y'),
('cleaning', 'Building B-009 Room 101 Meeting Room Cleaning', 'Pre-meeting cleaning, meeting venue setup', 'completed', 'medium', '2024-10-08 13:00:00', 1.00, '2024-10-08 14:00:00', 7, NULL, NULL, 'low', 'room', 9, 20, NULL, NULL, NULL, NULL, 'Y');

-- 21. Staff-Activity Association Table (WORKS_FOR) - 20 records (1-2 staff per activity)
INSERT INTO WORKS_FOR (staff_id, activity_id, activity_responsibility, assigned_datetime, active_flag)
VALUES 
(4, 1, 'Execute classroom cleaning and disinfection, operate cleaning equipment', '2024-09-30 10:00:00', 'Y'),
(5, 1, 'Prepare disinfection chemicals, supervise cleaning quality', '2024-09-30 10:00:00', 'Y'),
(6, 2, 'Operate cleaning robot, clean square garbage', '2024-10-01 09:00:00', 'Y'),
(7, 3, 'Clean branches, accumulated water, set warning signs', '2024-08-01 15:00:00', 'Y'),
(8, 4, 'Circuit fault troubleshooting, replace aging lines', '2024-10-02 10:00:00', 'Y'),
(9, 5, 'Access equipment disassembly, repair, testing', '2024-10-09 09:00:00', 'Y'),
(10, 6, 'Air conditioning system inspection, replace filter', '2024-10-25 14:00:00', 'Y'),
(11, 7, 'Operate water pump, clean basement accumulated water', '2024-07-20 16:00:00', 'Y'),
(12, 7, 'Unblock drainage pipes, check pipe integrity', '2024-07-20 16:00:00', 'Y'),
(13, 8, 'Water pipe insulation wrapping, anti-freeze measures check', '2025-11-25 10:00:00', 'Y'),
(14, 9, 'Canteen area cleaning, disinfection equipment operation', '2024-10-03 14:00:00', 'Y'),
(15, 10, 'Tool classification storage, integrity check', '2024-10-04 09:00:00', 'Y'),
(16, 11, 'Buffer zone cleaning, safety passage check', '2024-10-14 15:00:00', 'Y'),
(17, 12, 'Street light disassembly, replacement, lighting test', '2024-11-04 10:00:00', 'Y'),
(18, 13, 'Sunshade reinforcement, billboard check', '2024-06-13 14:00:00', 'Y'),
(4, 14, 'Guard room cleaning, duty record organization', '2024-10-05 08:00:00', 'Y'),
(5, 15, 'Computer fault troubleshooting, data backup', '2024-10-19 10:00:00', 'Y'),
(6, 16, 'Operate snow removal equipment, spread deicing agent', '2024-02-04 16:00:00', 'Y'),
(7, 17, 'Classroom cleaning, blackboard wiping', '2024-11-09 09:00:00', 'Y'),
(8, 18, 'Barrier gate equipment repair, lifting test', '2024-11-07 14:00:00', 'Y');

-- 22. Outsourcing Contract Table (Contract) - 10 records (each contract associated with one activity)
INSERT INTO Contract (contractor_id, activity_id, contract_date, contract_amount, end_date, status, payment_terms, notes)
VALUES 
(2, 2, '2024-09-25', 800.00, '2024-10-03', 'completed', 'Payment within 30 days after acceptance', 'Cleaning scope: Entire Central Square area'),
(3, 4, '2024-10-01', 1200.00, '2024-10-04', 'completed', 'Payment within 15 days after acceptance', 'Repair content: Equipment room circuit fault repair'),
(4, 6, '2024-10-20', 2000.00, NULL, 'active', '50% payment after repair completion, remaining 50% after 3-month warranty', 'Repair content: Air conditioning system cooling failure repair'),
(5, 9, '2024-10-02', 750.00, '2024-10-05', 'completed', 'Payment within 20 days after acceptance', 'Cleaning and disinfection scope: Entire First Canteen area'),
(1, 13, '2024-06-10', 1500.00, '2024-06-15', 'completed', 'One-time payment after reinforcement completion', 'Reinforcement facilities: Square sunshades, billboards'),
(6, 16, '2024-02-03', 1600.00, '2024-02-06', 'completed', 'Payment after snow removal completion', 'Snow removal scope: Campus main passages'),
(7, 18, '2024-11-05', 1300.00, NULL, 'active', 'Payment within 30 days after repair acceptance', 'Repair content: South Gate barrier gate lifting failure'),
(8, 12, '2024-10-30', 900.00, NULL, 'active', 'Payment after lighting facility repair completion', 'Repair content: Central Square street light replacement'),
(9, 11, '2024-10-12', 1100.00, NULL, 'active', 'Payment within 25 days after cleaning acceptance', 'Cleaning scope: Chemical warehouse external buffer zone'),
(10, 7, '2024-07-18', 1400.00, '2024-07-22', 'completed', 'Payment after drainage cleaning completion', 'Cleaning content: Building B-005 basement accumulated water');