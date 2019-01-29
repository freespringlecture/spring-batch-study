# Spring Batch 가이드 스터디
창천향로 이동욱님의 Spring Batch 가이드 스터디  

해당 스터디 진행도중 최신 기술 스펙에서 오류가 나는 부분 해결하면서 진행

각 단계별로 따라하며 브랜치 관리


### 적용 기술 스팩
- IntelliJ IDEA 2018.3
- Spring Boot 2.1.2
- Java 11
- Gradle

### 수업 GitHub 주소
https://github.com/jojoldu/spring-batch-in-action

### 수업 커리큘럼
1. [Spring Batch 가이드 - 배치 어플리케이션이란?](https://jojoldu.tistory.com/324)

2. [Spring Batch 가이드 - Batch Job 실행해보기](https://jojoldu.tistory.com/325)
   
3. [Spring Batch 가이드 - 메타테이블엿보기](https://jojoldu.tistory.com/326)

4. [Spring Batch 가이드 - Spring Batch Job Flow](https://jojoldu.tistory.com/328)

5. [Spring Batch 가이드 - Spring Batch Scope & Job Parameter](https://jojoldu.tistory.com/330)

6. [Spring Batch 가이드 - Chunk 지향 처리](https://jojoldu.tistory.com/331)

7. [Spring Batch 가이드 - ItemReader](https://jojoldu.tistory.com/336)

8. [Spring Batch 가이드 - ItemWriter](https://jojoldu.tistory.com/339)

9. [Spring Batch 가이드 - ItemProcessor](https://jojoldu.tistory.com/347)

### 번외
- [Spring Batch Paging Reader 사용시 같은 조건의 데이터를 읽고 수정할때 문제](https://jojoldu.tistory.com/337)


### Spring Batch 가이드 - 메타테이블엿보기
- JOB_INSTANCE_ID
    - BATCH_JOB_INSTANCE 테이블의 PK
- JOB_NAME
    - 수행한 Batch Job Name

BATCH_JOB_INSTANCE 테이블은 **Job Parameter**에 따라 생성되는 테이블


### BATCH_JOB_INSTANCE
- Job Parameter
    > Spring Batch가 실행될때 외부에서 받을 수 있는 파라미터

Spring Batch에서는 해당 날짜 데이터로 조회/가공/입력 등의 작업

같은 Batch Job 이라도 Job Parameter가 다르면 Batch_JOB_INSTANCE에는 기록되며

**Job Parameter** 가 같다면 기록되지 않음
`JobInstanceAlreadyCompleteException` 예외 발생

### BATCH_JOB_EXECUTION
> JOB_EXECUTION와 JOB_INSTANCE는 부모-자식 관계  

JOB_EXECUTION은 자신의 부모 JOB_INSTACNE가 성공/실패했던 모든 내역을 갖고 있음

Spring Batch는 동일한 Job Parameter로 성공한 기록이 있을때만 재수행이 안됨

- JOB INSTANCE: 부모
- JOB EXECUTION: 자식