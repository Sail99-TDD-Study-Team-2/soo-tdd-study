# Chapter 02. TDD 시작

## 1) TDD란?
- TDD(Test-Driven Development)는 테스트를 먼저 작성하는 소프트웨어 개발 방법론입니다. 기능을 검증하는 테스트 코드를 먼저 작성하고, 이 테스트를 통과시키기 위해 필요한 최소한의 코드를 작성합니다.

---
## 2) 간단한 덧셈 기능을 TDD로 구현하기

### 1. 테스트 코드 작성
- Calculator 클래스를 작성하지 않았으니 당연히 컴파일 에러가 발생해야 합니다.
```java
public class CalculatorTest {

    @Test
    void plus() {
        int result = Calculator.plus(1, 2);
        assertEquals(3, result);
    }
}
```

### 2. 구현할 메서드에 대해 고민하기

- 덧셈 메서드 이름은 `plus`가 좋을까? 아니면 `sum`이 좋을까?
- 덧셈 기능을 제공하는 메서드는 파라미터가 몇 개여야 할까? 파라미터의 타입은? 반환할 값은?
- 메서드를 정적 메서드로 구현할까 인스턴스 메서드로 구현할까?
- 메서드를 제공할 클래스 이름은 뭐가 좋을까?

### 3. Calculator 클래스 구현
(1) 우선 테스트 코드에서 사용하는 스펙을 만들어주고 내부 구현은 0으로 리턴하도록 작성합니다. 그리고 테스트 코드를 실행하면 실패가 발생합니다.

```java
public class Calculator {
    public static int plus(int a1, int a2) {
        return 0;
    }
}
```

(2) 실패된 테스트 케이스를 해결하기 위해 결과값을 3으로 변경합니다. 테스트 케이스는 성공합니다. 이제 덧셈 검증 코드를 하나 더 추가합니다.

```java
public class Calculator {
    public static int plus(int a1, int a2) {
        return 3;
    }
}
```

(3) 해당 테스트 실행 시 실패 결과를 확인할 수 있습니다.

```java
@Test
void plus() {
    int result = Calculator.plus(1, 2);
    assertEquals(3, result);
    assertEquals(5, Calculator.plus(4, 1));
}
```

(4) 두 테스트 케이스를 만족하는 덧셈 로직으로 구현합니다. 그러나 이렇게 테스트 케이스를 추가하다 보면 결국 아래와 같은 구현 코드가 작성될 것입니다.

```java
public class Calculator {
    public static int plus(int a1, int a2) {
        if (a1 == 4 && a2 == 1) return 5;
        else return 3;
    }
}
```

(5) 덧셈 기능에 대한 TDD로 구현해 봤습니다.
```java
public class Calculator {
    public static int plus(int a1, int a2) {
        return a1 + a2;
    }
}
```

---
## 3) TDD 예: 암호 검사기

- 암호 검사기는 문자열을 검사해서 규칙을 준수하는지에 따라 암호를 '약함', '보통', '강함'으로 구분합니다.

### 1. 검사할 규칙
1. 길이가 8글자 이상
2. 0부터 9 사이의 숫자를 포함
3. 대문자 포함

### 2. 규칙에 따른 분류
- 세 규칙을 모두 충족하면 암호는 강함입니다.
- 두 개의 규칙을 충족하면 암호는 보통입니다.
- 한 개 이하의 규칙을 충족하면 암호는 약함입니다.

### 3. 테스트 코드 작성

```java
public class PasswordStrengthMeterTest {
    @Test
    void name() { }
}
```

(1) 첫 번째 테스트: 모든 규칙을 충족하는 경우

```java
@Test
void meetsAllCriteria_Then_Strong() {
    private PasswordStrengthMeter meter = new PasswordStrengthMeter();
    PasswordStrength result = meter.meter("ab12!@AB");
    assertEquals(PasswordStrength.STRONG, result);
}
```

(2) PasswordStrengthMeter 타입과 PasswordStrength 타입이 존재하지 않으므로 컴파일 에러가 발생합니다. 이를 해결하기 위해 타입을 생성합니다.

```java
public enum PasswordStrength {
    STRONG
}

public class PasswordStrengthMeter {
    public PasswordStrength meter(String s) { return null; }
}
```

### 4. 모든 규칙을 충족하는 테스트 통과

```java
public class PasswordStrengthMeter {
    public PasswordStrength meter(String s) { return PasswordStrength.STRONG; }
}
```

### 5. 조건별 테스트 코드 작성 및 구현
- 각 조건별로 테스트 코드를 작성하고, 해당 조건을 통과하도록 구현 코드를 작성합니다. 이를 통해 점진적으로 기능을 완성합니다.

#### 예시)

- 길이만 8글자 미만이고 나머지 조건을 충족하는 경우

```java
@Test
void meetsOtherCriteria_except_for_length_Then_Normal() {
    private PasswordStrengthMeter meter = new PasswordStrengthMeter();
    PasswordStrength result = meter.meter("ab!@ABqwer");
    assertStrength(result, PasswordStrength.NORMAL);
}
```

- 구현 코드

```java
public class PasswordStrengthMeter {
    public PasswordStrength meter(String s) { 
        if (s.length() < 8) {
            return PasswordStrength.NORMAL;
        }
        return PasswordStrength.STRONG; 
    }
}
```

### 6. 코드 정리 및 리팩토링

- 테스트가 모두 통과한 후에는 코드를 리팩토링하여 가독성을 높이고 중복을 제거합니다.
---
## 4) TDD 내용 정리
### 1. TDD 흐름
- TDD는 기능을 검증하는 테스트를 먼저 작성하고, 테스트를 통과하기 위한 최소한의 코드를 작성한 후, 코드를 리팩토링하여 개선하는 과정을 반복합니다. 이를 통해 점진적으로 기능을 완성해 나갑니다.

### 2. 레드 - 그린 - 리팩터
- **레드(Red)**: 실패하는 테스트를 작성합니다.
- **그린(Green)**: 테스트를 통과시키기 위한 코드를 작성합니다.
- **리팩터(Refactor)**: 코드를 정리하고 개선합니다.

### 3. 테스트가 개발을 주도
- 테스트를 먼저 작성하면 테스트가 개발을 주도하게 됩니다. 테스트를 작성하는 과정에서 구현을 생각하지 않고 기능이 올바르게 동작하는지 검증할 수 있습니다.

### 4. 지속적인 코드 정리
- 구현을 완료한 후에는 리팩토링을 진행하여 코드 품질을 유지합니다. 테스트 코드가 있으면 리팩토링을 보다 과감하게 진행할 수 있습니다.

### 5. 빠른 피드백
- TDD는 코드 수정에 대한 피드백을 빠르게 제공합니다. 새로운 코드를 추가하거나 기존 코드를 수정하면 테스트를 돌려서 해당 코드가 올바른지 바로 확인할 수 있습니다.

---
## 5) 결론
- TDD는 테스트를 먼저 작성하고, 테스트를 통과시키기 위한 최소한의 코드를 작성한 후, 코드를 리팩토링하여 점진적으로 기능을 완성해 나가는 개발 방법론입니다. 이를 통해 코드 품질을 유지하고, 빠른 피드백을 받을 수 있습니다.