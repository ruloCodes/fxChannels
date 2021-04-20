package com.rulo.aareactiva.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Countrie {

    private String name;
    private Ambit[] ambits;

    @Override
    public String toString() {
        return name;
    }

}
