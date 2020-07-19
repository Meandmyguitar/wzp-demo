package com.wzp.cloud.support.application.query.impl;

import com.wzp.cloud.support.application.query.RoutingQueryService;
import com.wzp.cloud.support.application.query.assembler.CarrierMovementDTOAssembler;
import com.wzp.cloud.support.application.query.dto.CarrierMovementDTO;
import com.wzp.cloud.support.infrastructure.db.dataobject.CarrierMovementDO;
import com.wzp.cloud.support.infrastructure.db.dataobject.LocationDO;
import com.wzp.cloud.support.infrastructure.db.mapper.CarrierMovementMapper;
import com.wzp.cloud.support.infrastructure.db.mapper.LocationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoutingQueryServiceImpl implements RoutingQueryService {

    @Autowired
    private CarrierMovementMapper carrierMovementMapper;
    @Autowired
    private LocationMapper locationMapper;
    @Autowired
    private CarrierMovementDTOAssembler converter;

    @Override
    public List<CarrierMovementDTO> queryCarriers() {
        List<CarrierMovementDO> carrierMovementDOs = carrierMovementMapper.selectAll();
        return carrierMovementDOs.stream().map(converter::apply).collect(Collectors.toList());
    }

    @Override
    public List<LocationDO> queryLocations() {
        return locationMapper.selectAll();
    }

}
