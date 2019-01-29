## Spring Batch 가이드 - Spring Batch Scope & Job Parameter
5. [Spring Batch 가이드 - Spring Batch Scope & Job Parameter](https://jojoldu.tistory.com/330)

### JobParameter
외부 혹은 내부에서 파라미터를 받아 여러 Batch 컴포넌트에서 사용할 수 있게 지원 하는 파라미터
- 사용가능한 타입
  - `Double`, `Long`, `Date`, `String`
- 사용불가능한 타입
  - `LocalDate`, `LocalDateTime`

#### JobParameter 오해
Job Parameters는 ```@Value```를 통해서 가능  
Step이나, Tasklet, Reader 등 Batch 컴포넌트 Bean의 생성 시점에 호출  
정확히는 Scope Bean을 생성할때만 가능  
즉, **```@StepScope```, ```@JobScope``` Bean을 생성할때만 Job Parameters가 생성**

JobParameters를 사용하기 위해선 꼭 **```@StepScope```, ```@JobScope```로 Bean을 생성**

#### JobParameter vs 시스템 변수
Job Parameter를 써야하는 이유
##### Job Parameter를 안쓰면  
- 시스템 변수를 사용할 경우 **Spring Batch의 Job Parameter 관련 기능을 못쓰게** 됨
- Command line이 아닌 다른 방법으로 Job을 실행하기가 어려움
- **Late Binding을 못하게 됨**

##### Job Parameter 를 사용하면  
- 원하는 어느 타이밍이든 Job Parameter를 생성하고 Job을 수행할 수 있음
- Job Parameter를 각각의 Batch 컴포넌트들이 사용하면 되니 **변경이 심한 경우에도 쉽게 대응**할 수 있음

### Scope
Job Parameter를 사용하기 위해선 항상 Spring Batch 전용 Scope

- `@JobScope`
  - Step 선언문에서 사용 가능
  - Job 실행시점에 Bean이 생성
- `@StepScope`
  - Tasklet이나 ItemReader, ItemWriter, ItemProcessor에서 사용
  - Step의 실행시점에 해당 컴포넌트를 Spring Bean으로 생성

SpEL로 선언해서 사용
```java
@Value("#{jobParameters[파라미터명]}")
```

#### 실행시점 지연
Bean의 생성 시점을 지정된 Scope가 실행되는 시점으로 지연시킴

##### 지연처리 장점
1. **JobParameter**의 **Late Binding** 가능
  - StepContext 또는 JobExecutionContext 레벨에서 할당 가능
  - 비지니스 로직 처리 단계에서 Job Parameter를 할당 가능
2. 동일한 컴포넌트를 병렬 혹은 동시에 사용할때 유용
  - 각각의 Step에서 별도의 Tasklet을 생성하고 관리

> 웹서버에서 Batch를 관리하는 것은 권장하지 않음