package com.mycorp.util;

import com.mycorp.soporte.PromocionAplicada;
import com.mycorp.soporte.TipoPromocionEnum;
import es.sanitas.seg.simulacionpoliza.services.api.simulacion.vo.Promocion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ObtenerPromociones implements IObtenerPromociones {

    private static final Logger LOG = LoggerFactory.getLogger( ObtenerPromociones.class );

    public ObtenerPromociones() {}

    @Override
    public List<PromocionAplicada> recuperarPromocionesAgrupadas(
            final Promocion[] promociones,
            final int numeroAsegurados ) {

        List<PromocionAplicada> promocionesAgrupadas = new ArrayList< >();
        if( promociones != null && promociones.length > 0 ) {
            LOG.debug( promociones.toString() );
            final int numPromociones = promociones.length / numeroAsegurados;
            promocionesAgrupadas = toPromocionAplicadaList( Arrays.copyOfRange( promociones, 0, numPromociones ) );
        }
        return promocionesAgrupadas;
    }

    private List<PromocionAplicada> toPromocionAplicadaList(final Promocion[] promociones ) {
        final List<PromocionAplicada> promocionesParam = new ArrayList< >();

        for( final Promocion promocion : promociones ) {
            final PromocionAplicada promocionParam = toPromocionAplicada( promocion );
            if( promocionParam != null ) {
                promocionesParam.add( promocionParam );
            }
        }
        return promocionesParam;
    }

    private PromocionAplicada toPromocionAplicada(final Promocion promocion ) {
        PromocionAplicada promocionParam = null;
        if( promocion != null ) {
            promocionParam = new PromocionAplicada();
            promocionParam.setIdPromocion( promocion.getIdPromocion() != null ? Long.valueOf( promocion.getIdPromocion() ) : null );
            promocionParam.setDescripcion( promocion.getDescripcion() );
            promocionParam.setTipoPromocion( TipoPromocionEnum.obtenerTipoPromocion( promocion.getTipo() ) );
        }
        return promocionParam;
    }
}
