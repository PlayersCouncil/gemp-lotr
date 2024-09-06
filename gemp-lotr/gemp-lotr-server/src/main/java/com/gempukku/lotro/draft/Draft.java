package com.gempukku.lotro.draft;

import com.gempukku.lotro.SubscriptionConflictException;
import com.gempukku.lotro.SubscriptionExpiredException;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.tournament.TournamentCallback;

public interface Draft {
    public void advanceDraft();

    public void playerChosenCard(String playerName, String cardId);

    public void signUpForDraft(String playerName, DraftChannelVisitor draftChannelVisitor);

    public DraftCommunicationChannel getCommunicationChannel(String playerName, int channelNumber)  throws SubscriptionExpiredException, SubscriptionConflictException;

    public DraftCardChoice getCardChoice(String playerName);
    public CardCollection getChosenCards(String player);

    public boolean isFinished();
}
