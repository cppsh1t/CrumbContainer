package com;

import ch.qos.logback.classic.Level;
import com.crumb.annotation.PostConstruct;
import com.crumb.core.Container;
import com.crumb.core.EnhancedContainer;
import com.crumb.core.MainContainer;
import com.crumb.mail.MailMessage;
import com.crumb.mail.MailSender;
import com.crumb.mail.SimpleMailMessage;
import com.crumb.proxy.ProxyObject;
import com.entity.*;
import com.mapper.TestMapper;
import com.service.StudentService;
import com.service.StudentServiceImpl;


public class MainTest {

    private static final Container container;


    static {
        Container.setLoggerLevel(Level.DEBUG);
        container = MainContainer.getContainer();
    }

    public static void main(String[] args) {
        tranTest();
    }

    public static void dataTest() {
        var mapper = container.getBean(TestMapper.class);
        mapper.selectStudents().forEach(System.out::println);
    }

    public static void aopTest() {
        var foo = container.getBean(IFoo.class);
        foo.test();
        var inside = ((ProxyObject) foo).getOrigin();
        System.out.println(inside);
    }

    public static void mailTest() {
        var mailSender = container.getBean(MailSender.class);
        var target1 = (String) container.getFromValues("crumb.mail.target1");
        var target2 = (String) container.getFromValues("crumb.mail.target2");
        var from = (String) container.getFromValues("crumb.mail.username");
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(target1);
        message.addTo(target2);
        message.setSubject("主题");
        message.setText("内容");
        mailSender.send(message);
    }

    public static void injectTest() {
        var stone = container.getBean(Stone.class);
        System.out.println(stone.getWeight());
    }

    public static void tranTest() {
        var service = container.getBean(StudentService.class);
        System.out.println(((StudentServiceImpl) service).getMapper());
//        service.selectAll().forEach(System.out::println);
        service.addStudent(new Student(24, "男", "牢大"));
        service.selectAll().forEach(System.out::println);
    }

}
