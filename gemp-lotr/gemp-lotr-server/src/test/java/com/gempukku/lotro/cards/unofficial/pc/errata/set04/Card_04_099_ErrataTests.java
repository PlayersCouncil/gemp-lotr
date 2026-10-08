package com.gempukku.lotro.cards.unofficial.pc.errata.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_099_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("thunder", "54_99");
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: a fellowship ability used only on purpose
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is not Gandalf
					put("staff", "7_38");       // Gandalf's Staff: Free Peoples artifact borne by Gandalf, vitality +1
					put("sword", "1_299");      // Hobbit Sword: Free Peoples possession borne by a Hobbit, no text
					put("poison", "8_28");      // Spider Poison: [gollum] Shadow possession borne by a companion; exerts him when he is assigned
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man minion, no text
					put("brigand2", "4_10");
					put("axe", "4_26");         // Iron Axe: [dunland] Shadow possession borne by a [dunland] Man, no text
					put("axe2", "4_26");
					put("lemenya", "1_232");    // Ulaire Lemenya: Nazgul minion; strength -2 to bearers of [wraith] conditions (none here)
					put("rancor", "9_44");      // Ring of Rancor: [wraith] Shadow artifact borne by a Nazgul, vitality +1; abilities used only on purpose
					put("toldea", "7_219");     // Ulaire Toldea: Nazgul minion; a twilight discount when cheated in does nothing
					put("asperity", "9_42");    // Ring of Asperity: [wraith] Shadow artifact borne by a Nazgul; its when-played wound does nothing when cheated in
					put("palantir", "4_166");   // The Palantir of Orthanc: [isengard] Shadow artifact in the support area; a Shadow ability used only on purpose
					put("seeking", "1_276");    // Seeking Its Master: [sauron] Shadow condition in the support area; a Shadow ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RollOfThunderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: 4
		 * Name: Roll of Thunder
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Fellowship, Maneuver
		 * Game Text: Fellowship or Maneuver: Spot Gandalf to discard a Shadow possession or hinder a Shadow artifact.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("thunder");

		assertEquals("Roll of Thunder", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedWithoutGandalfThoughAGandalfCompanionIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thunder = scn.GetFreepsCard("thunder");
		var avenger = scn.GetFreepsCard("avenger");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCompanionsToTable(avenger);
		scn.AttachCardsTo(avenger, poison);
		scn.MoveCardsToHand(thunder);

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(thunder));
		assertInHand(thunder);
	}

	@Test
	public void InTheManeuverPhaseDiscardsAShadowPossessionBorneByAMinionOrACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thunder = scn.GetFreepsCard("thunder");
		var gandalf = scn.GetFreepsCard("gandalf");
		var staff = scn.GetFreepsCard("staff");
		var sword = scn.GetFreepsCard("sword");
		var frodo = scn.GetRingBearer();
		var poison = scn.GetShadowCard("poison");
		var brigand = scn.GetShadowCard("brigand");
		var brigand2 = scn.GetShadowCard("brigand2");
		var axe = scn.GetShadowCard("axe");
		var axe2 = scn.GetShadowCard("axe2");
		var lemenya = scn.GetShadowCard("lemenya");
		var rancor = scn.GetShadowCard("rancor");
		var seeking = scn.GetShadowCard("seeking");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, staff, poison);
		scn.AttachCardsTo(frodo, sword);
		scn.MoveCardsToHand(thunder);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand, brigand2, lemenya);
		scn.AttachCardsTo(brigand, axe);
		scn.AttachCardsTo(brigand2, axe2);
		scn.AttachCardsTo(lemenya, rancor);
		scn.MoveCardsToSupportArea(seeking);
		scn.HinderCard(axe2);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsPlayAvailable(thunder));
		int twilight = scn.GetTwilight();
		scn.FreepsPlayCard(thunder);
		assertEquals(twilight + 3, scn.GetTwilight());

		assertTrue(scn.FreepsChoiceAvailable("Discard a Shadow possession"));
		assertTrue(scn.FreepsChoiceAvailable("Hinder a Shadow artifact"));
		scn.FreepsChooseOption("Discard");

		// A hindered possession is no longer a possession (CR 5.0 "hinder"; Q-0098).
		assertTrue(scn.FreepsHasCardChoiceAvailable(axe, poison));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(axe2, sword, staff, rancor, seeking));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(poison);

		assertInDiscard(poison, thunder);
		assertAttachedTo(axe, brigand);
		assertAttachedTo(axe2, brigand2);
		assertAttachedTo(rancor, lemenya);
		assertFalse(scn.IsHindered(rancor));
		assertAttachedTo(staff, gandalf);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void InTheManeuverPhaseHindersAShadowArtifactBorneByAMinionOrInTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thunder = scn.GetFreepsCard("thunder");
		var gandalf = scn.GetFreepsCard("gandalf");
		var staff = scn.GetFreepsCard("staff");
		var brigand = scn.GetShadowCard("brigand");
		var axe = scn.GetShadowCard("axe");
		var lemenya = scn.GetShadowCard("lemenya");
		var rancor = scn.GetShadowCard("rancor");
		var toldea = scn.GetShadowCard("toldea");
		var asperity = scn.GetShadowCard("asperity");
		var palantir = scn.GetShadowCard("palantir");
		var seeking = scn.GetShadowCard("seeking");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, staff);
		scn.MoveCardsToHand(thunder);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand, lemenya, toldea);
		scn.AttachCardsTo(brigand, axe);
		scn.AttachCardsTo(lemenya, rancor);
		scn.AttachCardsTo(toldea, asperity);
		scn.MoveCardsToSupportArea(palantir, seeking);
		scn.HinderCard(asperity);
		scn.SkipToPhase(Phase.MANEUVER);

		int twilight = scn.GetTwilight();
		scn.FreepsPlayCard(thunder);
		assertEquals(twilight + 3, scn.GetTwilight());

		assertTrue(scn.FreepsChoiceAvailable("Discard a Shadow possession"));
		scn.FreepsChooseOption("Hinder");

		// A card already hindered cannot be hindered (Q-0060).
		assertTrue(scn.FreepsHasCardChoiceAvailable(rancor, palantir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(asperity, staff, axe, seeking));
		assertEquals(1, scn.FreepsGetChoiceMin());
		// Lemenya's vitality 2, +1 from the Ring of Rancor.
		assertEquals(3, scn.GetVitality(lemenya));
		scn.FreepsChooseCard(rancor);

		assertTrue(scn.IsHindered(rancor));
		assertAttachedTo(rancor, lemenya);
		assertEquals(2, scn.GetVitality(lemenya));
		assertFalse(scn.IsHindered(palantir));
		assertFalse(scn.IsHindered(seeking));
		assertAttachedTo(axe, brigand);
		assertInDiscard(thunder);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void InTheFellowshipPhaseWithOnlyAShadowArtifactInPlayItIsHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thunder = scn.GetFreepsCard("thunder");
		var gandalf = scn.GetFreepsCard("gandalf");
		var palantir = scn.GetShadowCard("palantir");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(palantir);
		scn.MoveCardsToHand(thunder);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(thunder));
		int twilight = scn.GetTwilight();
		scn.FreepsPlayCard(thunder);
		assertEquals(twilight + 3, scn.GetTwilight());

		// Only the hinder can be carried out, so it is what happens.
		assertTrue(scn.IsHindered(palantir));
		assertInZone(Zone.SUPPORT, palantir);
		assertInDiscard(thunder);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void InTheFellowshipPhaseWithOnlyAShadowPossessionInPlayItIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thunder = scn.GetFreepsCard("thunder");
		var gandalf = scn.GetFreepsCard("gandalf");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, poison);
		scn.MoveCardsToHand(thunder);

		scn.StartGame();

		int twilight = scn.GetTwilight();
		scn.FreepsPlayCard(thunder);
		assertEquals(twilight + 3, scn.GetTwilight());

		// Only the discard can be carried out, so it is what happens.
		assertInDiscard(poison, thunder);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsOfferedWithNoShadowPossessionOrArtifactInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thunder = scn.GetFreepsCard("thunder");
		var gandalf = scn.GetFreepsCard("gandalf");
		var staff = scn.GetFreepsCard("staff");
		var seeking = scn.GetShadowCard("seeking");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, staff);
		scn.MoveCardsToSupportArea(seeking);
		scn.MoveCardsToHand(thunder);

		scn.StartGame();

		// An event is offered whenever its requirements and costs are met (Q-0034).
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(thunder));
	}

	@Test
	public void PlayedWithNoShadowPossessionOrArtifactInPlayAsksNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thunder = scn.GetFreepsCard("thunder");
		var gandalf = scn.GetFreepsCard("gandalf");
		var staff = scn.GetFreepsCard("staff");
		var seeking = scn.GetShadowCard("seeking");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, staff);
		scn.MoveCardsToSupportArea(seeking);
		scn.MoveCardsToHand(thunder);

		scn.StartGame();

		int twilight = scn.GetTwilight();
		scn.FreepsPlayCard(thunder);
		assertEquals(twilight + 3, scn.GetTwilight());

		// Played for nothing (CR 4.2 "effect"); a choice with nothing on any branch is not put (F-0116).
		assertInDiscard(thunder);
		assertAttachedTo(staff, gandalf);
		assertFalse(scn.IsHindered(staff));
		assertInZone(Zone.SUPPORT, seeking);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
