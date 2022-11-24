package com.kwt.req;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
public class EmployeeLoginReq {
    private String username;
    private String password;
}
