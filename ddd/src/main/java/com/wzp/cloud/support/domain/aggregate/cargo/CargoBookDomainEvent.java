package com.wzp.cloud.support.domain.aggregate.cargo;

public class CargoBookDomainEvent {

    private Cargo cargo;

    public CargoBookDomainEvent(Cargo cargo) {
        this.cargo = cargo;
    }

    public Cargo getCargo() {
        return cargo;
    }

}
