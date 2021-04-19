package com.rulo.aareactiva.service;

import com.rulo.aareactiva.domain.TDT;
import rx.Observable;
import retrofit2.http.GET;

public interface CanalesService {

    @GET("/lists/radio.json")
    Observable<TDT> getListaRadios();

    @GET("/lists/tv.json")
    Observable<TDT> getListaTV();

}
