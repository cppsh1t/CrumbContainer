package org.crumb.util;

public class StringUtil {

    public static String toLowerFirstChar(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        char firstChar = str.charAt(0);
        if (Character.isLowerCase(firstChar)) {
            return str;
        }
        return Character.toLowerCase(firstChar) + str.substring(1);
    }

    public static String toUpperFirstChar(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        char firstChar = str.charAt(0);
        if (Character.isUpperCase(firstChar)) {
            return str;
        }
        return Character.toUpperCase(firstChar) + str.substring(1);
    }

    public static String getPackageName(String inputString) {
        // 寻找目标字符串的位置
        int targetIndex = inputString.indexOf("target");
        if (targetIndex == -1) {
            return ""; // 如果找不到目标字符串，则返回空字符串
        }

        // 从目标字符串位置开始查找第一个反斜杠的位置
        int slash1Index = inputString.indexOf("\\", targetIndex);
        if (slash1Index == -1) {
            return ""; // 如果找不到第一个反斜杠，则返回空字符串
        }

        // 从第一个反斜杠位置开始查找第二个反斜杠的位置
        int slash2Index = inputString.indexOf("\\", slash1Index + 1);
        if (slash2Index == -1) {
            return ""; // 如果找不到第二个反斜杠，则返回空字符串
        }

        // 从第二个反斜杠位置开始查找第三个反斜杠的位置
        int slash3Index = inputString.indexOf("\\", slash2Index + 1);
        if (slash3Index == -1) {
            return ""; // 如果找不到第三个反斜杠，则返回空字符串
        }

        // 提取两个反斜杠之间的字符串

        return inputString.substring(slash2Index + 1, slash3Index);
    }
}
