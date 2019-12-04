#### Java中final关键字
    许多编程语言都有某种方法，来向编译器告知一块数据是恒定不变的。有时候数据的恒定不变是很有用的，例如：
    
    一个编译时恒定不变的常量
    一个在运行时初始化，而你不希望它被改变的变量
    对于编译期常量的这种情况，编译器可以将该常量值代入任何可能用到它的计算式中，也就是说，可以在编译期就执行计算式，这减轻了一些运行时的负担。在Java中，这类常量必须是基本类型的，并且以final表示，在对这个常量在定义时，或者构造函数中，必须进行赋值。
    
    一个即是static又是final的域只占一段不能改变的存储空间。

#### final数据
    当final应用于对象引用时，而不是基本类型时，其含义让人疑惑。对基本类型使用final不能改变的是他的数值。而对于对象引用，不能改变的是他的引用，而对象本身是可以修改的。一旦一个final引用被初始化指向一个对象，这个引用将不能再指向其他对象。java并未提供对任何对象恒定不变的支持。这一限制也通用适用于数组，它也是对象。
    
    package com.bupt.java.test;
    
    import java.util.Random;
    
    class Value{
        int i;
        final int j;
        public Value( int i , int j){
            this.i = i;
            this.j = j;
        }
    }
    
    /*
     * 对基本类型使用final不能改变的是它的数值
     * 而对于对象引用，不能改变的是它的引用，而对象本身是可以修改的
     * 一旦一个final引用被初始化只想一个对象，这个引用不能再指向其他对象
     * */
    public class FinalData {
        private static Random rand = new Random(47);
        private String id;
        public FinalData( String id){
            this.id = id;
        }
        
        //编译时常量,根据惯例，既是static又是final的域将大写表示
        private final int valueOne = 9;
        private static final int VALUE_TWO = 99;
        
        //典型的公共常量
        public static final int VALUE_THREE = 39;
        
        //运行时常量
        private final int i4 = rand.nextInt(20);
        static final int INT_5 = rand.nextInt(20);
        private Value v1 = new Value(11,22);
        private final Value v2 = new Value(22,33);
        private static final Value VAL_3 = new Value(33,44);
        
        //数组Arrays
        private final int[] a = {1,2,3,4,5,6};
        
        public String toString(){
            return id + ": " + "i4 = " + i4 + ", INT_5 = " + INT_5;
        }
        
        public static void main(String[] args){
            FinalData fd1 = new FinalData("fd1");
            //fd1.valueOne++; //不能对终态字段 FinalData.valueOne 赋值
            fd1.v2.i++;
    //        System.out.println(fd1.v2.i);
            //fd1.v2.j++; //不能对终态字段 Value.j 赋值
            fd1.v1 = new Value(12,23); // ok -- not final
            
            for ( int i = 0 ; i < fd1.a.length ; i++ ) {
                fd1.a[i]++; //Object isn't constant!
            }
            //fd1.v2 = new Value(23,34);//不能对终态字段 FinalData.v2 赋值
            //fd1.VAL_3 = new Value(34,45); //不能对终态字段 FinalData.VAL_3 赋值
            //fd1.a = new int[3];//不能对终态字段 FinalData.a 赋值
            System.out.println(fd1);
            System.out.println("Creating new FinalData");
            FinalData fd2 = new FinalData("fd2");
            System.out.println(fd1);
            System.out.println(fd2);
        }
    }
    
    运行结果：
    
    fd1: i4 = 15, INT_5 = 18
    Creating new FinalData
    fd1: i4 = 15, INT_5 = 18
    fd2: i4 = 13, INT_5 = 18
    由于valueOne和VALUE_TOW都是带有编译时数字的final基本类型，所以它们二者均可以用作编译期常量，并且没有重大区别。
    
    VALUE_THREE是一种更加典型的对常量进行定义的方式：定义为public，可以被任何人访问；定义为static，则强调只有一份；定义为final，说明它是个常量。请注意被static和final修饰的变量全用大写字母命名，并且之间用下划线隔开。
    
    我们不能因为某些数据是final的就认为在编译时可以知道它的值。在运行时使用随机数来初始化i4和INT5时说明这一点。实例中fd1和fd2中i4分别被初始化为15和13，INT_5的值是不可以通过创建第二个FinalData实例加以改变的，这是因为INT_5是被static修饰的。
    
    空白final
    java也许生成“空白final”，所谓空白final是指被声明为final但又未给初值的域。无论什么情况下编译器都会保证final域在使用前初始化。但空白final在final的使用上提供了很大的灵活性，为此，一个final域可以根据某些对象有所不同，却又保持恒定不变的特性。
    
    
    package com.bupt.java.test;
    
    class Poppet{
        private int i;
        public Poppet(int i){
            this.i = i;
        }
        public int getI() {
            return i;
        }
        public void setI(int i) {
            this.i = i;
        }
        
        
    }
    public class BlankFinal {
        private final int i = 0; //Initialized final
        private  final int j; //blank final
        private final Poppet p; // blank final reference
        
        public BlankFinal(){
            j = 1;
            p = new Poppet(1);
        }
        
        public BlankFinal( int x) {
            j = x;
            p = new Poppet(x);
        }
        
        public static void main(String[] args){
            BlankFinal b1 = new BlankFinal();
            BlankFinal b2 = new BlankFinal(47);
            
            System.out.println("b1.j="+b1.j+"\t\t b1.p.i="+b1.p.getI());
            System.out.println("b2.j="+b2.j+"\t\t b2.p.i="+b2.p.getI());
        }
    }
    
    运行结果：
    
    b1.j=1         b1.p.i=1
    b2.j=47         b2.p.i=47
    final参数
    java中也会将参数列表中的参数以声明的方式指明为final。这意味着你无法改变参数所指向的对象。例如：
    
    
    package com.bupt.java.test;
    
    class Gizmo{
        public void spin(String temp){
            System.out.println(temp+" Method call Gizmo.spin()");
        }
    }
    public class FinalArguments {
        void with(final Gizmo g){
            //g = new Gizmo(); //不能对终态局部变量 g 赋值。它必须为空白，并且不使用复合赋值
        }
        
        void without(Gizmo g) {
            g = new Gizmo();
            g.spin("without");
        }
        
    //    void f(final int i){
    //        i++;
    //    }不能对终态局部变量 i 赋值。它必须为空白，并且不使用复合赋值
        
        int g(final int i){
            return i + 1;
        }
        public static void main(String[] args){
            FinalArguments bf = new FinalArguments();
            bf.without(null);
            bf.with(null);
            System.out.println("bf.g(10)="+bf.g(10));
        }
    }
    
    运行结果：
    
    without Method call Gizmo.spin()
    bf.g(10)=11
    使用final方法有两个原因。第一个原因是把方法锁定，以防止任何继承它的类修改它的含义。这是出于设计的考虑：想要确保在继承中使用的方法保持不变，并且不会被覆盖。过去建议使用final方法的第二个原因是效率。在java的早期实现中，如果将一个方法指明为final，就是同意编译器将针对该方法的所有调用都转为内嵌调用。当编译器发现一个final方法调用命令时，它会根据自己的谨慎判断，跳过插入程序代码这种正常的调用方式而执行方法调用机制（将参数压入栈，跳至方法代码处执行，然后跳回并清理栈中的参数，处理返回值），并且以方法体中的实际代码的副本来代替方法调用。这将消除方法调用的开销。当然，如果一个方法很大，你的程序代码会膨胀，因而可能看不到内嵌所带来的性能上的提高，因为所带来的性能会花费于方法内的时间量而被缩减。在最近的java版本中，虚拟机(特别是hotspot技术)可以探测到这些情况，并优化去掉这些效率反而降低的额外的内嵌调用，因此不再需要使用final方法来进行优化了。事实上，这种做法正逐渐受到劝阻。在使用java se5/6时，应该让编译器和JVM去处理效率问题，只有在想明确禁止覆盖式，才将方法设置为final的。

#### final和private关键字
    类中的所有private方法都是隐式的制定为final的。由于你无法访问private方法，你也就无法覆盖它。可以对private方法添加final修饰词，但这是毫无意义的。例如：
    
    
    package com.bupt.java.test;
    
    class WithFinals{
        private final void f(){
            System.out.println("WithFinals.f()");
        }
        
        private final void g(){
            System.out.println("WithFinals.g()");
        }
    }
    
    class OverridingPrivate extends WithFinals {
          private final void f() {
            System.out.println("OverridingPrivate.f()");
          }
          private void g() {
            System.out.println("OverridingPrivate.g()");
          }
        }
        class OverridingPrivate2 extends OverridingPrivate {
          public final void f() {
            System.out.println("OverridingPrivate2.f()");
          }
          public void g() {
            System.out.println("OverridingPrivate2.g()");
          }
        }
        
    public class OverrideFinal {
        public static void main(String[] args){
            WithFinals w1 = new WithFinals();
    //        w1.f(); //类型 WithFinals 中的方法 f（）不可视,无法访问私有方法
    //        w1.g();//类型 WithFinals 中的方法 g（）不可视,无法访问私有方法
            OverridingPrivate w2 = new OverridingPrivate();
    //        w2.f();
    //        w2.g();
            OverridingPrivate2 w3 = new OverridingPrivate2();
            w3.f();
            w3.g();
        }
    }
    
    运行结果：
    
    OverridingPrivate2.f()
    OverridingPrivate2.g()
#### final类

    当将类定义为final时，就表明你不打算继承该类，而且也不允许别人这么做。换句话说，出于某种考虑，你对该类的设计不需要做任何改动，或者出于安全的考虑，你不希望他有子类：
    

    package com.bupt.java.test;
    
    class SmallBrain{
        
    }
    final class Dinosaur{
        int i = 7;
        int j = 1;
        SmallBrain x = new SmallBrain();
        void f(){
            System.out.println("Dinosaur.f()");
        }
    }
    //class a extends Dinosaur{
    //    
    //}类型 a 不能成为终态类 Dinosaur 的子类
    public class Jurassic {
        public static void main(String[] args){
            Dinosaur n = new Dinosaur();
            n.f();
            n.i = 40;
            n.j++;
            System.out.println("n.i="+n.i);
            System.out.println("n.j="+n.j);
        }
    }
    复制代码
    运行结果：
    
    Dinosaur.f()
    n.i=40
    n.j=2
    由于final是无法继承的，所以被final修饰的类中的发你过分都隐式的制定为final，以为你无法覆盖他们。在final类中可以给方法添加final，但这不会产生任何意义。

#### 总结

    一个编译时恒定不变的常量
    一个在运行时初始化，而你不希望它被改变的变量
    
    final类不能被继承，没有子类，final类中的方法默认是final的，但是final类中的成员变量默认不是final的。
    final方法不能被子类覆盖，但可以被继承。
    final成员变量表示常量，只能被赋值一次，赋值后值不再改变。
    final不能用于修饰构造方法。

参考: https://www.cnblogs.com/fangpengchengbupter/p/7858510.html