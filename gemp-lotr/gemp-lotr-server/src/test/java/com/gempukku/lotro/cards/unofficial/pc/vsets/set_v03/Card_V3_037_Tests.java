package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_037_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("norearly", "103_37");
					put("savage1", "1_151");    // Uruk Savage: [isengard] Uruk-hai, cost 2, site 5; damage +1 is its whole text
					put("savage2", "1_151");
					put("savage3", "1_151");    // an [isengard] minion to hold in hand
					put("hollowing", "3_54");   // Hollowing of Isengard: [isengard] condition; a Shadow ability never used here
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, cost 1, site 4; adds (2) when played

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] companion
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] companion
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] companion, cost 2, nothing needed to play him
					put("merry", "1_302");      // Merry, Friend to Sam: [shire] companion, cost 1
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion, cost 4
					put("barliman", "1_70");    // Barliman Butterbur: [gandalf] ally, a Man, cost 0, nothing needed to play him
					put("gaffer", "1_291");     // The Gaffer: [shire] ally; a Free Peoples character who is no companion
					put("sword", "1_299");      // Hobbit Sword: possession, cost 1, bearer must be a Hobbit
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NorisHeEarlyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Nor is He Early
		 * Unique: true
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot 2 [isengard] minions.
		 * 	Response: If a Free Peoples character is played, add (3).  If you can spot 5 or more companions
		 * 	(or if that character is [Gandalf]), you may discard an [isengard] card from hand to hinder that
		 * 	character.  Hinder this condition.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("norearly");

		assertEquals("Nor is He Early", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilASecondIsengardMinionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var norearly = scn.GetShadowCard("norearly");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var runner = scn.GetShadowCard("runner");
		var hollowing = scn.GetShadowCard("hollowing");
		scn.MoveCardsToHand(norearly, savage2);
		// An [isengard] card that is no minion, beside the one [isengard] minion and a minion of another culture.
		scn.MoveCardsToSupportArea(hollowing);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, runner);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 10 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(13, scn.GetTwilight());
		assertFalse(scn.ShadowPlayAvailable(norearly));

		scn.ShadowPlayCard(savage2);
		// 13 - 2 to play - 2 roaming (a site 5 minion played at site 2)
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(norearly));
		assertInZone(Zone.HAND, norearly);

		scn.ShadowPlayCard(norearly);
		// It costs nothing.
		assertEquals(9, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, norearly);
		assertFalse(scn.IsHindered(norearly));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ResponseToAFifthCompanionAddsThreeDiscardsAnIsengardCardToHinderHimAndHindersItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var gaffer = scn.GetFreepsCard("gaffer");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		// Frodo and these three are 4 companions; Sam will be the fifth.
		scn.MoveCompanionsToTable(aragorn, gimli, boromir);
		scn.MoveAlliesToTable(gaffer);
		scn.MoveCardsToHand(sam, merry);

		var norearly = scn.GetShadowCard("norearly");
		var savage3 = scn.GetShadowCard("savage3");
		var hollowing = scn.GetShadowCard("hollowing");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(norearly);
		scn.MoveCardsToHand(savage3, hollowing, runner);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.IsHindered(norearly));

		scn.FreepsPlayCard(sam);
		// 2 for Sam
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(norearly));

		scn.ShadowChooseOptionalTrigger(norearly);
		// 2 for Sam + 3 from the response
		assertEquals(5, scn.GetTwilight());
		// Sam is on the table when the response is made, so he is the fifth companion.
		assertTrue(scn.ShadowDecisionAvailable("Would you like to discard"));
		assertEquals(3, scn.GetShadowHandCount());

		scn.ShadowChooseYes();
		// An [isengard] minion and an [isengard] condition may be discarded, a [moria] minion may not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage3, hollowing));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(hollowing);
		assertInZone(Zone.DISCARD, hollowing);
		assertInZone(Zone.HAND, savage3, runner);
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.IsHindered(sam));
		assertFalse(scn.IsHindered(frodo));
		assertFalse(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(gimli));
		assertFalse(scn.IsHindered(boromir));
		assertFalse(scn.IsHindered(gaffer));
		assertTrue(scn.IsHindered(norearly));
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Hindered, the condition answers nothing more.
		scn.FreepsPlayCard(merry);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		// 5 + 1 for Merry
		assertEquals(6, scn.GetTwilight());
		assertFalse(scn.IsHindered(merry));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithFourCompanionsACharacterOfAnotherCultureIsNotHinderedButThreeIsAddedAndItHindersItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gaffer = scn.GetFreepsCard("gaffer");
		var sam = scn.GetFreepsCard("sam");
		// Frodo and these two are 3 companions; Sam will be the fourth. The Gaffer is a fifth Free Peoples
		// character, but an ally. Gandalf is a [gandalf] character on the table, but not the one being played.
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.MoveAlliesToTable(gaffer);
		scn.MoveCardsToHand(sam);

		var norearly = scn.GetShadowCard("norearly");
		var savage3 = scn.GetShadowCard("savage3");
		var hollowing = scn.GetShadowCard("hollowing");
		scn.MoveCardsToSupportArea(norearly);
		scn.MoveCardsToHand(savage3, hollowing);

		scn.StartGame();

		scn.FreepsPlayCard(sam);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(norearly));

		scn.ShadowChooseOptionalTrigger(norearly);
		// 2 for Sam + 3 from the response
		assertEquals(5, scn.GetTwilight());
		assertFalse(scn.ShadowDecisionAvailable("Would you like to discard"));
		assertEquals(2, scn.GetShadowHandCount());
		assertInZone(Zone.HAND, savage3, hollowing);
		assertFalse(scn.IsHindered(sam));
		assertFalse(scn.IsHindered(gandalf));
		assertTrue(scn.IsHindered(norearly));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AGandalfCultureAllyIsHinderedHoweverFewCompanionsThereAre() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		// Barliman Butterbur is a [gandalf] character who is neither named Gandalf nor a Wizard.
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCardsToHand(barliman);

		var norearly = scn.GetShadowCard("norearly");
		var savage3 = scn.GetShadowCard("savage3");
		var hollowing = scn.GetShadowCard("hollowing");
		scn.MoveCardsToSupportArea(norearly);
		scn.MoveCardsToHand(savage3, hollowing);

		scn.StartGame();

		scn.FreepsPlayCard(barliman);
		// Barliman costs nothing.
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(norearly));

		scn.ShadowChooseOptionalTrigger(norearly);
		assertEquals(3, scn.GetTwilight());
		// Frodo is the only companion.
		assertTrue(scn.ShadowDecisionAvailable("Would you like to discard"));

		scn.ShadowChooseYes();
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage3, hollowing));

		scn.ShadowChooseCard(savage3);
		assertInZone(Zone.DISCARD, savage3);
		assertInZone(Zone.HAND, hollowing);
		assertTrue(scn.IsHindered(barliman));
		assertFalse(scn.IsHindered(frodo));
		assertTrue(scn.IsHindered(norearly));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningTheDiscardLeavesGandalfUnhinderedButThreeIsAddedAndItHindersItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCardsToHand(gandalf);

		var norearly = scn.GetShadowCard("norearly");
		var hollowing = scn.GetShadowCard("hollowing");
		scn.MoveCardsToSupportArea(norearly);
		scn.MoveCardsToHand(hollowing);

		scn.StartGame();

		scn.FreepsPlayCard(gandalf);
		// 4 for Gandalf
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(norearly));

		scn.ShadowChooseOptionalTrigger(norearly);
		// 4 for Gandalf + 3 from the response
		assertEquals(7, scn.GetTwilight());
		// Frodo and Gandalf are 2 companions; the offer is made because Gandalf is a [gandalf] character.
		assertTrue(scn.ShadowDecisionAvailable("Would you like to discard"));

		scn.ShadowChooseNo();
		assertInZone(Zone.HAND, hollowing);
		assertEquals(1, scn.GetShadowHandCount());
		assertFalse(scn.IsHindered(gandalf));
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.IsHindered(norearly));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoIsengardCardInHandNoDiscardIsOfferedAndNobodyIsHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir);
		scn.MoveCardsToHand(sam);

		var norearly = scn.GetShadowCard("norearly");
		var runner = scn.GetShadowCard("runner");
		var hollowing = scn.GetShadowCard("hollowing");
		scn.MoveCardsToSupportArea(norearly);
		// A Shadow card of another culture in hand, and an [isengard] card that is not in hand.
		scn.MoveCardsToHand(runner);
		scn.MoveCardsToDiscard(hollowing);

		scn.StartGame();

		scn.FreepsPlayCard(sam);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(norearly));

		scn.ShadowChooseOptionalTrigger(norearly);
		// 2 for Sam + 3 from the response
		assertEquals(5, scn.GetTwilight());
		assertFalse(scn.ShadowDecisionAvailable("Would you like to discard"));
		assertInZone(Zone.HAND, runner);
		assertEquals(1, scn.GetShadowHandCount());
		assertFalse(scn.IsHindered(sam));
		assertTrue(scn.IsHindered(norearly));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void APossessionOrAMinionPlayedIsNotAnsweredAndADeclinedResponseIsOfferedForTheNextCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sword = scn.GetFreepsCard("sword");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCardsToHand(sword, sam, merry);

		var norearly = scn.GetShadowCard("norearly");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(norearly);
		scn.MoveCardsToHand(runner);

		scn.StartGame();

		// A Free Peoples card that is no character.
		scn.FreepsPlayCard(sword);
		assertAttachedTo(sword, frodo);
		assertEquals(1, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(sam);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(norearly));

		scn.ShadowDeclineOptionalTrigger();
		// 1 for the sword + 2 for Sam, and nothing from the response
		assertEquals(3, scn.GetTwilight());
		assertFalse(scn.IsHindered(norearly));
		assertFalse(scn.IsHindered(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Not having been used, it answers the next character too.
		scn.FreepsPlayCard(merry);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(norearly));

		scn.ShadowDeclineOptionalTrigger();
		// 3 + 1 for Merry
		assertEquals(4, scn.GetTwilight());
		assertFalse(scn.IsHindered(norearly));

		scn.FreepsPass();
		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 4 + 3 companions + 2 for the shadow number of site 2
		assertEquals(9, scn.GetTwilight());

		// A character, but not a Free Peoples one.
		scn.ShadowPlayCard(runner);
		// 9 - 1 to play - 2 roaming (a site 4 minion played at site 2) + 2 added by Goblin Runner's own text
		assertEquals(8, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.IsHindered(norearly));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
