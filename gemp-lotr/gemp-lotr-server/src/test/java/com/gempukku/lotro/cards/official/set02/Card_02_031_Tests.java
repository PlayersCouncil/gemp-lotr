package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_031_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blood1", "2_31");
					put("blood2", "2_31");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4; a maneuver ability
					// Pippin, Trollslayer: a [gondor] Hobbit companion, vitality 4; a skirmish ability used only on purpose
					put("pippin", "103_31");
					// Gondorian Merchant: a [gondor] ally, vitality 1; a maneuver ability used only on purpose
					put("merchant", "7_97");
					// Narsil, Blade of the Faithful: vitality +1 on a [gondor] Man, so the Merchant can exert; its
					// response answers a wound to its bearer only
					put("narsil", "9_34");
					put("legolas", "1_51");     // Legolas, Prince of Mirkwood: an [elven] archer companion

					// Morgul Hunter: [sauron] Orc, cost 5, vitality 4; strength +1 for each companion
					put("hunter", "1_256");
					// Orc Swordsman: [sauron] Orc, cost 3, vitality 3; strength +2 while he bears a weapon
					put("swordsman1", "3_98");
					put("swordsman2", "3_98");
					// Corpse Lights: a [sauron] minion who is no Orc (a Wraith), cost 3; a skirmish ability used only on purpose
					put("lights", "6_99");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc, cost 6, vitality 3, no text
					put("marksman", "1_176");   // Goblin Marksman: a [moria] archer, vitality 1, no other text
					// Úlairë Nertëa, Sanctified for Cruelty: "Each time you play a minion, you may exert it to play a
					// possession on it from your discard pile"
					put("nertea", "103_77");
					put("scimitar", "1_269");   // Orc Scimitar: "Bearer must be a [sauron] Orc."
					// Mallorn-trees: a Free Peoples condition in the support area; its fellowship ability is used only on purpose
					put("mallorn", "1_54");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BloodofNumenorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Blood of Númenor
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, exert a [gondor] companion. Plays to your support area.<br>Each [sauron] Orc comes into play exhausted.<br>Skip the archery phase. Discard this condition during the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("blood1");

		assertEquals("Blood of Númenor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ToPlayExertsAGondorCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var blood = scn.GetFreepsCard("blood1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(aragorn, pippin, legolas);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);
		scn.MoveCardsToHand(blood);

		scn.StartGame();
		assertAttachedTo(narsil, merchant);
		assertTrue(scn.FreepsPlayAvailable(blood));

		scn.FreepsPlayCard(blood);
		// A [gondor] companion of either race; not the [gondor] ally who could exert, nor a companion of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merchant, legolas, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(pippin);

		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertInZone(Zone.SUPPORT, blood);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlyGondorCompanionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blood = scn.GetFreepsCard("blood1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(aragorn, legolas);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);
		scn.MoveCardsToHand(blood);

		scn.StartGame();
		assertAttachedTo(narsil, merchant);
		// Vitality 4 with 3 wounds: exhausted.  The Merchant and Legolas could exert, but neither is a [gondor] companion.
		scn.AddWoundsToChar(aragorn, 3);
		scn.FreepsRefreshDecision();
		assertEquals(3, scn.GetWoundsOn(aragorn));

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(blood));
		assertInHand(blood);
	}

	@Test
	public void EachSauronOrcPlayedComesIntoPlayExhaustedAndNoOtherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blood = scn.GetFreepsCard("blood1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var hunter = scn.GetShadowCard("hunter");
		var swordsman1 = scn.GetShadowCard("swordsman1");
		var swordsman2 = scn.GetShadowCard("swordsman2");
		var lights = scn.GetShadowCard("lights");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(blood);
		scn.MoveCardsToHand(hunter, swordsman2, lights, troop);

		scn.StartGame();
		// A [sauron] Orc already in play as the condition comes in.
		scn.MoveMinionsToTable(swordsman1);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(swordsman1));
		// Aragorn, the one [gondor] companion, exerts unasked.
		scn.FreepsPlayCard(blood);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SUPPORT, blood);
		assertEquals(0, scn.GetWoundsOn(swordsman1));

		scn.SetTwilight(30);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Vitality 4 and vitality 3: each arrives with 1 vitality left.
		scn.ShadowPlayCard(hunter);
		assertEquals(3, scn.GetWoundsOn(hunter));
		scn.ShadowPlayCard(swordsman2);
		assertEquals(2, scn.GetWoundsOn(swordsman2));
		// A [sauron] minion who is no Orc, and an Orc of another culture, come in fresh.
		scn.ShadowPlayCard(lights);
		assertEquals(0, scn.GetWoundsOn(lights));
		scn.ShadowPlayCard(troop);
		assertEquals(0, scn.GetWoundsOn(troop));
		// The Orc that was already in play is untouched.
		assertEquals(0, scn.GetWoundsOn(swordsman1));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASauronOrcIsExhaustedBeforeAnythingAnswersItsPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blood = scn.GetFreepsCard("blood1");
		var nertea = scn.GetShadowCard("nertea");
		var swordsman = scn.GetShadowCard("swordsman1");
		var scimitar = scn.GetShadowCard("scimitar");
		scn.MoveCardsToSupportArea(blood);
		scn.MoveCardsToHand(swordsman);
		scn.MoveCardsToDiscard(scimitar);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, blood);
		assertInDiscard(scimitar);
		scn.MoveMinionsToTable(nertea);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(swordsman);
		// By the time his play is answered the Swordsman cannot exert, so Úlairë Nertëa has nothing to offer.
		assertInZone(Zone.SHADOW_CHARACTERS, swordsman);
		assertEquals(2, scn.GetWoundsOn(swordsman));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(nertea));
		assertInDiscard(scimitar);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithoutThisConditionASauronOrcComesInFreshAndUlaireNerteaAnswersItsPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var swordsman = scn.GetShadowCard("swordsman1");
		var scimitar = scn.GetShadowCard("scimitar");
		scn.MoveCardsToHand(swordsman);
		scn.MoveCardsToDiscard(scimitar);

		scn.StartGame();
		assertInDiscard(scimitar);
		scn.MoveMinionsToTable(nertea);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(swordsman);
		assertEquals(0, scn.GetWoundsOn(swordsman));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(nertea));
		scn.ShadowDeclineOptionalTrigger();
		assertInDiscard(scimitar);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheArcheryPhaseIsSkippedAndEachCopyIsDiscardedInTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blood1 = scn.GetFreepsCard("blood1");
		var blood2 = scn.GetFreepsCard("blood2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var mallorn = scn.GetFreepsCard("mallorn");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCompanionsToTable(aragorn, legolas);
		scn.MoveCardsToSupportArea(blood1, blood2, mallorn);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, blood1, blood2, mallorn);
		scn.MoveMinionsToTable(marksman);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.PassManeuverActions();

		// Archers on both sides, and the maneuver phase goes straight on to the assignment phase.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(marksman));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, marksman);
		scn.FreepsResolveSkirmish(aragorn);
		// Just before the regroup phase both copies are still in play.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertInZone(Zone.SUPPORT, blood1, blood2, mallorn);

		// Aragorn 8 against the Marksman's 7.
		scn.PassSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInDiscard(blood1, blood2);
		// Another condition of the same player is not discarded with them.
		assertInZone(Zone.SUPPORT, mallorn);
	}

	@Test
	public void WithoutThisConditionTheArcheryPhaseIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCompanionsToTable(aragorn, legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.PassManeuverActions();

		assertEquals(Phase.ARCHERY, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
