## CrumbContainer

这是一个小ioc框架， 主要是来测试我之前想的解决依赖的思路

API基本和spring一样，但是只写了按class注入，所以使用起来非常残疾

### Sample

```java
public class MainTest {

    public static void main(String[] args) {
        CrumbContainer.setLoggerLevel(Level.DEBUG);
        var container = new CrumbContainer(AppConfig.class);
        var foo = container.getBean(Foo.class);
        foo.test();
    }
}
```

### Install

已经上传到maven中央仓库，复制下面的xml即可导入:

```xml
<dependency>
    <groupId>io.github.cppsh1t</groupId>
    <artifactId>crumbContainer</artifactId>
    <version>0.1.7</version>
</dependency>
```

### VM参数

因为是使用cglib在Java17环境下实现AOP，需要加VM参数: --add-opens java.base/java.lang=ALL-UNNAMED

### Logger

用logback写了Logger，能看到依赖解决的过程，可以通过添加logback.xml修改，也可以通过下面的方法直接修改输出等级:

```java
CrumbContainer.setLoggerLevel(Level.DEBUG);
```

### Bean

Component标记类或者Bean标记方法，标记Autowired的构造函数优先调用
如果想注册为其superClass，可以手动在Bean或Component的参数里指定:

```java
@Component(IFoo.class)
@Lazy
public class Foo implements IFoo {

}
```

### Inject

属性注入和spring不大一样，不需要给setter加Autowired，容器会自动寻找对应名字的setter，找不到再使用字段注入

### Init

Bean生命周期的相关接口写的很少，BeanPostProcessor和Aware根本没写，有InitializingBean和DisposableBean

### Values

可以使用Values注解注入外部值，默认是application.yaml，可以通过下面的方法增加或者修改默认路径，只支持yaml格式:

```java
PropFactory.setDefaultPath("defaultPath");
PropFactory.addFilePath("newPath");
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
        CrumbContainer.setLoggerLevel(Level.DEBUG);
        var container = new CrumbContainer(AppConfig.class);
        var mapper = container.getBean(TestMapper.class);
        mapper.selectStudents().forEach(System.out::println);
    }
}
```






