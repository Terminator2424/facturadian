# FacturaDIAN

Facturación electrónica de venta con las reglas de la DIAN (IVA 19 % y ReteIVA 15 %).
Proyecto integrador de Desarrollo Orientado a Servicios 2026-2, Universidad Santo Tomás, Seccional Tunja.

## Integrante

- Edison Javier Gonzalez Velosa

## Propuesta

- **Problema:** muchos negocios pequeños facturan a mano, escriben nombres y precios cada vez, calculan mal el IVA y el ReteIVA y no controlan qué facturas están emitidas, pagadas o anuladas.
- **A quién le sirve:** al dueño de la tienda, que administra el catálogo y supervisa las ventas, y a los cajeros, que facturan escaneando o buscando los productos.
- **Datos que guarda y consulta:** clientes (NIT, correo, si son agentes retenedores), productos del catálogo (código de barras o SKU, nombre, precio, estado), facturas con sus ítems, estado, totales y el cajero que las emitió, y los usuarios con su rol.

## Tecnologías

Java 17, Quarkus 3.16, Hibernate ORM con Panache, MariaDB (el "MySQL" de XAMPP), Qute, Bootstrap 5 (CDN), OpenPDF, SmallRye OpenAPI (Swagger) y Jackson (respuesta JSON del buscador de productos).

## Arquitectura hexagonal

    com.facturadian
    ├── domain                      (Java puro: sin Quarkus, JPA ni Jakarta)
    │   ├── model                   Client, Invoice, InvoiceItem, InvoiceStatus, SalesSummary,
    │   │                           AppUser, Role, Product
    │   ├── exception               BusinessException, DuplicateInvoiceNumberException
    │   └── service                 (reservada para reglas de dominio puras)
    ├── application
    │   ├── port
    │   │   ├── in                  InvoiceUseCase, ClientUseCase, UserUseCase, ProductUseCase
    │   │   └── out                 InvoiceRepositoryPort, ClientRepositoryPort, UserRepositoryPort,
    │   │                           ProductRepositoryPort, PdfGeneratorPort, PasswordHasherPort
    │   ├── usecase                 InvoiceService, ClientService, UserService, ProductService
    │   └── exception               (reservada)
    └── infrastructure
        ├── config                  ApplicationConfig, TemplateDataConfig, DataSeeder, ProductSeeder,
        │                           AdminSeeder, DemoUserSeeder
        └── adapter
            ├── in
            │   ├── rest            resource (controladores HTML con Qute, @Tag y @Operation),
            │   │                   dto y mapper (respuesta del buscador de productos)
            │   └── security        Proveedores de identidad de Quarkus y CurrentUser
            └── out
                ├── persistence     Entidades JPA, mappers, repositorios Panache y adaptadores
                ├── pdf             Generación de PDF con OpenPDF
                └── security        Cifrado de contraseñas con BCrypt

Las dependencias apuntan hacia adentro: infraestructura conoce a la aplicación y la aplicación conoce al dominio, nunca al revés. Los puertos y los casos de uso viven en la capa de aplicación. El dominio y la aplicación son Java puro; `ApplicationConfig` los registra como beans.

## Roles

| Acción | Administrador (dueño) | Cajero |
|---|---|---|
| Crear facturas y agregar ítems | No | Sí |
| Ver facturas, pagar, anular y descargar PDF | Sí | Sí |
| Clientes y reportes | Sí | Sí |
| Ver el catálogo de productos | Sí | Sí (solo lectura) |
| Crear, modificar y activar o desactivar productos | Sí | No |

El administrador es único y no se crea por el registro: se crea al arrancar. El registro público siempre crea cajeros.

## Reglas de negocio

- IVA del 19 % sobre el subtotal.
- Si el cliente es agente retenedor, ReteIVA = 15 % del IVA. Total = subtotal + IVA - ReteIVA.
- Una factura nace Emitida. Solo una Emitida se puede pagar, anular o recibir ítems; una Pagada no se anula.
- Los ítems salen del catálogo: el nombre y el precio los pone el sistema y el cajero solo elige la cantidad. Un producto inactivo no se puede vender.
- Cada ítem guarda el nombre y el precio que tenía el producto al venderse, así que cambiar precios no altera facturas anteriores.
- Máximo 50 ítems por factura; precio con 2 decimales como máximo y cantidad entre 1 y 100.000.
- El código del producto es único (3 a 30 caracteres: letras, números y guion).
- Cada factura guarda el cajero que la emitió (`issued_by_id`).
- El número de factura es consecutivo (`FV-000001`, ...). Si dos cajeros facturan a la vez y obtienen el mismo número, la restricción `unique` de la base rechaza al segundo y la aplicación reintenta con el siguiente.
- Solo puede existir un administrador.
- Las facturas Anuladas no suman en los reportes.

## Cómo ejecutar

Requisitos: Java 17 o superior y XAMPP (el wrapper `mvnw` descarga Maven si no lo tienes).

1. Abre XAMPP y arranca **MySQL** (puerto 3306). Con Apache puedes abrir phpMyAdmin.
2. No hay que crear la base ni las tablas: se crean solas al arrancar y se cargan datos de prueba (clientes, facturas y un catálogo de 14 productos).
3. Si tu usuario root tiene clave, copia `.env.example` como `.env` y completa `DB_PASSWORD`.
4. Desde la carpeta del proyecto:

        .\mvnw.cmd quarkus:dev        (Windows)
        ./mvnw quarkus:dev            (Linux o Mac)

5. Abre http://localhost:8080 e inicia sesión.

### Usuarios (solo en modo desarrollo)

| Rol | Usuario | Clave |
|---|---|---|
| Administrador | `admin` | `Tienda2026*Dian` |
| Cajero (demostración) | `demo` | `Fact2026*Dian` |

También puedes crear cuentas de cajero en la pantalla de registro.

### Cambiar la clave del administrador

1. Copia el archivo de ejemplo: `Copy-Item .env.example .env`
2. En `.env` escribe: `ADMIN_PASSWORD=TuClaveNueva*2026`
3. Reinicia la aplicación: la clave del administrador se actualiza sola.

La clave debe tener de 10 a 72 caracteres, con mayúscula, minúscula, número y símbolo, y no puede contener el nombre de usuario (`admin`). En producción no hay clave por defecto: si no se define `ADMIN_PASSWORD`, el administrador no se crea.

## Interfaz

Todas las pantallas siguen una misma idea: una caja registradora que imprime sus pantallas como recibos.

- **Login y registro:** caja con pantalla LCD que refleja lo que se escribe, teclado numérico y recibo que sale de la ranura.
- **Facturas:** contadores LCD que se recalculan con el filtro, chips de estado, buscador instantáneo y sellos de estado.
- **Nueva factura (cajero):** se elige el cliente y se agregan los productos escaneando el código de barras o escribiendo el inicio del nombre (por ejemplo, "li" muestra Libro, Libreta, Lima y Lijadora). Escanear el mismo producto otra vez suma una unidad. La pantalla del cliente muestra subtotal, IVA, ReteIVA y total en vivo (vista previa; el cálculo oficial lo hace el dominio).
- **Productos:** el administrador registra, modifica y activa o desactiva productos; el cajero solo los consulta.
- **Detalle:** comprobante con cajero, sello PAGADA o ANULADA, descarga en PDF e impresión. Los productos se agregan desde un menú.
- **Reportes (Cierre Z):** resumen por rango de fechas, dona de cobrado vs pendiente, barras por factura y PDF.

Bootstrap 5 aporta la grilla, los botones y las alertas; el estilo propio está en `css/app.css`, `css/receipt.css` y `css/pos.css`.

## Documentación Swagger

- Interfaz: http://localhost:8080/q/swagger-ui
- Documento: http://localhost:8080/q/openapi

## Seguridad

- Inicio de sesión por formulario y registro. Todo el sitio exige sesión, salvo login, registro y la documentación.
- Dos roles (`admin` y `cashier`). Cada ruta restringida usa `@RolesAllowed`: si alguien fuerza la URL sin el rol, el servidor responde 403. La interfaz además oculta lo que no corresponde.
- Contraseñas cifradas con BCrypt; política de 10 a 72 caracteres con mayúscula, minúscula, número y símbolo.
- Bloqueo de 15 minutos tras 5 intentos fallidos y mensaje de error genérico.
- Cookie de sesión HttpOnly, SameSite=Strict, cifrada, con caducidad por inactividad de 30 minutos.
- Protección CSRF: todos los formularios POST llevan un token (extensión `quarkus-rest-csrf`: cookie y campo oculto `csrf-token`, incluido desde `templates/partials/csrf.html`). Sin token válido, el servidor responde 400.
- Los precios nunca vienen del navegador: el servidor los toma del catálogo.
- Cabeceras X-Content-Type-Options, X-Frame-Options, Referrer-Policy y Permissions-Policy.
- La aplicación solo escucha en 127.0.0.1.

## Evidencia en phpMyAdmin

Abre http://localhost/phpmyadmin y entra a la base `facturadian`. Relaciones: `clients` 1-N `invoices` (`client_id`), `app_users` 1-N `invoices` (`issued_by_id`) e `invoices` 1-N `invoice_items` (`invoice_id`). Se ven en la pestaña Diseñador. Además, `products` 1-N `invoice_items` (`product_id`, opcional): cada ítem guarda la referencia al producto y también el nombre y el precio del momento de la venta.

## Cumplimiento de los lineamientos

| Lineamiento | Dónde se cumple |
|---|---|
| Arquitectura hexagonal, dominio sin framework | `domain/` y `application/` en Java puro; servicios conectados desde `ApplicationConfig` |
| Única entrada: vistas HTML | Controladores Qute; sin consola. El buscador de la vista de nueva factura consulta `/productos/buscar` (JSON) |
| JPA con `@ManyToOne` y `@JoinColumn` | `InvoiceEntity.client`, `InvoiceEntity.issuedBy`, `InvoiceItemEntity.invoice` |
| Base con `createDatabaseIfNotExist` y tablas con `update` | `application.properties` |
| Datos de prueba | `DataSeeder`, `ProductSeeder`, `AdminSeeder` y `DemoUserSeeder` |
| 3 o más pantallas, navbar, main y footer, alertas | Facturas, Nueva, Detalle, Clientes, Productos, Reportes y `layout.html` |
| Swagger con `@Tag` y `@Operation` | `/q/swagger-ui` y `/q/openapi` |
| Reporte PDF con OpenPDF | Comprobante de factura y resumen por fechas |
| Flujo crear, listar y cambiar estado | Emitida, Pagada y Anulada; productos activos e inactivos |
| Roles y control de acceso | `Role`, `@RolesAllowed`, `AdminSeeder` |
| Proyecto Maven con `mvnw` y README | Incluidos |

## Notas

- Las entidades del negocio son clientes, productos, facturas e ítems; `app_users` soporta el inicio de sesión y se relaciona con las facturas por el cajero emisor.
- XAMPP trae MariaDB, por eso se usa su driver. Para el curso es el "MySQL" de XAMPP.
- En Quarkus 3.16 la propiedad que crea las tablas es `quarkus.hibernate-orm.database.generation=update`; en versiones más nuevas equivale a `quarkus.hibernate-orm.schema-management.strategy=update`.