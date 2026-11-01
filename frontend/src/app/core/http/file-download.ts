/**
 * Lee el nombre del archivo de una cabecera Content-Disposition, por ejemplo
 * `attachment; filename="citas_laura-martinez_2026-11-03.csv"`.
 */
export function fileNameFrom(contentDisposition: string | null, fallback: string): string {
  if (!contentDisposition) {
    return fallback;
  }
  const encoded = /filename\*=UTF-8''([^;]+)/i.exec(contentDisposition);
  if (encoded) {
    return decodeURIComponent(encoded[1]);
  }
  const plain = /filename="?([^";]+)"?/i.exec(contentDisposition);
  return plain ? plain[1] : fallback;
}

/**
 * Descarga un archivo que llegó por HttpClient. No basta con un enlace normal porque la petición
 * necesita el token en la cabecera Authorization.
 */
export function saveFile(content: Blob, fileName: string): void {
  const url = URL.createObjectURL(content);
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName;
  link.click();
  URL.revokeObjectURL(url);
}
