package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;
import com.gempukku.lotro.logic.timing.Action;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ModifiersQuerying {
    LimitCounter getUntilEndOfPhaseLimitCounter(PhysicalCard card, Phase phase);

    LimitCounter getUntilEndOfPhaseLimitCounter(PhysicalCard card, String prefix, Phase phase);

    LimitCounter getUntilStartOfPhaseLimitCounter(PhysicalCard card, Phase phase);

    LimitCounter getUntilStartOfPhaseLimitCounter(PhysicalCard card, String prefix, Phase phase);

    LimitCounter getUntilEndOfTurnLimitCounter(PhysicalCard card);

    LimitCounter getUntilEndOfTurnLimitCounter(PhysicalCard card, String prefix);

    /**
     * Counts the uses of a card at the site the fellowship is at: "limit once per site".  The count is cleared
     * when the fellowship moves to another site and at the end of the turn, so it is once for each site in a turn
     * (Ketura on Q-0084 and F-0146).
     */
    LimitCounter getSiteLimitCounter(PhysicalCard card);

    LimitCounter getSiteLimitCounter(PhysicalCard card, String prefix);

    Collection<Modifier> getModifiersAffecting(LotroGame game, PhysicalCard card);

    Evaluator getFPStrengthOverrideEvaluator(LotroGame game, PhysicalCard fpCharacter);

    Evaluator getShadowStrengthOverrideEvaluator(LotroGame game, PhysicalCard fpCharacter);

    boolean hasTextRemoved(LotroGame game, PhysicalCard card);

    // Keywords
    boolean hasKeyword(LotroGame game, PhysicalCard physicalCard, Keyword keyword);

    int getKeywordCount(LotroGame game, PhysicalCard physicalCard, Keyword keyword);

    boolean hasSignet(LotroGame game, PhysicalCard physicalCard, Signet signet);

    // Archery
    int getArcheryTotal(LotroGame game, Side side, int baseArcheryTotal);

    boolean addsToArcheryTotal(LotroGame game, PhysicalCard card);

    // Movement
    int getMoveLimit(LotroGame game, int baseMoveLimit);

    /**
     * @return How many cards the given player's hand holds: the number they draw at the start of the game and
     * reconcile to at the end of each turn.  This is the format's hand size unless something has changed it.
     */
    int getHandSize(LotroGame game, String playerId);

    int getThreatLimit(LotroGame game, int baseThreatLimit);

    boolean addsTwilightForCompanionMove(LotroGame game, PhysicalCard companion);

    // Twilight cost
    int getTwilightCostToPlay(LotroGame game, PhysicalCard physicalCard, PhysicalCard target, int twilightCostModifier, boolean ignoreRoamingPenalty);
    /**
     * The twilight cost of a card that is not being played.  For a card in play that is the number printed on it:
     * nothing another card does to a twilight cost reaches a card that is not coming into play (CR 4.2 "twilight
     * cost"), and the card's own text about its cost is read the same way (Ketura, 2026-10-06, on Q-0073).  For a
     * card anywhere else it is what the card would cost to play, the roaming penalty apart.  A site answers with
     * its Shadow number as it stands, which is not a twilight cost and is changed where the site lies.
     */
    int getCurrentTwilightCost(LotroGame game, PhysicalCard physicalCard);

    /**
     * What it costs to transfer a card.  A transfer pays the card's twilight cost again and is not a play (CR 4.2
     * "transfer"), so the card is in play and not coming into play: it costs the number printed on it, whatever
     * another card or its own text says about its twilight cost (CR 4.2 "twilight cost"; Ketura, 2026-10-06, on
     * Q-0053 and Q-0074).
     */
    int getTwilightCostToTransfer(LotroGame game, PhysicalCard physicalCard);

    int getRoamingPenalty(LotroGame game, PhysicalCard physicalCard);

    // Stats
    int getStrength(LotroGame game, PhysicalCard physicalCard);

    boolean appliesStrengthBonusModifier(LotroGame game, PhysicalCard modifierSource, PhysicalCard modifierTarget);

    int getVitality(LotroGame game, PhysicalCard physicalCard);

    int getResistance(LotroGame game, PhysicalCard physicalCard);

    int getMinionSiteNumber(LotroGame game, PhysicalCard physicalCard);

    int getOverwhelmMultiplier(LotroGame game, PhysicalCard card);

    List<CardType> getCardTypes(LotroGame game, PhysicalCard card);
    boolean isCardType(LotroGame game, PhysicalCard card, CardType cardType);
    List<Race> getRaces(LotroGame game, PhysicalCard card);
    boolean isRace(LotroGame game, PhysicalCard card, Race race);
    List<Culture> getCultures(LotroGame game, PhysicalCard card);
    boolean isCulture(LotroGame game, PhysicalCard card, Culture culture);

    // Wounds/exertions
    boolean canTakeWounds(LotroGame game, Collection<PhysicalCard> woundSources, PhysicalCard card, int woundsToTake);

    boolean canTakeWoundsFromLosingSkirmish(LotroGame game, PhysicalCard card);

    boolean canTakeArcheryWound(LotroGame game, PhysicalCard card);

    boolean canBeExerted(LotroGame game, PhysicalCard exertionSource, PhysicalCard exertedCard);

    /**
     * Whether performingPlayer may heal a wound from the card: the card is not hindered and no bar on healing
     * that reaches it stops this player.  The player is whoever the heal is made by (HealResult's performing
     * player); a bar that names a player (CantHealModifier with a banned player) stops only that player's heals.
     */
    boolean canBeHealed(LotroGame game, String performingPlayer, PhysicalCard card);

    boolean canAddBurden(LotroGame game, String performingPlayer, PhysicalCard source);

    boolean canRemoveBurden(LotroGame game, PhysicalCard source);

    boolean canRemoveThreat(LotroGame game, PhysicalCard source);

    // Assignments
    boolean canBeAssignedToSkirmish(LotroGame game, Side playerSide, PhysicalCard card);

    boolean canCancelSkirmish(LotroGame game, PhysicalCard card);

    boolean isUnhastyCompanionAllowedToParticipateInSkirmishes(LotroGame game, PhysicalCard card);

    /**
     * Whether a card has this ally participate in archery fire (be counted for the fellowship archery total, be
     * assigned archery wounds) where the rules alone would not: away from home.  A card that says he may not
     * participate beats every card that says he may ("can't" beats "can"), so this is false of an ally of whom
     * isAllyPreventedFromParticipatingInArcheryFire is true, whatever allows him.
     */
    boolean isAllyAllowedToParticipateInArcheryFire(LotroGame game, PhysicalCard card);

    /**
     * Whether a card has this ally participate in skirmishes (be assigned to a minion by that side's player, and
     * fight) where the rules alone would not: away from home.  False of an ally of whom
     * isAllyPreventedFromParticipatingInSkirmishes is true, whatever allows him.
     */
    boolean isAllyAllowedToParticipateInSkirmishes(LotroGame game, Side sidePlayer, PhysicalCard card);

    /** Whether a card says this ally may not participate in skirmishes, at home or anywhere else. */
    boolean isAllyPreventedFromParticipatingInSkirmishes(LotroGame game, Side sidePlayer, PhysicalCard card);

    /** Whether a card says this ally may not participate in archery fire, at home or anywhere else. */
    boolean isAllyPreventedFromParticipatingInArcheryFire(LotroGame game, PhysicalCard card);

    boolean isValidAssignments(LotroGame game, Side side, Map<PhysicalCard, Set<PhysicalCard>> assignments);

    // Playing actions
    boolean canPlayAction(LotroGame game, String performingPlayer, Action action);

    boolean canPlayCard(LotroGame game, String performingPlayer, PhysicalCard card);

    boolean canHavePlayedOn(LotroGame game, PhysicalCard playedCard, PhysicalCard target);

    boolean canHaveTransferredOn(LotroGame game, PhysicalCard playedCard, PhysicalCard target);

    boolean canBeTransferred(LotroGame game, PhysicalCard attachment);
    int bearableItemsOfClass(LotroGame game, PhysicalCard attachedTo, PossessionClass itemClass);

    boolean shouldSkipPhase(LotroGame game, Phase phase, String playerId);

    List<? extends Action> getExtraPhaseActions(LotroGame game, PhysicalCard target);
    Collection<PhysicalCard> getGameTextCardsToDuplicate(LotroGame game, PhysicalCard target);

    boolean hasExtraPhaseActionsFromOtherPlayer(LotroGame game, PhysicalCard target);

    List<? extends Action> getExtraPhaseActionsFromStacked(LotroGame game, PhysicalCard target);

    boolean canPayExtraCostsToPlay(LotroGame game, PhysicalCard target);

    void appendExtraCosts(LotroGame game, CostToEffectAction action, PhysicalCard target);

    // Others
    boolean canBeDiscardedFromPlay(LotroGame game, String performingPlayer, PhysicalCard card, PhysicalCard source);
    boolean canBeHinderedBy(LotroGame game, String performingPlayer, PhysicalCard card, PhysicalCard source);

    boolean canBeReturnedToHand(LotroGame game, PhysicalCard card, PhysicalCard source);

    boolean canDrawCardNoIncrement(LotroGame game, String playerId);

    boolean canDrawCardAndIncrementForRuleOfFour(LotroGame game, String playerId);
    int getFellowshipDrawnCards(LotroGame game);

    boolean canLookOrRevealCardsInHand(LotroGame game, String revealingPlayerId, String performingPlayerId);

    boolean canDiscardCardsFromHand(LotroGame game, String playerId, PhysicalCard source);

    boolean canDiscardCardsFromTopOfDeck(LotroGame game, String playerId, PhysicalCard source);

    boolean canBeLiberated(LotroGame game, String playerId, PhysicalCard card, PhysicalCard source);

    Side hasInitiative(LotroGame game);

    /**
     * Which side has initiative as a requirement of the given card reads it.  As hasInitiative, except that a
     * card the Free Peoples player is playing from his hand is not counted in that hand: while it lies there,
     * and while it lies in the void from hand with its costs being paid (CR 4.2 "playing a card": "You cannot
     * count The Card for initiative. ... If you are the Free Peoples player and initiative is a requirement for
     * playing The Card, you must have four cards in your hand not counting The Card"; F-0464).  For any other
     * card, a card in play or in another player's hand, the answer is hasInitiative's.  The HaveInitiative
     * requirement is its one caller; whether a side has lost initiative is still read by hasInitiative, which
     * counts the card until its play is announced.
     */
    Side hasInitiativeNotCounting(LotroGame game, PhysicalCard cardAsking);

    int getNumberOfSpottableFPCultures(LotroGame game, String playerId);

    int getNumberOfSpottableShadowCultures(LotroGame game, String playerId);

    int getNumberOfSpottableControlledSites(LotroGame game, String playerId);

    /**
     * What the modifiers in force add to the spotting player's count of cards matching this one filter (Merry, In
     * the Bloom of Health 17_107: "add 1 to the number of Ents you can spot"). Null for a count that is no player's,
     * which gets no bonus (F-0502).
     */
    int getSpotBonus(LotroGame game, Filterable filter, String spottingPlayer);

    boolean hasFlagActive(LotroGame game, ModifierFlag modifierFlag);

    /**
     * As {@link #hasFlagActive(LotroGame, ModifierFlag)}, but also answers true for flags that were declared for this
     * one player (the {@code player} field on an {@code AddModifierFlag} modifier, e.g. the Race to Mount Doom
     * meta-site modifiers whose text says "you"/"your").  A flag declared without a player is global and is reported
     * for every player; a flag declared with a player is deliberately invisible to the player-agnostic query above.
     */
    boolean hasFlagActive(LotroGame game, ModifierFlag modifierFlag, String playerId);

    boolean canReplaceSite(LotroGame game, String playerId, PhysicalCard siteToReplace);

    boolean canPlaySite(LotroGame game, String playerId);

    /**
     * What the player must pay to assign the minion to skirmish the character, as an action whose costs are that
     * payment (ExtraAssignmentCostModifier: "the Free Peoples player must exert a companion to assign this minion
     * to a skirmish"), or null where the assignment costs nothing: nobody charges for this pair, or the player is
     * not the Free Peoples player.  Asked by a card's assignment once the pair is known and before the
     * assignment is made: the caller stacks the action, and a payment that was not carried out in full
     * (wasCarriedOut) means that one assignment is not made (Q-0221).  The Free Peoples player's own assigning
     * pays ahead instead (appendAssignmentCosts; Q-0283).
     */
    CostToEffectAction getAssignmentCosts(LotroGame game, String assigningPlayer, PhysicalCard fpCharacter, PhysicalCard minion);

    /**
     * Appends to the action, as costs, every cost of assigning the minion to skirmish the character, as
     * getAssignmentCosts has them, and says whether there was any.  For the payment the Free Peoples player is
     * offered ahead of his own assigning (AssignmentCostRule; Q-0283), which is the minion's optional trigger.
     */
    boolean appendAssignmentCosts(LotroGame game, CostToEffectAction action, PhysicalCard fpCharacter, PhysicalCard minion);

    int getSanctuaryHealModifier(LotroGame game);

    int getStartingFellowshipCostModifier(LotroGame game, String playerId);

    int getMinimumBid(LotroGame game, String playerId);

    boolean isHandRevealed(LotroGame game, String playerId);

    int getUniqueness(LotroGame game, PhysicalCard card);

    int getPotentialDiscount(LotroGame game, PhysicalCard playedCard);

    void appendPotentialDiscounts(LotroGame game, CostToEffectAction action, PhysicalCard playedCard);

    boolean deadPileGoesToDiscard(LotroGame game, PhysicalCard card);

    /**
     * Whether another card says that this card comes into play exhausted ("Each [sauron] Orc comes into play
     * exhausted", Blood of Númenor 2_31): PlayCardEffect asks it of a character as it is placed (Q-0302).
     */
    boolean comesIntoPlayExhausted(LotroGame game, PhysicalCard card);

    boolean sanctuaryMayRemoveBurdens(LotroGame game, String playerId);
}
