package com.wzp.cloud.support.application.query.assembler;

import com.wzp.cloud.support.application.query.dto.CarrierMovementDTO;
import com.wzp.cloud.support.application.query.dto.HandlingEventDTO;
import com.wzp.cloud.support.domain.aggregate.handlingevent.EventTypeEnum;
import com.wzp.cloud.support.infrastructure.db.dataobject.CarrierMovementDO;
import com.wzp.cloud.support.infrastructure.db.dataobject.HandlingEventDO;
import com.wzp.cloud.support.infrastructure.db.mapper.CarrierMovementMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.function.Function;

@Component
public class HandlingEventDTOAssembler implements Function<HandlingEventDO, HandlingEventDTO> {

    @Autowired
    private CarrierMovementMapper carrierMovementMapper;
    @Autowired
    private CarrierMovementDTOAssembler converter;

    public HandlingEventDTO apply(HandlingEventDO t) {
        HandlingEventDTO target = new HandlingEventDTO();
        target.setEventType(EventTypeEnum.of(t.getEventType()).toString());
        target.setDatetime(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(t.getDatetime()));

        if (!StringUtils.isEmpty(t.getScheduleId())) {
            CarrierMovementDO carrierMovementDO = carrierMovementMapper.select(t.getScheduleId());
            CarrierMovementDTO carrierMovement = converter.apply(carrierMovementDO);
            target.setCarrierMovement(carrierMovement);
        }
        return target;
    }

}
