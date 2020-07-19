package com.wzp.cloud.support.infrastructure.db.repository;

import com.wzp.cloud.support.domain.aggregate.location.Location;
import com.wzp.cloud.support.domain.aggregate.location.LocationRepository;
import com.wzp.cloud.support.infrastructure.db.converter.LocationConverter;
import com.wzp.cloud.support.infrastructure.db.dataobject.LocationDO;
import com.wzp.cloud.support.infrastructure.db.mapper.LocationMapper;
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
