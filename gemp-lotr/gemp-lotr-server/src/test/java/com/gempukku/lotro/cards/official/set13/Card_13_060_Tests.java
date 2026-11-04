package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_060_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wind", "13_60");
					put("wind2", "13_60");
					put("faramir", "4_117");   // Faramir, Son of Denethor: passive text only
					put("boromir", "1_97");    // Boromir, Son of Denethor: activated text only
					put("denethor", "7_86");   // Denethor, Wizened Steward: passive text only
					put("aragorn", "1_89");    // Aragorn, Ranger of the North: another [gondor] Man
					put("troop", "1_177");     // Goblin Patrol Troop, str 13: beats a 7-str companion without overwhelming

					// The same three titles under other subtitles, one of each title to a test:
					put("prince", "13_66");    // Faramir, Prince of Ithilien: vitality 3; his texts are about his cost and possessions
					// Faramir, Dúnadan of Gondor: "Skirmish: Exert Faramir to make him strength +1 for each wound on the
					// Ring-bearer"
					put("dunadan", "12_47");
					put("heir", "13_62");      // Boromir, Doomed Heir: a skirmish ability used only on purpose
					put("steward", "13_64");   // Denethor, Last Ruling Steward: a maneuver ability that needs a [gondor] possession
					// Boromir, My Brother: a condition Faramir bears (vitality +1) whose title is all five words, so it
					// is no card titled Boromir; its skirmish ability needs a [gondor] card in hand
					put("brother", "4_111");
					put("marksman", "1_176");  // Goblin Marksman: an archer and nothing else
					put("party", "1_158");     // Uruk-hai Raiding Party: strength 9, damage +1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AwayontheWindStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Away on the Wind
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Response:</b> If Faramir is about to take a wound, discard this condition from play and place Boromir or Denethor in the dead pile from play to prevent that and heal Faramir twice.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wind");

		assertEquals("Away on the Wind", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ResponsePreventsWoundOnFaramirByKillingBoromirOrDenethor() throws DecisionResultInvalidException, CardNotFoundException {
		// Regression for #1078: the kill filter was written as name(Boromir) AND name(Denethor), so the cost could
		// never be paid and the response was never offered.
		var scn = GetScenario();

		var wind = scn.GetFreepsCard("wind");
		var faramir = scn.GetFreepsCard("faramir");
		var boromir = scn.GetFreepsCard("boromir");
		var denethor = scn.GetFreepsCard("denethor");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(faramir, boromir, denethor, aragorn);
		scn.MoveCardsToSupportArea(wind);
		scn.AddWoundsToChar(faramir, 2);

		var troop = scn.GetShadowCard("troop");
		scn.MoveMinionsToTable(troop);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(faramir, troop);
		scn.FreepsResolveSkirmish(faramir);
		scn.PassSkirmishActions();

		// Faramir (7) loses to the Troop (13) and is about to take a lethal third wound
		assertEquals(2, scn.GetWoundsOn(faramir));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(wind));
		scn.FreepsAcceptOptionalTrigger();

		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, denethor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, faramir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(boromir);

		assertInZone(Zone.DISCARD, wind);
		assertInZone(Zone.DEAD, boromir);
		assertInZone(Zone.FREE_CHARACTERS, denethor);
		assertInZone(Zone.FREE_CHARACTERS, faramir);
		assertEquals(0, scn.GetWoundsOn(faramir));   // wound prevented, then healed twice
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ResponseIsNotOfferedWithoutBoromirOrDenethorInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var wind = scn.GetFreepsCard("wind");
		var faramir = scn.GetFreepsCard("faramir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var denethor = scn.GetFreepsCard("denethor");
		var brother = scn.GetFreepsCard("brother");
		scn.MoveCompanionsToTable(faramir, aragorn);
		scn.AttachCardsTo(faramir, brother);
		scn.MoveCardsToSupportArea(wind);
		// A Boromir and a Denethor there are, but neither is in play.
		scn.MoveCardsToDeadPile(boromir);

		var troop = scn.GetShadowCard("troop");
		scn.MoveMinionsToTable(troop);

		scn.StartGame();
		scn.MoveCardsToHand(denethor);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(faramir, troop);
		scn.FreepsResolveSkirmish(faramir);
		scn.PassSkirmishActions();

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(wind));
		assertEquals(1, scn.GetWoundsOn(faramir));
		assertInZone(Zone.SUPPORT, wind);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ResponseIsNotOfferedWhenSomeoneOtherThanFaramirIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var wind = scn.GetFreepsCard("wind");
		var faramir = scn.GetFreepsCard("faramir");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(faramir, boromir, aragorn);
		scn.MoveCardsToSupportArea(wind);

		var troop = scn.GetShadowCard("troop");
		scn.MoveMinionsToTable(troop);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(wind));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SUPPORT, wind);
		assertInZone(Zone.FREE_CHARACTERS, boromir);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnArcheryWoundIsPreventedAndTwoOfThreeWoundsHealedByTheCopyUsedWithDenethorPlacedInTheDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var wind = scn.GetFreepsCard("wind");
		var wind2 = scn.GetFreepsCard("wind2");
		var prince = scn.GetFreepsCard("prince");
		var brother = scn.GetFreepsCard("brother");
		var heir = scn.GetFreepsCard("heir");
		var steward = scn.GetFreepsCard("steward");
		scn.MoveCompanionsToTable(prince, heir, steward);
		scn.AttachCardsTo(prince, brother);
		scn.MoveCardsToSupportArea(wind, wind2);

		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		// Vitality 3 and 1 more for Boromir, My Brother: three wounds leave him one from death.
		scn.AddWoundsToChar(prince, 3);
		scn.MoveMinionsToTable(marksman);
		scn.SkipToArcheryWounds();

		// The Marksman's one archery wound is the Free Peoples player's to place, and he places it on Faramir.
		scn.FreepsChooseCard(prince);
		assertEquals(3, scn.GetWoundsOn(prince));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(wind));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(wind2));

		scn.FreepsChooseOptionalTrigger(wind);

		// A card titled Boromir or one titled Denethor: not the condition "Boromir, My Brother", nor anyone else.
		assertTrue(scn.FreepsHasCardChoiceAvailable(heir, steward));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(brother, prince, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(steward);

		// The copy used is discarded and the other stays; Denethor is dead and Boromir untouched.
		assertInZone(Zone.DISCARD, wind);
		assertInZone(Zone.SUPPORT, wind2);
		assertInZone(Zone.DEAD, steward);
		assertInZone(Zone.FREE_CHARACTERS, heir);
		// The wound is prevented and two of his three are healed.
		assertEquals(1, scn.GetWoundsOn(prince));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void OneWoundIsPreventedSoTheSecondWoundOfALostSkirmishIsOfferedToTheOtherCopyAndAnExertionToNeither() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wind = scn.GetFreepsCard("wind");
		var wind2 = scn.GetFreepsCard("wind2");
		var dunadan = scn.GetFreepsCard("dunadan");
		var boromir = scn.GetFreepsCard("boromir");
		var denethor = scn.GetFreepsCard("denethor");
		scn.MoveCompanionsToTable(dunadan, boromir, denethor);
		scn.MoveCardsToSupportArea(wind, wind2);

		var party = scn.GetShadowCard("party");

		scn.StartGame();
		scn.MoveMinionsToTable(party);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(dunadan, party);
		scn.FreepsResolveSkirmish(dunadan);

		// Faramir exerts by his own ability (strength +0, the Ring-bearer being unwounded): an exertion is no wound.
		assertEquals(0, scn.GetWoundsOn(dunadan));
		scn.FreepsUseCardAction(dunadan);
		assertEquals(1, scn.GetWoundsOn(dunadan));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(wind));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(wind2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// He (7) loses to the Raiding Party (9, damage +1) and is about to take the first of two wounds.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(wind));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(wind2));
		scn.FreepsChooseOptionalTrigger(wind);
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, denethor));
		scn.FreepsChooseCard(boromir);

		assertInZone(Zone.DEAD, boromir);
		assertInZone(Zone.DISCARD, wind);
		// That wound is prevented, and the one wound he had is healed.
		assertEquals(0, scn.GetWoundsOn(dunadan));

		// The second wound is another wound: the copy still in play offers again, and declined, the wound lands.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(wind2));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(dunadan));
		assertInZone(Zone.SUPPORT, wind2);
		assertInZone(Zone.FREE_CHARACTERS, denethor);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
