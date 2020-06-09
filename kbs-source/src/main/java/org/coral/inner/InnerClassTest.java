package org.coral.inner;

public class InnerClassTest {
    public static void main(String[] args) {
        StaticChildClass childClass = new StaticChildClass();
    }
    private void printClass(){
        ChildClass childClass = new ChildClass();
    }
    class ChildClass{
        private int qq;
        public  void ok1(){

        }
    }
    static class StaticChildClass{
        private int qq;
        public  void ok1(){

        }
    }
}
