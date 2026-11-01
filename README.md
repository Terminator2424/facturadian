# FacturaDIAN

Facturación electrónica de venta con las reglas de la DIAN (IVA 19 % y ReteIVA 15 %).
Proyecto integrador de Desarrollo Orientado a Servicios 2026-2, Universidad Santo Tomás, Seccional Tunja.

## Integrante

- Edison Javier Gonzalez Velosa

## Propuesta

- **Problema:** muchos negocios pequeños facturan a mano, calculan mal el IVA y el ReteIVA y no controlan qué facturas están emitidas, pagadas o anuladas.
- **A quién le sirve:** al personal administrativo y contable, que emite facturas, las cobra y consulta las ventas por periodo.
- **Datos que guarda y consulta:** clientes (NIT, correo, si son agentes retenedores), facturas con sus ítems, estado, totales y el cajero que las emitió, y los usuarios que acceden.

## Tecnologías

Java 17, Quarkus 3.16, Hibernate ORM con Panache, MariaDB (el "MySQL" de XAMPP), Qute, Bootstrap 5 (CDN), OpenPDF y SmallRye OpenAPI (Swagger).

## Arquitectura hexagonal

    com.facturadian
    ├── domain                      (Java puro: sin Quarkus, JPA ni Jakarta)
    │   ├── model                   Client, Invoice, InvoiceItem, InvoiceStatus, SalesSummary, AppUser
    │   ├── exception               BusinessException, DuplicateInvoiceNumberException
    │   ├── port.input              InvoiceUseCase, ClientUseCase, UserUseCase
    │   ├── port.output             InvoiceRepositoryPort, ClientRepositoryPort, UserRepositoryPort,
    │   │                           PdfGeneratorPort, PasswordHasherPort
    │   └── service                 InvoiceService, ClientService, UserService (reglas de negocio)
    └── infrastructure
        ├── config                  ApplicationConfig, TemplateDataConfig, DataSeeder, DemoUserSeeder
        └── adapter
            ├── in.web              Controladores HTML (Qute) con @Tag y @Operation
            ├── in.security         Proveedores de identidad de Quarkus y CurrentUser
            ├── out.persistence     Entidades JPA, mappers, repositorios Panache y adaptadores
            ├── out.pdf             Generación de PDF con OpenPDF
            └── out.security        Cifrado de contraseñas con BCrypt

Todas las dependencias apuntan hacia el dominio. La única entrada del sistema es la vista HTML.

## Reglas de negocio

- IVA del 19 % sobre el subtotal.
- Si el cliente es agente retenedor, ReteIVA = 15 % del IVA. Total = subtotal + IVA - ReteIVA.
- Una factura nace Emitida. Solo una Emitida se puede pagar, anular o recibir ítems; una Pagada no se anula.
- Máximo 50 ítems por factura; precio con 2 decimales como máximo y cantidad entre 1 y 100.000.
- Cada factura guarda el cajero que la emitió (`issued_by_id`).
- El número de factura es consecutivo (`FV-000001`, ...). Si dos cajeros facturan a la vez y obtienen el mismo número, la restricción `unique` de la base rechaza al segundo y el dominio reintenta con el siguiente.
- Las facturas Anuladas no suman en los reportes.

## Cómo ejecutar

Requisitos: Java 17 o superior y XAMPP (el wrapper `mvnw` descarga Maven si no lo tienes).

1. Abre XAMPP y arranca **MySQL** (puerto 3306). Con Apache puedes abrir phpMyAdmin.
2. No hay que crear la base ni las tablas: se crean solas al arrancar y se cargan datos de prueba.
3. Si tu usuario root tiene clave, copia `.env.example` como `.env` y completa `DB_PASSWORD`.
4. Desde la carpeta del proyecto:

        .\mvnw.cmd quarkus:dev        (Windows)
        ./mvnw quarkus:dev            (Linux o Mac)

5. Abre http://localhost:8080 e inicia sesión.

Usuario de demostración (solo en modo desarrollo): `demo` / `Fact2026*Dian`. También puedes crear una cuenta en la pantalla de registro.

## Interfaz

Todas las pantallas siguen una misma idea: una caja registradora que imprime sus pantallas como recibos.

- **Login y registro:** caja con pantalla LCD que refleja lo que se escribe, teclado numérico y recibo que sale de la ranura.
- **Facturas:** contadores LCD que se recalculan con el filtro, chips de estado, buscador instantáneo y sellos de estado.
- **Nueva factura:** pantalla del cliente con subtotal, IVA, ReteIVA y total en vivo (vista previa; el cálculo oficial lo hace el dominio).
- **Detalle:** comprobante con cajero, sello PAGADA o ANULADA, descarga en PDF e impresión.
- **Reportes (Cierre Z):** resumen por rango de fechas, dona de cobrado vs pendiente, barras por factura y PDF.

Bootstrap 5 aporta la grilla, los botones y las alertas; el estilo propio está en `css/app.css` y `css/receipt.css`.

## Documentación Swagger

- Interfaz: http://localhost:8080/q/swagger-ui
- Documento: http://localhost:8080/q/openapi

## Seguridad

- Inicio de sesión por formulario y registro. Todo el sitio exige sesión, salvo login, registro y la documentación.
- Contraseñas cifradas con BCrypt; política de 10 a 72 caracteres con mayúscula, minúscula, número y símbolo.
- Bloqueo de 15 minutos tras 5 intentos fallidos y mensaje de error genérico.
- Cookie de sesión HttpOnly, SameSite=Strict, cifrada, con caducidad por inactividad de 30 minutos.
- Protección CSRF: todos los formularios POST llevan un token (extensión `quarkus-rest-csrf`: cookie y campo oculto `csrf-token`, incluido desde `templates/partials/csrf.html`). Sin token válido, el servidor responde 400.
- Cabeceras X-Content-Type-Options, X-Frame-Options, Referrer-Policy y Permissions-Policy.
- La aplicación solo escucha en 127.0.0.1.

## Evidencia en phpMyAdmin

Abre http://localhost/phpmyadmin y entra a la base `facturadian`. Relaciones: `clients` 1-N `invoices` (`client_id`), `app_users` 1-N `invoices` (`issued_by_id`) e `invoices` 1-N `invoice_items` (`invoice_id`). Se ven en la pestaña Diseñador.

## Cumplimiento de los lineamientos

| Lineamiento | Dónde se cumple |
|---|---|
| Arquitectura hexagonal, dominio sin framework | `domain/` en Java puro; servicios conectados desde `ApplicationConfig` |
| Única entrada: vistas HTML | Controladores Qute; sin REST ni consola |
| JPA con `@ManyToOne` y `@JoinColumn` | `InvoiceEntity.client`, `InvoiceEntity.issuedBy`, `InvoiceItemEntity.invoice` |
| Base con `createDatabaseIfNotExist` y tablas con `update` | `application.properties` |
| Datos de prueba | `DataSeeder` |
| 3 o más pantallas, navbar, main y footer, alertas | Facturas, Nueva, Detalle, Clientes, Reportes y `layout.html` |
| Swagger con `@Tag` y `@Operation` | `/q/swagger-ui` y `/q/openapi` |
| Reporte PDF con OpenPDF | Comprobante de factura y resumen por fechas |
| Flujo crear, listar y cambiar estado | Emitida, Pagada y Anulada |
| Proyecto Maven con `mvnw` y README | Incluidos |

## Notas

- Las entidades del negocio son clientes, facturas e ítems; `app_users` soporta el inicio de sesión y se relaciona con las facturas por el cajero emisor.
- XAMPP trae MariaDB, por eso se usa su driver. Para el curso es el "MySQL" de XAMPP.
- En Quarkus 3.16 la propiedad que crea las tablas es `quarkus.hibernate-orm.database.generation=update`; en versiones más nuevas equivale a `quarkus.hibernate-orm.schema-management.strategy=update`.
