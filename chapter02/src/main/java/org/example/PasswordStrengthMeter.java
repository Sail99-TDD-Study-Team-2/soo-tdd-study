package org.example;

public class PasswordStrengthMeter {
    // 비밀번호 강도를 측정하는 메서드
    public PasswordStrength meter(String s) {
        if (isInvalid(s)) return PasswordStrength.INVALID;
        if (isWeak(s)) return PasswordStrength.WEAK;
        if (isNormal(s)) return PasswordStrength.NORMAL;
        return PasswordStrength.STRONG;
    }

    // 비밀번호가 유효하지 않은지 확인하는 메서드
    private boolean isInvalid(String s) {
        return (s == null || s.isEmpty());
    }

    // 비밀번호가 약한지 확인하는 메서드
    private boolean isWeak(String s) {
        return (getMetCriteriaCounts(s) <= 1);
    }

    // 비밀번호가 보통인지 확인하는 메서드
    private boolean isNormal(String s) {
        return (getMetCriteriaCounts(s) == 2);
    }

    // 비밀번호 강도 조건 충족 수를 계산하는 메서드
    private int getMetCriteriaCounts(String s) {
        int metCounts = 0;
        if (meetsLengthCriteria(s)) metCounts++;
        if (meetsContainingNumberCriteria(s)) metCounts++;
        if (meetsContainingUppercaseCriteria(s)) metCounts++;
        return metCounts;
    }

    // 비밀번호 길이 조건을 확인하는 메서드
    private boolean meetsLengthCriteria(String s) {
        return s.length() >= 8;
    }

    // 숫자 포함 조건을 확인하는 메서드
    private boolean meetsContainingNumberCriteria(String s) {
        for (char ch : s.toCharArray()) {
            if (ch >= '0' && ch <= '9') {
                return true;
            }
        }
        return false;
    }

    // 대문자 포함 조건을 확인하는 메서드
    private boolean meetsContainingUppercaseCriteria(String s) {
        for (char ch : s.toCharArray()) {
            if (Character.isUpperCase(ch)) {
                return true;
            }
        }
        return false;
    }
}
