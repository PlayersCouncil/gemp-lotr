package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_038_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("marauder", "30_38");
					put("marauder2", "30_38");
					// Yazneg, Orkish Assassin: [gundabad] Orc, cost 1; a Maneuver ability only
					put("yazneg", "30_41");

					// Thorin, Oakenshield: [dwarven] Dwarf companion, vitality 4 (exhausted he has 3 wounds); a
					// Fellowship ability and a bonus for the [dwarven] followers he bears
					put("thorin", "30_19");
					// Balin, Brother of Dwalin: [dwarven] Dwarf companion, vitality 3; a Fellowship ability only
					put("balin", "30_2");
					// Dwalin, Brother of Balin: [dwarven] companion; a Maneuver ability used only on purpose
					put("dwalin", "30_9");
					// Glóin, Father of Gimli: [dwarven] companion; a Skirmish ability only
					put("gloin", "30_11");
					// Nori, Of the Blue Mountains: [dwarven] companion; a Maneuver ability used only on purpose
					put("nori", "30_15");
					// Gandalf, Leader of Dwarves: [gandalf] Wizard companion; a reconcile draw and Skirmish abilities
					put("gandalf", "30_25");
					// Dain Ironfoot, Lord of the Iron Hills: [dwarven] Dwarf ALLY; a start-of-regroup offer
					put("dain", "32_2");

					// [dwarven] followers: Roäc, Son of Carc a Bird, the others Dwarves
					put("roac", "32_8");
					put("bofur", "30_6");
					put("bifur", "30_5");
					put("ori", "30_18");
					// Old Thrush, Messenger Bird: a [shire] follower
					put("thrush", "31_42");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void OrkishMarauderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Orkish Marauder
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gundabad
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: When you play this minion, you may spot 4 [dwarven] followers to exhaust a [dwarven] companion.
		 * 	<b>Maneuver:</b> Spot 7 companions and remove (2) to wound a companion (except Bilbo).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("marauder");

		assertEquals("Orkish Marauder", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GUNDABAD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedWhileFourDwarvenFollowersAreSpottedItExhaustsADwarvenCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var gandalf = scn.GetFreepsCard("gandalf");
		var dain = scn.GetFreepsCard("dain");
		var roac = scn.GetFreepsCard("roac");
		var bofur = scn.GetFreepsCard("bofur");
		var bifur = scn.GetFreepsCard("bifur");
		var ori = scn.GetFreepsCard("ori");

		var marauder = scn.GetShadowCard("marauder");
		var marauder2 = scn.GetShadowCard("marauder2");
		scn.MoveCardsToHand(marauder);

		scn.StartGame();
		scn.MoveCompanionsToTable(thorin, balin, gandalf);
		scn.MoveMinionsToTable(marauder2);
		scn.MoveAlliesToTable(dain);
		// Four [dwarven] followers: two borne, two in the support area, one of them a Bird.
		scn.AttachCardsTo(thorin, roac);
		scn.AttachCardsTo(balin, bofur);
		scn.MoveCardsToSupportArea(bifur, ori);
		assertAttachedTo(roac, thorin);
		assertAttachedTo(bofur, balin);
		assertInZone(Zone.SUPPORT, bifur, ori);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(marauder);
		// The copy played makes the offer; the copy already in play answers nothing.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(marauder));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(marauder2));
		scn.ShadowChooseOptionalTrigger(marauder);

		// [dwarven] companions only: not Bilbo or Gandalf, nor the [dwarven] ally.
		assertTrue(scn.ShadowHasCardChoiceAvailable(thorin, balin));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bilbo, gandalf, dain));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(thorin);

		// Vitality 4: exhausted with 3 wounds.
		assertEquals(3, scn.GetWoundsOn(thorin));
		assertTrue(scn.IsExhausted(thorin));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(dain));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(marauder2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWithThreeDwarvenFollowers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var roac = scn.GetFreepsCard("roac");
		var bifur = scn.GetFreepsCard("bifur");
		var ori = scn.GetFreepsCard("ori");
		var thrush = scn.GetFreepsCard("thrush");

		var marauder = scn.GetShadowCard("marauder");
		scn.MoveCardsToHand(marauder);

		scn.StartGame();
		scn.MoveCompanionsToTable(thorin, balin);
		// Three [dwarven] followers, beside a [shire] follower and [dwarven] companions.
		scn.AttachCardsTo(thorin, roac);
		scn.MoveCardsToSupportArea(bifur, ori, thrush);
		assertAttachedTo(roac, thorin);
		assertInZone(Zone.SUPPORT, bifur, ori, thrush);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(marauder);
		assertInZone(Zone.SHADOW_CHARACTERS, marauder);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(marauder));
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWhenEveryDwarvenCompanionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var roac = scn.GetFreepsCard("roac");
		var bofur = scn.GetFreepsCard("bofur");
		var bifur = scn.GetFreepsCard("bifur");
		var ori = scn.GetFreepsCard("ori");

		var marauder = scn.GetShadowCard("marauder");
		scn.MoveCardsToHand(marauder);

		scn.StartGame();
		scn.MoveCompanionsToTable(thorin, balin);
		scn.AttachCardsTo(thorin, roac);
		scn.MoveCardsToSupportArea(bofur, bifur, ori);
		scn.AddWoundsToChar(thorin, 3);
		scn.AddWoundsToChar(balin, 2);
		assertEquals(3, scn.GetWoundsOn(thorin));
		assertEquals(2, scn.GetWoundsOn(balin));
		assertAttachedTo(roac, thorin);
		assertInZone(Zone.SUPPORT, bofur, bifur, ori);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Exhausting the exhausted does nothing (F-0005), so the offer is not made; Bilbo is no [dwarven] companion.
		scn.ShadowPlayCard(marauder);
		assertInZone(Zone.SHADOW_CHARACTERS, marauder);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(marauder));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DeclinedNothingIsExhaustedAndAnotherOrcPlayedAfterSetsNothingOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var roac = scn.GetFreepsCard("roac");
		var bofur = scn.GetFreepsCard("bofur");
		var bifur = scn.GetFreepsCard("bifur");
		var ori = scn.GetFreepsCard("ori");

		var marauder = scn.GetShadowCard("marauder");
		var yazneg = scn.GetShadowCard("yazneg");
		scn.MoveCardsToHand(marauder, yazneg);

		scn.StartGame();
		scn.MoveCompanionsToTable(thorin, balin);
		scn.AttachCardsTo(thorin, roac);
		scn.MoveCardsToSupportArea(bofur, bifur, ori);
		assertAttachedTo(roac, thorin);
		assertInZone(Zone.SUPPORT, bofur, bifur, ori);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(marauder);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(marauder));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(balin));

		// Another [gundabad] Orc played while the Marauder is in play.
		scn.ShadowPlayCard(yazneg);
		assertInZone(Zone.SHADOW_CHARACTERS, yazneg);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(marauder));
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ManeuverSpotting7CompanionsRemoves2ToWoundACompanionOtherThanBilbo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var gloin = scn.GetFreepsCard("gloin");
		var nori = scn.GetFreepsCard("nori");
		var gandalf = scn.GetFreepsCard("gandalf");
		var dain = scn.GetFreepsCard("dain");

		var marauder = scn.GetShadowCard("marauder");

		scn.StartGame();
		// Seven companions with Bilbo, and an ally beside them.
		scn.MoveCompanionsToTable(thorin, balin, dwalin, gloin, nori, gandalf);
		scn.MoveAlliesToTable(dain);
		// Balin exhausted: vitality 3, 2 wounds.
		scn.AddWoundsToChar(balin, 2);
		assertEquals(2, scn.GetWoundsOn(balin));
		scn.MoveMinionsToTable(marauder);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(marauder));
		scn.ShadowUseCardAction(marauder);
		assertEquals(0, scn.GetTwilight());

		// Companions of two cultures, the exhausted one among them; not Bilbo, nor the ally, nor the minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gandalf, thorin, balin));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bilbo, dain, marauder));
		scn.ShadowChooseCard(balin);

		// A wound and not an exertion: the exhausted Balin is killed.
		assertInZone(Zone.DEAD, balin);
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(dain));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverNotOfferedWith6CompanionsAndAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var gloin = scn.GetFreepsCard("gloin");
		var gandalf = scn.GetFreepsCard("gandalf");
		var dain = scn.GetFreepsCard("dain");

		var marauder = scn.GetShadowCard("marauder");

		scn.StartGame();
		scn.MoveCompanionsToTable(thorin, balin, dwalin, gloin, gandalf);
		scn.MoveAlliesToTable(dain);
		scn.MoveMinionsToTable(marauder);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(marauder));
	}

	@Test
	public void ManeuverNotOfferedWithOneTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var gloin = scn.GetFreepsCard("gloin");
		var nori = scn.GetFreepsCard("nori");
		var gandalf = scn.GetFreepsCard("gandalf");

		var marauder = scn.GetShadowCard("marauder");

		scn.StartGame();
		scn.MoveCompanionsToTable(thorin, balin, dwalin, gloin, nori, gandalf);
		scn.MoveMinionsToTable(marauder);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(1);
		scn.FreepsPass();

		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(marauder));
	}
}
