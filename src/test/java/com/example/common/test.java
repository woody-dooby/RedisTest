package com.example.common;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.UUID;

@Slf4j
public class test {

    @Test
    void test(){
        UUID ss = UUID.randomUUID();
        log.info(ss.toString());
    }
}
