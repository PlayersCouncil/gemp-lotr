package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_047_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("voice", "3_47");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor], Gandalf signet; a maneuver ability used only on purpose
					put("rohanaragorn", "17_93"); // Aragorn, Defender of Rohan: the [rohan] Aragorn, no signet; his hunter needs the Ring-bearer assigned
					put("boromirson", "1_97");  // Boromir, Son of Denethor: [gondor] companion who is no Aragorn, Frodo signet; a skirmish ability used only on purpose

					// Companions with the Aragorn signet, of three cultures and races.
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf, strength 6; stronger only against a Nazgul
					put("boromir", "1_96");     // Boromir, Lord of Gondor: [gondor] Man, strength 7
					put("merry", "1_303");      // Merry, From O'er the Brandywine: [shire] Hobbit; stronger only bearing a weapon

					// Eomer, Eored Leader: [rohan] companion, no signet.  "Maneuver: If you can spot more minions than
					// companions, exert Eomer to play a [rohan] companion from your draw deck."
					put("eomer", "12_112");
					put("theoden", "8_92");     // Theoden, Tall and Proud: [rohan] companion with the Aragorn signet; a killed trigger only

					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1; its play trigger is never set off
					put("runner2", "1_178");
					put("savage1", "1_151");    // Uruk Savage: damage +1 and nothing else
					put("savage2", "1_151");
					put("troop", "1_177");      // Goblin Patrol Troop: no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void VoiceofRaurosStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Voice of Rauros
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Exert Aragorn to make each companion with the Aragorn signet <b>damage +1</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("voice");

		assertEquals("Voice of Rauros", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAragornToMakeEachCompanionWithTheAragornSignetDamagePlus1UntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var voice = scn.GetFreepsCard("voice");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		var frodo = scn.GetRingBearer();
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(aragorn, arwen, boromir, merry);
		scn.MoveCardsToHand(voice);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetKeywordCount(arwen, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(merry, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(frodo, Keyword.DAMAGE));
		assertTrue(scn.FreepsPlayAvailable(voice));

		scn.FreepsPlayCard(voice);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInDiscard(voice);
		// The Aragorn signet in three cultures and races.
		assertEquals(1, scn.GetKeywordCount(arwen, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(merry, Keyword.DAMAGE));
		// Aragorn has the Gandalf signet, Frodo the Frodo signet.
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(frodo, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, runner1}, new PhysicalCardImpl[]{arwen, runner2});
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();

		// Still in force in the skirmish that follows one already resolved...
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(arwen);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetKeywordCount(arwen, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(merry, Keyword.DAMAGE));
		scn.PassSkirmishActions();

		// ...and gone in the regroup phase.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetKeywordCount(arwen, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(merry, Keyword.DAMAGE));
	}

	@Test
	public void TheRohanAragornPaysTheExertion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var voice = scn.GetFreepsCard("voice");
		var rohanaragorn = scn.GetFreepsCard("rohanaragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(rohanaragorn, boromir);
		scn.MoveCardsToHand(voice);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(rohanaragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertTrue(scn.FreepsPlayAvailable(voice));

		scn.FreepsPlayCard(voice);
		assertEquals(1, scn.GetWoundsOn(rohanaragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		// He has no signet.
		assertEquals(0, scn.GetKeywordCount(rohanaragorn, Keyword.DAMAGE));
		assertInDiscard(voice);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileAragornIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var voice = scn.GetFreepsCard("voice");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromirson = scn.GetFreepsCard("boromirson");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(aragorn, boromirson);
		scn.MoveCardsToHand(voice);

		scn.StartGame();
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveMinionsToTable(runner1);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(3, scn.GetWoundsOn(aragorn));
		// Another [gondor] companion who could exert is no Aragorn.
		assertEquals(0, scn.GetWoundsOn(boromirson));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(voice));

		scn.RemoveWoundsFromChar(aragorn, 1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(voice));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ACompanionWithTheAragornSignetPlayedAfterTheEventIsNotDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var voice = scn.GetFreepsCard("voice");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var eomer = scn.GetFreepsCard("eomer");
		var theoden = scn.GetFreepsCard("theoden");
		scn.MoveCompanionsToTable(aragorn, boromir, eomer);
		scn.MoveCardsToHand(voice);

		scn.StartGame();
		// Five minions against four companions, so that Eomer's ability can be used.
		scn.MoveMinionsToTable(scn.GetShadowCard("runner1"), scn.GetShadowCard("runner2"),
				scn.GetShadowCard("savage1"), scn.GetShadowCard("savage2"), scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.DECK, theoden);
		scn.FreepsPlayCard(voice);
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(eomer, Keyword.DAMAGE));
		scn.ShadowPass();

		assertEquals(0, scn.GetWoundsOn(eomer));
		assertTrue(scn.FreepsActionAvailable(eomer));
		scn.FreepsUseCardAction(eomer);
		assertEquals(1, scn.GetWoundsOn(eomer));
		// The deck is shown first, then a [rohan] companion is chosen from it.
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(theoden);

		// Theoden has the Aragorn signet, but he was not in play when the event was played.
		assertInZone(Zone.FREE_CHARACTERS, theoden);
		assertEquals(0, scn.GetKeywordCount(theoden, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
