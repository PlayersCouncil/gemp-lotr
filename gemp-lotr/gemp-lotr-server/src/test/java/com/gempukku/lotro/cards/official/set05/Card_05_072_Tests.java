package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_072_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stalker", "5_72");
					put("stalker2", "5_72");
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: a [gollum] Shadow card; a regroup ability only

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion; a skirmish ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: a second [shire] companion; his abilities are used only on purpose
					put("barliman", "1_70");    // Barliman Butterbur: [gandalf] ally; a fellowship ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DesertStalkerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Desert Stalker
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 11
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Southron</b>. <b>Ambush (2)</b>.<br><b>Maneuver:</b> Exert this minion and spot 4 Free Peoples cultures to exert every companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stalker");

		assertEquals("Desert Stalker", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.SOUTHRON));
		assertTrue(scn.HasKeyword(card, Keyword.AMBUSH));
		assertEquals(2, scn.GetKeywordCount(card, Keyword.AMBUSH));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ManeuverAbilityExertsThisMinionAtFourCulturesToExertEveryCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(barliman);

		var stalker = scn.GetShadowCard("stalker");
		var stalker2 = scn.GetShadowCard("stalker2");

		scn.StartGame();
		scn.MoveMinionsToTable(stalker, stalker2);
		// Vitality 4 with 3 wounds: exhausted, so he cannot exert.
		scn.AddWoundsToChar(aragorn, 3);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		// Exactly 4: [shire] Frodo, [dwarven] Gimli, [gondor] Aragorn, and [gandalf] by an ally alone.
		assertEquals(4, scn.GetFreepsCultureCount());
		assertEquals(0, scn.GetWoundsOn(stalker));
		assertEquals(0, scn.GetWoundsOn(stalker2));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertTrue(scn.ShadowActionAvailable(stalker));

		scn.ShadowUseCardAction(stalker);
		// The cost: this copy exerts, the other does not.
		assertEquals(1, scn.GetWoundsOn(stalker));
		assertEquals(0, scn.GetWoundsOn(stalker2));
		// Every companion able to exert exerts once; the exhausted one is left as he was, not killed.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		// An ally is no companion.
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithThreeCulturesBesideAGollumCardAndASecondShireCompanionItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("aragorn", "gimli", "sam");

		var stalker = scn.GetShadowCard("stalker");
		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		scn.MoveMinionsToTable(stalker, gollum);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// [shire] Frodo and Sam, [gondor] Aragorn, [dwarven] Gimli; Gollum is a Shadow card and spots no Free
		// Peoples culture.
		assertEquals(3, scn.GetFreepsCultureCount());
		assertEquals(0, scn.GetWoundsOn(stalker));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(stalker));
	}

	@Test
	public void WithThisCopyExhaustedItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("aragorn", "gimli");
		scn.MoveAlliesToTable("barliman");

		var stalker = scn.GetShadowCard("stalker");
		var stalker2 = scn.GetShadowCard("stalker2");

		scn.StartGame();
		scn.MoveMinionsToTable(stalker, stalker2);
		// Vitality 2 with 1 wound: exhausted, so this copy cannot exert.
		scn.AddWoundsToChar(stalker, 1);
		assertEquals(1, scn.GetWoundsOn(stalker));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(4, scn.GetFreepsCultureCount());
		assertEquals(0, scn.GetWoundsOn(stalker2));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(stalker));
		// The second copy, able to exert, is offered its own.
		assertTrue(scn.ShadowActionAvailable(stalker2));
	}
}
