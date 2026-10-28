package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_093_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("footman", "12_93");
					put("footman2", "12_93");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man and a ranger, resistance 6
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf companion, resistance 6
					put("bounder", "1_286");    // Bounder: a Hobbit ally, and an ally's resistance is 0
					put("whatarethey", "1_119"); // What Are They?: Maneuver event, "Spot a ranger to discard a roaming minion"
					// Defend It and Hope: Maneuver event, "Exert a [gondor] Man to wound a minion once (or twice if that
					// minion is an Uruk-hai)"
					put("defend", "4_115");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcFootmanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Orc Footman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: When this minion is discarded from play, you may spot a companion who has resistance 4 or less to shuffle this minion into your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("footman");

		assertEquals("Orc Footman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void DiscardedFromPlayWithACompanionOfResistance4ItIsShuffledIntoItsOwnersDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var whatarethey = scn.GetFreepsCard("whatarethey");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(whatarethey);

		var footman = scn.GetShadowCard("footman");
		var footman2 = scn.GetShadowCard("footman2");

		scn.StartGame();
		// Two burdens: each companion's resistance 6 becomes 4.
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(footman, footman2);
		// At site 2 the two Orc Footmen, of site 4, are roaming.
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(4, scn.GetResistance(gimli));
		assertEquals(4, scn.GetResistance(aragorn));
		assertInZone(Zone.HAND, whatarethey);
		assertInZone(Zone.SHADOW_CHARACTERS, footman);
		assertInZone(Zone.SHADOW_CHARACTERS, footman2);
		int deck = scn.GetShadowDeckCount();
		int shuffles = scn.GetShadowDeckShuffleCount();

		scn.FreepsPlayCard(whatarethey);
		assertTrue(scn.FreepsHasCardChoiceAvailable(footman, footman2));
		scn.FreepsChooseCard(footman);

		assertInZone(Zone.DISCARD, footman);
		// Only the copy discarded makes the offer, and it is made to its owner.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(footman));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(footman2));
		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.DECK, footman);
		assertEquals(deck + 1, scn.GetShadowDeckCount());
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertInZone(Zone.SHADOW_CHARACTERS, footman2);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void DecliningTheOfferLeavesItInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var whatarethey = scn.GetFreepsCard("whatarethey");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(whatarethey);

		var footman = scn.GetShadowCard("footman");

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(footman);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(4, scn.GetResistance(aragorn));
		assertInZone(Zone.HAND, whatarethey);
		assertInZone(Zone.SHADOW_CHARACTERS, footman);
		int deck = scn.GetShadowDeckCount();
		int shuffles = scn.GetShadowDeckShuffleCount();

		// The one roaming minion is discarded with nothing asked.
		scn.FreepsPlayCard(whatarethey);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(footman));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.DISCARD, footman);
		assertEquals(deck, scn.GetShadowDeckCount());
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithEveryCompanionAtResistance5AndAnAllyAt0ItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		var whatarethey = scn.GetFreepsCard("whatarethey");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(whatarethey);

		var footman = scn.GetShadowCard("footman");

		scn.StartGame();
		// One burden: each companion's resistance 6 becomes 5, one too many. The ally's resistance is 0, but he is no companion.
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(footman);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(5, scn.GetResistance(aragorn));
		assertEquals(5, scn.GetResistance(gimli));
		assertEquals(0, scn.GetResistance(bounder));
		assertInZone(Zone.HAND, whatarethey);
		assertInZone(Zone.SHADOW_CHARACTERS, footman);
		int deck = scn.GetShadowDeckCount();

		scn.FreepsPlayCard(whatarethey);

		assertInZone(Zone.DISCARD, footman);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(footman));
		assertEquals(deck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void KilledAndNotDiscardedItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(defend);

		var footman = scn.GetShadowCard("footman");

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(footman);
		scn.AddWoundsToChar(footman, 1);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(4, scn.GetResistance(gimli));
		assertEquals(1, scn.GetWoundsOn(footman));
		assertInZone(Zone.HAND, defend);
		int deck = scn.GetShadowDeckCount();

		// Aragorn exerts, and the Footman's second wound kills it.
		scn.FreepsPlayCard(defend);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.DISCARD, footman);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(footman));
		assertEquals(deck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
