package until.the.eternity.dcs.demo.controller;

import static until.the.eternity.dcs.demo.controller.DemoDomainExceptionCode.DEMO_DOMAIN_NOT_FOUND_EXCEPTION;

import until.the.eternity.dcs.common.exception.CustomException;

/** 예시 Exception 입니다. CustomException을 상속받아 구현합니다. */
public class DemoException extends CustomException {
    public DemoException() {
        super(DEMO_DOMAIN_NOT_FOUND_EXCEPTION);
    }
}
