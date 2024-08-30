# Chapter 10. 테스트 코드와 유지보수

## 1) 테스트 코드와 유지보수

- TDD로 작성한 테스트 코드는 CI/CD에서 자동화 테스트로 사용되어, 버그가 배포되는 것을 막아 소프트웨어 품질이 저하되는 것을 방지합니다.
- 테스트 코드는 유지보수 대상이기 때문에 방치하면 다음과 같은 문제가 발생할 수 있습니다:
  - 실패한 테스트가 새로 발생해도 무감각해집니다.
  - 실패한 테스트를 주석 처리하고 고치지 않은 채 빌드하고 배포하게 됩니다.
  - 회귀 테스트가 검증하는 범위가 줄어들어 소프트웨어 품질이 낮아질 가능성이 큽니다.
- 좋은 테스트 코드를 만들기 위해 몇 가지 주의해야 할 사항이 있습니다.

### 깨진 유리창 이론

- 작은 무질서를 방치하면 큰 문제로 이어질 가능성이 크다는 이론입니다.
- 테스트 코드에서 깨진 유리창과 같은 테스트 실패를 방치하면 시스템의 무질서로 이어질 수 있습니다.

### 변수나 필드를 사용해서 기댓값 표현하지 않기

- 테스트에서 `get` 메서드 등을 사용하여 기댓값을 표현하는 대신 상수를 사용하는 것이 가독성이 좋습니다.

```java
// 안좋은 사례
@Test
void dateFormat() {
    LocalDate date = LocalDate.of(1945,8,15);
    String dateStr = formatDate(date);
    assertEquals(date.getYear() + "년 " + date.getMonthValue() + "월 " + date.getDayOfMonth() + "일", dateStr);
}

// 개선된 사례
@Test
void dateFormat() {
    LocalDate date = LocalDate.of(1945,8,15);
    String dateStr = formatDate(date);
    assertEquals("1945년 8월 15일", dateStr);
}
```

### 두 개 이상을 검증하지 않기

- 한 테스트 메서드에서 가능한 많은 단언을 하는 대신, 검증 대상이 명확하게 구분된다면 테스트 메서드도 구분하는 것이 유지보수에 유리합니다.

### 정확하게 일치하는 값으로 모의 객체 설정하지 않기

- 특정 값에 의존하는 모의 객체 설정은 테스트가 작은 변화에도 실패하게 만듭니다. 대신, 범용적인 설정을 사용합니다.

```java
// 안좋은 사례
BDDMockito.given(mockPasswordChecker.checkPasswordWeak("pw")).willReturn(true);

// 개선된 사례
BDDMockito.given(mockPasswordChecker.checkPasswordWeak(Mockito.anyString())).willReturn(true);
```

### 과도하게 구현 검증하지 않기

- 테스트 코드는 내부 구현이 아닌 실행 결과를 검증해야 합니다. 내부 구현 검증은 코드 변경에 취약하게 만들 수 있습니다.

### 셋업을 이용해서 중복된 상황을 설정하지 않기

- `@BeforeEach` 메서드를 사용하여 중복된 상황을 구성하면 나중에 테스트 코드를 분석할 때 어려워질 수 있습니다. 모든 테스트 메서드가 동일한 상황을 공유하게 되므로 코드 유지보수에 불리할 수 있습니다.

### 통합 테스트의 상황 설정을 위한 보조 클래스 사용하기

- 상황을 설정하기 위해 보조 클래스를 사용하면 중복을 줄이고 코드 품질을 높일 수 있습니다.

```java
@BeforeEach
void setUp() {
    given = new UserGivenHelper(jdbcTemplate);
}

@Test
void 동일ID가_이미_존재하면_익셉션() {
    given.givenUser("cbk", "pw", "cbk@cbk.com");
    assertThrows(DupIdException.class, () -> register.register("cbk", "strongpw", "email@email.com"));
}
```

---

## 2) 실행 환경과 시점에 독립적인 테스트

- **실행 환경이 달라도 실패하지 않기**: 파일 경로나 OS 환경에 따라 테스트가 달라지지 않도록 작성합니다.

```java
// 개선된 사례
@Test
void export() {
    String folder = System.getProperty("java.io.tmpdir");
    Exporter exporter = new Exporter(folder);
    ...
}
```

- **실행 시점이 달라도 실패하지 않기**: 시간이나 날짜에 의존하지 않는 테스트 코드를 작성합니다.

```java
// 개선된 사례
@Test
void notExpired() {
    LocalDateTime expiry = LocalDateTime.of(2019,12,31,0,0,0);
    Member m = Member.builder().expiryDate(expiry).build();
    assertFalse(m.passedExpiryDate(LocalDateTime.of(2019,12,31,0,0,0)));
}
```

### 랜덤하게 실패하지 않기

- 랜덤 값에 의존하는 테스트 코드는 구조를 변경하여 테스트가 실패하지 않도록 합니다.

```java
// 개선된 사례
public Game() {
    this.nums = nums;
}
```

### 단위 테스트를 위한 객체 생성 보조 클래스

- 단위 테스트에서 객체 생성이 복잡할 경우, 테스트 객체를 쉽게 생성할 수 있는 팩토리 클래스를 사용합니다.

```java
public class TestSurveyFactory {
    public static Survey createAnswerableSurvey(Long id) {
        return Survey.builder()
                .id(id).status(SurveyStatus.OPEN)
                .endOfPeriod(LocalDateTime.now().plusDays(5)).build();
    }
}
```

### 조건부로 검증하지 않기

- 테스트는 항상 성공하거나 실패해야 하며, 조건에 따라 검증하지 않아야 합니다.

```java
// 개선된 사례
@Test
void canTranslateBasicWord() {
    Translator tr = new Translator();
    assertTranslationOfBasicWord(tr, "cat");
}

private void assertTranslationOfBasicWord(Translator tr, String word) {
    assertTrue(tr.contains("cat"));
    assertEquals("고양이", tr.translate("cat"));
}
```

### 통합 테스트는 필요하지 않은 범위까지 연동하지 않기

- `@SpringBootTest` 대신, 더 가벼운 애노테이션(`@JdbcTest`)을 사용하여 테스트 범위를 최소화합니다.

### 더 이상 쓸모없는 테스트 코드

- 사용법을 익히기 위해 작성한 테스트 코드나 유지할 필요가 없는 테스트 코드는 삭제하여 코드베이스를 깔끔하게 유지합니다.

---
