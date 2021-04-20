package com.rulo.aareactiva.service;

import com.rulo.aareactiva.domain.TDT;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;

import static com.rulo.aareactiva.util.Constants.URL_BASE;

public class ChannelServiceImp implements ChannelService {

    private final ChannelService listas;

    public ChannelServiceImp() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL_BASE)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        listas = retrofit.create(ChannelService.class);
    }

    @Override
    public Observable<TDT> getListaRadios() {
        return listas.getListaRadios();
    }

    @Override
    public Observable<TDT> getListaTV() {
        return listas.getListaTV();
    }

}
