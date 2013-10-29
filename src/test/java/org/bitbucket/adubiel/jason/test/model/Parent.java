package org.bitbucket.adubiel.jason.test.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Adam Dubiel
 */
public class Parent {

    private long id;

    private String name;

    private SingleChild singleChild;

    private List<Child> children = new ArrayList<Child>();

    public Parent() {
    }

    public Parent(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getComposite() {
        return name + id;
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

    public String getGetterName() {
        return name;
    }

    public SingleChild getSingleChild() {
        return singleChild;
    }

    public void setSingleChild(SingleChild singleChild) {
        this.singleChild = singleChild;
    }

    public Parent withSingleChild(SingleChild singleChild) {
        setSingleChild(singleChild);
        return this;
    }

    public List<Child> getChildren() {
        return children;
    }

    public void setChildren(List<Child> children) {
        this.children = children;
    }

    public Parent addChild(Child child) {
        children.add(child);
        return this;
    }
}
