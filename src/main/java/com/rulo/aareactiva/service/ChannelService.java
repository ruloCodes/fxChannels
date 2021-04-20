package com.rulo.aareactiva.service;

import com.rulo.aareactiva.domain.TDT;
import retrofit2.http.GET;
import rx.Observable;

public interface ChannelService {

    @GET("/lists/radio.json")
    Observable<TDT> getListaRadios();

    @GET("/lists/tv.json")
    Observable<TDT> getListaTV();

}
