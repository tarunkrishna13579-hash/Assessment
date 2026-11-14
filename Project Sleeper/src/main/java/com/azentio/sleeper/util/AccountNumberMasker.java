package com.azentio.sleeper.util;

public class AccountNumberMasker {

    private AccountNumberMasker() {
    }

    public static String mask(String accountNumber) {

        if (accountNumber == null) {
            return null;
        }

        if (accountNumber.length() <= 4) {
            return "XXXXXXXX" + accountNumber;
        }

        return "XXXXXXXX" +
                accountNumber.substring(accountNumber.length() - 4);
    }

    public static String maskEpisodeKey(String episodeKey) {

        if (episodeKey == null) {
            return null;
        }

        int separatorIndex = episodeKey.indexOf("|");

        if (separatorIndex == -1) {
            return mask(episodeKey);
        }

        String accountNumber =
                episodeKey.substring(0, separatorIndex);

        return mask(accountNumber) +
                episodeKey.substring(separatorIndex);
    }
}