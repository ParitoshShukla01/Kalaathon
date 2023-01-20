package kalaathon.com.utils;

public class CheckVideoFormat {

    public static boolean accept(String pathname) {
        String ext = null;
        int i = pathname.lastIndexOf('.');


        if (i > 0 && i < pathname.length() - 1) {
            ext = pathname.substring(i).toLowerCase();
        }
        if (ext == null)
            return false;
        else if (ext.equals(".webm")||ext.equals(".mkv")||ext.equals(".flv")||ext.equals(".vob")||ext.equals(".ogv")
                ||ext.equals(".ogg")||ext.equals(".gif")||ext.equals(".mov")||ext.equals(".qt")||ext.equals(".wmv")
                ||ext.equals(".amv")||ext.equals(".mp4")||ext.equals(".mpg")||ext.equals(".mpeg")||ext.equals(".m2v")
                ||ext.equals(".m4v")||ext.equals(".3gp"))
            return true;
        else
            return false;
    }
};

