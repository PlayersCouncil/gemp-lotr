package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_102_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("overlord", "15_102");
					put("denizen", "11_115");     // Denizen of Khazad-dum: [orc] Orc; +2 only at an underground site
					put("halftroll", "14_12");    // Half-troll of Far Harad: an [orc] minion who is no Orc; a start-of-assignment trigger only
					put("goblin", "1_178");       // Goblin Runner: a [moria] Orc; its (2) only when played
					put("retribution", "12_101"); // Retribution: [orc] support-area condition; a win trigger and a Shadow ability used only on purpose
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: unbound [gondor] Man; a maneuver ability used only on purpose
					put("gimli", "1_13");         // Gimli, Son of Gloin: unbound [dwarven] Dwarf; a skirmish ability used only on purpose
					put("glorfindel", "9_16");    // Glorfindel, Revealed in Wrath: unbound [elven] Elf, vitality 3; abilities used only on purpose
					put("sam", "1_311");          // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer; a fellowship ability used only on purpose
					put("bounder", "1_286");      // Bounder: a Hobbit ally (home 2F); a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BlackLandOverlordStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Black Land Overlord
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 11
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Maneuver:</b> Remove a threat and spot another [orc] minion to exert an unbound companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("overlord");

		assertEquals("Black Land Overlord", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ManeuverAbilityRemovesAThreatToExertAnUnboundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var glorfindel = scn.GetFreepsCard("glorfindel");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, gimli, glorfindel, sam);
		scn.MoveAlliesToTable(bounder);

		var overlord = scn.GetShadowCard("overlord");
		var denizen = scn.GetShadowCard("denizen");

		scn.StartGame();
		scn.MoveMinionsToTable(overlord, denizen);
		// Glorfindel, vitality 3, exhausted: he cannot exert.
		scn.AddWoundsToChar(glorfindel, 2);
		// Two threats: the cost takes exactly one of them.
		scn.AddThreats(2);
		assertEquals(2, scn.GetWoundsOn(glorfindel));
		assertEquals(2, scn.GetThreats());
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(overlord));
		scn.ShadowUseCardAction(overlord);

		assertEquals(1, scn.GetThreats());
		// Unbound companions of two cultures and races; not the Ring-bearer, Sam (Ring-bound), the Bounder (an ally)
		// or the exhausted Glorfindel.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, sam, bounder, glorfindel));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(gimli);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(2, scn.GetWoundsOn(glorfindel));
		assertEquals(0, scn.GetWoundsOn(denizen));
		assertEquals(0, scn.GetWoundsOn(overlord));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AnOrcMinionWhoIsNoOrcWillDoForTheSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var overlord = scn.GetShadowCard("overlord");
		var halftroll = scn.GetShadowCard("halftroll");

		scn.StartGame();
		scn.MoveMinionsToTable(overlord, halftroll);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(overlord));
		scn.ShadowUseCardAction(overlord);

		// Aragorn is the one unbound companion, exerted with nothing asked.
		assertEquals(0, scn.GetThreats());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWithoutAnotherOrcMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var overlord = scn.GetShadowCard("overlord");
		var goblin = scn.GetShadowCard("goblin");
		var retribution = scn.GetShadowCard("retribution");

		scn.StartGame();
		// A [moria] Orc, an [orc] condition, and the Overlord itself.
		scn.MoveMinionsToTable(overlord, goblin);
		scn.MoveCardsToSupportArea(retribution);
		assertInZone(Zone.SUPPORT, retribution);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(overlord));
		assertEquals(1, scn.GetThreats());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWithNoThreatToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var overlord = scn.GetShadowCard("overlord");
		var denizen = scn.GetShadowCard("denizen");

		scn.StartGame();
		scn.MoveMinionsToTable(overlord, denizen);
		assertEquals(0, scn.GetThreats());
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(overlord));
	}

	@Test
	public void WithEveryUnboundCompanionExhaustedTheThreatIsRemovedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var overlord = scn.GetShadowCard("overlord");
		var denizen = scn.GetShadowCard("denizen");

		scn.StartGame();
		scn.MoveMinionsToTable(overlord, denizen);
		// Aragorn, vitality 4, exhausted.
		scn.AddWoundsToChar(aragorn, 3);
		scn.AddThreats(1);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetThreats());
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(overlord));
		scn.ShadowUseCardAction(overlord);

		assertEquals(0, scn.GetThreats());
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
