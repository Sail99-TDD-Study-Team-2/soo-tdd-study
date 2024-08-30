# Chapter 05. JUnit 5 모듈 구성 및 사용법

## 1) JUnit 5 모듈 구성

JUnit 5는 크게 세 가지 요소로 구성되어 있습니다.

- **JUnit 플랫폼**: 테스팅 프레임워크를 구동하기 위한 런처와 테스트 엔진을 위한 API를 제공합니다.
- **JUnit 주피터(Jupiter)**: JUnit 5를 위한 테스트 API와 실행 엔진을 제공합니다.
- **JUnit 빈티지(Vintage)**: JUnit 3과 4로 작성된 테스트를 JUnit 5 플랫폼에서 실행하기 위한 모듈을 제공합니다.

---

## 2) @Test 애노테이션과 테스트 메서드

- 테스트로 사용할 클래스를 만들고 `@Test` 애노테이션을 메서드에 붙이기만 하면 됩니다. 단, private 메서드는 적용이 안됩니다.
- JUnit의 `Assertions` 클래스는 `assertEquals()` 메서드와 같이 값을 검증하기 위한 다양한 정적 메서드를 제공합니다.

```java
@Test
void 만원_납부하면_한달_뒤가_만료일이_됨() {
    ExpiryDateCalculator cal = new ExpiryDateCalculator();
    LocalDate realExpiryDate = cal.calculateExpiryDate(payData);
    assertEquals(expectedExpiryDate, realExpiryDate);
}
```

### 주요 단언 메서드

| 메서드                                   | 설명                                          |
|----------------------------------------|---------------------------------------------|
| `assertEquals(expected, actual)`       | 실제 값(actual)이 기대하는 값(expected)과 같은지 검사합니다.   |
| `assertNotEquals(expected, actual)`    | 실제 값(actual)이 기대하는 값(expected)과 같지 않은지 검사합니다. |
| `assertSame(Object expected, Object actual)`  | 두 객체가 동일한 객체인지 검사합니다.                    |
| `assertNotSame(Object expected, Object actual)` | 두 객체가 동일하지 않은 객체인지 검사합니다.                |
| `assertTrue(boolean condition)`        | 값이 true인지 검사합니다.                            |
| `assertFalse(boolean condition)`       | 값이 false인지 검사합니다.                           |
| `assertNull(Object actual)`            | 값이 null인지 검사합니다.                            |
| `assertNotNull(Object actual)`         | 값이 null이 아닌지 검사합니다.                         |
| `fail()`                               | 테스트를 실패 처리합니다.                             |

---

## 3) 테스트 라이프사이클

### @BeforeEach, @AfterEach

JUnit은 각 테스트 메서드마다 다음 순서대로 코드를 실행합니다:

1. 테스트 메서드를 포함한 객체 생성
2. (존재하면) `@BeforeEach` 애노테이션이 붙은 메서드 실행
3. `@Test` 애노테이션이 붙은 메서드 실행
4. (존재하면) `@AfterEach` 애노테이션이 붙은 메서드 실행

- `@BeforeEach`: 테스트를 실행하는데 필요한 준비 작업을 할 때 사용합니다.
- `@AfterEach`: 테스트를 실행한 후에 정리할 것이 있을 때 사용합니다.

### @BeforeAll, @AfterAll

- `@BeforeAll`: 한 클래스의 모든 테스트 메서드가 실행되기 전에 특정 작업을 수행해야 할 때 사용합니다. 이 애노테이션은 정적 메서드에 붙이며 클래스의 모든 테스트 메서드를 실행하기 전에 한 번 실행됩니다.
- `@AfterAll`: 클래스의 모든 테스트 메서드를 실행한 뒤에 실행됩니다. 이 메서드 역시 정적 메서드에 적용합니다.

---

## 4) 테스트 메서드 간 실행 순서 의존과 필드 공유하지 않기

```java
public class BadTest {
    private FileOperator operator = new FileOperator();
    private static File file; // 두 테스트가 데이터를 공유할 목적으로 필드 사용
    
    @Test
    void fileCreationTest() {
        File createdFile = operator.createFile();
        assertTrue(createdFile.length() > 0);
        this.file = createdFile;
    }

    @Test
    void readFileTest() {
        long data = operator.readData(file);
        assertTrue(data >0);
    }
}
```

- 각 테스트 메서드는 서로 독립적으로 동작해야 합니다. 한 테스트 메서드의 결과에 따라 다른 테스트 메서드의 실행 결과가 달라지면 안 됩니다.
- 테스트 메서드가 서로 필드를 공유하거나 실행 순서를 가정하지 않고 작성해야 합니다.

---

## 5) 추가 애노테이션: @DisplayName, @Disabled

### @DisplayName

- 자바는 메서드 이름에 공백이나 특수 문자를 사용할 수 없기 때문에 메서드 이름만으로 테스트 내용을 설명하기가 부족할 수 있습니다. 이럴 때는 `@DisplayName` 애노테이션을 사용해서 테스트에 표시 이름을 붙일 수 있습니다.

```java
@DisplayName("약한 암호면 가입 실패")
@Test
void weakPassword() {
    stubPasswordChecker.setWeak(true);

    assertThrows(WeakPasswordException.class, () -> {
        userRegister.register("id", "pw", "email");
    });
}
```

### @Disabled

- 특정 테스트를 실행하고 싶지 않을 때는 `@Disabled` 애노테이션을 사용합니다. JUnit은 `@Disabled` 애노테이션이 붙은 클래스나 메서드는 테스트 실행 대상에서 제외합니다.

```java
@Disabled
@Test
void failMethod() {
    try {
        AuthService authService = new AuthService();
        authService.authenticate(null, null);
        fail();
    } catch(IllegalArgumentException e) {
    }
}
```

---