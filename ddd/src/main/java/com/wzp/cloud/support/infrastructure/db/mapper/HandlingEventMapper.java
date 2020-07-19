package com.wzp.cloud.support.infrastructure.db.mapper;

import com.wzp.cloud.support.infrastructure.db.dataobject.HandlingEventDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HandlingEventMapper {

    List<HandlingEventDO> selectByCargo(@Param("cargoId") String cargoId);

    List<HandlingEventDO> selectByScheduleId(@Param("scheduleId") String scheduleId);

    void save(HandlingEventDO handlingEventDO);

}
