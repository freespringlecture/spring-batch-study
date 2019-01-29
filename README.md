## Spring Batch 가이드 - Chunk 지향 처리
6. [Spring Batch 가이드 - Chunk 지향 처리](https://jojoldu.tistory.com/331)

### Chunk
Spring Batch에서의 Chunk란 데이터 덩어리로 작업 할 때 **각 커밋 사이에 처리되는 row 수**
즉, Chunk 지향 처리란 **한 번에 하나씩 데이터를 읽어 Chunk라는 덩어리를 만든 뒤, Chunk 단위로 트랜잭션**을 다루는 것을 의미

* Reader에서 데이터를 하나 읽어옴
* 읽어온 데이터를 Processor에서 가공 
* 가공된 데이터들을 별도의 공간에 모은 뒤, Chunk 단위만큼 쌓이게 되면 Writer에 전달하고 Writer는 일괄 저장  

**Reader와 Processor에서는 1건씩 다뤄지고, Writer에선 Chunk 단위로 처리**됨

### ChunkOrientedTasklet
Chunk 지향 처리의 전체 로직을 다루는 것은 ```ChunkOrientedTasklet``` 클래스

Chunk를 처리하며 이를 구성하는 3 요소로 ItemReader, ItemWriter, ItemProcessor가 있음

#### ```execute()```
* ```chunkProvider.provide()```로 Reader에서 Chunk size만큼 데이터를 가져옴
* ```chunkProcessor.process()``` 에서 Reader로 받은 데이터를 가공(Processor)하고 저장(Writer)

### SimpleChunkProcessor
Processor와 Writer 로직을 담고 있는 것은 ```ChunkProcessor``` 가 담당
기본적으로 ```SimpleChunkProcessor``` 구현체 사용

#### ```process()```
* ```Chunk<I> inputs```를 파라미터로 받음
    * 이 데이터는 앞서 ```chunkProvider.provide()``` 에서 받은 ChunkSize만큼 쌓인 item
* ```transform()``` 에서는 전달 받은 ```inputs```을 ```doProcess()```로 전달하고 변환값을 받음
* ```transform()```을 통해 가공된 대량의 데이터는 ```write()```를 통해 일괄 저장
    * ```write()```는 저장이 될수도 있고, 외부 API로 전송할 수 도 있음
    * 이는 개발자가 ItemWriter를 어떻게 구현했는지에 따라 달라짐

### Page Size vs Chunk Size
- **Chunk Size는 한번에 처리될 트랜잭션 단위**
- **Page Size는 한번에 조회할 Item의 양**
  - **페이징 쿼리에서 Page의 Size를 지정하기 위한 값**

#### 만약 2개 값이 다르면 ?
PageSize가 10이고, ChunkSize가 50이라면 **ItemReader에서 Page 조회가 5번 일어나면 1번 의 트랜잭션이 발생하여 Chunk가 처리**됨
  
성능상 이슈 외에도 2개 값을 다르게 할 경우 JPA를 사용하신다면 영속성 컨텍스트가 깨지는 문제도 발생함
(이전에 관련해서 [문제를 정리](http://jojoldu.tistory.com/146)했으니 참고해보세요)  
  
2개 값이 의미하는 바가 다르지만 위에서 언급한 여러 이슈로 **2개 값을 일치시키는 것이 보편적으로 좋은 방법**이니 꼭 2개 값을 일치시키시길 추천