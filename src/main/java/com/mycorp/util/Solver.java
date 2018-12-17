package com.mycorp.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycorp.soporte.*;
import es.sanitas.bravo.ws.stubs.contratacionws.consultasoperaciones.DatosContratacionPlan;
import es.sanitas.seg.simulacionpoliza.services.api.simulacion.vo.Error;
import es.sanitas.seg.simulacionpoliza.services.api.simulacion.vo.Simulacion;
import es.sanitas.seg.simulacionpoliza.services.api.simulacion.vo.Tarificacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import wscontratacion.contratacion.fuentes.parametros.DatosAlta;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class Solver implements ISolver {


    private static final String LINE_BREAK = "<br/>";

    private static final Logger LOG = LoggerFactory.getLogger( Solver.class );

    private SimulacionWS servicioSimulacion;

    @Autowired
    IInformacionPromociones informacionPromociones;
    @Autowired
    ITier tier;
    @Autowired
    IBeneficiarios beneficiarios;
    @Autowired
    IInformacionContratacion informacionContratacion;

    public Solver() {
    }

    @Override
    public Callable<TarificacionPoliza> simularPolizaFrecuencia(
            final Map< String, Object > hmValores, final DatosAlta oDatosAlta,
            final List<ProductoPolizas> lProductos,
            final List<BeneficiarioPolizas> lBeneficiarios, final FrecuenciaEnum frecuencia ) {
        return new Callable<TarificacionPoliza>() {

            @Override
            public TarificacionPoliza call() throws ExcepcionContratacion {
                return simular( hmValores, oDatosAlta, lProductos, lBeneficiarios, frecuencia );
            }
        };
    }

    private TarificacionPoliza simular(final Map< String, Object > hmValores, final DatosAlta oDatosAlta,
                                       final List<ProductoPolizas> lProductos, final List<BeneficiarioPolizas> lBeneficiarios,
                                       final FrecuenciaEnum frecuencia ) throws ExcepcionContratacion {

        TarificacionPoliza resultado = null;
        final Simulacion in = new Simulacion();
        final DatosContratacionPlan oDatosPlan = ( DatosContratacionPlan )hmValores
                .get( StaticVarsContratacion.DATOS_PLAN );

        if( lBeneficiarios != null ) {
            in.setOperacion( StaticVarsContratacion.INCLUSION_BENEFICIARIO );
        } else {
            in.setOperacion( StaticVarsContratacion.ALTA_POLIZA );
        }
        in.setInfoPromociones( informacionPromociones.obtenerInfoPromociones( oDatosAlta ) );
        in.setInfoTier( tier.obtenerTier( oDatosAlta ) );
        in.setListaBeneficiarios(
                beneficiarios.obtenerBeneficiarios( oDatosAlta, lProductos, lBeneficiarios, oDatosPlan ) );
        in.setInfoContratacion(
                informacionContratacion.obtenerInfoContratacion( oDatosAlta, lBeneficiarios, lProductos, frecuencia,
                        in.getOperacion()) );

        final RESTResponse<Tarificacion, Error > response = servicioSimulacion
                .simular( in );
        if( !response.hasError() && response.out.getTarifas() != null ) {
            resultado = new TarificacionPoliza();
            resultado.setTarificacion( response.out );

            // Si se ha introducido un código promocional no válido se repite la simulación sin el
            // código promocional
        } else if( response.hasError() && StaticVarsContratacion.SIMULACION_ERROR_COD_PROMOCIONAL
                .equalsIgnoreCase( response.error.getCodigo() ) ) {
            if( oDatosAlta instanceof DatosAltaAsegurados) {
                final DatosAltaAsegurados oDatosAltaAsegurados = (DatosAltaAsegurados)oDatosAlta;
                oDatosAltaAsegurados.setCodigoPromocional( null );
            }
            LOG.info( toMensaje( in, response.rawResponse ) );

            resultado = simular( hmValores, oDatosAlta, lProductos, lBeneficiarios, frecuencia );
            resultado.setCodigoError( StaticVarsContratacion.SIMULACION_ERROR_COD_PROMOCIONAL );
            return resultado;
        } else {
            System.err.println( toMensaje (in, response.rawResponse ) );
            throw new ExcepcionContratacion( response.error.getDescripcion() );
        }

        return resultado;
    }

    private String toMensaje( final Simulacion in, final String error ) {
        final StringBuffer sb = new StringBuffer();
        final ObjectMapper om = new ObjectMapper();
        try {
            sb.append( error );
            sb.append( LINE_BREAK );
            sb.append( LINE_BREAK );
            sb.append( om.writeValueAsString( in ) );
        } catch( final JsonProcessingException e ) {
            LOG.error( e.getMessage(), e );
        }
        return sb.toString();
    }


    /**
     * @return the servicioSimulacion
     */
    public SimulacionWS getServicioSimulacion() {
        return servicioSimulacion;
    }

    /**
     * @param servicioSimulacion the servicioSimulacion to set
     */
    public void setServicioSimulacion( final SimulacionWS servicioSimulacion ) {
        this.servicioSimulacion = servicioSimulacion;
    }
}
