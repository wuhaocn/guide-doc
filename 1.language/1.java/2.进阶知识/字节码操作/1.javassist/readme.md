### 前言

    Javassist是日本人开发的一款编辑class字节码框架，可以用来检查、动态修改及创建Java类。与JDK自带的反射功能相比Javassist功能更加强大，熟练使用Javassist工具对提高Java动态编程有着重要意义。
    应用：
    javassist对字节码操作为JBoss实现动态"AOP"框架
    dubbo-common使用javassist

### 常用类

    ClassPool：javassist的类池，使用ClassPool 类可以跟踪和控制所操作的类,它的工作方式与 JVM 类装载器非常相似。 
    CtClass：CtClass提供了检查类数据（如字段和方法）以及在类中添加新字段、方法和构造函数、以及改变类、父类和接口的方法。 
    CtField：代表类中的字段 
    CtMethod ：代表类中方法，可以在方法前后添加源码，也可以添加try..catch 
    CtConstructor：代表当前类的构造函数 
    ClassFile: 代表当前加载类的类文件，里面可以查看.class文件的内部数据

### 创建类

    在Javassist中要操作类需要首先创建ClassPool，所有被加载或创建的类都会被保存在对象里，它可以像Java中的classLoader一样多个Pool连接起来形成父子关系。这里需要注意的是Javassist类池中保存的类不能够重复加载，而且这些类并不是生成对象真正的类，它们代表Compile Time Class也就是编译时的类，CtClass等类前面的Ct正是编译时的意思。

    直接创建一个编译时类使用pool.makeClass方法，如果要加载已经存在的类使用get方法，之后再为类增加CtField、CtMethod、CtConstructor等类元素。注意在定义这些元素的时候需要传入声明的类，定义之后还要调用CtClass.addXXX方法才能将元素加入到类中。

    ClassPool pool = ClassPool.getDefault();
    CtClass driver = pool.makeClass("com.test.Driver");
    CtClass string = pool.get("java.lang.String");
    CtField name = new CtField(string, "name", driver);
    name.setModifiers(Modifier.PRIVATE);
    driver.addField(name);
    
    CtField age = new CtField(CtClass.intType, "age", driver);
    age.setModifiers(Modifier.PUBLIC);
    driver.addField(age);
    
    CtMethod getName = CtNewMethod.getter("getName", name);
    driver.addMethod(getName);
    CtMethod setName = CtNewMethod.setter("setName", name);
    driver.addMethod(setName);
    
    
    CtMethod print = CtNewMethod.make("public void print() { System.out.println(name); }", driver);
    driver.addMethod(print);
    
    CtConstructor constructor = new CtConstructor(new CtClass[] { string }, driver);
    constructor.setModifiers(Modifier.PUBLIC);
    constructor.setBody("{this.name = $1;}");
    driver.addConstructor(constructor);

    上面的CtNewMethod是一个工具类提供了各种创建或复制CtMethod的静态方法，只要传入方法的Java实现代码即可，Javassist会自动将这些代码编译成JVM指令。最后的CtConstructor的设置方法体里面使用了$1这个占位符。
    
    $0, $1, $2：$0代码的是this，$1代表方法参数的第一个参数、$2代表方法参数的第二个参数,以此类推，$N代表是方法参数的第N个 
    $args ：$args 指的是方法所有参数的数组类似Object[]，需要注意$args[0]对应的是$1,而不是$0 
    $$：$$是所有方法参数的简写，主要用在方法调用上，相当于（$1，$2） 
    $cflow：一个方法调用的深度 
    $r：指的是方法返回值的类型，主要用在类型的转型上 
    $w：$w代表一个包装类型。主要用在转型上。比如：Integer i = ($w)5;如果该类型不是基本类型，则会忽略 
    $_：$_代表的是方法的返回值 
    $sig：所有请求参数类型数组 
    $type：返回结果值的类型 
    $class：$class 指的是this的类型（Class），也就是$0的类型

    对于构造函数而言第一个参数其实是this，不过在Java代码中并不会被表示出来，$1代表的就是第二个参数也就是string类型的那个参数。

### 生成类对象

    前面已经在ClassPool中生成了CtClass编译时的Driver类，只要调用writeFile就能够将内存中的编译时类保存到磁盘中的.class文件中。不过保存之后的类是禁止在做修改操作，也就是编译时类被冻结了，调用defrost方法能够解冻。为了能够生成Driver类的对象，需要将CtClass转换成Class对象，也就是JVM中运行时的类对象。这时就可以使用反射的方式设置类里的字段，调用类的方法。
    
    driver.writeFile("C:\\Users\\");
    driver.defrost();
    
    Class<?> clazz = driver.toClass();
    Constructor<?> constr = clazz.getConstructor(String.class);
    Object obj = constr.newInstance("Hello");
    Field field = clazz.getDeclaredField("name");
    field.setAccessible(true);
    System.out.println(field.get(obj));
    Field ageField = clazz.getDeclaredField("age");
    System.out.println(ageField.get(obj));
    
    Method method = clazz.getDeclaredMethod("print");
    method.invoke(obj);

    上面的示例通过运行时类获取Driver类的包含一个String参数的构造函数，之后调用构造函数创建了一个Driver对象，通过Field反射设置了age字段，最后通过Method反射调用了print函数，执行结果如下：
    
    // 打印的name字段值
    // Hello
    // 打印的age字段值
    // 0
    // 调用print打印的那么值
    // Hello

### 修改类

    前面已经生成了com.test.Driver类，现在通过Javassist来修改生成的CtClass对象，为了确保能够查找到类文件，可以像ClassPool对象添加classpath类路径，这里直接添加了类文件所在的文件系统路径，当然也可以添加网络路径从远端请求加载类。为了避免修改后的类和原来的类冲突，为新修改的类设置新的类名。
    
    ClassPool pool = ClassPool.getDefault();
    pool.insertClassPath("C://Users//");
    CtClass driver = pool.get("com.test.Driver");
    driver.setName("com.test.Person");
    CtMethod method = driver.getDeclaredMethod("print");
    CtMethod printPerson = CtNewMethod.copy(method, "printPerson", driver, null);
    printPerson.insertBefore("System.out.println(\"StartTime = \" + System.currentTimeMillis());");
    printPerson.insertAfter("System.out.println(\"EndTime = \" + System.currentTimeMillis());");
    driver.addMethod(printPerson);
    
    Class<?> clazz = driver.toClass();
    Constructor<?> constructor = clazz.getConstructor(String.class);
    Object obj = constructor.newInstance("Zhangsan");
    Method print = clazz.getMethod("printPerson");
    print.invoke(obj);


    上面的例子主要是修改CtMethod方法，这里通过调用CtMethod.insertBefore和CtMethod.insertAfter在print方法前后添加开始执行的时间，这样就可以计算当前应用执行消耗的时间。
    
    // 在打印前面添加的开始时间逻辑
    StartTime = 1530709940987
    // 打印的name
    Zhangsan
    // 在打印后面添加的结束时间逻辑
    EndTime = 1530709940987

### 修改方法源码
    
    前面的修改只是针对方法前面后面添加源码，如果希望更加强大的定制方法代码能力，需要用到CtMethod.instrument方法，可以在其中查找属性访问、方法调用、new新对象等多种操作，开发者可以使用reploce方法将原来的逻辑替换成自己的逻辑。
    
    package callsuper;
    
    // 自定义的类
    public class Student extends Person {
        private String name;
    
        public void speak() {
            name = "World";
            say();
            System.out.println(name);
        }
    }
    
    ClassPool pool = ClassPool.getDefault();
    CtClass ctClass = pool.get("callsuper.Student");
    CtMethod method = ctClass.getDeclaredMethod("speak");
    method.instrument(new ExprEditor() {
        // 当遇到访问Student的字段时替换成反射调用
        @Override
        public void edit(FieldAccess f) throws CannotCompileException {
            String fieldName = f.getFieldName();
            String className = f.getClassName();
            if (className.equals("callsuper.Student")) {
                String accessField = "java.lang.reflect.Field field = callsuper.Student.class.getDeclaredField(\"" + fieldName + "\");"
                        + "field.setAccessible(true);";
                if (f.isReader()) {
                    accessField += "$_ = ($r) field.get($0);";
                } else if (f.isWriter()) {
                    accessField += "field.set($0, $1);";
                }
                f.replace(accessField);
            }
        }
    
        // 当遇到Student方法调用时，将方法调用也替换成反射调用
        @Override
        public void edit(MethodCall m) throws CannotCompileException {
            String method = m.getMethodName();
            String className = m.getClassName();
            if (className.equals("callsuper.Student")) {
                System.out.println(method);
                String methodCall = "java.lang.reflect.Method method = callsuper.Student.class.getMethod(\"" + method +"\", null);";
                methodCall += "method.setAccessible(true);";
                methodCall += "$_ = ($r) method.invoke($0, null);";
                m.replace(methodCall);
            }
        }
    });
    ctClass.writeFile("C:\\test");

    上面例子首先获取Student旧的实现类，获取类之后查找到需要修改的类方法speak方法，在调用instrument监听FieldAccess和MethodCall两中，也就是访问Student的字段或者方法时，使用replace方法将这些访问统统都变成反射访问，输出的.class文件反编译之后如下：
    
    package callsuper;
    
    import java.io.PrintStream;
    import java.lang.reflect.AccessibleObject;
    import java.lang.reflect.Field;
    import java.lang.reflect.Method;
    
    public class Student extends Person
    {
      private String name;
    
      public void speak()
      {
        Object localObject1 = "World"; 
        Student localStudent = this; 
        Object localObject2 = Student.class.getDeclaredField("name"); ((AccessibleObject)localObject2).setAccessible(true); ((Field)localObject2).set(localStudent, localObject1);
        localStudent = this; 
        localObject2 = Student.class.getMethod("say", null); ((AccessibleObject)localObject2).setAccessible(true); 
        localObject1 = ((Method)localObject2).invoke(localStudent, null);
        localStudent = this; 
        localObject1 = null; 
        localObject2 = Student.class.getDeclaredField("name"); ((AccessibleObject)localObject2).setAccessible(true); 
        localObject1 = (String)((Field)localObject2).get(localStudent); System.out.println((String)localObject1);
      }
    }

    可以看到访问name字段和访问say方法都变成了反射调用，可见Javassist在修改class实现上功能非常强大。

### 参考
    作者：xingzhong128 
    来源：CSDN 
    原文：https://blog.csdn.net/xingzhong128/article/details/80931045 
    版权声明：本文为博主原创文章，转载请附上博文链接！