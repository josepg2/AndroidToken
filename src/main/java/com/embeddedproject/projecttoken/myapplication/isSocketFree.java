package com.embeddedproject.projecttoken.myapplication;

/**
 * Created by George_Joseph02 on 2/25/2017.
 */

public class IsSocketFree {
    private boolean socketStatus;

    public  IsSocketFree(){
        this.socketStatus = true;
    }

    public void blockSocket(){
        this.socketStatus = false;
    }

    public void openSocket(){
        this.socketStatus = true;
    }

    public boolean getSocketStatus(){
        return this.socketStatus;
    }

    public boolean isSocketBusy(){
        return !this.socketStatus;
    }
}
