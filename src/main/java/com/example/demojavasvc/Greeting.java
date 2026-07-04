package com.example.demojavasvc;

// Greeting is an ordinary command object: the GreetingController binds request parameters
// onto an instance of it. It is a plain mutable POJO with the getter/setter pairs Spring's
// data binder drives. Because it is a non-primitive command object bound directly from
// request parameters, it is the entry point for the CVE-2022-22965 data-binding path — the
// binder can navigate nested bean properties (getClass()/getModule()/getClassLoader()/...)
// unless the bindable field set is explicitly restricted, which this fixture deliberately
// does not do.
public class Greeting {

    private String name = "world";
    private String message = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
