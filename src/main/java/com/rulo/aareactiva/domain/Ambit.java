package com.rulo.aareactiva.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Ambit {

    private String name;
    private Channel[] channels;

    @Override
    public String toString() {
        return name;
    }

}
