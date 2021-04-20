package com.rulo.aareactiva.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Channel {

    private String name;
    private String web;
    private String logo;
    private Option[] options;

    @Override
    public String toString() {
        return name;
    }

}
