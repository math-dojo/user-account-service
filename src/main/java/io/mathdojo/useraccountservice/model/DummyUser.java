package io.mathdojo.useraccountservice.model;

public class DummyUser {

    public DummyUser() {
    }

    public DummyUser(String name) {
        this.name = name;
    }

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
