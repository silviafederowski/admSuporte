package br.com.silsys.admsuporte.model;

public enum ResetMethod {
    EMAIL,
    SMS;

    public static ResetMethod fromParam(String value) {
        if (value != null && value.trim().equalsIgnoreCase("sms")) {
            return SMS;
        }
        return EMAIL;
    }

    public String paramValue() {
        return this == SMS ? "sms" : "email";
    }
}
