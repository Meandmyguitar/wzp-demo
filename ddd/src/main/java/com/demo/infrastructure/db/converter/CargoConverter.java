package com.demo.infrastructure.db.converter;

import com.demo.domain.aggregate.cargo.Cargo;
import com.demo.domain.aggregate.cargo.valueobject.DeliverySpecification;
import com.demo.infrastructure.db.dataobject.CargoDO;

public class CargoConverter {

    public static CargoDO serialize(Cargo cargo) {
        CargoDO target = new CargoDO();
        target.setId(cargo.id());
        target.setSenderPhone(cargo.sender());
        target.setDescription(cargo.description());
        DeliverySpecification delivery = cargo.delivery();
        target.setDestinationLocationCode(delivery.getDestinationLocationCode());
        target.setOriginLocationCode(delivery.getOriginLocationCode());
        return target;
    }

    public static Cargo deserialize(CargoDO cargo) {
        Cargo target = Cargo.newCargo(cargo.getId(), cargo.getSenderPhone(), cargo.getDescription(),
                new DeliverySpecification(cargo.getOriginLocationCode(),
                        cargo.getDestinationLocationCode()));
        return target;
    }

}
