package jpabook.jpashop.repository;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;

@Repository // 스프링 빈으로 등록.
@RequiredArgsConstructor
public class MemberRepository {

	// @PersistenceContext // jpa가 제공하는 표준 어노테이션, 스프링이 컴포넌트 스캔하며 엔티티 매니저를 주입시켜줌
	// private EntityManager em;
	// 스프링 안쓰면 내가 직접 꺼내서 써야하는데 스프링이 다 알아서 해줌. 굉장히 편함

	private final EntityManager em;

	// @PersistenceUnit
	// private EntityManagerFactory emf;
	// 이렇게 emf를 주입받을 수도 있는데 @PersistenceContext쓰면 되서 얘는 거의 안쓰임.

	public void save(Member member) {
		em.persist(member);
	}

	public Member findOne(Long id) {
		return em.find(Member.class, id);
	}

	public List<Member> findAll() {
		return em.createQuery("select m from Member m", Member.class)
			.getResultList();
	}

	public List<Member> findByName(String name) {
		return em.createQuery("select m from Member m where m.name = :name", Member.class)
			.setParameter("name", name) // 파라미터 바인딩
			.getResultList();
	}
}
