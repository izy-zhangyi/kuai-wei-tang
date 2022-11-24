package com.kwt.req;

import lombok.Data;

@Data
public class UserLoginReq {
    private String phone;
    private String code;
}
