package com.embeddedproject.projecttoken.myapplication;

/**
 * Created by George_Joseph02 on 2/16/2017.
 */

public interface Listener {
    void updateTokenStatus(int token, boolean status);

    void listAllTokens();

    void listUnattentedTokens();

    void enterTokenNumber();

    void updateTokenHeaderAndTitle(TokenData tokenData);

    void updateViewAndDb(TokenData tokenData);

    void goToToken(int newTokenNumber);

    boolean getTokenConnectionStatus();
}