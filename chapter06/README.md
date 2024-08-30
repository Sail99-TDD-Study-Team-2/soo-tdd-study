# Chapter 06. 테스트 코드의 구성

## 1) 기능에서의 상황

- 기능은 주어진 상황에 따라 다르게 동작합니다. 예를 들어, 파일에서 숫자를 읽어와 숫자의 합을 구하는 기능이 있다고 가정해 봅시다. 이 기능을 구현하려면 다음과 같은 상황을 고려해야 합니다:
  - 파일이 없는 경우: 데이터 파일이 없으면 인자가 잘못되었다는 예외를 발생시켜야 합니다.
  - 숫자가 아닌 잘못된 데이터가 있는 경우: 적절한 예외 처리나 값을 리턴해야 합니다.

---

## 2) 테스트 코드의 구성 요소: 상황, 실행, 결과 확인

- 테스트 코드는 기능을 실행하고 그 결과를 확인하기 때문에 **상황 (Given), 실행 (When), 결과 확인 (Then)**의 세 가지 요소로 구성됩니다.
- 기본 골격은 다음과 같습니다:
  - **Given**: 상황 설정
  - **When**: 기능 실행
  - **Then**: 결과 확인

### 예시 1: 상황이 없는 경우

```java
@Test
void meetsAllCriteria_Then_Strong() {
    assertStrength("ab12!@AB", PasswordStrength.STRONG);
    assertStrength("abc1!Add", PasswordStrength.STRONG);
}
```

- 암호 강도 측정의 경우, 결과에 영향을 주는 특정 상황이 없으므로 기능 실행과 결과 확인만 필요합니다.

### 예시 2: 결과값이 항상 존재하는 것이 아닌 경우

```java
@Test
void getnGame_With_DupNumber_Then_File() {
    assertThrows(IllegalArgumentException.class, () -> new BaseballGame("100"));
}
```

- 실행 결과로 예외가 발생하는 것이 정상인 경우도 있습니다.

> 상황-실행-결과 확인 구조에 너무 집착하지 마십시오. 이 구조는 테스트 코드 작성에 도움이 되지만, 모든 테스트 메서드가 이 구조를 따라야 하는 것은 아닙니다.

---

## 3) 외부 상황과 외부 결과

- 상황 설정은 테스트 대상에 국한되지 않습니다. 외부 요인도 상황에 포함될 수 있습니다. 예를 들어, `MathUtils.sum()` 메서드를 테스트할 때 파일이 없는 상황에서의 결과도 확인해야 합니다.

### 예시 1: 파일이 없는 상황을 명시적으로 만드는 코드

```java
@Test
void noDataFile_Then_Exception() {
    givenNoFile("badpath.txt");

    File dataFile = new File("badpath.txt");
    assertThrows(IllegalArgumentException.class, () -> MathUtils.sum(dataFile));
}

private void givenNoFile(String path) {
    File file = new File(path);
    if (file.exists()) {
        boolean deleted = file.delete();
        if (!deleted)
            throw new RuntimeException("fail givenNoFile: " + path);
    }
}
```

- `givenNoFile()` 메서드는 파일이 존재하면 삭제하여 파일이 없는 상황을 만듭니다.

### 예시 2: 상황에 맞는 파일을 생성하는 코드

```java
@Test
void dataFileSumTest2() {
    givenDataFile("target/datafile.txt", "1", "2", "3", "4");
    File dataFile = new File("src/test/resources/datafile.txt");
    long sum = MathUtils.sum(dataFile);
    assertEquals(10L, sum);
}

private void givenDataFile(String path, String... lines) {
    try {
        Path dataPath = Paths.get(path);
        if (Files.exists(dataPath)) {
            Files.delete(dataPath);
        }
        Files.write(dataPath, Arrays.asList(lines));
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}
```

- `givenDataFile()` 메서드는 테스트에 필요한 데이터를 가진 파일을 생성합니다.

---

## 4) 외부 상태가 테스트 결과에 영향을 주지 않게 하기

- 테스트 코드는 반복적으로 실행되며, 외부 상태에 의존하지 않아야 합니다. 테스트 실행 전에 외부 상태를 원하는 상태로 설정하거나, 테스트 실행 후에 외부 상태를 원래대로 되돌려 놓아야 합니다.

### (1) 외부 상태와 테스트의 어려움

- 외부 요인(파일, DBMS, 외부 서버 등)이 테스트 결과에 영향을 줄 수 있습니다.
- 예를 들어, 자동이체 등록 기능에서 REST API를 사용하는 경우 다음과 같은 상황을 고려해야 합니다:
  - REST API 응답 결과가 유효한 계좌 번호인 상황
  - REST API 응답 결과가 유효하지 않은 계좌 번호인 상황
  - REST API 서버에 연결할 수 없는 상황
  - REST API 서버에서 응답을 5초 이내에 받지 못하는 상황

### (2) 외부 시스템에 기록되는 실행 결과의 어려움

- 예를 들어, 외부 택배사 DB 테이블을 사용하는 경우 DELETE 권한이 없으면 동일한 테스트를 여러 번 수행하기 어렵습니다.

> 외부 요인이 테스트 대상의 상황과 결과에 관여할 경우, **대역**을 사용하여 테스트 작성이 쉬워질 수 있습니다. 대역에 대한 자세한 내용은 7장에서 다룹니다.

---