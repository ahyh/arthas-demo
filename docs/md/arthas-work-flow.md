# Arthas工作流程

通过curl -O https://arthas.aliyun.com/arthas-boot.jar命令下载arthas-boot.jar或者将arthas-boot.jar这个包手动上传到server后，就可以使用arthas了

## 1- 启动arthas boot

通过java -jar arthas-boot.jar就可以启动arthas了，那这个命令主要完成了哪些工作呢？

执行java -jar arthas-boot.jar，会执行到arthas-boot模块下Bootstrap.main(), 主要完成：

- 通过static代码块，创建arthas工作目录，{user.home}/.arthas

- 解析启动命令，如果带参数-h/-v，就直接返回help/version信息，不会往下执行了

- check将要使用的telnet port & http port没有被占用

- 找到{java.home} & jps命令目录，拼装一个jps -l命令，通过Runtime.getRuntime().exec()方式获取本机所有正在运行的JVM进程，将所有JVM进程编号展示，供用户选择要attach的进程

- check arthas-core.jar & arthas-spy.jar & arthas-client.jar是否存在{arthas.home}/{lastest-version}目录下，不存在则直接联网下载最新版本

- 创建一个URLClassloader来加载arthas-client.jar中的类

- 拼装命令执行java -jar arthas-core.jar

  ```
  C:\work\javaEnv\jdk8\jre\..\bin\java.exe -Xbootclasspath/a:C:\work\javaEnv\jdk8_181\jre\..\lib\tools.jar, -jar, C:\Users\yanhuan\.arthas\lib\3.6.1\arthas\arthas-core.jar, -pid, 12336, -core, C:\Users\yanhuan\.arthas\lib\3.6.1\arthas\arthas-core.jar, -agent, C:\Users\yanhuan\.arthas\lib\3.6.1\arthas\arthas-agent.jar
  ```

  命令里面指定了pid, core, agent参数

- 反射执行TelnetConsole.main，启动Telnet客户端



## 2- 启动arthas-core

在执行arthas-boot包下Bootstrap.main方法的时候，通过java -jar命令的方式启动arthas-core.jar，参数里面带pid, core, agent参数，会执行到arthas-core.jar的mainClass的main方法，也就是Arthas.main方法

在Arthas.main方法中通过attach API来attach到目标JVM进程，然后执行loadAgent方法，来动态的加载agent jar

```
virtualMachine = VirtualMachine.attach("" + configure.getJavaPid());
// ......
String arthasAgentPath = configure.getArthasAgent();
//convert jar path to unicode string
configure.setArthasAgent(encodeArg(arthasAgentPath));
configure.setArthasCore(encodeArg(configure.getArthasCore()));
try {
	virtualMachine.loadAgent(arthasAgentPath, 
	configure.getArthasCore() + ";" + configure.toString());
} catch (IOException e) {
// 省略catch中的日志
}
```

loadAgent加载的agent jar就是arthas-agent.jar



## 3- load agent jar

arthas-agent模块中的pom.xml中定义了Agent-Class

```
<manifestEntries>
      <Premain-Class>com.taobao.arthas.agent334.AgentBootstrap</Premain-Class>
      <Agent-Class>com.taobao.arthas.agent334.AgentBootstrap</Agent-Class>
      <Can-Redefine-Classes>true</Can-Redefine-Classes>
      <Can-Retransform-Classes>true</Can-Retransform-Classes>
      <Specification-Title>${project.name}</Specification-Title>
      <Specification-Version>${project.version}</Specification-Version>
      <Implementation-Title>${project.name}</Implementation-Title>
      <Implementation-Version>${project.version}</Implementation-Version>
</manifestEntries>
```

Agent-Class是com.taobao.arthas.agent334.AgentBootstrap，那接下来就会执行到AgentBootstrap的agentmain方法，主要完成以下工作：

- 加载arthas-spy.jar下java.arthas.SpyAPI，这个类定义了字节码增强逻辑
- 创建ArthasClassloader，用于加载arthas-core.jar里面的类
- 用创建的ArthasClassloader加载ArthasBootstrap(com.taobao.arthas.core.server.ArthasBootstrap)
- 反射执行ArthasBootstrap.getInstance方法



## 4- ArthasBootstrap.getInstance

ArthasBootstrap位置arthas-core.jar中，ArthasBootstrap是一个全局单例的对象，在第一次执行getInstance的时候，会执行ArthasBootstrap的构造器方法，这个构造器方法主要完成以下功能：

- 初始化arthas environment，加一些配置信息封装起来（arthas.properties）
- 创建arthas-output目录，后续可以存放增强后的class文件
- 创建一个ShellServer
- 创建BuiltinCommandPack对象，这个对象实现了CommandResolver，后续用于解析用户输入的命令，BuiltinCommandPack里面维护了一个Command集合，每个Command对象都对应一种支持的Arthas命令，Command对象里面同时也封装了处理这个Arthas命令的Handler，即AnnotatedCommandImpl的内部类ProcessHandler, 将BuildinCommandPack维护到ShellServer中
- 创建HttpTelnetTermServer & HttpTermServer并设置termHandler为TermServerTermHandler，将两个Server维护到ShellServer中，启动这两个Server，开始监听客户端输入
- 创建一个ScheduledThreadPool来处理Arthas Command, 至此可以开始处理Arthas客户端输入的命令了
- 创建一个TransformerManager，负责管理ClassFileTransformer



## 5- 处理用户输入的Arthas命令

步骤4创建的HttpTelnetTermServer & HttpTermServer的termHandler都是TermServerTermHandler，用户输入的命令从client到server就是这个Handler负责处理，会调用到步骤创建的ShellServer.handleTerm方法

按一下步骤处理

5.1- 创建ShellImpl

5.2- 创建ShellLineHandler(负责解析输入的命令并生成一个job来执行)，ShellLineHandler封装到RequestHandler中，封装到Readline对象，执行readline方法，最终会走到ShellLineHandler.handle

5.3- ShellLineHandler.handle开始处理用户输入的命令，首先会将命令解析成CliToken集合

5.4- 根据第一个CliToken判断是否需要生成Job来执行，exit/quit这些命令就不行在执行了

5.5- 根据CliToken创建CommandProcess，这个CommandProcess里面就包含了具体的Command对象和Command的处理器Handler

5.6- 将CommandProcess对象封装到Job中

5.7- 创建Job对应的TimeoutTask放入ArthasBootstrap中的ScheduledExecutorService线程池中，处理超时了还未完成的Job，如果达到超时时间，Job还没处理完成的话，会执行job.terminate方法来终止

5.8- 执行Job，执行Job中CommandProcess.run方法

5.9- 对于确实需要执行的Job，将CommandProcess包装成CommandProcessTask，然后提交到ArthasBootstrap中的ScheduledExecutorService线程池中

5.10- 开始执行ProcessImpl.CommandProcessTask中run方法，就是找到Command对象对应的Handler，以watch命令为例，此时会执行AnnotatedCommandImpl的内部类ProcessHandler

5.11- 执行WatchCommand的父类EnhancerCommand.process方法，这里方法里的enhance()里面就是字节码增强的具体逻辑，核心code如下

```
Enhancer enhancer = new Enhancer(listener, listener instanceof InvokeTraceable, skipJDKTrace, getClassNameMatcher(), getClassNameExcludeMatcher(), getMethodNameMatcher());
process.register(listener, enhancer);
effect = enhancer.enhance(inst);
```

Enhancer类实现了ClassFileTransformer接口，Enhancer中定义spyImpl实例，这个类会织入增强逻辑，主要处理AdviceListener逻辑，通知Arthas命令执行结果的

```
private static SpyImpl spyImpl = new SpyImpl();
```

transform方法里面实现了字节码增强的过程

```
//keep origin class reader for bytecode optimizations, avoiding JVM metaspace OOM.
ClassNode classNode = new ClassNode(Opcodes.ASM9);
ClassReader classReader = AsmUtils.toClassNode(classfileBuffer, classNode);
// remove JSR https://github.com/alibaba/arthas/issues/1304
classNode = AsmUtils.removeJSRInstructions(classNode);

// 生成增强字节码
DefaultInterceptorClassParser defaultInterceptorClassParser = new DefaultInterceptorClassParser();

final List<InterceptorProcessor> interceptorProcessors = new ArrayList<InterceptorProcessor>();

interceptorProcessors.addAll(defaultInterceptorClassParser.parse(SpyInterceptor1.class));
interceptorProcessors.addAll(defaultInterceptorClassParser.parse(SpyInterceptor2.class));
interceptorProcessors.addAll(defaultInterceptorClassParser.parse(SpyInterceptor3.class));

if (this.isTracing) {
	if (!this.skipJDKTrace) {
		interceptorProcessors.addAll(defaultInterceptorClassParser.parse(SpyTraceInterceptor1.class));
		interceptorProcessors.addAll(defaultInterceptorClassParser.parse(SpyTraceInterceptor2.class));
		interceptorProcessors.addAll(defaultInterceptorClassParser.parse(SpyTraceInterceptor3.class));
	} else {
		interceptorProcessors.addAll(defaultInterceptorClassParser.parse(SpyTraceExcludeJDKInterceptor1.class));
		interceptorProcessors.addAll(defaultInterceptorClassParser.parse(SpyTraceExcludeJDKInterceptor2.class));
		interceptorProcessors.addAll(defaultInterceptorClassParser.parse(SpyTraceExcludeJDKInterceptor3.class));
	}
}

List<MethodNode> matchedMethods = new ArrayList<MethodNode>();
for (MethodNode methodNode : classNode.methods) {
	if (!isIgnore(methodNode, methodNameMatcher)) {
		matchedMethods.add(methodNode);
	}
}
```

​	此处用到了bytekit这个字节码增强工具，定义了多个增强拦截器

```
public static class SpyInterceptor1 {

        @AtEnter(inline = true)
        public static void atEnter(@Binding.This Object target, @Binding.Class Class<?> clazz,
                @Binding.MethodInfo String methodInfo, @Binding.Args Object[] args) {
            SpyAPI.atEnter(clazz, methodInfo, target, args);
        }
    }
    
    public static class SpyInterceptor2 {
        @AtExit(inline = true)
        public static void atExit(@Binding.This Object target, @Binding.Class Class<?> clazz,
                @Binding.MethodInfo String methodInfo, @Binding.Args Object[] args, @Binding.Return Object returnObj,
                                  @Binding.Line int endLine) {
            SpyAPI.atExit(clazz, methodInfo, target, args, returnObj, endLine);
        }
    }
    
    public static class SpyInterceptor3 {
        @AtExceptionExit(inline = true)
        public static void atExceptionExit(@Binding.This Object target, @Binding.Class Class<?> clazz,
                @Binding.MethodInfo String methodInfo, @Binding.Args Object[] args,
                @Binding.Throwable Throwable throwable, @Binding.Line int endLine) {
            SpyAPI.atExceptionExit(clazz, methodInfo, target, args, throwable, endLine);
        }
    }
```

参考bytekit的demo，很容易理解这里的逻辑，Binding各种信息到目标方法中

```
for (MethodNode methodNode : matchedMethods) {
	if (AsmUtils.isNative(methodNode)) {
		logger.info("ignore native method: {}",
				AsmUtils.methodDeclaration(Type.getObjectType(classNode.name), methodNode));
		continue;
	}
	// 先查找是否有 atBeforeInvoke 函数，如果有，则说明已经有trace了，则直接不再尝试增强，直接插入 listener
	if(AsmUtils.containsMethodInsnNode(methodNode, Type.getInternalName(SpyAPI.class), "atBeforeInvoke")) {
		for (AbstractInsnNode insnNode = methodNode.instructions.getFirst(); insnNode != null; insnNode = insnNode
				.getNext()) {
			if (insnNode instanceof MethodInsnNode) {
				final MethodInsnNode methodInsnNode = (MethodInsnNode) insnNode;
				if(this.skipJDKTrace) {
					if(methodInsnNode.owner.startsWith("java/")) {
						continue;
					}
				}
				// 原始类型的box类型相关的都跳过
				if(AsmOpUtils.isBoxType(Type.getObjectType(methodInsnNode.owner))) {
					continue;
				}
				AdviceListenerManager.registerTraceAdviceListener(inClassLoader, className,
						methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc, listener);
			}
		}
	}else {
		MethodProcessor methodProcessor = new MethodProcessor(classNode, methodNode, groupLocationFilter);
		for (InterceptorProcessor interceptor : interceptorProcessors) {
			try {
				List<Location> locations = interceptor.process(methodProcessor);
				for (Location location : locations) {
					if (location instanceof MethodInsnNodeWare) {
						MethodInsnNodeWare methodInsnNodeWare = (MethodInsnNodeWare) location;
						MethodInsnNode methodInsnNode = methodInsnNodeWare.methodInsnNode();

						AdviceListenerManager.registerTraceAdviceListener(inClassLoader, className,
								methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc, listener);
					}
				}

			} catch (Throwable e) {
				logger.error("enhancer error, class: {}, method: {}, interceptor: {}", classNode.name, methodNode.name, interceptor.getClass().getName(), e);
			}
		}
	}

	// enter/exist 总是要插入 listener
	AdviceListenerManager.registerAdviceListener(inClassLoader, className, methodNode.name, methodNode.desc,
			listener);
	affect.addMethodAndCount(inClassLoader, className, methodNode.name, methodNode.desc);
}
```

对于ClassMatcher和MethodMatcher匹配出来的方法执行增强逻辑，至此就完成了字节码动态替换了，结果通知的逻辑也封装在字节码增强逻辑中了



## 6- demo

打开dump开关，options dump true，这样在处理字节码增强后会把增强的class文件输出，执行如下命令

watch com.share.arthas.demo.service.impl.UserServiceImpl getUserById '{params,returnObj,throwExp}'  -n 5  -x 3

触发，得到class文件

```
 public User getUserById(long id) {
	Object[] var9 = new Object[]{new Long(id)};
	String var8 = "getUserById|(J)Lcom/share/arthas/demo/model/User;";
	Class var7 = UserServiceImpl.class;
	SpyAPI.atEnter(var7, var8, this, var9);

	try {
		User user = this.userMapper.getUserById(id);
		if (user == null) {
			Object var10 = null;
			Object[] var14 = new Object[]{new Long(id)};
			String var13 = "getUserById|(J)Lcom/share/arthas/demo/model/User;";
			Class var12 = UserServiceImpl.class;
			SpyAPI.atExit(var12, var13, this, var14, var10);
			return (User)var10;
		} else {
			String roleType = RoleUtil.getRoleType((int)(id % 3L));
			user.setRole(roleType);
			String typeStr = UserUtil.getTypeStr((int)(id % 3L));
			user.setType(typeStr);
			Object[] var19 = new Object[]{new Long(id)};
			String var18 = "getUserById|(J)Lcom/share/arthas/demo/model/User;";
			Class var17 = UserServiceImpl.class;
			SpyAPI.atExit(var17, var18, this, var19, user);
			return user;
		}
	} catch (Throwable var27) {
		Object[] var25 = new Object[]{new Long(id)};
		String var24 = "getUserById|(J)Lcom/share/arthas/demo/model/User;";
		Class var23 = UserServiceImpl.class;
		SpyAPI.atExceptionExit(var23, var24, this, var25, var27);
		throw var27;
	}
}
```

