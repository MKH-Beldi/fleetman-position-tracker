package com.virtualpairprogrammers.tracker.data;

import com.virtualpairprogrammers.tracker.domain.VehiclePosition;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface PositionRepository extends MongoRepository<VehiclePosition, String> {
	List<VehiclePosition> findByNameAndTimestampAfter(String name, Date timestamp);
	Collection<VehiclePosition> findByTimestampAfter(Date since);
}
