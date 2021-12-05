# section 7

# 웹 계층 개발

- 홈 화면
- 회원 기능
    - 회원 등록
    - 회원 조회
- 상품 기능
    - 상품 등록
    - 상품 수정
    - 상품 조회
- 주문 기능
    - 상품 주문
    - 주문 내역 조회
    - 주문 취소
    

## 홈 화면과 레이아웃

> 참고: Hierarchical-style layouts 
예제에서는 뷰 템플릿을 최대한 간단하게 설명하려고, header , footer 같은 템플릿 파일을 반복해서 포함한다. 다음 링크의 Hierarchical-style layouts을 참고하면 이런 부분도 중복을 제거할 수 있다. [https://www.thymeleaf.org/doc/articles/layouts.html](https://www.thymeleaf.org/doc/articles/layouts.html)
> 

> 참고: 뷰 템플릿 변경사항을 서버 재시작 없이 즉시 반영하기
1. spring-boot-devtools 추가
2. html 파일 build-> Recompile



## 회원 등록

## 회원 목록 조회

> 참고: 폼 객체 vs 엔티티 직접 사용

참고: 요구사항이 정말 단순할 때는 폼 객체( MemberForm ) 없이 엔티티( Member )를 직접 등록과 수정 화면에서 사용해도 된다. 하지만 화면 요구사항이 복잡해지기 시작하면, 엔티티에 화면을 처리하기 위한 기능이 점점 증가한다. 결과적으로 엔티티는 점점 화면에 종속적으로 변하고, 이렇게 화면 기능 때문에 지저분해진 엔티티는 결국 유지보수하기 어려워진다.

실무에서 엔티티는 핵심 비즈니스 로직만 가지고 있고, 화면을 위한 로직은 없어야 한다. 화면이나 API에 맞는 폼 객체나 DTO를 사용하자. 그래서 화면이나 API 요구사항을 이것들로 처리하고, 엔티티는 최대한 순수하게 유지하자.



## 상품 등록

## 상품 목록

## 상품 수정

## 변경 감지와 병합(merge)

```java
@PostMapping("/items/{itemId}/edit")
public String updateItem(@PathVariable String itemId, @ModelAttribute("form") BookForm form) {
	Book book = new Book();
	book.setId(form.getId());
	book.setName(form.getName());
	book.setPrice(form.getPrice());
	book.setStockQuantity(form.getStockQuantity());
	book.setAuthor(form.getAuthor());
	book.setIsbn(form.getIsbn());

	itemService.saveItem(book);
	return "redirect:/items";
}
```

**준영속 엔티티?**
영속성 컨텍스트가 더는 관리하지 않는 엔티티를 말한다.
(여기서는 itemService.saveItem(book)에서 수정을 시도하는 Book 객체다. Book 객체는 이미 DB에 한번 저장되어서 식별자가 존재한다. 이렇게 임의로 만들어낸 엔티티도(new 키워드) 기존 식별자를 가지고 있으면 `준영속 엔티티`로 볼 수 있다.)

**준영속 엔티티를 수정하는 2가지 방법**

1. 변경 감지 기능 사용
2. 병합(merge) 사용

**변경 감지 기능 사용**

```java
@Transactional
// param : 파라미터로 넘어온 준영속 상태의 엔티티
public Item updateItem(Long itemId, Book param) { 
// 같은 에티티 조회 (이때 findItem은 db에서 가져온 값이므로 영속 상태가 됨)
	Item findItem = itemRepository.findOne(itemId); 
// 데이터를 수정 하면 변경 감지 발생하므로 따로 save, update, merge할 필요없이
// 트랜잭션 커밋시점에 insert 쿼리가 나감.
	findItem.setPrice(param.getPrice());
	findItem.setName(param.getName());
	findItem.setStockQuantity(param.getStockQuantity());
	return findItem;
}
```

영속성 컨텍스트에서 엔티티를 다시 조회한 후에 데이터를 수정하는 방법
트랜잭션 안에서 엔티티를 다시 조회, 변경할 값 선택 → 트랜잭션 커밋 시점에 변경 감지(Dirty Checking) 이 동작해서 데이터베이스에 UPDATE SQL 실행

**병합 (merge) 사용**

위 변경 감지 기능 사용에서 만든 updateItem 메소드를 JPA가 `merge`라는 메소드로 제공해줌.

```java
@Transactional
void update(Item itemParam) { //itemParam: 파리미터로 넘어온 준영속 상태의 엔티티
	Item mergeItem = em.merge(item);
}
```

![Untitled](section%207%209b4c286c8cbb4d22aef1c48098a9add4/Untitled.png)

**병합 동작 방식**

1. merge()를 실행한다.
2. 파라미터로 넘어온 준영속 엔티티의 식별자 값으로 1차 캐시에서 엔티티를 조회한다.
   
    2-1. 만약 1차 캐시에 엔티티가 없으면 데이터베이스에서 엔티티를 조회하고, 1차 캐시에 저장한다.
    
3. 조회한 영속 엔티티(mergeMember)에 member 엔티티의 값을 채워 넣는다. (member 엔티티의 모든 값을 mergeMember에 밀어 넣는다. 이때 mergeMember의 "회원1"이라는 이름이 "회원명 변경"으로 바뀐다.)
4. 영속 상태인 mergeMember를 반환한다.

여기서 파라미터로 넘긴 item이 영속성 컨텍스트에서 관리되지 않고 merge메소드의 반환값인 mergeItem이 영속성 컨텍스트에서 관리하는 얘가 된다.

**병합시 동작 방식을 간단하게 정리하면**

1. 준영속 엔티티의 식별자 값으로 영속 엔티티를 조회한다.
2. 영속 엔티티의 값을 준영속 엔티티의 값으로 모두 교체한다.(병합한다.)
3. 트랜잭션 커밋 시점에 변경 감지 기능이 동작해서 데이터베이스에 UPDATE SQL이 실행

`**주의**`: 변경 감지 기능을 사용하면 원하는 속성만 선택해서 변경할 수 있지만, 병합을 사용하면 모든 속성이 변경된다. 병합시 값이 없으면 null 로 업데이트 할 위험도 있다. (병합은 모든 필드를 교체한다.)

그래서 가급적 merge를 쓰지 않는게 맞다. 실무에서는 변경이 필요한 얘들만 따로 바꿔 주는게 맞다. 그리고 의미없이 set을 막 깔면 안되고 의미있는 change 메소드로 무엇을 변경할지 만들어주는게 맞다.

### 가장 좋은 해결 방법

**엔티티를 변경할 때는 항상 변경 감지를 사용하세요**

+추가적으로

- 컨트롤러에서 어설프게 엔티티를 생성하지 마세요.

```java
@Transactional
public void updateItem(Long itemId, String name, int price, int stockQuantity) {
	Item findItem = itemRepository.findOne(itemId);
	findItem.setPrice(price);
	findItem.setName(name);
	findItem.setStockQuantity(stockQuantity);
}
```

```java
@PostMapping("/items/{itemId}/edit")
	public String updateItem(@PathVariable Long itemId, @ModelAttribute("form") BookForm form) {
	// Book book = new Book();
	// book.setId(form.getId());
	// book.setName(form.getName());
	// book.setPrice(form.getPrice());
	// book.setStockQuantity(form.getStockQuantity());
	// book.setAuthor(form.getAuthor());
	// book.setIsbn(form.getIsbn());

	// itemService.saveItem(book);
	itemService.updateItem(itemId, form.getName(), form.getPrice(), form.getStockQuantity());
	return "redirect:/items";
}
```

이렇게 하면 어설프게 컨트롤러에서 엔티티를 만들지 않아도 훨씬 깔끔하게 유지보수할 수 있다. 

파라미터가 길면 아예 UpdateItemDto클래스를 만들어서 빼도 된다.

위 updateItem에서 setter도 안쓰는게 좋다.

```java
@Transactional
public void updateItem(Long itemId, String name, int price, int stockQuantity) {
	Item findItem = itemRepository.findOne(itemId);
	findItem.change(name, price, stockQuantity);
}
```

이렇게 엔티티에 change 메서드를 만드는게 훨씬 낫다. (에러날때 추적하기 훨씬 쉬움)

- 트랜잭션이 있는 서비스 계층에 식별자( id )와 변경할 데이터를 명확하게 전달하세요.(파라미터 or dto)
- 트랜잭션이 있는 서비스 계층에서 영속 상태의 엔티티를 조회하고, 엔티티의 데이터를 직접 변경하세요.
- 트랜잭션 커밋 시점에 변경 감지가 실행됩니다.

## 상품 주문

```java
@PostMapping("/order")
public String order(@RequestParam("memberId") Long memberId,
					@RequestParam("itemId") Long itemId,
					@RequestParam("count") int count) {
	orderService.order(memberId, itemId, count);
	return "redirect:/orders";
}
```

주문 요청할때 orderController에서 멤버와 아이템을 find에서 찾으면 얘는 영속상태가 아님. (트랜잭션 안에서 관리되는것도 아님 → repository에 의존적이지 않기 때문)

그래서 이럴때는 그냥 service 레이어로 식별자만 넘기고 service레이어에서 트랜잭션 안에서 영속성 컨텍스트가 관리할 수 있도록 find 해서 비즈니스 로직을 처리하는게 테스트할때도 그렇고 깔끔하고 좋다.

## 상품 주문

## 주문 목록 검색, 취소