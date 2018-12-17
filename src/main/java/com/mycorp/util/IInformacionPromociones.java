package com.mycorp.util;

import es.sanitas.seg.simulacionpoliza.services.api.simulacion.vo.InfoPromociones;
import wscontratacion.contratacion.fuentes.parametros.DatosAlta;

public interface IInformacionPromociones {

    InfoPromociones obtenerInfoPromociones(final DatosAlta oDatosAlta);
}
