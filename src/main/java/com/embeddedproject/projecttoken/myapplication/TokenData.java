package com.embeddedproject.projecttoken.myapplication;

import android.media.session.MediaSession;

import java.io.Serializable;

/**
 * Created by George_Joseph02 on 2/16/2017.
 */

public class TokenData implements Serializable {

    int tokenNumber;
    boolean tokenStatus;

    public TokenData(){

    }

    public TokenData(int tokenNumber, boolean tokenStatus){
        this.tokenNumber = tokenNumber;
        this.tokenStatus = tokenStatus;
    }

    public void setTokenNumber(int tokenNumber){
        this.tokenNumber = tokenNumber;
    }

    public void setTokenStatus(boolean tokenStatus){
        this.tokenStatus = tokenStatus;
    }
}
