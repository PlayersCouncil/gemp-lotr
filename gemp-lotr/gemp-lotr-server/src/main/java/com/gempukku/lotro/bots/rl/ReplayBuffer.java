package com.gempukku.lotro.bots.rl;

import java.util.*;

public class ReplayBuffer {
    private final List<LearningStep> buffer = new ArrayList<>();
    private final int capacity;
    private ReplayBufferListener listener;
    private int listenerThreshold;

    public ReplayBuffer(int capacity) {
        this.capacity = capacity;
    }

    public void addEpisode(List<LearningStep> episode) {
        if (episode == null || episode.isEmpty())
            return;

        for (LearningStep step : episode) {
            if (buffer.size() >= capacity) {
                buffer.remove(0); // simple FIFO
            }
            buffer.add(step);
        }

        // Notify listeners if threshold is met or exceeded
        if (listener != null && buffer.size() > listenerThreshold) {
            listener.bufferReady(this);
        }
    }

    public void addListener(int threshold, ReplayBufferListener listener) {
        this.listener = listener;
        this.listenerThreshold = threshold;
    }

    public List<LearningStep> sampleBatch(int batchSize) {
        List<LearningStep> batch = new ArrayList<>();
        if (buffer.isEmpty()) return batch;

        Random rand = new Random();
        for (int i = 0; i < batchSize; i++) {
            batch.add(buffer.get(rand.nextInt(buffer.size())));
        }
        return batch;
    }

    public int size() {
        return buffer.size();
    }

    public void clear() {
        buffer.clear();
    }
}
