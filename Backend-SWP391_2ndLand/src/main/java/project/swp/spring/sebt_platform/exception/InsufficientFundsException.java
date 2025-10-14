package project.swp.spring.sebt_platform.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {
    private final BigDecimal required;
    private final BigDecimal current;

    public InsufficientFundsException(BigDecimal required, BigDecimal current) {
        super("Insufficient funds. Required=" + required + ", current=" + current);
        this.required = required;
        this.current = current;
    }

    public BigDecimal getRequired() { return required; }
    public BigDecimal getCurrent() { return current; }
}
