package com.wzp.cloud.support.application.query;

import com.wzp.cloud.support.application.query.dto.CarrierMovementDTO;
import com.wzp.cloud.support.infrastructure.db.dataobject.LocationDO;

import java.util.List;

public interface RoutingQueryService {

    List<CarrierMovementDTO> queryCarriers();
    List<LocationDO> queryLocations();

}
