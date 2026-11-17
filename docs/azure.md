# Deploy no Azure

Troque `<...>` pelos seus nomes. Região sugerida: `brazilsouth`.

## 1. Grupo de recursos e banco

```bash
az group create -n rg-almoxarifado -l brazilsouth

az postgres flexible-server create \
  -g rg-almoxarifado -n <pg-almoxarifado> -l brazilsouth \
  --admin-user pgadmin --admin-password '<SENHA-FORTE>' \
  --sku-name Standard_B1ms --tier Burstable --version 16 \
  --public-access 0.0.0.0     # libera serviços do Azure (ajuste o firewall depois)

az postgres flexible-server db create -g rg-almoxarifado -s <pg-almoxarifado> -d almoxarifado
```

## 2. API (App Service Java 17)

```bash
az appservice plan create -g rg-almoxarifado -n plan-almox --sku B1 --is-linux
az webapp create -g rg-almoxarifado -p plan-almox -n <api-almoxarifado> --runtime "JAVA:17-java17"

az webapp config appsettings set -g rg-almoxarifado -n <api-almoxarifado> --settings \
  DB_URL="jdbc:postgresql://<pg-almoxarifado>.postgres.database.azure.com:5432/almoxarifado?sslmode=require" \
  DB_USER=pgadmin \
  DB_PASSWORD='<SENHA-FORTE>' \
  CORS_ORIGINS="https://<url-do-static-web-app>" \
  WEBSITES_PORT=8080
```

O Flyway cria as tabelas sozinho no primeiro start.

## 3. Front (Static Web Apps)

Crie um Static Web App (portal ou `az staticwebapp create`) e copie o **deployment token**.
Antes do build, ajuste `frontend/src/environments/environment.prod.ts` com a URL da API:
`https://<api-almoxarifado>.azurewebsites.net/api`.

## 4. Segredos no GitHub (Settings > Secrets and variables > Actions)

| Segredo | Valor |
|---|---|
| `AZURE_WEBAPP_NAME` | nome do App Service da API |
| `AZURE_WEBAPP_PUBLISH_PROFILE` | baixe em App Service > Get publish profile |
| `AZURE_STATIC_WEB_APPS_API_TOKEN` | deployment token do Static Web App |

## 5. Extra: exportar CSV para o Blob (opcional)

```bash
az storage account create -g rg-almoxarifado -n <stalmoxarifado> -l brazilsouth --sku Standard_LRS
az storage container create --account-name <stalmoxarifado> -n relatorios
```
