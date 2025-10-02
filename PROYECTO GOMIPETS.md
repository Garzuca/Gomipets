# 📘 Documento Técnico — Sistema de Gestión de Gomitas para Mascotas

## 🧱 Modelo de Base de Datos

### Tabla: `usuario`
- `usuario_id` SERIAL PRIMARY KEY  
- `nombre_usuario` TEXT NOT NULL UNIQUE  
- `correo` TEXT UNIQUE  
- `hash_contraseña` TEXT NOT NULL  
- `rol` TEXT CHECK (rol IN ('Administrador', 'Cliente'))  
- `fecha_registro` DATE DEFAULT CURRENT_DATE  
- `estado` BOOLEAN DEFAULT TRUE  

### Tabla: `cliente`
- `cliente_id` SERIAL PRIMARY KEY  
- `usuario_id` INTEGER REFERENCES usuario(usuario_id) NOT NULL  
- `nombre` TEXT NOT NULL  
- `direccion` TEXT  
- `telefono` TEXT  
- `tipo_mascota` TEXT  

### Tabla: `producto`
- `producto_id` SERIAL PRIMARY KEY  
- `nombre` TEXT NOT NULL  
- `descripcion` TEXT  
- `precio_unitario` NUMERIC NOT NULL  
- `stock_minimo` INTEGER DEFAULT 0  
- `fecha_creacion` DATE DEFAULT CURRENT_DATE  
- `estado` BOOLEAN DEFAULT TRUE  

### Tabla: `pedido`
- `pedido_id` SERIAL PRIMARY KEY  
- `cliente_id` INTEGER REFERENCES cliente(cliente_id)  
- `fecha_pedido` DATE DEFAULT CURRENT_DATE  
- `estado` TEXT CHECK (estado IN ('Pendiente', 'En_Proceso', 'Entregado', 'Cancelado'))  
- `metodo_pago` TEXT  
- `total` NUMERIC  
- `observaciones` TEXT  

### Tabla: `detalle_pedido`
- `detalle_id` SERIAL PRIMARY KEY  
- `pedido_id` INTEGER REFERENCES pedido(pedido_id)  
- `producto_id` INTEGER REFERENCES producto(producto_id)  
- `cantidad` INTEGER NOT NULL  
- `precio_unitario` NUMERIC NOT NULL  
- `subtotal` NUMERIC NOT NULL  

### Tabla: `inventario_producto`
- `inventario_id` SERIAL PRIMARY KEY  
- `producto_id` INTEGER REFERENCES producto(producto_id) UNIQUE  
- `cantidad_disponible` INTEGER NOT NULL DEFAULT 0  
- `fecha_actualizacion` TIMESTAMP DEFAULT CURRENT_TIMESTAMP  

### Tabla: `movimiento_inventario`
- `movimiento_id` SERIAL PRIMARY KEY  
- `producto_id` INTEGER REFERENCES producto(producto_id)  
- `tipo_movimiento` TEXT CHECK (tipo_movimiento IN ('Entrada', 'Salida', 'Ajuste'))  
- `cantidad` INTEGER NOT NULL  
- `cantidad_anterior` INTEGER  
- `cantidad_nueva` INTEGER  
- `fecha` TIMESTAMP DEFAULT CURRENT_TIMESTAMP  
- `motivo` TEXT  
- `usuario_id` INTEGER REFERENCES usuario(usuario_id)  

### Tabla: `insumo`
- `insumo_id` SERIAL PRIMARY KEY  
- `nombre` TEXT NOT NULL  
- `descripcion` TEXT  
- `unidad_medida` TEXT NOT NULL  
- `cantidad_stock` NUMERIC NOT NULL DEFAULT 0  
- `stock_minimo` NUMERIC DEFAULT 0  
- `fecha_ingreso` DATE DEFAULT CURRENT_DATE  
- `fecha_vencimiento` DATE  
- `costo_unitario` NUMERIC NOT NULL  
- `proveedor` TEXT  
- `estado` BOOLEAN DEFAULT TRUE  

### Tabla: `producto_insumo` (Recetas de producción)
- `id` SERIAL PRIMARY KEY  
- `producto_id` INTEGER REFERENCES producto(producto_id)  
- `insumo_id` INTEGER REFERENCES insumo(insumo_id)  
- `cantidad_requerida` NUMERIC NOT NULL  
- `unidad` TEXT  
- UNIQUE(producto_id, insumo_id)  

### Tabla: `orden_produccion`
- `orden_id` SERIAL PRIMARY KEY  
- `producto_id` INTEGER REFERENCES producto(producto_id)  
- `cantidad_planificada` INTEGER NOT NULL  
- `cantidad_producida` INTEGER DEFAULT 0  
- `fecha_creacion` TIMESTAMP DEFAULT CURRENT_TIMESTAMP  
- `fecha_inicio` TIMESTAMP  
- `fecha_fin` TIMESTAMP  
- `estado` TEXT CHECK (estado IN ('Planificada', 'En_Proceso', 'Completada', 'Cancelada'))  
- `usuario_id` INTEGER REFERENCES usuario(usuario_id)  
- `observaciones` TEXT  

### Tabla: `detalle_produccion`
- `detalle_id` SERIAL PRIMARY KEY  
- `orden_id` INTEGER REFERENCES orden_produccion(orden_id)  
- `insumo_id` INTEGER REFERENCES insumo(insumo_id)  
- `cantidad_requerida` NUMERIC NOT NULL  
- `cantidad_utilizada` NUMERIC DEFAULT 0  
- `costo_insumo` NUMERIC  

### Tabla: `movimiento_insumo`
- `movimiento_id` SERIAL PRIMARY KEY  
- `insumo_id` INTEGER REFERENCES insumo(insumo_id)  
- `tipo_movimiento` TEXT CHECK (tipo_movimiento IN ('Entrada', 'Salida', 'Ajuste', 'Vencimiento'))  
- `cantidad` NUMERIC NOT NULL  
- `cantidad_anterior` NUMERIC  
- `cantidad_nueva` NUMERIC  
- `fecha` TIMESTAMP DEFAULT CURRENT_TIMESTAMP  
- `motivo` TEXT  
- `orden_produccion_id` INTEGER REFERENCES orden_produccion(orden_id)  
- `usuario_id` INTEGER REFERENCES usuario(usuario_id)  

### Tabla: `ventas_historicas`
- `id` SERIAL PRIMARY KEY  
- `producto_id` INTEGER REFERENCES producto(producto_id)  
- `fecha_venta` DATE NOT NULL  
- `cantidad_vendida` INTEGER NOT NULL  
- `precio_venta` NUMERIC  
- `pedido_id` INTEGER REFERENCES pedido(pedido_id)  

### Tabla: `pronostico`
- `id` SERIAL PRIMARY KEY  
- `producto_id` INTEGER REFERENCES producto(producto_id)  
- `fecha_pronosticada` DATE NOT NULL  
- `cantidad_estimada` INTEGER NOT NULL  
- `metodo` TEXT CHECK (metodo IN ('Promedio_Movil', 'Promedio_Ponderado', 'Suavizado_Exponencial', 'Regresion_Lineal'))  
- `parametros` JSONB  
- `fecha_calculo` TIMESTAMP DEFAULT CURRENT_TIMESTAMP  
- `usuario_id` INTEGER REFERENCES usuario(usuario_id)  

### Tabla: `error_pronostico`
- `id` SERIAL PRIMARY KEY  
- `pronostico_id` INTEGER REFERENCES pronostico(id)  
- `valor_real` INTEGER  
- `valor_pronosticado` INTEGER  
- `error_absoluto` INTEGER  
- `mad` NUMERIC  
- `mse` NUMERIC  
- `mape` NUMERIC  
- `fecha_evaluacion` TIMESTAMP DEFAULT CURRENT_TIMESTAMP  

### Tabla: `parametros_eoq`
- `id` SERIAL PRIMARY KEY  
- `producto_id` INTEGER REFERENCES producto(producto_id) UNIQUE  
- `demanda_anual` INTEGER NOT NULL  
- `costo_pedido` NUMERIC NOT NULL  
- `costo_mantenimiento` NUMERIC NOT NULL  
- `eoq_calculado` NUMERIC  
- `punto_reorden` INTEGER  
- `fecha_calculo` TIMESTAMP DEFAULT CURRENT_TIMESTAMP  

### Tabla: `clasificacion_abc`
- `id` SERIAL PRIMARY KEY  
- `producto_id` INTEGER REFERENCES producto(producto_id)  
- `valor_ventas_anuales` NUMERIC NOT NULL  
- `porcentaje_acumulado` NUMERIC  
- `categoria` TEXT CHECK (categoria IN ('A', 'B', 'C'))  
- `fecha_clasificacion` TIMESTAMP DEFAULT CURRENT_TIMESTAMP  

### Tabla: `alerta_sistema`
- `alerta_id` SERIAL PRIMARY KEY  
- `tipo` TEXT CHECK (tipo IN ('Stock_Bajo', 'Insumo_Vencido', 'Produccion_Pendiente', 'Demanda_Alta'))  
- `entidad_id` INTEGER  
- `entidad_tipo` TEXT CHECK (entidad_tipo IN ('Producto', 'Insumo', 'Orden'))  
- `mensaje` TEXT NOT NULL  
- `prioridad` TEXT CHECK (prioridad IN ('Baja', 'Media', 'Alta', 'Critica'))  
- `fecha_creacion` TIMESTAMP DEFAULT CURRENT_TIMESTAMP  
- `leida` BOOLEAN DEFAULT FALSE  
- `fecha_lectura` TIMESTAMP  

---

## 🧩 Fases del Desarrollo

1. **Autenticación y control de acceso**  
2. **Gestión de clientes y usuarios**  
3. **Gestión de productos y pedidos**  
4. **Inventario de productos terminados**  
5. **Gestión de insumos y proveedores**  
6. **Módulo de producción**  
7. **Pronóstico de demanda**  
8. **Análisis logístico y económico (EOQ, ABC)**  
9. **Sistema de alertas**  
10. **Reportes operativos y estratégicos**  

---

## 📘 Historias de Usuario

### Autenticación y Usuarios
- **Como** cliente, **quiero** registrarme con mis datos personales, **para** realizar pedidos personalizados.  
  - *Criterios:* correo único, rol cliente automático, validación de datos, creación de registro en cliente

- **Como** usuario, **quiero** iniciar sesión, **para** acceder según mi rol.  
  - *Criterios:* login con JWT, token válido por sesión, redirección según rol

- **Como** administrador, **quiero** gestionar usuarios, **para** mantener control del acceso.  
  - *Criterios:* crear/editar/desactivar usuarios, asignación de roles

### Gestión de Pedidos
- **Como** cliente, **quiero** ver el catálogo de productos, **para** seleccionar lo que necesito.  
  - *Criterios:* productos activos, precios actuales, descripción completa

- **Como** cliente, **quiero** agregar productos al carrito, **para** realizar un pedido múltiple.  
  - *Criterios:* validación de stock, cálculo automático de subtotales

- **Como** administrador, **quiero** gestionar estados de pedidos, **para** controlar el flujo operativo.  
  - *Criterios:* cambio de estado con timestamps, notificaciones automáticas

### Producción e Insumos
- **Como** administrador, **quiero** crear órdenes de producción, **para** fabricar productos según demanda.  
  - *Criterios:* verificación de insumos disponibles, cálculo de costos, programación

- **Como** administrador, **quiero** ejecutar una orden de producción, **para** convertir insumos en productos.  
  - *Criterios:* descuento automático de insumos, incremento de inventario, registro de trazabilidad

- **Como** administrador, **quiero** recibir alertas de insumos próximos a vencer, **para** evitar pérdidas.  
  - *Criterios:* alerta 7 días antes del vencimiento, categorización por prioridad

### Pronóstico y Análisis
- **Como** administrador, **quiero** generar pronósticos de demanda, **para** planificar la producción.  
  - *Criterios:* múltiples métodos disponibles, evaluación de precisión, histórico de pronósticos

- **Como** administrador, **quiero** clasificar productos ABC, **para** priorizar la gestión de inventario.  
  - *Criterios:* cálculo automático basado en ventas, actualización periódica

### Reportes y Alertas
- **Como** administrador, **quiero** ver un dashboard con alertas, **para** tomar acciones inmediatas.  
  - *Criterios:* alertas en tiempo real, priorización visual, acciones directas

- **Como** administrador, **quiero** generar reportes personalizables, **para** análisis estratégico.  
  - *Criterios:* filtros por fecha/producto/cliente, exportación a PDF/Excel

---

## ⚙️ Requerimientos Funcionales
- **RF01:** Registro y autenticación de usuarios con roles diferenciados
- **RF02:** Gestión completa de clientes (CRUD)
- **RF03:** Catálogo de productos con inventario en tiempo real
- **RF04:** Procesamiento de pedidos con múltiples estados
- **RF05:** Control de inventario con movimientos auditables
- **RF06:** Gestión de insumos con fechas de vencimiento
- **RF07:** Módulo de producción con consumo automático de insumos
- **RF08:** Pronóstico de demanda con múltiples algoritmos
- **RF09:** Análisis EOQ y clasificación ABC
- **RF10:** Sistema de alertas automatizado
- **RF11:** Reportes operativos y estratégicos
- **RF12:** Dashboard administrativo con métricas clave

## 🚫 Requerimientos No Funcionales
- **RNF01:** Seguridad: JWT, bcrypt, control de roles, validación de entrada
- **RNF02:** Performance: respuesta < 2s, paginación, índices optimizados
- **RNF03:** Disponibilidad: 99% uptime, manejo de errores graceful
- **RNF04:** Escalabilidad: conexiones concurrentes, pool de conexiones BD
- **RNF05:** Mantenibilidad: arquitectura modular, documentación Swagger
- **RNF06:** Usabilidad: interfaz intuitiva, mensajes de error claros
- **RNF07:** Portabilidad: ejecutable .jar, PostgreSQL, multi-plataforma

---

## 🌐 Endpoints REST

### Autenticación
- `POST /api/auth/register` - Registro de clientes
- `POST /api/auth/login` - Inicio de sesión
- `GET /api/auth/me` - Información del usuario actual
- `POST /api/auth/refresh` - Renovar token JWT

### Gestión de Usuarios y Clientes
- `GET /api/usuarios` - Listar usuarios (admin)
- `POST /api/usuarios` - Crear usuario (admin)
- `PUT /api/usuarios/{id}` - Actualizar usuario (admin)
- `DELETE /api/usuarios/{id}` - Desactivar usuario (admin)
- `GET /api/clientes` - Listar clientes
- `GET /api/clientes/{id}` - Obtener cliente específico
- `PUT /api/clientes/{id}` - Actualizar datos del cliente

### Productos y Catálogo
- `GET /api/productos` - Catálogo de productos
- `POST /api/productos` - Crear producto (admin)
- `GET /api/productos/{id}` - Detalle de producto
- `PUT /api/productos/{id}` - Actualizar producto (admin)
- `DELETE /api/productos/{id}` - Desactivar producto (admin)

### Pedidos
- `GET /api/pedidos` - Listar pedidos (filtros por rol)
- `POST /api/pedidos` - Crear pedido (cliente)
- `GET /api/pedidos/{id}` - Detalle de pedido
- `PUT /api/pedidos/{id}/estado` - Cambiar estado (admin)
- `GET /api/pedidos/cliente/{clienteId}` - Pedidos de un cliente

### Inventario
- `GET /api/inventario/productos` - Estado del inventario
- `POST /api/inventario/productos/movimiento` - Registrar movimiento (admin)
- `GET /api/inventario/productos/{id}/movimientos` - Historial de movimientos
- `GET /api/inventario/alertas/stock-bajo` - Productos con stock bajo

### Insumos
- `GET /api/insumos` - Listar insumos
- `POST /api/insumos` - Crear insumo (admin)
- `PUT /api/insumos/{id}` - Actualizar insumo (admin)
- `POST /api/insumos/movimiento` - Movimiento de insumos (admin)
- `GET /api/insumos/vencimientos` - Insumos próximos a vencer
- `GET /api/insumos/{id}/movimientos` - Historial de movimientos

### Producción
- `GET /api/produccion/ordenes` - Listar órdenes de producción
- `POST /api/produccion/ordenes` - Crear orden de producción (admin)
- `GET /api/produccion/ordenes/{id}` - Detalle de orden
- `PUT /api/produccion/ordenes/{id}/iniciar` - Iniciar producción (admin)
- `PUT /api/produccion/ordenes/{id}/completar` - Completar producción (admin)
- `GET /api/produccion/recetas/{productoId}` - Ver receta de producto

### Pronóstico y Análisis
- `GET /api/ventas/historicas` - Datos históricos de ventas
- `POST /api/pronostico` - Generar pronóstico (admin)
- `GET /api/pronostico/{productoId}` - Pronósticos de un producto
- `GET /api/pronostico/errores/{id}` - Evaluación de precisión
- `POST /api/analisis/abc` - Ejecutar clasificación ABC (admin)
- `POST /api/analisis/eoq` - Calcular EOQ (admin)

### Alertas y Notificaciones
- `GET /api/alertas` - Listar alertas activas
- `PUT /api/alertas/{id}/marcar-leida` - Marcar alerta como leída
- `GET /api/alertas/dashboard` - Resumen para dashboard

### Reportes
- `GET /api/reportes/ventas` - Reporte de ventas
- `GET /api/reportes/inventario` - Reporte de inventario
- `GET /api/reportes/produccion` - Reporte de producción
- `GET /api/reportes/demanda` - Reporte de demanda
- `GET /api/reportes/costos` - Análisis de costos
- `POST /api/reportes/export` - Exportar reportes (PDF/Excel)

---

## 🛠️ Tecnologías Utilizadas

### Backend
- **Spring Boot 3.x** - Framework principal
- **Spring Security + JWT** - Autenticación y autorización
- **Spring Data JPA (Hibernate)** - ORM y persistencia
- **PostgreSQL 15+** - Base de datos relacional
- **Maven** - Gestión de dependencias
- **Swagger/OpenAPI 3** - Documentación de API
- **Jackson** - Serialización JSON
- **DTO y Patron Builder** - se usaran ambas formas en casos concretos de forma independiente los dto seran records independientes
- **JUnit 5** - Testing unitario

### Frontend (Independiente)
- **React 18+** / Angular 16+ / Vue 3+
- **TypeScript** - Tipado estático
- **Axios** - Cliente HTTP
- **Chart.js** - Gráficos y visualizaciones
- **Material-UI / Bootstrap** - Componentes UI

### DevOps y Herramientas
- **Git + GitHub** - Control de versiones
- **Docker** (opcional) - Containerización
- **Postman** - Testing de APIs
- **pgAdmin** - Administración de PostgreSQL

---

## 🧱 Arquitectura Modular

```
src/main/java/com/gomitas/
├── config/                 # Configuración (Security, CORS, etc.)
├── exception/             # Manejo global de excepciones
├── auth/                  # Autenticación y JWT
├── usuario/              # Gestión de usuarios
├── cliente/              # Gestión de clientes
├── producto/             # Productos y catálogo
├── pedido/               # Procesamiento de pedidos
├── inventario/           # Control de inventario
├── insumo/               # Gestión de insumos
├── produccion/           # Órdenes de producción
├── pronostico/           # Algoritmos de pronóstico
├── analisis/             # EOQ, ABC, métricas
├── alerta/               # Sistema de notificaciones
├── reporte/              # Generación de reportes
└── util/                 # Utilidades comunes
```

---

## 🔐 Seguridad

### Autenticación
- **JWT (JSON Web Tokens)** con expiración configurable
- **Refresh Tokens** para renovación automática
- **BCrypt** para hash de contraseñas (factor 12)

### Autorización
- **Roles diferenciados:** Cliente, Administrador
- **Filtros por endpoint:** acceso basado en rol
- **Validación de ownership:** clientes solo ven sus datos

### Protecciones Adicionales
- **Validación de entrada** en todos los endpoints
- **CORS configurado** para dominios específicos
- **Rate limiting** para prevenir abuso
- **Logs de auditoría** para trazabilidad

---

## ⚠️ Sistema de Alertas Automatizado

### Tipos de Alertas
1. **Stock Bajo:** Productos por debajo del stock mínimo
2. **Insumo Vencido:** Materias primas próximas a expirar
3. **Producción Pendiente:** Órdenes atrasadas o con problemas
4. **Demanda Alta:** Picos de demanda detectados por pronóstico

### Prioridades
- **Crítica:** Requiere acción inmediata (stock agotado)
- **Alta:** Acción dentro de 24h (stock muy bajo)
- **Media:** Atención en 3-5 días (vencimientos próximos)
- **Baja:** Información general (tendencias)

---

## 📊 Métodos de Pronóstico Implementados

### 1. Promedio Móvil Simple
- **Fórmula:** Promedio de las últimas n observaciones
- **Uso:** Demanda estable sin tendencia
- **Parámetros:** Períodos (3, 6, 12 meses)

### 2. Promedio Móvil Ponderado
- **Fórmula:** Promedio con pesos decrecientes hacia el pasado
- **Uso:** Mayor sensibilidad a cambios recientes
- **Parámetros:** Pesos por período

### 3. Suavizado Exponencial
- **Fórmula:** Ft+1 = α·At + (1-α)·Ft
- **Uso:** Series con cambios graduales
- **Parámetros:** Factor α (0.1-0.9)

### 4. Regresión Lineal (Futuro)
- **Fórmula:** y = mx + b
- **Uso:** Tendencias lineales claras
- **Parámetros:** Período de análisis

### Métricas de Evaluación
- **MAD:** Error Absoluto Medio
- **MSE:** Error Cuadrático Medio
- **MAPE:** Error Porcentual Absoluto Medio

---

## 🎯 EOQ y Clasificación ABC

### Economic Order Quantity (EOQ)
- **Fórmula:** EOQ = √(2·D·S/H)
  - D: Demanda anual
  - S: Costo por pedido
  - H: Costo de mantenimiento por unidad
- **Punto de reorden:** Considerando tiempo de entrega
- **Actualización:** Trimestral o por cambios significativos

### Clasificación ABC
- **Categoría A:** 80% del valor, 20% de productos (alta rotación)
- **Categoría B:** 15% del valor, 30% de productos (media rotación)
- **Categoría C:** 5% del valor, 50% de productos (baja rotación)
- **Criterio:** Valor anual de ventas por producto

---

## 📱 Dashboard y Reportes

### Dashboard Administrativo
- **Métricas en tiempo real:** Ventas hoy, stock crítico, producción activa
- **Gráficos:** Tendencias de ventas, rotación de inventario, precisión de pronósticos
- **Alertas prioritarias:** Notificaciones que requieren acción inmediata
- **KPIs:** Margen de ganancia, eficiencia de producción, satisfacción del cliente

### Reportes Disponibles
1. **Ventas:** Por período, producto, cliente
2. **Inventario:** Valorización, rotación, obsolescencia
3. **Producción:** Eficiencia, costos, desperdicios
4. **Financiero:** P&L, costos por producto, rentabilidad
5. **Pronóstico:** Precisión, tendencias, planificación

---

## 🚀 Despliegue del Sistema

### Arquitectura de Despliegue
```
┌─────────────────┐    ┌───────────────────┐    ┌─────────────────┐
│   Frontend      │    │     Backend       │    │   PostgreSQL    │
│   (React/Vue)   │◄──►│  (Spring Boot)    │◄──►│   Database      │
│   Puerto 3000   │    │   Puerto 8080     │    │   Puerto 5432   │
└─────────────────┘    └───────────────────┘    └─────────────────┘
```

### Servidor Local/Pago
- **Opción 1 - Local:** Servidor dedicado en empresa
  - Ventajas: Control total, sin costos recurrentes
  - Desventajas: Mantenimiento, backup manual
  
- **Opción 2 - Cloud:** AWS/Digital Ocean/Heroku
  - Ventajas: Backup automático, escalabilidad
  - Desventajas: Costo mensual, dependencia externa

### Configuración Recomendada
- **Java 17+** en el servidor
- **PostgreSQL 15+** con backup automático
- **Nginx** como proxy reverso (producción)
- **SSL/HTTPS** para seguridad
- **Monitoring** con logs centralizados

---

## 🔄 Flujo Operativo Completo

### 1. Ciclo de Vida del Producto
```
Planificación → Compra Insumos → Producción → Inventario → Venta → Análisis
```

### 2. Proceso de Pedidos
```
Cliente → Carrito → Pedido → Validación Stock → Producción (si necesario) → Entrega
```

### 3. Gestión de Inventario
```
Pronóstico → EOQ → Orden Producción → Inventario → Alertas → Reabastecimiento
```

### 4. Análisis y Mejora Continua
```
Datos Históricos → Pronóstico → Comparación Real vs Estimado → Ajuste Parámetros
```

---

## 📈 Métricas Clave del Sistema

### Operacionales
- **Nivel de servicio:** % pedidos entregados a tiempo
- **Rotación de inventario:** Ventas anuales / Inventario promedio
- **Precisión de pronóstico:** 100% - MAPE promedio
- **Eficiencia de producción:** Unidades producidas / Horas trabajadas

### Financieras
- **Margen bruto:** (Ventas - Costo de ventas) / Ventas
- **ROI de inventario:** Ganancia / Inversión en inventario
- **Costo por pedido:** Gastos operativos / Número de pedidos
- **Valor del inventario:** Stock valorizado por producto

### Calidad
- **Satisfacción del cliente:** Encuestas post-venta
- **Productos defectuosos:** % productos con problemas
- **Tiempo de ciclo:** Desde pedido hasta entrega
- **Disponibilidad del sistema:** % uptime del sistema

---

## 🎯 Roadmap de Implementación

### Fase 1 - Base (4-6 semanas)
- ✅ Configuración proyecto y base de datos
- ✅ Autenticación y gestión de usuarios
- ✅ CRUD de productos y clientes
- ✅ Sistema básico de pedidos

### Fase 2 - Operativo (4-5 semanas)
- ✅ Control de inventario con movimientos
- ✅ Gestión de insumos y proveedores
- ✅ Módulo de producción básico
- ✅ Alertas de stock

### Fase 3 - Inteligencia (4-6 semanas)
- ✅ Pronóstico de demanda (3 métodos)
- ✅ Análisis EOQ y ABC
- ✅ Dashboard con métricas
- ✅ Sistema de alertas avanzado

### Fase 4 - Reportes y Optimización (3-4 semanas)
- ✅ Generación de reportes
- ✅ Exportación PDF/Excel
- ✅ Optimización de consultas
- ✅ Testing y documentación final

### Futuras Mejoras
- 📱 App móvil para clientes
- 🤖 Machine Learning para pronósticos
- 📊 Business Intelligence avanzado
- 🔗 Integración con sistemas de terceros (facturación, logística)

---

## 💡 Ventajas Competitivas del Sistema

1. **Integración Completa:** Desde la materia prima hasta la venta final
2. **Pronóstico Inteligente:** Múltiples algoritmos con evaluación automática
3. **Gestión Proactiva:** Alertas que previenen problemas
4. **Análisis Financiero:** EOQ y ABC para optimización de costos
5. **Escalabilidad:** Arquitectura preparada para crecimiento
6. **Usabilidad:** Interfaz intuitiva para usuarios no técnicos

