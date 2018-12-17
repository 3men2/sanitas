package com.mycorp.util;

import com.mycorp.soporte.PromocionAplicada;

import java.util.List;

public interface IObtenerPromociones {

    public List<PromocionAplicada> recuperarPromocionesAgrupadas(
            final es.sanitas.seg.simulacionpoliza.services.api.simulacion.vo.Promocion[] promociones,
            final int numeroAsegurados);
}
