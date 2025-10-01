package org.jboss.weld.junit.jupiter.alternative;

public class Fish {

    private int legs;

    public Fish(int numberOfLegs) {
        this.legs = numberOfLegs;
    }

    public int getNumberOfLegs() {
        return legs;
    }
}
