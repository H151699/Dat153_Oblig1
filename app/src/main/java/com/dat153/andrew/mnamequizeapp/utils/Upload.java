package com.dat153.andrew.mnamequizeapp.utils;

public class Upload {
    private String imgName;
    private String imgUrl;

    /**
     *
     */
    public Upload() {
    }

    /**
     *
     * @param imgName
     * @param imgUrl
     */
    public Upload(String imgName, String imgUrl) {
        if(imgName.trim().equals(""))
        {
            imgName="No name";
        }
        this.imgName = imgName;
        this.imgUrl = imgUrl;
    }

    /**
     *
     * @return
     */
    public String getImgName() {
        return imgName;
    }

    /**
     *
     * @param imgName
     */
    public void setImgName(String imgName) {
        this.imgName = imgName;
    }

    /**
     *
     * @return
     */
    public String getImgUrl() {
        return imgUrl;
    }

    /**
     *
     * @param imgUrl
     */
    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
