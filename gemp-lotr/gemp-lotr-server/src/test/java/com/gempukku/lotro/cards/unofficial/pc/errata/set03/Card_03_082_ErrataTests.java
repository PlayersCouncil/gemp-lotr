package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_082_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("news", "53_82");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1
					put("forger", "3_56");      // Isengard Forger: [isengard] Orc, strength 6, vitality 2; a Shadow ability only
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, vitality 2, damage +1 and no other text
					put("lemenya", "1_232");    // Ulaire Lemenya: [wraith] Nazgul, strength 9, vitality 2; weakens bearers of [wraith] conditions
					put("attea", "103_61");     // Ulaire Attea, Khamul of the East: [raider] Nazgul, vitality 3; fierce, the rest needs 3 burdens
					put("creeper", "8_75");     // Morgul Creeper: [wraith] Orc, vitality 2; its text needs 3 burdens

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3, damage +1
					put("guardian", "4_76");    // Lorien Guardian: "Regroup: Exert this companion to make a Shadow player wound a minion."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NewsOfMordorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: News of Mordor
		 * Unique: False
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Choose one: exert an [isengard] minion to make a Nazgul damage +1 until the regroup phase,
		 * or exert a Nazgul to make an [isengard] minion unable to take wounds until the regroup phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("news");

		assertEquals("News of Mordor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void FirstChoiceExertsAnIsengardMinionToMakeANazgulOfAnyCultureDamagePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var news = scn.GetShadowCard("news");
		var savage = scn.GetShadowCard("savage");
		var forger = scn.GetShadowCard("forger");
		var sentry = scn.GetShadowCard("sentry");
		var lemenya = scn.GetShadowCard("lemenya");
		var attea = scn.GetShadowCard("attea");
		var creeper = scn.GetShadowCard("creeper");
		scn.MoveCardsToHand(news);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, forger, sentry, lemenya, attea, creeper);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		assertTrue(scn.ShadowPlayAvailable(news));
		scn.ShadowPlayCard(news);
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		scn.ShadowChooseOption("damage +1");

		// The exertion: either [isengard] minion, whatever its race; not the [sauron] Uruk-hai, nor a Nazgul.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, forger));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sentry, lemenya, attea, creeper));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(forger);
		assertEquals(1, scn.GetWoundsOn(forger));
		assertEquals(0, scn.GetWoundsOn(savage));

		// The Nazgul: the [raider] one as well as the [wraith] one; not the [wraith] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, attea));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(creeper, savage, forger, sentry));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(0, scn.GetKeywordCount(attea, Keyword.DAMAGE));
		scn.ShadowChooseCard(attea);

		assertEquals(1, scn.GetKeywordCount(attea, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		assertEquals(twilight - 1, scn.GetTwilight());
		assertInDiscard(news);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void SecondChoiceExertsANazgulOfAnyCultureToProtectAnIsengardMinionOfAnyRace() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var news = scn.GetShadowCard("news");
		var savage = scn.GetShadowCard("savage");
		var forger = scn.GetShadowCard("forger");
		var sentry = scn.GetShadowCard("sentry");
		var lemenya = scn.GetShadowCard("lemenya");
		var attea = scn.GetShadowCard("attea");
		var creeper = scn.GetShadowCard("creeper");
		scn.MoveCardsToHand(news);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, forger, sentry, lemenya, attea, creeper);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		scn.ShadowPlayCard(news);
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		scn.ShadowChooseOption("wounds");

		// The exertion: either Nazgul, [wraith] or [raider]; not the [wraith] Orc, nor an [isengard] minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, attea));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(creeper, savage, forger, sentry));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(attea);
		assertEquals(1, scn.GetWoundsOn(attea));
		assertEquals(0, scn.GetWoundsOn(lemenya));

		// The minion protected: the [isengard] Uruk-hai or the [isengard] Orc; not the [sauron] Uruk-hai.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, forger));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sentry, creeper, lemenya, attea));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(savage);

		assertEquals(twilight - 1, scn.GetTwilight());
		assertInDiscard(news);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void NotPlayableWhenNeitherAnIsengardMinionNorANazgulCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var news = scn.GetShadowCard("news");
		var savage = scn.GetShadowCard("savage");
		var sentry = scn.GetShadowCard("sentry");
		var lemenya = scn.GetShadowCard("lemenya");
		var creeper = scn.GetShadowCard("creeper");
		scn.MoveCardsToHand(news);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, sentry, lemenya, creeper);
		// The one [isengard] minion and the one Nazgul exhausted; the [sauron] Uruk-hai and the [wraith] Orc are fresh.
		scn.AddWoundsToChar(savage, 2);
		scn.AddWoundsToChar(lemenya, 1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(news));

		scn.RemoveWoundsFromChar(lemenya, 1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(news));
	}

	@Test
	public void WithOnlyANazgulAbleToExertTheSecondChoiceIsTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var news = scn.GetShadowCard("news");
		var savage = scn.GetShadowCard("savage");
		var sentry = scn.GetShadowCard("sentry");
		var lemenya = scn.GetShadowCard("lemenya");
		var attea = scn.GetShadowCard("attea");
		var creeper = scn.GetShadowCard("creeper");
		scn.MoveCardsToHand(news);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, sentry, lemenya, attea, creeper);
		// The one [isengard] minion is exhausted; the [sauron] Uruk-hai beside it is fresh.
		scn.AddWoundsToChar(savage, 2);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(news));
		scn.ShadowPlayCard(news);
		// No choice between the two is put: the Nazgul to exert is asked for at once.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, attea));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, sentry, creeper));
		scn.ShadowChooseCard(lemenya);

		// The exhausted Uruk Savage is the one [isengard] minion, so it is protected without a choice.
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(attea));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(attea, Keyword.DAMAGE));
		assertInDiscard(news);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoNazgulAbleToExertTheFirstChoiceIsTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var news = scn.GetShadowCard("news");
		var savage = scn.GetShadowCard("savage");
		var forger = scn.GetShadowCard("forger");
		var lemenya = scn.GetShadowCard("lemenya");
		var creeper = scn.GetShadowCard("creeper");
		scn.MoveCardsToHand(news);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, forger, lemenya, creeper);
		scn.AddWoundsToChar(lemenya, 1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(news));
		scn.ShadowPlayCard(news);
		// No choice between the two is put: the [isengard] minion to exert is asked for at once.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, forger));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(lemenya, creeper));
		scn.ShadowChooseCard(savage);

		// The exhausted Lemenya is the one Nazgul, and takes the bonus without a choice.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(1, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		assertInDiscard(news);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheDamageBonusLastsIntoALaterSkirmishAndEndsAtTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var news = scn.GetShadowCard("news");
		var savage = scn.GetShadowCard("savage");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(news);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, lemenya);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowPlayCard(news);
		scn.ShadowChooseOption("damage +1");
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{gimli, lemenya});

		// Aragorn 8 against Uruk Savage 5: the first skirmish is resolved before Lemenya's.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(savage));

		scn.FreepsResolveSkirmish(gimli);
		assertEquals(1, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.PassSkirmishActions();
		// Lemenya 9 beats Gimli 6: one wound for the skirmish and one for damage +1.
		assertEquals(2, scn.GetWoundsOn(gimli));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
	}

	@Test
	public void AnIsengardMinionMadeUnableToTakeWoundsTakesNoneInALaterSkirmishAndCanBeWoundedAtTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var news = scn.GetShadowCard("news");
		var savage = scn.GetShadowCard("savage");
		var forger = scn.GetShadowCard("forger");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var guardian = scn.GetFreepsCard("guardian");
		scn.MoveCompanionsToTable(aragorn, gimli, guardian);
		scn.MoveCardsToHand(news);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, forger, lemenya);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowPlayCard(news);
		scn.ShadowChooseOption("wounds");
		assertEquals(1, scn.GetWoundsOn(lemenya));
		scn.ShadowChooseCard(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, forger}, new PhysicalCardImpl[]{gimli, savage});
		scn.ShadowDeclineAssignments();

		// Aragorn 8 beats Isengard Forger 6, the [isengard] minion that was not chosen: it is wounded.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(forger));

		// Gimli 6 beats Uruk Savage 5 (not overwhelmed): two wounds with damage +1, and Savage takes none.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(0, scn.GetWoundsOn(savage));
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(savage));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsUseCardAction(guardian);
		assertEquals(1, scn.GetWoundsOn(guardian));
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, forger, lemenya));
		scn.ShadowChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
