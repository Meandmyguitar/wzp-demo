package com.wzp.cloud.support.application.query.impl;

import com.wzp.cloud.support.application.query.CargoQueryService;
import com.wzp.cloud.support.application.query.assembler.CargoDTOAssembler;
import com.wzp.cloud.support.application.query.dto.CargoDTO;
import com.wzp.cloud.support.application.query.qry.CargoFindbyCustomerQry;
import com.wzp.cloud.support.infrastructure.db.dataobject.CargoDO;
import com.wzp.cloud.support.infrastructure.db.mapper.CargoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CargoQueryServiceImpl implements CargoQueryService {

    @Autowired
    private CargoMapper cargoMapper;
    
    @Autowired
    private CargoDTOAssembler converter;

    @Override
    public List<CargoDTO> queryCargos() {
        List<CargoDO> cargos = cargoMapper.selectAll();
        return cargos.stream().map(converter::apply).collect(Collectors.toList());
    }

    @Override
    public List<CargoDTO> queryCargos(CargoFindbyCustomerQry qry) {
        List<CargoDO> cargos = cargoMapper.selectByCustomer(qry.getCustomerPhone());
        return cargos.stream().map(converter::apply).collect(Collectors.toList());
    }

    @Override
    public CargoDTO getCargo(String cargoId) {
        CargoDO select = cargoMapper.select(cargoId);
        return converter.apply(select);
    }

}
