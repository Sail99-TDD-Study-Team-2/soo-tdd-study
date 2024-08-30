# Chapter 09. 테스트 범위와 종류

## 1) 테스트 범위

- 일반적인 웹 애플리케이션은 다음과 같은 구성 요소를 갖습니다:
  - 컨트롤러, 서비스, 모델 등의 자바 코드
  - 프레임워크 설정
  - 브라우저에서 실행되는 자바스크립트 코드
  - HTML과 CSS
  - 데이터베이스 테이블

- 하나의 기능이 올바르게 동작하려면 위의 모든 요소가 정상적으로 동작해야 합니다.

### 1. 기능 테스트와 E2E 테스트

- **기능 테스트 (Functional Testing)**:
  - 사용자 입장에서 시스템이 제공하는 기능이 올바르게 동작하는지 확인합니다.
  - 예: 회원 가입 기능이 올바르게 작동하는지 확인하려면 웹 서버, 데이터베이스, 웹 브라우저가 필요합니다.
  - 기능 테스트는 E2E(End to end) 테스트로 볼 수 있으며, QA 조직에서 수행하는 테스트가 주로 이에 해당합니다.

### 2. 통합 테스트

- **통합 테스트 (Integration Testing)**:
  - 시스템의 각 구성 요소가 올바르게 연동되는지 확인합니다.
  - 기능 테스트가 사용자 입장에서 테스트하는 것이라면, 통합 테스트는 소프트웨어의 코드를 직접 테스트합니다.
  - 예: 서버의 회원 가입 코드를 직접 테스트하는 방식.

### 3. 단위 테스트

- **단위 테스트 (Unit Testing)**:
  - 개별 코드나 컴포넌트가 기대한 대로 동작하는지 확인합니다.
  - 작은 범위를 테스트하며, 의존 대상은 스텁이나 모의 객체 등을 이용해 대체할 수 있습니다.

---

## 2) 테스트 범위 간 차이

- **통합 테스트**:
  - DB나 캐시 서버 등 연동 대상을 구성해야 하므로 준비 작업이 필요합니다.
  - DB 연결, 소켓 통신, 스프링 컨테이너 초기화 등으로 인해 테스트 실행 속도가 느려질 수 있습니다.

- **기능 테스트**:
  - 웹 서버를 구동하거나 모바일 앱을 설치해야 할 수도 있습니다.
  - 브라우저나 앱을 구동하고 화면의 흐름에 따라 상호작용을 테스트해야 하므로 시간이 많이 소요됩니다.

- **단위 테스트**:
  - 준비 작업이 거의 필요 없으며, 테스트 속도가 빠릅니다.
  - 서버를 구동하거나 DB를 준비할 필요가 없으므로 테스트 실행 속도가 빠르며, 다양한 상황을 다룰 수 있습니다.

- **테스트 범위에 따른 테스트 코드 개수와 시간**:
  - 기능 테스트나 통합 테스트에서 모든 예외 상황을 테스트하면 단위 테스트의 필요성이 줄어듭니다.
  - 테스트 속도는 통합 테스트보다 단위 테스트가 빠르므로, 단위 테스트에서 다양한 상황을 다루고 통합 테스트나 기능 테스트는 주요 상황에 집중해야 합니다.

---

## 3) 외부 연동이 필요한 테스트 예

### 1. 스프링 부트와 DB 통합 테스트

- **통합 테스트**:
  - 실제로 DB를 사용하여 여러 번 실행해도 같은 결과를 도출해야 합니다.
  - DB 데이터를 알맞게 제어해야 하며, 데이터 삽입이나 삭제를 통해 테스트 상황을 만듭니다.
  - 스프링 부트를 이용한 통합 테스트는 스프링 컨테이너 초기화가 필요하므로 시간이 더 소요됩니다.

- **단위 테스트**:
  - 대역을 사용하여 상황을 만들 수 있으며, 스프링 컨테이너 초기화 과정이 없으므로 시간이 짧습니다.

### 2. WireMock을 이용한 REST 클라이언트 테스트

- 외부 서버와의 통합 테스트가 어려운 경우, **WireMock**을 사용하여 서버 API를 스텁으로 대체할 수 있습니다.

```java
public class CardNumberValidatorTest {

    private WireMockServer wireMockServer;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(options().port(8089));
        wireMockServer.start();
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void valid() {
        wireMockServer.stubFor(post(urlEqualTo("/card"))
                .withRequestBody(equalTo("1234567890"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "text/plain")
                        .withBody("ok"))
        );

        CardNumberValidator validator =
                new CardNumberValidator("<http://localhost:8089>");
        CardValidity validity = validator.validate("1234567890");
        assertEquals(CardValidity.VALID, validity);
    }
}
```

- WireMock은 HTTP 서버를 흉내 내어 테스트 실행 전에 시작되고, 테스트가 끝나면 중지됩니다.
- JSON/XML 응답, HTTPS 지원 등 다양한 기능을 제공하여 외부 연동 코드를 테스트할 때 유용합니다.

### 3. 스프링 부트의 내장 서버를 이용한 API 기능 테스트

- 스프링 부트를 사용하면 내장 톰캣을 이용하여 API에 대한 테스트를 JUnit 코드로 작성할 수 있습니다.

```java
@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class UserApiE2ETest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void weakPwResponse() {
        String reqBody = "{\\"id\\": \\"id\\", \\"pw\\": \\"123\\", \\"email\\": \\"a@a.com\\" }";
        RequestEntity<String> request =
                RequestEntity.post(URI.create("/users"))
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .body(reqBody);

        ResponseEntity<String> response = restTemplate.exchange(
                request,
                String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("WeakPasswordException"));
    }
}
```

- 스프링 부트의 내장 서버를 사용하여 API 테스트를 실행할 수 있습니다.
- `TestRestTemplate`은 테스트 목적으로 제공되는 RestTemplate로, 내장 서버의 임의 포트에 맞춰 전송 요청을 할 수 있습니다.

---
