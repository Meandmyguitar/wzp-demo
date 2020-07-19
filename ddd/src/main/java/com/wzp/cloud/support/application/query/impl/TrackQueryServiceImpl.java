package com.wzp.cloud.support.application.query.impl;

import com.wzp.cloud.support.application.query.TrackQueryService;
import com.wzp.cloud.support.application.query.assembler.CargoDTOAssembler;
import com.wzp.cloud.support.application.query.assembler.HandlingEventDTOAssembler;
import com.wzp.cloud.support.application.query.dto.CargoDTO;
import com.wzp.cloud.support.application.query.dto.CargoHandlingEventDTO;
import com.wzp.cloud.support.application.query.dto.HandlingEventDTO;
import com.wzp.cloud.support.application.query.qry.EventFindbyCargoQry;
import com.wzp.cloud.support.infrastructure.db.dataobject.CargoDO;
import com.wzp.cloud.support.infrastructure.db.dataobject.HandlingEventDO;
import com.wzp.cloud.support.infrastructure.db.mapper.CargoMapper;
import com.wzp.cloud.support.infrastructure.db.mapper.HandlingEventMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrackQueryServiceImpl implements TrackQueryService {

    @Autowired
    private HandlingEventMapper handlingEventMapper;

    @Autowired
    private CargoMapper cargoMapper;

    @Autowired
    private CargoDTOAssembler converter;
    @Autowired
    private HandlingEventDTOAssembler handlingEventDTOAssembler;

    @Override
    public CargoHandlingEventDTO queryHistory(EventFindbyCargoQry qry) {

        CargoDO cargo = cargoMapper.select(qry.getCargoId());
        List<HandlingEventDO> events = handlingEventMapper.selectByCargo(qry.getCargoId());

        // convertor
        CargoDTO cargoDTO = converter.apply(cargo);
        List<HandlingEventDTO> dtoEvents = events.stream().map(handlingEventDTOAssembler::apply)
                .collect(Collectors.toList());

        CargoHandlingEventDTO cargoHandlingEventDTO = new CargoHandlingEventDTO();
        cargoHandlingEventDTO.setCargo(cargoDTO);
        cargoHandlingEventDTO.setEvents(dtoEvents);

        return cargoHandlingEventDTO;
    }

}
