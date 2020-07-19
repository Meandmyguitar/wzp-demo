package com.wzp.cloud.support.domain.service;

import com.wzp.cloud.support.domain.aggregate.cargo.Cargo;
import com.wzp.cloud.support.domain.aggregate.handlingevent.HandlingEvent;
import com.wzp.cloud.support.infrastructure.rpc.salessystem.SalersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class CargoDomainService {

    public static final int MAX_CARGO_LIMIT = 10;
    public static final String PREFIX_ID = "CARGO-NO-";

    @Autowired
    private SalersService salersService;

    /**
     * 货物物流id生成规则
     * 
     * @return
     */
    public static String nextCargoId() {
        return PREFIX_ID + (10000 + new Random().nextInt(9999));
    }

    public void updateCargoSender(Cargo cargo, String senderPhone, HandlingEvent latestEvent) {

        if (null != latestEvent
                && !latestEvent.canModifyCargo()) { throw new IllegalArgumentException(
                        "Sender cannot be changed after RECIEVER Status."); }

        cargo.changeSender(senderPhone);
    }

    public boolean mayAccept(int size, int cargoSize, Cargo cargo) {
        return size <= MAX_CARGO_LIMIT && salersService.mayAccept(cargoSize, cargo);
    }

}
