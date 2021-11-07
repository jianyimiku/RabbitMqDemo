package com.example.orderservice.converter;

import com.example.orderservice.dto.OrderMessageDto;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.ClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ：sjq
 * @date ：Created in 2021/11/4 下午9:48
 * @description：
 * @modified By：
 * @version: $
 */
@Configuration
public class ConverterConfig {
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(){
        Jackson2JsonMessageConverter jackson2JsonMessageConverter
                = new Jackson2JsonMessageConverter();
        jackson2JsonMessageConverter.setClassMapper(new ClassMapper() {
            @Override
            public void fromClass(Class<?> clazz, MessageProperties properties) {
                System.out.println("fromClass");
            }

            @Override
            public Class<?> toClass(MessageProperties properties) {
                System.out.println("toClass");
                return OrderMessageDto.class;
            }
        });

        return jackson2JsonMessageConverter;
    }
}
