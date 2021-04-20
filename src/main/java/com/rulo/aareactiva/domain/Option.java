package com.rulo.aareactiva.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Option {

    private String format;
    private String url;

    @Override
    public String toString() {
        return format + url + "\n";
    }

}
