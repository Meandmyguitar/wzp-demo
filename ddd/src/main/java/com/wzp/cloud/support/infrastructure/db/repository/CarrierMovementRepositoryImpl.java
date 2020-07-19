package com.wzp.cloud.support.infrastructure.db.repository;

import com.wzp.cloud.support.domain.aggregate.carriermovement.CarrierMovement;
import com.wzp.cloud.support.domain.aggregate.carriermovement.CarrierMovementRepository;
import com.wzp.cloud.support.infrastructure.db.converter.CarrierMovementConverter;
import com.wzp.cloud.support.infrastructure.db.dataobject.CarrierMovementDO;
import com.wzp.cloud.support.infrastructure.db.mapper.CarrierMovementMapper;
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
