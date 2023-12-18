**JAVA17, Spring(boot), JPA, H2 Database**

## 문제 해결 전략
### 뿌리기 API
1. 요청값 검증
2. 요청에 맞게 'Spread' 객체를 생성, 뿌려진 금액에 대한 'SpreadDetail' 객체들 생성
3. 생성된 Spread의 token 값 응답

### 받기 API
1. 요청값 검증
2. 요청한 token의 Spread '받기' 가능 여부 확인
3. 요청한 token의 SpreadDetail 중 할당되지 않은 객체 할당
4. 할당된 객체의 distributedMoney 값 응답

### 조회 API
1. 요청값 검증
2. 요청한 token의 Spread '조회' 가능 여부 확인
3. 요청한 token의 Spread, SpreadDetail의 현재 상태 응답

#### 만료된 뿌리기(ExpiredSpread)
token 값은 3자리 무작위 문자열이며 고유해야 함.  
하지만 허용되는 문자열에 따라 다르지만 개수의 한계가 있음.  
ExpiredSpread는 Spread와 SpreadDetail의 정보를 한 객체에서 관리하며, 요청에 의한 사용 용도가 아닌 데이터 기록 용도
1. 뿌리기 건이 조회 만료 상태일 경우 Spread, SpreadDetail 테이블에서 제거한 후 ExpiredSpread 테이블로 데이터 이관
- 조회 요청시 조회 만료된 Spread라면 이관 로직 실행
- 정해진 시간마다 Scheduler를 실행하여 만료된 Spread 확인 후 이관 로직 실행

#### Work Flow
*Client* <-(HTTP)-> **Controller, Scheduler** <-(DTO, parameter)-> **Service** <-(Entity, parameter)-> **Repository** <-(Entity)-> *DB*