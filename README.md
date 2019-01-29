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

## Spring Batch 가이드 - Spring Batch Job Flow
- Step: 실제 Batch 작업을 수행하는 역할
  > Batch로 실제 처리하고자 하는 기능과 설정을 모두 포함하는 장소
- 앞의 step에서 오류가 나면 나머지 뒤에 있는 step 들은 실행되지 못함
  - 조건별로 Step을 사용

### Next
`next()`는 순차적으로 Step들 연결시킬때 사용  
step1 -> step2 -> stpe3 순으로 하나씩 실행

#### 필요한 값 Job 만 처리되도록 설정
`application.yml`파일에 `spring.batch.job.names` 옵션 추가  
> Spring Batch가 실행될때 Program arguments로 `job.name` 값이 넘어오면 해당 값과 일치하는 Job만 실행  
```yaml
spring.batch.job.names: ${job.name:NONE}
```

##### Program arguments로 job.name 넘기기
```
--job.name=stepNextJob
```

### 조건별 흐름 제어 (Flow)

```java
@Bean
public Job stepNextConditionalJob() {
    return jobBuilderFactory.get("stepNextConditionalJob")
            .start(conditionalJobStep1())
                .on("FAILD") // FAILED 일 경우
                .to(conditionalJobStep3()) // step3으로 이동한다
                .on("*") // step3의 결과 관계 없이
                .end() // step3으로 이동하면 Flow가 종료된다
            .from(conditionalJobStep1()) // step1로부터
                .on("*") // FAILED 외에 모든 경우
                .to(conditionalJobStep2()) // step2로 이동한다
                .next(conditionalJobStep3()) // step2가 정상 종료되면 step3으로 이동한다
                .on("*") // step3의 결과 관계 없이
                .end() // step3으로 이동하면 Flow가 종료된다
            .end() // Job 종료
            .build();

}
```

* ```.on()```
    * 캐치할 **ExitStatus** 지정
    * ```*``` 일 경우 모든 ExitStatus가 지정된다.
* ```to()```
    * 다음으로 이동할 Step 지정
* ```from()``` 
    * 일종의 **이벤트 리스너** 역할
    * 상태값을 보고 일치하는 상태라면 ```to()```에 포함된 ```step```을 호출합니다.
    * step1의 이벤트 캐치가 FAILED로 되있는 상태에서 **추가로 이벤트 캐치**하려면 ```from```을 써야만 함
* ```end()```
    * end는 FlowBuilder를 반환하는 end와 FlowBuilder를 종료하는 end 2개가 있음
    * ```on("*")```뒤에 있는 end는 FlowBuilder를 반환하는 end
    * ```build()``` 앞에 있는 end는 FlowBuilder를 종료하는 end
    * FlowBuilder를 반환하는 end 사용시 계속해서 ```from```을 이어갈 수 있음
  
> 분기처리를 위해 상태값 조정이 필요하시다면 ExitStatus를 조정

### Batch Status vs. Exit Status
- BatchStatus: Job 또는 Step 의 실행 결과를 Spring에서 기록할 때 사용하는 Enum  
  COMPLETED, STARTING, STARTED, STOPPING, STOPPED, FAILED, ABANDONED, UNKNOWN 
- ExitStatus: Step의 실행 후 상태(ExitStatus는 Enum이 아님)

### Decide
```java
@Bean
public Job deciderJob() {
    return jobBuilderFactory.get("deciderJob")
            .start(startStep())
            .next(decider()) // 홀수 | 짝수 구분
            .from(decider()) // decider의 상태가
                .on("ODD") // ODD 라면
                .to(oddStep()) // oddStep로 간다
            .from(decider()) // decider의 상태가
            .on("EVEN") // EVEN 이라면
            .to(evenStep()) // evenStep로 간다.
            .end() // builder 종료
            .build();
}
```

* ```start()```
    * Job Flow의 첫번째 Step을 시작
* ```next()```
    * ```startStep``` 이후에 ```decider```를 실행
* ```from()```
    * `조건별 흐름 제어 (Flow)`와 마찬가지로 from은 이벤트 리스너 역할
    * decider의 상태값을 보고 일치하는 상태라면 ```to()```에 포함된 ```step``` 를 호출


```java
@Bean
public JobExecutionDecider decider() {
    return new OddDecider();
}

public class OddDecider implements JobExecutionDecider {

    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        Random rand = new Random();

        int randomNumber = rand.nextInt(50) + 1;
        log.info("랜덤숫자: {}", randomNumber);

        if(randomNumber % 2 == 0) {
            return new FlowExecutionStatus("EVEN");
        } else {
            return new FlowExecutionStatus("ODD");
        }
    }
}
```

Step으로 처리하는게 아니기 때문에 ExitStatus가 아닌 FlowExecutionStatus로 상태를 관리