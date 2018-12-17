package com.mycorp.util;

import com.mycorp.soporte.DatosAltaAsegurados;
import es.sanitas.seg.simulacionpoliza.services.api.simulacion.vo.InfoTier;
import es.sanitas.seg.simulacionpoliza.services.api.simulacion.vo.TierProducto;
import org.apache.commons.lang3.StringUtils;
import wscontratacion.contratacion.fuentes.parametros.DatosAlta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Tier implements ITier {

    private static final String SEPARADOR_TIER = "#";

    public Tier() {
    }

    @Override
    public InfoTier obtenerTier(final DatosAlta oDatosAlta ) {
        InfoTier infoTier = null;
        if( oDatosAlta instanceof DatosAltaAsegurados) {
            final DatosAltaAsegurados oDatosAltaAsegurados = (DatosAltaAsegurados)oDatosAlta;
            final String coeficientesTier = oDatosAltaAsegurados.getCoeficientesTier();
            if( !StringUtils.isEmpty( coeficientesTier ) ) {
                final List< String > productos = Arrays.asList( "producto-1", "producto-5", "producto-3" );
                final String[] st = coeficientesTier.split( SEPARADOR_TIER );

                infoTier = new InfoTier();
                final List<TierProducto> tierProductos = new ArrayList< >();
                int i = 1;
                for( final String idProducto : productos ) {
                    final TierProducto tier = new TierProducto();
                    tier.setIdProducto( Integer.valueOf( idProducto ) );
                    tier.setValor( Double.valueOf( st[ i++ ] ) );
                    tierProductos.add( tier );
                }

                infoTier.setListaTierProductos( tierProductos.toArray( new TierProducto[ 0 ] ) );
                infoTier.setTierGlobal( Double.valueOf( st[ st.length - 1 ] ).intValue() );
            }
        }
        return infoTier;
    }
}
