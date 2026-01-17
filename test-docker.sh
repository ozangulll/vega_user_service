#!/bin/bash

# VEGA User Service - Docker Test Script
# Bu script Docker Compose ile servisleri başlatır ve test eder

set -e

echo "========================================="
echo "🚀 VEGA User Service - Docker Test"
echo "========================================="
echo ""

cd "$(dirname "$0")"

# 1. Eski container'ları temizle
echo "📦 Eski container'ları temizliyorum..."
docker compose down -v 2>/dev/null || true
echo "✅ Temizlik tamamlandı"
echo ""

# 2. Servisleri build ve başlat
echo "🔨 Servisleri build ediyorum..."
docker compose build
echo ""

echo "🚀 Servisleri başlatıyorum..."
docker compose up -d
echo ""

# 3. MySQL'in hazır olmasını bekle
echo "⏳ MySQL veritabanının hazır olmasını bekliyorum..."
timeout=60
counter=0
while ! docker compose exec -T mysql mysqladmin ping -h localhost --silent 2>/dev/null; do
    sleep 2
    counter=$((counter + 2))
    if [ $counter -ge $timeout ]; then
        echo "❌ MySQL başlatılamadı (timeout: ${timeout}s)"
        docker compose logs mysql
        exit 1
    fi
    echo -n "."
done
echo ""
echo "✅ MySQL hazır!"
echo ""

# 4. User Service'in hazır olmasını bekle
echo "⏳ User Service'in hazır olmasını bekliyorum..."
timeout=120
counter=0
while ! curl -s http://localhost:8085/actuator/health > /dev/null 2>&1; do
    sleep 3
    counter=$((counter + 3))
    if [ $counter -ge $timeout ]; then
        echo "❌ User Service başlatılamadı (timeout: ${timeout}s)"
        docker compose logs user-service
        exit 1
    fi
    echo -n "."
done
echo ""
echo "✅ User Service hazır!"
echo ""

# 5. Health Check
echo "========================================="
echo "🏥 Health Check"
echo "========================================="
echo ""

echo "MySQL Health:"
docker compose exec -T mysql mysqladmin ping -h localhost
echo ""

echo "User Service Health:"
curl -s http://localhost:8085/actuator/health | jq . || curl -s http://localhost:8085/actuator/health
echo ""
echo ""

# 6. Login Test
echo "========================================="
echo "🔐 Login Test (versionengineai / versionengineai)"
echo "========================================="
echo ""

RESPONSE=$(curl -s -X POST http://localhost:8085/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "versionengineai",
    "password": "versionengineai"
  }')

echo "Response:"
echo "$RESPONSE" | jq . 2>/dev/null || echo "$RESPONSE"
echo ""

TOKEN=$(echo "$RESPONSE" | jq -r '.token' 2>/dev/null || echo "")

if [ -z "$TOKEN" ] || [ "$TOKEN" = "null" ]; then
    echo "❌ Login başarısız! Token alınamadı."
    echo "Logs:"
    docker compose logs user-service | tail -20
    exit 1
fi

echo "✅ Login başarılı! Token alındı."
echo "Token: ${TOKEN:0:50}..."
echo ""

# 7. Token Validation Test
echo "========================================="
echo "✅ Token Validation Test"
echo "========================================="
echo ""

VALIDATION_RESPONSE=$(curl -s -X POST http://localhost:8085/api/auth/validate \
  -H "Authorization: Bearer $TOKEN")

echo "Validation Response: $VALIDATION_RESPONSE"
echo ""

if [ "$VALIDATION_RESPONSE" = "true" ]; then
    echo "✅ Token validation başarılı!"
else
    echo "❌ Token validation başarısız!"
    exit 1
fi

echo ""

# 8. Veritabanı Kontrolü
echo "========================================="
echo "🗄️  Veritabanı Kontrolü"
echo "========================================="
echo ""

echo "Kullanıcı listesi:"
docker compose exec -T mysql mysql -uversionengineai -pversionengineai vega_user_db \
  -e "SELECT username, email, role, is_active FROM users;" 2>/dev/null || \
  docker compose exec -T mysql mysql -uroot -prootpassword vega_user_db \
  -e "SELECT username, email, role, is_active FROM users;"

echo ""

# 9. Servis Durumu
echo "========================================="
echo "📊 Servis Durumu"
echo "========================================="
echo ""

docker compose ps

echo ""
echo "========================================="
echo "✅ Tüm testler başarılı!"
echo "========================================="
echo ""
echo "Kullanım:"
echo "  - Logları görmek: docker compose logs -f user-service"
echo "  - Servisleri durdur: docker compose stop"
echo "  - Servisleri sil: docker compose down"
echo "  - Verileri de sil: docker compose down -v"
echo ""
