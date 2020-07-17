package com.demo.application.query;

import com.demo.application.query.dto.CargoDTO;
import com.demo.application.query.qry.CargoFindbyCustomerQry;

import java.util.List;

public interface CargoQueryService {

    List<CargoDTO> queryCargos();

    List<CargoDTO> queryCargos(CargoFindbyCustomerQry qry);

    CargoDTO getCargo(String cargoId);

}
