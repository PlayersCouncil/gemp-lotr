package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.db.RtmdFeedbackDAO;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.util.JsonUtils;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.*;

public class RtmdFeedbackRequestHandler extends LotroServerRequestHandler implements UriRequestHandler {
    private final RtmdFeedbackDAO _feedbackDAO;
    private final LotroCardBlueprintLibrary _cardLibrary;
    private static final Logger _log = LogManager.getLogger(RtmdFeedbackRequestHandler.class);

    private static final Set<Integer> MODIFIER_SETS = Set.of(91, 92, 93, 94);
    private static final int MAX_IDEA_LENGTH = 250;
    private static final int MAX_SUBMISSIONS_PER_IP = 50;
    private static final double ELO_K = 32.0;
    private static final double ELO_DEFAULT = 1500.0;

    public RtmdFeedbackRequestHandler(Map<Type, Object> context) {
        super(context);
        _feedbackDAO = extractObject(context, RtmdFeedbackDAO.class);
        _cardLibrary = extractObject(context, LotroCardBlueprintLibrary.class);
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context,
                              ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.startsWith("/modifiers") && request.method() == HttpMethod.GET) {
            getModifiers(responseWriter);
        } else if (uri.startsWith("/matchup") && request.method() == HttpMethod.GET) {
            getMatchup(responseWriter);
        } else if (uri.startsWith("/vote") && !uri.startsWith("/voteIdea") && request.method() == HttpMethod.POST) {
            recordVote(request, responseWriter, remoteIp);
        } else if (uri.startsWith("/ratings") && request.method() == HttpMethod.GET) {
            getRatings(responseWriter);
        } else if (uri.startsWith("/submitIdea") && request.method() == HttpMethod.POST) {
            submitIdea(request, responseWriter, remoteIp);
        } else if (uri.startsWith("/ideas") && request.method() == HttpMethod.GET) {
            getIdeas(responseWriter);
        } else if (uri.startsWith("/voteIdea") && request.method() == HttpMethod.POST) {
            voteOnIdea(request, responseWriter, remoteIp);
        } else {
            throw new HttpProcessingException(404);
        }
    }

    // ---- Modifier listing (public, no auth) ----

    private void getModifiers(ResponseWriter responseWriter) throws Exception {
        var modifiers = new ArrayList<Map<String, Object>>();

        for (var entry : _cardLibrary.getBaseCards().entrySet()) {
            String blueprintId = entry.getKey();
            int setId;
            try {
                setId = Integer.parseInt(blueprintId.split("_")[0]);
            } catch (NumberFormatException e) {
                continue;
            }

            if (!MODIFIER_SETS.contains(setId)) continue;

            var bp = entry.getValue();
            var mod = new LinkedHashMap<String, Object>();
            mod.put("blueprintId", blueprintId);
            mod.put("title", bp.getFullName());
            mod.put("intensity", bp.getIntensity());
            mod.put("gameText", bp.getGameText());
            modifiers.add(mod);
        }

        var result = new LinkedHashMap<String, Object>();
        result.put("modifiers", modifiers);
        responseWriter.writeJsonResponse(JsonUtils.Serialize(result));
    }

    // ---- Matchup selection ----

    private List<String> getModifierBlueprintIds() {
        var ids = new ArrayList<String>();
        for (var entry : _cardLibrary.getBaseCards().entrySet()) {
            String blueprintId = entry.getKey();
            try {
                int setId = Integer.parseInt(blueprintId.split("_")[0]);
                if (MODIFIER_SETS.contains(setId)) {
                    ids.add(blueprintId);
                }
            } catch (NumberFormatException e) {
                // skip
            }
        }
        Collections.sort(ids);
        return ids;
    }

    private void getMatchup(ResponseWriter responseWriter) throws Exception {
        var ids = getModifierBlueprintIds();
        if (ids.size() < 2) {
            throw new HttpProcessingException(500);
        }

        var voteCounts = _feedbackDAO.getPairVoteCounts();

        // Build list of all pairs with their vote counts
        var zeroPairs = new ArrayList<String[]>();
        var allPairs = new ArrayList<Map.Entry<String[], Integer>>();

        for (int i = 0; i < ids.size(); i++) {
            for (int j = i + 1; j < ids.size(); j++) {
                String a = ids.get(i);
                String b = ids.get(j);
                String key = a + "|" + b;
                int count = voteCounts.getOrDefault(key, 0);
                if (count == 0) {
                    zeroPairs.add(new String[]{a, b});
                }
                allPairs.add(Map.entry(new String[]{a, b}, count));
            }
        }

        var rng = new Random();
        String[] selected;

        if (!zeroPairs.isEmpty()) {
            // Coverage phase: pick from unvoted pairs
            selected = zeroPairs.get(rng.nextInt(zeroPairs.size()));
        } else {
            // Refinement phase: bias toward close-rated pairs with fewer votes
            var votes = _feedbackDAO.getAllComparisonVotes();
            var ratings = computeEloRatings(votes);

            // Weight by (1/voteCount) * (1/(1 + eloDiff))
            double totalWeight = 0;
            var weights = new double[allPairs.size()];
            for (int i = 0; i < allPairs.size(); i++) {
                var pair = allPairs.get(i);
                String a = pair.getKey()[0];
                String b = pair.getKey()[1];
                double ratingA = ratings.getOrDefault(a, ELO_DEFAULT);
                double ratingB = ratings.getOrDefault(b, ELO_DEFAULT);
                double eloDiff = Math.abs(ratingA - ratingB);
                int count = Math.max(pair.getValue(), 1);
                double weight = (1.0 / count) * (1.0 / (1.0 + eloDiff / 100.0));
                weights[i] = weight;
                totalWeight += weight;
            }

            // Weighted random selection
            double r = rng.nextDouble() * totalWeight;
            double cumulative = 0;
            selected = allPairs.get(allPairs.size() - 1).getKey(); // fallback
            for (int i = 0; i < weights.length; i++) {
                cumulative += weights[i];
                if (cumulative >= r) {
                    selected = allPairs.get(i).getKey();
                    break;
                }
            }
        }

        // Randomize left/right presentation
        if (rng.nextBoolean()) {
            String tmp = selected[0];
            selected[0] = selected[1];
            selected[1] = tmp;
        }

        var result = new LinkedHashMap<String, Object>();
        result.put("blueprintA", selected[0]);
        result.put("blueprintB", selected[1]);
        responseWriter.writeJsonResponse(JsonUtils.Serialize(result));
    }

    // ---- Vote recording ----

    private void recordVote(HttpRequest request, ResponseWriter responseWriter, String remoteIp) throws Exception {
        var postDecoder = new HttpPostRequestDecoder(request);
        try {
            String blueprintA = getFormParameterSafely(postDecoder, "blueprintA");
            String blueprintB = getFormParameterSafely(postDecoder, "blueprintB");
            String winner = getFormParameterSafely(postDecoder, "winner");

            if (blueprintA == null || blueprintB == null || winner == null)
                throw new HttpProcessingException(400);

            // Validate winner is one of the blueprints or "tie"
            if (!winner.equals(blueprintA) && !winner.equals(blueprintB) && !winner.equals("tie"))
                throw new HttpProcessingException(400);

            _feedbackDAO.addComparisonVote(blueprintA, blueprintB, winner, remoteIp);
            responseWriter.sendJsonOK();
        } finally {
            postDecoder.destroy();
        }
    }

    // ---- Elo ratings ----

    private void getRatings(ResponseWriter responseWriter) throws Exception {
        var votes = _feedbackDAO.getAllComparisonVotes();
        var ratings = computeEloRatings(votes);

        // Count votes per modifier
        var voteCountMap = new HashMap<String, Integer>();
        for (var vote : votes) {
            voteCountMap.merge(vote.blueprint_a, 1, Integer::sum);
            voteCountMap.merge(vote.blueprint_b, 1, Integer::sum);
        }

        // Build response with titles
        var result = new ArrayList<Map<String, Object>>();
        for (var entry : ratings.entrySet()) {
            String bpId = entry.getKey();
            var bp = _cardLibrary.getLotroCardBlueprint(bpId);
            var item = new LinkedHashMap<String, Object>();
            item.put("blueprintId", bpId);
            item.put("title", bp != null ? bp.getFullName() : bpId);
            item.put("rating", Math.round(entry.getValue() * 10.0) / 10.0);
            item.put("officialIntensity", bp != null ? bp.getIntensity() : 0);
            item.put("voteCount", voteCountMap.getOrDefault(bpId, 0));
            result.add(item);
        }

        // Sort by rating descending
        result.sort((a, b) -> Double.compare((double) b.get("rating"), (double) a.get("rating")));

        responseWriter.writeJsonResponse(JsonUtils.Serialize(result));
    }

    static Map<String, Double> computeEloRatings(List<DBDefs.RtmdComparisonVote> votes) {
        Map<String, Double> ratings = new LinkedHashMap<>();

        // Initialize all participants
        for (var vote : votes) {
            ratings.putIfAbsent(vote.blueprint_a, ELO_DEFAULT);
            ratings.putIfAbsent(vote.blueprint_b, ELO_DEFAULT);
        }

        // Process chronologically
        for (var vote : votes) {
            double rA = ratings.get(vote.blueprint_a);
            double rB = ratings.get(vote.blueprint_b);
            double eA = 1.0 / (1.0 + Math.pow(10, (rB - rA) / 400.0));
            double eB = 1.0 - eA;

            double sA, sB;
            if ("tie".equals(vote.winner)) {
                sA = 0.5;
                sB = 0.5;
            } else if (vote.winner.equals(vote.blueprint_a)) {
                sA = 1.0;
                sB = 0.0;
            } else {
                sA = 0.0;
                sB = 1.0;
            }

            ratings.put(vote.blueprint_a, rA + ELO_K * (sA - eA));
            ratings.put(vote.blueprint_b, rB + ELO_K * (sB - eB));
        }

        return ratings;
    }

    // ---- Idea submissions ----

    private void submitIdea(HttpRequest request, ResponseWriter responseWriter, String remoteIp) throws Exception {
        var postDecoder = new HttpPostRequestDecoder(request);
        try {
            String ideaText = getFormParameterSafely(postDecoder, "ideaText");
            if (ideaText == null || ideaText.isBlank())
                throw new HttpProcessingException(400);

            // Sanitize: trim, enforce length limit, strip HTML tags
            ideaText = ideaText.trim();
            if (ideaText.length() > MAX_IDEA_LENGTH)
                ideaText = ideaText.substring(0, MAX_IDEA_LENGTH);
            ideaText = ideaText.replaceAll("<[^>]*>", "");

            // Rate limit
            int existingCount = _feedbackDAO.getSubmissionCountForIp(remoteIp);
            if (existingCount >= MAX_SUBMISSIONS_PER_IP)
                throw new HttpProcessingException(429);

            int id = _feedbackDAO.addIdeaSubmission(ideaText, remoteIp);

            var result = new LinkedHashMap<String, Object>();
            result.put("id", id);
            responseWriter.writeJsonResponse(JsonUtils.Serialize(result));
        } finally {
            postDecoder.destroy();
        }
    }

    private void getIdeas(ResponseWriter responseWriter) throws Exception {
        var ideas = _feedbackDAO.getRandomSubmissions(10);

        var result = new ArrayList<Map<String, Object>>();
        for (var idea : ideas) {
            var item = new LinkedHashMap<String, Object>();
            item.put("id", idea.id);
            item.put("ideaText", idea.idea_text);
            item.put("upvotes", idea.upvotes);
            item.put("downvotes", idea.downvotes);
            result.add(item);
        }

        responseWriter.writeJsonResponse(JsonUtils.Serialize(result));
    }

    private void voteOnIdea(HttpRequest request, ResponseWriter responseWriter, String remoteIp) throws Exception {
        var postDecoder = new HttpPostRequestDecoder(request);
        try {
            String submissionIdStr = getFormParameterSafely(postDecoder, "submissionId");
            String voteStr = getFormParameterSafely(postDecoder, "vote");
            if (submissionIdStr == null || voteStr == null)
                throw new HttpProcessingException(400);

            int submissionId;
            int vote;
            try {
                submissionId = Integer.parseInt(submissionIdStr);
                vote = Integer.parseInt(voteStr);
            } catch (NumberFormatException e) {
                throw new HttpProcessingException(400);
            }

            if (vote != 1 && vote != -1)
                throw new HttpProcessingException(400);

            boolean success = _feedbackDAO.voteOnIdea(submissionId, remoteIp, vote);
            if (!success)
                throw new HttpProcessingException(409);

            responseWriter.sendJsonOK();
        } finally {
            postDecoder.destroy();
        }
    }
}
