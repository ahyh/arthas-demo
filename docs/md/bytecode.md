# ByteCode

## 虚拟机栈

每个JVM线程都有一个自己私有的Java虚拟机栈（JVM stack），是与线程同时创建的，用于存储栈帧，可以理解为就是存储局部变量与一些尚未算好的结果，在方法的调用与返回中也起到很重要的作用，调用一个方法就会创建一个栈帧（特殊优化的除外），方法结束就会销毁栈帧



## 栈帧

栈帧（frame）是存储数据和部分过程结果的数据结构，同时也用来处理动态链接、方法返回值和异常分派，是支持JVM进行方法调用和执行的数据结构，随着方法的调用被创建，随着方法的结束而销毁，每个栈帧都有自己的本地变量表（Local varible）、操作数栈（operand stack）和指向当前方法所属的类的运行时常量池的引用

- 局部变量表：是一个大小在编译期就确定下来的一个变量列表，存储在class文件中，大小就是方法Code属性的max_locals；一个局部变量可以保存一个类型为boolean/byte/char/short/int/float/reference或returnAddress的数据，两个局部变量可以保存一个类型为long/double的数据；局部变量表通过索引来进行定位访问，0<index<max_locals
- 操作数栈：是一个后进先出（Last-In-First-Out）栈，操作数栈的最大深度游编译期决定，对应方法Code属性的max_stack；栈帧刚开始创建的时候，操作数栈是空的，JVM提供了一些字节码指令来从局部变量表或者对象实例的字段中复制常量或者变量值到操作数栈中，也提供了一些指令用于从操作数栈中取走数据、操作数据以及把操作结果重新入栈，在调用方法时，操作数栈也用来准备调用方法的参数以及接收方法的返回结果



## 字节码指令

### 2.1- 加载和存储指令

分为load类，store类，常量加载类

1- load类：将局部变量表中变量加载到操作数栈，根据不同的数据变量类型还有lload、fload、dload、aload指令，分别表示加载局部变量表中long、float、double、引用类型的变量

2- store类：将栈顶的元素保存到局部变量表中，比如istore_0将操作数栈顶元素存储到局部变量表中下标为0的位置且要求这个位置的元素类型为int，根据不同的数据变量类型还有lstore、fstore、dstore、astore这些指令

3- 常量加载相关指令，常见的有const类、push类、ldc类，const、push类指令时将常量值直接加载到操作数栈顶，比如iconst_0是将整数0加载到操作数栈上，bipush 100是将int类型常量100加载到操作数栈上，ldc指令是从常量池中加载对应的常量到操作数栈顶，比如ldc #10是将常量池冲下标为10的常量数据加载到操作数栈上

| 指令名          | 描述                                       |
| ------------ | ---------------------------------------- |
| aconst_null  | 将null入栈到栈顶                               |
| iconst_m1    | 将int类型值-1压入栈顶                            |
| iconst_<n>   | 将int类型值n(0-5)压入栈顶，比如iconst_0是将0压入栈顶      |
| lconst_<n>   | 将long类型值n(0-1)压入栈顶                       |
| fconst_<n>   | 将float类型值n(0-2)压入栈顶                      |
| dconst_<n>   | 将double类型值n(0-1)压入栈顶                     |
| bipush       | 将范围在-128 ~ 127的int类型值压入栈顶                |
| sipush       | 将范围在-32768 ~ 32767的int类型值压入栈顶            |
| ldc          | 将int、float、String类型的常量值从常量池中压入栈顶, 比如ldc #10 |
| ldc_w        | 作用同ldc，ldc的操作码是一个字节，ldc_w的操作码是两个字节，ldc只能寻址255个常量池索引，ldc_w可以寻址2个字节长度，可覆盖常量池所有的值 |
| ldc2_w       | ldc2_w是将long / double类型常量从常量池中压入栈顶，寻址范围也是2个字节 |
| <T>load      | 将局部变量表中指定位置的类型为T的变量加载到栈上，T可以是i、l、f、d、a，分表表示int、long、float、double、引用类型 |
| <T>load_n    | 将局部变量表中下标为n(0-3)的类型为T的变量加载到栈上            |
| <T>aload     | 将指定数组中特定位置的类型为T的变量加载到栈上，T可以是i、l、f、d、a、b、c、s，分表表示int、long、float、double、引用类型、boolean或者byte、char、short |
| <T>store     | 将栈顶类型为T的数据存储到局部变量表的指定位置，T可以是i、l、f、d、a    |
| <T>store_<n> | 将栈顶类型为T的数据存储到局部变量表下标为n(0-3)的位置，T可以是i、l、f、d、a |
| <T>astore    | 将栈顶类型为T的数据存储到数组的指定位置，T可以是i、l、f、d、a、b、c、s，分表表示int、long、float、double、引用类型、boolean或者byte、char、short |
| <T>return    | 从方法中返回T类型数据，T可以是i、l、f、d、a，分表表示返回一个int、long、float、double、引用类型数据; T也可以是空，表示返回void类型 |

Tips：加载int类型的常量分为不同类型，比如可以用iconst_<n>加载0-5的int类型常量，iconst_m1加载-1, bipush <n>加载-128 ~ 127的int类型常量，sipush加载-32768 ~ 32767的int类型常量，原因是这样设计可以使字节码更紧凑，iconst_<n>指令只占用一个字节，bipush指令操作码和操作数在一起占用2个字节，sipush指令操作码和操作数一起占用3个字节



### 2.2- 操作数栈指令

常用的操作数栈指令由pop、dup和swap

| 指令名     | 描述                                |
| ------- | --------------------------------- |
| pop     | 将栈顶元素出栈，非long/double              |
| pop2    | 弹出栈顶一个long/double类型的数据或者两个其他类型的数据 |
| dup     | 复制栈顶数据并将复制的数据压入栈顶                 |
| dup_x1  | 复制栈顶数据并将复制的数据压入栈顶第二个元素之下          |
| dup_x2  | 复制栈顶数据并将复制的数据压入栈顶第三个元素之下          |
| dup2    | 复制栈顶两个数据并将复制的数据入栈                 |
| dup2_x1 | 复制栈顶两个数据并将复制的数据插入到栈第二个元素之下        |
| dup2_x2 | 复制栈顶两个数据并将复制的数据插入到栈第三个元素之下        |
| swap    | 交换栈顶两个元素                          |



### 2.3- 运算和类型转换指令

#### 运算指令

| Operator  | int  | long | float | double |
| --------- | ---- | ---- | ----- | ------ |
| +         | iadd | ladd | fadd  | dadd   |
| -         | isub | lsub | fsub  | dsub   |
| /         | idiv | ldiv | fdiv  | ddiv   |
| *         | imul | lmul | fmul  | dmul   |
| %         | irem | lrem | frem  | drem   |
| negate(-) | ineg | lneg | fneg  | dneg   |
| &         | iand | land | -     | -      |
| \|        | ior  | lor  | -     | -      |
| ^         | ixor | lxor |       |        |

#### 转换指令

| type   | int  | long | float | double | byte | char | short |
| ------ | ---- | ---- | ----- | ------ | ---- | ---- | ----- |
| int    | -    | i2l  | i2f   | i2d    | i2b  | i2c  | i2s   |
| long   | l2i  | -    | l2f   | l2d    | -    | -    | -     |
| float  | f2i  | f2l  | -     | f2d    | -    | -    | -     |
| double | d2i  | d2l  | d2f   | -      | -    | -    | -     |

宽化类型转换（widening）: 将数据转换为范围更大的数据类型，比如int转换为long

窄化类型转换（narrowing）: 将大范围数据类型转化为小范围数据类型，比如long转换为int，会丧失精度



### 2.4- 控制转移指令

控制转移指令用于有条件和无条件的分支跳转，常见的if-then-else、三目表达式、for循环、异常处理都是控制转移指令

条件转移：ifeq、iflt、ifle、ifne、ifgt、ifnull、ifnonnull、if_icmpeq、if_icmpne、if_icmplt、if_icmpgt、if_icmple、if_icmpge、if_acmpeq和if_acmpne

复合条件转移：tableswitch、lookupswitch

无条件转移：goto、goto_w、jsr、jsr_w、ret

ifle <n>: 将操作数栈顶元素出栈跟0进行比较，如果小于等于0则跳转到<n>行字节码处执行，如果大于0则继续执行接下来的字节码

| 指令名          | 描述                       |
| ------------ | ------------------------ |
| ifeq         | 如果栈顶int型变量等于0，则跳转        |
| ifne         | 如果栈顶int型变量不等于0，则跳转       |
| iflt         | 如果栈顶int型变量小于0，则跳转        |
| ifge         | 如果栈顶int型变量大于等于0，则跳转      |
| ifgt         | 如果栈顶int型变量大于0，则跳转        |
| ifle         | 如果栈顶int型变量小于等于0，则跳转      |
| if_icmpeq    | 比较栈顶两个int型变量，相等则跳转       |
| if_icmpne    | 比较栈顶两个int型变量，不等则跳转       |
| if_icmplt    | 比较栈顶两个int型变量，小于则跳转       |
| if_icmpge    | 比较栈顶两个int型变量，大于等于则跳转     |
| if_icmpgt    | 比较栈顶两个int型变量，大于则跳转       |
| if_icmple    | 比较栈顶两个int型变量，小于等于则跳转     |
| if_acmpeq    | 比较栈顶两个引用类型变量，相等则跳转       |
| if_acmpne    | 比较栈顶两个引用类型变量，不等则跳转       |
| goto         | 无条件跳转                    |
| tableswitch  | switch条件跳转，case值紧凑的情况下使用 |
| lookupswitch | switch条件跳转，case值稀疏的情况下使用 |



### 2.5- 对象相关的字节码指令

<init>: <init>方法是对象初始化方法，类的构造方法、非静态变量的初始化、对象初始化代码块都被被编译进这个方法，这个不是JVM指令

new 、 dup、invokespecial对象创建三条指令：创建一个对象需要三条指令，new 、dup、<init>方法的invokespecial调用，调用new指令时，只是创建一个类实例引用，将这个引用压入操作数栈顶，此时还没有调用初始化方法，然后调用dup指令，复制栈顶元素并压栈，invokespecial指令调用<init>方法用会消耗掉栈顶元素，因为dup复制一个栈顶元素的步骤，<init>方法执行之后栈顶仍是创建的对象的引用

<clinit>方法：类的静态初始化方法，类的静态初始化快、静态变量初始化都被编译进这个方法中，这个方法不会直接被调用，它在4个指令触发时被调用(new / getstatic / putstatic / invokestatic), 对应的java代码：

1- 创建类对象的实例，比如new、反射、反序列化

2- 访问类的静态变量或者静态方法

3- 访问类的静态字段或者对静态字段赋值

4- 初始化某个类的子类



### 2.6- 方法调用指令

invokestatic：用于调用静态方法

invokespecial：用于调用私有实例方法、构造器方法以及使用super关键字调用父类的实例方法

invokevirtual：用于调用非私有实例方法

invokeinterface：用于调用接口方法

invokedynamic：用于调用动态方法

| 指令              | 描述                                       |
| --------------- | ---------------------------------------- |
| invokestatic    | 调用的方法编译期确定，运行期不会变化，静态绑定，不需要将对象加载到操作数栈，只需要将所需要的的参数入栈就可以执行invokestatic指令了 |
| invokespecial   | 1- 调用<init>方法2- 调用private修饰的私有实例方法3- 调用使用super关键字调用的父类方法 |
| invokevirtual   | 调用普通的实例方法，调用的方法在运行期才能根据对象实例的类型确定，在编译期无法确定，因为普通实例方法是可以重写的指令执行之前，需要将对象引用、方法参数入栈，调用结束对象引用、方法参数都会出栈，如果方法有返回值那会将返回值压入栈顶 |
| invokeinterface | 调用接口方法，也需要在运行期才能确定目标方法JVM提供了itable(interface method table)的结构来支持多接口实现，itable由偏移量表（offset table）和方法表（method table）两部分组成在调用某个接口方法时，JVM先在itable的offset table中查找到对应的方法表位置和方法位置，然后再method table中查找具体的方法实现，然后执行 |
| invokedynamic   | 1- JVM首次执行invokedynamic指令时会调用引导方法（Bootstrap Method）2- 引导方法返回一个CallSite对象，CallSite内部根据方法签名进行目标方法查找，getTarget方法返回方法句柄（MethodHandle）对象3- 在CallSite没有变化的情况下，MethodHandle可以一直被调用，如果CallSite有变化，重新查找即可 |



### 2.7- 数组相关指令

| 指令             | 描述                         |
| -------------- | -------------------------- |
| anewarray      | 从数据中加载一个reference类型数据到操作数栈 |
| arraylength    | 取数组长度                      |
| multianewarray | 创建一个新的多维数组                 |
| newarray       | 创建一个新数组                    |



### 2.8- 其他指令

| 指令           | 描述             |
| ------------ | -------------- |
| athrow       | 抛出一个异常或者错误     |
| checkcast    | 检查对象是否符合给定的类型  |
| getfield     | 获取对象的字段值       |
| putfield     | 设置对象的字段值       |
| getstatic    | 获取类的静态字段值      |
| putstatic    | 设置类的静态字段值      |
| instanceof   | 判断对象是否是指定的类型   |
| monitorenter | 进入一个对象的monitor |
| monitorexit  | 退出一个对象的monitor |