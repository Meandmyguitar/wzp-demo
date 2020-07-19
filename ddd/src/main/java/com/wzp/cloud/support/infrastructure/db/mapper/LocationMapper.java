package com.wzp.cloud.support.infrastructure.db.mapper;

import com.wzp.cloud.support.infrastructure.db.dataobject.LocationDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LocationMapper {

    LocationDO select(@Param("code") String code);

    List<LocationDO> selectAll();


}
