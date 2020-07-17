package com.demo.controller;

import com.demo.application.query.RoutingQueryService;
import com.demo.application.query.dto.CarrierMovementDTO;
import com.demo.infrastructure.db.dataobject.LocationDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/routing")
public class RoutingController {

    @Autowired
    private RoutingQueryService routingQueryService;

    @RequestMapping(value = "/carrier", method = RequestMethod.GET)
    public List<CarrierMovementDTO> carriers() {
        return routingQueryService.queryCarriers();
    }

    @RequestMapping(value = "/location", method = RequestMethod.GET)
    public List<LocationDO> locations() {
        return routingQueryService.queryLocations();
    }

}
