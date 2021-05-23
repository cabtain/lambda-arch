package com.sds.iot.dao;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import com.sds.iot.dao.entity.POITrafficData;

import java.util.UUID;

/**
 * DAO class for poi_traffic
 *
 */
@Repository
public interface POITrafficDataRepository extends CassandraRepository<POITrafficData,UUID>{
	 
}
