package com.embeddedproject.tokenManager;

interface Listener {

    void setListItemToRemove(int position, int tokenNumber);

    void updateTokenStatus(int token, boolean status);

    void listAllTokens();

    void listUnattentedTokens();

    void enterTokenNumber();

    void updateTokenHeaderAndTitle(TokenData tokenData);

    void updateViewAndDb(TokenData tokenData);

    void updateLastNonCalledToken(int nextTokenNumber);

    void goToToken(int newTokenNumber);

    boolean getTokenConnectionStatus();

    boolean isTokenPresent(int tokenNumber);
}