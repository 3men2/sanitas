package com.mycorp.util;

import com.mycorp.soporte.Recibo;
import es.sanitas.seg.simulacionpoliza.services.api.simulacion.vo.ReciboProducto;

import java.util.List;

public interface IRecibos {

    List<Recibo> toReciboList(final ReciboProducto[] recibos);
}
