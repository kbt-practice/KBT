# KBT 코드 스타일 가이드

이 문서는 `amumal` 프로젝트의 컨벤션을 정리한 것입니다.
새 코드를 작성하거나 리뷰할 때 이 문서를 기준으로 판단합니다.

## 1. 패키지 구조

도메인 주도 패키지 구조를 사용합니다.

```
domain/{도메인명}/
├── controller/
├── service/
├── repository/
├── entity/
└── dto/
```

도메인에 속하지 않는 공통 코드는 `global/` 아래에 역할별로 둡니다.

```
global/
├── config/       # Bean 설정 (SecurityConfig, WebConfig, S3Config, ...)
├── common/       # 여러 도메인이 공유하는 클래스 (ApiResponse, BaseEntity, ImageHandler, ...)
├── error/        # ErrorCode, CustomException, GlobalExceptionHandler
├── filter/       # Servlet Filter
├── interceptor/  # HandlerInterceptor, ArgumentResolver
└── util/         # 순수 유틸리티 (JwtUtil, ...)
```

- 도메인 간 의존은 필요한 경우에만 허용합니다 (예: `PostService`가 `UserRepository`, `CommentRepository`를 참조).
- 도메인에 종속되지 않는 로직은 `global`로 올립니다.

## 2. 네이밍

| 대상 | 규칙 | 예시 |
|---|---|---|
| 클래스 / 인터페이스 | PascalCase | `PostService`, `PostRepository`, `CustomException`, `CommentRepository`, `CommentController` |
| 메서드 / 변수 | lowerCamelCase | `findPostsWithCursor`, `postImageUrl` |
| 상수 | UPPER_SNAKE_CASE | `REFRESH_PREFIX` |
| Repository 인터페이스 | `{Entity}Repository` | `PostRepository`, `UserRepository`, `LikeRepository`, `CommentRepository` |
| Custom Repository 구현체 | `{Entity}RepositoryImpl` / `{Entity}RepositoryCustom` | `PostRepositoryImpl`, `PostRepositoryCustom` |
| 요청/응답 DTO 컨테이너 클래스 | `{Domain}ReqDTO` / `{Domain}ResDTO` (도메인당 하나, 내부에 여러 record) | `PostReqDTO`, `UserResDTO`, `AuthReqDTO` |
| DTO 내부 record | PascalCase | `PostReqDTO.CreatePost`, `PostResDTO.PostDetailResponse`, `UserReqDTO.Signup`, `AuthReqDTO.LoginReq` |
| 서비스 메서드(CRUD) | `create`, `get`, `getList`, `update`, `delete` 를 기본으로 사용 | `PostService.create`, `PostService.getList` |
| 테스트 메서드 | `{동작}_{조건}` snake_case | `login_success` |

## 3. 포맷팅

- 들여쓰기: 스페이스 4칸 (탭 금지).
- import: 명시적 import를 기본으로 하되, 한 클래스에서 같은 패키지의 심볼을 다수 쓸 때(`jakarta.persistence.*`, `lombok.*`, Spring MVC 어노테이션 등)는 와일드카드 import를 허용합니다.
- 단순 guard clause는 중괄호 없는 한 줄 `if`를 허용합니다:
  ```java
  if (post.getUserId() != id)
      throw new CustomException(ErrorCode.POST_FORBIDDEN_UPDATE);
  ```
  분기 내부가 두 문장 이상이거나 `if/else`가 함께 오면 중괄호를 사용합니다.
- 메서드 체이닝(`stream()`, QueryDSL 등)이나 여러 줄에 걸친 인자는 항목마다 줄바꿈 후 정렬합니다.
- 메서드 내부에서 로직 단락이 바뀌는 지점에는 빈 줄을 넣어 단계를 구분합니다 (조회 → 검증 → 처리 → 반환).

## 4. Lombok

- 의존성 주입: 필드에 `private final`, 클래스에 `@RequiredArgsConstructor`. 생성자를 직접 작성하지 않습니다.
- Entity: `@Getter` + `@Builder` + `@NoArgsConstructor` + `@AllArgsConstructor`. `@Setter`는 사용하지 않고, 의미 있는 이름의 도메인 메서드(`updateTitle`, `softDelete` 등)로 상태를 변경합니다.
- 기본값이 있는 필드는 `@Builder.Default`로 명시합니다.
- 예외/응답 래퍼 등 불변 값 객체: `@Getter` + private 생성자 + static factory 메서드 (`ApiResponse.success(...)`, `ApiResponse.fail(...)`).
- 로깅: `@Slf4j`.

## 5. 계층별 규칙

### Controller
- `@RestController` + `@RequiredArgsConstructor` + `@RequestMapping("/{리소스}")`.
- 반환형은 `ApiResponse<?>`로 통일합니다.
- 인증된 사용자 ID는 커스텀 어노테이션 `@LoginUserId int userId`로 받습니다.
- 요청 검증이 필요한 DTO 파라미터에는 `@Valid`를 붙입니다.
- 컨트롤러에는 비즈니스 로직을 두지 않고 Service 호출 결과를 `ApiResponse`로 감싸는 역할만 합니다.

### Swagger 문서화
- 클래스에 `@Tag(name = "{한글 도메인명}", description = "{그 도메인 API가 제공하는 기능 요약}")`을 붙입니다. (예: `@Tag(name = "게시글", description = "게시글 CRUD·좋아요 API")`)
- 각 메서드에는 `@Operation(summary = "{짧은 동작명}", description = "{동작 방식·부수효과·제약조건}")`을 붙입니다. `summary`는 "게시글 등록"처럼 명사형으로, `description`은 "제목·내용·이미지(선택)로 게시글을 생성합니다."처럼 실제 동작을 한 문장으로 설명합니다.
- 경로 변수·쿼리 파라미터 중 이름만으로 역할이 분명하지 않은 것에는 `@Parameter(description = "...")`를 붙입니다. (예: `@Parameter(description = "수정할 게시글 ID") @PathVariable Integer postId`)
- 인증이 필요 없는 엔드포인트(목록/상세 조회 등)에는 `@SecurityRequirements`를 붙여 Swagger UI에서 인증 입력란이 뜨지 않게 합니다.
- `@LoginUserId`처럼 Swagger 문서에 노출되면 안 되는 파라미터 애너테이션은 `SwaggerConfig`의 `SpringDocUtils.getConfig().addAnnotationsToIgnore(...)`에 등록해서 문서에서 숨깁니다.
- `@Tag`/`@Operation`/`@Parameter`의 모든 설명 문구는 한국어로 작성합니다.

### Service
- `@Service` + `@Transactional`(클래스 레벨, 메서드 단위로 세분화하지 않음) + `@RequiredArgsConstructor`.
- 존재하지 않는 리소스 조회는 `.orElseThrow(() -> new CustomException(ErrorCode.XXX_NOT_FOUND))` 패턴을 사용합니다.
- 권한/상태 검증 실패는 `CustomException`을 던지며, 그 외의 흐름 제어에는 예외를 사용하지 않습니다.
- 여러 곳에서 반복되는 하위 로직은 `private` 메서드로 추출합니다 (`uploadImageIfPresent`, `registerImageRollbackOnFailure` 등).
- 외부 리소스(S3 이미지 등) 업로드와 DB 저장을 함께 처리할 때는 `TransactionSynchronizationManager.registerSynchronization`으로 커밋/롤백 이후 정리 로직을 등록합니다. (성공 시 기존 파일 삭제, 롤백 시 새로 올린 파일 삭제)
- N+1 문제가 예상되는 연관 조회는 ID를 모아 `findXxxByIdIn` 형태의 Projection 조회로 한 번에 가져옵니다.

### Repository
- Spring Data JPA 파생 쿼리 메서드명을 우선 사용하고, 복잡한 조건일 때만 `@Query`를 작성합니다.
- 벌크 업데이트는 `@Modifying(clearAutomatically = true, flushAutomatically = true)`를 명시합니다.
- QueryDSL이 필요한 커스텀 조회는 `{Entity}RepositoryCustom` 인터페이스 + `{Entity}RepositoryImpl` 구현체로 분리하고, 기본 Repository가 이를 상속(`extends JpaRepository<T, ID>, XxxRepositoryCustom`)합니다.
- 여러 줄에 걸친 판단이 필요한 쿼리 메서드에는 Javadoc(`/** ... */`)으로 파라미터와 동작을 설명합니다.

### Entity
- `BaseEntity`(생성/수정/삭제 시각)를 상속합니다. (단, Like 스키마 제외)
- 소프트 삭제는 `deletedAt` 필드 + `softDelete()` 메서드로 처리합니다.
- 상태 변경은 setter가 아닌 의도가 드러나는 메서드로 노출합니다.
- 비정규화된 카운트 필드(`likeCount`, `commentCount`, `viewCount`)를 두는 경우, 원자적 갱신은 Repository의 `@Modifying` 쿼리로 처리하고 그 이유를 주석으로 남깁니다.

### DTO
- 도메인당 요청/응답 DTO를 각각 하나의 컨테이너 클래스(`XxxReqDTO`, `XxxResDTO`)로 모으고, 그 안에 `record`로 세부 타입을 PascalCase로 정의합니다.
- 검증 애너테이션(`@NotBlank` 등)의 메시지는 하드코딩하지 않고 `ValidationMessage` 상수를 참조합니다.
- 엔티티 → 응답 DTO 변환은 응답 record에 `static from(Entity entity)` 팩토리 메서드로 둡니다.

## 6. 예외 처리

- 모든 비즈니스 예외 원인은 `ErrorCode` enum 하나에 등록합니다: `(HttpStatus status, String message, String reason)`. 도메인별로 주석 구분선을 두고 그룹화합니다.
- 예외는 `CustomException(ErrorCode)` 또는 상세 사유가 다를 때 `CustomException(ErrorCode, String reason)`으로 던집니다.
- `GlobalExceptionHandler`(`@RestControllerAdvice`)에서만 예외 → HTTP 응답 변환을 담당합니다. 각 핸들러는 `ApiResponse.fail(...)`로 응답을 감쌉니다.
- 로깅 레벨: 클라이언트 귀책(4xx류)은 `log.warn`, 예상치 못한 서버 오류(`Exception` catch-all)는 `log.error` + 스택트레이스 포함.

## 7. 주석

- 주석은 코드를 더 빠르게 이해하는 데 실제로 도움이 될 때만 남깁니다.
- 트레이드오프, 비정규화 이유, 특정 버그 우회처럼 "왜 이렇게 했는지"를 설명하는 주석을 가장 우선합니다.
- 메서드나 블록 단위로 흐름을 짚어주는 간결한 설명 주석(`// 댓글 생성`, `// 헤더 로드`처럼 한 줄로 그 구간이 뭘 하는지 요약하는 주석)도 허용합니다. 단, 코드를 그대로 번역하듯 줄마다 달거나 문단 단위로 길어지지 않게 합니다.
- 메서드/변수 이름만으로 이미 충분히 설명되는 경우에는 같은 내용을 반복하는 주석을 덧붙이지 않습니다.
- 모든 주석/Swagger 설명/에러 메시지는 한국어로 작성합니다.

## 8. 테스트

- 단위 테스트: JUnit 5 + Mockito(`@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks`).
- 테스트 클래스는 `{대상클래스}Test`, 통합 테스트는 `{대상}IntegrationTest`(별도 `integration` 태그로 분리 실행).
- 각 테스트 메서드에 `@DisplayName`으로 한국어 설명을 답니다.
- 테스트 본문은 `// given`, `// when`, `// then` 주석으로 3단 구성합니다.
- 검증은 AssertJ(`assertThat`), 스텁은 BDDMockito(`given(...).willReturn(...)`)를 사용합니다.

## 9. 기타 규칙

- ID 타입: 엔티티/DTO의 식별자 필드와 `@PathVariable`은 `Integer`(nullable 허용, JPA 매핑 편의), 서비스/컨트롤러 메서드 파라미터로 로그인 사용자 ID를 받을 때는 `int`를 사용하는 기존 패턴을 따릅니다.
- 매직 리터럴 대신 의미 있는 이름의 `private static final` 상수를 사용합니다(`REFRESH_PREFIX` 등).

## 10. 설계 원칙

- **단일 책임 원칙**: 클래스와 메서드가 여러 책임을 동시에 갖지 않도록 쪼갭니다.
  - `ImageHandler`는 이미지 확장자/MIME/Magic Bytes 검증과 S3 업로드·삭제만 전담하고, 도메인 서비스 로직과 섞이지 않습니다.
  - `PostService`/`UserService`처럼 여러 메서드에서 반복되는 하위 로직은 `uploadImageIfPresent`, `registerImageRollbackOnFailure` 같은 이름이 명확한 `private` 메서드로 분리합니다.
  - 하나의 클래스가 너무 많은 책임을 갖게 되면(예: 검증 + 저장 + 알림을 한 메서드가 전부 처리) 책임 단위로 별도 클래스나 메서드로 나눕니다.
- **의존성 분리와 주입**: 협력 객체를 직접 `new`로 생성하지 않고, `private final` 필드 + 생성자 주입(`@RequiredArgsConstructor`)으로 받습니다. 구현이 여러 개일 수 있거나 교체 가능성이 있는 경우 구체 클래스보다 인터페이스(예: `PostRepositoryCustom`, `PostRepository extends JpaRepository<...>`)에 의존합니다.