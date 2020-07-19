package com.wzp.cloud.support.infrastructure.db.converter;

import com.wzp.cloud.support.domain.aggregate.location.Location;
import com.wzp.cloud.support.infrastructure.db.dataobject.LocationDO;
import org.springframework.beans.BeanUtils;

public class LocationConverter {

    public static LocationDO serialize(Location location) {
        LocationDO target = new LocationDO();
        BeanUtils.copyProperties(location, target);
        return target;
    }

    public static Location deserialize(LocationDO locationDO) {
        Location target = new Location();
        BeanUtils.copyProperties(locationDO, target);
        return target;
    }

}
