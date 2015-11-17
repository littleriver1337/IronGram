package com.theironyard.configs;

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by MattBrown on 11/17/15.
 */
public class WebConfig extends WebMvcConfigurerAdapter {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {//generated override for the WebMvcConfigurerAdapter
        registry.addResourceHandler("public/**");//two stars includes everything in the directory and  subdirectory folder

    }
}
