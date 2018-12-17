package com.mycorp.util;

import es.sanitas.seg.simulacionpoliza.services.api.simulacion.vo.ReciboProducto;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class Recibos implements IRecibos {




    private final SimpleDateFormat sdf = new SimpleDateFormat( "dd/MM/yyyy" );

    @Override
    public List< com.mycorp.soporte.Recibo > toReciboList(final ReciboProducto[] recibos ) {
        final List< com.mycorp.soporte.Recibo > recibosList = new LinkedList< >();

        if( recibos != null ) {
            for( final ReciboProducto recibo : recibos ) {
                final com.mycorp.soporte.Recibo reciboParam = toRecibo( recibo );
                if( reciboParam != null ) {
                    recibosList.add( reciboParam );
                }
            }
        }
        return recibosList;
    }

    /**
     * Popula un objeto ReciboProviderOutParam con la simulación de un recibo.
     *
     * @param recibo datos del recibo
     * @return objeto ReciboProviderOutParam con la simulación de un recibo.
     */
    private com.mycorp.soporte.Recibo toRecibo( final ReciboProducto recibo ) {
        com.mycorp.soporte.Recibo reciboParam = null;
        if( recibo != null ) {
            reciboParam = new com.mycorp.soporte.Recibo();
            final Calendar fechaEmision = Calendar.getInstance();
            try {
                fechaEmision.setTime( sdf.parse( "25/12/2016" ) );
            } catch( final ParseException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            reciboParam.setFechaEmision( fechaEmision );
            reciboParam.setImporte( recibo.getIdProducto() * 1000. );
        }
        return reciboParam;
    }
}
