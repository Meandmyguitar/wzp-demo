package com.wzp.cloud.support.application.command;


import com.wzp.cloud.support.application.command.cmd.CargoBookCommand;
import com.wzp.cloud.support.application.command.cmd.CargoDeleteCommand;
import com.wzp.cloud.support.application.command.cmd.CargoDeliveryUpdateCommand;
import com.wzp.cloud.support.application.command.cmd.CargoSenderUpdateCommand;

public interface CargoCmdService {
    
    void bookCargo(CargoBookCommand cargoBookCommand);

    void updateCargoDelivery(CargoDeliveryUpdateCommand cmd);

    void deleteCargo(CargoDeleteCommand cmd);

    void updateCargoSender(CargoSenderUpdateCommand cmd);

}
