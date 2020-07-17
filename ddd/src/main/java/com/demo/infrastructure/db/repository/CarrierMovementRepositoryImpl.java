package com.demo.infrastructure.db.repository;

import com.demo.domain.aggregate.carriermovement.CarrierMovement;
import com.demo.domain.aggregate.carriermovement.CarrierMovementRepository;
import com.demo.infrastructure.db.converter.CarrierMovementConverter;
import com.demo.infrastructure.db.dataobject.CarrierMovementDO;
import com.demo.infrastructure.db.mapper.CarrierMovementMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CarrierMovementRepositoryImpl implements CarrierMovementRepository {

    @Autowired
    private CarrierMovementMapper mapper;

    @Override
    public CarrierMovement find(String id) {
        CarrierMovementDO carrierMovementDO = mapper.select(id);
        return CarrierMovementConverter.deserialize(carrierMovementDO);
    }

}
