package com.mycorp.util;

import com.mycorp.soporte.BeneficiarioPolizas;
import com.mycorp.soporte.FrecuenciaEnum;
import com.mycorp.soporte.StaticVarsContratacion;
import wscontratacion.contratacion.fuentes.parametros.DatosAlta;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FrecuenciaTarificar implements IFrecuenciaTarificar {

    public FrecuenciaTarificar() {}

    @Override
    public Set<FrecuenciaEnum> getFrecuenciaTarificar(
            DatosAlta oDatosAlta,
            List<BeneficiarioPolizas> lBeneficiarios,
            Map<String, Object> hmValores) {
        Set<FrecuenciaEnum> frecuenciasTarificar = EnumSet.noneOf( FrecuenciaEnum.class );
        if( hmValores.containsKey( StaticVarsContratacion.FREC_MENSUAL ) ) {
            frecuenciasTarificar.clear();
            frecuenciasTarificar.add( FrecuenciaEnum.MENSUAL );
        }
        if( lBeneficiarios != null ) {
            frecuenciasTarificar.clear();
            frecuenciasTarificar
                    .add( FrecuenciaEnum.obtenerFrecuencia( oDatosAlta.getGenFrecuenciaPago() ) );
        }
        if( frecuenciasTarificar.isEmpty() ) {
            frecuenciasTarificar = EnumSet.allOf( FrecuenciaEnum.class );
        }
        return frecuenciasTarificar;
    }
}
