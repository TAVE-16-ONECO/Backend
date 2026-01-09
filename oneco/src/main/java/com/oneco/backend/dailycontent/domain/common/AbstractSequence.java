package com.oneco.backend.dailycontent.domain.common;

import java.util.Objects;

import com.oneco.backend.dailycontent.domain.exception.constant.ContentErrorCode;
import com.oneco.backend.global.exception.BaseException;

import lombok.Getter;

/**
 * 1부터 시작하는 "순번/일차" 계열 값객체들의 공통 기반 클래스.
 * <p>
 * 목적
 * 1) 공통 규칙 재사용
 * - 모든 순번은 1 이상이어야 한다.
 * 2) 도메인 타입 안전성 유지
 * - DaySequence, QuestionOrder, ItemOrder 같은 서로 다른 개념을
 * "같은 숫자"라고 해서 동일 객체로 취급하지 않는다.
 * 3) 값객체로서의 불변성/일관성 보장
 * <p>
 * * [JPA 매핑 전략 원칙]
 * * - AbstractSequence를 상속받는 값 객체는 @Embeddable(상속) 매핑의 제약/복잡도를 피하기 위해
 * *   기본적으로 AttributeConverter(@Convert)로 단일 컬럼 매핑한다.
 * * - 이 방식은 JPA가 리플렉션으로 VO를 생성할 필요가 없고,
 * *   도메인 불변성/순수성을 유지하기에 유리하다.
 * *
 */
@Getter
public class AbstractSequence {

	/**
	 * private + final: 불변성 보장
	 * - 값객체의 상태는 생성 시에만 설정되고 이후에는 변경 불가
	 * - 외부/자식이 값을 직접 수정 불가
	 */
	private final int value;

	/**
	 * protected 생성자
	 * - 직접 new AbstractSequence() 불가
	 * - 서브클래스에서 super(value) 호출하여 사용
	 * <p>
	 * 여기서 공통 규칙만 검증
	 * - value는 1 이상이어야 한다.
	 * <p>
	 * 중요한 원칙:
	 * - 다른 애그리거트/엔티티(ex. category)의 상태를 알아야 하는
	 * 교차 규칙 ( 예: daySequence가 category의 최대 일차수를 넘지 않아야 한다 )은
	 * 여기서 검증하지 않는다.
	 * - 그런 검증은 도메인 서비스나 애그리거트 루트에서 수행해야 한다.
	 *
	 * @param value
	 */
	protected AbstractSequence(int value) {
		if (value < 1) {
			throw BaseException.from(ContentErrorCode.INVALID_SEQUENCE_VALUE,
				this.getClass().getSimpleName() + "은(는) 1 이상의 값이어야 합니다: " + value);
		}
		this.value = value;
	}

	/**
	 * 현재 값을 반환하는 메서드
	 * final을 붙인 이유:
	 * - 서브클래스에서 오버라이드하여 다른 동작을 하게 하지 않기 위함
	 */
	public final int value() {
		return value;
	}

	/**
	 * 공통 편의 메서드
	 * - 다음 순번의 값객체를 생성하여 반환
	 * - 서브클래스에서 동일한 타입으로 반환하도록 강제
	 */
	protected final int nextValue() {
		return this.value + 1;
	}

	/**
	 * 값 객체의 타입 이름 반환
	 * 예: DaySequence, QuestionOrder 등
	 */
	protected String getTypeName() {
		return this.getClass().getSimpleName();
	}

	/**
	 * 값 객체의 동등성(equals) 구현
	 * <p>
	 * 이 equals의 핵심 철학:
	 * - 값+타입이 모두 같아야 같은 값객체다.
	 * <p>
	 * getClass()를 쓰는 이유:
	 * - instanceof를 쓰면 서로 다른 하위 타입끼리도 같다고 판단될 여지가 생길 수 있다.
	 * - 예: DaySequence(1)와 QuestionOrder(1)이 같다고 판단되는 상황 방지
	 * - 둘은 숫자 1이라는 값이 같아도 도메인 의미가 다르므로 다른 객체로 취급해야 한다.
	 */
	@Override
	public final boolean equals(Object o) {
		// 같은 객체를 가리키면(참조 주소) true
		if (this == o)
			return true;
		// null이거나 다른 클래스면 false
		// getClass()는 부모클래스를 가리키는 게 아니라 실제 인스턴스의 클래스를 반환
		if (o == null || getClass() != o.getClass())
			return false;

		// 위에서 이미 클래스가 동일한지 확인했기 때문에 안전하게 캐스팅 가능
		AbstractSequence that = (AbstractSequence)o;
		// 위에서 같은 타입인지 확인했으므로 값만 비교
		return value == that.value;
	}

	/**
	 * 값 객체의 해시코드(hashCode) 구현
	 * - equals와 일관성 유지
	 * - 타입 정보와 값을 해시에 포함
	 */
	@Override
	public final int hashCode() {
		return Objects.hash(this.getClass(), value);
	}

	/**
	 * 값 객체의 문자열 표현 구현
	 * 예: DaySequence(3), QuestionOrder(5) 등
	 */
	@Override
	public String toString() {
		return getTypeName() + "(" + value + ")";
	}

}
