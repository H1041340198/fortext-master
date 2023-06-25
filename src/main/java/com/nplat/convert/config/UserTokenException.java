package com.nplat.convert.config;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserTokenException extends RuntimeException {
    private String message;
}
