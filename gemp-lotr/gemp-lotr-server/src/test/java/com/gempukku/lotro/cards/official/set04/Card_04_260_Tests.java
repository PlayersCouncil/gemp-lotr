package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_260_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("strike1", "4_260");
					put("strike2", "4_260");    // a second copy, for a second skirmish
					// Easterling Axeman: [raider] Man, strength 6, vitality 2; strength +3 and fierce only at 2 burdens
					put("axeman1", "4_224");
					put("axeman2", "4_224");
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man, strength 8, no text
					// Ulaire Attea, Khamul of the East: a [raider] minion who is a Nazgul and no Man, strength 12, fierce;
					// his bonuses to Easterlings need 3 burdens
					put("khamul", "103_61");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4; a maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf, strength 6, vitality 3; a skirmish ability used only on purpose
					// Bounder: [shire] Hobbit ally, strength 2, vitality 2, home site 2, where the first move takes the fellowship
					put("bounder", "1_286");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhirlingStrikeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Whirling Strike
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Wound a companion or ally skirmishing a [raider] Man.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("strike1");

		assertEquals("Whirling Strike", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WoundsTheCompanionSkirmishingARaiderManAndKillsOneWhoIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var strike1 = scn.GetShadowCard("strike1");
		var strike2 = scn.GetShadowCard("strike2");
		var axeman1 = scn.GetShadowCard("axeman1");
		var axeman2 = scn.GetShadowCard("axeman2");
		scn.MoveCardsToHand(strike1, strike2);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(axeman1, axeman2);
		// Vitality 3 with 2 wounds: Gimli is exhausted.
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(2, scn.GetWoundsOn(gimli));

		// A skirmish event: not offered at the Shadow player's assignment actions.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(strike1));
		scn.ShadowPass();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, axeman1}, new PhysicalCardImpl[]{gimli, axeman2});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(axeman1));
		assertTrue(scn.ShadowPlayAvailable(strike1));
		scn.ShadowPlayCard(strike1);

		// Aragorn is the one character skirmishing a [raider] Man, so nothing is asked. Gimli is assigned to a
		// [raider] Man, but that skirmish has not begun; the Axeman is no companion or ally.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(axeman1));
		assertInZone(Zone.DISCARD, strike1);

		scn.FreepsPass();
		scn.ShadowPass();

		// Gimli's skirmish: the wound kills him, exhausted as he is.
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertTrue(scn.ShadowPlayAvailable(strike2));
		scn.ShadowPlayCard(strike2);

		assertInZone(Zone.DEAD, gimli);
		assertInZone(Zone.DISCARD, strike2);
		assertEquals(0, scn.GetWoundsOn(axeman2));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		// His skirmish is over with him gone, and it was the last.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WoundsAnAllySkirmishingARaiderMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bounder);

		var strike1 = scn.GetShadowCard("strike1");
		var axeman1 = scn.GetShadowCard("axeman1");
		scn.MoveCardsToHand(strike1);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(axeman1);

		// The fellowship has moved to site 2, the Bounder's home.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bounder, axeman1);
		scn.FreepsResolveSkirmish(bounder);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowPlayAvailable(strike1));
		scn.ShadowPlayCard(strike1);

		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(axeman1));
		assertInZone(Zone.DISCARD, strike1);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void PlayedInASkirmishAgainstAManOfAnotherCultureOrARaiderMinionWhoIsNoManItWoundsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var strike1 = scn.GetShadowCard("strike1");
		var strike2 = scn.GetShadowCard("strike2");
		var brigand = scn.GetShadowCard("brigand");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCardsToHand(strike1, strike2);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(brigand, khamul);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, khamul}, new PhysicalCardImpl[]{gimli, brigand});

		// Aragorn against a [raider] minion who is no Man.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowPlayAvailable(strike1));
		scn.ShadowPlayCard(strike1);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertInZone(Zone.DISCARD, strike1);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(khamul));

		scn.FreepsPass();
		scn.ShadowPass();
		// 8 against Khamul's 12: Aragorn loses and takes a wound.
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Gimli against a Man of [dunland].
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(strike2));
		scn.ShadowPlayCard(strike2);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertInZone(Zone.DISCARD, strike2);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(brigand));
	}
}
