## Spring Batch 가이드 - ItemReader
7. [Spring Batch 가이드 - ItemReader](https://jojoldu.tistory.com/336)

### ItemReader 소개
Spring Batch의 ItemReader는 **데이터를 읽어들임**
**Spring Batch에서 지원하지 않는 Reader가 필요할 경우 직접 해당 Reader를 만들수도 있음**

Spring Batch의 Reader에서 읽어올 수 있는 데이터 유형
* 입력 데이터에서 읽어오기
* 파일에서 읽어오기
* Database에서 읽어오기
* Java Message Service등 다른 소스에서 읽어오기
* 본인만의 커스텀한 Reader로 읽어오기

#### ItemStream 인터페이스
ItemStream 인터페이스는 **주기적으로 상태를 저장하고 오류가 발생하면 해당 상태에서 복원**하기 위한 마커 인터페이스
즉, 배치 프로세스의 실행 컨텍스트와 연계해서 **ItemReader의 상태를 저장하고 실패한 곳에서 다시 실행할 수 있게 해주는 역할**을 함

ItemStream의 3개 메소드 역할

* ```open()```, ```close()```는 스트림을 열고 닫음
* ```update()```를 사용하면 Batch 처리의 상태를 업데이트 할 수 있음

개발자는 **ItemReader와 ItemStream 인터페이스를 직접 구현해서 원하는 형태의 ItemReader**를 만들 수 있음

## ItemReader 구현체
### Database Reader
2개의 Reader 타입을 지원
- Cursor: JDBC ResultSet의 기본 기능
  > Cursor 방식은 Database와 커넥션을 맺은 후, Cursor를 한칸씩 옮기면서 지속적으로 데이터를 빨아옵  
  - esultSet이 open 될 때마다 ```next()``` 메소드가 호출 되어 Database의 데이터가 반환 됨
  - 이를 통해 필요에 따라 **Database에서 데이터를 Streaming** 할 수 있음
- Paging
  > Paging 방식에서는 한번에 10개 (혹은 개발자가 지정한 PageSize)만큼 데이터를 가져옴
  - Paging 개념은 페이지라는 Chunk로 Database에서 데이터를 검색
  - **페이지 단위로 한번에 데이터를 조회**해오는 방식

### CursorItemReader
Streaming 으로 데이터를 처리
Database와 어플리케이션 사이에 통로를 하나 연결하고 하나씩 빨아들인다고 생각하면 됨

#### JdbcCursorItemReader
Cursor 기반의 JDBC Reader 구현체

JdbcCursorItemReader의 설정값

* chunk
    * ```<Pay, Pay>``` 에서 **첫번째 Pay는 Reader에서 반환할 타입**이며, **두번째 Pay는 Writer에 파라미터로 넘어올 타입**
    * ```chunkSize```로 인자값을 넣은 경우는 Reader & Writer가 묶일 Chunk 트랜잭션 범위
        * Chunk에 대한 자세한 이야기는 [쳅터 6](https://jojoldu.tistory.com/331)을 참고
* fetchSize
    * Database에서 한번에 가져올 데이터 양
    * Paging과는 다른 것이, Paging은 실제 쿼리를 ```limit```, ```offset```을 이용해서 분할 처리하는 반면, Cursor는 쿼리는 분할 처리 없이 실행되나 내부적으로 가져오는 데이터는 FetchSize만큼 가져와 ```read()```를 통해서 하나씩 가져옴
* dataSource
    * Database에 접근하기 위해 사용할 Datasource 객체를 할당
* rowMapper
    * 쿼리 결과를 Java 인스턴스로 매핑하기 위한 Mapper
    * 커스텀하게 생성해서 사용할 수 도 있지만, 이렇게 될 경우 매번 Mapper 클래스를 생성해야 되서 보편적으로는 Spring에서 공식적으로 지원하는 ```BeanPropertyRowMapper.class```를 많이 사용
* sql
    * Reader로 사용할 쿼리문을 사용
* name
    * reader의 이름을 지정
    * Bean의 이름이 아니며 Spring Batch의 ExecutionContext에서 저장되어질 이름

#### CursorItemReader의 주의 사항
Database와 SocketTimeout을 충분히 큰 값으로 설정해야만 함
**Batch 수행 시간이 오래 걸리는 경우에는 PagingItemReader를 사용**
Paging의 경우 한 페이지를 읽을때마다 Connection을 맺고 끊기 때문에 아무리 많은 데이터라도 타임아웃과 부하 없이 수행될 수 있음

### PagingItemReader
Paging: 여러 쿼리를 실행하여 각 쿼리가 결과의 일부를 가져 오는 방법

Spring Batch에서는 ```offset```과 ```limit```을 **PageSize에 맞게 자동으로 생성해줌**
각 페이지마다 새로운 쿼리를 실행하므로 **페이징시 결과를 정렬하는 것이 중요**
데이터 결과의 순서가 보장될 수 있도록 order by가 권장

#### JdbcPagingItemReader
JdbcPagingItemRedaer는 JdbcCursorItemReader와 같은 JdbcTemplate 인터페이스를 이용한 PagingItemReader

쿼리는 각 Database의 Paging 전략에 맞춰 구현되어야만 함
**SqlPagingQueryProviderFactoryBean을 통해 Datasource 설정값을 보고 Provider중 하나를 자동으로 선택**하도록 함

* parameterValues
    * 쿼리에 대한 매개 변수 값의 Map을 지정
    * ```queryProvider.setWhereClause``` 을 보시면 어떻게 변수를 사용하는지 자세히 알 수 있음
    * where 절에서 선언된 파라미터 변수명과 parameterValues에서 선언된 파라미터 변수명이 일치해야만 함

#### JpaPagingItemReader
JPA는 Hibernate와 많은 유사점을 가지고 있지만 한가지 다른 것이 있다면 
Hibernate 에선 Cursor가 지원되지만 **JPA에는 Cursor 기반 Database 접근을 지원하지 않음**

**EntityManagerFactory를 지정하는 것 외에** JdbcPagingItemReader와 크게 다른 점은 없음

#### PagingItemReader 주의 사항
정렬 (```Order```) 가 무조건 포함되어 있어야 함

### ItemReader 주의 사항
* JpaRepository를 ListItemReader, QueueItemReader에 사용하면 안됨
    * 간혹 JPA의 조회 쿼리를 쉽게 구현하기 위해 JpaRepository를 이용해서 ```new ListItemReader<>(jpaRepository.findByAge(age))``` 로 Reader를 구현하는 분들을 종종 봄
    * 이렇게 할 경우 Spring Batch의 장점인 페이징 & Cursor 구현이 없어 대규모 데이터 처리가 불가능함 (물론 Chunk 단위 트랜잭션은 됨)
    * 만약 정말 JpaRepository를 써야 하신다면 ```RepositoryItemReader```를 사용하시는 것을 추천
        * [예제 코드](https://stackoverflow.com/a/43986718)
        * Paging을 기본적으로 지원함
* Hibernate, JPA 등 영속성 컨텍스트가 필요한 Reader 사용시 fetchSize와 ChunkSize는 같은 값을 유지해야 함
    * [Spring Batch 영속성 컨텍스트 문제](https://jojoldu.tistory.com/146)
