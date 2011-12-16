PRAGMA foreign_keys=ON;
BEGIN TRANSACTION;
CREATE TABLE `services` (
	`id` INTEGER PRIMARY KEY, 
	`name` TEXT
);
CREATE TABLE `engines` (
	`id` INTEGER PRIMARY KEY, 
	`name` TEXT, 
	`host` TEXT, 
	`port` INTEGER, 
	`desc` TEXT
);
CREATE TABLE service_map (
	`service_id` INTEGER,
	`engine_id` INTEGER,
	CONSTRAINT `my_index` UNIQUE ( `service_id`, `engine_id` ),
	FOREIGN KEY (`service_id`) REFERENCES `services` (`id`) ON DELETE CASCADE,
	FOREIGN KEY (`engine_id`) REFERENCES `engines` ( `id`) ON DELETE CASCADE
);
INSERT INTO `services` VALUES (1, 'serv1');
INSERT INTO `engines` VALUES (1, 'eng1', '0.0.0.0', 1234, 'test');
INSERT INTO `service_map` VALUES (1, 1);
CREATE VIEW `services_to_engines` AS 
SELECT
    `engine_id`,
    e.`name` AS `engine_name`,
    e.`host` AS `host`,
    e.`port` AS `port`,
    e.`desc` AS `engine_desc`,
    `service_id`,
    s.`name` AS `service_name`
FROM 
    `services` s INNER JOIN `service_map` ON `service_id` = s.`id`
    INNER JOIN `engines` e ON e.`id` = `engine_id`
ORDER BY `engine_id`, `service_id` ASC;
COMMIT;
