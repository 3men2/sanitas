package com.mycorp.util;

import com.mycorp.soporte.BeneficiarioPolizas;
import com.mycorp.soporte.ProductoPolizas;
import es.sanitas.bravo.ws.stubs.contratacionws.consultasoperaciones.DatosContratacionPlan;
import wscontratacion.contratacion.fuentes.parametros.DatosAlta;

import java.util.List;

public interface IBeneficiarios {

    public es.sanitas.seg.simulacionpoliza.services.api.simulacion.vo.Beneficiario[] obtenerBeneficiarios(
            final DatosAlta oDatosAlta, final List<ProductoPolizas> lProductos,
            final List<BeneficiarioPolizas> lBeneficiarios, final DatosContratacionPlan oDatosPlan);
}
