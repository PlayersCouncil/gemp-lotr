package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_067_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("plotting", "7_67");
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: [gollum] Gollum, strength 5; a regroup ability used only on purpose
					put("wraithgollum", "103_66"); // Gollum, Half a Wraith Himself: [wraith] Gollum, strength 5; his texts are used only on purpose
					put("shelob", "8_26");      // Shelob, Last Child of Ungoliant: a [gollum] minion who is no Gollum, strength 8; fierce, enduring
					put("ambition", "1_133");   // Saruman's Ambition: another Shadow condition in the support area; a cost bonus and a skirmish ability

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion; a maneuver ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: a companion; abilities used only on purpose
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a companion; a strength bonus only
					put("boromir", "1_97");     // Boromir, Son of Denethor: a companion; a skirmish ability used only on purpose
					put("legolas", "1_50");     // Legolas, Son of Thranduil: a companion; abilities used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: a companion; abilities used only on purpose
					put("merry", "1_303");      // Merry, Friend to Frodo: a companion; abilities used only on purpose
					put("pippin", "1_306");     // Pippin, Hobbit of the Shire: a companion; abilities used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PlottingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Plotting
		 * Unique: True
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 5
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot Gollum and add up to 9 threats.<br>Gollum is strength +2.<br>Discard this condition and remove 9 threats at the start of the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("plotting");

		assertEquals("Plotting", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(5, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilGollumIsSpottedThenAddsTheThreatsChosenAndIsDiscardedRemovingThemAtTheStartOfTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		var plotting = scn.GetShadowCard("plotting");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		var ambition = scn.GetShadowCard("ambition");
		scn.MoveCardsToHand(plotting, gollum);
		scn.MoveCardsToSupportArea(ambition);
		assertInHand(plotting, gollum);
		assertInZone(Zone.SUPPORT, ambition);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob);
		// Frodo, Aragorn and Sam: room for 3 threats; one is laid already, so 2 can be added.
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// A [gollum] minion, but no Gollum.
		assertFalse(scn.ShadowPlayAvailable(plotting));

		scn.ShadowPlayCard(gollum);
		assertEquals(5, scn.GetStrength(gollum));
		assertEquals(8, scn.GetStrength(shelob));
		assertTrue(scn.ShadowPlayAvailable(plotting));

		scn.ShadowPlayCard(plotting);
		// Up to 9, from none, and no more than the threats that can still be added.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("2");

		assertInZone(Zone.SUPPORT, plotting);
		assertEquals(3, scn.GetThreats());
		// Gollum alone is strength +2.
		assertEquals(7, scn.GetStrength(gollum));
		assertEquals(8, scn.GetStrength(shelob));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToAssignments();
		// Through the starts of the maneuver, archery and assignment phases it stays, and so do the threats.
		assertInZone(Zone.SUPPORT, plotting);
		assertEquals(3, scn.GetThreats());
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		// Shelob is fierce: a fierce assignment phase, declined as well.
		scn.PassCurrentPhaseActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		// At the start of the regroup phase it is discarded and every threat is removed, those it did not add as well;
		// another Shadow condition stays, and Gollum, still in play, is back to 5.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInDiscard(plotting);
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.SUPPORT, ambition);
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertEquals(5, scn.GetStrength(gollum));
	}

	@Test
	public void TheWraithGollumIsSpottedAndStrengthenedAndWithNoThreatsAddedItIsStillDiscardedAtTheStartOfTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var plotting = scn.GetShadowCard("plotting");
		var gollum = scn.GetShadowCard("wraithgollum");
		scn.MoveCardsToHand(plotting);
		assertInHand(plotting);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertEquals(0, scn.GetThreats());
		assertEquals(5, scn.GetStrength(gollum));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(plotting));

		scn.ShadowPlayCard(plotting);
		// Frodo and Aragorn: room for 2.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("0");

		assertInZone(Zone.SUPPORT, plotting);
		assertEquals(0, scn.GetThreats());
		assertEquals(7, scn.GetStrength(gollum));

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInDiscard(plotting);
		assertEquals(0, scn.GetThreats());
		assertEquals(5, scn.GetStrength(gollum));
	}

	@Test
	public void NineCompanionsLetNineThreatsBeAddedAndTheStartOfTheRegroupPhaseRemovesAllNine() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var plotting = scn.GetShadowCard("plotting");
		var gollum = scn.GetShadowCard("wraithgollum");
		// Frodo and eight more: the most companions there can be, so the most threats there can be.
		scn.MoveCompanionsToTable("aragorn", "sam", "gandalf", "boromir", "legolas", "gimli", "merry", "pippin");
		scn.MoveCardsToHand(plotting);
		assertInHand(plotting);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertEquals(0, scn.GetThreats());
		assertEquals(9, scn.GetThreatLimit());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(plotting));

		scn.ShadowPlayCard(plotting);
		// Up to 9 is the whole of what can be added here.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(9, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("9");

		assertInZone(Zone.SUPPORT, plotting);
		assertEquals(9, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		// "Remove 9": all nine go, not eight.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInDiscard(plotting);
		assertEquals(0, scn.GetThreats());
	}
}
