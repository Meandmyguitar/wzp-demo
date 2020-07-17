package com.demo.application.command;


import com.demo.application.command.cmd.HandlingEventAddCommand;

public interface IncidentLoggingCmdService {

    void addHandlingEvent(HandlingEventAddCommand cmd);

}
