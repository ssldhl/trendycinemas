package com.trendycinemas.model;

/*
* Created by sushilthe on 8/25/15
*/
public class PosterParam {
    int page;
    String sort;
    public PosterParam(int page, String sort){
        this.page = page;
        this.sort = sort;
    }

    public int getPage(){
        return page;
    }

    public String getSort(){
        return sort;
    }
}
