package com.mycorp.util;

import com.mycorp.soporte.BeneficiarioPolizas;
import com.mycorp.soporte.FrecuenciaEnum;
import wscontratacion.contratacion.fuentes.parametros.DatosAlta;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IFrecuenciaTarificar {

    Set<FrecuenciaEnum> getFrecuenciaTarificar(
            final DatosAlta oDatosAlta,
            final List<BeneficiarioPolizas> lBeneficiarios,
            final Map<String, Object> hmValores);
}
