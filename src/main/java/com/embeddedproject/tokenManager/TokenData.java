package com.embeddedproject.tokenManager;

import java.io.Serializable;

class TokenData implements Serializable {

    int tokenNumber;
    boolean tokenStatus;

    TokenData(){

    }

    TokenData(int tokenNumber, boolean tokenStatus){
        this.tokenNumber = tokenNumber;
        this.tokenStatus = tokenStatus;
    }

    void setTokenNumber(int tokenNumber){
        this.tokenNumber = tokenNumber;
    }

    void setTokenStatus(boolean tokenStatus){
        this.tokenStatus = tokenStatus;
    }
}
