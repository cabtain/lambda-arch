//Create keyspace
CREATE KEYSPACE IF NOT EXISTS EquipmentKeySpace WITH replication = {'class':'SimpleStrategy', 'replication_factor':1};

//Drop table
DROP TABLE IF EXISTS EquipmentKeySpace.Total_Equipment;
DROP TABLE IF EXISTS EquipmentKeySpace.Window_Equipment;
DROP TABLE IF EXISTS EquipmentKeySpace.Total_Equipment_batch;
DROP TABLE IF EXISTS EquipmentKeySpace.Window_Equipment_batch;

//Create table
CREATE TABLE EquipmentKeySpace.Total_Equipment (routeId text ,  vehicleType text,  totalCount bigint,  totalSum bigint, timeStamp timestamp,recordDate text,PRIMARY KEY (routeId,recordDate,vehicleType));
CREATE TABLE EquipmentKeySpace.Window_Equipment (routeId text , vehicleType text,  totalCount bigint,  totalSum bigint, timeStamp timestamp,recordDate text,PRIMARY KEY (routeId,recordDate,vehicleType));
CREATE TABLE EquipmentKeySpace.Total_Equipment_batch (routeId text ,  vehicleType text,  totalCount bigint, timeStamp timestamp,recordDate text,PRIMARY KEY (routeId,recordDate,vehicleType));
CREATE TABLE EquipmentKeySpace.Window_Equipment_batch (routeId text , vehicleType text,  totalCount bigint, timeStamp timestamp,recordDate text,PRIMARY KEY (routeId,recordDate,vehicleType));
