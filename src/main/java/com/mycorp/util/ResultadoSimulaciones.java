package com.mycorp.util;

import com.mycorp.soporte.ExcepcionContratacion;
import com.mycorp.soporte.TarificacionPoliza;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class ResultadoSimulaciones implements IResultadoSimulaciones {

    private static final Logger LOG = LoggerFactory.getLogger( ResultadoSimulaciones.class );
    private static final int TIMEOUT = 30;
    private static final int NUMERO_HILOS = 4;

    @Autowired
    private final ExecutorService pool = Executors.newFixedThreadPool( NUMERO_HILOS );

    public ResultadoSimulaciones() {}

    @Override
    public List<TarificacionPoliza> getResultadoSimulaciones(
            Collection<Callable<TarificacionPoliza>> solvers)
                    throws ExcepcionContratacion {
        final CompletionService<TarificacionPoliza> ecs = new ExecutorCompletionService< >(
                pool );
        int n = 0;
        for( final Callable<TarificacionPoliza> s : solvers ) {
            try {
                ecs.submit( s );
                n++;
            } catch( final RuntimeException ree ) {
                LOG.error( "RejectedExecutionException con el metodo " + s.toString(), ree );
            }
        }
        final List<TarificacionPoliza> resultadoSimulaciones = new ArrayList< >();
        for( int i = 0; i < n; ++i ) {
            try {
                final Future<TarificacionPoliza> future = ecs.poll( TIMEOUT, TimeUnit.SECONDS );
                if( future != null ) {
                    resultadoSimulaciones.add( future.get() );
                } else {
                    LOG.error(
                            "La llamada asincrona al servicio de simulacion ha fallado por timeout" );
                }
            } catch( final InterruptedException e ) {
                LOG.error( "InterruptedException", e );
            } catch( final ExecutionException e ) {
                LOG.error( "ExecutionException", e );
                throw new ExcepcionContratacion(
                        e.getCause().getMessage());
            }
        }
        return resultadoSimulaciones;
    }
}
