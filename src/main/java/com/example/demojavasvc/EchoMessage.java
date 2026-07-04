package com.example.demojavasvc;

// EchoMessage is a plain, concrete, non-polymorphic DTO. It has no @JsonTypeInfo, no
// abstract/interface-typed fields, and no polymorphic base — so jackson deserializes it by
// its declared concrete type only. This is what keeps the /echo endpoint clear of the
// CVE-2019-17531 gadget path: there is no polymorphic slot for a malicious type name to
// select even if an attacker supplied one.
public class EchoMessage {

    private String name;
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
