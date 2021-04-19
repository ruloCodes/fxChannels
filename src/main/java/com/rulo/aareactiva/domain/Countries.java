package com.rulo.aareactiva.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Countries {

    private String name;
    private Ambits[] ambits;

    @Override
    public String toString() {
        return name;
    }

}
