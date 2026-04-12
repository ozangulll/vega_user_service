# VEGA User Service - Docker Setup

Bu dokümantasyon vega_user_service'i Docker ile çalıştırma adımlarını içerir.

**Seed (örnek) kullanıcılar:** Tek kaynak `src/main/java/com/vega/userservice/config/SeedUsers.java`. Listeyi veya şifre kuralını değiştirirseniz `test-docker.sh`, repo kökündeki `start-all-services.sh` ve `init-scripts/01-init-user.sql` yorumlarını da güncelleyin.

## 🚀 Hızlı Başlangıç

### 1. Docker Compose ile Çalıştırma

```bash
cd vega_user_service
docker-compose up -d
```

Bu komut:
- MySQL veritabanını başlatır (port 3306)
- User Service'i build eder ve başlatır (port 8085)
- Veritabanı otomatik oluşturulur
- Hazır kullanıcılar uygulama açılışında oluşturulur (varsayılan ana hesap: `versionengineai` / `versionengineai` — ayrıntı `SeedUsers.java`)

### 2. Servisleri Kontrol Etme

```bash
# Servislerin durumunu kontrol et
docker-compose ps

# Logları görüntüle
docker-compose logs -f user-service

# Veritabanı logları
docker-compose logs -f mysql
```

### 3. Servisleri Durdurma

```bash
# Servisleri durdur (veriler kalır)
docker-compose stop

# Servisleri durdur ve container'ları sil (veriler kalır - volume korunur)
docker-compose down

# Her şeyi sil (veriler de silinir - dikkatli!)
docker-compose down -v
```

## 📋 Hazır Kullanıcı

Docker başlatıldığında otomatik olarak aşağıdaki kullanıcı oluşturulur:

- **Username:** `versionengineai`
- **Password:** `versionengineai`
- **Email:** `versionengineai@vega.local`
- **Role:** `USER`

## 🔧 Yapılandırma

### Portlar
- **User Service:** `8085`
- **MySQL:** `3306`

### Veritabanı
- **Database:** `vega_user_db`
- **Username:** `versionengineai`
- **Password:** `versionengineai`
- **Root Password:** `rootpassword`

### Veri Kalıcılığı

Veritabanı verileri Docker volume'da saklanır (`mysql_data`). Container'lar silinse bile veriler korunur.

## 🧪 Test Etme

### 1. Health Check

```bash
curl http://localhost:8085/actuator/health
```

### 2. Login Test

```bash
curl -X POST http://localhost:8085/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "versionengineai",
    "password": "versionengineai"
  }'
```

### 3. Token Validation

```bash
# Önce login yap ve token al
TOKEN=$(curl -s -X POST http://localhost:8085/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"versionengineai","password":"versionengineai"}' \
  | jq -r '.token')

# Token'ı validate et
curl -X POST http://localhost:8085/api/auth/validate \
  -H "Authorization: Bearer $TOKEN"
```

## 📁 Dosya Yapısı

```
vega_user_service/
├── Dockerfile              # User Service için Docker image
├── docker-compose.yml      # Docker Compose yapılandırması
├── init-scripts/           # MySQL init script'leri (isteğe bağlı)
│   └── 01-init-user.sql
└── src/
    └── main/
        └── java/
            └── com/vega/userservice/
                └── config/
                    └── DataInitializer.java  # Otomatik kullanıcı oluşturma
```

## 🔍 Sorun Giderme

### Port Zaten Kullanımda

```bash
# Port 8085 veya 3306 zaten kullanımda ise
# docker-compose.yml'de port numaralarını değiştirebilirsiniz
```

### Veritabanı Bağlantı Hatası

```bash
# MySQL container'ın hazır olduğundan emin olun
docker-compose logs mysql

# Health check
docker-compose ps
```

### Verileri Sıfırlama

```bash
# Tüm verileri sil ve yeniden başlat
docker-compose down -v
docker-compose up -d
```

## 🌐 Ana VEGA Projesi ile Bağlantı

Ana VEGA CLI projesi user service'e şu şekilde bağlanır:

```java
// AuthService.java
private static final String USER_SERVICE_URL = "http://localhost:8085/api/auth";
```

User service'in `http://localhost:8085` adresinde çalıştığından emin olun.
