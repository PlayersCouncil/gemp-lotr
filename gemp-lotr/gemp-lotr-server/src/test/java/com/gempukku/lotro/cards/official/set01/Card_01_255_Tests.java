package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_255_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("strength", "1_255");
					put("strength2", "1_255");
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, vitality 2; text only on a weapon played on him
					put("inquisitor", "1_268"); // Orc Inquisitor: [sauron] Orc, vitality 3; text only as he is played
					put("ambusher", "1_261");   // Orc Ambusher: [sauron] Orc, vitality 2; a response only to a Free Peoples regroup event
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] minion who is an Uruk-hai, vitality 2; damage +1
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc, vitality 2; his text needs 2 wounds on the Ring-bearer
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: vitality 3; a skirmish ability used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, vitality 2; "Maneuver: Exert Thrarin to allow him to participate in archery fire and skirmishes until the regroup phase."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MordorsStrengthStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Mordor's Strength
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Exert a [sauron] Orc to wound a character he is skirmishing.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("strength");

		assertEquals("Mordor's Strength", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsASauronOrcToWoundTheCharacterHeIsSkirmishingAndNobodyForAnOrcWhoseSkirmishHasNotBegun() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetShadowCard("strength");
		var strength2 = scn.GetShadowCard("strength2");
		var chieftain = scn.GetShadowCard("chieftain");
		var inquisitor = scn.GetShadowCard("inquisitor");
		var ambusher = scn.GetShadowCard("ambusher");
		var guard = scn.GetShadowCard("guard");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(strength, strength2);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(chieftain, inquisitor, ambusher, guard, hound);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, chieftain}, new PhysicalCardImpl[]{boromir, inquisitor});
		// The Uruk-hai and the [wraith] Orc each join a skirmish, so each fails only its culture or its race.
		scn.ShadowAssignToMinions(new PhysicalCardImpl[]{aragorn, guard}, new PhysicalCardImpl[]{boromir, hound});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(strength));
		// The Ambusher was given to nobody.
		assertFalse(scn.IsCharAssigned(ambusher));
		assertFalse(scn.IsCharSkirmishing(ambusher));

		scn.ShadowPlayCard(strength);

		// Any [sauron] Orc may pay: the one in this skirmish, the one whose skirmish is still to come and the one
		// assigned to nobody.
		assertTrue(scn.ShadowHasCardChoiceAvailable(chieftain, inquisitor, ambusher));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(inquisitor));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));

		// The Inquisitor is assigned to Boromir, but that skirmish has not begun: he is skirmishing nobody.
		scn.ShadowChooseCard(inquisitor);

		assertEquals(1, scn.GetWoundsOn(inquisitor));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertInDiscard(strength);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsPass();
		scn.ShadowPlayCard(strength2);
		assertEquals(0, scn.GetWoundsOn(chieftain));
		scn.ShadowChooseCard(chieftain);

		// The Chieftain exerts, and Aragorn, whom he is skirmishing, is wounded; nobody else is.
		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(inquisitor));
		assertEquals(0, scn.GetWoundsOn(ambusher));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(hound));
		assertInDiscard(strength2);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WoundsAnAllyHeIsSkirmishingAndAWoundKillsHimWithOneVitalityLeft() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetShadowCard("strength");
		var chieftain = scn.GetShadowCard("chieftain");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(strength);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(chieftain);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);

		// Thrarin has exerted to take part in skirmishes: vitality 2, one left.
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, chieftain);
		scn.FreepsResolveSkirmish(thrarin);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(chieftain));
		scn.ShadowPlayCard(strength);

		// A wound, not an exertion: an exhausted ally takes it and is killed.
		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertInZone(Zone.DEAD, thrarin);
		assertInDiscard(strength);
		// His skirmish is over with him gone, and it was the only one.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlySauronOrcIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetShadowCard("strength");
		var chieftain = scn.GetShadowCard("chieftain");
		var guard = scn.GetShadowCard("guard");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(strength);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(chieftain, guard, hound);
		scn.AddWoundsToChar(chieftain, 1);
		assertEquals(1, scn.GetWoundsOn(chieftain));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, chieftain}, new PhysicalCardImpl[]{boromir, hound});
		scn.ShadowAssignToMinions(aragorn, guard);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// The Chieftain, vitality 2, has one wound; the Uruk-hai and the [wraith] Orc could exert but are no [sauron] Orcs.
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(hound));
		assertTrue(scn.GetTwilight() >= 1);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(strength));
	}
}
