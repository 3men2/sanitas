package com.mycorp.util;

import com.mycorp.soporte.BeneficiarioPolizas;
import com.mycorp.soporte.FrecuenciaEnum;
import com.mycorp.soporte.ProductoPolizas;
import com.mycorp.soporte.TarificacionPoliza;
import wscontratacion.contratacion.fuentes.parametros.DatosAlta;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public interface ISolver {

    public Callable<TarificacionPoliza> simularPolizaFrecuencia(
            final Map<String, Object> hmValores, final DatosAlta oDatosAlta,
            final List<ProductoPolizas> lProductos,
            final List<BeneficiarioPolizas> lBeneficiarios, final FrecuenciaEnum frecuencia);
}
