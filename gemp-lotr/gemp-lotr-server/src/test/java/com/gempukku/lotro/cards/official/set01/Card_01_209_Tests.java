package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_209_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tip", "1_209");
					put("tip2", "1_209");
					put("nelya", "2_84");       // Úlairë Nelya, Ringwraith in Twilight: [wraith] Nazgûl, strength 10; nothing below 2 burdens or 2 wounds on the Ring-bearer
					put("khamul", "103_61");    // Úlairë Attëa, Khamûl of the East: a [raider] Nazgûl, strength 12, fierce; nothing below 3 burdens
					put("creeper", "8_75");     // Morgul Creeper: a [wraith] minion who is an Orc and no Nazgûl, strength 8; nothing below 3 burdens
					put("runner", "1_178");     // Goblin Runner: a minion of strength 5 to stand beside the Nazgûl on the winning side
					put("breath", "1_207");     // Black Breath: another [wraith] condition a character may bear
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_96");     // Boromir, Lord of Gondor: strength 7, not overwhelmed unless his strength is tripled
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: an ally, strength 8, vitality 4, home site 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BladeTipStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Blade Tip
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype:
		 * Game Text: Plays to your support area.<br><b>Response:</b> If your Nazgûl wins a skirmish, transfer this condition from your support area to the losing character. Limit 1 per character. Wound bearer at the start of each fellowship phase. (If bearer is the Ring-bearer, add a burden instead.)
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("tip");

		assertEquals("Blade Tip", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenYourNazgulWinsTheCopyInYourSupportAreaIsTransferredToTheLosingCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tip = scn.GetShadowCard("tip");
		var tip2 = scn.GetShadowCard("tip2");
		var khamul = scn.GetShadowCard("khamul");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();
		scn.MoveCardsToSupportArea(tip);
		// A second copy already borne: not in the support area.
		scn.AttachCardsTo(boromir, tip2);
		scn.MoveMinionsToTable(khamul);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, khamul);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// 12 against 8: the Nazgûl wins, Aragorn takes his wound.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(tip));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(tip2));

		scn.ShadowChooseOptionalTrigger(tip);

		assertAttachedTo(tip, aragorn);
		assertAttachedTo(tip2, boromir);
		// Khamûl is fierce: the fierce assignments come next.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheLosingAllyIsGivenItAndNotAMinionOnTheWinningSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tip = scn.GetShadowCard("tip");
		var nelya = scn.GetShadowCard("nelya");
		var runner = scn.GetShadowCard("runner");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToSupportArea(tip);
		scn.FreepsPass();
		// Site 3: Elrond's home, where he may be assigned.
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(nelya, runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elrond, nelya);
		scn.ShadowAssignToMinions(elrond, runner);
		scn.FreepsResolveSkirmish(elrond);
		scn.PassSkirmishActions();

		// 10 + 5 against 8: not doubled, so Elrond loses and takes a wound.
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(tip));

		scn.ShadowChooseOptionalTrigger(tip);

		// No choice is asked: the Goblin Runner won beside the Nazgûl and is no losing character.
		assertAttachedTo(tip, elrond);
		assertNotAttachedTo(tip, runner);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenAWraithMinionWhoIsNoNazgulWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tip = scn.GetShadowCard("tip");
		var creeper = scn.GetShadowCard("creeper");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);

		scn.StartGame();
		scn.MoveCardsToSupportArea(tip);
		scn.MoveMinionsToTable(creeper);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, creeper);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();

		// 8 against 7: the Orc wins.
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(tip));
		assertInZone(Zone.SUPPORT, tip);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACharacterBearingBladeTipIsNotGivenASecondAndOneBearingAnotherWraithConditionIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tip = scn.GetShadowCard("tip");
		var tip2 = scn.GetShadowCard("tip2");
		var breath = scn.GetShadowCard("breath");
		var nelya = scn.GetShadowCard("nelya");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveCardsToSupportArea(tip, tip2);
		scn.AttachCardsTo(aragorn, breath);
		scn.MoveMinionsToTable(nelya);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// 10 against 8. Black Breath on him does not count against the limit: both copies are offered.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(tip));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(tip2));

		scn.ShadowChooseOptionalTrigger(tip);

		assertAttachedTo(tip, aragorn);
		// He bears one now: the other copy is not offered for him.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(tip2));
		assertInZone(Zone.SUPPORT, tip2);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WoundsItsBearerAtTheStartOfEachOfHisFellowshipPhases() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tip = scn.GetShadowCard("tip");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(sam, aragorn);

		scn.StartGame();
		scn.AttachCardsTo(sam, tip);

		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.FreepsGetBurdens());

		// The rest of this turn's phases, and the other player's turn, on which the card is not active.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(0, scn.GetWoundsOn(sam));

		scn.SkipToOtherPlayersTurn();

		// The start of his next fellowship phase: Sam, who is Ring-bound but not the Ring-bearer, is wounded,
		// and no burden is added.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.FreepsGetBurdens());

		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();

		// Two turns on the fellowship starts at site 3, a sanctuary: the heals are declined.
		assertTrue(scn.FreepsDecisionAvailable("Sanctuary healing"));
		scn.FreepsDeclineSanctuaryHealing();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.FreepsGetBurdens());
	}

	@Test
	public void OnTheRingBearerItAddsABurdenInsteadOfAWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tip = scn.GetShadowCard("tip");
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.AttachCardsTo(frodo, tip);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.FreepsGetBurdens());

		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.FreepsGetBurdens());
	}
}
