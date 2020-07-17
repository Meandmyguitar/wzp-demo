package com.demo.infrastructure.db.converter;

import com.demo.domain.aggregate.handlingevent.EventTypeEnum;
import com.demo.domain.aggregate.handlingevent.HandlingEvent;
import com.demo.infrastructure.db.dataobject.HandlingEventDO;

public class HandlingEventConverter {

    public static HandlingEventDO serialize(HandlingEvent location) {
        HandlingEventDO target = new HandlingEventDO();
        target.setId(location.id());
        target.setCargoId(location.cargoId());
        target.setDatetime(location.datetime());
        target.setScheduleId(location.scheduleId());
        target.setEventType(location.eventType().index());
        return target;
    }

    public static HandlingEvent deserialize(HandlingEventDO locationDO) {
        HandlingEvent target = HandlingEvent.newHandlingEvent(locationDO.getId(),
                locationDO.getCargoId(), locationDO.getDatetime(),
                EventTypeEnum.of(locationDO.getEventType()), locationDO.getScheduleId());
        return target;
    }

}