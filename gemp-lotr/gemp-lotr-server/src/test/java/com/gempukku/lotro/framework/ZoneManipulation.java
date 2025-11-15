package com.gempukku.lotro.framework;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCardImpl;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * While the ability to programmatically execute games is a boon to testing efforts, the real strength of this test rig
 * is in bald-faced cheating to arrange the table however we like without needing to abide by all the costs,
 * requirements, and game rules that slow things down.  Use these functions to stack the table just the way you want,
 * and then your tests will only be a handful of true steps and assertions.
 *
 * Note that these functions can be called at any time and they will be executed by Gemp, even if it is currently
 * awaiting a decision.  This can be used and abused by a clever tester, but remember that if you were hoping to
 * utilize the action on a card, that card must be in the proper zone at the time that Gemp checks for pending
 * decisions, meaning you may need to perform some other action (or a pass) to get Gemp to realize that you have moved
 * a card.
 */
public interface ZoneManipulation extends TestBase{

	/**
	 * Removes a physical card's current zone.  This is a prerequisite to a card actually properly moving to a new zone.
	 * This shouldn't be necessary to call directly within tests.
	 * @param card The card to make zoneless.
	 */
	default void RemoveCardZone(PhysicalCardImpl card) {
		if(card.getZone() != null)
		{
			//The "performing player" parameter only alters which player sees the animation, so we don't care here
			gameState().removeCardsFromZone(null, new ArrayList<>() {{
				add(card);
			}});
		}
	}

	/**
	 * Manually moves a given card to a given zone.  This ignores any game costs, requirements, or rules and
	 * effectively teleports the card to whatever zone you like.  Do be careful where you stick weird things.
	 * @param card The card to teleport
	 * @param zone The zone to teleport into
	 */
	default void MoveCardToZone(PhysicalCardImpl card, Zone zone) {
		RemoveCardZone(card);
		gameState().addCardToZone(game(), card, zone);
	}


	default void MoveCompanionsToTable(String...names) {
		Arrays.stream(names).forEach(name -> MoveCompanionsToTable(GetFreepsCard(name)));
	}
	default void MoveCompanionsToTable(PhysicalCardImpl...cards) {
		Arrays.stream(cards).forEach(card -> MoveCardToZone(card, Zone.FREE_CHARACTERS));
	}
	default void MoveMinionsToTable(String...names) {
		Arrays.stream(names).forEach(name -> MoveMinionsToTable(GetShadowCard(name)));
	}
	default void MoveMinionsToTable(PhysicalCardImpl...cards) {
		Arrays.stream(cards).forEach(card -> MoveCardToZone(card, Zone.SHADOW_CHARACTERS));
	}

	default void MoveFreepsCardsToSupportArea(String...names) {
		Arrays.stream(names).forEach(name -> MoveCardsToSupportArea(GetFreepsCard(name)));
	}

	default void MoveShadowCardsToSupportArea(String...names) {
		Arrays.stream(names).forEach(name -> MoveCardsToSupportArea(GetShadowCard(name)));
	}

	default void MoveCardsToSupportArea(PhysicalCardImpl...cards) {
		Arrays.stream(cards).forEach(card -> MoveCardToZone(card, Zone.SUPPORT));
	}


	default void MoveCardsToTopOfFreepsDeck(String...names) {
		Arrays.stream(names).forEach(name -> MoveCardsToTopOfDeck(GetFreepsCard(name)));
	}
	default void MoveCardsToTopOfShadowDeck(String...names) {
		Arrays.stream(names).forEach(name -> MoveCardsToTopOfDeck(GetShadowCard(name)));
	}
	default void MoveCardsToTopOfDeck(PhysicalCardImpl...cards) {
		Arrays.stream(cards).forEach(card -> {
			RemoveCardZone(card);
			gameState().putCardOnTopOfDeck(card);
		});
	}

	default void MoveCardsToBottomOfFreepsDeck(String...names) {
		Arrays.stream(names).forEach(name -> MoveCardsToBottomOfDeck(GetFreepsCard(name)));
	}
	default void MoveCardsToBottomOfShadowDeck(String...names) {
		Arrays.stream(names).forEach(name -> MoveCardsToBottomOfDeck(GetShadowCard(name)));
	}
	default void MoveCardsToBottomOfDeck(PhysicalCardImpl...cards) {
		Arrays.stream(cards).forEach(card -> {
			RemoveCardZone(card);
			gameState().putCardOnBottomOfDeck(card);
		});
	}

	default void MoveCardsToFreepsDiscard(String...names) {
		Arrays.stream(names).forEach(name -> MoveCardsToDiscard(GetFreepsCard(name)));
	}

	default void MoveCardsToShadowDiscard(String...names) {
		Arrays.stream(names).forEach(name -> MoveCardsToDiscard(GetShadowCard(name)));
	}

	default void MoveCardsToDiscard(PhysicalCardImpl...cards) {
		Arrays.stream(cards).forEach(card -> MoveCardToZone(card, Zone.DISCARD));
	}

	default void MoveCardToAdventurePath(PhysicalCardImpl card) {
		int num = gameState().getCurrentSiteNumber() + 1;
		MoveCardToZone(card, Zone.ADVENTURE_PATH);
		card.setSiteNumber(num);
	}

	default void MoveCardsToAdventureDeck(PhysicalCardImpl...cards) {
		Arrays.stream(cards).forEach(card -> MoveCardToZone(card, Zone.ADVENTURE_DECK));
	}

	default void MoveCardsToDeadPile(PhysicalCardImpl...cards) {
		Arrays.stream(cards).forEach(card -> MoveCardToZone(card, Zone.DEAD));
	}





	default void MoveCardsToFreepsHand(String...names) {
		Arrays.stream(names).forEach(name -> MoveCardsToHand(GetFreepsCard(name)));
	}
	default void MoveCardsToShadowHand(String...names) {
		Arrays.stream(names).forEach(name -> MoveCardsToHand(GetShadowCard(name)));
	}
	default void MoveCardsToHand(PhysicalCardImpl...cards) {
		for(PhysicalCardImpl card : cards) {
			RemoveCardZone(card);
			MoveCardToZone(card, Zone.HAND);
		}
	}


	/**
	 * Directly attaches one or more cards to a target card, regardless of legality or costs.  This is often used once
	 * a card has already proven to deploy properly for expedience in follow-up tests.
	 * @param bearer Which card should be bearing the given cards.
	 * @param cards One or more cards to attach
	 */
    default void AttachCardsTo(PhysicalCardImpl bearer, PhysicalCardImpl...cards) {
        Arrays.stream(cards).forEach(card -> {
            RemoveCardZone(card);
            gameState().attachCard(game(), card, bearer);
        });
    }

	/**
	 * Directly stacks one or more cards on a target card, regardless of legality or costs.  This is often used once
	 * a card has already proven to stack properly for expedience in follow-up tests.
	 * @param on Which card the given cards should be stacked on.
	 * @param cards One or more cards to stack
	 */
    default void StackCardsOn(PhysicalCardImpl on, PhysicalCardImpl...cards) {
        Arrays.stream(cards).forEach(card -> {
            RemoveCardZone(card);
            gameState().stackCard(game(), card, on);
        });
    }

	default void FreepsDrawCard() { FreepsDrawCards(1); }
	default void FreepsDrawCards() { FreepsDrawCards(1); }

	default void FreepsDrawCards(int count) {
		for(int i = 0; i < count; i++) {
			gameState().playerDrawsCard(P1);
		}
	}

	default void ShadowDrawCard() { ShadowDrawCards(1); }
	default void ShadowDrawCards() { ShadowDrawCards(1); }

	default void ShadowDrawCards(int count) {
		for(int i = 0; i < count; i++) {
			gameState().playerDrawsCard(P2);
		}
	}


	default void ShuffleCardsIntoFreepsDeck(PhysicalCardImpl...cards) { ShuffleCardsIntoDrawDeck(P1, cards); }
	default void ShuffleCardsIntoShadowDeck(PhysicalCardImpl...cards) { ShuffleCardsIntoDrawDeck(P2, cards); }
	default void ShuffleCardsIntoDrawDeck(String player, PhysicalCardImpl...cards) {
		gameState().shuffleCardsIntoDeck(Arrays.stream(cards).toList(), player);
	}


	/**
	 * Shuffles the Free Peoples player's draw deck.
	 */
    default void ShuffleFreepsDeck() { ShuffleDeck(P1); }
	/**
	 * Shuffles the Shadow player's reserve deck.
	 */
    default void ShuffleShadowDeck() { ShuffleDeck(P2); }

	/**
	 * Shuffles the given player's reserve deck.
	 * @param player The player's deck to shuffle
	 */
    default void ShuffleDeck(String player) {
        gameState().shuffleDeck(player);
    }




}
