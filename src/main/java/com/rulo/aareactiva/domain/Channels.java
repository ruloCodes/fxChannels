package com.rulo.aareactiva.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Channels {

    private String name;
    private String web;
    private String logo;
    private Options[] options;

    @Override
    public String toString() {
        return name;
    }

}
