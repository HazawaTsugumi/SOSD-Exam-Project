package com.sosd.constant;

import java.util.HashMap;

public class MessageConstant {

    public static final String EMAIL_NOT_FOUND = "邮箱不存在";

    public static final String EMAIL_IS_USED = "邮箱已被注册";

    public static final String WRONG_EMAIL_FORMAT = "邮箱格式错误";

    public static final String USERNAME_IS_USED = "用户名已存在";

    public static final String WRONG_VERIFY_CODE = "验证码错误";

    public static final String USER_NOT_FOUND = "该用户不存在";

    public static final String PUBLISH_ERROR="文章上传失败";

    public static final String TAG_IS_IS_NULL="标签不能为空";

    public static final String CONTENT_IS_NULL="文章不能为空";

    public static final String TITLE_IS_NULL="标题不能为空";

    public static final String FILE_IS_NULL="文件不能为空";

    public static final String FILENAME_IS_NULL="文件名不能为空";

    public static final String[] IMAGE_FILE_SUFFIXES = {"jpg", "png", "gif"};

    public static final String FILE_IS_NOT_IMAGE="文件必须为图片";

    public static final String FAILED_UPLOAD="文件上传失败";

    public static final String FAILED_DELETE="文件删除失败";

    public static final HashMap<String,String> FILE_HEX_MAP=new HashMap<>();

    static{
        FILE_HEX_MAP.put("jpg","FFD8FF");
        FILE_HEX_MAP.put("png","89504E470D0A1A0A");
        FILE_HEX_MAP.put("gif","47494638");
    }

    public static final byte MAX_HEX_LENGTH=16;

    public static byte getHexLengthOfMagicNumber(String suffix){
         switch (suffix){
            case "jpg":
                return 6;
             case "png":
                return 16;
             case "gif":
                return 8;
             default:
                return 0;
        }
    }

    public static final String SOSD_IMAGE="sosd-image";

    public static final String FAILED_GET="文件获取失败";

    public static final String AUTH_FAIL = "您没有权限访问此资源";

    public static final String WRONG_AUTH = "不合法的权限";

    public static final String INTERNAL_ERROR = "服务器出现错误，请联系管理员";

    public static final String UNKNOWN_BLOG = "文章不存在";

    public static final String DELETE_AUTH_FAIL = "您没有权限删除此资源";

    public static final String UNKNOWN_COMMENT = "评论不存在";

    public static final String DATE_ERROR = "结束日期不能早于开始日期";
}
