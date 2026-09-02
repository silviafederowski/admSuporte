package br.com.silsys.admsuporte.dao;

import br.com.silsys.admsuporte.model.DriveArquivo;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * Le a pasta "ParaWeb" do Google Drive via API key (pasta publica, "qualquer pessoa com o
 * link"). Sem OAuth/senha: a chave so tem permissao de leitura na Drive API, restrita no
 * proprio Google Cloud Console.
 *
 * Usa um truststore proprio, embutido no WAR (ver TRUSTSTORE_RESOURCE), em vez do cacerts
 * padrao da JVM do servidor: nessa hospedagem o Java instalado tem um cacerts desatualizado/
 * incompleto, o que causava SSLHandshakeException ("unable to find valid certification path")
 * ao chamar googleapis.com. O truststore bundlado e o cacerts padrao de um JDK atual.
 */
public class GoogleDriveClient {

    private static final String API_KEY = "AIzaSyAeMfLT14tVCaT2-Lk98taw9qpk15yLXFM";
    private static final String FOLDER_ID = "1-4diQ3c8sjymIpn6u24ZZeNMQ8baBEnl";
    private static final String TRUSTSTORE_RESOURCE = "/truststore/cacerts.jks";
    private static final char[] TRUSTSTORE_PASSWORD = "changeit".toCharArray();

    private static final SSLSocketFactory TRUSTED_SOCKET_FACTORY = buildTrustedSocketFactory();

    public List<DriveArquivo> listarArquivosDaPasta() throws IOException {
        String query = "'" + FOLDER_ID + "' in parents and trashed = false "
                + "and mimeType != 'application/vnd.google-apps.folder'";
        String url = "https://www.googleapis.com/drive/v3/files"
                + "?q=" + URLEncoder.encode(query, "UTF-8")
                + "&fields=" + URLEncoder.encode("files(id,name,mimeType)", "UTF-8")
                + "&orderBy=" + URLEncoder.encode("name", "UTF-8")
                + "&pageSize=1000"
                + "&key=" + API_KEY;

        String body = fetch(url);
        List<DriveArquivo> result = new ArrayList<>();
        try (JsonReader reader = Json.createReader(new java.io.StringReader(body))) {
            JsonObject root = reader.readObject();
            JsonArray files = root.getJsonArray("files");
            if (files != null) {
                for (int i = 0; i < files.size(); i++) {
                    JsonObject item = files.getJsonObject(i);
                    DriveArquivo arquivo = new DriveArquivo();
                    arquivo.setId(item.getString("id"));
                    arquivo.setNome(item.getString("name"));
                    arquivo.setMimeType(item.getString("mimeType", null));
                    result.add(arquivo);
                }
            }
        }
        return result;
    }

    private String fetch(String urlString) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
        if (conn instanceof HttpsURLConnection && TRUSTED_SOCKET_FACTORY != null) {
            ((HttpsURLConnection) conn).setSSLSocketFactory(TRUSTED_SOCKET_FACTORY);
        }
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        int status = conn.getResponseCode();
        try (InputStream in = status >= 200 && status < 300 ? conn.getInputStream() : conn.getErrorStream()) {
            String body = readAll(in);
            if (status < 200 || status >= 300) {
                throw new IOException("Google Drive API retornou HTTP " + status + ": " + body);
            }
            return body;
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Monta uma SSLSocketFactory que confia no truststore bundlado no WAR, para nao depender
     * do cacerts (possivelmente desatualizado) da JVM do servidor. Se o resource nao puder ser
     * lido por algum motivo, retorna null e a conexao cai de volta no comportamento padrao da JVM.
     */
    private static SSLSocketFactory buildTrustedSocketFactory() {
        try (InputStream in = GoogleDriveClient.class.getResourceAsStream(TRUSTSTORE_RESOURCE)) {
            if (in == null) {
                return null;
            }
            KeyStore trustStore = KeyStore.getInstance("JKS");
            trustStore.load(in, TRUSTSTORE_PASSWORD);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            return sslContext.getSocketFactory();
        } catch (IOException | GeneralSecurityException e) {
            return null;
        }
    }

    private String readAll(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[4096];
        int read;
        while ((read = in.read(chunk)) != -1) {
            buffer.write(chunk, 0, read);
        }
        return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
    }
}
