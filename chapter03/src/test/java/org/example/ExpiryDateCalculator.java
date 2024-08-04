package org.example;

import java.time.LocalDate;
import java.time.YearMonth;

public class ExpiryDateCalculator {

    /**
     * 결제 데이터를 기반으로 만료일을 계산합니다.
     */
    public LocalDate calculateExpiryDate(PayData payData) {
        int addedMonths = calculateAddedMonths(payData.getPayAmount());
        if (payData.getFirstBillingDate() != null) {
            return calculateExpiryDateUsingFirstBillingDate(payData, addedMonths);
        } else {
            return calculateExpiryDateWithoutFirstBillingDate(payData, addedMonths);
        }
    }

    /**
     * 결제 금액을 기준으로 추가될 개월 수를 계산합니다.
     */
    private int calculateAddedMonths(int payAmount) {
        return payAmount == 100_000 ? 12 : payAmount / 10_000;
    }

    /**
     * 첫 결제일을 사용하지 않고 만료일을 계산합니다.
     */
    private LocalDate calculateExpiryDateWithoutFirstBillingDate(PayData payData, int addedMonths) {
        return payData.getBillingDate().plusMonths(addedMonths);
    }

    /**
     * 첫 결제일을 사용하여 만료일을 계산합니다.
     */
    private LocalDate calculateExpiryDateUsingFirstBillingDate(PayData payData, int addedMonths) {
        LocalDate candidateExp = payData.getBillingDate().plusMonths(addedMonths);
        final int dayOfFirstBilling = payData.getFirstBillingDate().getDayOfMonth();

        if (isDifferentDayOfMonth(candidateExp, dayOfFirstBilling)) {
            return adjustDayOfMonth(candidateExp, dayOfFirstBilling);
        } else {
            return candidateExp;
        }
    }

    /**
     * 만료일의 일(day)과 첫 결제일의 일이 다른지 확인합니다.
     */
    private boolean isDifferentDayOfMonth(LocalDate candidateExp, int dayOfFirstBilling) {
        return dayOfFirstBilling != candidateExp.getDayOfMonth();
    }

    /**
     * 만료일의 일을 첫 결제일의 일로 조정합니다.
     */
    private LocalDate adjustDayOfMonth(LocalDate candidateExp, int dayOfFirstBilling) {
        final int dayLenOfCandiMon = YearMonth.from(candidateExp).lengthOfMonth();

        if (dayLenOfCandiMon < dayOfFirstBilling) {
            return candidateExp.withDayOfMonth(dayLenOfCandiMon);
        }

        return candidateExp.withDayOfMonth(dayOfFirstBilling);
    }
}
