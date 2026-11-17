package com.gempukku.lotro.cards.unofficial.pc.errata.set19;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_19_037_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lemenya", "69_37");
					// Ulaire Nelya, Ringwraith in Twilight: another Nazgul, strength 10; nothing else below 2
					// burdens
					put("nelya", "2_84");

					// Three [wraith] possessions, each borne by a Nazgul: Nazgul Sword (hand weapon, strength
					// +2), Black Steed (mount) and Nazgul Blade (hand weapon). None does anything in a pile.
					put("sword1", "1_218");
					put("sword2", "1_218");
					put("sword3", "1_218");
					put("sword4", "1_218");
					put("steed1", "1_208");
					put("steed2", "1_208");
					put("steed3", "1_208");
					put("steed4", "1_208");
					put("blade1", "12_168");
					put("blade2", "12_168");
					put("blade3", "12_168");
					put("blade4", "12_168");

					// Ring of Asperity: a [wraith] artifact, so a [wraith] item that is no possession
					put("ring", "9_42");
					// Goblin Scimitar: a [moria] possession
					put("scimitar1", "1_180");
					put("scimitar2", "1_180");
					// Fell Beast: a [wraith] mount, cost 0, strength +2; "To play, discard 2 cards from hand."
					put("beast", "6_83");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireLemenyaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: Úlairë Lemenya, Dark Enemy
		 * Unique: true
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Nazgul
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: <b>Fierce.</b>
		 * 	Úlairë Lemenya is strength +1 for each [wraith] possession in your discard pile (limit +9).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lemenya");

		assertEquals("Úlairë Lemenya", card.getBlueprint().getTitle());
		assertEquals("Dark Enemy", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void StrengthPlusOneForEachWraithPossessionInHisOwnersDiscardPileAndForNothingElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lemenya = scn.GetShadowCard("lemenya");
		var nelya = scn.GetShadowCard("nelya");
		var swordInHand = scn.GetShadowCard("sword1");
		var swordInPlay = scn.GetShadowCard("sword2");
		var steed = scn.GetShadowCard("steed1");
		var blade = scn.GetShadowCard("blade1");
		var ring = scn.GetShadowCard("ring");
		var scimitar1 = scn.GetShadowCard("scimitar1");
		var scimitar2 = scn.GetShadowCard("scimitar2");
		var otherPlayersSword = scn.GetFreepsCard("sword3");
		var otherPlayersSteed = scn.GetFreepsCard("steed2");

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, nelya);
		scn.SkipToPhase(Phase.SHADOW);

		// In his owner's discard pile: a [wraith] artifact and two possessions of another culture. In the other
		// player's discard pile: two [wraith] possessions. His owner's own [wraith] possessions are in hand, on
		// the other Nazgul, and (every other copy) in the draw deck.
		scn.MoveCardsToDiscard(ring, scimitar1, scimitar2, otherPlayersSword, otherPlayersSteed);
		scn.MoveCardsToHand(swordInHand);
		scn.AttachCardsTo(nelya, swordInPlay);

		assertInZone(Zone.DECK, steed, blade);
		assertEquals(3, scn.GetShadowDiscardCount());
		assertEquals(2, scn.GetFreepsDiscardCount());
		assertEquals(9, scn.GetStrength(lemenya));
		// 10 + 2 for the Nazgul Sword he bears
		assertEquals(12, scn.GetStrength(nelya));

		scn.MoveCardsToDiscard(swordInHand);
		assertEquals(10, scn.GetStrength(lemenya));

		// A mount and a second hand weapon
		scn.MoveCardsToDiscard(steed, blade);
		assertEquals(6, scn.GetShadowDiscardCount());
		// 9 + 3: every [wraith] card there would make it 13, every possession 14, both piles 14
		assertEquals(12, scn.GetStrength(lemenya));
		// The other Nazgul gains nothing from the pile
		assertEquals(12, scn.GetStrength(nelya));
		assertAttachedTo(swordInPlay, nelya);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void GrowsByOneWhenAWraithPossessionAndAWraithArtifactAreDiscardedFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lemenya = scn.GetShadowCard("lemenya");
		var nelya = scn.GetShadowCard("nelya");
		var beast = scn.GetShadowCard("beast");
		var sword = scn.GetShadowCard("sword1");
		var ring = scn.GetShadowCard("ring");
		// The Fell Beast's two discards are the whole of the rest of the hand
		scn.MoveCardsToHand(beast, sword, ring);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, nelya);
		scn.SkipToPhase(Phase.SHADOW);

		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(10, scn.GetStrength(nelya));

		scn.ShadowPlayCard(beast);
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, nelya));
		scn.ShadowChooseCard(nelya);

		assertAttachedTo(beast, nelya);
		assertInDiscard(sword, ring);
		assertEquals(2, scn.GetShadowDiscardCount());
		// 9 + 1 for the Nazgul Sword; the Ring of Asperity is an artifact
		assertEquals(10, scn.GetStrength(lemenya));
		// 10 + 2 for the Fell Beast, and nothing for the pile
		assertEquals(12, scn.GetStrength(nelya));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheBonusStopsAtPlusNine() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lemenya = scn.GetShadowCard("lemenya");
		var borneSword = scn.GetShadowCard("sword4");

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SkipToPhase(Phase.SHADOW);

		assertEquals(9, scn.GetStrength(lemenya));

		scn.MoveCardsToDiscard(scn.GetShadowCard("sword1"), scn.GetShadowCard("sword2"), scn.GetShadowCard("sword3"),
				scn.GetShadowCard("steed1"), scn.GetShadowCard("steed2"), scn.GetShadowCard("steed3"),
				scn.GetShadowCard("steed4"), scn.GetShadowCard("blade1"));
		assertEquals(8, scn.GetShadowDiscardCount());
		// 9 + 8
		assertEquals(17, scn.GetStrength(lemenya));

		scn.MoveCardsToDiscard(scn.GetShadowCard("blade2"));
		assertEquals(9, scn.GetShadowDiscardCount());
		// 9 + 9
		assertEquals(18, scn.GetStrength(lemenya));

		// A tenth and an eleventh add nothing
		scn.MoveCardsToDiscard(scn.GetShadowCard("blade3"));
		assertEquals(10, scn.GetShadowDiscardCount());
		assertEquals(18, scn.GetStrength(lemenya));
		scn.MoveCardsToDiscard(scn.GetShadowCard("blade4"));
		assertEquals(11, scn.GetShadowDiscardCount());
		assertEquals(18, scn.GetStrength(lemenya));

		// The limit is on this bonus and not on his strength: 9 + 9 + 2 for a Nazgul Sword he bears
		scn.AttachCardsTo(lemenya, borneSword);
		assertEquals(20, scn.GetStrength(lemenya));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
