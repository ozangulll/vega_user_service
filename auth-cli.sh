#!/bin/bash

# Vega User Service CLI Authentication Script
# Kullanım: ./auth-cli.sh [login|register|validate] [username] [password] [email] [firstName] [lastName]

BASE_URL="http://localhost:8082/api/auth"
TOKEN_FILE="$HOME/.vega_token"

# Renkli çıktı için
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Yardım fonksiyonu
show_help() {
    echo "Vega User Service CLI Authentication"
    echo ""
    echo "Kullanım:"
    echo "  $0 register <username> <password> <email> <firstName> <lastName>"
    echo "  $0 login <username> <password>"
    echo "  $0 validate [token]"
    echo "  $0 logout"
    echo ""
    echo "Örnekler:"
    echo "  $0 register myuser mypass123 myuser@example.com My User"
    echo "  $0 login myuser mypass123"
    echo "  $0 validate"
    echo "  $0 logout"
}

# Token'ı dosyaya kaydet
save_token() {
    echo "$1" > "$TOKEN_FILE"
    chmod 600 "$TOKEN_FILE"
}

# Token'ı dosyadan oku
load_token() {
    if [ -f "$TOKEN_FILE" ]; then
        cat "$TOKEN_FILE"
    fi
}

# Token'ı sil
clear_token() {
    rm -f "$TOKEN_FILE"
}

# Kullanıcı kaydı
register() {
    local username="$1"
    local password="$2"
    local email="$3"
    local firstName="$4"
    local lastName="$5"
    
    if [ -z "$username" ] || [ -z "$password" ] || [ -z "$email" ] || [ -z "$firstName" ] || [ -z "$lastName" ]; then
        echo -e "${RED}Hata: Tüm parametreler gerekli${NC}"
        show_help
        exit 1
    fi
    
    echo -e "${YELLOW}Kullanıcı kaydı yapılıyor...${NC}"
    
    response=$(curl -s -X POST "$BASE_URL/register" \
        -H "Content-Type: application/json" \
        -d "{
            \"username\": \"$username\",
            \"email\": \"$email\",
            \"password\": \"$password\",
            \"firstName\": \"$firstName\",
            \"lastName\": \"$lastName\"
        }")
    
    if echo "$response" | grep -q '"token"'; then
        token=$(echo "$response" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
        save_token "$token"
        echo -e "${GREEN}✓ Kullanıcı başarıyla kaydedildi!${NC}"
        echo -e "${GREEN}✓ Token kaydedildi: $token${NC}"
    else
        echo -e "${RED}✗ Kayıt başarısız: $response${NC}"
        exit 1
    fi
}

# Giriş yapma
login() {
    local username="$1"
    local password="$2"
    
    if [ -z "$username" ] || [ -z "$password" ]; then
        echo -e "${RED}Hata: Kullanıcı adı ve şifre gerekli${NC}"
        show_help
        exit 1
    fi
    
    echo -e "${YELLOW}Giriş yapılıyor...${NC}"
    
    response=$(curl -s -X POST "$BASE_URL/login" \
        -H "Content-Type: application/json" \
        -d "{
            \"usernameOrEmail\": \"$username\",
            \"password\": \"$password\"
        }")
    
    if echo "$response" | grep -q '"token"'; then
        token=$(echo "$response" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
        save_token "$token"
        echo -e "${GREEN}✓ Giriş başarılı!${NC}"
        echo -e "${GREEN}✓ Token kaydedildi: $token${NC}"
    else
        echo -e "${RED}✗ Giriş başarısız: $response${NC}"
        exit 1
    fi
}

# Token doğrulama
validate() {
    local token="$1"
    
    if [ -z "$token" ]; then
        token=$(load_token)
    fi
    
    if [ -z "$token" ]; then
        echo -e "${RED}Hata: Token bulunamadı. Önce giriş yapın.${NC}"
        exit 1
    fi
    
    echo -e "${YELLOW}Token doğrulanıyor...${NC}"
    
    response=$(curl -s -X POST "$BASE_URL/validate" \
        -H "Authorization: Bearer $token")
    
    if [ "$response" = "true" ]; then
        echo -e "${GREEN}✓ Token geçerli!${NC}"
    else
        echo -e "${RED}✗ Token geçersiz: $response${NC}"
        clear_token
        exit 1
    fi
}

# Çıkış yapma
logout() {
    clear_token
    echo -e "${GREEN}✓ Çıkış yapıldı. Token silindi.${NC}"
}

# Ana fonksiyon
main() {
    case "$1" in
        "register")
            register "$2" "$3" "$4" "$5" "$6"
            ;;
        "login")
            login "$2" "$3"
            ;;
        "validate")
            validate "$2"
            ;;
        "logout")
            logout
            ;;
        "help"|"-h"|"--help")
            show_help
            ;;
        *)
            echo -e "${RED}Geçersiz komut: $1${NC}"
            show_help
            exit 1
            ;;
    esac
}

# Script'i çalıştır
main "$@"
