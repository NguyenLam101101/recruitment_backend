//package com.example.recruitment2.Config;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.messaging.simp.config.MessageBrokerRegistry;
//import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
//import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
//import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
//
//@Configuration
//@EnableWebSocketMessageBroker
//public class SocketConfig implements WebSocketMessageBrokerConfigurer {
//    final String webServer = new Link().getWebServer();
//    @Override
//    public void configureMessageBroker(MessageBrokerRegistry registry) {
//        registry.enableSimpleBroker("/topic", "/queue"); //các topic bắt đầu bằng tiền tố /topic, server có thể gửi thông điệp đến các clients thông qua các địa chỉ bắt đầu bằng "/topic"
////        registry.setApplicationDestinationPrefixes("/recruitment"); //endpoint để client gửi đến server có tiền tố /recruitment
//        registry.setUserDestinationPrefix("/user"); //endpoint để server gửi đến user bắt đầu bằng /user và thêm định danh của user vào sau
//    }
//
//    @Override
//    public void registerStompEndpoints(StompEndpointRegistry registry) {
//        registry.addEndpoint("/ws") //client có thể đăng ký các endpoint có đường dãn /ws
//                .setAllowedOrigins(webServer, "http://localhost:3000")
//                .withSockJS();
//    }
//}
