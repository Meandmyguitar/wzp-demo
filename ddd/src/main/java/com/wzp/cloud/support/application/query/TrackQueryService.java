package com.wzp.cloud.support.application.query;

import com.wzp.cloud.support.application.query.dto.CargoHandlingEventDTO;
import com.wzp.cloud.support.application.query.qry.EventFindbyCargoQry;

public interface TrackQueryService {
    
    CargoHandlingEventDTO queryHistory(EventFindbyCargoQry qry);


}
