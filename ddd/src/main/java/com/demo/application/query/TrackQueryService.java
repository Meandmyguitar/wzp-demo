package com.demo.application.query;

import com.demo.application.query.dto.CargoHandlingEventDTO;
import com.demo.application.query.qry.EventFindbyCargoQry;

public interface TrackQueryService {
    
    CargoHandlingEventDTO queryHistory(EventFindbyCargoQry qry);


}
