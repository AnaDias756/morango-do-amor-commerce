# Configuração do Abacatepay

Este documento descreve como configurar a integração com o gateway de pagamentos Abacatepay.

## Variáveis de Ambiente Obrigatórias

Para usar a integração com o Abacatepay em produção, você deve configurar as seguintes variáveis de ambiente:

### API Configuration

```bash
# Token de autenticação da API do Abacatepay
ABACATEPAY_API_TOKEN=seu_token_aqui

# URL base da API (opcional, padrão: https://api.abacatepay.com)
ABACATEPAY_API_BASE_URL=https://api.abacatepay.com

# Habilitar/desabilitar modo mock (padrão: true para dev, false para prod)
ABACATEPAY_API_MOCK_ENABLED=false
```

### Webhook Configuration

```bash
# Secret para validação de assinatura dos webhooks
ABACATEPAY_WEBHOOK_SECRET=seu_webhook_secret_aqui

# Habilitar validação de assinatura (padrão: false para dev, true para prod)
ABACATEPAY_WEBHOOK_SIGNATURE_ENABLED=true
```

## Configuração por Ambiente

### Desenvolvimento

```bash
ABACATEPAY_API_TOKEN=mock-token
ABACATEPAY_API_MOCK_ENABLED=true
ABACATEPAY_WEBHOOK_SIGNATURE_ENABLED=false
```

### Produção

```bash
ABACATEPAY_API_TOKEN=seu_token_de_producao
ABACATEPAY_API_MOCK_ENABLED=false
ABACATEPAY_WEBHOOK_SECRET=seu_webhook_secret_de_producao
ABACATEPAY_WEBHOOK_SIGNATURE_ENABLED=true
```

## Endpoints da Integração

### Webhook

O endpoint para receber webhooks do Abacatepay é:

```
POST /api/webhooks/abacatepay
```

Configure este endpoint no painel do Abacatepay para receber notificações de pagamento.

## Funcionalidades Implementadas

- ✅ Criação de cobranças (billing)
- ✅ Gerenciamento de clientes
- ✅ Processamento de webhooks
- ✅ Validação de assinatura de webhooks
- ✅ Modo mock para desenvolvimento
- ✅ Configuração via variáveis de ambiente

## Segurança

- Todas as credenciais são obtidas via variáveis de ambiente
- Nenhuma informação sensível está hardcoded no código
- Validação de assinatura HMAC-SHA256 para webhooks
- Logs de segurança para tentativas de webhook inválidas

## Como Obter as Credenciais

1. Acesse o painel do Abacatepay
2. Vá para a seção de API/Integrações
3. Gere um token de API
4. Configure o webhook secret
5. Configure o endpoint de webhook: `https://seu-dominio.com/api/webhooks/abacatepay`

## Troubleshooting

### Erro de Autenticação

Verifique se a variável `ABACATEPAY_API_TOKEN` está configurada corretamente.

### Webhook não Funciona

1. Verifique se o endpoint está acessível publicamente
2. Confirme se o `ABACATEPAY_WEBHOOK_SECRET` está correto
3. Verifique os logs da aplicação para erros de validação

### Modo Mock

Para testar sem fazer chamadas reais para a API:

```bash
ABACATEPAY_API_MOCK_ENABLED=true
```