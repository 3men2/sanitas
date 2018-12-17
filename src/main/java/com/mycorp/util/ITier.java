package com.mycorp.util;

import es.sanitas.seg.simulacionpoliza.services.api.simulacion.vo.InfoTier;
import wscontratacion.contratacion.fuentes.parametros.DatosAlta;

public interface ITier {

    InfoTier obtenerTier(final DatosAlta oDatosAlta);
}
