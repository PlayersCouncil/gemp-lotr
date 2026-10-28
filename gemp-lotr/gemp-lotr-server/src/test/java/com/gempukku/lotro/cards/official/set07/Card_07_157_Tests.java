package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_157_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wrath", "7_157");
					put("wrath2", "7_157");
					put("explorer", "4_250");   // Southron Explorer: a [raider] Southron Man, vitality 3; a regroup ability used only on purpose
					put("explorer2", "4_250");
					// Easterling Axeman: a [raider] Easterling Man, vitality 2; strength +3 and fierce only at 2 burdens
					put("axeman", "4_224");
					// Ulaire Attea, Khamul of the East: a [raider] minion who is a Nazgul and no Man; fierce, his text needs 3 burdens
					put("khamul", "103_61");
					put("brigand", "4_10");     // Dunlending Brigand: a Man who is not [raider], vitality 1; no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: vitality 3; a skirmish ability used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, vitality 2; "Maneuver: Exert Thrarin to allow him to participate in archery fire and skirmishes until the regroup phase."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RedWrathStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Red Wrath
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 5
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Exert a [raider] Man to wound a companion he is skirmishing twice.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wrath");

		assertEquals("Red Wrath", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(5, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsARaiderManToWoundTheCompanionHeIsSkirmishingTwiceAndNobodyForAManWhoseSkirmishHasNotBegun() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wrath = scn.GetShadowCard("wrath");
		var wrath2 = scn.GetShadowCard("wrath2");
		var explorer = scn.GetShadowCard("explorer");
		var explorer2 = scn.GetShadowCard("explorer2");
		var axeman = scn.GetShadowCard("axeman");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(wrath, wrath2);
		assertInHand(wrath, wrath2);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.MoveMinionsToTable(explorer, axeman, khamul, brigand, explorer2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, explorer}, new PhysicalCardImpl[]{gimli, axeman});
		// The [raider] Nazgul and the [dunland] Man each join a skirmish, so each fails only its race or its culture.
		scn.ShadowAssignToMinions(new PhysicalCardImpl[]{aragorn, khamul}, new PhysicalCardImpl[]{gimli, brigand});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(wrath));
		// The second Southron was given to nobody.
		assertFalse(scn.IsCharAssigned(explorer2));
		assertEquals(0, scn.GetWoundsOn(axeman));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.ShadowPlayCard(wrath);

		// Any [raider] Man may pay: the one in this skirmish, the one whose skirmish is still to come and the one
		// assigned to nobody.
		assertTrue(scn.ShadowHasCardChoiceAvailable(explorer, axeman, explorer2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(khamul, brigand, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());

		// The Axeman is assigned to Gimli, but that skirmish has not begun: he is skirmishing nobody.
		scn.ShadowChooseCard(axeman);

		assertEquals(1, scn.GetWoundsOn(axeman));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInDiscard(wrath);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(explorer));
		scn.ShadowPlayCard(wrath2);
		scn.ShadowChooseCard(explorer);

		// The Southron exerts, and Aragorn, whom he is skirmishing, takes two wounds; nobody else is wounded.
		assertEquals(1, scn.GetWoundsOn(explorer));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(axeman));
		assertEquals(0, scn.GetWoundsOn(explorer2));
		assertEquals(0, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(brigand));
		assertInDiscard(wrath2);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheWoundsKillAnExhaustedCompanionHeIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wrath = scn.GetShadowCard("wrath");
		var explorer = scn.GetShadowCard("explorer");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(wrath);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(explorer);
		// Vitality 3 with 2 wounds: Gimli is exhausted, so no exertion could be placed on him.
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(2, scn.GetWoundsOn(gimli));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, explorer);
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(explorer));
		scn.ShadowPlayCard(wrath);

		assertEquals(1, scn.GetWoundsOn(explorer));
		assertInZone(Zone.DEAD, gimli);
		assertInDiscard(wrath);
		// His skirmish is over with him gone, and it was the only one.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnAllyHeIsSkirmishingIsNoCompanionAndIsNotWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wrath = scn.GetShadowCard("wrath");
		var explorer = scn.GetShadowCard("explorer");
		var frodo = scn.GetRingBearer();
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(wrath);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(explorer);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);

		// Thrarin has exerted to take part in skirmishes: vitality 2, one left.
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, explorer);
		scn.FreepsResolveSkirmish(thrarin);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.ShadowPlayAvailable(wrath));
		scn.ShadowPlayCard(wrath);

		// The cost is paid, and the ally the Southron is skirmishing is no companion: nobody is wounded.
		assertEquals(1, scn.GetWoundsOn(explorer));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertInZone(Zone.SUPPORT, thrarin);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInDiscard(wrath);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlyRaiderManIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wrath = scn.GetShadowCard("wrath");
		var axeman = scn.GetShadowCard("axeman");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(wrath);
		assertInHand(wrath);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(axeman, khamul, brigand);
		scn.AddWoundsToChar(axeman, 1);
		assertEquals(1, scn.GetWoundsOn(axeman));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, axeman}, new PhysicalCardImpl[]{gimli, brigand});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// The Axeman, vitality 2, has one wound; the Nazgul and the [dunland] Man could exert but are no [raider] Men.
		assertEquals(1, scn.GetWoundsOn(axeman));
		assertEquals(0, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(brigand));
		assertTrue(scn.GetTwilight() >= 5);
		assertInHand(wrath);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(wrath));
	}
}
