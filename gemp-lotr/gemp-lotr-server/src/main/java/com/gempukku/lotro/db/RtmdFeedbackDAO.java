package com.gempukku.lotro.db;

import com.gempukku.lotro.common.DBDefs;

import java.util.List;
import java.util.Map;

public interface RtmdFeedbackDAO {
    // --- Comparison votes ---
    void addComparisonVote(String blueprintA, String blueprintB, String winner, String ipAddress);

    /** Returns vote counts per normalized pair. Key format: "blueprintA|blueprintB" */
    Map<String, Integer> getPairVoteCounts();

    /** Returns all comparison votes ordered chronologically (for Elo computation). */
    List<DBDefs.RtmdComparisonVote> getAllComparisonVotes();

    // --- Idea submissions ---
    int addIdeaSubmission(String ideaText, String ipAddress);

    /** Returns a random sample of submissions. */
    List<DBDefs.RtmdIdeaSubmission> getRandomSubmissions(int count);

    /** Vote on an idea submission. Returns false if this IP already voted on this submission. */
    boolean voteOnIdea(int submissionId, String ipAddress, int vote);

    /** Returns the number of submissions from a given IP (for rate limiting). */
    int getSubmissionCountForIp(String ipAddress);
}
