# Class文件结构简介

class文件通过编译器将java源文件编译而来，JDK中自带的javac编译器就可以完成编译动作

Java虚拟机规定用u1, u2, u4三种数据结构来表示1,2,4字节无符号整数，相同类型的若干条数据集合用表的形式来存储，表是一个变长结构，有代表长度的表头n和紧随着的n个数据项组成

## classFile

```
classFile {
    u4	magic number;	
    u2	minor_version;
    u2	major_version;
    u2	constant_pool_count;
    cp_info	constant_pool[constant_pool_count-1];
    u2	access_flag;
    u2	this_class;
    u2	super_class;
    u2	interfaces_count;
    u2	interfaces[interface_count];
    u2	field_count;
    field_info	fields[fields_count];
    u2	method_count;
    method_info	methods[methods_count];
    u2	attributes_count;
    attribute_info attributes[attributes_count];
}
```

class文件由10个部分组成：
1- 魔数（magic）——CAFEBABE
2- 版本号（minor & major version）
3- 常量池（constant pool）
4- 类访问标记（access flag）
5- 类索引（This class）
6- 父类索引（Super class）
7- 接口表索引（Interfaces）
8- 字段表（Fields）
9- 方法表（Methods）
10- 属性表（Attributes）



### magic number

占4个字节，值为0xCAFEBABE，是JVM识别.class文件的标志，虚拟机在加载类文件之前会先校验这4个字节

### minor & major version

占4个字节，分为副版本号和主版本号，版本号标识是哪个版本的Java编译出来的class文件，比如主版本号是52(0x34)，虚拟机在解析这个class文件的时候就知道这个class文件是Java8编译出来的，如果类文件的版本号高于JVM自身的版本号，则加载class文件的时候就会抛出java.lang.UnsupportedClassVersionError，Java版本与Major version对应关系如下表

| Java版本   | Major Version |
| -------- | ------------- |
| Java 1.4 | 48            |
| Java 5   | 49            |
| Java 6   | 50            |
| Java 7   | 51            |
| Java 8   | 52            |
| Java 9   | 53            |
| Java 10  | 54            |
| Java 11  | 55            |
| Java 12  | 56            |
| Java 13  | 57            |
| Java 14  | 58            |
| Java 15  | 59            |
| Java 16  | 60            |
| Java 17  | 61            |

### constant pool

```
constant pool struct {
    u2		constant_pool_count;
    cp_info	constant_pool[constant_pool_count-1];
}
```

1- constant_pool_count: 常量池大小，占2个字节，需要注意0是保留索引，真正有效的索引是从1 ~ n-1
2- cp_info，常量池项，最多包含n-1个元素（n为常量池大小），需要注意long & double类型的常量会占用两个索引位置

```
cp_info {
    u1   tag;
    u1   info[];
}
```

cp_info的第一个字节表示常量项的类型（tag），info[]表示常量项的具体内容
目前定义的常量项类型

| 类型                               | tag  |
| -------------------------------- | ---- |
| CONSTANT_Utf8_info               | 1    |
| CONSTANT_Integer_info            | 3    |
| CONSTANT_Float_info              | 4    |
| CONSTANT_Long_info               | 5    |
| CONSTANT_Double_info             | 6    |
| CONSTANT_Class_info              | 7    |
| CONSTANT_String_info             | 8    |
| CONSTANT_Fieldref_info           | 9    |
| CONSTANT_Methodref_info          | 10   |
| CONSTANT_InterfaceMethodref_info | 11   |
| CONSTANT_NameAndType_info        | 12   |
| CONSTANT_MethodHandle_info       | 15   |
| CONSTANT_MethodType_info         | 16   |
| CONSTANT_InvokeDynamic_info      | 18   |

#### CONSTANT_Integer_info & CONSTANT_Float_info

这两种类型表示4个字节的数值常量

```
CONSTANT_Integer_info {
	u1 tag; // 常量3
	u4 bytes;
}

CONSTANT_Float_info {
	u1 tag; // 常量4
	u4 bytes;
}
```

#### CONSTANT_Long_info & CONSTANT_Double_info

这两种类型表示8个字节的数值常量

```
CONSTANT_Long_info {
	u1 tag;  //常量5
	u4 high_bytes;
	u4 low_bytes;
}

CONSTANT_Double_info {
	u1 tag;  //常量6
	u4 high_bytes;
	u4 low_bytes;
}
```

#### CONSTANT_Utf8_info

这种类型存储了字符串内容

```
CONSTANT_Utf8_info {
	u1 tag;   // 常量1
	u2 length;  // 表示这个常量byte[]的长度
	u1 bytes[length];  // 字符串字节数组表示
}
```

#### CONSTANT_String_info

表示java.lang.String类型的常量对象，CONSTANT_Utf8_info存储了字符串真正的内容，而CONSTANT_String_info并不包含字符串的真正内容，仅仅包含一个指向常量池中CONSTANT_Utf8_info常量类型的索引

```
CONSTANT_String_info {
	u1 tag; // 常量8
	u2 string_index;  // 一个常量池索引，指向CONSTANT_Utf8_info类型常量
}
```

#### CONSTANT_Class_info

表示类或者接口

```
CONSTANT_Class_info {
	u1 tag;  // 常量7
	u2 name_index; // 一个常量池索引，指向CONSTANT_Utf8_info类型常量
}
```

####  CONSTANT_NameAndType_info

表示字段或者方法

```
CONSTANT_NameAndType_info {
	u1 tag;  // 常量12
	u2 name_index;  // 一个常量池索引，指向CONSTANT_Utf8_info常量，表示字段或方法名字
	u2 descriptor_index; // 一个常量池索引，指向CONSTANT_Utf8_info常量，表示字段或者方法描述符
}
```

#### CONSTANT_Fieldref_info,CONSTANT_Methodref_info,CONSTANT_InterfaceMethodref_info

这三种常量类型结构比较类似，结构如下

```
CONSTANT_Fieldref_info {
	u1 tag;  // 常量9
	u2 class_index;
	u2 name_and_type_index;
}

CONSTANT_Methodref_info {
	u1 tag;  // 常量10
	u2 class_index;
	u2 name_and_type_index;
}

CONSTANT_InterfaceMethodref_info {
	u1 tag;  // 常量11
	u2 class_index;
	u2 name_and_type_index;
}
```

class_index: 指向CONSTANT_Class_info的常量池索引值，表示Field/Method所在的类信息

name_and_type_index: 指向 CONSTANT_NameAndType_info的常量池索引值，表示Field/Method的名字和描述符

#### CONSTANT_MethodType_info，CONSTANT_MethodHandle_info， CONSTANT_InvokeDynamic_info

为支持动态语言调用，从JDK1.7开始新增的3中常量池类型

```
CONSTANT_MethodType_info {
	u1 tag;   // 常量16
	u2 descriptor_index;  // CONSTANT_Utf8_info类型索引，方法描述符
}

CONSTANT_MethodHandle_info {
	u1 tag;    // 常量15
	u1 reference_kind;  // 引用类型，1-9
	u2 reference_index; // 指向引用的索引，CONSTANT_InterfaceMethodref_info或者其他类型常量的索引
}

CONSTANT_InvokeDynamic_info {
	u1 tag;  // 常量18
	u2 bootstrap_method_attr_index;  // 指向引导方法表bootstrap_methods[]数组的索引
	u2 name_and_type_index;  //指向CONSTANT_NameAndType_info常量的索引，表示方法描述符
}
```



### Access flags

访问标记，标识一个类为final、abstract、public等访问控制信息

| 访问标记名          | 十六进制值  | 描述        |
| -------------- | ------ | --------- |
| ACC_PUBLIC     | 0x1    | 是否public  |
| ACC_FINAL      | 0x10   | 是否final   |
| ACC_SUPER      | 0x20   | 不再使用      |
| ACC_INTERFACE  | 0x200  | 是否接口      |
| ACC_ABSTRACT   | 0x400  | 是否抽象类     |
| ACC_SYNTHETIC  | 0x1000 | 是否编译器自动生成 |
| ACC_ANNOTATION | 0x2000 | 是否注解      |
| ACC_ENUM       | 0x4000 | 是否枚举      |



### this_class, super_class, interfaces

this_class表示类索引

super_class表示直接父类索引

interfaces表示类或者接口的直接父接口



### Fields

字段表，表示class中的Field信息

```
Fields {
	u2	          fields_count;
	field_info    fields[fields_count];
}

field_info {
	u2 				access_flags;
	u2 				name_index;
	u2 				descriptor_index;
	u2 				attributes_count;
	attribute_info 	 attributes[attributes_count];
}
```

access_flags: 字段访问标记

| 访问标记名         | 十六进制值  | 描述          |
| ------------- | ------ | ----------- |
| ACC_PUBLIC    | 0x1    | 是否public    |
| ACC_PRIVATE   | 0x2    | 是否private   |
| ACC_PROTECTED | 0x4    | 是否protected |
| ACC_STATIC    | 0x8    | 是否static    |
| ACC_FINAL     | 0x10   | 是否final     |
| ACC_VOLATILE  | 0x40   | 是否volatile  |
| ACC_TRANSIENT | 0x80   | 是否transient |
| ACC_SYNTHETC  | 0x1000 | 是否由编译器自动生成  |
| ACC_ENUM      | 0x4000 | 表示是一个枚举类型变量 |

name_index: 字段名，指向常量池的字符串常量

descriptor_index: 字段描述符，指向常量池的字符串常量，JVM使用更精简的信息来表示字段描述信息

| 描述符         | 类型                     |
| ----------- | ---------------------- |
| B           | byte                   |
| C           | char                   |
| D           | double                 |
| F           | float                  |
| I           | int                    |
| J           | long                   |
| S           | short                  |
| Z           | boolean                |
| L ClassName | 引用类型，“L”+对象类型的全限定名+“;” |
| [           | 一维数组                   |

attributes_count & attribute_info: 字段的属性个数和属性集合，比较常见的ConstantValue，Deprecated, RuntimeVisibleAnnotations等信息



### Methods

方法表，描述class中Method信息

```
Fields {
	u2	          methods_count;
	method_info   methods[methods_count];
}

method_info {
	u2 				access_flags;
	u2 				name_index;
	u2 				descriptor_index;
	u2 				attributes_count;
	attribute_info 	 attributes[attributes_count];
}
```

access_flags: 方法访问标记

| 方法访问标记           | 十六进制值  | 描述               |
| ---------------- | ------ | ---------------- |
| ACC_PUBLIC       | 0x1    | 是否public         |
| ACC_PRIVATE      | 0x2    | 是否private        |
| ACC_PROTECTED    | 0x4    | 是否protected      |
| ACC_STATIC       | 0x8    | 是否static         |
| ACC_FINAL        | 0x10   | 是否final          |
| ACC_SYNCHRONIZED | 0x20   | 是否synchorized    |
| ACC_BRIDGE       | 0x40   | 是否bridge，编译器生成   |
| ACC_VARARGS      | 0x80   | 是否包含可变长度参数       |
| ACC_NATIVE       | 0x100  | 是否native         |
| ACC_ABSTRACT     | 0x400  | 是否abstract       |
| ACC_STRICT       | 0x800  | 是否strict, 精确浮点计算 |
| ACC_SYNTHETIC    | 0x1000 | 这个方法编译器自动生成      |

name_index: 方法名索引值，指向常量池中的字符串常量

descriptor_index: 方法描述符，指向常量池中类型为CONSTANT_Utf8_info类型字符串常量，格式：

（参数1类型 参数2类型 ......）返回值类型

比如Object foo(int i, double d, String)的描述符为(IDLjava/lang/String;)Ljava/lang/Object;

attributes_count & attribute_info: 方法属性个数和属性集合，比如方法的字节码，异常表，方法是否被标记为Deprecated等信息，比较重要的是Code和Exceptions属性



### Attributes

class的属性表

```
Attributes {
    u2                attributes_count;             //属性个数
    attribute_info    attributes[attributes_count]; //属性项集合
}

attribute_info {
    u2    attribute_name_index;
    u4    attribute_length;
    u1    info[attribute_length];
}
```

attribute_name_index: 指向常量池的索引，根据这个索引可以得到这个attribute的名字

attribute_length: info数组长度

#### Code Attribute

```
Code_attribute {
      u2 attribute_name_index;
      u4 attribute_length;
      u2 max_stack;
      u2 max_locals;
      u4 code_length;
      u1 code[code_length];
      u2 exception_table_length;
      { 
          u2 start_pc;
          u2 end_pc;
          u2 handler_pc;
          u2 catch_type;
      } 
      exception_table[exception_table_length];
      u2 attributes_count;
      attribute_info attributes[attributes_count];
}
```

- attribute_name_index: 属性名索引，占2个字节，指向常量池中CONSTANT_Utf8_info常量，表示属性的名字，此处对应的常量池的字符串常量"Code"

- attribute_length: 属性长度，占2个字节

- max_stack: 操作数栈的最大深度，方法执行的任意期间操作数栈的深度都不会超过这个数值，计算规则：有入栈指令stack增加，有出栈指令stack减少，在整个过程中stack的最大值就是max_stack的值，增加和减少的值一般是1，但是long和double相关的指令入栈stack会加2，VOID相关指令则为0

- max_locals：局部变量表的大小，大小并不是方法中所有局部变量的数量之和，当一个局部作用域结束，它内部的局部变量占用的位置就可以被接下来的局部变量复用

- code_length: 字节码指令的长度

- code: 长度为code_length的字节数组，存储真正的字节码指令

- exception_table_length：异常表大小

- exception_table：异常表信息，try-catch语法会生成对应的异常表

  ​	start_pc：异常处理器覆盖的字节码开始位置，包含start_pc
  ​	end_pc: 异常处理器覆盖的字节码结束位置，不包含end_pc，是一个左闭右开的区间
  ​	handler_pc：表示异常处理handler在code字节数组的起始位置，异常被捕捉以后该跳转到何处继续执行
  ​	catch_type：表示需要处理的catch的异常类型，执行常量池中类型为CONSTANT_Class_info的常量项，为0时表示可以处理任意异常，可用来实现finally语义
  总结：当JVM执行到这个方法[start_pc, end_pc)范围内的字节码发生异常时，如果发生的异常时个catch_type对应的异常或者是其子类，则跳转到code字节数组handler_pc处继续执行

- attributes_count：Code相关属性个数

- attribute_info：JVM规定Code属性只能包含4种可选属性：LineNumberTable, LocalVariableTable, LocalVariableTypeTable, StackMapTable

  LineNumberTable用来存放源码行号和字节码偏移量之间的对应关系，属于调试信息

  LocalVariableTable & LocalVariableTypeTable用来存放局部变量信息，也属于调试信息

  ​

#### LineNumberTable Attribute

```
LineNumberTable_attribute {
      u2 attribute_name_index;
      u4 attribute_length;
      u2 line_number_table_length;
      { 
          u2 start_pc;
          u2 line_number;
      } 
      line_number_table[line_number_table_length];
}
```

- attribute_name_index: 属性名索引，占2个字节，指向常量池中CONSTANT_Utf8_info常量，表示属性的名字, 此处对应的常量池的字符串常量" LineNumberTable "
- attribute_length: 属性长度，占2个字节
- line_number_table_length: line_number_table数组大小
- line_number_table: 包含start_pc（字节码偏移量），line_number（源码行号）两个属性

可见class文件是可以解析出来字节码和行号对应关系的，这也是程序可以debug的原因之一