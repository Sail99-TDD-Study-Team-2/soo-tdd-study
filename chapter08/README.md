# Chapter 08. 테스트 가능한 설계

## 1) 테스트가 어려운 코드

- 모든 코드를 테스트할 수 있는 것은 아닙니다. 개발을 진행하다 보면 테스트하기 어려운 코드를 만나게 됩니다. 대표적인 예는 다음과 같습니다:

### 1. 하드 코딩된 경로

- 파일 경로, IP 주소, 포트 번호 등이 하드 코딩된 경우 테스트가 어렵습니다.

```java
public class PaySync {
    private PayInfoDao payInfoDao = new PayInfoDao();

    public void sync() throws IOException {
        Path path = Paths.get("/data/pay/cp0001.csv");
        List<PayInfo> payInfos = Files.lines(path);
        ...
    }
}
```

- 이 코드를 테스트하려면 해당 경로에 파일이 반드시 위치해야 합니다. 특정 환경(예: 윈도우의 D 드라이브)이 아닌 경우 테스트가 불가능해집니다.

### 2. 의존 객체를 직접 생성

- 의존 객체를 직접 생성하면 테스트가 어렵습니다. 예를 들어, `PaySync` 클래스에서 `PayInfoDao`를 직접 생성하는 경우 테스트 시 필요한 모든 환경(DB, 테이블 등)을 구성해야 합니다.

```java
public class PaySync {
    private PayInfoDao payInfoDao = new PayInfoDao();
    ...
}
```

### 3. 정적 메서드 사용

- 정적 메서드를 사용하면 테스트가 어려워질 수 있습니다. 외부 서버와 통신하는 정적 메서드를 사용하는 경우, 테스트 환경에 맞는 서버가 필요합니다.

```java
public class LoginService {
    private String authKey = "somekey";
    private CustomerRepository customerRepo;

    public LoginService(CustomerRepository customerRepo) {
        this.customerRepo = customerRepo;
    }

    public LoginResult login(String id, String pw) {
        boolean authorized = AuthUtil.authorize(authKey);
        ...
    }
}
```

### 4. 실행 시점에 따라 달라지는 결과

- `LocalDate.now()`나 `Random`을 사용하는 경우, 실행 시점에 따라 테스트 결과가 달라질 수 있습니다. 이는 테스트의 신뢰성을 떨어뜨립니다.

```java
public int calculatePoint(User u) {
    LocalDate now = LocalDate.now();
    ...
}
```

### 5. 역할이 섞여 있는 코드

- 여러 역할이 섞여 있는 코드는 특정 기능만 따로 테스트하기가 쉽지 않습니다. 의존 관계를 적절히 설정해야만 합니다.

### 6. 그 외 테스트가 어려운 코드

- 메서드 중간에 소켓 통신 코드가 포함된 경우
- 콘솔에서 입력을 받거나 결과를 콘솔에 출력하는 경우
- 테스트 대상이 사용하는 의존 대상 클래스나 메서드가 `final`인 경우
- 테스트 대상의 소스를 소유하고 있지 않아 수정이 어려운 경우

---

## 2) 테스트 가능한 설계

- 테스트가 어려운 주된 이유는 의존하는 코드를 교체할 수 있는 수단이 없기 때문입니다. 상황에 따라 알맞은 방법을 적용하여 의존 코드를 교체할 수 있게 만들 수 있습니다.

### 1. 하드 코딩된 상수를 생성자나 메서드 파라미터로 받기

- 하드 코딩된 상수를 교체할 수 있도록 생성자나 세터를 이용해 전달받도록 수정합니다.

```java
public class PaySync {
    private PayInfoDao payInfoDao = new PayInfoDao();
    private String filePath = "D:\\data\\pay\\cp0001.csv";

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    ...
}
```

### 2. 의존 대상을 주입 받기

- 의존 대상은 생성자나 세터를 통해 주입받아 교체할 수 있도록 합니다.

### 3. 테스트하고 싶은 코드를 분리하기

- 기능의 일부만 테스트하고 싶다면 해당 코드를 별도의 기능으로 분리하여 테스트를 진행합니다.

### 4. 시간이나 임의 값 생성 기능 분리하기

- 시간(`LocalDate.now()`)이나 임의 값(`Random`)을 사용하는 코드는 별도의 클래스로 분리하여 테스트에서 제어할 수 있도록 합니다.

### 5. 외부 라이브러리는 직접 사용하지 말고 감싸서 사용하기

- 외부 라이브러리가 정적 메서드를 제공하는 경우, 외부 라이브러리와 연동하기 위한 타입을 따로 만들어 대역으로 대체할 수 있도록 합니다.

```java
// 변경 전 코드
public class LoginService {
    private String authKey = "somekey";
    private CustomerRepository customerRepo;

    public LoginService(CustomerRepository customerRepo) {
        this.customerRepo = customerRepo;
    }

    public LoginResult login(String id, String pw) {
        boolean authorized = AuthUtil.authorize(authKey); // 대체 불가능한 외부 라이브러리
        ...
    }
}

// 변경 후 코드
public class LoginService {
    private AuthService authService; // 외부 라이브러리 대신 사용할 타입 주입
    private CustomerRepository customerRepo;

    public LoginService(CustomerRepository customerRepo, AuthService authService) {
        this.customerRepo = customerRepo;
        this.authService = authService;
    }

    public LoginResult login(String id, String pw) {
        boolean authorized = authService.authorize(authKey); // 스텁으로 대체 가능
        ...
    }
}
```

### 6. Final 클래스나 메서드의 대체

- 의존 대상이 `final` 클래스이거나 호출 메서드가 `final`인 경우에도 외부 타입을 감싸는 방식을 적용해 테스트할 수 있습니다.

---

