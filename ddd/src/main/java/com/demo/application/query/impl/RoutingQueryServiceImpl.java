package com.demo.application.query.impl;

import com.demo.application.query.RoutingQueryService;
import com.demo.application.query.assembler.CarrierMovementDTOAssembler;
import com.demo.application.query.dto.CarrierMovementDTO;
import com.demo.infrastructure.db.dataobject.CarrierMovementDO;
import com.demo.infrastructure.db.dataobject.LocationDO;
import com.demo.infrastructure.db.mapper.CarrierMovementMapper;
import com.demo.infrastructure.db.mapper.LocationMapper;
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
