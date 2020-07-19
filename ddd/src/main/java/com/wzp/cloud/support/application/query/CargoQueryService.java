package com.wzp.cloud.support.application.query;

import com.wzp.cloud.support.application.query.dto.CargoDTO;
import com.wzp.cloud.support.application.query.qry.CargoFindbyCustomerQry;

import java.util.List;

public interface CargoQueryService {

    List<CargoDTO> queryCargos();

    List<CargoDTO> queryCargos(CargoFindbyCustomerQry qry);

    CargoDTO getCargo(String cargoId);

}
