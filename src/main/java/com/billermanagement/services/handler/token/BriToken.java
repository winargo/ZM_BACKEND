package com.billermanagement.services.handler.token;

import com.billermanagement.services.handler.common.TokenResponseVO;
import com.billermanagement.util.InitDB;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class BriToken {

    public String getBody(String clientId,String clientSecret){
        return new StringBuilder().append("client_id=").append(InitDB.getInstance().get(clientId))
                .append("&client_secret=").append(InitDB.getInstance().get(clientSecret)).toString();
    }

    public TokenResponseVO getResponse(String resBody) throws IOException {
        TokenResponseVO responseVO = new TokenResponseVO();
        ObjectMapper mapper = new ObjectMapper();
        String payload = resBody.replace("developer.email", "developer_email");
        BriTokenResVO resVO = mapper.readValue(payload, BriTokenResVO.class);
        responseVO.setAccess_token(resVO.getAccess_token());
        responseVO.setExpires_in(resVO.getExpires_in());
        return responseVO;
    }

    @Data
    private static class BriTokenResVO {
        private String refresh_token_expires_in;
        private String api_product_list;
        private String[] api_product_list_json;
        private String organization_name;
        private String developer_email;
        private String token_type;
        private String issued_at;
        private String client_id;
        private String access_token;
        private String application_name;
        private String scope;
        private String expires_in;
        private String refresh_count;
        private String status;
    }
}
