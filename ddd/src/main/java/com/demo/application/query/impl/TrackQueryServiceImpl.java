package com.demo.application.query.impl;

import com.demo.application.query.TrackQueryService;
import com.demo.application.query.assembler.CargoDTOAssembler;
import com.demo.application.query.assembler.HandlingEventDTOAssembler;
import com.demo.application.query.dto.CargoDTO;
import com.demo.application.query.dto.CargoHandlingEventDTO;
import com.demo.application.query.dto.HandlingEventDTO;
import com.demo.application.query.qry.EventFindbyCargoQry;
import com.demo.infrastructure.db.dataobject.CargoDO;
import com.demo.infrastructure.db.dataobject.HandlingEventDO;
import com.demo.infrastructure.db.mapper.CargoMapper;
import com.demo.infrastructure.db.mapper.HandlingEventMapper;
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
