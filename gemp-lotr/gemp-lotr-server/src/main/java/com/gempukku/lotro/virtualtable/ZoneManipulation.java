package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.GameUtils;

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
public interface ZoneManipulation extends TableBase {

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


	/**
	 * Puts the Free Peoples player's named companions into play, without playing them.
	 * @param names The names the companions were given when the table was set up.
	 */
	default void MoveCompanionsToTable(String...names) {
		Arrays.stream(names).forEach(name -> MoveCompanionsToTable(GetFreepsCard(name)));
	}
	/**
	 * Puts companions into play, where companions go, without playing them.  Only companions: an ally put here
	 * would sit where no legally played ally ever does, so allies have {@link #MoveAlliesToTable}.
	 * @param cards The companions to put into play.
	 */
	default void MoveCompanionsToTable(PhysicalCardImpl...cards) {
		Arrays.stream(cards).forEach(card -> {
			Verify(card.getBlueprint().getCardType() == CardType.COMPANION,
					GameUtils.getFullName(card) + " is not a companion.  Allies go to the table with MoveAlliesToTable.");
			MoveCardToZone(card, Zone.FREE_CHARACTERS);
		});
	}
	/**
	 * Puts the Free Peoples player's named allies into play, without playing them.
	 * @param names The names the allies were given when the table was set up.
	 */
	default void MoveAlliesToTable(String...names) {
		Arrays.stream(names).forEach(name -> MoveAlliesToTable(GetFreepsCard(name)));
	}
	/**
	 * Puts allies into play, in the support area where a legally played ally goes, without playing them.
	 * @param cards The allies to put into play.
	 */
	default void MoveAlliesToTable(PhysicalCardImpl...cards) {
		Arrays.stream(cards).forEach(card -> {
			Verify(card.getBlueprint().getCardType() == CardType.ALLY,
					GameUtils.getFullName(card) + " is not an ally.  Companions go to the table with MoveCompanionsToTable.");
			MoveCardToZone(card, Zone.SUPPORT);
		});
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


	/**
	 * Gives the Free Peoples player (player 1) control of the first site that can be controlled, without any card
	 * taking it.  Nothing in the rules keeps the first player from controlling a site; on his own turn he is only
	 * out of step with when he would have taken it.
	 * See {@link #TakeControlOfSite(String)}.
	 */
	default void FreepsTakeControlOfSite() { TakeControlOfSite(P1()); }
	/**
	 * Gives the Shadow player (player 2) control of the first site that can be controlled, without any card
	 * taking it.
	 * See {@link #TakeControlOfSite(String)}.
	 */
	default void ShadowTakeControlOfSite() { TakeControlOfSite(P2()); }
	/**
	 * Gives the Free Peoples player (player 1) control of the given site, without any card taking it.
	 * See {@link #TakeControlOfSite(String, PhysicalCardImpl)}.
	 * @param site The site to take, which need not be the first that can be controlled.
	 */
	default void FreepsTakeControlOfSite(PhysicalCardImpl site) { TakeControlOfSite(P1(), site); }
	/**
	 * Gives the Shadow player (player 2) control of the given site, without any card taking it.
	 * See {@link #TakeControlOfSite(String, PhysicalCardImpl)}.
	 * @param site The site to take, which need not be the first that can be controlled.
	 */
	default void ShadowTakeControlOfSite(PhysicalCardImpl site) { TakeControlOfSite(P2(), site); }

	/**
	 * Gives the given player control of the first site that can be controlled: the lowest-numbered site on the
	 * adventure path that nobody controls and that every player's marker has moved beyond, which is the site an
	 * effect that takes control of a site would take.  This is a cheat like the rest of this interface: it answers
	 * no decision, so it can be called at any time, whoever is being asked what; no "take control of a site"
	 * effect is played, so nothing that responds to one is set off; and a decision already pending does not
	 * change what it offers.
	 * Fails if no site can be controlled.
	 * @param player The player who is to control the site.
	 */
	default void TakeControlOfSite(String player) {
		PhysicalCardImpl first = null;
		for (int i = 1; i < LowestPlayerPosition() && first == null; i++) {
			var site = (PhysicalCardImpl) gameState().getSite(i);
			if (site != null && site.getCardController() == null)
				first = site;
		}
		Verify(first != null, "No site can be controlled by " + player + ": there is no site nobody controls "
				+ "that every player's marker has moved beyond (the lowest marker is at site " + LowestPlayerPosition() + ").");
		TakeControlOfSite(player, first);
	}

	/**
	 * Gives the given player control of the given site, which is put in that player's support area as the rules
	 * have it.  The site need not be the lowest-numbered one that can be controlled, so that a board can hold a
	 * site of one player's between sites of the other's.  A cheat: see {@link #TakeControlOfSite(String)}.
	 * Fails if the card is not a site on the adventure path, if somebody already controls it, or if some player's
	 * marker is at that site or has yet to reach it.
	 * @param player The player who is to control the site.
	 * @param site The site to take.
	 */
	default void TakeControlOfSite(String player, PhysicalCardImpl site) {
		Verify(site != null && site.getBlueprint().getCardType() == CardType.SITE,
				(site == null ? "Nothing" : GameUtils.getFullName(site)) + " is not a site, so " + player + " cannot take control of it.");
		Verify(site.getCardController() == null,
				GameUtils.getFullName(site) + " is already controlled by " + site.getCardController() + ", so " + player + " cannot take control of it.");
		Verify(site.getZone() == Zone.ADVENTURE_PATH,
				GameUtils.getFullName(site) + " is not on the adventure path, so " + player + " cannot take control of it.");
		Verify(site.getSiteNumber() < LowestPlayerPosition(),
				GameUtils.getFullName(site) + " is site " + site.getSiteNumber() + " and a player's marker is at site "
						+ LowestPlayerPosition() + ": a site can be controlled only once every marker has moved beyond it.");

		gameState().takeControlOfCard(player, game(), site, Zone.SUPPORT);

		Verify(player.equals(site.getCardController()) && site.getZone() == Zone.SUPPORT,
				player + " did not take control of " + GameUtils.getFullName(site) + ".");
	}

	// The site number of the marker furthest back on the adventure path: the sites below it can be controlled.
	private int LowestPlayerPosition() {
		int lowest = Integer.MAX_VALUE;
		for (String player : gameState().getPlayerOrder().getAllPlayers())
			lowest = Math.min(lowest, gameState().getPlayerPosition(player));
		return lowest;
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
			gameState().playerDrawsCard(P1());
		}
	}

	default void ShadowDrawCard() { ShadowDrawCards(1); }
	default void ShadowDrawCards() { ShadowDrawCards(1); }

	default void ShadowDrawCards(int count) {
		for(int i = 0; i < count; i++) {
			gameState().playerDrawsCard(P2());
		}
	}


	default void ShuffleCardsIntoFreepsDeck(PhysicalCardImpl...cards) { ShuffleCardsIntoDrawDeck(P1(), cards); }
	default void ShuffleCardsIntoShadowDeck(PhysicalCardImpl...cards) { ShuffleCardsIntoDrawDeck(P2(), cards); }
	default void ShuffleCardsIntoDrawDeck(String player, PhysicalCardImpl...cards) {
		gameState().shuffleCardsIntoDeck(Arrays.stream(cards).toList(), player);
	}


	/**
	 * Shuffles the Free Peoples player's draw deck.
	 */
    default void ShuffleFreepsDeck() { ShuffleDeck(P1()); }
	/**
	 * Shuffles the Shadow player's reserve deck.
	 */
    default void ShuffleShadowDeck() { ShuffleDeck(P2()); }

	/**
	 * Shuffles the given player's reserve deck.
	 * @param player The player's deck to shuffle
	 */
    default void ShuffleDeck(String player) {
        gameState().shuffleDeck(player);
    }




}
