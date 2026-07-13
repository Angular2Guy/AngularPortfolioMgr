package ch.xxx.manager.common.client;

import ch.xxx.manager.common.dto.OwnershipDocumentDto;
import ch.xxx.manager.common.dto.SecSubmissionDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.dataformat.xml.XmlMapper;

import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


@Component
public class SecFormConnector {
    private static final String USER_AGENT = "Sven Smith Privat (sven@gmx.de)";
    private final XmlMapper xmlMapper;
    private final RestClient restClient;
    private final Semaphore secRateLimiter = new Semaphore(5, true);

    public SecFormConnector(XmlMapper xmlMapper) {
        this.xmlMapper = xmlMapper;
        // RestClient mit globalem User-Agent konfigurieren
        this.restClient = RestClient.builder()
                .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
                .defaultHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate")
                .build();
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            int releaseCount = 5 - secRateLimiter.availablePermits();
            if (releaseCount > 0) {
                secRateLimiter.release(releaseCount);
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    public OwnershipDocumentDto downloadLatestForm4Xml(String cik10Digit) {
        String submissionsUrl = "https://sec.gov" + cik10Digit + ".json";

        this.acquireToken();
        // 1. JSON-Metadaten abrufen und automatisch in DTO mappen
        SecSubmissionDto submissions = restClient.get()
                .uri(submissionsUrl)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new RuntimeException("SEC API Fehler: " + res.getStatusCode());
                })
                .body(SecSubmissionDto.class);

        if (submissions == null || submissions.getFilings() == null || submissions.getFilings().getRecent() == null) {
            throw new IllegalStateException("Keine Daten von der SEC erhalten.");
        }

        SecSubmissionDto.RecentDto recent = submissions.getFilings().getRecent();
        int form4Index = -1;

        // 2. Den Index des neuesten "Form 4"-Eintrags finden
        for (int i = 0; i < recent.getForm().size(); i++) {
            if ("4".equals(recent.getForm().get(i))) {
                form4Index = i;
                break;
            }
        }

        if (form4Index == -1) {
            throw new IllegalArgumentException("Keine Form 4 Einreichung für dieses Unternehmen gefunden.");
        }

        // Daten für die URL extrahieren
        String rawAccession = recent.getAccessionNumber().get(form4Index);
        String cleanAccession = rawAccession.replace("-", "");
        String xmlDocumentName = recent.getPrimaryDocument().get(form4Index);

        // CIK für den Archiv-Pfad ohne führende Nullen bereinigen
        String archiveCik = String.valueOf(Long.parseLong(cik10Digit));

        // 3. SEC-Archiv URL zusammenbauen
        String xmlUrl = String.format("https://sec.gov",
                archiveCik, cleanAccession, xmlDocumentName);

        System.out.println("Lade XML herunter von: " + xmlUrl);

        this.acquireToken();
        // 4. XML-Datei als Byte-Array herunterladen
        var xmlData = restClient.get()
                .uri(xmlUrl)
                .retrieve()
                .body(String.class);
        var result = xmlMapper.readValue(xmlData, OwnershipDocumentDto.class);
        return result;
    }

    private void acquireToken() {
        try {
            secRateLimiter.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Das Rate-Limiting wurde unerwartet unterbrochen", e);
        }
    }
}
