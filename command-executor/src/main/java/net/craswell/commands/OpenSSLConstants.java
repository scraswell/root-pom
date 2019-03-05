package net.craswell.commands;

public class OpenSSLConstants {
    /**
     * The URL for the Mozilla managed trust store.
     */
    public static final String MOZILLA_TRUST_STORE = "https://hg.mozilla.org/mozilla-central/raw-file/tip/security/nss/lib/ckfw/builtins/certdata.txt";

    /**
     * The CURL project's version of the above.
     */
    public static final String CURL_PROJECT_VERSION = "http://curl.haxx.se/docs/caextract.html";

    private OpenSSLConstants() {
    }
}
