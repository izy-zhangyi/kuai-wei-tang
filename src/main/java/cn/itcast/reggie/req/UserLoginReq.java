package cn.itcast.reggie.req;

import lombok.Data;

@Data
public class UserLoginReq {
    private String phone;
    private String code;
}
