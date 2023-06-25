package com.nplat.convert.entity.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data()
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UserLoginRequest {
    private String user;
    private String password;

}
