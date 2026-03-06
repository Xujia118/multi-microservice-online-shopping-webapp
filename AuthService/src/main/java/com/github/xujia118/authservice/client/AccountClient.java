package com.github.xujia118.authservice.client;

import com.github.xujia118.common.dto.AccountDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "AccountService",
        url = "${ACCOUNT_SERVICE_URL:http://localhost:8081}",
        path = "/api/v1/accounts"
)
public interface AccountClient {

    @PostMapping
    void createAccount(@RequestBody AccountDto accountDto);
}
