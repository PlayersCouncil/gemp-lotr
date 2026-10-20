package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_128_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cry", "1_128");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8, vitality 4; a maneuver ability used only on purpose
					put("guard1", "1_7");       // Dwarf Guard: companion, strength 4, vitality 2, no text in play

					put("uruk", "1_151");       // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1
					put("uruk2", "1_151");      // a second Uruk Savage, left unassigned
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, strength 8, vitality 2, damage +1
					put("forger", "3_56");      // Isengard Forger: [isengard] Orc that is no Uruk-hai, strength 6; a Shadow ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LurtzsBattleCryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Lurtz's Battle Cry
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make an Uruk-hai strength -1 and <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("cry");

		assertEquals("Lurtz's Battle Cry", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesTheChosenUrukHaiOfEitherCultureStrengthMinus1AndDamagePlus1AndNoOtherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cry = scn.GetShadowCard("cry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard1 = scn.GetFreepsCard("guard1");
		var uruk = scn.GetShadowCard("uruk");
		var uruk2 = scn.GetShadowCard("uruk2");
		var sentry = scn.GetShadowCard("sentry");
		var forger = scn.GetShadowCard("forger");
		scn.MoveCompanionsToTable(aragorn, guard1);
		scn.MoveCardsToHand(cry);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk, uruk2, sentry, forger);
		scn.SetTwilight(10);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, uruk}, new PhysicalCardImpl[]{guard1, sentry});
		// The Shadow player may assign the second Savage and the Forger; he does not.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(5, scn.GetStrength(uruk));
		assertEquals(1, scn.GetKeywordCount(uruk, Keyword.DAMAGE));
		assertTrue(scn.ShadowPlayAvailable(cry));

		scn.ShadowPlayCard(cry);
		// The Savage in the skirmish, the Sentry assigned to a skirmish not yet begun, the second Savage assigned
		// to nothing; not the [isengard] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(uruk, sentry, uruk2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(forger));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(uruk);

		// 5 - 1, and damage +1 on top of its own
		assertEquals(4, scn.GetStrength(uruk));
		assertEquals(2, scn.GetKeywordCount(uruk, Keyword.DAMAGE));
		assertEquals(5, scn.GetStrength(uruk2));
		assertEquals(1, scn.GetKeywordCount(uruk2, Keyword.DAMAGE));
		assertEquals(8, scn.GetStrength(sentry));
		assertEquals(1, scn.GetKeywordCount(sentry, Keyword.DAMAGE));
		assertEquals(6, scn.GetStrength(forger));
		assertEquals(0, scn.GetKeywordCount(forger, Keyword.DAMAGE));
		assertInDiscard(cry);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotPlayableInTheAssignmentPhaseAndItLastsOnlyForTheSkirmishItIsPlayedIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cry = scn.GetShadowCard("cry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard1 = scn.GetFreepsCard("guard1");
		var uruk = scn.GetShadowCard("uruk");
		var sentry = scn.GetShadowCard("sentry");
		scn.MoveCompanionsToTable(aragorn, guard1);
		scn.MoveCardsToHand(cry);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk, sentry);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		// Not a skirmish phase: not playable.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(cry));

		scn.ShadowPass();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, uruk}, new PhysicalCardImpl[]{guard1, sentry});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(8, scn.GetStrength(sentry));
		assertEquals(1, scn.GetKeywordCount(sentry, Keyword.DAMAGE));
		assertTrue(scn.ShadowPlayAvailable(cry));

		scn.ShadowPlayCard(cry);
		// Played in Aragorn's skirmish, on the Sentry whose skirmish comes next.
		scn.ShadowChooseCard(sentry);
		// 8 - 1, and damage +1 on top of its own
		assertEquals(7, scn.GetStrength(sentry));
		assertEquals(2, scn.GetKeywordCount(sentry, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Aragorn, 8, beats the Savage, 5, which lives with 1 wound.
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(uruk));

		// The next skirmish is another skirmish phase: both are gone.
		scn.FreepsResolveSkirmish(guard1);
		assertEquals(8, scn.GetStrength(sentry));
		assertEquals(1, scn.GetKeywordCount(sentry, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoUrukHaiItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cry = scn.GetShadowCard("cry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var forger = scn.GetShadowCard("forger");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(cry);

		scn.StartGame();
		scn.MoveMinionsToTable(forger);
		scn.SetTwilight(10);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, forger);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(6, scn.GetStrength(forger));
		assertEquals(0, scn.GetKeywordCount(forger, Keyword.DAMAGE));
		// An event is offered whatever its effect will find (CR 4.2 "effect").
		assertTrue(scn.ShadowPlayAvailable(cry));
		scn.ShadowPlayCard(cry);

		// The [isengard] Orc in the skirmish is no Uruk-hai.
		assertEquals(6, scn.GetStrength(forger));
		assertEquals(0, scn.GetKeywordCount(forger, Keyword.DAMAGE));
		assertInDiscard(cry);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
