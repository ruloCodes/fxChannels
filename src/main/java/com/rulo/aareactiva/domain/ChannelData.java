package com.rulo.aareactiva.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChannelData {

    private String pais;
    private String tipo;
    private String nombre;
    private String imagen;
    private String sitio;
    private String repro;

    @Override
    public String toString() {
        return nombre ;
    }

}
