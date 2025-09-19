package com.sosd.utils;

import com.sosd.constant.MessageConstant;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

public class FileUtil {

    public static boolean fileAndNameNotNull(MultipartFile file) {
        if(file==null||file.isEmpty()){
            return false;
        }
        String fileName = file.getOriginalFilename();
        if(fileName==null||fileName.isEmpty()||fileName.isBlank()){
            return false;
        }
        return true;
    }

}
