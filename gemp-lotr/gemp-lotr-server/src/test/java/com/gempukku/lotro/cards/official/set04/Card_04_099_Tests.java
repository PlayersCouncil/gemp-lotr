package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_099_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("thunder", "4_99");
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: a fellowship ability used only on purpose
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is not Gandalf
					put("staff", "7_38");       // Gandalf's Staff: Free Peoples artifact borne by Gandalf, vitality +1
					put("sword", "1_299");      // Hobbit Sword: Free Peoples possession borne by a Hobbit, no text
					put("poison", "8_28");      // Spider Poison: [gollum] Shadow possession borne by a companion; exerts him when he is assigned
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man minion, no text
					put("axe", "4_26");         // Iron Axe: [dunland] Shadow possession borne by a [dunland] Man, no text
					put("lemenya", "1_232");    // Ulaire Lemenya: Nazgul minion; strength -2 to bearers of [wraith] conditions (none here)
					put("rancor", "9_44");      // Ring of Rancor: [wraith] Shadow artifact borne by a Nazgul, vitality +1; abilities used only on purpose
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
		 * Game Text: <b>Fellowship</b> <i>or</i> <b>Maneuver:</b> Spot Gandalf to discard a Shadow possession or Shadow artifact.
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
	public void InTheManeuverPhaseDiscardsAShadowArtifactOrPossessionWhereverItIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thunder = scn.GetFreepsCard("thunder");
		var gandalf = scn.GetFreepsCard("gandalf");
		var staff = scn.GetFreepsCard("staff");
		var sword = scn.GetFreepsCard("sword");
		var frodo = scn.GetRingBearer();
		var poison = scn.GetShadowCard("poison");
		var brigand = scn.GetShadowCard("brigand");
		var axe = scn.GetShadowCard("axe");
		var lemenya = scn.GetShadowCard("lemenya");
		var rancor = scn.GetShadowCard("rancor");
		var palantir = scn.GetShadowCard("palantir");
		var seeking = scn.GetShadowCard("seeking");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, staff, poison);
		scn.AttachCardsTo(frodo, sword);
		scn.MoveCardsToHand(thunder);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand, lemenya);
		scn.AttachCardsTo(brigand, axe);
		scn.AttachCardsTo(lemenya, rancor);
		scn.MoveCardsToSupportArea(palantir, seeking);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsPlayAvailable(thunder));
		scn.FreepsPlayCard(thunder);

		// Shadow possessions on a minion and on a companion, Shadow artifacts on a minion and in the support area.
		assertTrue(scn.FreepsHasCardChoiceAvailable(axe, poison, rancor, palantir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sword, staff, seeking));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(rancor);

		assertInDiscard(rancor, thunder);
		assertAttachedTo(axe, brigand);
		assertAttachedTo(poison, gandalf);
		assertAttachedTo(staff, gandalf);
		assertAttachedTo(sword, frodo);
		assertInZone(Zone.SUPPORT, palantir, seeking);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void InTheFellowshipPhaseDiscardsAShadowPossessionBorneByACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thunder = scn.GetFreepsCard("thunder");
		var gandalf = scn.GetFreepsCard("gandalf");
		var staff = scn.GetFreepsCard("staff");
		var poison = scn.GetShadowCard("poison");
		var palantir = scn.GetShadowCard("palantir");
		var seeking = scn.GetShadowCard("seeking");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, staff, poison);
		scn.MoveCardsToSupportArea(palantir, seeking);
		scn.MoveCardsToHand(thunder);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(thunder));
		scn.FreepsPlayCard(thunder);

		assertTrue(scn.FreepsHasCardChoiceAvailable(poison, palantir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(staff, seeking));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(poison);

		assertInDiscard(poison, thunder);
		assertInZone(Zone.SUPPORT, palantir, seeking);
		assertAttachedTo(staff, gandalf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsPlayedForNothingWithNoShadowPossessionOrArtifactInPlay() throws DecisionResultInvalidException, CardNotFoundException {
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

		// An event's target is no requirement: it is offered and played for nothing (CR 4.2 "effect").
		assertTrue(scn.FreepsPlayAvailable(thunder));
		scn.FreepsPlayCard(thunder);

		assertInDiscard(thunder);
		assertAttachedTo(staff, gandalf);
		assertInZone(Zone.SUPPORT, seeking);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
