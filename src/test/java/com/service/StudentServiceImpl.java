package com.service;

import com.crumb.annotation.Autowired;
import com.crumb.data.Transactional;
import com.crumb.web.Service;
import com.entity.Student;
import com.mapper.TestMapper;

import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {

    @Autowired
    TestMapper mapper;

    @Override
    public List<Student> selectAll() {
        return mapper.selectStudents();
    }

    @Override
    @Transactional
    public void addStudent(Student student) {
        var result = mapper.addStudent(student);
        getException();
    }

    public TestMapper getMapper() {
        return mapper;
    }

    private void getException() {
        throw new RuntimeException();
    }
}
