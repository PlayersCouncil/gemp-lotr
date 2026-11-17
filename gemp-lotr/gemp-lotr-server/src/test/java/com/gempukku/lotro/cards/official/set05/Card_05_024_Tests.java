package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_024_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gollum", "5_24");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4; a maneuver ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion, vitality 4; his abilities are used only on purpose
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally, vitality 2; a skirmish ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc minion of vitality 3 with no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GollumStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Gollum, Nasty Treacherous Creature
		 * Unique: True
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype:
		 * Strength: 5
		 * Vitality: 4
		 * Site Number: 3
		 * Game Text: <b>Regroup:</b> Discard Gollum to exert a companion or ally.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gollum");

		assertEquals("Gollum", card.getBlueprint().getTitle());
		assertEquals("Nasty Treacherous Creature", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		// CR 4.2 "Gollum/Sméagol": he has no race.
		assertNull(card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void RegroupAbilityDiscardsGollumToExertACompanionOrAllyOfTheShadowPlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gollum = scn.GetShadowCard("gollum");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		// Laid as they would be played in the Shadow phase; nobody assigns them, so both live to the regroup phase.
		scn.MoveMinionsToTable(gollum, troop);
		// Vitality 4 with 3 wounds: Sam is exhausted and cannot exert.
		scn.AddWoundsToChar(sam, 3);
		assertEquals(3, scn.GetWoundsOn(sam));

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.ShadowActionAvailable(gollum));

		scn.ShadowUseCardAction(gollum);
		assertInDiscard(gollum);
		// Two companions of different cultures and an ally; not the exhausted Sam, and not a minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn, bounder));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sam, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(bounder);
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(gollum));

		// The same turn's regroup phase: offered.
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(gollum));
	}

	@Test
	public void WithNobodyAbleToExertGollumIsStillDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gollum = scn.GetShadowCard("gollum");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, troop);
		// Frodo, vitality 4 with 3 wounds, is the only companion and is exhausted; there is no ally.
		scn.AddWoundsToChar(frodo, 3);
		assertEquals(3, scn.GetWoundsOn(frodo));

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.ShadowActionAvailable(gollum));

		scn.ShadowUseCardAction(gollum);
		assertInDiscard(gollum);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
