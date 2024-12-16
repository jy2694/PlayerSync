# PlayerSync

## 프로젝트 개요
PlayerSync는 Minecraft 서버 간 플레이어 데이터를 동기화하는 데 중점을 둔 플러그인입니다. 이 플러그인은 플레이어가 서버를 이동할 때 데이터가 일관되게 유지되도록 보장합니다. 이를 통해 플레이어는 여러 서버에서 동일한 경험을 할 수 있습니다.

이 플러그인은 다음과 같은 주요 기능을 제공합니다:
- 서버 간 플레이어 데이터 동기화
- 데이터베이스를 통한 플레이어 데이터 저장 및 로드

## 사용 방법

1. Maven 혹은 Gradle을 이용한 플러그인 Import

### Maven
```
준비중
```

### Gradle
```
준비중
```

2. 엔티티 클래스 바인드

이 플러그인의 기능을 사용하고자 하는 엔티티 클래스를 바인딩해야 합니다.
바인딩이 가능한 엔티티 클래스는 @Synchronizable 어노테이션을 붙여야하고 반드시 엔티티의 구분값이 될 UUID 필드에는 @SyncID 어노테이션을 붙여야합니다.

```
//이 소스코드는 예시입니다.
@Synchronizable
public class PlayerEntity {
    @SyncID
    private UUID uuid;
}
```

그리고 아래 메소드를 사용하여 플러그인에 클래스를 바인딩합니다.
```
//이 소스코드는 예시입니다.
PlayerSync.getAPI().getBinder().bindClass(PlayerEntity.class);
```

만약 엔티티 클래스의 작성이 정확하지 않다면 NotSynchronizableClassException 예외가 발생합니다.

3. 플레이어 데이터 저장 및 로드

바인딩된 클래스 객체는 config.yml에 작성된 데이터베이스에 직렬화하여 저장됩니다.
로드 시점은 사용자가 서버에 접속한 시점인 PlayerJoinEvent 발생 시점입니다.
저장 시점은 사용자가 서버에서 나간 시점인 PlayerQuitEvent 발생 시점입니다.

로드된 객체는 아래와 같이 획득할 수 있습니다.
```
//이 소스코드는 예시입니다.
PlayerEntity playerEntity = PlayerSync.getAPI().get(PlayerEntity.class, player.getUniqueId());
```

만약 요청한 클래스가 바인딩된 클래스가 아니라면 NotBindedClassException 예외가 발생합니다.
메소드의 반환 결과가 null일 경우 로드되지 않은 객체임을 말합니다.

4. 이벤트 지원 목록

* ``PlayerDataLoadedEvent`` - 플레이어 관련 객체가 로드 완료된 시점에 발생
* ``PlayerDataLoadEvent`` - 플레이어 관련 객체가 로드 시작하는 시점에 발생
* ``PlayerDataStoredEvent`` - 플레이어 관련 객체가 저장 완료된 시점에 발생
* ``PlayerDataStoreEvent`` - 플레이어 관련 객체가 저장 시작하는 시점에 발생
* ``PlayerSyncReloadedEvent`` - 플러그인이 리로드 완료된 시점에 발생
