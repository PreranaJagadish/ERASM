package com.erasm.exception;

public class EmployeeSkillNotFoundException extends RuntimeException {
    public EmployeeSkillNotFoundException(String message) {
        super(message);
    }
}
