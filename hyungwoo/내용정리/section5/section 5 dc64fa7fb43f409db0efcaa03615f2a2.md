# section 5

# 상품 도메인 개발

**구현 기능**

- 상품 등록
- 상품 목록 조회
- 상품 수정

**순서**

- 상품 엔티티 개발(비즈니스 로직 추가)
- 상품 리포지토리 개발
- 상품 서비스 개발
- 상품 기능 테스트

### 상품 엔티티 개발 (비즈니스 로직 추가)

상품의 재고가 늘고 주는 로직을 만들어야 하는데, 도메인 주도 설계에서는 엔티티 자체가 해결할 수 있는 것들은 엔티티 안에 비즈니스 로직을 만드는게 좋다. 

데이터(필드값)을 가지고 있는 클래스 안에서 비즈니스 로직을 수행하는게 가장 응집도 있고 객체지향 스럽다.

그래서 stockQuantity 필드값의 변경이 필요할때 setter로 변경하는게 하니라 핵심 비즈니스로직을 이용하여 값을 변경한다.

### **상품 리포지토리 개발**

```java
public void save(Item item) {
	if (item.getId() == null) {
		em.persist(item);
	} else {
		em.merge(item);
	}
}
```

item id 값이 null 이냐 아니냐로 분기를 나눈 이유는 item을 진짜 새로 new해서 생성하면 id 값이 null이기 때문임. null 이아닐때는 db에서 가져온 값으로 보고 저장된 db 값을 수정하는 것으로 본다.

따라서 merge는 update같은 개념이다. (뒤 챕터인 웹 어플리케이션 때 설명)

### **상품 서비스 개발**

```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {
	private final ItemRepository itemRepository;

	@Transactional
	public void saveItem(Item item) {
		itemRepository.save(item);
	}

	public List<Item> findItems() {
		return itemRepository.findAll();
	}

	public Item findOne(Long itemId) {
		return itemRepository.findOne(itemId);
	}
}
```

ItemService클래스는 정말 ItemRepository에게 권한을 위임하는 클래스로, 이런 경우에는 굳이 Service를 만들 필요없이 Controller에서 repository를 바로 호출해서 불러와도 되긴 함. (고민이 필요한 부분)

상품 서비스 테스트는 일단 로직이 단순하고 회원테스트와 비슷하기 때문에 생략한다.