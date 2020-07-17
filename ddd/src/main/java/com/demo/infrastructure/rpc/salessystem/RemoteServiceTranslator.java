package com.demo.infrastructure.rpc.salessystem;

import com.demo.infrastructure.rpc.salessystem.dataobject.UserDO;
import org.springframework.stereotype.Component;

@Component
public class RemoteServiceTranslator {

    public UserDO toUserDO(Object obj) {
        return new UserDO();
    }

}
