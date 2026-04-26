# Nexora - Red Social Universitaria (Backend Core)

Nexora es una red social universitaria para estudiantes de la UTP, inspirada en la experiencia de Twitter. Este repositorio contiene el backend core de la aplicación.

## 🚀 Descripción

El backend de Nexora gestiona la autenticación institucional, la persistencia de perfiles y la orquestación de datos mediante una arquitectura robusta en Spring Boot.

## 🛠️ Tecnologías

- **Lenguaje:** Java 21
- **Framework:** Spring Boot 3.x+
- **Seguridad:** Spring Security + JWT + Autenticación Supabase
- **Persistencia:** Spring Data JPA + PostgreSQL
- **API:** REST & GraphQL (Spring GraphQL)
- **Testing:** JUnit 5 + Mockito

## ⚙️ Reglas de Negocio Críticas

### Perfil Completo
Un perfil de usuario se considera "Completo" (`isComplete`) únicamente si cumple con los siguientes requisitos:
1. Nombre de Usuario (`username`) definido.
2. Nombre Completo (`fullName`) definido.
3. Carrera Académica seleccionada.
4. Al menos un Interés Académico registrado.

*Referencia: `AuthService.java -> isProfileComplete`*

### Normalización de Identidad
Debido a las reglas institucionales de la UTP:
- Los correos `@utp.edu.pe` deben ser tratados con la parte local (ID de estudiante/docente) en **MAYÚSCULAS** para garantizar la compatibilidad con los sistemas de autenticación.

---

## 🏗️ Estructura del Proyecto

```text
src/main/java/com/nexora/core
  auth/      - Lógica de autenticación y registro
  common/    - Entidades y utilidades base
  graphql/   - Resolvers y esquemas
  security/  - Configuración de filtros y JWT
  user/      - Gestión de perfiles y relaciones
```

---

## 🔌 Variables de Entorno

Configura estas variables en tu archivo `.env` o plataforma de despliegue:

```dotenv
DB_URL=jdbc:postgresql://localhost:5432/nexora
DB_USERNAME=postgres
DB_PASSWORD=postgres
JWT_SECRET=[Tu-Secret-Key]
JWT_REFRESH_SECRET=[Tu-Refresh-Key]
SUPABASE_URL=[URL-Proyecto]
SUPABASE_KEY=[Anon-Key]
```

---

## 🚀 Instalación y Ejecución

```powershell
./mvnw.cmd clean spring-boot:run
```

La API inicia por defecto en `http://localhost:8080`.

---

## 🧪 Testing

```powershell
./mvnw.cmd test
```

---

## 👥 Equipo de Desarrollo

- @Crismar12
- @kath144 (Katherine Salas)
- @KennySth

---

## 📄 Licencia

Proyecto académico para la UTP. Uso educativo.
