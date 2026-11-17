package com.gempukku.lotro.cards.official.set19;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_19_031_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stew1", "19_31");
					put("stew2", "19_31");

					// Fellowship companions (set 19's keyword), of two cultures and races
					put("sam", "19_32");        // Sam, Loyal Friend: [shire] Hobbit; strength +2 while you can spot 3 [shire] companions
					put("gimli", "19_2");       // Gimli, Opinionated Guide: [dwarven] Dwarf; "may draw" when a [dwarven] possession is played
					// Companions without the keyword
					put("merry", "1_303");      // Merry, From O'er the Brandywine: [shire] Hobbit, strength +2 while he bears a weapon
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man; a maneuver ability only
					put("pippin", "103_31");    // Pippin, Trollslayer: a Hobbit companion who is [gondor]; a skirmish ability only
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally, no companion; a skirmish ability used only on purpose

					put("sword1", "1_299");     // Hobbit Sword: hand weapon, strength +2, on a Hobbit; no other text
					put("sword2", "1_299");

					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1; its text is about being played
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RabbitStewStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: Rabbit Stew
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: 
		 * Game Text: Bearer must be a [shire] companion.<br>Each time bearer wins a skirmish, you may transfer this condition to a fellowship companion.<br>Each time this condition is transferred to a companion, heal that companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stew1");

		assertEquals("Rabbit Stew", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnAShireCompanionOnlyAndPlayingItHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stew = scn.GetFreepsCard("stew1");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(sam, pippin, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(stew);

		scn.StartGame();
		scn.AddWoundsToChar(sam, 1);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertInHand(stew);
		assertTrue(scn.FreepsPlayAvailable(stew));

		scn.FreepsPlayCard(stew);
		// The Ring-bearer and Sam are [shire] companions; Pippin is a Hobbit of [gondor], Aragorn a companion of
		// another culture, Bounder a [shire] Hobbit who is an ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, aragorn, bounder));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(sam);

		assertAttachedTo(stew, sam);
		// Played, not transferred: Sam keeps his wound.
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// Merry, 3 + 2 for the Sword + 2 while he bears a weapon, and Sam, 3 + 2 for the Sword + 2 with three [shire]
	// companions (Frodo, Merry, Sam), each beat a Goblin Runner of 5.
	@Test
	public void EachTimeBearerWinsItMayGoToAFellowshipCompanionWhoIsHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stew1 = scn.GetFreepsCard("stew1");
		var stew2 = scn.GetFreepsCard("stew2");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var sword1 = scn.GetFreepsCard("sword1");
		var sword2 = scn.GetFreepsCard("sword2");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(merry, sam, gimli, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.AttachCardsTo(merry, sword1, stew1);
		scn.AttachCardsTo(sam, sword2);
		// A second copy on the Ring-bearer, to show which copy moves and heals.
		scn.AttachCardsTo(frodo, stew2);

		scn.StartGame();
		scn.AddWoundsToChar(merry, 1);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(sam, 2);
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(aragorn, 1);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertAttachedTo(stew1, merry);
		assertAttachedTo(stew2, frodo);
		assertAttachedTo(sword1, merry);
		assertAttachedTo(sword2, sam);
		scn.FreepsPass();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{merry, runner1},
				new PhysicalCardImpl[]{sam, runner2});
		scn.FreepsResolveSkirmish(merry);
		assertEquals(7, scn.GetStrength(merry));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();

		// Merry has won: his copy offers itself, the Ring-bearer's does not.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stew1));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(stew2));
		scn.FreepsAcceptOptionalTrigger();
		// Fellowship companions only: Sam and Gimli; not Merry, who bears it, Aragorn or the Ring-bearer, who have no
		// Fellowship keyword, nor Bounder, an ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, aragorn, frodo, bounder));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(sam);

		assertAttachedTo(stew1, sam);
		assertAttachedTo(stew2, frodo);
		// Sam, the companion it went to, is healed once; nobody else is.
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Sam, its bearer now, wins his own skirmish: it may go on again, to Gimli of another culture.
		scn.FreepsResolveSkirmish(sam);
		assertEquals(7, scn.GetStrength(sam));
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stew1));
		// Gimli is the one Fellowship companion who does not bear it, and it goes to him unasked.
		scn.FreepsAcceptOptionalTrigger();

		assertAttachedTo(stew1, gimli);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DeclinedItStaysAndAnotherShireCompanionWinningDoesNotOfferIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stew = scn.GetFreepsCard("stew1");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		var sword1 = scn.GetFreepsCard("sword1");
		var sword2 = scn.GetFreepsCard("sword2");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(merry, sam, gimli);
		scn.AttachCardsTo(merry, sword1, stew);
		scn.AttachCardsTo(sam, sword2);

		scn.StartGame();
		scn.AddWoundsToChar(sam, 1);
		scn.AddWoundsToChar(gimli, 1);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertAttachedTo(stew, merry);
		assertAttachedTo(sword1, merry);
		assertAttachedTo(sword2, sam);
		scn.FreepsPass();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{merry, runner1},
				new PhysicalCardImpl[]{sam, runner2});
		scn.FreepsResolveSkirmish(merry);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(gimli));
		scn.PassSkirmishActions();

		// Merry has won, and the offer is declined: nothing moves and nobody is healed.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stew));
		scn.FreepsDeclineOptionalTrigger();
		assertAttachedTo(stew, merry);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(gimli));

		// Sam, a [shire] companion who does not bear it, wins his skirmish: no offer.
		scn.FreepsResolveSkirmish(sam);
		assertEquals(7, scn.GetStrength(sam));
		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(stew));
		assertAttachedTo(stew, merry);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenBearerLosesASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stew = scn.GetFreepsCard("stew1");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(merry, sam);
		scn.AttachCardsTo(merry, stew);

		scn.StartGame();
		scn.AddWoundsToChar(sam, 1);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertAttachedTo(stew, merry);
		scn.FreepsPass();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, runner1);
		scn.FreepsResolveSkirmish(merry);
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(sam));

		// Merry, 3 with no weapon, loses to the Runner's 5 and lives: no offer, though Sam is there to take it.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(merry));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(stew));
		assertAttachedTo(stew, merry);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenNoOtherFellowshipCompanionCanTakeIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stew = scn.GetFreepsCard("stew1");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword2 = scn.GetFreepsCard("sword2");
		var runner1 = scn.GetShadowCard("runner1");
		// Sam is the only Fellowship companion, and he bears it.
		scn.MoveCompanionsToTable(sam, merry, aragorn);
		scn.AttachCardsTo(sam, sword2, stew);

		scn.StartGame();
		scn.AddWoundsToChar(merry, 1);
		scn.AddWoundsToChar(aragorn, 1);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertAttachedTo(stew, sam);
		assertAttachedTo(sword2, sam);
		scn.FreepsPass();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, runner1);
		scn.FreepsResolveSkirmish(sam);
		assertEquals(7, scn.GetStrength(sam));

		// Sam wins; nobody else may take it, so a yes would do nothing and it is not offered.
		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(stew));
		assertAttachedTo(stew, sam);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
