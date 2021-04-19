package com.rulo.aareactiva.service;

import com.rulo.aareactiva.domain.TDT;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;

import static com.rulo.aareactiva.util.Constants.URL_BASE;

public class CanalesServiceImp {

    private CanalesService listas;

    public CanalesServiceImp() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL_BASE)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        listas = retrofit.create(CanalesService.class);
    }

    public Observable<TDT> getListasRadio() {
        return listas.getListaRadios();
    }

    public Observable<TDT> getListasTV() {
        return listas.getListaTV();
    }

}
