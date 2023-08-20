package com.mapper;

import com.entity.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TestMapper {

    @Select("select * from student")
    List<Student> selectStudents();
}
