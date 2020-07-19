package com.wzp.cloud.support.application.command;


import com.wzp.cloud.support.application.command.cmd.HandlingEventAddCommand;

public interface IncidentLoggingCmdService {

    void addHandlingEvent(HandlingEventAddCommand cmd);

}
