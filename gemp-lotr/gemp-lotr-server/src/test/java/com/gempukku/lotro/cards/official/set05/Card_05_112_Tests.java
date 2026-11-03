package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_112_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nohelp", "5_112");
					put("sam", "1_311");            // Sam, Son of Hamfast: [shire] Hobbit companion; abilities used only on purpose
					put("merry", "1_303");          // Merry, From O'er the Brandywine: a [shire] Hobbit companion of another title
					// Sméagol, Poor Creature: [gollum] companion; a skirmish ability used only on purpose
					put("poorcreature", "6_45");
					// Gollum, Half a Wraith Himself: [wraith] minion; a trigger on a Nazgûl played and an assignment
					// ability, neither reached
					put("wraithgollum", "103_66");
					put("gollum", "5_24");          // Gollum, Nasty Treacherous Creature: [gollum] minion; a regroup ability used only on purpose
					// Shelob, Last Child of Ungoliant: [gollum] minion of another title; enduring, fierce
					put("shelob", "8_26");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSamScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nohelp", "5_112");
					// Smeagol, Once Like Us: [shire] companion; an assignment ability used only on purpose
					put("oncelikeus", "103_116");
					put("gollum", "5_24");          // Gollum, Nasty Treacherous Creature: [gollum] minion; a regroup ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.SamRB,         // Sam, Bearer of Great Need: Sam as the Ring-bearer
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NoHelpforItStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: No Help for It
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: <b>Regroup:</b> Spot Sam and discard Sméagol or Gollum to remove 3 burdens.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("nohelp");

		assertEquals("No Help for It", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpotsSamAndDiscardsSmeagolToRemoveThreeBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nohelp = scn.GetFreepsCard("nohelp");
		var sam = scn.GetFreepsCard("sam");
		var poorcreature = scn.GetFreepsCard("poorcreature");
		scn.MoveCompanionsToTable(sam, poorcreature);
		scn.MoveCardsToHand(nohelp);

		var wraithgollum = scn.GetShadowCard("wraithgollum");
		var shelob = scn.GetShadowCard("shelob");

		scn.StartGame();
		scn.AddBurdens(4);
		assertEquals(4, scn.GetBurdens());
		scn.MoveMinionsToTable(wraithgollum, shelob);

		// Nobody is assigned, so both minions are still in play in the regroup phase.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		// Shelob is fierce: the fierce assignment phase is declined too.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(4, scn.GetBurdens());
		assertInZone(Zone.SHADOW_CHARACTERS, wraithgollum, shelob);
		assertTrue(scn.FreepsPlayAvailable(nohelp));
		scn.FreepsPlayCard(nohelp);

		assertEquals(1, scn.FreepsGetChoiceMin());
		// A Sméagol, and a Gollum of the other player's.
		assertTrue(scn.FreepsHasCardChoiceAvailable(poorcreature, wraithgollum));
		// A [gollum] minion of another title, and Sam.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(shelob, sam));
		scn.FreepsChooseCard(poorcreature);

		assertInZone(Zone.DISCARD, poorcreature, nohelp);
		// 4 - 3
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.SHADOW_CHARACTERS, wraithgollum, shelob);
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void SpotsSamTheRingBearerAndDiscardsGollumToRemoveTheBurdensThereAre() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSamScenario();

		var nohelp = scn.GetFreepsCard("nohelp");
		var oncelikeus = scn.GetFreepsCard("oncelikeus");
		scn.MoveCompanionsToTable(oncelikeus);
		scn.MoveCardsToHand(nohelp);

		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		scn.MoveMinionsToTable(gollum);

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.FreepsPlayAvailable(nohelp));
		scn.FreepsPlayCard(nohelp);

		// The [shire] Sméagol is offered beside Gollum.
		assertTrue(scn.FreepsHasCardChoiceAvailable(oncelikeus, gollum));
		scn.FreepsChooseCard(gollum);

		assertInZone(Zone.DISCARD, gollum, nohelp);
		assertInZone(Zone.FREE_CHARACTERS, oncelikeus);
		// Only 2 to remove.
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedWithNoSmeagolOrGollumToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nohelp = scn.GetFreepsCard("nohelp");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(nohelp);

		var shelob = scn.GetShadowCard("shelob");

		scn.StartGame();
		assertInZone(Zone.HAND, nohelp);
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		scn.MoveMinionsToTable(shelob);

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		// Shelob is fierce: the fierce assignment phase is declined too.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, shelob);
		assertInZone(Zone.HAND, nohelp);
		assertFalse(scn.FreepsPlayAvailable(nohelp));
	}

	@Test
	public void CannotBePlayedWithoutSamThoughAnotherShireCompanionIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nohelp = scn.GetFreepsCard("nohelp");
		var merry = scn.GetFreepsCard("merry");
		var poorcreature = scn.GetFreepsCard("poorcreature");
		scn.MoveCompanionsToTable(merry, poorcreature);
		scn.MoveCardsToHand(nohelp);

		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		assertInZone(Zone.HAND, nohelp);
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		scn.MoveMinionsToTable(gollum);

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.FREE_CHARACTERS, merry, poorcreature);
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertInZone(Zone.HAND, nohelp);
		assertFalse(scn.FreepsPlayAvailable(nohelp));
	}
}
