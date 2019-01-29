## Spring Batch 가이드 - ItemProcessor
9. [Spring Batch 가이드 - ItemProcessor](https://jojoldu.tistory.com/347)

이번 챕터에서 배울 내용

* process 단계에서 처리 할 수 있는 비즈니스 로직의 종류
* 청크 지향 처리에서 ItemProcessor 를 구성하는 방법
* Spring Batch와 함께 제공되는 ItemProcessor 구현

## ItemProcessor 소개
ItemProcessor는 **Reader에서 넘겨준 데이터 개별건을 가공/처리**해줌
ChunkSize 단위로 묶은 데이터를 한번에 처리하는 ItemWriter와는 대조됨

ItemProcessor를 사용하는 방법

* 변환
    * Reader에서 읽은 데이터를 원하는 타입으로 변환해서 Writer에 넘겨 줄 수 있음
* 필터
    * Reader에서 넘겨준 데이터를 Writer로 넘겨줄 것인지를 결정할 수 있음
    * ```null```을 반환하면 **Writer에 전달되지 않음**

## 기본 사용법
ItemProcessor 인터페이스는 두 개의 제네릭 타입이 필요함
* I
    * ItemReader에서 받을 데이터 타입
* O
    * ItemWriter에 보낼 데이터 타입

Reader에서 읽은 데이터가 ItemProcessor의 ```process```를 통과해서 Writer에 전달됨

## 변환
Reader에서 읽은 타입을 변환하여 Writer에 전달해주는 것

## 필터
**Writer에 값을 넘길지 말지를 Processor에서 판단하는 것**

## 트랜잭션 범위
Spring Batch에서 **트랜잭션 범위는 Chunk단위**
Reader에서 Entity를 반환해주었다면 **Entity간의 Lazy Loading이 가능**
rocessor뿐만 아니라 Writer에서도 가능

### Processor
**Processor는 트랜잭션 범위 안이며, Entity의 Lazy Loading이 가능**

### Writer
Processor와 Writer는 트랜잭션 범위 안이며, Lazy Loading이 가능

## ItemProcessor 구현체
Spring Batch에서 자주 사용하는 용도의 Processor 클래스

* ItemProcessorAdapter
* ValidatingItemProcessor
* CompositeItemProcessor

ItemProcessorAdapter, ValidatingItemProcessor는 거의 사용하지 않음

CompositeItemProcessor는 **ItemProcessor간의 체이닝을 지원**하는 Processor