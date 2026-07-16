package hestia.prometheus.silences;

import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.reflect.TypeToken;

import github.soltaufintel.amalia.rest.REST;
import hestia.prometheus.silences.Silence.Matcher;
import hestia.prometheus.silences.Silence.SilenceCreated;

/**
 * Service for loading, creating and deleting Prometheus Alertmanager silences
 */
public class PrometheusSilencesService {
    private static final String ENDPOINT1 = "/api/v2/silence/";
    private static final String ENDPOINT2 = "/api/v2/silences";
    private final String alertmanagerHost;

    /**
     * @param alertmanagerHost e.g. "http://server:9093"
     */
    public PrometheusSilencesService(String alertmanagerHost) {
        this.alertmanagerHost = alertmanagerHost;
    }

    /**
     * @return all silences
     */
    public List<Silence> getSilences() {
        var rest = new REST(alertmanagerHost + ENDPOINT2).get();
        var json = rest.response();
        rest.close();
        Type silencesType = new TypeToken<List<Silence>>() {}.getType();
        return Silence.gson().fromJson(json, silencesType);
    }

    /**
     * @return not expired silences
     */
    public List<Silence> getActiveSilences() {
        return getSilences().stream()
                .filter(s -> !"expired".equals(s.status()))
                .collect(Collectors.toList());
    }

    /**
     * @param title must not be empty
     * @param comment must not be empty
     * @param hours -
     * @param matchers -
     * @return Silence ID
     */
    public String createSilence(String title, String comment, long hours, List<Matcher> matchers) {
        return createSilence(title, comment, null, OffsetDateTime.now().plusHours(hours), matchers);
    }

    /**
     * @param title must not be empty
     * @param comment must not be empty
     * @param startsAt -
     * @param endsAt -
     * @param matchers -
     * @return Silence ID
     */
    public String createSilence(String title, String comment, OffsetDateTime startsAt, OffsetDateTime endsAt,
            List<Matcher> matchers) {
        Silence silence = new Silence();
        silence.setCreatedBy(title);
        silence.setComment(comment);
        silence.setStartsAt(startsAt == null ? OffsetDateTime.now() : startsAt);
        silence.setEndsAt(endsAt);
        silence.setMatchers(matchers);
        return createSilence(silence);
    }

    /**
     * @param silence -
     * @return Silence ID
     */
    public String createSilence(Silence silence) {
        var json = Silence.gson().toJson(silence);
        return new REST(alertmanagerHost + ENDPOINT2).post(json, REST.json_cp1252())
                .fromJson(SilenceCreated.class).getSilenceID();
    }

    /**
     * Delete silence
     * @param id silence ID
     */
    public void expireSilence(String id) {
        new REST(alertmanagerHost + ENDPOINT1 + id).delete().close();
    }
}
