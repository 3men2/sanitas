package com.mycorp.util;

import es.sanitas.bravo.ws.stubs.contratacionws.consultasoperaciones.DatosContratacionPlan;
import es.sanitas.seg.simulacionpoliza.services.api.simulacion.vo.Cobertura;

public interface ICoberturas {

    Cobertura[] obtenerCoberturas(final int idProducto, final DatosContratacionPlan oDatosPlan);
}
