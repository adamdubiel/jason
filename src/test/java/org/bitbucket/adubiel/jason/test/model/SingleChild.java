package org.bitbucket.adubiel.jason.test.model;

/**
 *
 * @author Adam Dubiel
 */
public class SingleChild {

    private long id;

    private String name;

    public SingleChild() {
    }

    public SingleChild(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
