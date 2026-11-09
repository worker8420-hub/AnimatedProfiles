# Animated Profiles

Aplicación Android local para crear, importar, compartir y reproducir perfiles animados mediante archivos .wprofile.

## Funciones MVP
- Importar vídeo MP4 o GIF desde el almacenamiento del móvil.
- Exportar un perfil como archivo .wprofile y compartirlo mediante el selector de Android.
- Importar perfiles recibidos por archivo.
- Guardar perfiles localmente y reproducir MP4/GIF en bucle.
- Android 8.0 (API 26) o superior.

## Compilar
El workflow de GitHub Actions ejecuta tests y genera el APK debug en cada push a main. El APK se publica como artefacto descargable de la ejecución.

## Limitación importante
WhatsApp no ofrece una API pública para establecer un vídeo o GIF como avatar. La animación se reproduce dentro de Animated Profiles, no como foto de perfil animada nativa de WhatsApp.

Proyecto personal/MVP; probar en dispositivos reales antes de distribuir.
