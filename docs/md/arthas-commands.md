# Arthas常用命令

官方文档：https://arthas.aliyun.com/doc/commands.html

主要关注官方文档上介绍比较少的，平常用到比较多的命令

## sc

search class的简写，查询加载到JVM中的类信息，这个命令支持的参数有 `[d]`、`[E]`、`[f]` 和 `[x:]`

| 参数名称                  | 参数说明                                     |
| --------------------- | ---------------------------------------- |
| *class-pattern*       | 类名表达式匹配                                  |
| *method-pattern*      | 方法名表达式匹配                                 |
| [d]                   | 输出当前类的详细信息，包括这个类所加载的原始文件来源、类的声明、加载的ClassLoader等详细信息。如果一个类被多个ClassLoader所加载，则会出现多次 |
| [E]                   | 开启正则表达式匹配，默认为通配符匹配                       |
| [f]                   | 输出当前类的成员变量信息（需要配合参数-d一起使用）               |
| [x:]                  | 指定输出静态变量时属性的遍历深度，默认为 0，即直接使用 `toString` 输出 |
| `[c:]`                | 指定class的 ClassLoader 的 hashcode          |
| `[classLoaderClass:]` | 指定执行表达式的 ClassLoader 的 class name        |
| `[n:]`                | 具有详细信息的匹配类的最大数量（默认为100）                  |

一般用法：sc -d {class full name}, 查询到class的类加载器的hashcode为进一步使用其他命令做准备



## watch

观测方法执行情况，能观察到的范围为：`返回值`、`抛出异常`、`入参`，通过编写 OGNL 表达式进行对应变量的查看。

| 参数名称                | 参数说明                                    |
| ------------------- | --------------------------------------- |
| *class-pattern*     | 类名表达式匹配                                 |
| *method-pattern*    | 函数名表达式匹配                                |
| *express*           | 观察表达式，默认值：`{params, target, returnObj}` |
| *condition-express* | 条件表达式                                   |
| [b]                 | 在**函数调用之前**观察                           |
| [e]                 | 在**函数异常之后**观察                           |
| [s]                 | 在**函数返回之后**观察                           |
| [f]                 | 在**函数结束之后**(正常返回和异常返回)观察                |
| [E]                 | 开启正则表达式匹配，默认为通配符匹配                      |
| [x:]                | 指定输出结果的属性遍历深度，默认为 1，最大值是4               |

监控单个方法：

```
watch com.share.arthas.demo.service.impl.UserServiceImpl update '{params,returnObj,throwExp}' -n 5 -x 3 'params[0].name.indexOf("name")> 0'
```

- 观测UserServiceImpl.update方法，查看入参，返回值，异常信息，观测5次执行情况，遍历属性深度为3，条件表达式要求第一个参数的name属性包含"name"

监控多个方法：

```
watch -E com.share.arthas.demo.service.impl.UserServiceImpl getUserById|update '{params,returnObj,throwExp}' -n 5 -x 3 '(params[0] instanceof Long and params[0]==2) || params[0].name.indexOf("yan")>-1'
```

- 观测UserServiceImpl的getUserById和update方法，查看入参，返回值，异常信息，观测5次执行情况，遍历属性深度为3，条件表达式要求第一个参数是Long型且值为2或者第一个参数的name属性包含"yan"



## trace

命令能主动搜索 `class-pattern`／`method-pattern` 对应的方法调用路径，渲染和统计整个调用链路上的所有性能开销和追踪调用链路。

| 参数名称                | 参数说明               |
| ------------------- | ------------------ |
| *class-pattern*     | 类名表达式匹配            |
| *method-pattern*    | 方法名表达式匹配           |
| *condition-express* | 条件表达式              |
| [E]                 | 开启正则表达式匹配，默认为通配符匹配 |
| `[n:]`              | 命令执行次数             |
| `#cost`             | 方法执行耗时             |

trace单个方法

```
trace com.share.arthas.demo.controller.ArthasDemoController updateUser  -n 5 --skipJDKMethod false
```

- trace ArthasDemoController的updateUser方法，需要注意trace只能跟踪一级方法的调用链路

trace多个方法

```
trace -E com.share.arthas.demo.controller.ArthasDemoController|com.share.arthas.demo.service.impl.UserServiceImpl updateUser|update|getUserById  -n 5 --skipJDKMethod false
```

- trace ArthasDemoController & UserServiceImpl的updateUser & update & getUserById方法，如果多个方法有层级关系，可以在输出中看到层级展示的trace的结果

动态trace

打开终端1，trace命令，结果里面可以看到打印出 listenerId: 1

```
trace com.share.arthas.demo.controller.ArthasDemoController updateUser  -n 5 --skipJDKMethod false
Press Q or Ctrl+C to abort.
Affect(class count: 1 , method count: 1) cost in 328 ms, listenerId: 1
`---ts=2022-05-19 00:00:46;thread_name=http-nio-8080-exec-2;id=1f;is_daemon=true;priority=5;TCCL=org.springframework.boot.web.embedded.tomcat.TomcatEmbeddedWebappClassLoader@9aa2002
    `---[7609.959ms] com.share.arthas.demo.controller.ArthasDemoController:updateUser()
        +---[0.00% 0.0203ms ] com.share.arthas.demo.model.condition.UserCondition:getId() #88
        +---[0.00% 0.0091ms ] com.share.arthas.demo.model.condition.UserCondition:getName() #92
        +---[0.05% 3.6991ms ] org.apache.commons.lang3.StringUtils:isBlank() #92
        +---[0.00% 0.0094ms ] com.share.arthas.demo.model.condition.UserCondition:getBirthday() #93
        +---[0.00% 0.0072ms ] org.apache.commons.lang3.StringUtils:isBlank() #93
        +---[0.00% min=0.0049ms,max=0.0215ms,total=0.0264ms,count=2] com.share.arthas.demo.model.condition.UserCondition:getAge() #96
        +---[99.34% 7559.4024ms ] com.share.arthas.demo.service.UserService:getUserById() #99
        +---[0.00% 0.0067ms ] com.share.arthas.demo.model.condition.UserCondition:getAge() #103
        +---[0.00% 0.0078ms ] com.share.arthas.demo.model.User:setAge() #103
        +---[0.00% 0.0069ms ] com.share.arthas.demo.model.condition.UserCondition:getName() #104
        +---[0.00% 0.0078ms ] com.share.arthas.demo.model.User:setName() #104
        `---[0.61% 46.283ms ] com.share.arthas.demo.service.UserService:update() #105
```

这个时候打开终端2，对最耗时的方法做进一步的网下层trace，通过listenerId参数就可以实现动态trace

```
trace -E com.share.arthas.demo.service.impl.UserServiceImpl update|getUserById  -n 5 --skipJDKMethod false --listenerId 1
Press Q or Ctrl+C to abort.
Affect(class count: 1 , method count: 2) cost in 59 ms, listenerId: 1
```

方法在触发，在终端1可以看到输出

```
`---ts=2022-05-19 00:03:13;thread_name=http-nio-8080-exec-5;id=22;is_daemon=true;priority=5;TCCL=org.springframework.boot.web.embedded.tomcat.TomcatEmbeddedWebappClassLoader@9aa2002
    `---[119.2922ms] com.share.arthas.demo.controller.ArthasDemoController:updateUser()
        +---[0.01% 0.0063ms ] com.share.arthas.demo.model.condition.UserCondition:getId() #88
        +---[0.00% 0.0042ms ] com.share.arthas.demo.model.condition.UserCondition:getName() #92
        +---[0.01% 0.0119ms ] org.apache.commons.lang3.StringUtils:isBlank() #92
        +---[0.00% 0.0051ms ] com.share.arthas.demo.model.condition.UserCondition:getBirthday() #93
        +---[0.00% 0.0042ms ] org.apache.commons.lang3.StringUtils:isBlank() #93
        +---[0.01% min=0.0034ms,max=0.0047ms,total=0.0081ms,count=2] com.share.arthas.demo.model.condition.UserCondition:getAge() #96
        +---[4.26% 5.0782ms ] com.share.arthas.demo.service.UserService:getUserById() #99
        |   `---[98.69% 5.0119ms ] com.share.arthas.demo.service.impl.UserServiceImpl:getUserById()
        |       +---[96.66% 4.8443ms ] com.share.arthas.demo.dao.UserMapper:getUserById() #37
        |       +---[0.32% 0.0158ms ] com.share.arthas.demo.utils.RoleUtil:getRoleType() #41
        |       +---[0.29% 0.0144ms ] com.share.arthas.demo.model.User:setRole() #42
        |       +---[0.62% 0.031ms ] com.share.arthas.demo.utils.UserUtil:getTypeStr() #44
        |       `---[0.20% 0.0102ms ] com.share.arthas.demo.model.User:setType() #45
        +---[0.00% 0.0053ms ] com.share.arthas.demo.model.condition.UserCondition:getAge() #103
        +---[0.00% 0.0048ms ] com.share.arthas.demo.model.User:setAge() #103
        +---[0.00% 0.005ms ] com.share.arthas.demo.model.condition.UserCondition:getName() #104
        +---[0.00% 0.0049ms ] com.share.arthas.demo.model.User:setName() #104
        `---[95.50% 113.926ms ] com.share.arthas.demo.service.UserService:update() #105
            `---[99.96% 113.8821ms ] com.share.arthas.demo.service.impl.UserServiceImpl:update()
                `---[99.95% 113.8196ms ] com.share.arthas.demo.dao.UserMapper:update() #73
```



## stack

输出当前方法被调用的调用路径

| 参数名称                | 参数说明               |
| ------------------- | ------------------ |
| *class-pattern*     | 类名表达式匹配            |
| *method-pattern*    | 方法名表达式匹配           |
| *condition-express* | 条件表达式              |
| [E]                 | 开启正则表达式匹配，默认为通配符匹配 |
| `[n:]`              | 执行次数限制             |

stack单个方法

```
stack com.share.arthas.demo.service.impl.UserServiceImpl update
```

- 查看UserServiceImp.update方法的执行路径

stack多个方法

```
stack -E com.share.arthas.demo.service.impl.UserServiceImpl getUserById|update
```

- 查看UserServiceImp.update & getUserById这两个方法的执行路径

stack命令也支持动态stack，用法和动态trace一样

思考：遇到过一个问题，某个请求会触发一个Service的update方法吗？怎么触发的？

1- Spring内部的Event-Listener机制

```
stack -E com.share.arthas.demo.service.impl.UserServiceImpl|com.share.arthas.demo.service.impl.UserChangeServiceImpl \
insert 'params[0].name.indexOf("yan")>-1'
```

stack UserServiceImpl和UserChangeServiceImpl的insert方法

```
ts=2022-05-19 00:30:20;thread_name=http-nio-8080-exec-1;id=1e;is_daemon=true;priority=5;TCCL=org.springframework.boot.web.embedded.tomcat.TomcatEmbeddedWebappClassLoader@9aa2002
    @com.share.arthas.demo.service.impl.UserChangeServiceImpl.insert()
        at com.share.arthas.demo.listener.AddUserListener.onAddUserGt20(AddUserListener.java:37)
        at com.share.arthas.demo.listener.AddUserListener$$FastClassBySpringCGLIB$$c78bfa16.invoke(<generated>:-1)
        
ts=2022-05-19 00:30:20;thread_name=http-nio-8080-exec-1;id=1e;is_daemon=true;priority=5;TCCL=org.springframework.boot.web.embedded.tomcat.TomcatEmbeddedWebappClassLoader@9aa2002
    @com.share.arthas.demo.service.impl.UserServiceImpl.insert()
        at com.share.arthas.demo.controller.ArthasDemoController.createUser(ArthasDemoController.java:76)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(NativeMethodAccessorImpl.java:-2)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at java.lang.reflect.Method.invoke(Method.java:498)
        at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:190)
        at org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:138)
        at org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:106)
```

相同的线程执行的，两个方法在一个线程中完成，通过event方法触发



2- Spring内部的Event-Listener机制+@Async

```
stack -E com.share.arthas.demo.service.impl.UserServiceImpl|com.share.arthas.demo.service.impl.UserChangeServiceImpl \
insert 'params[0].name.indexOf("yan")>-1'
```

stack UserServiceImpl和UserChangeServiceImpl的insert方法

```
stack -E com.share.arthas.demo.service.impl.UserServiceImpl|com.share.arthas.demo.service.impl.UserChangeServiceImpl \
> insert 'params[0].name.indexOf("yan")>-1'
Press Q or Ctrl+C to abort.
Affect(class count: 2 , method count: 2) cost in 94 ms, listenerId: 3
ts=2022-05-19 00:24:10;thread_name=task-1;id=44;is_daemon=false;priority=5;TCCL=org.springframework.boot.web.embedded.tomcat.TomcatEmbeddedWebappClassLoader@9aa2002
    @com.share.arthas.demo.service.impl.UserChangeServiceImpl.insert()
        at com.share.arthas.demo.listener.AddUserListener.onAddUserLt20Async(AddUserListener.java:54)
        at com.share.arthas.demo.listener.AddUserListener$$FastClassBySpringCGLIB$$c78bfa16.invoke(<generated>:-1)
        at org.springframework.cglib.proxy.MethodProxy.invoke(MethodProxy.java:218)
        at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.invokeJoinpoint(CglibAopProxy.java:769)
        at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163)
        at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:747)
        at org.springframework.aop.interceptor.AsyncExecutionInterceptor.lambda$invoke$0(AsyncExecutionInterceptor.java:115)
      

ts=2022-05-19 00:24:10;thread_name=http-nio-8080-exec-5;id=22;is_daemon=true;priority=5;TCCL=org.springframework.boot.web.embedded.tomcat.TomcatEmbeddedWebappClassLoader@9aa2002
    @com.share.arthas.demo.service.impl.UserServiceImpl.insert()
        at com.share.arthas.demo.controller.ArthasDemoController.createUser(ArthasDemoController.java:76)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(NativeMethodAccessorImpl.java:-2)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43
```

不同线程执行的，通过相同的name串起来，大概率可以判断是一个操作触发的



3- mq触发

同上，只是thread name上可以看出是mq的线程处理的



## ongl

ognl语法：https://commons.apache.org/proper/commons-ognl/language-guide.html>

1- 获取静态属性

```
ognl '@com.share.arthas.demo.utils.RoleUtil@ROLE_MEMBER
```

2- 执行静态方法

```
ognl '@com.share.arthas.demo.utils.RoleUtil@getRoleType(1)'
```

3- 执行静态方法（入参是List/Set）

```
ognl '@com.share.arthas.demo.utils.RoleUtil@filterRole({1,2,3,4})'
```

4- 执行静态方法（入参是Map）

```
ognl -x 3 '@com.share.arthas.demo.utils.RoleUtil@getUsersByMap(#{"ids": "1,2,3","test":"abc"})'
```

5- 执行静态方法（入参是个对象）

```
ognl '#user=new com.share.arthas.demo.model.User(),#user.setId(9L),#user.setRole("3"),#user.setName("yan"),@com.share.arthas.demo.utils.RoleUtil@getRoleTypeByUser(#user)' -x 3
```

先new出来对象，在set属性，然后调用静态方法

6- 执行非静态方法

```
ognl '#userHelper=@com.share.arthas.demo.utils.SpringUtil@getBean(@com.share.arthas.demo.helper.UserHelper@class),#userHelper.getRoleType(1)' -c 18b4aac2
```

这种需要一般是需要获取到Spring组件，然后执行方法



## vmtool

`vmtool` 利用`JVMTI`接口，实现查询内存对象，强制GC等功能

获取JVM进程中的对象，然后执行对象的方法

```
vmtool --action getInstances --className com.share.arthas.demo.helper.UserHelper --express 'instances[0].getRoleType(1)'
```

也执行强制gc

```
vmtool --action forceGc
```

