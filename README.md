## CrumbContainer

这是一个小ioc框架， 主要是来测试我之前想的解决依赖的思路

功能写的比较少

### Sample

```java
public class MainTest {

    public static void main(String[] args) {
        Container.setLoggerLevel(Level.DEBUG);
        var container = new EnhancedContainer(AppConfig.class);
        container.getBean(SleepService.class).sleep();
    }
}
```

### Install

已经上传到maven中央仓库，复制下面的xml即可导入:

```xml
<dependency>
    <groupId>io.github.cppsh1t</groupId>
    <artifactId>crumbContainer</artifactId>
    <version>0.1.8</version>
</dependency>
```

### VM参数

因为是使用cglib在Java17环境下实现AOP，需要加VM参数: `--add-opens java.base/java.lang=ALL-UNNAMED`

### Logger

用logback写了Logger，能看到依赖解决的过程，可以通过添加logback.xml修改，也可以通过下面的方法直接修改输出等级:

```java
Container.setLoggerLevel(Level.DEBUG);
```

### Banner

启动时会输出banner，在resources里写一个banner.txt应该可以改

### 创建Container

```java
Container container1 = new DefaultContainer(AppConfig.class);
Container container2 = new EnhancedContainer(AppConfig.class);
Container container3 = MainContainer.getContainer();
//Container container3 = MainContainer.getContainer(DefaultContainer.class);
```

Container有两个实现，目前EnhancedContainer除了使用ClassGraph库进行Component扫描外和另外一个没什么不同。
`MainContainer.getContainer()`会创建一个单例Container，并自动扫描到标记了`MainConfiguraion`的注解的类当作配置类生成Container，
默认实现是EnhancedContainer，当使用以class作为参数的重载时，内部的Container就是参数的类型

### Bean

用Bean标记方法将返回值注册为单例
如果想注册为其父类型，可以手动在Bean参数里指定:
另外也可以通过name参数指定名字而非默认名字

```java
@Component(IFoo.class)
@Lazy
public class Foo implements IFoo {

}
```

### Component

Component和Bean基本一样
```java
@Component
@Scope(ScopeType.PROTOTYPE)
public class Stone {

    @Autowired
    private int weight;

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
```

### Inject

属性注入和spring不大一样，不需要给setter加Autowired，容器会自动寻找对应名字的setter，找不到再使用字段注入
在字段上进行注入时支持Autowired和Resource，一个是按类型，一个是按名字

### Init

相关的初始化接口和postProcessor和spring基本一样，postProcessor没写几个

### Values

可以使用Value注解注入外部值，默认是application.yaml，可以通过下面的方法增加或者修改默认路径，只支持yaml格式:

```java
DefaultValuesFactory.setDefaultPath("defaultPath");
DefaultValuesFactory.addFilePath("newPath");
```

### AOP

AOP和spring的API不大一样，使用AOP之前需要先将配置类加上EnableAspectProxy注解:

```java
@ComponentScan("com")
@Configuration
@EnableAspectProxy
public class AppConfig {

    @Bean
    @Lazy
    public String getName() {
        return "Xqc";
    }

}
```

AOP类需要加上Aspect注解，参数是要增强的类:

```java
@Component
@Aspect(Foo.class)
public class FooAOP {

    @Before("test")
    public void before() {
        System.out.println("before test");
    }

    @After("test")
    public void after() {
        System.out.println("after test");
    }

    @Around("toString")
    public Object changeToString(JoinPoint joinPoint) {
        return "bull shit";
    }

}
```

要增强的方法标记上相关的注解，参数直接就是要增强的方法名

此外还有一些规则:
1. Before和After需要是无参函数或者是单个参数类型为Object[]， 为后者时会自动填充进原函数的参数
2. AfterReturn需要参数类型和返回类型都为Object
3. Around需要参数类型为JoinPoint，返回类型为Object，JoinPoint里有原函数的相关数据

### DataBase

整合了一点mybatis，写的很简单，只能进行一点简单的操作:

configClass:
```java
@ComponentScan("com")
@Configuration
@EnableAspectProxy
@MapperScan("com.mapper")
public class AppConfig {

    @Bean
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/test");
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUsername("username");
        dataSource.setPassword("password");
        return dataSource;
    }

    @Bean
    public SqlSessionFactoryBean sqlSessionFactoryBean(DataSource dataSource){
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        return bean;
    }
}
```

getMapper:
```java
public class MainTest {

    public static void main(String[] args) {
        var container = new EnhancedContainer(AppConfig.class);
        var mapper = container.getBean(TestMapper.class);
        mapper.selectStudents().forEach(System.out::println);
    }
}
```






