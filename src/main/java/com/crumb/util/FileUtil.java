package com.crumb.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {

    public static List<File> getAllFiles(File directory) {
        List<File> fileList = new ArrayList<>();
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        fileList.add(file); // 添加文件到列表
                    } else if (file.isDirectory()) {
                        List<File> subFiles = getAllFiles(file); // 递归调用，获取子目录下的文件
                        fileList.addAll(subFiles); // 将子目录下的文件添加到列表
                    }
                }
            }
        }
        return fileList;
    }
}