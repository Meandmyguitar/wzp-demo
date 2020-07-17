package com.demo.application.query;

import com.demo.application.query.dto.CarrierMovementDTO;
import com.demo.infrastructure.db.dataobject.LocationDO;

import java.util.List;

public interface RoutingQueryService {

    List<CarrierMovementDTO> queryCarriers();
    List<LocationDO> queryLocations();

}
