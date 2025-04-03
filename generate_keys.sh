#!/bin/bash

# Definir o diretório de saída
KEY_DIR="src/main/resources"

# Criar o diretório se não existir
mkdir -p "$KEY_DIR"

# Gerar a chave privada (app.key)
openssl genpkey -algorithm RSA -out "$KEY_DIR/app.key"

# Gerar a chave pública (app.pub) a partir da chave privada
openssl rsa -in "$KEY_DIR/app.key" -pubout -out "$KEY_DIR/app.pub"

# Mensagem de sucesso
echo "Chaves RSA geradas com sucesso em $KEY_DIR"
