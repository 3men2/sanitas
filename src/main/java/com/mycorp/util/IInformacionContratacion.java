package com.mycorp.util;

import com.mycorp.soporte.BeneficiarioPolizas;
import com.mycorp.soporte.FrecuenciaEnum;
import com.mycorp.soporte.ProductoPolizas;
import es.sanitas.seg.simulacionpoliza.services.api.simulacion.vo.InfoContratacion;
import wscontratacion.contratacion.fuentes.parametros.DatosAlta;

import java.util.List;

public interface IInformacionContratacion {

    InfoContratacion obtenerInfoContratacion(final DatosAlta oDatosAlta, final List<BeneficiarioPolizas>
            lBeneficiarios,
                                             final List<ProductoPolizas> lProductos, final FrecuenciaEnum frecuencia,
                                             final Integer tipoOperacion);
}
