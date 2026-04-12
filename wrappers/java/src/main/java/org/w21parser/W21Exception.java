package org.w21parser;

public class W21Exception extends Exception {
    public final int error;
    private final String faultstring;
    private final String XMLfaultdetail;
    W21Exception(
            String message, int error,
            String faultstring, String XMLfaultdetail
    ) {
        super(message);
        this.error = error;
        this.faultstring = (faultstring != null)?faultstring:"";
        this.XMLfaultdetail = (XMLfaultdetail != null)?XMLfaultdetail:"";
    }

    public String getFaultstring() { return this.faultstring; }
    public String getXMLfaultdetail() { return this.XMLfaultdetail; }
}
