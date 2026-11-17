package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_022_Tests
{

	// The Hobbit table: the Hobbit path (site 7 is Erebor 30_55, a mountain, where Smaug may stand) and Bilbo,
	// Reliable Companion 30_46 as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bard", "30_22");
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, vitality 2; an assignment ability only
					put("spider", "31_55");     // Wicked Spider: [spider] Spider, vitality 2, fierce; a skirmish ability only
					// Smaug, The Golden: [smaug] Dragon minion, vitality 5; discarded if not at a mountain, a regroup ability
					put("smaug", "30_66");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void BardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Bard, The Bowman
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Esgaroth
		 * Twilight Cost: 2
		 * Type: Ally
		 * Subtype: Man
		 * Home: 6H
		 * Strength: 6
		 * Vitality: 3
		 * Game Text: <b>Archer</b>.<br><b>Regroup:</b> Exert Bard to wound a minion. If that minion is Smaug, wound it again.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bard");

		assertEquals("Bard", card.getBlueprint().getTitle());
		assertEquals("The Bowman", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ESGAROTH, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.HOBBIT, 6)));
	}

	@Test
	public void RegroupActionExertsBardToWoundAMinionOnceUntilHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bard = scn.GetFreepsCard("bard");
		var bilbo = scn.GetRingBearer();
		var aggressor = scn.GetShadowCard("aggressor");
		var spider = scn.GetShadowCard("spider");
		var smaug = scn.GetShadowCard("smaug");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveAlliesToTable(bard);
		// On to Esgaroth, and from there a second move, in the regroup phase, to Erebor.
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		// Esgaroth makes no start-of-regroup offer: Bard, the one [esgaroth] ally, is in play (F-0804).
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		scn.PassRegroupActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();
		// At Erebor, a mountain: Smaug stands. The Spider is exhausted, so a wound kills it.
		assertEquals(7, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(aggressor, spider, smaug);
		scn.AddWoundsToChar(spider, 1);
		assertEquals(1, scn.GetWoundsOn(spider));
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(smaug));
		assertEquals(0, scn.GetWoundsOn(bard));

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.FreepsActionAvailable(bard));
		scn.FreepsUseCardAction(bard);
		assertEquals(1, scn.GetWoundsOn(bard));
		// Minions of three cultures and races, Smaug among them; not a companion, nor an ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aggressor, spider, smaug));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo, bard));
		scn.FreepsChooseCard(spider);
		// A wound, and not an exertion: the exhausted Spider is killed.
		assertInZone(Zone.DISCARD, spider);
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(smaug));

		scn.ShadowPass();
		scn.FreepsUseCardAction(bard);
		assertEquals(2, scn.GetWoundsOn(bard));
		scn.FreepsChooseCard(aggressor);
		// An Orc is wounded once, though Smaug is on the table: only Smaug chosen is wounded again.
		assertEquals(1, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(smaug));

		// Vitality 3 with 2 wounds: exhausted, so he can no longer exert.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(bard));
	}

	@Test
	public void SmaugChosenIsWoundedTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bard = scn.GetFreepsCard("bard");
		var aggressor = scn.GetShadowCard("aggressor");
		var smaug = scn.GetShadowCard("smaug");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveAlliesToTable(bard);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		scn.PassRegroupActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();
		assertEquals(7, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(aggressor, smaug);
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(smaug));
		assertEquals(0, scn.GetWoundsOn(bard));

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsUseCardAction(bard);
		assertEquals(1, scn.GetWoundsOn(bard));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aggressor, smaug));
		scn.FreepsChooseCard(smaug);

		// Wounded, and wounded again: vitality 5, so he lives.
		assertEquals(2, scn.GetWoundsOn(smaug));
		assertInZone(Zone.SHADOW_CHARACTERS, smaug);
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoMinionTheRegroupActionIsOfferedAndPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bard = scn.GetFreepsCard("bard");
		var bilbo = scn.GetRingBearer();

		scn.StartGame();
		scn.MoveAlliesToTable(bard);
		assertEquals(0, scn.GetWoundsOn(bard));

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.FreepsActionAvailable(bard));
		scn.FreepsUseCardAction(bard);

		assertEquals(1, scn.GetWoundsOn(bard));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
