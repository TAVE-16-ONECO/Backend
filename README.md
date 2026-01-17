<h1 align="center">ONECO_v1</h1>

<p align="center">
  <img alt="oneco-logo.png" height="250" src="docs/imgs/oneco-logo.png" width="250"/>
</p>

<h3 align="center">부모-자녀 페어링 기반 경제·금융 키워드 교육 서비스</h3>

<p align="center">
    <a href="https://oneco.io.kr/"> 원코 바로가기 </a>
</p>

---

## 💿 ERD 설계

![erd_oneco.png](docs/imgs/oneco-erd.png)

---

## ⚙️ 인프라 아키텍처 
![system_architecture.png](docs/imgs/oneco-system-arch.png)
[단일 EC2에서 운영하기: 컨테이너 분리와 SSH→SSM 전환 개발 스토리 보러가기](https://goodjunseon-tech-blog.tistory.com/12)

## 🔁 소프트웨어 아키텍처
![oneco-software-arch.png](docs/imgs/oneco-software-arch.png)

## 🚀 도메인 아키텍처

### DailyContent 도메인

![DailyContent_Domain_Architecture.png](docs/imgs/oneco-domain-dailycontent.png)
[DailyContent 도메인 개발 스토리 보러가기](https://gimini.tistory.com/46)

### Mission 도메인
![Mission_Domain_Architecture.png](docs/imgs/oneco-domain-mission.png)
[Mission 도메인 개발 스토리 보러가기]

### StudyRecord 도메인
![StudyRecord_Domain_Architecture.png](docs/imgs/oneco-domain-studyrecord.png)
[StudyRecord 도메인 개발 스토리 보러가기]

### Family 도메인
![Family_Domain_Architecture.png](docs/imgs/oneco-domain-family.png)
[Family 도메인 개발 스토리 보러가기]

### Category 도메인
![Category_Domain_Architecture.png](docs/imgs/oneco-domain-category.png)
[Category 도메인 개발 스토리 보러가기]

---

## 시스템 아키텍처 [구현 예정]
### 최소 1회 전송(At-least-once)을 보장하는 알림 시스템
![Notification_Architecture.png](docs/imgs/Notification_Architecture.png)
[누락 없이 알림을 보내기 위한 Redis 큐 & 워커 설계 보러가기 ](https://gimini.tistory.com/60)

