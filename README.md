# Redis

#### 1. 상담 대화 내용 이력 실시간 저장 Stream 저장 Redis 구축 테스트.
> 진행 과정
```bash
1. 각 상담에 대한 상담 내용(상담 문장)을 Redis 저장.
2. 상담이 끝이 됬을 경우 Redis 전체 에 대하여 RDB 에 저장.
```
> 전제 조건
```bash
1. Redis cluster를 이용하여 구성한다.
2. Redis Pub-Sub 모델 형식으로 데이터 스트림 방식 구축한다.
3. RDB로는 embedded DB인 H2를 사용하여 저장한다.
4. 요청 데이터는 상담사와 상담자 둘 중 하나의 데이터만이 들어오게 되며 공통 포맷이 존재한다.
5. 상담의 시작과 끝에 해당하는 요청일 경우, Flag 정보를 Request Header로 전달 한다.   
6. 하나의 상담에는 고유한 번호(SEQ_NO)가 존재하며, 상담의 타입(CALL_TYPE)도 존재하여 매 요청에 Request Header로 전달한다.
7. 상담사 요청 데이터 포맷에서는 상담사 이름(CNSL_NAME), 상담사 소속(CNSL_TEAM), 상담사의 대답(CONTENT), 시간(RESP_TIME) 가 존재한다.
8. 상담사 응답 데이터 포맷에서는 상담자 이름(USER_NAME), 상담자 번호(USER_NUMBER), 상담자의 대답(CONTENT), 시간(RESP_TIME) 가 존재한다.
9. 상담 내용을 JSON 형식으로 저장한다.
```
> 테이블 설계
- index는 고려하지 않음.(검색까지 고려하지 않기에.)
```mysql
CREATE TABLE CALL_HISTORY (
 SEQ_NO int(11) PRIMARY KEY,
 CALL_TYPE VARCHAR(10) NOT NULL,
 CNSL_NAME VARCHAR(10) NOT NULL,
 CNSL_TEAM VARCHAR(10) NOT NULL,
 USER_ID VARCHAR(10) NOT NULL ,
 USER_PHONE_NUMBER VARCHAR(10) NOT NULL ,
 CONTENT_JSON VARCHAR(600) NOT NULL ,
 START_TIME DATETIME NOT NULL,
 END_TIME DATETIME NOT NULL, 
)
```
