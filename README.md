## Spring Batch 가이드 - ItemWriter
8. [Spring Batch 가이드 - ItemWriter](https://jojoldu.tistory.com/339)

## ItemWriter 소개
ItemWriter는 Spring Batch에서 사용하는 **출력** 기능

ItemWriter는 item 하나를 작성하지 않고 **Chunk 단위로 묶인 item List**를 다룸

* ItemReader를 통해 각 항목을 개별적으로 읽고 이를 처리하기 위해 ItemProcessor에 전달  
* 이 프로세스는 청크의 Item 개수 만큼 처리 될 때까지 계속됨
* 청크 단위만큼 처리가 완료되면 Writer에 전달되어 Writer에 명시되어있는대로 일괄처리

Reader와 Processor를 거쳐 처리된 Item을 Chunk 단위 만큼 쌓은 뒤 이를 Writer에 전달

## Database Writer
Spring Batch는 JDBC와 ORM 모두 Writer를 제공
Writer는 Chunk단위의 마지막 단계임
그래서 Database의 영속성과 관련해서는 **항상 마지막에 Flush를 해줘야만** 함

Writer가 받은 모든 Item이 처리 된 후, Spring Batch는 현재 트랜잭션을 커밋

데이터베이스와 관련된 Writer

* JdbcBatchItemWriter
* HibernateItemWriter
* JpaItemWriter

## JdbcBatchItemWriter
ORM을 사용하지 않는 경우 Writer는 대부분 JdbcBatchItemWriter를 사용
JdbcBatchItemWriter는 **JDBC의 Batch 기능을 사용하여 한번에 Database로 전달하여 Database 내부에서 쿼리들이 실행**되도록 함

* 업데이트를 일괄 처리로 그룹화하면 데이터베이스와 어플리케이션간 왕복 횟수가 줄어들어 성능이 향상 됨

JdbcBatchItemWriterBuilder 설정값

|  Property     |  Parameter Type     |  설명   |
|  ---                          |  ---                              |  ---  |
| assertUpdates                 | boolean |  적어도 하나의 항목이 행을 업데이트하거나 삭제하지 않을 경우 예외를 throw할지 여부를 설정, 기본값은 ```true```, Exception:```EmptyResultDataAccessException```     | 
| columnMapped        | 없음 | Key,Value 기반으로 Insert SQL의 Values를 매핑 (ex: ```Map<String, Object>```)      |
| beanMapped        | 없음  | Pojo 기반으로 Insert SQL의 Values를 매핑      |

```JdbcBatchItemWriter```의 설정에서 주의

* JdbcBatchItemWriter의 제네릭 타입은 **Reader에서 넘겨주는 값의 타입**

## JpaItemWriter
ORM을 사용할 수 있는 ```JpaItemWriter```
Writer에 전달하는 데이터가 Entity 클래스라면 JpaItemWriter를 사용

JpaItemWriter는 JPA를 사용하기 때문에 영속성 관리를 위해 EntityManager를 할당해줘야 함  

> 일반적으로 ```spring-boot-starter-data-jpa```를 의존성에 등록하면 Entity Manager가 Bean으로 자동생성되어 DI 코드만 추가하면 됨

대신 **필수로 설정해야할 값이 EntityManager뿐**임

JdbcBatchItemWriter와 다른것이 있다면 processor가 추가 됨  
이유는 Pay Entity를 읽어서 Writer에는 Pay2 Entity를 전달해주기 위함  

> Reader에서 읽은 데이터를 가공해야할 때 Processor가 필요함  

JpaItemWriter는 JdbcBatchItemWriter와 달리 **넘어온 Entity를 데이터베이스에 반영**함
JpaItemWriter는 **Entity 클래스를 제네릭 타입으로 받아야만 함**

## Custom ItemWriter
Reader와 달리 Writer의 경우 Custom하게 구현해야할 일이 많음

* Reader에서 읽어온 데이터를 RestTemplate으로 외부 API로 전달해야할때
* 임시저장을 하고 비교하기 위해 싱글톤 객체에 값을 넣어야할때
* 여러 Entity를 동시에 save 해야할때

등등 여러 상황이 있음

Spring Batch에서 공식적으로 지원하지 않는 Writer를 사용하고 싶을때 **ItemWriter인터페이스를 구현**하면 됨

```write()```만 ```@Override``` 하시면 구현체 생성은 끝남

## 주의 사항
ItemWriter를 사용할 때 **Processor에서 Writer에 List를 전달**하고 싶을때가 있음
이때 ItemWriter의 제네릭을 List로 선언해서는 문제를 해결할 수 없음

[Writer에 List형 Item을 전달하고 싶을때](https://jojoldu.tistory.com/140)