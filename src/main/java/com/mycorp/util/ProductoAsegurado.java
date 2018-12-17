package com.mycorp.util;

import es.sanitas.bravo.ws.stubs.contratacionws.consultasoperaciones.DatosContratacionPlan;
import es.sanitas.seg.simulacionpoliza.services.api.simulacion.vo.Producto;
import org.springframework.beans.factory.annotation.Autowired;
import wscontratacion.contratacion.fuentes.parametros.DatosProductoAlta;

import java.util.ArrayList;
import java.util.List;

public class ProductoAsegurado implements IProductoAsegurado {

    @Autowired
    IProducto producto;

    public ProductoAsegurado() {
    }

    @Override
    public Producto[] obtenerProductosAsegurado(final List< DatosProductoAlta > productosCobertura,
                                                 final DatosContratacionPlan oDatosPlan ) {
        final List< Producto > productos = new ArrayList< >();
        if( productosCobertura != null && !productosCobertura.isEmpty() ) {
            for( final DatosProductoAlta productoCobertura : productosCobertura ) {
                productos.add( producto.obtenerProducto( productoCobertura, oDatosPlan ) );
            }
        }

        return productos.toArray( new Producto[ 0 ] );
    }
}
