package com.billermanagement.services.handler;

public interface HandlerConstant {
    int CALLBACK = 0;
    int IRS = 1;
    int BS = 2;
    int IM = 3;
    int BNI= 4;
    int IDN= 5;
    int MP= 6;
    int XF= 7;
    int BTN= 8;
    int BRI= 9;
    int CIMB= 10;
    int XFT= 11;
    int IMINQ = 12;

    int SUCCESS = 0;
    int IN_PROCESS = 99;
    int MALFORMED_REQUEST = 1000;
    int INVALID_PARAMETER_FORMAT = 1001;
    int REQUEST_ERRORS = 1099;
    int MISSING_TRANSACTION = 1100;
    int TRANSACTIONID_ERRORS = 1199;
    int OPERATION_TIMEOUT = 1200;
    int BACKEND_ERROR = 1299;
    int OTHER_ERROR = 2000;
}
