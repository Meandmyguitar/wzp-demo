package com.wzp.cloud.support.infrastructure.rpc.salessystem;

import com.wzp.cloud.support.infrastructure.rpc.salessystem.dataobject.UserDO;
import org.springframework.stereotype.Component;

@Component
public class RemoteServiceTranslator {

    public UserDO toUserDO(Object obj) {
        return new UserDO();
    }

}
