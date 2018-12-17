package com.mycorp.util;

import com.mycorp.soporte.BeneficiarioPolizas;
import com.mycorp.soporte.DatosAseguradoInclusion;
import com.mycorp.soporte.ProductoPolizas;
import es.sanitas.bravo.ws.stubs.contratacionws.consultasoperaciones.DatosContratacionPlan;
import es.sanitas.seg.simulacionpoliza.services.api.simulacion.vo.Producto;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import wscontratacion.contratacion.fuentes.parametros.DatosAlta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class Beneficiarios implements IBeneficiarios{
    private static final String DATE_FORMAT = "dd/MM/yyyy";

    @Autowired
    IProductoAsegurado productoAsegurado;
    @Autowired
    IProducto iProducto;

    public Beneficiarios() {
    }

    @Override
    public es.sanitas.seg.simulacionpoliza.services.api.simulacion.vo.Beneficiario[] obtenerBeneficiarios(
            final DatosAlta oDatosAlta, final List<ProductoPolizas> lProductos,
            final List<BeneficiarioPolizas> lBeneficiarios, final DatosContratacionPlan oDatosPlan ) {
        final List< es.sanitas.seg.simulacionpoliza.services.api.simulacion.vo.Beneficiario > beneficiarios = new ArrayList< >();

        // Si hay lista de beneficiarios se trata de una inclusion de beneficiarios
        if( lBeneficiarios != null && lBeneficiarios.size() > 0 ) {
            for( final BeneficiarioPolizas oBeneficiario : lBeneficiarios ) {
                final es.sanitas.seg.simulacionpoliza.services.api.simulacion.vo.Beneficiario beneficiario = new es.sanitas.seg.simulacionpoliza.services.api.simulacion.vo.Beneficiario();
                beneficiario.setFechaNacimiento(
                        cambiarFecha( oBeneficiario.getDatosPersonales().getFNacimiento(),
                                oDatosAlta.getFAlta() ) );
                beneficiario.setParentesco( 11 );
                beneficiario.setSexo( oBeneficiario.getDatosPersonales().getGenSexo() );
                if( oBeneficiario.getDatosPersonales().getIdProfesion() > 0 ) {
                    beneficiario
                            .setIdProfesion( oBeneficiario.getDatosPersonales().getIdProfesion() );
                } else {
                    beneficiario.setIdProfesion( 1 );
                }
                beneficiario.setNombre( oBeneficiario.getDatosPersonales().getNombre() );
                final Producto[] productos = productoAsegurado.obtenerProductosAsegurado(
                        oDatosAlta.getTitular().getProductosContratados(), oDatosPlan );
                beneficiario.setListaProductos( productos );
                /* @TODO NO BORRAR!!!
                 * String tarjeta = oBeneficiario.getSNumTarjetaSanitas(); if( !StringUtils.isEmpty(
                 * tarjeta ) ) { obtenerProcedencia(tarjeta, oBeneficiario.getDatosPersonales(),
                 * beneficiario); }
                 */
                beneficiarios.add( beneficiario );
            }
        } else {
            // Si no hay lista de beneficiarios se trata de un alta
            // Primero se procesa el titular
            es.sanitas.seg.simulacionpoliza.services.api.simulacion.vo.Beneficiario beneficiario = new es.sanitas.seg.simulacionpoliza.services.api.simulacion.vo.Beneficiario();

            beneficiario.setFechaNacimiento(
                    cambiarFecha( oDatosAlta.getTitular().getDatosPersonales().getFNacimiento(), oDatosAlta.getFAlta() ) );
            beneficiario.setParentesco( 1 );
            // aunque se permite el genero 3 cuando no hay uno definido no podemos usarlo.
            // Así que enviamos un 2 (por temas de ginecologia tambien).
            beneficiario.setSexo( oDatosAlta.getTitular().getDatosPersonales().getGenSexo() == 0 ? 2
                    : oDatosAlta.getTitular().getDatosPersonales().getGenSexo() );
            beneficiario.setIdProfesion( 1 );
            beneficiario.setNombre(
                    String.valueOf( oDatosAlta.getTitular().getDatosPersonales().getNombre() ) );
            if( oDatosAlta.getTitular() instanceof DatosAseguradoInclusion) {
                final DatosAseguradoInclusion dai = (DatosAseguradoInclusion)oDatosAlta.getTitular();
                if( dai.getSIdCliente() != null && dai.getSIdCliente() > 0 ) {
                    beneficiario.setIdCliente( dai.getSIdCliente().intValue() );
                }
            }

            // Si hay lista de productos se incluyen como productos añadidos al alta
            Producto[] productos = productoAsegurado.obtenerProductosAsegurado(
                    oDatosAlta.getTitular().getProductosContratados(), oDatosPlan );
            if( lProductos != null && !lProductos.isEmpty() ) {
                productos = ArrayUtils.addAll( productos,
                        iProducto.obtenerProductos( lProductos.get( 0 ).getProductos(), oDatosPlan ) );
            }
            beneficiario.setListaProductos( productos );

            /*
             * // Las procedencias pueden venir de tarjetas Sanitas List<String> tarjetas = ( (
             * DatosAltaAsegurados )oDatosAlta ).getLNumTarjAsegurados(); if (tarjetas != null &&
             * !tarjetas.isEmpty()){ String tarjeta = tarjetas.get( 0 ); obtenerProcedencia(tarjeta,
             * oDatosAlta.getTitular().getDatosPersonales(), beneficiario); }else{
             * beneficiario.setProcedencia( obtenerProcedencia(lProductos) ); }
             */
            beneficiarios.add( beneficiario );

            // Y luego se procesan el resto de asegurados
            if( oDatosAlta.getAsegurados() != null && oDatosAlta.getAsegurados().size() > 0 ) {
                final Iterator<DatosAseguradoInclusion> iteradorAsegurados = oDatosAlta.getAsegurados()
                        .iterator();
                int contadorBeneficiario = 1;
                while( iteradorAsegurados.hasNext() ) {
                    final DatosAseguradoInclusion oDatosAsegurado = iteradorAsegurados.next();

                    beneficiario = new es.sanitas.seg.simulacionpoliza.services.api.simulacion.vo.Beneficiario();

                    beneficiario.setFechaNacimiento(
                            cambiarFecha( oDatosAsegurado.getDatosPersonales().getFNacimiento(),
                                    oDatosAlta.getFAlta() ) );
                    beneficiario.setParentesco( 11 );
                    // aunque se permiten el genero 3 cuando no hay uno definido no podemos usarlo.
                    // Así que enviamos un 2 (por temas de ginecologia tambien).
                    beneficiario.setSexo( oDatosAsegurado.getDatosPersonales().getGenSexo() == 0 ? 2
                            : oDatosAsegurado.getDatosPersonales().getGenSexo() );
                    beneficiario.setNombre( oDatosAsegurado.getDatosPersonales().getNombre() );
                    beneficiario.setIdProfesion( 1 );
                    if( oDatosAsegurado.getSIdCliente() != null ) {
                        beneficiario.setIdCliente( oDatosAsegurado.getSIdCliente().intValue() );
                    }

                    productos = productoAsegurado.obtenerProductosAsegurado( oDatosAsegurado.getProductosContratados
                            (), oDatosPlan );
                    if( lProductos != null && !lProductos.isEmpty() ) {
                        productos = ArrayUtils.addAll( productos,
                                iProducto.obtenerProductos( lProductos.get( contadorBeneficiario ).getProductos(),
                                        oDatosPlan
                                ) );
                    }
                    beneficiario.setListaProductos( productos );

                    /*
                     * if (tarjetas != null && tarjetas.size() > contadorBeneficiario){ String
                     * tarjeta = tarjetas.get( contadorBeneficiario ); obtenerProcedencia(tarjeta,
                     * oDatosAsegurado.getDatosPersonales(), beneficiario); }else{
                     * beneficiario.setProcedencia( obtenerProcedencia(lProductos) ); }
                     */
                    beneficiarios.add( beneficiario );
                    contadorBeneficiario++;
                }
            }
        }

        return beneficiarios.toArray(
                new es.sanitas.seg.simulacionpoliza.services.api.simulacion.vo.Beneficiario[ 0 ] );
    }

    private String cambiarFecha( String fecha, final String fechaAlta ) {
        String convertida = fecha;

        if( fecha == null || "//".equals( fecha ) ) {
            // Si viene null, le ponemos que tiene 18
            fecha = "18";
        }

        if( fecha != null && !fecha.contains( "/" ) ) {
            final int edad = Integer.valueOf( fecha );
            final Calendar dob = Calendar.getInstance();
            dob.add( Calendar.YEAR, -edad );
            dob.set( Calendar.DAY_OF_MONTH, 1 );
            final SimpleDateFormat sdf = new SimpleDateFormat( DATE_FORMAT );
            convertida = sdf.format( dob.getTime() );
        }
        return convertida;
    }
}
