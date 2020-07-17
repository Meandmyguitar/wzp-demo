package com.demo.application.command.impl;

import com.demo.application.command.IncidentLoggingCmdService;
import com.demo.application.command.cmd.HandlingEventAddCommand;
import com.demo.domain.aggregate.handlingevent.EventTypeEnum;
import com.demo.domain.aggregate.handlingevent.HandlingEvent;
import com.demo.domain.aggregate.handlingevent.HandlingEventRepository;
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
