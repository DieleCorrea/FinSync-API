# Integração com Supabase

## O que mudou
- `pom.xml` → removido H2, adicionado PostgreSQL
- `application.properties` → configurado com a URL do Supabase

## O que você precisa fazer

Adicione as variáveis de ambiente no IntelliJ:
Run → Edit Configurations → Environment Variables
DB_PASSWORD=solicitar para a Diele
Rode o projeto normalmente. ▶

## Confirmação de sucesso
Se aparecer isso no log, está funcionando:
Database JDBC URL [jdbc:postgresql://aws-1-sa-east-1.pooler.supabase.com]

