package com.demo.infrastructure.db.repository;

import com.demo.domain.aggregate.location.Location;
import com.demo.domain.aggregate.location.LocationRepository;
import com.demo.infrastructure.db.converter.LocationConverter;
import com.demo.infrastructure.db.dataobject.LocationDO;
import com.demo.infrastructure.db.mapper.LocationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LocationRepositoryImpl implements LocationRepository{
    
    @Autowired
    private LocationMapper mapper;

    @Override
    public Location find(String code) {
        LocationDO locationDO = mapper.select(code);
        return LocationConverter.deserialize(locationDO);
    }

}
