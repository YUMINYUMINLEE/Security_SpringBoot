package com.EmperorPenguin.SangmyungBank.baseUtil.config;

import com.EmperorPenguin.SangmyungBank.baseUtil.filter.MyFilter1;
import com.EmperorPenguin.SangmyungBank.baseUtil.filter.MyFilter2;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    //	Spring Security Filter 이후에 적용된다.
    @Bean
    public FilterRegistrationBean<MyFilter1> filter1(){
        FilterRegistrationBean<MyFilter1> bean = new FilterRegistrationBean<>(new MyFilter1());
        bean.addUrlPatterns("/*");
        bean.setOrder(0);           // 낮은 번호가 필터중에서 가장 먼저 실행
        return bean;
    }

    @Bean
    public FilterRegistrationBean<MyFilter2> filter2(){
        FilterRegistrationBean<MyFilter2> bean = new FilterRegistrationBean<>(new MyFilter2());
        bean.addUrlPatterns("/*");
        bean.setOrder(1);           // 낮은 번호가 필터중에서 가장 먼저 실행
        return bean;
    }
}
