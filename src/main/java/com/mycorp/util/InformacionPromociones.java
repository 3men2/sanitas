package com.mycorp.util;

import com.mycorp.soporte.DatosAltaAsegurados;
import com.mycorp.soporte.StaticVarsContratacion;
import es.sanitas.seg.simulacionpoliza.services.api.simulacion.vo.InfoPromociones;
import es.sanitas.seg.simulacionpoliza.services.api.simulacion.vo.Promocion;
import wscontratacion.contratacion.fuentes.parametros.DatosAlta;

public class InformacionPromociones implements IInformacionPromociones {

    public InformacionPromociones() {
    }

    @Override
    public InfoPromociones obtenerInfoPromociones(final DatosAlta oDatosAlta ) {
        InfoPromociones infoPromociones = null;
        if( oDatosAlta instanceof DatosAltaAsegurados) {
            final DatosAltaAsegurados oDatosAltaAsegurados = (DatosAltaAsegurados)oDatosAlta;
            infoPromociones = new InfoPromociones();
            infoPromociones
                    .setAutomaticas( StaticVarsContratacion.SIMULACION_PROMOCIONES_AUTOMATICAS );
            // Si no se ha introducido un código promocional se debe enviar
            // de cero elementos
            Promocion[] promociones = new Promocion[ 0 ];
            final String codigoPromocion = oDatosAltaAsegurados.getCodigoPromocional();
            if( codigoPromocion != null ) {
                promociones = new Promocion[ 1 ];
                final Promocion promocion = new Promocion();
                promocion.setIdPromocion( codigoPromocion );
                promociones[ 0 ] = promocion;
            }
            infoPromociones.setListaPromociones( promociones );
        }
        return infoPromociones;
    }
}
