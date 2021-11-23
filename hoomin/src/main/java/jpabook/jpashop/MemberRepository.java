package jpabook.jpashop;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

import jpabook.jpashop.domain.Member;

@Repository
public class MemberRepository {

	// Springboot가 이 어노테이션이 있으면 엔티티 매니저 설정해준다.
	@PersistenceContext
	private EntityManager em;

	// 커맨드와 쿼리를 분리해라!
	public Long save(Member member) {
		em.persist(member);
		return member.getId();
	}

	public Member find(Long id) {
		return em.find(Member.class, id);
	}
}
