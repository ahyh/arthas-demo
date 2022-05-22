# Java Instrumentation机制与Attach API

Arthas动态替换正在运行中的JVM已经加载的class文件，就是基于Java Instrumentation机制，Attach API就是Java Instrumentation机制的一种实现方式

Java Instrumentation是JVMTI的一部分（JVM Tool Interface），官网地址：<https://docs.oracle.com/javase/8/docs/platform/jvmti/jvmti.html>

```
The JVM Tool Interface (JVM TI) is a programming interface used by development and monitoring tools. It provides both a way to inspect the state and to control the execution of applications running in the Java virtual machine (VM).
```

JVMTI是一套编程接口，用于开发和监控的工具集，提供了一种检查JVM状态和控制程序执行的方法

Instrumentation机制，在JVM Tool Interface官方文档上称为Bytecode Instrumentation，中文名就是字节码插桩技术，是一种从JDK1.5起引入的字节码增强技术。

从JDK 1.5开始，引入了java.lang.instrument包，方便开发者实现字节码增强，这里的增强就是我们常说的Java的Instrumentation机制，翻译过来就是字节码插桩技术。这个技术的核心功能包括注册类文件转换器、获取已加载的类、允许对已加载的和未加载的类进行修改，从而实现JVM级别的AOP、性能监控（APM）功能。很多APM产品（Pinpoint & Skywalking）都是基于这个技术实现的，Arthas也是基于这个技术实现的。



## Instrumentation

Java的Instrumentation的核心功能都是由一个核心接口（java.lang.instrument.Instrumentation）来提供的

```
/**
 <P>
 * There are two ways to obtain an instance of the
 * <code>Instrumentation</code> interface:
 *
 * <ol>
 *   <li><p> When a JVM is launched in a way that indicates an agent
 *     class. In that case an <code>Instrumentation</code> instance
 *     is passed to the <code>premain</code> method of the agent class.
 *     </p></li>
 *   <li><p> When a JVM provides a mechanism to start agents sometime
 *     after the JVM is launched. In that case an <code>Instrumentation</code>
 *     instance is passed to the <code>agentmain</code> method of the
 *     agent code. </p> </li>
 * </ol>
*/
public interface Instrumentation {
   
    void addTransformer(ClassFileTransformer transformer, boolean canRetransform);

    void addTransformer(ClassFileTransformer transformer);

    boolean removeTransformer(ClassFileTransformer transformer);

    void retransformClasses(Class<?>... classes) throws UnmodifiableClassException;

    boolean isRedefineClassesSupported();

    void redefineClasses(ClassDefinition... definitions) throws  ClassNotFoundException, UnmodifiableClassException;
    
    Class[] getAllLoadedClasses();
  
    Class[] getInitiatedClasses(ClassLoader loader);

    long getObjectSize(Object objectToSize);

    void appendToBootstrapClassLoaderSearch(JarFile jarfile);

    void appendToSystemClassLoaderSearch(JarFile jarfile);

    boolean isNativeMethodPrefixSupported();
    
    void setNativeMethodPrefix(ClassFileTransformer transformer, String prefix);
}
```

从类的注释中，可知，有两种方法来使用Instrumentation技术

- 在JVM启动的时候通过agent class的方式指定一个Instrument的实例，也就是在JVM启动的时候指定Agent jar包，这种方式称为静态Instrumentaion
- 在JVM启动后的任意时刻，动态的加载Agent jar包，这种方式是通过attach API远程加载Agent jar来实现的，这种方式称为动态Instrumentation

在Instrumentation接口中，核心方法有addTransformer，retransformClasses，

- addTransformer方法给Instrumentation注册一个类型为ClassFileTransformer的类文件转换器

```
  public interface ClassFileTransformer { 
   byte[] transform(  ClassLoader         loader,
                      String              className,
                      Class<?>            classBeingRedefined,
                      ProtectionDomain    protectionDomain,
                      byte[]              classfileBuffer)
          throws IllegalClassFormatException;
}
```

className表示当前加载类的类名，classfileBuffer表示待加载类文件的字节数组，调用addTransformer方法注册ClassFileTransformer后，后续JVM加载的所有类都被被transform方法拦截，这个方法接收源class文件的字节数组，输出经过改写后的字节数组，从而完成更改字节码的功能

- retransformClasses方法对JVM已经加载的类重新触发类加载，这个方法是支持动态加载Agent jar的基石



### JVM Attach API

从JDK1.6开始引入的动态Attach Agent方案，可以在JVM启动后的任意时刻通过Attach API远程加载Agent jar包，动态修改已经加载的class的文件的字节码，Arthas就是使用这种方式来实现的

### 使用案例

#### step-0: 启动一个目标JVM进程

```
public class TestDemo {

    public static void main(String[] args) throws Exception{
        while(true){
            Thread.sleep(5000);
            testMethod();
        }
    }

    public static void testMethod(){
        System.out.println("test method");
    }
}
```

不停的循环，每隔5s输出test method



#### step-1: 自定义ClassFileTransformer

```
public class MyAttachTransformer implements ClassFileTransformer {

    /**
     * 如果是testMethod方法，就通过改写字节码的方式改写此方法
     * 改写后的内容：System.out.println("update testMethod");
     */
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        ClassReader classReader = new ClassReader(classfileBuffer);
        ClassNode classNode = new ClassNode(Opcodes.ASM5);
        classReader.accept(classNode, ClassReader.SKIP_FRAMES);
        for (MethodNode methodNode : classNode.methods) {
            if ("testMethod".equals(methodNode.name)) {
                InsnList insnList = new InsnList();
                insnList.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
                insnList.add(new LdcInsnNode("update testMethod"));
                insnList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false));
                methodNode.instructions.insert(insnList);
                break;
            }
        }
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }
}
```

拦截testMethod方法，把方法体的内容更新为System.out.println("update testMethod");



#### step-2: 编写Agent类，实现agentmain方法

```
public class MyAttachAgent {

    /**
     * 调用instrumentation的addTransformer以及retransformClasses完成字节码的改写
     */
    public static void agentmain(String agentArgs, Instrumentation instrumentation) {
        System.out.println("Loading attach agent......");
        ClassFileTransformer myAttachTransformer = new MyAttachTransformer();
        instrumentation.addTransformer(myAttachTransformer, true);
        if (instrumentation.isRedefineClassesSupported()) {
            Class[] allLoadedClasses = instrumentation.getAllLoadedClasses();
            for (Class<?> clazz : allLoadedClasses) {
                if (clazz.getName().contains("TestDemo")) {
                    try {
                        instrumentation.retransformClasses(clazz);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
```

这里的方法名和入参是固定的，在agentmain方法中注册自定义的ClassFileTransfomer，然后调用Instrumentation.retransformClasses触发类的重新加载



#### step-3: 启动Attach端，更新目标JVM的目标方法字节码

```
public class AttachTest {

    /**
     * 调用attach API，connect到目标JVM，然后loadAgent来动态加载agent jar包从而完成更新运行中的JVM已加载class
     * 字节码的功能
     */
    public static void main(String[] args) throws Exception {
        // 每次运行之前都要通过jps获取到pid
        VirtualMachine virtualMachine = VirtualMachine.attach("2128");
        // TimeUnit.SECONDS.sleep(10);
        // 需要替换成自己的jar地址
        virtualMachine.loadAgent("D:\\myCode\\base-boot\\byte-jvm\\target\\byte-jvm-jar-with-dependencies.jar");
    }

}
```

需要指定目标JVM的进程id，然后通过loadAgent方法触发agent jar的加载

上述例子就是在目标JVM进程不停止的情况下，通过attach API动态的加载Agent jar完成class文件改写的功能，可以方法TestDemo输出的内容发生了变化，说明完成了动态更新



### Attach API工作流程图

见attach API.drawio

这个流程图描述了Linux环境下Attach API使用域套接字来完成Attach端与目标JVM之间的进程间通信

首先JVM Attach API的底层实现原理在Windows和Linux不同的操作系统下，实现方式是不一样的

JVM Attach API的实现是基于信号和UNIX与套接字来实现的，信号是某事件发生时对进程的通知机制，也被称为软件中断，也可以看做为轻量级的进程间通信，信号由一个进程发送给另一个进程，只不过经由内核做中间人转发；域套接字（Domain Socket）可以实现同一个主机上的进程间通信，相对于通过127.0.0.1环回地址进行通信域套接字可靠性更高、效率更好，相对于普通的Socket，域套接字不用进行协议处理，也不需要进行发送确认报文，只需要读写数据即可，可以这么理解，两个需要通信的进程共同读写一个.sock文件来完成通信

可以查看Linux版本的jdk，LinuxAttachProvider类，主要是attachVirtualMachine方法，里面有findSocketFile以及createAttachFile方法，实现了attach过程中的文件创建；sendQuitTo方法实现了发送中断信号给目标JVM

目标JVM在启动的时候，会起一个线程来监听中断信号，在hotspot/os/share/runtime/os.cpp中的signal_thread_entry方法，AixAttachListener则实现了监听的逻辑