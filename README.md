### 프로젝트 패키지 구성

server-java/
├── .gitignore                # Git에서 추적하지 않을 파일 및 폴더 설정
├── build.gradle              # Gradle 빌드 설정 파일
├── gradle/                    # Gradle 관련 설정 및 wrapper
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── kr/
│   │   │   │   └── hhplus/
│   │   │   │       └─── be/
│   │   │   │           ├── server/
│   │   │   │           │   ├── config/         # 서버 관련 설정 (Spring 설정, DB 연결 등)
│   │   │   │           │   ├── controller/    # API 요청을 처리하는 컨트롤러
│   │   │   │           │   ├── service/       # 비즈니스 로직 처리
│   │   │   │           │   ├── repository/    # 데이터 접근 계층 (JPA, Mock 등)
│   │   │   │           │   └── model/          # 도메인 모델 (DTO, 엔티티 등)
│   │   │   │           └─── usecase/            # 비즈니스 로직 유스케이스
│   │   │   │           
│   │   │   │       
│   │   └── resources/             # 서버 설정 파일들 (application.properties, 로그 설정 등)
│   │       └── application.properties  # DB, API, 로깅 등 설정
├── .github/                   # GitHub 관련 설정 (Pull Request 템플릿 등)
└── README.md                   # 프로젝트 설명