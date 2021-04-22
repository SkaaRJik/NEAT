package ru.filippov.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ValidatorTest {

    @Test
    void allowedNameOfFile() {
        String str = "";

        Assertions.assertTrue(Validator.allowedNameOfFile(str));



    }
}