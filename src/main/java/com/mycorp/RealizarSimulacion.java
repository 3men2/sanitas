package com.mycorp;

import com.mycorp.soporte.*;
import com.mycorp.util.*;
import es.sanitas.bravo.ws.stubs.contratacionws.consultasoperaciones.DatosContratacionPlan;
import es.sanitas.bravo.ws.stubs.contratacionws.consultasoperaciones.DatosPlanProducto;
import es.sanitas.bravo.ws.stubs.contratacionws.documentacion.Primas;
import es.sanitas.seg.simulacionpoliza.services.api.simulacion.vo.TarifaBeneficiario;
import es.sanitas.seg.simulacionpoliza.services.api.simulacion.vo.TarifaDesglosada;
import es.sanitas.seg.simulacionpoliza.services.api.simulacion.vo.TarifaProducto;
import es.sanitas.seg.simulacionpoliza.services.api.simulacion.vo.Tarificacion;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import wscontratacion.contratacion.fuentes.parametros.DatosAlta;
import wscontratacion.contratacion.fuentes.parametros.DatosAsegurado;

import java.util.*;
import java.util.concurrent.Callable;


public class RealizarSimulacion {

    @Autowired
    IFrecuenciaTarificar frecuenciaTarificar;
    @Autowired
    IResultadoSimulaciones resultadoSimulaciones;
    @Autowired
    IObtenerPromociones obtenerPromociones;
    @Autowired
    ISolver iSolver;
    @Autowired
    IRecibos iRecibos;

    private static final Logger LOG = LoggerFactory.getLogger( RealizarSimulacion.class );

    /**
     * Método que realiza las llamadas a las diferentes clases de simulación, para tarificar
     *
     * @param oDatosAlta
     *            Objeto del tipo DatosAlta
     * @param lProductos
     *            Listado de productos que sólo se tendrán en cuenta en caso de inclusión de
     *            productos, en el resto de casos no aplica
     * @return Map con diferentes valores obtenidos de la simulación, incluida la lista de precios
     *         por asegurado
     * @throws Exception
     *             Excepción lanzada en caso de que haya errores
     * @throws ExcepcionContratacion
     *             Excepción controlada
     */
    public Map< String, Object > realizarSimulacion( final DatosAlta oDatosAlta,
                                                     final List< ProductoPolizas > lProductos, final List< BeneficiarioPolizas > lBeneficiarios,
                                                     final boolean desglosar, final Map< String, Object > hmValores )
            throws Exception, ExcepcionContratacion {

        final Map< String, Object > hmSimulacion = new HashMap< >();
        @SuppressWarnings( "unchecked" ) final List< String > lExcepciones = ( List< String > )hmValores
                .get( "EXCEPCIONES" );
        final DatosContratacionPlan oDatosPlan = ( DatosContratacionPlan )hmValores
                .get( StaticVarsContratacion.DATOS_PLAN );

        final List< Primas > primas = new ArrayList< >();
        final Double descuentosTotales[] = { 0.0, 0.0, 0.0, 0.0 };
        final Double pagoTotal[] = { 0.0, 0.0, 0.0, 0.0 };
        final Double precioConPromocion[] = { 0.0, 0.0, 0.0, 0.0 };
        final List< List< PrimasPorProducto > > primasDesglosadas = new ArrayList< >();
        final List< List< PromocionAplicada > > promociones = new ArrayList< >();
        final List< List< com.mycorp.soporte.Recibo > > recibos = new ArrayList< >();
        final List< String > errores = new ArrayList< >();

        Set< FrecuenciaEnum > frecuenciasTarificar =
                frecuenciaTarificar.getFrecuenciaTarificar(oDatosAlta, lBeneficiarios, hmValores);

        final Collection< Callable< TarificacionPoliza > > solvers = new ArrayList< >(
                0 );
        for( final FrecuenciaEnum frecuencia : frecuenciasTarificar ) {
            solvers.add( iSolver.simularPolizaFrecuencia( hmValores, oDatosAlta, lProductos, lBeneficiarios,
                    frecuencia ) );
        }

        final List< TarificacionPoliza > resultadoSimulacionesList =
                resultadoSimulaciones.getResultadoSimulaciones(solvers);


        for( final FrecuenciaEnum frecuencia : frecuenciasTarificar ) {
            final TarificacionPoliza retornoPoliza = IterableUtils.find( resultadoSimulacionesList,
                    new Predicate< TarificacionPoliza >() {

                        @Override
                        public boolean evaluate( final TarificacionPoliza object ) {
                            return object != null && object.getTarificacion() != null;
                        }
                    } );

            if( retornoPoliza == null ) {
                throw new ExcepcionContratacion(
                        "No se ha podido obtener un precio para el presupuesto. Por favor, inténtelo de nuevo más tarde." );
            }
            final Tarificacion retorno = retornoPoliza.getTarificacion();
            final String codigoError = retornoPoliza.getCodigoError();
            if( codigoError != null && !StringUtils.isEmpty( codigoError ) ) {
                errores.add( codigoError );
            }

            int contadorBeneficiario = 0;
            double css = 0;
            for( final TarifaBeneficiario tarifaBeneficiario : retorno.getTarifas()
                    .getTarifaBeneficiarios() ) {
                List< PrimasPorProducto > listaProductoPorAseg = new ArrayList< >();
                if( primasDesglosadas.size() > contadorBeneficiario ) {
                    listaProductoPorAseg = primasDesglosadas.get( contadorBeneficiario );
                } else {
                    primasDesglosadas.add( listaProductoPorAseg );
                }

                Primas primaAsegurado = new Primas();
                if( primas.size() > contadorBeneficiario ) {
                    primaAsegurado = primas.get( contadorBeneficiario );
                } else {
                    primas.add( primaAsegurado );
                }

                int contadorProducto = 0;
                for( final TarifaProducto tarifaProducto : tarifaBeneficiario.getTarifasProductos() ) {

                    if( ( tarifaProducto.getIdProducto() != 389
                            || !comprobarExcepcion( lExcepciones,
                            StaticVarsContratacion.PROMO_ECI_COLECTIVOS )
                            || hayTarjetas( oDatosAlta ) ) && tarifaProducto.getIdProducto() != 670
                            || !comprobarExcepcion( lExcepciones,
                            StaticVarsContratacion.PROMO_FARMACIA )
                            || hayTarjetas( oDatosAlta ) ) {

                        PrimasPorProducto oPrimasProducto = new PrimasPorProducto();
                        if( listaProductoPorAseg.size() > contadorProducto ) {
                            oPrimasProducto = listaProductoPorAseg.get( contadorProducto );
                        } else {
                            oPrimasProducto
                                    .setCodigoProducto( tarifaProducto.getIdProducto().intValue() );
                            oPrimasProducto.setNombreProducto( tarifaProducto.getDescripcion() );
                            final DatosPlanProducto producto = getDatosProducto( oDatosPlan,
                                    tarifaProducto.getIdProducto() );
                            if( producto != null ) {
                                oPrimasProducto
                                        .setObligatorio( producto.isSwObligatorio() ? "S" : "N" );
                                oPrimasProducto.setNombreProducto( producto.getDescComercial() );
                            }
                            listaProductoPorAseg.add( oPrimasProducto );
                        }

                        final TarifaDesglosada tarifaDesglosada = tarifaProducto.getTarifaDesglosada();
                        final Primas primaProducto = oPrimasProducto.getPrimaProducto();

                        // Se calcula el CSS total para poder calcular el precio con promoción
                        css += tarifaDesglosada.getCss();

                        /**
                         * No sumamos tarifaDesglosada.getCss() + tarifaDesglosada.getCssre() porque
                         * la Compensación del Consorcio de Seguros sólo se aplica en la primera
                         * mensualidad. Y queremos mostrar al usuario el precio de todos los meses.
                         */
                        final double pago = tarifaDesglosada.getPrima() + tarifaDesglosada.getISPrima();
                        final double descuento = tarifaDesglosada.getDescuento();
                        switch( frecuencia.getValor() ) {
                            case 1:
                                // Mensual
                                primaProducto.setPrima( "" + descuento );
                                break;
                            case 2:
                                // Trimestral
                                primaProducto.setPrima( "" + descuento );
                                break;
                            case 3:
                                // Semestral
                                primaProducto.setPrima( "" + descuento*2 );
                                break;
                            case 4:
                                // Anual
                                primaProducto.setPrima( "" + descuento*2 );
                                break;
                        }
                        descuentosTotales[ frecuencia.getValor() - 1 ] += tarifaDesglosada
                                .getDescuento();
                        pagoTotal[ frecuencia.getValor() - 1 ] += pago
                                + tarifaDesglosada.getDescuento();

                    }
                    contadorProducto++;
                }
                contadorBeneficiario++;
            }

            // Promociones aplicadas a la simulación
            promociones.add(obtenerPromociones.recuperarPromocionesAgrupadas(
                    retorno.getPromociones().getListaPromocionesPoliza(),
                    contadorBeneficiario));

            // Lista de recibos del primer año
            if( retorno.getRecibos() != null ) {
                recibos.add( iRecibos.toReciboList( retorno.getRecibos().getListaRecibosProductos() ) );

                // Se calcula el precio total con promoción
                // Es el importe del primer recibo sin el impuesto del consorcio
                precioConPromocion[ frecuencia.getValor()
                        - 1 ] = retorno.getRecibos().getReciboPoliza().getRecibos()[ 0 ].getImporte() - css;
            }
        }

        hmSimulacion.put( StaticVarsContratacion.PRIMAS_SIMULACION, primas );
        hmSimulacion.put( StaticVarsContratacion.PRIMAS_SIMULACION_DESGLOSE, primasDesglosadas );
        hmSimulacion.put( StaticVarsContratacion.SIMULACION_PROVINCIA, "Madrid" );
        hmSimulacion.put( StaticVarsContratacion.HAY_DESGLOSE, desglosar );
        hmSimulacion.put( StaticVarsContratacion.DESCUENTOS_TOTALES, descuentosTotales );
        hmSimulacion.put( StaticVarsContratacion.TOTAL_ASEGURADOS, primas );
        hmSimulacion.put( StaticVarsContratacion.PROMOCIONES_SIMULACION, promociones );
        hmSimulacion.put( StaticVarsContratacion.RECIBOS_SIMULACION, recibos );
        hmSimulacion.put( StaticVarsContratacion.PAGO_TOTAL, pagoTotal );
        hmSimulacion.put( StaticVarsContratacion.ERROR, errores );

        // Si en la simulación hay apliacada alguna promoción
        // de descuento sobre la prima
        if( hayPromocionDescuento( promociones ) ) {
            hmSimulacion.put( StaticVarsContratacion.PAGO_TOTAL, precioConPromocion );
            hmSimulacion.put( StaticVarsContratacion.PRECIOS_SIN_PROMOCION_SIMULACION, pagoTotal );
        }
        return hmSimulacion;
    }

    private DatosPlanProducto getDatosProducto( final DatosContratacionPlan oDatosPlan,
                                                final long idProducto ) {
        for( final DatosPlanProducto producto : oDatosPlan.getProductos() ) {
            if( producto.getIdProducto() == idProducto ) {
                return producto;
            }
        }
        return null;
    }

    private boolean hayPromocionDescuento(
            final List< List< PromocionAplicada > > promocionesAplicadas ) {
        boolean codigoAplicado = Boolean.FALSE;
        if( promocionesAplicadas != null ) {
            for( final List< PromocionAplicada > promociones : promocionesAplicadas ) {
                for( final PromocionAplicada promocion : promociones ) {
                    if( promocion != null && TipoPromocionEnum.DESCUENTO_PORCENTAJE
                            .equals( promocion.getTipoPromocion() ) ) {
                        codigoAplicado = Boolean.TRUE;
                    }
                }
            }
        }
        return codigoAplicado;
    }


    /**
     * @param oDatosAlta
     * @return true si el titular o alguno de los asegurados tiene tarjeta de sanitas.
     */
    private boolean hayTarjetas( final DatosAlta oDatosAlta ) {
        boolean tieneTarjeta = false;
        if( oDatosAlta != null && oDatosAlta.getTitular() != null ) {
            if( "S".equals( oDatosAlta.getTitular().getSwPolizaAnterior() ) ) {
                tieneTarjeta = true;
            }
        }
        if( oDatosAlta != null && oDatosAlta.getAsegurados() != null
                && oDatosAlta.getAsegurados().size() > 0 ) {
            @SuppressWarnings( "unchecked" ) final Iterator< DatosAseguradoInclusion > iterAseg = oDatosAlta
                    .getAsegurados().iterator();
            while( iterAseg.hasNext() ) {
                final DatosAsegurado aseg = iterAseg.next();
                if( "S".equals( aseg.getSwPolizaAnterior() ) ) {
                    tieneTarjeta = true;
                }
            }
        }
        return tieneTarjeta;
    }

    /**
     * Popula una lista de Recibos con la información de los recibos de la simulación.
     *
     * @param recibos recibos del primer año de la simulación
     * @return lista de Recibos con la información de los recibos de la simulación.
     */



    /**
     * Comprueba si pertenece la excepcion a la lista.
     *
     * @param lExcepciones Lista de excepciones.
     * @param comprobar Dato a comprobar.
     * @return True si pertenece false en caso contrario.
     */
    public static boolean comprobarExcepcion( final List<String> lExcepciones, final String comprobar ) {
        LOG.debug( "Se va a comprobar si " + comprobar + " estÃ¡ en la lista " + lExcepciones );
        boolean bExcepcion = false;
        if( comprobar != null && lExcepciones != null && lExcepciones.contains( comprobar ) ) {
            bExcepcion = true;
        }
        return bExcepcion;
    }

}
