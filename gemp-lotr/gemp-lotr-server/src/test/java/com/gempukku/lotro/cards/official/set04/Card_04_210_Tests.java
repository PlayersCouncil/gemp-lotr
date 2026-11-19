package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_210_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fighting", "4_210");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8; a maneuver ability used only on purpose
					put("merry", "1_303");      // Merry, From O'er the Brandywine: companion, strength 3, vitality 4; +2 only with a weapon
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1 and nothing else
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, strength 8, damage +1 and nothing else
					// Isengard Retainer: [isengard] Orc, strength 8, vitality 3; a regroup ability used only on purpose
					put("retainer", "3_57");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WeAretheFightingUrukhaiStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: We Are the Fighting Uruk-hai
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make an Uruk-hai strength +2 (and <b>damage +1</b> if you control a site).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fighting");

		assertEquals("We Are the Fighting Uruk-hai", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithOnlyTheFreePeoplesPlayerControllingASiteAnUrukHaiIsStrengthPlus2AndNoMoreDamageForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn, merry);

		var fighting = scn.GetShadowCard("fighting");
		var savage = scn.GetShadowCard("savage");
		var sentry = scn.GetShadowCard("sentry");
		var retainer = scn.GetShadowCard("retainer");

		scn.StartGame();
		scn.SkipToSite(2);
		// Player 1 took site 1 on player 2's turn: a site is controlled, but not by the event's player.
		scn.FreepsTakeControlOfSite();
		scn.MoveCardsToHand(fighting);
		assertInZone(Zone.HAND, fighting);
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage, sentry, retainer);
		assertEquals(1, scn.GetFreepsControlledSiteCount());
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertInZone(Zone.HAND, fighting);

		// A Skirmish event: not among the Shadow player's assignment actions.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(fighting));
		scn.ShadowPass();

		// The Sentry is left unassigned.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, savage},
				new PhysicalCardImpl[]{merry, retainer}
		);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(fighting));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(8, scn.GetStrength(sentry));
		assertEquals(8, scn.GetStrength(retainer));
		assertEquals(1, scn.GetKeywordCount(sentry, Keyword.DAMAGE));

		scn.ShadowPlayCard(fighting);
		// Uruk-hai of two cultures, the one skirmishing and one in no skirmish; an [isengard] Orc is not offered.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, sentry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(retainer, aragorn, merry));
		scn.ShadowChooseCard(sentry);

		// 8 + 2, and the printed damage +1 alone: the site the Free Peoples player controls is not "a site you control".
		assertInZone(Zone.DISCARD, fighting);
		assertEquals(10, scn.GetStrength(sentry));
		assertEquals(1, scn.GetKeywordCount(sentry, Keyword.DAMAGE));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(8, scn.GetStrength(retainer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Still in force at the next decision of this skirmish.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(10, scn.GetStrength(sentry));

		// The first skirmish is resolved; Merry's is next.
		scn.ShadowPass();
		scn.FreepsResolveSkirmish(merry);

		// Gone in the next skirmish, which is another phase.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(sentry));
	}

	@Test
	public void WhileYouControlASiteTheUrukHaiIsAlsoDamagePlus1ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn, merry);

		var fighting = scn.GetShadowCard("fighting");
		var savage = scn.GetShadowCard("savage");
		var sentry = scn.GetShadowCard("sentry");
		var retainer = scn.GetShadowCard("retainer");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		scn.MoveCardsToHand(fighting);
		assertInZone(Zone.HAND, fighting);
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage, sentry, retainer);
		assertEquals(0, scn.GetFreepsControlledSiteCount());
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertInZone(Zone.HAND, fighting);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, savage},
				new PhysicalCardImpl[]{merry, retainer}
		);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(8, scn.GetStrength(sentry));
		assertEquals(1, scn.GetKeywordCount(sentry, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		scn.ShadowPlayCard(fighting);
		scn.ShadowChooseCard(sentry);

		// The chosen Uruk-hai is 8 + 2 and damage 1 + 1; the other keeps its own damage +1.
		assertInZone(Zone.DISCARD, fighting);
		assertEquals(10, scn.GetStrength(sentry));
		assertEquals(2, scn.GetKeywordCount(sentry, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Still in force at the next decision of this skirmish.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(2, scn.GetKeywordCount(sentry, Keyword.DAMAGE));

		scn.ShadowPass();
		scn.FreepsResolveSkirmish(merry);

		// Gone in the next skirmish, which is another phase.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetKeywordCount(sentry, Keyword.DAMAGE));
		assertEquals(8, scn.GetStrength(sentry));
	}
}
