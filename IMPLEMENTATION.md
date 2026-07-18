# Uygulama Notları – Gereksinimlerin Kod İçinde Karşılanması

Bu doküman, proje gereksinimlerinin kodun neresinde, nasıl karşılandığını; hangi
teknolojilerin ve tasarım desenlerinin kullanıldığını satır/sınıf bazında açıklar.

---

## 1. Genel Mimari

Uygulama bir **Spring Boot** (Java 21, Maven) REST servisidir. Katmanlar:

```
controller/   -> REST uç noktaları (giriş noktası)
service/      -> İş kuralları (mesafe hesabı, mağaza giriş tespiti, ingestion)
observer/     -> Observer deseni bileşenleri (event yayınlama/dinleme)
strategy/     -> Strategy deseni bileşeni (mesafe hesaplama algoritması)
repository/   -> Kurye konum event'lerinin bellek-içi (in-memory) saklanması
catalog/      -> stores.json dosyasının okunup Store nesnelerine dönüştürülmesi
domain/       -> Alan modelleri (CourierLocationEvent, Store, StoreVisitLog)
dto/          -> HTTP istek/yanıt sözleşmeleri (request/response record'ları)
```

Veri akışı: `POST /api/locations` → `TrackingController` → `CourierLocationIngestionService`
→ event repository'ye kaydedilir → `LocationEventPublisher` ile observer'lara
yayınlanır → observer'lar `CourierStatisticsService` (mesafe) ve `StoreVisitService`
(mağaza girişi) yeniden hesaplamalarını tetikler.

---

## 2. Gereksinim: Mağazaya 100m Girişte Log Tutma + 1 Dakika Tekrar Giriş Filtresi

**Nerede:** `src/main/java/com/migros/couriertracking/service/StoreVisitService.java`

- `ENTRY_RADIUS_METERS = 100.0` sabiti giriş yarıçapını tanımlar.
- `REENTRY_COOLDOWN = Duration.ofMinutes(1)` aynı mağazaya 1 dakika içindeki
  tekrar girişleri "yeni giriş" saymamak için kullanılır.
- `recalculate(String courierId)` metodu, o kuryeye ait tüm konum event'lerini
  zaman sırasına göre gezer ve her mağaza için ayrı ayrı "içeride miydi / şimdi
  içeride mi" durumunu takip eder (`previousInside`). Dışarıdan içeriye geçiş
  anı (`entered = inside && !previousInside`) gerçek bir "giriş" olayıdır.
- `isPastCooldown(...)` metodu, bir önceki sayılan girişin zaman damgasına 1 dakika
  eklenip eklenmediğini kontrol ederek erken tekrar girişleri filtreler.
- `isInsideStore(...)` metodu mesafe hesabı için **Strategy** deseninden
  (`DistanceStrategy`) yararlanır; store'a olan mesafe 100m'nin altındaysa
  kurye "içeride" kabul edilir.
- Onaylanan her giriş `StoreVisitLog` (domain/`StoreVisitLog.java`) olarak
  saklanır: zaman, kuryeId, storeId, storeName, lat, lng.
- Dış dünyaya bu loglar `GET /api/store-visits` (tüm kuryeler) uç noktasıyla
  `TrackingController.getStoreVisits()` üzerinden sunulur.

**Mağaza verisi:** `src/main/resources/stores.json` dosyasından `JsonStoreCatalog`
(`catalog/JsonStoreCatalog.java`) tarafından uygulama açılışında (`@PostConstruct`)
okunur ve `Store` (domain) listesine dönüştürülür. `GET /api/stores` ile de
katalog dışarıya açılmıştır (`StoreController`).

---

## 3. Gereksinim: Toplam Kat Edilen Mesafe Sorgusu

**Nerede:** `src/main/java/com/migros/couriertracking/service/CourierStatisticsService.java`

- `recalculate(String courierId)`: kuryenin tüm event'lerini zaman sırasına göre
  ikişerli gruplar halinde gezip (`events.get(index-1)` → `events.get(index)`)
  ardışık noktalar arası mesafeyi `DistanceStrategy.distanceMeters(...)` ile
  toplayarak `totalDistanceByCourier` map'inde saklar.
- `getTotalTravelDistance(String courierId)`: istenen imzaya karşılık gelen
  metottur (ödevdeki örnek `Double getTotalTravelDistance(courierId)` burada
  `double` olarak, aynı davranışla karşılanmıştır).
- REST karşılığı: `GET /api/couriers/{courierId}/distance` →
  `TrackingController.getCourierDistance(...)` → `CourierDistanceResponse`
  (courierId, totalDistanceMeters) döner.

---

## 4. Kullanılan Tasarım Desenleri (En az 2 istendi, 2 tane uygulandı)

### 4.1 Strategy Deseni – Mesafe Hesaplama

- Arayüz: `src/main/java/com/migros/couriertracking/strategy/DistanceStrategy.java`
  → `double distanceMeters(lat1, lng1, lat2, lng2)`.
- Somut strateji: `strategy/HaversineDistanceStrategy.java` — Haversine
  formülü ile iki koordinat arası büyük daire mesafesini metre cinsinden
  hesaplar (Dünya yarıçapı `6_371_000.0` m).
- Kullanan yerler: `CourierStatisticsService` (toplam mesafe) ve
  `StoreVisitService` (mağazaya mesafe / içeride mi kontrolü) bu stratejiyi
  constructor injection ile alır; algoritma değişirse (örn. Vincenty formülü)
  sadece yeni bir `DistanceStrategy` implementasyonu eklemek yeterli olur,
  servis kodları değişmez.

### 4.2 Observer Deseni – Konum Event'lerinin Yayınlanması

- Arayüz: `observer/LocationEventObserver.java` → `onLocationEvent(event)`.
- Yayıncı (Subject): `observer/LocationEventPublisher.java` — Spring tarafından
  enjekte edilen tüm `LocationEventObserver` bean'lerini (`List<LocationEventObserver>`)
  tutar ve `publish(event)` çağrıldığında hepsini sırayla tetikler.
- Somut gözlemciler:
  - `observer/DistanceTrackingObserver.java` → yeni event geldiğinde
    `CourierStatisticsService.recalculate(courierId)` çağırır.
  - `observer/StoreEntranceObserver.java` → yeni event geldiğinde
    `StoreVisitService.recalculate(courierId)` çağırır.
- Akış: `CourierLocationIngestionService.ingest(...)` yeni event'i repository'ye
  kaydettikten sonra `locationEventPublisher.publish(event)` çağırır; bu da her
  iki gözlemciyi otomatik tetikler. Böylece "mesafe güncelleme" ve "mağaza giriş
  kontrolü" iş mantıkları birbirinden bağımsız, gevşek bağlı (loosely coupled)
  bileşenler olarak eklenmiştir — yeni bir gözlemci eklemek için
  `CourierLocationIngestionService` değiştirilmez, sadece yeni bir
  `LocationEventObserver` bean'i tanımlamak yeterlidir.

> Not: Projede ayrıca Repository deseni (`CourierEventRepository` arayüzü +
> `InMemoryCourierEventRepository` implementasyonu) ve DTO/Adapter tarzı
> dönüşümler (`TrackingController` içinde domain nesnelerinin response
> DTO'larına map'lenmesi) de mevcuttur; ancak zorunlu 2 desen olarak
> **Strategy** ve **Observer** esas alınmıştır.

---

## 5. REST Uç Noktaları (Özet)

| Metot | Yol | Açıklama | Kod |
|---|---|---|---|
| POST | `/api/locations` | Kurye konum event'i girişi (time, courierId, lat, lng) | `TrackingController.ingestLocation` |
| GET | `/api/couriers/{courierId}/distance` | Kuryenin toplam kat ettiği mesafe | `TrackingController.getCourierDistance` |
| GET | `/api/store-visits` | Tüm kuryelerin onaylanmış mağaza girişleri | `TrackingController.getStoreVisits` |
| GET | `/api/stores` | Yüklenen mağaza kataloğu | `StoreController.getStores` |

Giriş doğrulaması (`@Valid`) `dto/CourierLocationRequest.java` üzerinde
`@NotNull`/`@NotBlank` anotasyonlarıyla yapılır (`spring-boot-starter-validation`).

---

## 6. Veri Modeli

- `domain/CourierLocationEvent.java`: gelen ham event (time, courierId, lat, lng).
- `domain/Store.java`: mağaza (id, name, lat, lng) + `distanceTo(...)` yardımcı metodu.
- `domain/StoreVisitLog.java`: onaylanmış giriş kaydı (time, courierId, storeId, storeName, lat, lng).
- `repository/InMemoryCourierEventRepository.java`: `ConcurrentHashMap` ile
  courierId → event listesi eşlemesi; her ekleme sonrası zaman sırasına göre
  sıralanır (thread-safe, in-memory).

---

## 7. Testler

- `src/test/java/com/migros/couriertracking/strategy/HaversineDistanceStrategyTest.java`:
  Haversine hesaplamasının bilinen bir mesafe aralığında doğru sonuç verdiğini
  doğrular.
- `src/test/java/com/migros/couriertracking/service/TrackingFlowTest.java`:
  Uçtan uca senaryo — bir kurye tek bir mağazaya birden fazla kez girip
  çıkıyor; testte 1 dakikadan kısa aralıklı tekrar girişlerin sayılmadığı,
  1 dakikadan uzun aralıktaki girişin ayrı bir giriş olarak sayıldığı
  (`hasSize(2)`) ve toplam mesafenin sıfırdan büyük hesaplandığı doğrulanır.

Çalıştırma: `mvn test`

---

## 8. Çalıştırma Talimatları

Ayrıntılı adımlar için bkz. `README.md`. Özetle:

```bash
mvn spring-boot:run   # uygulamayı başlatır (varsayılan port 8080)
mvn test              # testleri çalıştırır
```

Örnek istek:

```bash
curl -X POST http://localhost:8080/api/locations \
  -H "Content-Type: application/json" \
  -d '{"time":"2026-07-18T10:00:00Z","courierId":"courier-1","lat":40.9923307,"lng":29.1244229}'

curl http://localhost:8080/api/couriers/courier-1/distance
curl http://localhost:8080/api/store-visits
```
