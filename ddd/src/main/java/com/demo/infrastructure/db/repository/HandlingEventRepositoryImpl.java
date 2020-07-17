package com.demo.infrastructure.db.repository;

import com.demo.domain.aggregate.handlingevent.HandlingEvent;
import com.demo.domain.aggregate.handlingevent.HandlingEventRepository;
import com.demo.infrastructure.db.converter.HandlingEventConverter;
import com.demo.infrastructure.db.dataobject.HandlingEventDO;
import com.demo.infrastructure.db.mapper.HandlingEventMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class HandlingEventRepositoryImpl implements HandlingEventRepository {

    @Autowired
    private HandlingEventMapper mapper;

    @Override
    public List<HandlingEvent> findByCargo(String cargoId) {
        List<HandlingEventDO> handlingEventDOs = mapper.selectByCargo(cargoId);

        return handlingEventDOs.stream().map(HandlingEventConverter::deserialize)
                .collect(Collectors.toList());
    }

    @Override
    public List<HandlingEvent> findByScheduleId(String scheduleId) {
        List<HandlingEventDO> handlingEventDOs = mapper.selectByScheduleId(scheduleId);

        return handlingEventDOs.stream().map(HandlingEventConverter::deserialize)
                .collect(Collectors.toList());
    }

    @Override
    public void save(HandlingEvent handlingEvent) {
        HandlingEventDO handlingEventDO = HandlingEventConverter.serialize(handlingEvent);
        mapper.save(handlingEventDO);
    }

}
