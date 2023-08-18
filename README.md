## CrumbContainer

这是一个小ioc框架， 主要是来测试我之前想的解决依赖的思路，所以aware, postprocessor 啥的都没写

API基本和spring一样，但是只写了按class注入

属性注入和spring不大一样，不需要给setter加Autowired，容器会自动寻找对应名字的setter，找不到再使用字段注入

用logback写了Logger，能看到依赖解决的过程，可以通过添加logback.xml修改，也可以通过下面的方法直接修改输出等级:

```java
CrumbContainer.setLoggerLevel(Level.DEBUG);
```

可以使用Values注解注入外部值，默认是application.yaml，可以通过下面的方法增加或者修改默认路径，只支持yaml格式:

```java
PropFactory.setDefaultPath("defaultPath");
PropFactory.addFilePath("newPath");
```




