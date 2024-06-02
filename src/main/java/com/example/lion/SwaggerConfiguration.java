package com.example.lion;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.servers.Server;

import java.util.List;
import java.util.Map;

@Configuration
public class SwaggerConfiguration {

    @Value("${server.url}" )
    private String serverUrl;

    @Bean
    public OpenAPI configuration() {
        var server = new Server();
        server.setUrl(serverUrl);

        var contact = new Contact();
        contact.setName("Radek Huebner");
        contact.setUrl("https://github.com/rhubner/lion");

        var information = new Info()
                .title("File management system")
                .version("0.0.2")
                .description("Example application for simple file management.")
                .contact(contact);
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("http-auth", new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic"))
                )
                .info(information)
                .servers(List.of(server));

    }


}
