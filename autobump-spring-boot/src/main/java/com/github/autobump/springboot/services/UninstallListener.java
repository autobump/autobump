package com.github.autobump.springboot.services;

import com.atlassian.connect.spring.AddonUninstalledEvent;
import com.atlassian.connect.spring.AtlassianHostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class UninstallListener implements ApplicationListener<AddonUninstalledEvent> {
    @Autowired
    private AtlassianHostRepository repository;

    @Override
    public void onApplicationEvent(AddonUninstalledEvent event) {
        repository.delete(event.getHost());
        System.out.println("event = " + event);
    }
}
