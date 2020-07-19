package com.wzp.cloud.support.application.command.impl;

import com.wzp.cloud.support.application.command.IncidentLoggingCmdService;
import com.wzp.cloud.support.application.command.cmd.HandlingEventAddCommand;
import com.wzp.cloud.support.domain.aggregate.handlingevent.EventTypeEnum;
import com.wzp.cloud.support.domain.aggregate.handlingevent.HandlingEvent;
import com.wzp.cloud.support.domain.aggregate.handlingevent.HandlingEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IncidentLoggingCmdServiceImpl implements IncidentLoggingCmdService {

    @Autowired
    private HandlingEventRepository handlingEventRepository;

    @Override
    public void addHandlingEvent(HandlingEventAddCommand cmd) {
        // validate

        // create
        HandlingEvent handlingEvent = HandlingEvent.newHandlingEvent(cmd.getCargoId(),
                cmd.getDatetime(), EventTypeEnum.of(cmd.getEventType()), cmd.getScheduleId());

        // save
        handlingEventRepository.save(handlingEvent);

    }

}
