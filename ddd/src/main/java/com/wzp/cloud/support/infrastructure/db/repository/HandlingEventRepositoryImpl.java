package com.wzp.cloud.support.infrastructure.db.repository;

import com.wzp.cloud.support.domain.aggregate.handlingevent.HandlingEvent;
import com.wzp.cloud.support.domain.aggregate.handlingevent.HandlingEventRepository;
import com.wzp.cloud.support.infrastructure.db.converter.HandlingEventConverter;
import com.wzp.cloud.support.infrastructure.db.dataobject.HandlingEventDO;
import com.wzp.cloud.support.infrastructure.db.mapper.HandlingEventMapper;
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
