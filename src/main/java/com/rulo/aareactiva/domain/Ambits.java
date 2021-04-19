package com.rulo.aareactiva.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Ambits {

    private String name;
    private Channels[] channels;

    @Override
    public String toString() {
        return name;
    }

}
