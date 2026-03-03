# Distributed Sales System with SMS Authentication
----------------------------------------------------------
# Sistema Distribuido de Ventas con Autenticación SMS


## Arquitectura del Sistema
El proyecto implementa una arquitectura distribuida híbrida:
* **Aplicación Cliente (Java):** Registra detalles de ventas en una BD MySQL local.
* **Aplicación Servidor (Python/Flask):** Genera vouchers, gestiona cabeceras de ventas en una BD en la nube y maneja la autenticación.
* **Autenticación:** Sistema de validación de tokens de 6 dígitos enviados por SMS.

## Tecnologías Utilizadas
* **Backend:** Python 3, Flask
* **Frontend/Cliente:** Java, Apache HttpClient
* **Base de Datos:** MySQL (Local y Cloud)

## Arquitectura
<img width="861" height="522" alt="image" src="https://github.com/user-attachments/assets/6bae2197-cfa5-4cd1-96bf-0115722140bc" />


## Flujo de datos
1. El cliente inicia la venta desde la aplicación local.
2. La aplicación cliente solicita al servidor la generación del voucher.
3. El servidor registra la cabecera en la base de datos en la nube.
4. El servidor genera un token y lo envía al cliente vía SMS.
5. El cliente recibe el token y lo ingresa en la aplicación.
6. La aplicación cliente valida el token con el servidor.
7. Si es válido, se registra el detalle de la venta en la base de datos local.

<img width="678" height="221" alt="image" src="https://github.com/user-attachments/assets/b1f9e614-41a3-4303-b590-a29061cf59b1" />
<img width="204" height="169" alt="image" src="https://github.com/user-attachments/assets/22a4f7d4-e7b1-4d66-8eda-44d40d102c0d" />
<img width="583" height="634" alt="image" src="https://github.com/user-attachments/assets/c8915e41-0d3d-4921-a630-ec6f18b7847e" />
<img width="590" height="493" alt="image" src="https://github.com/user-attachments/assets/cca17963-c883-46de-bbf7-cc4755889f00" />
