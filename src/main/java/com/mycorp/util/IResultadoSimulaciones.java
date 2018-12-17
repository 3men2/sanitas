package com.mycorp.util;

import com.mycorp.soporte.ExcepcionContratacion;
import com.mycorp.soporte.TarificacionPoliza;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

public interface IResultadoSimulaciones {

    List<TarificacionPoliza> getResultadoSimulaciones(
            Collection<Callable<TarificacionPoliza>> solvers)
                    throws ExcepcionContratacion;
}
