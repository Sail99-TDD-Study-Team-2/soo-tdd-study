# Chapter 07. 대역

## 1) 대역의 필요성

- 테스트를 작성하다 보면 외부 요인이 필요한 시점이 있습니다. 외부 요인이 테스트에 관여하는 주요 예는 다음과 같습니다:
  - 테스트 대상에서 파일 시스템을 사용
  - 테스트 대상에서 DB로부터 데이터를 조회하거나 추가
  - 테스트 대상에서 외부의 HTTP 서버와 통신
  

- 테스트 대상이 이러한 외부 요인에 의존하면 테스트 작성과 실행이 어려워집니다. 예를 들어, 카드 정보 검사 대행업체에서 테스트할 때 사용할 카드번호를 제공하지 않으면 테스트 진행이 불가능해질 수 있습니다.
- 외부 요인은 테스트 작성과 결과 예측을 어렵게 만듭니다. 예를 들어, 자동이체 정보 등록 기능 테스트는 한 달 뒤에 유효 기간이 만료되어 실패할 수 있습니다.

---

## 2) 대역을 이용한 테스트

- 대역을 이용하여 `AutoDebitRegister`를 테스트할 수 있습니다. 다음은 `CardNumberValidator`를 대신할 대역 클래스를 작성한 예시입니다:

```java
public class StubCardNumberValidator extends CardNumberValidator {
    private String invalidNo;
    private String theftNo;

    public void setInvalidNo(String invalidNo) {
        this.invalidNo = invalidNo;
    }

    public void setTheftNo(String theftNo) {
        this.theftNo = theftNo;
    }

    @Override
    public CardValidity validate(String cardNumber) {
        if (invalidNo != null && invalidNo.equals(cardNumber)) {
            return CardValidity.INVALID;
        }
        if (theftNo != null && theftNo.equals(cardNumber)) {
            return CardValidity.THEFT;
        }
        return CardValidity.VALID;
    }
}
```

- `StubCardNumberValidator`는 실제 카드번호 검증 기능을 구현하지 않고, 단순한 구현으로 대체합니다. 이를 이용해 `AutoDebitRegister`를 테스트할 수 있습니다:

```java
@BeforeEach
void setUp() {
    stubValidator = new StubCardNumberValidator();
    stubRepository = new StubAutoDebitInfoRepository();
    register = new AutoDebitRegister(stubValidator, stubRepository);
}

@Test
void invalidCard() {
    stubValidator.setInvalidNo("111122223333");

    AutoDebitReq req = new AutoDebitReq("user1", "111122223333");
    RegisterResult result = this.register.register(req);

    assertEquals(INVALID, result.getValidity());
}
```

---

## 3) 대역의 종류

- **스텁(Stub)**: 약한 암호 확인 기능에 스텁을 사용하여 단순히 특정 결과를 반환하도록 만듭니다.
  - `StubWeakPasswordChecker` 클래스를 생성하고, 테스트 클래스에 스텁 클래스를 주입하여 테스트를 수행합니다.

- **가짜 객체(Fake Object)**: 리포지토리를 가짜 구현으로 대체하여 메모리에서 동작하도록 합니다.
  - `MemoryUserRepository`를 생성하여 실제 DB 대신 메모리를 이용하도록 구현합니다.

- **스파이(Spy)**: 이메일 발송 여부를 확인하기 위해 스파이를 사용합니다.
  - `SpyEmailNotifier`를 생성하고, 내부 발송 여부를 확인할 수 있도록 합니다.

- **모의 객체(Mock Object)**: Mockito와 같은 라이브러리를 사용하여 스텁과 스파이를 대체할 수 있습니다.
  - 모의 객체를 사용하면 객체가 기대한 대로 호출되었는지 검증할 수 있습니다.

---

## 4) 상황과 결과 확인을 위한 협업 대상(의존) 도출과 대역 사용

- 제어하기 힘든 외부 상황이 존재할 경우, 다음과 같은 방법으로 의존을 도출하고 이를 대역으로 대체할 수 있습니다:
  - 제어하기 힘든 외부 상황을 별도의 타입으로 분리합니다.
  - 테스트 코드는 별도로 분리한 타입의 대역을 생성합니다.
  - 생성한 대역을 테스트 대상의 생성자 등을 통해 전달합니다.
  - 대역을 이용해 상황을 구성합니다.

---

## 5) 대역과 개발 속도

- TDD 과정에서 대역을 사용하지 않고 실제 구현을 사용한다면 다음과 같은 일이 발생할 수 있습니다:
  - 카드 정보 제공 업체에서 도난 카드번호를 받을 때까지 테스트를 기다려야 합니다.
  - 카드 정보 제공 API가 비정상 응답을 주는 상황을 테스트하기 위해 업체의 변경 대응을 기다려야 합니다.
  - 회원 가입 테스트 후 편지가 도착할 때까지 메일함을 확인해야 합니다.
  - 약한 암호 검사 기능을 개발할 때까지 회원 가입 테스트를 대기해야 합니다.
- 대역을 사용하면 실제 구현이 없어도 다양한 상황에 대해 테스트할 수 있으며, 이는 개발 속도를 올리는 데 도움이 됩니다.

---

## 6) 모의 객체를 과하게 사용하지 않기

- 모의 객체를 이용하면 대역 클래스를 만들지 않아도 되므로 처음에는 편리합니다. 하지만 결과값을 확인하는 수단으로 모의 객체를 사용하기 시작하면 결과 검증 코드가 길고 복잡해질 수 있습니다.
- 특히 하나의 테스트에서 여러 모의 객체를 사용하기 시작하면 코드의 복잡도는 배로 증가합니다. 모의 객체는 메서드 호출 여부를 검증하는 수단이기 때문에 상호작용이 바뀌면 테스트가 깨지기 쉽습니다.
- 따라서, 모의 객체의 메서드 호출 여부를 결과 검증 수단으로 사용하는 것은 주의해야 합니다. 특히, DAO나 리포지토리와 같은 저장소에 대한 대역은 모의 객체보다는 메모리를 이용한 가짜 구현을 사용하는 것이 테스트 코드 관리에 유리합니다.

---