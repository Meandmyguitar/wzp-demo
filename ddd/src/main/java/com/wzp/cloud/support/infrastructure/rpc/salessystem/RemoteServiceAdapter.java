package com.wzp.cloud.support.infrastructure.rpc.salessystem;

import com.wzp.cloud.support.domain.aggregate.cargo.Cargo;
import com.wzp.cloud.support.domain.aggregate.cargo.valueobject.EnterpriseSegment;
import com.wzp.cloud.support.infrastructure.rpc.salessystem.dataobject.UserDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RemoteServiceAdapter {

    @Autowired
    private RemoteServiceTranslator translator;

    // @Autowired
    // remoteService

    public UserDO getUser(String phone) {
        // User user = remoteService.getUser(phone);
        // return this.translator.toUserDO(user);
        return null;
    }

    public EnterpriseSegment deriveEnterpriseSegment(Cargo cargo) {
        // remote service
        // translator
        return EnterpriseSegment.FRUIT;
    }

    public boolean mayAccept(int cargoSize, Cargo cargo) {
        // remote service
        // translator
        return true;
    }

}
