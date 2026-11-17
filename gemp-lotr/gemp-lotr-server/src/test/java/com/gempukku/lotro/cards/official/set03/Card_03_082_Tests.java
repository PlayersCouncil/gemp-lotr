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

public class Card_03_082_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("news", "3_82");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1
					put("forger", "3_56");      // Isengard Forger: [isengard] Orc, strength 6, vitality 2; a Shadow ability only
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, strength 8, damage +1 and no other text
					put("armory", "1_157");     // Uruk-hai Armory: [isengard] support-area condition; archery total -1 while an Uruk-hai is spotted
					put("lemenya", "1_232");    // Úlairë Lemenya: [wraith] Nazgûl, strength 9, vitality 2; weakens only bearers of a [wraith] condition
					put("khamul", "103_61");    // Úlairë Attëa, Khamûl of the East: [raider] Nazgûl, fierce; his bonuses need 3 burdens
					put("creeper", "8_75");     // Morgul Creeper: [wraith] Orc, no Nazgûl; its text needs 3 burdens

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Glóin: strength 6, vitality 3; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NewsofMordorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: News of Mordor
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Spot an [isengard] minion to make a Nazgûl <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("news");

		assertEquals("News of Mordor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedWithoutAnIsengardMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var news = scn.GetShadowCard("news");
		var lemenya = scn.GetShadowCard("lemenya");
		var sentry = scn.GetShadowCard("sentry");
		var armory = scn.GetShadowCard("armory");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(news);
		assertInHand(news);

		scn.StartGame();
		// An [isengard] card that is no minion, and minions of other cultures (an Uruk-hai among them).
		scn.MoveCardsToSupportArea(armory);
		scn.MoveMinionsToTable(lemenya, sentry);
		scn.SetTwilight(5);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, lemenya}, new PhysicalCardImpl[]{aragorn, sentry});
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.GetTwilight() >= 1);
		assertFalse(scn.ShadowPlayAvailable(news));
	}

	@Test
	public void SpottingAnUrukHaiMakesANazgulOfEitherCultureDamagePlus1UntilTheEndOfThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var news = scn.GetShadowCard("news");
		var savage = scn.GetShadowCard("savage");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var creeper = scn.GetShadowCard("creeper");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(news);
		assertInHand(news);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, lemenya, khamul, creeper);
		scn.SetTwilight(5);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// The assignment phase, next door to the skirmishes: not playable.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(news));
		scn.ShadowPass();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, lemenya}, new PhysicalCardImpl[]{aragorn, savage});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(news));
		assertEquals(0, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(khamul, Keyword.DAMAGE));
		scn.ShadowPlayCard(news);

		// The Nazgûl in the skirmish and the one in none, of either culture; not the [wraith] Orc nor the Uruk-hai.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(creeper, savage));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(lemenya);

		assertEquals(1, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(khamul, Keyword.DAMAGE));
		assertInDiscard(news);
		// Still so at the next decision of the same skirmish.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));

		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.PassSkirmishActions();
		// Lemenya 9 beats Gimli 6: one wound for the skirmish and one for damage +1.
		assertEquals(2, scn.GetWoundsOn(gimli));

		// The next skirmish is a phase of its own: the bonus is gone.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SpottingAnIsengardOrcAnUnassignedNazgulMayBeMadeDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var news = scn.GetShadowCard("news");
		var forger = scn.GetShadowCard("forger");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(news);
		assertInHand(news);

		scn.StartGame();
		// The Isengard Orc is the only [isengard] minion.
		scn.MoveMinionsToTable(forger, lemenya, khamul);
		scn.SetTwilight(5);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, lemenya}, new PhysicalCardImpl[]{aragorn, forger});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(news));
		scn.ShadowPlayCard(news);
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, khamul));
		assertEquals(0, scn.GetKeywordCount(khamul, Keyword.DAMAGE));
		scn.ShadowChooseCard(khamul);

		assertEquals(1, scn.GetKeywordCount(khamul, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoNazgulInPlayItIsStillPlayedAndMakesNobodyDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var news = scn.GetShadowCard("news");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(news);
		assertInHand(news);

		scn.StartGame();
		// The Uruk-hai is the only minion: no Nazgûl is anywhere in play.
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(5);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage);
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();

		// Uruk Savage prints damage +1 and is the one minion the event could wrongly reach.
		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		assertTrue(scn.ShadowPlayAvailable(news));
		scn.ShadowPlayCard(news);

		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		assertInDiscard(news);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
