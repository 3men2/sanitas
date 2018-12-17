package com.mycorp.util;

import es.sanitas.bravo.ws.stubs.contratacionws.consultasoperaciones.DatosCobertura;
import es.sanitas.bravo.ws.stubs.contratacionws.consultasoperaciones.DatosContratacionPlan;
import es.sanitas.bravo.ws.stubs.contratacionws.consultasoperaciones.DatosPlanProducto;
import es.sanitas.seg.simulacionpoliza.services.api.simulacion.vo.Cobertura;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Coberturas implements ICoberturas {

    public Coberturas() {
    }

    @Override
    public Cobertura[] obtenerCoberturas(final int idProducto, final DatosContratacionPlan oDatosPlan ) {
        final List< Cobertura > coberturas = new ArrayList< >();

        final Iterator< DatosPlanProducto > iteradorProdsPlan = oDatosPlan.getProductos().iterator();
        boolean found = false;
        while( iteradorProdsPlan.hasNext() && !found ) {
            final DatosPlanProducto productoPlan = iteradorProdsPlan.next();
            if( idProducto == productoPlan.getIdProducto() ) {
                found = true;
                for( final DatosCobertura oDatosCobertura : productoPlan.getCoberturas() ) {
                    if( oDatosCobertura.isSwObligatorio()
                            && oDatosCobertura.getCapitalMinimo() != null
                            && oDatosCobertura.getCapitalMinimo() > 0 ) {
                        final Cobertura cobertura = new Cobertura();
                        cobertura
                                .setCapital( Double.valueOf( oDatosCobertura.getCapitalMinimo() ) );
                        cobertura.setIdCobertura( oDatosCobertura.getIdCobertura().intValue() );
                        coberturas.add( cobertura );
                    }
                }
            }
        }

        return coberturas.toArray( new Cobertura[ 0 ] );
    }
}
