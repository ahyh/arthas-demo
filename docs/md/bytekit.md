# bytekit

bytekit是阿里开源的字节码增强工具，是对ASM的更进一步封装，提供了更高层的字节码处理能力，面向诊断/APM领域，bytekit提供了一套简洁的API，让开发人员更方便完成字节码增强

官网地址：<https://github.com/alibaba/bytekit>

Arthas使用bytekit作为字节码增强工具



## bytekit核心组件

### 1- Location & LocationMatcher

Location: 表示触发字节码增强的位置

LocationMatcher: Location匹配器，在bytekit中是一个接口，提供找出需要进行相应字节码增强位置的功能

UML类图：

![../png/Location.png]()

![../png/LocationMatcher.png]()

LocationMatcher

```
public interface LocationMatcher {

    public List<Location> match(MethodProcessor methodProcessor);

}
```

举个例子，如果需要监控一个方法的返回值，那么很明显需要在返回最后return的地方做字节码增强，也就是需要有个ExitLocation，同样也需要一个ExitLocationMatcher，那ExitLocationMatcher就需要找到方法的Code属性中所有的return指令，return指令的地方就是ExitLocation

ExitLocation

```
public static class ExitLocation extends Location {

        public ExitLocation(AbstractInsnNode insnNode) {
            super(insnNode);
            stackNeedSave = true;
        }

        @Override
        public boolean canChangeByReturn() {
            return true;
        }

        public LocationType getLocationType() {
            return LocationType.EXIT;
        }
        
        /**
         * store/load return指令的操作数，即方法的返回值，把方法的返回收集起来
        */
        public StackSaver getStackSaver() {
            StackSaver stackSaver = new StackSaver() {

                @Override
                public void store(InsnList instructions, BindingContext bindingContext) {
                    Type returnType = bindingContext.getMethodProcessor().getReturnType();
                    if(!returnType.equals(Type.VOID_TYPE)) {
                        LocalVariableNode returnVariableNode = bindingContext.getMethodProcessor().initReturnVariableNode();
                        AsmOpUtils.storeVar(instructions, returnType, returnVariableNode.index);
                    }
                }

                @Override
                public void load(InsnList instructions, BindingContext bindingContext) {
                    Type returnType = bindingContext.getMethodProcessor().getReturnType();
                    if(!returnType.equals(Type.VOID_TYPE)) {
                        LocalVariableNode returnVariableNode = bindingContext.getMethodProcessor().initReturnVariableNode();
                        AsmOpUtils.loadVar(instructions, returnType, returnVariableNode.index);
                    }
                }

                @Override
                public Type getType(BindingContext bindingContext) {
                    return bindingContext.getMethodProcessor().getReturnType();
                }
                
            };
            return stackSaver;
        }

    }
```

ExitLocationMatcher

```
public class ExitLocationMatcher implements LocationMatcher {

    @Override
    public List<Location> match(MethodProcessor methodProcessor) {
        List<Location> locations = new ArrayList<Location>();
        AbstractInsnNode insnNode = methodProcessor.getEnterInsnNode();

        while (insnNode != null) {
            if (insnNode instanceof InsnNode) {
                InsnNode node = (InsnNode) insnNode;
                if (matchExit(node)) {
                    LocationFilter locationFilter = methodProcessor.getLocationFilter();
                    if (locationFilter.allow(node, LocationType.EXIT, false)) {
                        ExitLocation ExitLocation = new ExitLocation(node);
                        locations.add(ExitLocation);
                    }
                }
            }
            insnNode = insnNode.getNext();
        }

        return locations;
    }

    /**
    * 可以发现，遍历到*RETURN指令就是正常返回的指令，这些地方就是需要做字节码增强的Location
    */
    public boolean matchExit(InsnNode node) {
        switch (node.getOpcode()) {
        case Opcodes.RETURN: // empty stack
        case Opcodes.IRETURN: // 1 before n/a after
        case Opcodes.FRETURN: // 1 before n/a after
        case Opcodes.ARETURN: // 1 before n/a after
        case Opcodes.LRETURN: // 2 before n/a after
        case Opcodes.DRETURN: // 2 before n/a after
            return true;
        }
        return false;
    }
```



### 2- Binding

包含*Binding, 已经对应的各种BindingParser

*Binding: 表示各种可以通过字节码增强的收集到的元素以及收集方式的注解和对象

*BindParser: 表示各种Binding的解析器，只是将Binding注解解析出Binding对象，便于后期在拦截器处理的时候将各种Binding信息收集起来，完成字节码增强功能

![../png/Binding.png]()

![../png/BindingParser.png]()

举个例子，还是以监控方法的返回值来说明，需要监控方法的返回值，那么需要在拦截器里面描述出来需要监控返回值，这个描述动作就是Binding注解来完成的，这个注解后期会被拦截器解析出来Binding对象，Binding对象里面描述了具体的值怎么进行字节码增强

ReturnBinding的注解和解析器

```
@Documented
    @Retention(RetentionPolicy.RUNTIME)
    @java.lang.annotation.Target(ElementType.PARAMETER)
    @BindingParserHandler(parser = ReturnBindingParser.class)
    public static @interface Return {
        
        boolean optional() default false;

    }
    
    public static class ReturnBindingParser implements BindingParser {
        @Override
        public Binding parse(Annotation annotation) {
            return new ReturnBinding();
        }
        
    }
```

可以看到解析器就直接返回一个new出来的ReturnBinding对象

ReturnBinding

```
public class ReturnBinding extends Binding {


    @Override
    public void pushOntoStack(InsnList instructions, BindingContext bindingContext) {
        //check location
        
        Location location = bindingContext.getLocation();

        if (!AsmOpUtils.isReturnCode(location.getInsnNode().getOpcode())) {
            throw new IllegalArgumentException("current location is not return location. location: " + location);
        }
        
        Type returnType = bindingContext.getMethodProcessor().getReturnType();
        if(returnType.equals(Type.VOID_TYPE)) {
            AsmOpUtils.push(instructions, null);
        }else {
            LocalVariableNode returnVariableNode = bindingContext.getMethodProcessor().initReturnVariableNode();
            AsmOpUtils.loadVar(instructions, returnType, returnVariableNode.index);
        }
        
    }

    @Override
    public boolean fromStack() {
        return true;
    }

    @Override
    public Type getType(BindingContext bindingContext) {
        return bindingContext.getMethodProcessor().getReturnType();
    }
    
}
```

- 判断Location是不是*Return指令对应的Location，不是的话抛出异常
- 判断return的值的类型，如果返回值类型为void，则直接把null放入增强的字节码指令中
- 如果return的值的类型不是void，则从局部变量表中根据index找出返回值，在压入操作数栈中，类似于是写了一行java code, ReturnType var1 = returnValue;



### 3- InterceptorProcessor与@At*注解

Bytekit并没有提供任何具体的Interceptor实现，只是提供了一些@At*注解，来表示在方法执行过程中的哪个动作需要进行拦截，在不同的At注解中定义了不同的InterceptorParserHander，通过InterceptorParserHander又可以解析出来不同的InterceptorProcessor实例，在InterceptorProcessor中包含了LocationMather以及各种Binding信息，InterceptorProcessor的process方法即可以根据LocationMatcher找出所有的需要进行字节码增强的Location，然后根据各种Binding，通过直接插入字节码的方式，将各种信息收集起来

还是以方法正常返回结束，监控方法返回值为例，可以发现bytekit提供了@AtExit注解

```
@Documented
@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(ElementType.METHOD)
@InterceptorParserHander(parserHander = ExitInterceptorProcessorParser.class)
public @interface AtExit {
    boolean inline() default true;
    Class<? extends Throwable> suppress() default None.class;
    Class<?> suppressHandler() default Void.class;

    class ExitInterceptorProcessorParser implements InterceptorProcessorParser {

        @Override
        public InterceptorProcessor parse(Method method, Annotation annotationOnMethod) {

            AtExit atExit = (AtExit) annotationOnMethod;

            LocationMatcher locationMatcher = new ExitLocationMatcher();

            return InterceptorParserUtils.createInterceptorProcessor(method,
                    locationMatcher,
                    atExit.inline(),
                    atExit.suppress(),
                    atExit.suppressHandler());

        }

    }
}
```

注解中定义的InterceptorParserHander就是这个注解的内部类，里面描述了LocationMatcher就是之前看到的ExitLocationMather，也就是方法Code属性所有*RETURN的地方，InterceptorParserUtils.createInterceptorProcessor方法里面有解析Method中所有Parameter的Binding信息，放在interceptorMethodConfig里面

```
public static InterceptorProcessor createInterceptorProcessor(
            Method method,
            LocationMatcher locationMatcher,
            boolean inline,
            Class<? extends Throwable> suppress,
            Class<?> suppressHandler) {

        InterceptorProcessor interceptorProcessor = new InterceptorProcessor(method.getDeclaringClass().getClassLoader());

        //locationMatcher
        interceptorProcessor.setLocationMatcher(locationMatcher);

        //interceptorMethodConfig
        InterceptorMethodConfig interceptorMethodConfig = new InterceptorMethodConfig();
        interceptorProcessor.setInterceptorMethodConfig(interceptorMethodConfig);
        interceptorMethodConfig.setOwner(Type.getInternalName(method.getDeclaringClass()));
        interceptorMethodConfig.setMethodName(method.getName());
        interceptorMethodConfig.setMethodDesc(Type.getMethodDescriptor(method));

        //inline
        interceptorMethodConfig.setInline(inline);

        //bindings
        List<Binding> bindings = BindingParserUtils.parseBindings(method);
        interceptorMethodConfig.setBindings(bindings);

        //errorHandlerMethodConfig
        InterceptorMethodConfig errorHandlerMethodConfig = ExceptionHandlerUtils
                .errorHandlerMethodConfig(suppress, suppressHandler);
        if (errorHandlerMethodConfig != null) {
            interceptorProcessor.setExceptionHandlerConfig(errorHandlerMethodConfig);
        }

        return interceptorProcessor;
    }
```

至此InterceptorProcessor与Binding, Location, @At*注解都联系起来，后续的字节码增强就是在InterceptorProcessor的process方法中



### 4- InterceptorClassParser

拦截器类解析器，主要的功能就是根据静态方法上@At*注解，与方法参数中的@Binding注解构造出InterceptorProcessor集合，后续根据这个集合完成方法的字节码增强

bytekit提供了一个默认实现

```
public class DefaultInterceptorClassParser implements InterceptorClassParser {

    @Override
    public List<InterceptorProcessor> parse(Class<?> clazz) {
        final List<InterceptorProcessor> result = new ArrayList<InterceptorProcessor>();

        MethodCallback methodCallback = new MethodCallback() {

            @Override
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                for (Annotation onMethodAnnotation : method.getAnnotations()) {
                    for (Annotation onAnnotation : onMethodAnnotation.annotationType().getAnnotations()) {
                        if (InterceptorParserHander.class.isAssignableFrom(onAnnotation.annotationType())) {

                            if (!Modifier.isStatic(method.getModifiers())) {
                                throw new IllegalArgumentException("method must be static. method: " + method);
                            }

                            InterceptorParserHander handler = (InterceptorParserHander) onAnnotation;
                            InterceptorProcessorParser interceptorProcessorParser = InstanceUtils
                                    .newInstance(handler.parserHander());
                            InterceptorProcessor interceptorProcessor = interceptorProcessorParser.parse(method,
                                    onMethodAnnotation);
                            result.add(interceptorProcessor);
                        }
                    }
                }
            }

        };
        ReflectionUtils.doWithMethods(clazz, methodCallback);

        return result;
    }

}
```



## bytekit工作的流程图

/docs/bytekit.drawio



## bytekit demo

见：arthas-demo-bytekit模块下AtExitTest类